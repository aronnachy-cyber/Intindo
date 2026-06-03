package com.testcord.service;

import com.testcord.model.BotToken;
import com.testcord.model.User;
import com.testcord.repository.BotTokenRepository;
import com.testcord.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class BotTokenService {

    private final BotTokenRepository botTokenRepository;
    private final UserRepository userRepository;
    private final SnowflakeService snowflakeService;
    private final SecureRandom secureRandom = new SecureRandom();

    public BotTokenService(BotTokenRepository botTokenRepository,
                           UserRepository userRepository,
                           SnowflakeService snowflakeService) {
        this.botTokenRepository = botTokenRepository;
        this.userRepository = userRepository;
        this.snowflakeService = snowflakeService;
    }

    @Transactional
    public String createBot(String username) {
        String id = snowflakeService.generate();
        User botUser = new User(id, username, true);
        userRepository.save(botUser);

        String token = generateToken(id);
        BotToken botToken = new BotToken(token, botUser);
        botTokenRepository.save(botToken);
        return token;
    }

    public Optional<User> validateToken(String token) {
        return botTokenRepository.findByTokenAndActiveTrue(token)
                .map(BotToken::getBotUser);
    }

    public Optional<BotToken> findByToken(String token) {
        return botTokenRepository.findByTokenAndActiveTrue(token);
    }

    private String generateToken(String userId) {
        String encodedId = Base64.getEncoder().encodeToString(userId.getBytes());
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String random = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return encodedId + "." + random;
    }
}
