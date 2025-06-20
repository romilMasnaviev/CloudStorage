package ru.masnaviev.cloudfile.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.masnaviev.cloudfile.user.model.SecurityUser;
import ru.masnaviev.cloudfile.user.repository.UserRepository;

import static ru.masnaviev.cloudfile.user.constatnts.ErrorMessages.USERNAME_NOT_FOUND;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new SecurityUser(repository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException(USERNAME_NOT_FOUND)));
    }
}
