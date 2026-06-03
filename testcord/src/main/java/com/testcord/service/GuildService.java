package com.testcord.service;

import com.testcord.model.*;
import com.testcord.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class GuildService {

    private final GuildRepository guildRepository;
    private final GuildMemberRepository memberRepository;
    private final BanRepository banRepository;
    private final UserRepository userRepository;
    private final SnowflakeService snowflakeService;

    public GuildService(GuildRepository guildRepository,
                        GuildMemberRepository memberRepository,
                        BanRepository banRepository,
                        UserRepository userRepository,
                        SnowflakeService snowflakeService) {
        this.guildRepository = guildRepository;
        this.memberRepository = memberRepository;
        this.banRepository = banRepository;
        this.userRepository = userRepository;
        this.snowflakeService = snowflakeService;
    }

    public Optional<Guild> findById(String id) {
        return guildRepository.findById(id);
    }

    public List<Guild> findByOwnerId(String ownerId) {
        return guildRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public Guild createGuild(String name, String ownerId) {
        Guild guild = new Guild(snowflakeService.generate(), name, ownerId);
        return guildRepository.save(guild);
    }

    public List<GuildMember> getMembers(Guild guild) {
        return memberRepository.findByGuild(guild);
    }

    @Transactional
    public boolean addMember(Guild guild, User user) {
        if (memberRepository.existsByGuildAndUser(guild, user)) {
            return false;
        }
        memberRepository.save(new GuildMember(guild, user));
        return true;
    }

    @Transactional
    public boolean kickMember(Guild guild, String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) return false;
        Optional<GuildMember> member = memberRepository.findByGuildAndUser(guild, user.get());
        if (member.isEmpty()) return false;
        memberRepository.delete(member.get());
        return true;
    }

    @Transactional
    public boolean banUser(Guild guild, String userId, String reason) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) return false;
        if (banRepository.existsByGuildAndUser(guild, user.get())) return false;
        kickMember(guild, userId);
        banRepository.save(new Ban(guild, user.get(), reason));
        return true;
    }

    @Transactional
    public boolean unbanUser(Guild guild, String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) return false;
        Optional<Ban> ban = banRepository.findByGuildAndUser(guild, user.get());
        if (ban.isEmpty()) return false;
        banRepository.delete(ban.get());
        return true;
    }

    public List<Ban> getBans(Guild guild) {
        return banRepository.findByGuild(guild);
    }

    public List<Guild> findGuildsByMember(User user) {
        return memberRepository.findByUser(user).stream()
                .map(GuildMember::getGuild)
                .toList();
    }
}
