package dayum.dayumserver.application.contents;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.CommonExceptionCode;
import dayum.dayumserver.application.common.response.PageResponse;
import dayum.dayumserver.application.contents.dto.ContentsAnalyzeResponse;
import dayum.dayumserver.application.contents.dto.ContentsDetailResponse;
import dayum.dayumserver.application.contents.dto.ContentsResponse;
import dayum.dayumserver.application.contents.dto.internal.ExtractedIngredientData;
import dayum.dayumserver.client.cv.FrameExtractorService;
import dayum.dayumserver.client.s3.S3ClientService;
import dayum.dayumserver.domain.contents.Contents;
import dayum.dayumserver.domain.contents.ContentsRepository;
import dayum.dayumserver.domain.member.MemberRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentsService {

  private final ContentsRepository contentsRepository;
  private final MemberRepository memberRepository;
  private final ContentAnalysisService contentAnalysisService;
  private final FrameExtractorService frameExtractorService;
  private final S3ClientService s3ClientService;

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
    var nextItem = contentsList.getLast();
    return new PageResponse<>(
        items, new PageResponse.PageInfo(String.valueOf(nextItem.id()), false));
  }

  public ContentsDetailResponse retrieve(long id) {
    var contents = contentsRepository.fetchBy(id);
    return ContentsDetailResponse.from(contents);
  }

  public ContentsAnalyzeResponse analyze(String contentsUrl, Long memberId) {
    String thumbnailUrl;
    List<ExtractedIngredientData> extractedIngredients;

    Path workingDir = createWorkingDirectory();
    try {
      File contentsFile = s3ClientService.downloadFile(contentsUrl, workingDir);
      File thumbnail = frameExtractorService.extractThumbnail(contentsFile, workingDir);
      thumbnailUrl = s3ClientService.uploadFile("contents/thumbnails", thumbnail, workingDir);
      extractedIngredients =
          contentAnalysisService.analyzeIngredients(contentsUrl, contentsFile, workingDir);
    } finally {
      deleteWorkingDirectory(workingDir);
    }

    var contents =
        Contents.createDraft(memberRepository.fetchBy(memberId), contentsUrl, thumbnailUrl);
    contentsRepository.save(contents);

    // TODO 추출된 재료와 DB 데이터 매핑후 반환
    return new ContentsAnalyzeResponse();
  }

  private Path createWorkingDirectory() {
    try {
      Path workingDir =
          Paths.get(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
      Files.createDirectory(workingDir);
      return workingDir;
    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.INTERNAL_SERVER_ERROR);
    }
  }

  private void deleteWorkingDirectory(Path path) {
    try {
      Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.INTERNAL_SERVER_ERROR);
    }
  }
}
