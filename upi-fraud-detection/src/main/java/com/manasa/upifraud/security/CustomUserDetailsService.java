package com.manasa.upifraud.security;

import com.manasa.upifraud.entity.User;
import com.manasa.upifraud.repository.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String email)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + email
                        )
                );

        String password = user.getPassword();

        /*
         * Existing Admin password is stored as:
         *
         * {noop}admin123
         *
         * BCrypt passwords are stored as:
         *
         * $2a$...
         *
         * Keep the prefix so DelegatingPasswordEncoder
         * knows which encoder to use.
         */

        if (password != null
                && !password.startsWith("{")
                && !password.startsWith("$2")) {

            password = "{noop}" + password;
        }

        String role = user.getRole();

        if (role == null || role.trim().isEmpty()) {
            role = "USER";
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                password,
                Collections.singletonList(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        )
                )
        );
    }
}