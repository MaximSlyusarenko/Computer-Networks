package ru.ifmo.ctddev.computer.networks.messages;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;

/**
 * @author Alexey Katsman
 * @since 25.02.17
 */

@Getter
@Setter
@AllArgsConstructor
public class ConsumerResponse extends Message {
    public static final String HEADER = "ConsumerResponse";

    private String name;
    private InetAddress ip;
    private String generatedFileName;

    public ConsumerResponse(String json) {
        _decode(json);
    }

    @Override
    public String encode() {
        JsonObject jsonObject = gson.toJsonTree(this).getAsJsonObject();
        jsonObject.addProperty("header", HEADER);
        return jsonObject.toString();
    }

    @Override
    public void _decode(String s) {
        ConsumerResponse consumerResponse = gson.fromJson(s, ConsumerResponse.class);
        this.name = consumerResponse.name;
        this.ip = consumerResponse.ip;
        this.generatedFileName = consumerResponse.generatedFileName;
    }

    @Override
    public boolean isConsumerResponse() {
        return true;
    }

    @Override
    public ConsumerResponse asConsumerResponse() {
        return this;
    }
}
