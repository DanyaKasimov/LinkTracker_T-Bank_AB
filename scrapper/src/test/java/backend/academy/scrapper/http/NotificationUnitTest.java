package backend.academy.scrapper.http;

import static org.mockito.Mockito.*;

import backend.academy.scrapper.dto.UserMessage;
import backend.academy.scrapper.service.impl.notification.NotificationDelegatorService;
import backend.academy.scrapper.service.impl.notification.NotificationHttpServiceImpl;
import backend.academy.scrapper.service.impl.notification.NotificationKafkaServiceImpl;
import org.junit.jupiter.api.Test;

class NotificationUnitTest {

    @Test
    void shouldFallbackToKafkaWhenHttpFails() {
        var http = mock(NotificationHttpServiceImpl.class);
        var kafka = mock(NotificationKafkaServiceImpl.class);
        var delegator = new NotificationDelegatorService(http, kafka);

        doThrow(new RuntimeException("fail")).when(http).sendNotification(any(), any());

        doNothing().when(kafka).sendNotification(any(), any());

        UserMessage msg = mock(UserMessage.class);
        delegator.sendNotification("test-link", msg);

        verify(http).sendNotification(eq("test-link"), eq(msg));
        verify(kafka).sendNotification(eq("test-link"), eq(msg));
    }
}
