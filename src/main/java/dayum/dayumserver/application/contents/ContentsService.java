package dayum.dayumserver.application.contents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.CommonExceptionCode;
import dayum.dayumserver.application.common.response.PageResponse;
import dayum.dayumserver.application.contents.dto.ContentsAnalyzeResponse;
import dayum.dayumserver.application.contents.dto.ContentsDetailResponse;
import dayum.dayumserver.application.contents.dto.ContentsResponse;
import dayum.dayumserver.client.cv.FrameExtractorService;
import dayum.dayumserver.client.ocr.OcrService;
import dayum.dayumserver.client.s3.S3ClientService;
import dayum.dayumserver.domain.contents.Contents;
import dayum.dayumserver.domain.contents.ContentsRepository;
import dayum.dayumserver.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentsService {

  private final ContentsRepository contentsRepository;
  private final MemberRepository memberRepository;

  private final S3ClientService s3ClientService;
  private final FrameExtractorService frameExtractorService;
  private final OcrService ocrService;

  public PageResponse<ContentsResponse> retrieveNextPage(Long memberId, long cursorId, int size) {
    var contentsList =
        switch (memberId) {
          case null -> contentsRepository.fetchNextPage(cursorId, size + 1);
          default -> contentsRepository.fetchNextPageByMember(memberId, cursorId, size + 1);
        };

    if (contentsList.size() <= size) {
      var items = contentsList.stream().map(ContentsResponse::from).toList();
      return new PageResponse<>(items, new PageResponse.PageInfo("", true));
    }
    var items = contentsList.subList(0, size).stream().map(ContentsResponse::from).toList();
    return new PageResponse<>(
        items, new PageResponse.PageInfo(String.valueOf(items.getLast().id()), false));
  }

  public ContentsDetailResponse retrieve(long id) {
    var contents = contentsRepository.fetchBy(id);
    return ContentsDetailResponse.from(contents);
  }

  public ContentsAnalyzeResponse extractIngredientsFromContent(String contentsUrl, Long memberId) {

    var contents = saveContentsAsPending(memberId, contentsUrl);

    String uniqueId = UUID.randomUUID().toString();
    Path workingDir = Paths.get(System.getProperty("java.io.tmpdir"), uniqueId);

    File downloadedFile = null;
    List<File> frameFiles = null;
    try {
      // 워킹 디렉토리 생성
      // Files.createDirectory(workingDir);
      // // 1. ncp object storage 에서 영상 다운로드
      // downloadedFile = s3ClientService.downloadFile(contentsUrl, workingDir);
      // // 2. 영상을 JavaCV(FFmpegFrameGrabber) 로 이미지 추출
      // frameFiles = frameExtractorService.extractFrames(downloadedFile, workingDir);
      // // // 3. NCP OCR 로 자막 데이터 추출
      // Map<String, String> ocrExtractText = ocrService.extractTextFromFiles(frameFiles);
      // 4. "자막 + 이미지" 를 묶어서 NCP CLOVA Studio HXR-005 모델로 식재료 추출 요청

      // 5. 추출된 식재료를 데이터베이스에서 조회
      // 6. DB에 존재하는 식재료로 응답

      // } catch (IOException e) {
      //   throw new RuntimeException(e);
    } finally {
      // 식재료 추출 작업이 끝나면 작업동안 생긴 폴더를 제거
      if (workingDir != null && Files.exists(workingDir)) {
        deleteDirectoryRecursively(workingDir);
      }
    }

    return new ContentsAnalyzeResponse();
  }

  private Contents saveContentsAsPending(Long memberId, String contentsUrl) {
    return contentsRepository.save(
        Contents.createDraft(memberRepository.fetchBy(memberId), contentsUrl));
  }

  private void deleteDirectoryRecursively(Path path) {
    try {
      Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.INTERNAL_SERVER_ERROR);
    }
  }
}
