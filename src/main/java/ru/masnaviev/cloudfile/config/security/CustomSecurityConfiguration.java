package ru.masnaviev.cloudfile.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static ru.masnaviev.cloudfile.constatnts.ApiPath.*;

@Configuration
@RequiredArgsConstructor
class CustomSecurityConfiguration {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(configurer -> configurer
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_SIGN_UP_URL).anonymous()
                        .requestMatchers(AUTH_SIGN_IN_URL).anonymous()
                        .requestMatchers(USER_ME_URL).authenticated()
                        .requestMatchers(AUTH_SIGN_OUT_URL).authenticated()
                        .requestMatchers(GET_RESOURCE_INFO).authenticated()
                        .requestMatchers(DELETE_RESOURCE).authenticated()
                        .requestMatchers(DOWNLOAD_RESOURCE).authenticated()
                        .requestMatchers(UPLOAD_DIRECTORY).authenticated()
                        .requestMatchers(GET_DIRECTORY_CONTENTS_INFO).authenticated()
                        .requestMatchers(UPLOAD_RESOURCE).authenticated()
                        .requestMatchers(MOVE_RESOURCE).authenticated()
                        .requestMatchers(FIND_RESOURCE).authenticated()
                        .requestMatchers(SWAGGER_AUTH_WHITELIST).permitAll() //TODO при deploy убрать в denyAll
                        .anyRequest().denyAll())
                .build();
    }

}
