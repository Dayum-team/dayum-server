package dayum.dayumserver.client.s3;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.s3.TransferManagerS3OutputStreamProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3ClientConfig {

  @Bean
  public S3Client ncpS3Client(NcpProperties ncpProperties) {
    return S3Client.builder()
        .endpointOverride(URI.create(ncpProperties.getS3Endpoint()))
        .region(Region.of(ncpProperties.getRegion()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    ncpProperties.getAccessKey(), ncpProperties.getSecretKey())))
        .serviceConfiguration(
            S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .checksumValidationEnabled(false)
                .build())
        .build();
  }
}
