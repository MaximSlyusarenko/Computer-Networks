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
public class ConsumerRequest extends Message {
    public static final String HEADER = "ConsumerRequest";

    private String name;
    private String fileName;
    private InetAddress ip;

    public ConsumerRequest(String json) {
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
        ConsumerRequest consumerRequest = gson.fromJson(s, ConsumerRequest.class);
        this.name = consumerRequest.name;
        this.fileName = consumerRequest.fileName;
        this.ip = consumerRequest.ip;
    }

    @Override
    public boolean isConsumerRequest() {
        return true;
    }

    @Override
    public ConsumerRequest asConsumerRequest() {
        return this;
    }
}
