package dayum.dayumserver.client.ai.speech;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NcpSpeechClient {

  @Value("${ncp.speech.base-url}")
  private String baseUrl;

  @Value("${ncp.speech.secret-key}")
  private String secretKey;
}
