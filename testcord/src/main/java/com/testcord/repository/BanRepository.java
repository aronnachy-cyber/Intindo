package com.testcord.repository;

import com.testcord.model.Ban;
import com.testcord.model.Guild;
import com.testcord.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BanRepository extends JpaRepository<Ban, Long> {
    List<Ban> findByGuild(Guild guild);
    Optional<Ban> findByGuildAndUser(Guild guild, User user);
    boolean existsByGuildAndUser(Guild guild, User user);
    void deleteByGuildAndUser(Guild guild, User user);
}
