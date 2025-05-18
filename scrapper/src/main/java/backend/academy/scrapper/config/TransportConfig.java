package backend.academy.scrapper.config;

import backend.academy.scrapper.service.NotificationService;
import backend.academy.scrapper.service.impl.NotificationHttpServiceImpl;
import backend.academy.scrapper.service.impl.NotificationKafkaServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class TransportConfig {

    private final NotificationKafkaServiceImpl kafkaService;
    private final NotificationHttpServiceImpl httpService;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "transport.type", havingValue = "KAFKA")
    public NotificationService kafkaNotificationService() {
        return kafkaService;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "transport.type", havingValue = "HTTP", matchIfMissing = true)
    public NotificationService httpNotificationService() {
        return httpService;
    }
}
