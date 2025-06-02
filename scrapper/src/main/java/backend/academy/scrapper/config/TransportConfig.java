package backend.academy.scrapper.config;

import backend.academy.scrapper.exceptions.InvalidDataException;
import backend.academy.scrapper.service.NotificationService;
import backend.academy.scrapper.service.impl.notification.NotificationDelegatorService;
import backend.academy.scrapper.service.impl.notification.NotificationHttpServiceImpl;
import backend.academy.scrapper.service.impl.notification.NotificationKafkaServiceImpl;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TransportConfig {

    private static final String HTTP_TYPE = "HTTP";
    private static final String KAFKA_TYPE = "KAFKA";

    @Bean
    @Primary
    public NotificationService notificationService(
            NotificationHttpServiceImpl httpService,
            NotificationKafkaServiceImpl kafkaService,
            @Value("${transport.type:HTTP}") String transportType) {

        return switch (transportType.toUpperCase(Locale.ROOT)) {
            case HTTP_TYPE -> new NotificationDelegatorService(httpService, kafkaService);
            case KAFKA_TYPE -> new NotificationDelegatorService(kafkaService, httpService);
            default -> throw new InvalidDataException("Неизвестный тип транспорта: " + transportType);
        };
    }
}
