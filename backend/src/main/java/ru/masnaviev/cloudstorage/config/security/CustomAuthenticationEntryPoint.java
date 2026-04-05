package ru.masnaviev.cloudstorage.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ru.masnaviev.cloudstorage.exception.ErrorResponse;

import java.io.IOException;

import static ru.masnaviev.cloudstorage.constants.ErrorMessages.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(UNAUTHORIZED);
        String jsonErrorResponse = mapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonErrorResponse);
    }
}