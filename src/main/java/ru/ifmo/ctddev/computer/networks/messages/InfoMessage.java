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
public class InfoMessage extends Message {
    public static final String HEADER = "InfoMessage";

    private String name;
    private String message;
    private InetAddress ip;

    public InfoMessage(String json) {
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
        InfoMessage infoMessage = gson.fromJson(s, InfoMessage.class);
        this.name = infoMessage.name;
        this.message = infoMessage.message;
        this.ip = infoMessage.ip;
    }

    @Override
    public boolean isInfoMessage() {
        return true;
    }

    @Override
    public InfoMessage asInfoMessage() {
        return this;
    }
}
