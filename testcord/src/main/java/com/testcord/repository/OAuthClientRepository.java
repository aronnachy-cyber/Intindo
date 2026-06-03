package com.testcord.repository;

import com.testcord.model.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OAuthClientRepository extends JpaRepository<OAuthClient, String> {
    Optional<OAuthClient> findByClientIdAndClientSecret(String clientId, String clientSecret);
}
