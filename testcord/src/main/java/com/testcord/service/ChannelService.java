package com.testcord.service;

import com.testcord.model.Channel;
import com.testcord.model.Guild;
import com.testcord.model.Message;
import com.testcord.model.User;
import com.testcord.repository.ChannelRepository;
import com.testcord.repository.MessageRepository;
import com.testcord.websocket.GatewaySessionManager;
import com.testcord.dto.MessageDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final SnowflakeService snowflakeService;
    private final GatewaySessionManager gatewaySessionManager;

    public ChannelService(ChannelRepository channelRepository,
                          MessageRepository messageRepository,
                          SnowflakeService snowflakeService,
                          GatewaySessionManager gatewaySessionManager) {
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
        this.snowflakeService = snowflakeService;
        this.gatewaySessionManager = gatewaySessionManager;
    }

    public Optional<Channel> findById(String id) {
        return channelRepository.findById(id);
    }

    public List<Channel> findByGuild(Guild guild) {
        return channelRepository.findByGuild(guild);
    }

    @Transactional
    public Channel createChannel(String name, Guild guild) {
        Channel channel = new Channel(snowflakeService.generate(), name, guild);
        return channelRepository.save(channel);
    }

    @Transactional
    public Message sendMessage(Channel channel, User author, String content) {
        Message message = new Message(snowflakeService.generate(), channel, author, content);
        message = messageRepository.save(message);

        MessageDTO dto = MessageDTO.from(message);
        gatewaySessionManager.broadcastToGuild(
                channel.getGuild() != null ? channel.getGuild().getId() : null,
                "MESSAGE_CREATE", dto);

        return message;
    }

    public List<Message> getMessages(Channel channel, int limit) {
        return messageRepository.findByChannelOrderByTimestampDesc(
                channel, PageRequest.of(0, Math.min(limit, 100)));
    }
}
