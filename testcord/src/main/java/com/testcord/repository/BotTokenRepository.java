package com.testcord.repository;

import com.testcord.model.BotToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BotTokenRepository extends JpaRepository<BotToken, Long> {
    Optional<BotToken> findByTokenAndActiveTrue(String token);
    Optional<BotToken> findByBotUserIdAndActiveTrue(String botUserId);
}
