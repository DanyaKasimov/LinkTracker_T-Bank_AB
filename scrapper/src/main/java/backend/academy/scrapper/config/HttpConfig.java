package backend.academy.scrapper.config;

import backend.academy.scrapper.exceptions.HttpConnectException;
import backend.academy.scrapper.filter.RateLimitingFilter;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import jakarta.servlet.Filter;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@Configuration
@RequiredArgsConstructor
public class HttpConfig {

    private final ScrapperConfig scrapperConfig;

    @Bean
    public Retry webClientRetry() {
        return Retry.fixedDelay(
                        scrapperConfig.http().retry().maxAttempts(),
                        Duration.ofMillis(scrapperConfig.http().retry().backoffMillis()))
                .filter(throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        return scrapperConfig.http().retry().statusCodes().contains(ex.getStatusCode());
                    }
                    return false;
                })
                .onRetryExhaustedThrow((spec, signal) -> new HttpConnectException(
                        "Повторные попытки исчерпаны: " + signal.failure().getLocalizedMessage()));
    }

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int)
                        scrapperConfig.http().timeout().connect().toMillis())
                .responseTimeout(scrapperConfig.http().timeout().response())
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(
                        scrapperConfig.http().timeout().response().toMillis(), TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public FilterRegistrationBean<Filter> rateLimitingFilter(ScrapperConfig config) {
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new RateLimitingFilter(config.http()));
        filterFilterRegistrationBean.addUrlPatterns("/*");
        filterFilterRegistrationBean.setOrder(1);
        return filterFilterRegistrationBean;
    }
}
