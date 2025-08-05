package dayum.dayumserver.client.clova;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties("ncp.clova")
public class ClovaProperties {
	private String invokeUrl;
	private String authKey;
}
