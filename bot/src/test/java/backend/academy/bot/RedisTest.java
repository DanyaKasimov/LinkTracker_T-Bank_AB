package backend.academy.bot;

import backend.academy.bot.accessor.RestAccessor;
import backend.academy.bot.dto.*;
import backend.academy.bot.services.SubscriptionService;
import backend.academy.bot.utils.JsonUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.http.ResponseEntity;

@Testcontainers
@SpringBootTest
class RedisTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("redis.duration", () -> "5");
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestAccessor restAccessor() {
            return mock(RestAccessor.class);
        }
    }

    @Autowired
    SubscriptionService service;

    @Autowired
    RestAccessor accessor;

    @Autowired
    CacheManager cacheManager;

    private final String chatId = "12345";
    private final SubscriptionRequestDto req = new SubscriptionRequestDto("https://example.com", List.of("tag1"), List.of("filter1"));

    @BeforeEach
    void clearCache() {
        cacheManager.getCache("subscriptions").clear();
        reset(accessor);
    }

    @Test
    void testCachingWorksOnGetSubscriptions() {
        Subscription sub = new Subscription(UUID.randomUUID().toString(), "https://example.com", List.of("tag1"), List.of("filter1"));
        ListLinksResponse mockResponse = ListLinksResponse.builder()
            .links(List.of(sub))
            .size(1)
            .build();
        when(accessor.get(anyString(), eq(ListLinksResponse.class), anyMap())).thenReturn(ResponseEntity.ok(mockResponse));

        ListLinksResponse first = service.getSubscriptions(chatId);

        assertThat(first.getLinks()).hasSize(1);
        assertThat(first.getSize()).isEqualTo(1);
        verify(accessor, times(1)).get(anyString(), eq(ListLinksResponse.class), anyMap());
    }

    @Test
    void testCacheEvictedOnAddSubscription() {
        ListLinksResponse initial = ListLinksResponse.builder().links(List.of()).size(0).build();
        when(accessor.get(anyString(), eq(ListLinksResponse.class), anyMap())).thenReturn(ResponseEntity.ok(initial));
        Subscription sub = new Subscription(UUID.randomUUID().toString(), "https://example.com", List.of("tag1"), List.of("filter1"));
        when(accessor.post(anyString(), any(), eq(Subscription.class), anyMap())).thenReturn(ResponseEntity.ok(sub));

        service.getSubscriptions(chatId);
        service.addSubscription(chatId, req);
        service.getSubscriptions(chatId);

        verify(accessor, times(2)).get(anyString(), eq(ListLinksResponse.class), anyMap());
    }

    @Test
    void testCacheEvictedOnRemoveSubscription() {
        ListLinksResponse initial = ListLinksResponse.builder().links(List.of()).size(0).build();
        when(accessor.get(anyString(), eq(ListLinksResponse.class), anyMap())).thenReturn(ResponseEntity.ok(initial));
        LinkResponse removed = LinkResponse.builder()
            .id(UUID.randomUUID().toString())
            .link("https://example.com")
            .tags(List.of("tag1"))
            .filters(List.of("filter1"))
            .build();
        when(accessor.delete(anyString(), any(), eq(LinkResponse.class), anyMap())).thenReturn(ResponseEntity.ok(removed));

        service.getSubscriptions(chatId);
        service.removeSubscription(chatId, "https://example.com");
        service.getSubscriptions(chatId);

        verify(accessor, times(2)).get(anyString(), eq(ListLinksResponse.class), anyMap());
    }

    @Test
    void testJsonToDtoMapping() {
        String json = "{\"id\":1,\"url\":\"https://test.ru\",\"description\":\"desc\",\"tgChatIds\":[100]}";
        LinkUpdateDto dto = JsonUtil.fromJson(json, LinkUpdateDto.class);
        assertEquals(1L, dto.getId());
        assertEquals("https://test.ru", dto.getUrl());
        assertEquals("desc", dto.getDescription());
        assertEquals(100L, dto.getTgChatIds().iterator().next());
    }
}
