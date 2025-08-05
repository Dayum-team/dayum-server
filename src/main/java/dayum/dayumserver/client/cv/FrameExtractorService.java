package dayum.dayumserver.client.cv;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.CommonExceptionCode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;

@Slf4j
@Component
@RequiredArgsConstructor
public class FrameExtractorService {

  // ===== 텍스트 밴드 후보 탐지 =====
  private static final int MAX_BANDS = 3;
  private static final double BAND_MIN_H_RATIO = 0.06;
  private static final double BAND_MAX_H_RATIO = 0.35;
  private static final double BAND_PAD_RATIO = 0.03;
  private static final double ROW_PEAK_REL_THRESH = 0.35;

  // ===== 변화 판정(히스테리시스) : 마스크 + 보조 MAD =====
  private static final double XOR_UPPER = 0.24;
  private static final double XOR_LOWER = 0.12;
  private static final int DHASH_UPPER = 8;
  private static final int DHASH_LOWER = 3;

  private static final double BAND_MAD_UPPER = 0.13;
  private static final double BAND_MAD_LOWER = 0.07;

  private static final int REQUIRED_DIFF_STREAK = 2;
  private static final int REQUIRED_SAME_STREAK = 3;
  private static final int MIN_SAVE_GAP_FRAMES = 8; // ≈0.33s @24fps
  private static final int REARM_TIMEOUT_FRAMES = 48; // ≈2s : 동일판정 없이도 재무장

  // ===== 자막 "등장 감지 + 최적 프레임 픽킹" =====
  private static final double DENSITY_RISE = 0.006; // EMA 대비 절대 상승량
  private static final double DENSITY_ABS = 0.015; // 자막 존재로 볼 최소 밀도
  private static final int PICK_WINDOW_FRAMES = 6; // 등장 후 이 윈도우에서 최적 1장 저장
  private static final double EMA_ALPHA = 0.2; // 밀도 EMA

  // 정규화 크기
  private static final Size MASK_NORM_SIZE = new Size(256, 64);
  private static final Size BAND_NORM_SIZE = new Size(320, 64);

  // 탐지용 축소 크기(첫 프레임 비율에 맞춰 고정)
  private Size detectSizeRef = null;

  // 디버그 저장
  private static final boolean DEBUG_SAVE_MASKS = false;
  private static final boolean DEBUG_SAVE_TEXTMAP = false;
  private static final String DEBUG_MASK_PREFIX = "mask-";
  private static final String DEBUG_TEXTMAP_PREFIX = "textmap-";

  // 파일 네이밍
  private static final String FRAME_FILE_PREFIX = "frame-";
  private static final String FRAME_FILE_EXTENSION = "png";

  private final Java2DFrameConverter java2D = new Java2DFrameConverter();
  private final OpenCVFrameConverter.ToMat toMat = new OpenCVFrameConverter.ToMat();

  // ===== 상태 =====
  private Mat stableMask = null;
  private long stableHash = 0L;
  private Mat stableBandNorm = null;

  private boolean armed = true;
  private int diffStreak = 0;
  private int sameStreak = 0;

  // density EMA & pending picker
  private double densityEma = 0.0;
  private boolean pending = false;
  private int pendingLeft = 0;
  private Candidate best = null;
  private long lastSavedFrame = -MIN_SAVE_GAP_FRAMES;

  private static class Candidate {
    Mat srcBgr;
    Mat mask;
    Mat bandNorm;
    double density;
    double sharpness;
    long frameIndex;
  }

