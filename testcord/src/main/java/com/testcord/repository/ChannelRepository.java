package com.testcord.repository;

import com.testcord.model.Channel;
import com.testcord.model.Guild;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChannelRepository extends JpaRepository<Channel, String> {
    List<Channel> findByGuild(Guild guild);
}
