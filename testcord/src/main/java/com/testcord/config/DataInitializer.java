package com.testcord.config;

import com.testcord.model.*;
import com.testcord.repository.*;
import com.testcord.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;
    private final BotTokenService botTokenService;
    private final GuildService guildService;
    private final ChannelService channelService;
    private final UserRepository userRepository;

    public DataInitializer(UserService userService,
                            BotTokenService botTokenService,
                            GuildService guildService,
                            ChannelService channelService,
                            UserRepository userRepository) {
        this.userService = userService;
        this.botTokenService = botTokenService;
        this.guildService = guildService;
        this.channelService = channelService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping initialization.");
            return;
        }

        log.info("Seeding initial data...");

        User admin = userService.register("admin", "admin123");
        log.info("Created admin user: {}", admin.getId());

        String botToken = botTokenService.createBot("SP4RK");
        log.info("Created bot SP4RK with token: {}", botToken);

        Guild guild = guildService.createGuild("Testcord HQ", admin.getId());
        guildService.addMember(guild, admin);

        User botUser = botTokenService.validateToken(botToken).orElseThrow();
        guildService.addMember(guild, botUser);

        Channel general = channelService.createChannel("general", guild);
        Channel botCommands = channelService.createChannel("bot-commands", guild);

        log.info("Created guild '{}' with channels '{}' and '{}'",
                guild.getName(), general.getId(), botCommands.getId());
        log.info("==============================================");
        log.info("  TESTCORD READY!");
        log.info("  Dashboard: http://localhost:5000");
        log.info("  Gateway WS: ws://localhost:5000/gateway");
        log.info("  Bot Token: {}", botToken);
        log.info("  Guild ID: {}", guild.getId());
        log.info("  General Channel ID: {}", general.getId());
        log.info("==============================================");
    }
}
