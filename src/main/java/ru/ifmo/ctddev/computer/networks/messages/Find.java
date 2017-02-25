package ru.ifmo.ctddev.computer.networks.messages;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;

/**
 * Created by vi34 on 25/02/2017.
 */
@Getter
@Setter
@AllArgsConstructor
public class Find extends Message {
    public static final String HEADER = "FIND";
    private String type;
    private String name;
    private InetAddress ip;

    public Find(String json) {
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
        Find find = gson.fromJson(s, Find.class);
        this.type = find.type;
        this.name = find.name;
        this.ip = find.ip;
    }

    @Override
    public boolean isFind() {
        return true;
    }

    @Override
    public Find asFind() {
        return this;
    }
}
