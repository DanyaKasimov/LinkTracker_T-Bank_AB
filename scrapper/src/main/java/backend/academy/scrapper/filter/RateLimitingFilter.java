package backend.academy.scrapper.filter;

import backend.academy.scrapper.config.ScrapperConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
@RequiredArgsConstructor
public class RateLimitingFilter implements Filter {

    private final ScrapperConfig.HttpProperties properties;
    private final Map<String, RequestWindow> ipRequests = new ConcurrentHashMap<>();

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!properties.rateLimiting().enabled()) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String ip = extractClientIp(req);

        if (!isAllowed(ip)) {
            log.warn("Достигнут лимит запросов для IP-адреса {}", ip);
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            res.setContentType("application/json");
            res.getWriter().write("{\"error\": \"Превышен лимит запросов. Попробуйте позже.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isAllowed(String ip) {
        int maxRequests = properties.rateLimiting().requests();
        int windowSec = properties.rateLimiting().seconds();

        RequestWindow window = ipRequests.computeIfAbsent(ip, k -> new RequestWindow(windowSec));
        return window.tryRequest(maxRequests);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        String[] parts = COMMA_PATTERN.split(xfHeader, 2);
        return parts.length > 0 ? parts[0].trim() : "";
    }

    private static class RequestWindow {
        private final int windowSeconds;
        private long windowStartEpoch;
        private int counter;

        public RequestWindow(int windowSeconds) {
            this.windowSeconds = windowSeconds;
            this.windowStartEpoch = Instant.now().getEpochSecond();
        }

        public synchronized boolean tryRequest(int maxRequests) {
            long now = Instant.now().getEpochSecond();

            if (now >= windowStartEpoch + windowSeconds) {
                windowStartEpoch = now;
                counter = 0;
            }

            return counter++ < maxRequests;
        }
    }
}
