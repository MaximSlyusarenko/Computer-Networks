package ru.ifmo.ctddev.computer.networks.messages.work;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.ifmo.ctddev.computer.networks.messages.Message;

import java.net.InetAddress;

/**
 * Created by vi34 on 04/03/2017.
 */
@Getter
@Setter
@AllArgsConstructor
public class Ready extends Message {
    public static final String HEADER = "READY";
    private String name;
    private String workId;
    private InetAddress ip;

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public void _decode(String s) {
        Ready ready = gson.fromJson(s, Ready.class);
        this.name = ready.name;
        this.workId = ready.workId;
        this.ip = ready.ip;
    }

    @Override
    public String getName() {
        return name;
    }
}
