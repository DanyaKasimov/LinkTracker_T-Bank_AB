package backend.academy.scrapper;

import backend.academy.scrapper.config.kafka.KafkaTopics;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.UrlConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({ScrapperConfig.class, UrlConfig.class, KafkaTopics.class})
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
