package dayum.dayumserver.client.ai.chat.clova;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("ncp.clova")
public class ClovaStudioProperties {

  private String baseUrl;
  private String apiKey;

  // 모델 파라미터 상수
  public static class ModelConfig {
    public static final double TOP_P = 0.8;
    public static final int TOP_K = 0;
    public static final int MAX_TOKENS = 1000;
    public static final double TEMPERATURE = 0.5;
    public static final double REPETITION_PENALTY = 1.1;
    public static final long SEED = 0;
    public static final boolean INCLUDE_AI_FILTERS = false;
  }

  // 프롬프트 상수
  public static class PromptConfig {
    public static final String INGREDIENT_EXTRACTION =
        """
            당신은 다이어트 요리 영상의 자막과 음성에서 식재료를 추출하는 AI입니다.
            주어진 텍스트에서 식재료와 사용량을 JSON 형식으로 추출해주세요.
            레시피와 재료도구는 추출하지 않아도 됩니다.
            사용량은 g 단위를 사용하세요.
            응답 형식에 유의하세요.


            응답 형식:
            재료를 추출하여 JSON 형식으로 반환하세요.
            무조건 마크다운과 코드 블록 없이 순수 JSON만 응답하세요.
            형식: {\\"ingredients\\": [{\\"name\\": \\"재료명\\", \\"quantity\\": \\"양\\"}, ...]}

            예시:
            {
              "ingredients": [
                {"name": "닭가슴살", "quantity": "200g"},
                {"name": "양파", "quantity": "1개"}
              ]
            }
        """;
  }
}
