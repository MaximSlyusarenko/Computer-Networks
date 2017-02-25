package ru.ifmo.ctddev.computer.networks.containers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
@Getter
@Setter
public class NodeInfo {
    private InetAddress inetAddress;
    /**
     * Need expiration time and time to live to update expiration time using ttl in some responses
     */
    private long expirationTime;
    /**
     * Time to live in seconds
     */
    private int ttl;

    public NodeInfo(InetAddress inetAddress, int ttl) {
        this.inetAddress = inetAddress;
        this.ttl = ttl;
        this.expirationTime = System.currentTimeMillis() + this.ttl * 1000;
    }
}
