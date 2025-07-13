package ru.masnaviev.cloudfile.user.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.masnaviev.cloudfile.user.dto.request.user.UserAuthorizationRequest;
import ru.masnaviev.cloudfile.user.dto.response.user.UserAuthorizationResponse;
import ru.masnaviev.cloudfile.user.service.AuthService;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AuthServiceImpl implements AuthService {
    private final AuthenticationManager manager;

    public UserAuthorizationResponse authorization(UserAuthorizationRequest request, HttpServletRequest servletRequest) {
        var token = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        Authentication authentication = manager.authenticate(token);

        SecurityContext context = setSecurityContext(authentication);
        setSession(servletRequest, context);

        return new UserAuthorizationResponse(request.username());
    }

    public void logout(HttpServletRequest servletRequest) {
        servletRequest.getSession().invalidate();
        SecurityContextHolder.clearContext();
    }

    private void setSession(HttpServletRequest servletRequest, SecurityContext context) {
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);
    }

    private SecurityContext setSecurityContext(Authentication authentication) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
