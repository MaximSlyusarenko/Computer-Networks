package ru.ifmo.ctddev.computer.networks.containers;

import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
@Getter
@Setter
public class NodeInfo {
    private InetAddress inetAddress;

    /**
     * Expiration time in millis
     */
    private long expirationTime;

    public NodeInfo() {
        expirationTime = -1;
    }

    public NodeInfo(InetAddress inetAddress, long ttl, TimeUnit timeUnit) {
        this.inetAddress = inetAddress;
        this.expirationTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(ttl, timeUnit);
    }
}
