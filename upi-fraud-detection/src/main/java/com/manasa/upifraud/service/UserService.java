package com.manasa.upifraud.service;

import com.manasa.upifraud.entity.User;
import com.manasa.upifraud.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        user.setRole("USER");

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String storedPassword = user.getPassword();

        boolean validPassword;

        /*
         * Existing old account:
         * password stored as plain text.
         */
        if (storedPassword != null
                && !storedPassword.startsWith("{bcrypt}")
                && !storedPassword.startsWith("$2a$")
                && !storedPassword.startsWith("$2b$")
                && !storedPassword.startsWith("$2y$")) {

            validPassword =
                    storedPassword.equals(password);

            /*
             * Convert old password to BCrypt
             * after successful login.
             */
            if (validPassword) {

                user.setPassword(
                        passwordEncoder.encode(password)
                );

                userRepository.save(user);
            }

        } else {

            /*
             * BCrypt password.
             */
            validPassword =
                    passwordEncoder.matches(
                            password,
                            storedPassword
                    );
        }

        if (!validPassword) {
            throw new RuntimeException(
                    "Invalid password"
            );
        }

        return user;
    }

    public User createAdmin(User user) {

        if (userRepository
                .findByEmail(user.getEmail())
                .isPresent()) {

            throw new RuntimeException(
                    "Email already registered"
            );
        }

        user.setRole("ADMIN");

        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        return userRepository.save(user);
    }
}