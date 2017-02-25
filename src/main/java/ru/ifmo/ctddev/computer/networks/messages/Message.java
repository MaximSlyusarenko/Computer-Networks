package ru.ifmo.ctddev.computer.networks.messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Created by vi34 on 25/02/2017.
 */
public abstract class Message {
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public abstract String encode();
    public abstract void _decode(String s);
    public abstract String header();

    public static Message decode(String json) {
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(json);
        String header = jsonTree.getAsJsonObject().get("header").getAsString();
        return build(header, json);
    }

    private static Message build(String header, String json) {
        switch (header) {
            case Find.HEADER: return new Find(json);
        }
        return null;
    }
}