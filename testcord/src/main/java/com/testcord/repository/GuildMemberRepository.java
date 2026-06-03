package com.testcord.repository;

import com.testcord.model.Guild;
import com.testcord.model.GuildMember;
import com.testcord.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GuildMemberRepository extends JpaRepository<GuildMember, Long> {
    List<GuildMember> findByGuild(Guild guild);
    List<GuildMember> findByUser(User user);
    Optional<GuildMember> findByGuildAndUser(Guild guild, User user);
    boolean existsByGuildAndUser(Guild guild, User user);
    void deleteByGuildAndUser(Guild guild, User user);
}
