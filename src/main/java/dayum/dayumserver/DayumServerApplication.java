package dayum.dayumserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.persistence.Entity;

@SpringBootApplication
@EnableConfigurationProperties
@EnableJpaAuditing
public class DayumServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DayumServerApplication.class, args);
	}

}
