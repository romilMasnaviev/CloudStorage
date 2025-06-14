package ru.masnaviev.cloudfile.user.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.masnaviev.cloudfile.user.repository.UserRepository;
import ru.masnaviev.cloudfile.user.service.SecurityUserService;

@Configuration
@RequiredArgsConstructor
public class UserManagementConfig {
    private final UserRepository repository;

    @Bean
    public UserDetailsService userDetailsService() {
        return new SecurityUserService(repository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
