package ru.masnaviev.cloudfile.config.security;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ru.masnaviev.cloudfile.exception.ErrorResponse;

import java.io.IOException;

import static ru.masnaviev.cloudfile.constatnts.ErrorMessages.UNAUTHORIZED;

@Component
class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(UNAUTHORIZED);
        String jsonErrorResponse = new Gson().toJson(errorResponse);

        response.getWriter().write(jsonErrorResponse);
    }
}