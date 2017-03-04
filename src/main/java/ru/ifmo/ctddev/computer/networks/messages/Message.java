package ru.ifmo.ctddev.computer.networks.messages;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by vi34 on 25/02/2017.
 */
@Getter
@Setter
public abstract class Message {
    protected static Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public String encode() {
        JsonObject jsonObject = gson.toJsonTree(this).getAsJsonObject();
        jsonObject.addProperty("header", getHeader());
        return jsonObject.toString();
    }

    public abstract String getHeader();
    public abstract void _decode(String s);
    public abstract String getName();

    public static Message decode(String json) {
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(json);
        String header = jsonTree.getAsJsonObject().get("header").getAsString();
        return build(header, json);
    }

    private static Message build(String header, String json) {
        switch (header) {
            case Find.HEADER: return new Find(json);
            case Acknowledgement.HEADER: return new Acknowledgement(json);
            case Resolve.HEADER: return new Resolve(json);
            case ResolveResponse.HEADER: return new ResolveResponse(json);
            case ConsumerRequest.HEADER: return new ConsumerRequest(json);
            case InfoMessage.HEADER: return new InfoMessage(json);
        }
        return null;
    }

    public boolean isFind() {
        return false;
    }

    public Find asFind() {
        return null;
    }

    public boolean isAcknowledgement() {
        return false;
    }

    public Acknowledgement asAcknowledgement() {
        return null;
    }

    public boolean isResolve() {
        return false;
    }

    public Resolve asResolve() {
        return null;
    }

    public boolean isResolveResponse() {
        return false;
    }

    public ResolveResponse asResolveResponse() {
        return null;
    }

    public boolean isConsumerRequest() {
        return false;
    }

    public ConsumerRequest asConsumerRequest() {
        return null;
    }

    public boolean isInfoMessage() {
        return false;
    }

    public InfoMessage asInfoMessage() {
        return null;
    }
}
