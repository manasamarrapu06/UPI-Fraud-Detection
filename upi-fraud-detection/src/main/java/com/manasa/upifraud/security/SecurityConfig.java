package com.manasa.upifraud.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // ==================================================
    // PASSWORD ENCODER
    // Supports both BCrypt and existing {noop} passwords
    // ==================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        Map<String, PasswordEncoder> encoders =
                new HashMap<>();

        encoders.put(
                "bcrypt",
                new BCryptPasswordEncoder()
        );

        encoders.put(
                "noop",
                NoOpPasswordEncoder.getInstance()
        );

        return new DelegatingPasswordEncoder(
                "bcrypt",
                encoders
        );
    }

    // ==================================================
    // AUTHENTICATION PROVIDER
    // ==================================================

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }

    // ==================================================
    // AUTHENTICATION MANAGER
    // ==================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    // ==================================================
    // SECURITY CONTEXT
    // ==================================================

    @Bean
    public SecurityContextRepository securityContextRepository() {

        return new HttpSessionSecurityContextRepository();
    }

    // ==================================================
    // SECURITY FILTER CHAIN
    // ==================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository)
            throws Exception {

        http

            // Disable CSRF for this project
            .csrf(csrf -> csrf.disable())

            // Store login in HTTP session
            .securityContext(securityContext ->
                securityContext
                    .securityContextRepository(
                        securityContextRepository
                    )
            )

            // Authentication provider
            .authenticationProvider(
                authenticationProvider()
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth

                // ------------------------------------------
                // PUBLIC PAGES
                // ------------------------------------------

                .requestMatchers(
                    "/",
                    "/index.html",
                    "/home.html",
                    "/login.html",
                    "/register.html",
                    "/transaction.html"
                ).permitAll()

                // ------------------------------------------
                // STATIC RESOURCES
                // ------------------------------------------

                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()

                // ------------------------------------------
                // AUTHENTICATION APIs
                // ------------------------------------------

                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register"
                ).permitAll()

                // ------------------------------------------
                // TRANSACTION APIs
                // USER + ADMIN
                // ------------------------------------------

                .requestMatchers(
                    "/api/transactions/**"
                ).hasAnyRole("USER", "ADMIN")

                // ------------------------------------------
                // USER DASHBOARD
                // ------------------------------------------

                .requestMatchers(
                    "/user-dashboard.html"
                ).hasRole("USER")

                // ------------------------------------------
                // ADMIN DASHBOARD
                // ------------------------------------------

                .requestMatchers(
                    "/admin-dashboard.html"
                ).hasRole("ADMIN")

                // ------------------------------------------
                // EVERYTHING ELSE
                // ------------------------------------------

                .anyRequest().authenticated()
            );

        return http.build();
    }
}