package ru.ifmo.ctddev.computer.networks.messages;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
@Getter
@Setter
@AllArgsConstructor
public class Acknowledgement extends Message {
    public static final String HEADER = "ACK";

    private String name;
    private String type;

    @Override
    public String encode() {
        JsonObject jsonObject = gson.toJsonTree(this).getAsJsonObject();
        jsonObject.addProperty("header", HEADER);
        return jsonObject.toString();
    }

    @Override
    public void _decode(String s) {
        Acknowledgement ack = gson.fromJson(s, Acknowledgement.class);
        this.type = ack.type;
        this.name = ack.name;
    }

    @Override
    public boolean isAcknowledgement() {
        return true;
    }

    @Override
    public Acknowledgement asAcknowledgement() {
        return this;
    }
}
