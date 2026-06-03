package com.testcord.repository;

import com.testcord.model.Channel;
import com.testcord.model.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByChannelOrderByTimestampDesc(Channel channel, Pageable pageable);
}
