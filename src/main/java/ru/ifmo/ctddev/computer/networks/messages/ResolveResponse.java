package ru.ifmo.ctddev.computer.networks.messages;

import com.google.gson.JsonObject;
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
@AllArgsConstructor
public class ResolveResponse extends Message {
    public static final String HEADER = "ResolveResponse";

    private String name;
    private InetAddress ip;

    public ResolveResponse(String json) {
        _decode(json);
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public void _decode(String s) {
        ResolveResponse resolveResponse = gson.fromJson(s, ResolveResponse.class);
        this.name = resolveResponse.name;
        this.ip = resolveResponse.ip;
    }

    @Override
    public boolean isResolveResponse() {
        return true;
    }

    @Override
    public ResolveResponse asResolveResponse() {
        return this;
    }
}
