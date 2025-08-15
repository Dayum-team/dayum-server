package dayum.dayumserver.client.cv.config;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaCvConfig {

  @Bean
  public Java2DFrameConverter converter() {
    return new Java2DFrameConverter();
  }
}
