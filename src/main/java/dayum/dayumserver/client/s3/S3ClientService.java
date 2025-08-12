package dayum.dayumserver.client.s3;

import dayum.dayumserver.application.common.exception.AppException;
import dayum.dayumserver.application.common.exception.CommonExceptionCode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3ClientService {

  private final NcpProperties ncpProperties;
  private final S3Client s3Client;

  public Optional<S3Object> get(String path, String fileName) {
    var request =
        ListObjectsV2Request.builder()
            .bucket(ncpProperties.getS3Bucket())
            .prefix(String.join("/", path, fileName))
            .maxKeys(1)
            .build();
    return s3Client.listObjectsV2(request).contents().stream().findFirst();
  }

  /** Object Storage에서 파일을 다운로드하여 로컬 임시 파일로 반환 */
  public File downloadFile(String contentsUrl, Path downloadDirPath) {
    String objectKey = extractObjectKeyFromUrl(contentsUrl);
    String fileName = objectKey.substring(objectKey.lastIndexOf('/') + 1);
    Path destinationPath = downloadDirPath.resolve(fileName);

    try {
      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(ncpProperties.getS3Bucket()).key(objectKey).build();

      ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest);
      Files.copy(s3ObjectStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);

      return destinationPath.toFile();
    } catch (IOException e) {
      throw new AppException(CommonExceptionCode.S3_DOWNLOAD_FAILED);
    }
  }

  public String uploadFile(String prefix, File file, Path workingDir) {
    String ext = file.getName().substring(file.getName().lastIndexOf("."));
    UUID uuid = UUID.randomUUID();

    String objectKey = prefix + "/" + uuid + ext;
    PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(ncpProperties.getS3Bucket())
            .key(objectKey)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build();
    s3Client.putObject(request, RequestBody.fromFile(file));
    return String.format(
        "%s/%s/%s", ncpProperties.getS3Endpoint(), ncpProperties.getS3Bucket(), objectKey);
  }

  // 전체 URL에서 Object Storage의 객체 키만 추출
  private String extractObjectKeyFromUrl(String fullUrl) {
    String bucketName = ncpProperties.getS3Bucket();
    String bucketBaseUrl = String.format("%s/%s/", ncpProperties.getS3Endpoint(), bucketName);

    return fullUrl.substring(bucketBaseUrl.length());
  }
}
