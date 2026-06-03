package com.testcord.repository;

import com.testcord.model.Guild;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GuildRepository extends JpaRepository<Guild, String> {
    List<Guild> findByOwnerId(String ownerId);
}
