package dayum.dayumserver.client.s3;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

@Component
@RequiredArgsConstructor
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
}
