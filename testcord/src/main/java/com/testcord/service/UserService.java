package com.testcord.service;

import com.testcord.model.User;
import com.testcord.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SnowflakeService snowflakeService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       SnowflakeService snowflakeService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.snowflakeService = snowflakeService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        User user = new User(snowflakeService.generate(), username, false);
        user.setPasswordHash(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()));
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
}
