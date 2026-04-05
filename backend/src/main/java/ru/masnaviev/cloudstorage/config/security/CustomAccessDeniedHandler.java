package ru.masnaviev.cloudstorage.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import ru.masnaviev.cloudstorage.exception.ErrorResponse;

import java.io.IOException;

import static ru.masnaviev.cloudstorage.constants.ErrorMessages.ACCESS_DENIED;

@Component
@RequiredArgsConstructor
class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(ACCESS_DENIED);
        String jsonErrorResponse = mapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonErrorResponse);
    }
}
