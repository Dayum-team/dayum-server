package dayum.dayumserver.client.cv;

import static dayum.dayumserver.client.cv.FrameExtractorService.*;

import dayum.dayumserver.common.helper.FileHelper;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

@Slf4j
public class FrameExtractor implements Closeable {
  private static final int FRAME_EXTRACTION_INTERVAL_SECONDS = 3;

  private final FFmpegFrameGrabber grabber;
  private final FFmpegFrameFilter filter;
  private final Java2DFrameConverter converter;

  private final Path workdir;
  private final ArrayDeque<File> buffer = new ArrayDeque<>();

  private final int frameInterval;
  private int frameCount = 0;

  public FrameExtractor(File video, Path workdir, Java2DFrameConverter converter) throws Exception {
	this.workdir = workdir;
	this.converter = converter;

	this.grabber = new FFmpegFrameGrabber(video);
    this.grabber.setOption("skip_frame", "nokey");
	this.grabber.start();

	String fx = String.join(",",
	  "setpts=N/FRAME_RATE/TB",
  	  "scale=480:480:force_original_aspect_ratio=decrease:flags=area:force_divisible_by=2",
	  "format=gray",
	  "setsar=1");
	this.filter = new FFmpegFrameFilter(fx, grabber.getImageWidth(), grabber.getImageHeight());
	this.filter.setPixelFormat(grabber.getPixelFormat());
	this.filter.start();
	this.frameInterval = Math.max(1, (int) Math.round(grabber.getFrameRate() * FRAME_EXTRACTION_INTERVAL_SECONDS));
  }

  public File next() throws Exception {
	while (this.buffer.isEmpty()) {
	  Frame image = this.grabber.grabImage();
	  if (image == null) {
		return null;
	  }
	  if (this.frameCount % this.frameInterval == 0) {
		this.filter.push(image);
		Frame scaled;
		while ((scaled = this.filter.pullImage()) != null) {
		  BufferedImage scaledImage = this.converter.convert(scaled);
		  if (scaledImage != null) {
			int frameIndex = this.frameCount / this.frameInterval;
			String fileName = STR."\{FRAME_FILE_PREFIX}\{frameIndex}.jpg";
			File out = this.workdir.resolve(fileName).toFile();
			FileHelper.writeJpeg(scaledImage, out, 0.82f);
			this.buffer.add(out);
		  }
		}
	  }
	  this.frameCount++;
    }
    return this.buffer.pollFirst();
  }

  @Override
  public void close() {
	try {
  	  this.filter.stop();
	} catch (Exception ignore) {}
    try {
  	  this.grabber.stop();
	} catch (Exception ignore) {}
  }
}
