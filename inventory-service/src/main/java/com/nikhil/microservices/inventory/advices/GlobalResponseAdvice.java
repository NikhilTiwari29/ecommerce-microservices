package com.nikhil.microservices.inventory.advices;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss z")
                    .withZone(ZoneId.of("Asia/Kolkata"));

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {

        String path = ((ServletServerHttpRequest) request)
                .getServletRequest()
                .getRequestURI();

        // 🔹 Exclude non-business endpoints
        if (path.startsWith("/actuator")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/api-docs")) {
            return body;
        }

        // Avoid double wrapping
        if (body instanceof ApiResponse || body instanceof ApiErrorResponse) {
            return body;
        }

        HttpServletRequest servletRequest =
                ((ServletServerHttpRequest) request).getServletRequest();

        int status = ((ServletServerHttpResponse) response)
                .getServletResponse()
                .getStatus();

        return new ApiResponse<>(
                FORMATTER.format(Instant.now()),
                status,
                servletRequest.getRequestURI(),
                body,
                null
        );
    }
}
