package ru.masnaviev.cloudstorage.config.logs;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class LogFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        UUID uuid = UUID.randomUUID();

        MDC.put("traceId", uuid.toString());
        log.info("Входящий запрос {} {}", request.getMethod(), request.getRequestURI());
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("Запрос завершен: Статус {}. Время выполнения {} мс.", response.getStatus(), System.currentTimeMillis() - startTime);
            MDC.clear();
        }
    }
}
