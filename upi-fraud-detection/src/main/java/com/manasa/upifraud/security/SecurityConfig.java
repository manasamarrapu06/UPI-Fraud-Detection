package com.manasa.upifraud.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(
            UserDetailsService userDetailsService) {

        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        // Supports BCrypt for new passwords
        // and {noop} temporarily for existing users.
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {

        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .securityContext(securityContext ->
                securityContext
                    .securityContextRepository(
                            securityContextRepository
                    )
            )

            .authenticationProvider(
                    authenticationProvider()
            )

            .authorizeHttpRequests(auth -> auth

                // Public pages
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/home.html",
                        "/login.html",
                        "/register.html",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                ).permitAll()

                // Public authentication APIs
                .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register"
                ).permitAll()

                // Transactions require login
                .requestMatchers(
                        "/api/transactions/**"
                ).hasAnyRole("USER", "ADMIN")

                // Admin dashboard
                .requestMatchers(
                        "/admin-dashboard.html"
                ).hasRole("ADMIN")

                // User dashboard
                .requestMatchers(
                        "/user-dashboard.html"
                ).hasRole("USER")

                // Everything else requires authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }
}