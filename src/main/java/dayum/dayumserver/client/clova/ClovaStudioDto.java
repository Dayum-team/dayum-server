package dayum.dayumserver.client.clova;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * CLOVA Studio Chat Completions v3 API와 통신하기 위한 DTO 컨테이너 클래스
 */
public class ClovaStudioDto {

	// --- 요청(Request) 관련 DTO ---

	/**
	 * API 요청의 전체 Body
	 * @param messages 메시지 목록 (보통 사용자 메시지 1개)
	 * @param maxTokens 응답의 최대 토큰 길이
	 * @param model 사용할 모델 이름 (예: "HCX-005")
	 */
	public record ClovaRequest(
		List<Message> messages,
		int maxTokens,
		String model) {}

	/**
	 * 대화 메시지
	 * @param role 메시지 주체 (보통 "user")
	 * @param content 실제 내용 (텍스트와 이미지 파트의 리스트)
	 */
	public record Message(
		String role,
		List<ContentPart> content) {}

	/**
	 * 멀티모달 콘텐츠의 각 부분 (텍스트 또는 이미지)
	 * null인 필드는 JSON으로 변환 시 제외합니다.
	 * @param type "text" 또는 "image_url"
	 * @param text 텍스트 내용 (type이 "text"일 때 사용)
	 * @param imageUrl 이미지 정보 (type이 "image_url"일 때 사용)
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record ContentPart( // ✅ 파라미터는 3개입니다.
							   String type,
							   String text,
							   @JsonProperty("imageUrl") ImageUrl imageUrl) {}


	public record ImageUrl(String url) {}

	// --- 응답(Response) 관련 DTO ---

	/**
	 * API 응답의 전체 Body
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ClovaResponse(Result result) {}

	/**
	 * 응답 결과
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Result(ResponseMessage message) {}

	/**
	 * AI 모델이 생성한 실제 메시지
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ResponseMessage(String content) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ClovaStreamResponse(String content) {} // 단순화된 스트리밍 응답 DTO
}
