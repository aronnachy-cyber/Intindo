package com.testcord.dto;

import lombok.Data;

@Data
public class GatewayEvent {
    private int op;
    private Object d;
    private Integer s;
    private String t;

    public static final int DISPATCH = 0;
    public static final int HEARTBEAT = 1;
    public static final int IDENTIFY = 2;
    public static final int HELLO = 10;
    public static final int HEARTBEAT_ACK = 11;

    public static GatewayEvent hello() {
        GatewayEvent e = new GatewayEvent();
        e.op = HELLO;
        e.d = java.util.Map.of("heartbeat_interval", 41250);
        return e;
    }

    public static GatewayEvent heartbeatAck() {
        GatewayEvent e = new GatewayEvent();
        e.op = HEARTBEAT_ACK;
        return e;
    }

    public static GatewayEvent dispatch(String type, Object data, int seq) {
        GatewayEvent e = new GatewayEvent();
        e.op = DISPATCH;
        e.t = type;
        e.d = data;
        e.s = seq;
        return e;
    }
}
