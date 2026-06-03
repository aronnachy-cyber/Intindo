package com.testcord.repository;

import com.testcord.model.OAuthCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OAuthCodeRepository extends JpaRepository<OAuthCode, Long> {
    Optional<OAuthCode> findByCodeAndUsedFalse(String code);
}
