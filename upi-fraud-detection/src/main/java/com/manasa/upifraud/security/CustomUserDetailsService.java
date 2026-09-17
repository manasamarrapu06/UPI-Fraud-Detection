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
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        )
                );

        String password = user.getPassword();

        /*
         * Existing users may have plain-text passwords.
         *
         * New users use {bcrypt} passwords.
         *
         * If the password is already encoded with
         * {bcrypt}, keep it unchanged.
         */

        if (password != null
                && !password.startsWith("{bcrypt}")
                && !password.startsWith("{noop}")) {

            password = "{noop}" + password;
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                password,
                Collections.singletonList(
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole()
                        )
                )
        );
    }
}