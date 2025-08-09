package dayum.dayumserver.application.contents;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;

import dayum.dayumserver.client.ai.ocr.OcrService;
import dayum.dayumserver.client.cv.FrameExtractorService;
import dayum.dayumserver.client.s3.S3ClientService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubtitleAnalysisService {

	private final S3ClientService s3ClientService;
	private final FrameExtractorService frameExtractorService;
	private final OcrService ocrService;

	public String extractSubtitleFromVideo(String contentsUrl, Path workingDir) {
		File downloaded = s3ClientService.downloadFile(contentsUrl, workingDir);
		List<File> frames = frameExtractorService.extractFrames(downloaded, workingDir);
		return ocrService.extractTextFromFiles(frames);
	}
}
