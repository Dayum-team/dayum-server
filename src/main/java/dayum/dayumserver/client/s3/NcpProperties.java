package dayum.dayumserver.client.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties("ncp")
public class NcpProperties {

  private String region;
  private String accessKey;
  private String secretKey;
  private String s3Endpoint;
  private String s3Bucket;
}