  public List<File> extractFrames(File videoFile, Path workingDir) {
    List<File> frameFiles = new ArrayList<>();

    try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile)) {
      grabber.start();

      Frame frame;
      int savedIndex = 0;

      // 초기화
      detectSizeRef = null;
      stableMask = null;
      stableHash = 0L;
      stableBandNorm = null;
      armed = true;
      diffStreak = 0;
      sameStreak = 0;
      densityEma = 0.0;
      pending = false;
      pendingLeft = 0;
      best = null;
      long frameIndex = -1;
      lastSavedFrame = -MIN_SAVE_GAP_FRAMES;

      while ((frame = grabber.grabImage()) != null) {
        frameIndex++;

        Mat src = toMat.convert(frame);
        if (src == null || src.empty()) continue;

        if (detectSizeRef == null) detectSizeRef = chooseDetectSize(src);

        // 1) 축소 BGR / GRAY
        Mat smallBgr = resizeToBgr(src, detectSizeRef);
        Mat graySmall = new Mat();
        cvtColor(smallBgr, graySmall, COLOR_BGR2GRAY);

        // 2) 자막 전용 맵(흰 본문 + 검은 외곽선) 생성 → 배경 활자 억제
        Mat captionMap = makeCaptionMapSmall(smallBgr);
        if (DEBUG_SAVE_TEXTMAP && frameIndex % 10 == 0) {
          saveDebugBinary(workingDir, captionMap, DEBUG_TEXTMAP_PREFIX, frameIndex);
        }

        // 3) 자막맵의 세로 히스토그램으로 밴드 탐지 (없으면 하단 45% fallback)
        List<Rect> curBands = findBandsFromTextMap(captionMap);
        if (curBands.isEmpty()) {
          int h = graySmall.rows(), w = graySmall.cols();
          int y0 = (int) Math.round(h * 0.55);
          curBands = List.of(new Rect(0, y0, w, h - y0));
        } else if (curBands.size() > 1) {
          // 자막스러움(밀도)으로 최적 밴드 1개만 선택
          Rect bestBand = curBands.get(0);
          double bestD = -1;
          for (Rect r : curBands) {
            Mat bandMask = new Mat(captionMap, r);
            double d = countNonZero(bandMask) / (double) (bandMask.rows() * bandMask.cols());
            if (d > bestD) {
              bestD = d;
              bestBand = r;
            }
          }
          curBands = List.of(bestBand);
        }

        // 4) 밴드 union을 원본 좌표로 변환해 ROI 추출
        Rect unionSmall = unionBands(curBands, graySmall);
        Mat roiHiRes = cropFromOriginal(src, detectSizeRef, unionSmall);

        // 5) 현재 프레임의 자막 마스크/밴드정규화 생성
        Mat curMask = buildSubtitleMask(roiHiRes); // HSV 기반
        Mat curBandNorm = buildBandNorm(graySmall, curBands);

        // 자막 픽셀 밀도/선명도
        double density = countNonZero(curMask) / (double) (curMask.rows() * curMask.cols());
        double sharp = focusMeasure(roiHiRes);

        if (stableMask == null) {
          stableMask = curMask.clone();
          stableHash = dhash64(stableMask);
          stableBandNorm = curBandNorm.clone();
          densityEma = density;
          continue;
        }

        // 6) 차이 신호
        Diff m = diffMask(stableMask, stableHash, curMask);
        double bandMad = meanAbsDiff(stableBandNorm, curBandNorm);

        boolean isDifferent =
            (m.xorRatio >= XOR_UPPER) || (m.hamming >= DHASH_UPPER) || (bandMad >= BAND_MAD_UPPER);
        boolean isSame =
            (m.xorRatio <= XOR_LOWER) && (m.hamming <= DHASH_LOWER) && (bandMad <= BAND_MAD_LOWER);

        if (isDifferent) {
          diffStreak++;
          sameStreak = 0;
        } else if (isSame) {
          sameStreak++;
          diffStreak = 0;
        } else {
          diffStreak = 0;
          sameStreak = 0;
        }

        if (isSame) {
          stableMask = curMask.clone();
          stableHash = m.curHash;
          stableBandNorm = curBandNorm.clone();
        }

        // 7) 자막 "등장" 감지 → 최적 프레임 픽킹 윈도우
        densityEma = (1.0 - EMA_ALPHA) * densityEma + EMA_ALPHA * density;
        boolean onset =
            (density - densityEma >= DENSITY_RISE)
                && density >= DENSITY_ABS
                && (frameIndex - lastSavedFrame) >= MIN_SAVE_GAP_FRAMES;

        if (armed && !pending && onset) {
          pending = true;
          pendingLeft = PICK_WINDOW_FRAMES;
          best = makeCandidate(src, curMask, curBandNorm, density, sharp, frameIndex);
        }

        if (pending) {
          // 더 좋은 후보(밀도 우선, 동률이면 선명도)로 갱신
          if (density > best.density
              || (Math.abs(density - best.density) < 1e-6 && sharp > best.sharpness)) {
            best = makeCandidate(src, curMask, curBandNorm, density, sharp, frameIndex);
          }
          pendingLeft--;
          if (pendingLeft <= 0) {
            // 저장
            File out = saveMat(workingDir, best.srcBgr, savedIndex++);
            frameFiles.add(out);
            lastSavedFrame = best.frameIndex;

            // 대표 업데이트 + 상태 리셋
            stableMask = best.mask.clone();
            stableHash = dhash64(stableMask);
            stableBandNorm = best.bandNorm.clone();

            pending = false;
            best = null;
            armed = false;
            diffStreak = 0;
            sameStreak = 0;
          }
        }

        // 8) 완전 다른 자막일 때는 기존 트리거도 병행
        if (!pending
            && armed
            && diffStreak >= REQUIRED_DIFF_STREAK
            && (frameIndex - lastSavedFrame) >= MIN_SAVE_GAP_FRAMES) {

          File out = saveMat(workingDir, src, savedIndex++);
          frameFiles.add(out);
          lastSavedFrame = frameIndex;

          armed = false;
          diffStreak = 0;
          sameStreak = 0;
          stableMask = curMask.clone();
          stableHash = dhash64(stableMask);
          stableBandNorm = curBandNorm.clone();
        }

        // 9) 재무장
        if (!armed
            && (sameStreak >= REQUIRED_SAME_STREAK
                || (frameIndex - lastSavedFrame) >= REARM_TIMEOUT_FRAMES)) {
          armed = true;
          sameStreak = 0;
        }

        if (DEBUG_SAVE_MASKS && frameIndex % 10 == 0) {
          saveDebugBinary(
              workingDir,
              curMask,
              DEBUG_MASK_PREFIX,
              frameIndex,
              String.format("_x%.2f_h%d_m%.2f_d%.3f.png", m.xorRatio, m.hamming, bandMad, density));
        }
      }

      return frameFiles;

    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.FRAME_EXTRACTION_FAILED);
    }
  }

  // ===== 후보 생성/저장/선명도 =====
  private Candidate makeCandidate(
      Mat srcBgr, Mat mask, Mat bandNorm, double density, double sharp, long fi) {
    Candidate c = new Candidate();
    c.srcBgr = srcBgr.clone();
    c.mask = mask.clone();
    c.bandNorm = bandNorm.clone();
    c.density = density;
    c.sharpness = sharp;
    c.frameIndex = fi;
    return c;
  }

  /** 라플라시안 기반 간단 선명도(값이 클수록 선명) */
  private double focusMeasure(Mat bgrOrGray) {
    Mat g = new Mat();
    if (bgrOrGray.channels() == 1) g = bgrOrGray;
    else {
      cvtColor(bgrOrGray, g, COLOR_BGR2GRAY);
    }
    Mat lap = new Mat();
    Laplacian(g, lap, CV_16S);
    Mat absLap = new Mat();
    convertScaleAbs(lap, absLap);
    return mean(absLap).get(0);
  }

  private File saveMat(Path dir, Mat bgr, int index) throws IOException {
    OpenCVFrameConverter.ToMat c = new OpenCVFrameConverter.ToMat();
    Frame f = c.convert(bgr);
    BufferedImage img = java2D.convert(f);
    File out = dir.resolve(FRAME_FILE_PREFIX + index + "." + FRAME_FILE_EXTENSION).toFile();
    ImageIO.write(img, FRAME_FILE_EXTENSION, out);
    return out;
  }

  // ===== 텍스트맵/마스크/밴드 =====
  private Mat resizeToBgr(Mat src, Size size) {
    Mat small = new Mat();
    resize(src, small, size, 0, 0, INTER_AREA);
    return small;
  }

  private Mat resizeToGray(Mat src, Size size) {
    Mat small = new Mat();
    resize(src, small, size, 0, 0, INTER_AREA);
    Mat gray = new Mat();
    cvtColor(small, gray, COLOR_BGR2GRAY);
    return gray;
  }

  /** 축소 프레임에서 자막스러운 픽셀(흰 본문 + 검은 외곽선)만 강조 */
  private Mat makeCaptionMapSmall(Mat bgrSmall) {
    Mat hsv = new Mat();
    cvtColor(bgrSmall, hsv, COLOR_BGR2HSV);
    MatVector channels = new MatVector();
    split(hsv, channels);
    Mat S = channels.get(1), V = channels.get(2);

    // 흰 본문: 밝고(>210) 채도 낮음(<80)
    Mat white = new Mat();
    threshold(V, white, 210, 255, THRESH_BINARY);
    Mat lowSat = new Mat();
    threshold(S, lowSat, 80, 255, THRESH_BINARY_INV);
    bitwise_and(white, lowSat, white);

    // 검은 픽셀: V < 60 (필요시 70~80으로 완화)
    Mat black = new Mat();
    threshold(V, black, 60, 255, THRESH_BINARY_INV);

    // 링(흰 본문 주변)
    Mat dil = new Mat();
    Mat ero = new Mat();
    Mat k3 = getStructuringElement(MORPH_RECT, new Size(3, 3));
    dilate(white, dil, k3);
    erode(white, ero, k3);
    Mat ring = new Mat();
    subtract(dil, ero, ring);

    // 외곽선 = 링 ∩ black
    Mat outline = new Mat();
    bitwise_and(ring, black, outline);

    // 최종 자막맵
    Mat caption = new Mat();
    bitwise_or(white, outline, caption);

    // 잡음 정리
    Mat k2 = getStructuringElement(MORPH_RECT, new Size(2, 2));
    morphologyEx(caption, caption, MORPH_OPEN, k2);
    morphologyEx(caption, caption, MORPH_CLOSE, k2);
    return caption;
  }

  /** ROI(원본)에서도 동일한 규칙으로 마스크 생성 → MASK_NORM_SIZE로 정규화 */
  private Mat buildSubtitleMask(Mat roiBgr) {
    Mat hsv = new Mat();
    cvtColor(roiBgr, hsv, COLOR_BGR2HSV);
    MatVector channels = new MatVector();
    split(hsv, channels);
    Mat S = channels.get(1), V = channels.get(2);

    Mat white = new Mat();
    threshold(V, white, 210, 255, THRESH_BINARY);
    Mat lowSat = new Mat();
    threshold(S, lowSat, 80, 255, THRESH_BINARY_INV);
    bitwise_and(white, lowSat, white);

    Mat black = new Mat();
    threshold(V, black, 60, 255, THRESH_BINARY_INV);

    Mat dil = new Mat();
    Mat ero = new Mat();
    Mat k3 = getStructuringElement(MORPH_RECT, new Size(3, 3));
    dilate(white, dil, k3);
    erode(white, ero, k3);
    Mat ring = new Mat();
    subtract(dil, ero, ring);

    Mat outline = new Mat();
    bitwise_and(ring, black, outline);

    Mat mask = new Mat();
    bitwise_or(white, outline, mask);

    Mat k2 = getStructuringElement(MORPH_RECT, new Size(2, 2));
    morphologyEx(mask, mask, MORPH_OPEN, k2);
    morphologyEx(mask, mask, MORPH_CLOSE, k2);

    Mat maskNorm = new Mat();
    resize(mask, maskNorm, MASK_NORM_SIZE, 0, 0, INTER_AREA);
    if (maskNorm.type() != CV_8U) {
      Mat t = new Mat();
      maskNorm.convertTo(t, CV_8U);
      maskNorm = t;
    }
    return maskNorm;
  }

  private List<Rect> findBandsFromTextMap(Mat textMap) {
    int h = textMap.rows(), w = textMap.cols();
    double[] rowSum = new double[h];
    for (int y = 0; y < h; y++) rowSum[y] = countNonZero(textMap.row(y));
    smooth1D(rowSum, 11);
    double maxVal = Arrays.stream(rowSum).max().orElse(1.0);
    double thresh = Math.max(5, maxVal * ROW_PEAK_REL_THRESH);
    int minBandH = (int) Math.round(h * BAND_MIN_H_RATIO);
    int maxBandH = (int) Math.round(h * BAND_MAX_H_RATIO);
    int pad = (int) Math.max(1, Math.round(h * BAND_PAD_RATIO));
    List<Rect> bands = new ArrayList<>();
    int y = 0;
    while (y < h) {
      while (y < h && rowSum[y] < thresh) y++;
      if (y >= h) break;
      int y0 = y;
      while (y < h && rowSum[y] >= thresh) y++;
      int y1 = y - 1;
      int bandH = (y1 - y0 + 1);
      if (bandH < minBandH) {
        int c = (y0 + y1) / 2;
        y0 = Math.max(0, c - minBandH / 2);
        y1 = Math.min(h - 1, y0 + minBandH - 1);
      } else if (bandH > maxBandH) {
        int c = (y0 + y1) / 2;
        y0 = Math.max(0, c - maxBandH / 2);
        y1 = Math.min(h - 1, y0 + maxBandH - 1);
      }
      int py0 = Math.max(0, y0 - pad);
      int py1 = Math.min(h - 1, y1 + pad);
      bands.add(new Rect(0, py0, w, py1 - py0 + 1));
    }
    bands = nonMaxSuppressBands(bands, 0.6, MAX_BANDS);
    bands.sort(Comparator.comparingInt(Rect::y));
    return bands;
  }

  private Mat buildBandNorm(Mat graySmall, List<Rect> bands) {
    Rect u = unionBands(bands, graySmall);
    Mat roi = new Mat(graySmall, u);
    Mat blur = new Mat();
    GaussianBlur(roi, blur, new Size(7, 7), 0);
    Mat norm = new Mat();
    resize(blur, norm, BAND_NORM_SIZE, 0, 0, INTER_AREA);
    return norm;
  }

  // ===== 공통 유틸 =====
  private static class Diff {
    double xorRatio;
    int hamming;
    long curHash;
  }

  private Diff diffMask(Mat refMask, long refHash, Mat curMask) {
    Mat xo = new Mat();
    Mat oo = new Mat();
    bitwise_xor(refMask, curMask, xo);
    bitwise_or(refMask, curMask, oo);
    int diffCnt = countNonZero(xo);
    int unionCnt = Math.max(1, countNonZero(oo));
    double ratio = diffCnt / (double) unionCnt;
    long ch = dhash64(curMask);
    int hd = Long.bitCount(refHash ^ ch);
    Diff d = new Diff();
    d.xorRatio = ratio;
    d.hamming = hd;
    d.curHash = ch;
    return d;
  }

  private long dhash64(Mat grayBin) {
    Mat small = new Mat();
    resize(grayBin, small, new Size(9, 8), 0, 0, INTER_AREA);
    if (small.type() != CV_8U) {
      Mat t = new Mat();
      small.convertTo(t, CV_8U);
      small = t;
    }
    long hash = 0L;
    for (int y = 0; y < 8; y++)
      for (int x = 0; x < 8; x++) {
        int p1 = small.ptr(y, x).get() & 0xFF;
        int p2 = small.ptr(y, x + 1).get() & 0xFF;
        if (p1 > p2) hash |= (1L << (y * 8 + x));
      }
    return hash;
  }

  private double meanAbsDiff(Mat a, Mat b) {
    if (a == null || b == null || a.empty() || b.empty()) return 0.0;
    Mat a1 = a, b1 = b;
    if (a1.channels() != 1) {
      Mat t = new Mat();
      cvtColor(a1, t, COLOR_BGR2GRAY);
      a1 = t;
    }
    if (b1.channels() != 1) {
      Mat t = new Mat();
      cvtColor(b1, t, COLOR_BGR2GRAY);
      b1 = t;
    }
    if (a1.rows() != b1.rows() || a1.cols() != b1.cols()) {
      Mat r = new Mat();
      resize(b1, r, a1.size(), 0, 0, INTER_AREA);
      b1 = r;
    }
    if (a1.type() != b1.type()) {
      Mat t = new Mat();
      b1.convertTo(t, a1.type());
      b1 = t;
    }
    Mat diff = new Mat();
    absdiff(a1, b1, diff);
    return mean(diff).get(0) / 255.0;
  }

  private Size chooseDetectSize(Mat src) {
    int h = src.rows(), w = src.cols();
    return (h > w) ? new Size(360, 640) : new Size(640, 360);
  }

  private void smooth1D(double[] arr, int k) {
    if (k <= 1) return;
    double[] tmp = new double[arr.length];
    int r = k / 2;
    for (int i = 0; i < arr.length; i++) {
      int a = Math.max(0, i - r), b = Math.min(arr.length - 1, i + r);
      double s = 0;
      for (int j = a; j <= b; j++) s += arr[j];
      tmp[i] = s / (b - a + 1);
    }
    System.arraycopy(tmp, 0, arr, 0, arr.length);
  }

  private List<Rect> nonMaxSuppressBands(List<Rect> bands, double iouThresh, int maxKeep) {
    bands.sort((a, b) -> Integer.compare(b.area(), a.area()));
    List<Rect> kept = new ArrayList<>();
    for (Rect r : bands) {
      boolean overlap = false;
      for (Rect k : kept)
        if (iou(r, k) > iouThresh) {
          overlap = true;
          break;
        }
      if (!overlap) kept.add(r);
      if (kept.size() >= maxKeep) break;
    }
    return kept;
  }

  private double iou(Rect a, Rect b) {
    int x1 = Math.max(a.x(), b.x());
    int y1 = Math.max(a.y(), b.y());
    int x2 = Math.min(a.x() + a.width(), b.x() + b.width());
    int y2 = Math.min(a.y() + a.height(), b.y() + b.height());
    int inter = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);
    int ua = a.width() * a.height(), ub = b.width() * b.height();
    int uni = ua + ub - inter;
    return uni > 0 ? (double) inter / uni : 0.0;
  }

  private Rect unionBands(List<Rect> bands, Mat graySmall) {
    if (bands == null || bands.isEmpty()) return new Rect(0, 0, graySmall.cols(), graySmall.rows());
    int y0 = bands.stream().mapToInt(Rect::y).min().orElse(0);
    int y1 = bands.stream().mapToInt(r -> r.y() + r.height()).max().orElse(graySmall.rows());
    return new Rect(0, y0, graySmall.cols(), Math.max(1, y1 - y0));
  }

  private Mat cropFromOriginal(Mat src, Size detectSizeRef, Rect small) {
    double sx = src.cols() / (double) detectSizeRef.width();
    double sy = src.rows() / (double) detectSizeRef.height();
    int x = (int) Math.round(small.x() * sx);
    int y = (int) Math.round(small.y() * sy);
    int w = (int) Math.round(small.width() * sx);
    int h = (int) Math.round(small.height() * sy);
    int pad = Math.max(2, (int) Math.round(0.01 * src.rows()));
    x = Math.max(0, x - pad);
    y = Math.max(0, y - pad);
    w = Math.min(src.cols() - x, w + 2 * pad);
    h = Math.min(src.rows() - y, h + 2 * pad);
    return new Mat(src, new Rect(x, y, w, h));
  }

  // ===== 디버그 저장 =====
  private void saveDebugBinary(Path dir, Mat bin, String prefix, long frameIndex) {
    saveDebugBinary(dir, bin, prefix, frameIndex, "");
  }

  private void saveDebugBinary(Path dir, Mat bin, String prefix, long frameIndex, String suffix) {
    try {
      Mat vis = new Mat();
      resize(bin, vis, new Size(bin.cols() * 2, bin.rows() * 2), 0, 0, INTER_NEAREST);
      File out = dir.resolve(String.format("%s%06d%s.png", prefix, frameIndex, suffix)).toFile();
      ImageIO.write(matToBuffered(vis), "png", out);
    } catch (Exception ignore) {
    }
  }

  private BufferedImage matToBuffered(Mat mat) {
    OpenCVFrameConverter.ToMat c = new OpenCVFrameConverter.ToMat();
    Frame f = c.convert(mat);
    return java2D.convert(f);
  }
}
