package dayum.dayumserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DayumServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DayumServerApplication.class, args);
	}

}
