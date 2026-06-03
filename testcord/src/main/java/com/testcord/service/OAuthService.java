package com.testcord.service;

import com.testcord.model.*;
import com.testcord.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class OAuthService {

    private final OAuthClientRepository clientRepository;
    private final OAuthCodeRepository codeRepository;
    private final OAuthAccessTokenRepository accessTokenRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuthService(OAuthClientRepository clientRepository,
                        OAuthCodeRepository codeRepository,
                        OAuthAccessTokenRepository accessTokenRepository,
                        UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.codeRepository = codeRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<OAuthClient> findClient(String clientId) {
        return clientRepository.findById(clientId);
    }

    @Transactional
    public String generateAuthCode(String userId, String clientId, String scope) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        codeRepository.save(new OAuthCode(code, userId, clientId, scope));
        return code;
    }

    @Transactional
    public Optional<OAuthAccessToken> exchangeCode(String code, String clientId, String clientSecret) {
        Optional<OAuthClient> client = clientRepository.findByClientIdAndClientSecret(clientId, clientSecret);
        if (client.isEmpty()) return Optional.empty();

        Optional<OAuthCode> oauthCode = codeRepository.findByCodeAndUsedFalse(code);
        if (oauthCode.isEmpty()) return Optional.empty();

        OAuthCode authCode = oauthCode.get();
        if (authCode.getExpiresAt().isBefore(Instant.now())) return Optional.empty();
        if (!authCode.getClientId().equals(clientId)) return Optional.empty();

        authCode.setUsed(true);
        codeRepository.save(authCode);

        String accessToken = generateRandomToken();
        String refreshToken = generateRandomToken();
        OAuthAccessToken token = new OAuthAccessToken(
                accessToken, refreshToken, authCode.getUserId(), clientId, authCode.getScope());
        return Optional.of(accessTokenRepository.save(token));
    }

    public Optional<User> validateAccessToken(String accessToken) {
        return accessTokenRepository.findByAccessToken(accessToken)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .flatMap(t -> userRepository.findById(t.getUserId()));
    }

    @Transactional
    public OAuthClient registerClient(String name, String redirectUri, String ownerUserId,
                                      SnowflakeService snowflakeService) {
        String clientId = snowflakeService.generate();
        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        String clientSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
        OAuthClient client = new OAuthClient(clientId, clientSecret, name, redirectUri, ownerUserId);
        return clientRepository.save(client);
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
