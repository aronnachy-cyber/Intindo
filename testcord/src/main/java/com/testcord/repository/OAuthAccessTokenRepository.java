package com.testcord.repository;

import com.testcord.model.OAuthAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OAuthAccessTokenRepository extends JpaRepository<OAuthAccessToken, Long> {
    Optional<OAuthAccessToken> findByAccessToken(String accessToken);
    Optional<OAuthAccessToken> findByRefreshToken(String refreshToken);
}
