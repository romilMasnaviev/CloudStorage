package ru.masnaviev.cloudfile.user.config.security;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import ru.masnaviev.cloudfile.user.exception.ErrorResponse;

import java.io.IOException;

import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.ACCESS_DENIED;

@Component
class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(ACCESS_DENIED);
        String jsonErrorResponse = new Gson().toJson(errorResponse);

        response.getWriter().write(jsonErrorResponse);
    }
}
