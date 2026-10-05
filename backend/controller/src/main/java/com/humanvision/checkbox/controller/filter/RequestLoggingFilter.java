package com.humanvision.checkbox.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_MDC_KEY = "request_id";
    private static final Pattern VALID_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        long startNanos = System.nanoTime();

        setRequestContext(requestId, response);

        try {
            chain.doFilter(request, response);
        } finally {
            logRequest(request, response, startNanos);
            clearRequestContext();
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(REQUEST_ID_HEADER))
                .filter(VALID_REQUEST_ID.asMatchPredicate())
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    private void setRequestContext(
            String requestId,
            HttpServletResponse response) {
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
    }

    private void logRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            long startNanos) {
        log.info(
                "request completed method={} path={} status={} duration_ms={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                elapsedMillis(startNanos)
        );
    }

    private void clearRequestContext() {
        MDC.remove(REQUEST_ID_MDC_KEY);
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
