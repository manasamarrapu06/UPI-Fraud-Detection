package com.manasa.upifraud.controller;

import com.manasa.upifraud.entity.User;
import com.manasa.upifraud.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    // ==================================================
    // REGISTER
    // ==================================================

    @PostMapping("/register")
    public User register(@RequestBody User user) {

        return userService.registerUser(user);
    }

    // ==================================================
    // LOGIN
    // ==================================================

    @PostMapping("/login")
    public User login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        System.out.println("=================================");
        System.out.println("LOGIN REQUEST");
        System.out.println("Email: " + request.getEmail());
        System.out.println("=================================");

        // Authenticate user
        Authentication authentication;

try {

    authentication =
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

    System.out.println("AUTHENTICATION SUCCESS");
    System.out.println(
            "Authenticated user: "
                    + authentication.getName()
    );

} catch (Exception e) {

    System.out.println("=================================");
    System.out.println("AUTHENTICATION FAILED");
    System.out.println(
            "Exception: "
                    + e.getClass().getName()
    );
    System.out.println(
            "Message: "
                    + e.getMessage()
    );

    e.printStackTrace();

    throw e;
}

        // Create security context
        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        // IMPORTANT:
        // Save authentication into HTTP session
        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );

        // Get user from database
        User user = userService.loginUser(
                request.getEmail(),
                request.getPassword()
        );

        System.out.println("LOGIN SUCCESS");
        System.out.println("User: " + user.getEmail());
        System.out.println("Role: " + user.getRole());
        System.out.println("Session ID: "
                + httpRequest.getSession().getId());

        return user;
    }

    // ==================================================
    // CHECK CURRENT LOGIN
    // ==================================================

    @GetMapping("/me")
    public User currentUser(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "User is not logged in"
            );
        }

        String email = authentication.getName();

        return userService.getUserByEmail(email);
    }

    // ==================================================
    // LOGIN REQUEST CLASS
    // ==================================================

    public static class LoginRequest {

        private String email;
        private String password;

        public LoginRequest() {
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}