package com.testcord.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SnowflakeService {

    private static final long TESTCORD_EPOCH = 1700000000000L;
    private final AtomicLong sequence = new AtomicLong(0);

    public synchronized String generate() {
        long timestamp = Instant.now().toEpochMilli() - TESTCORD_EPOCH;
        long seq = sequence.incrementAndGet() & 0xFFF;
        long snowflake = (timestamp << 22) | (1L << 17) | (1L << 12) | seq;
        return String.valueOf(snowflake);
    }
}
