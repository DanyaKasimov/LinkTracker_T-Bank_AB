package backend.academy.scrapper.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class UserMessageMetricsService {
    private final Counter userMessageCounter;

    public UserMessageMetricsService(MeterRegistry registry) {
        this.userMessageCounter = Counter.builder("user_messages_total")
                .description("Total number of user messages")
                .register(registry);
    }

    public void increment() {
        userMessageCounter.increment();
    }
}
