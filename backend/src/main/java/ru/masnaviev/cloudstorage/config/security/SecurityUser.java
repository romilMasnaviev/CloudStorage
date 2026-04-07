package ru.masnaviev.cloudstorage.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.masnaviev.cloudstorage.model.User;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class SecurityUser implements UserDetails {
    private final User user;

    public Long getUserId() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
