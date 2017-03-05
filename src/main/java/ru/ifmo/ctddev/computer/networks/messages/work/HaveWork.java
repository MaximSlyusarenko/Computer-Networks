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
public class HaveWork extends Message {
    public static final String HEADER = "HAVE_WORK";
    private String name;
    private String workId;
    private InetAddress ip;

    public HaveWork(String json) {
        _decode(json);
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public void _decode(String s) {
        HaveWork haveWork = gson.fromJson(s, HaveWork.class);
        this.name = haveWork.name;
        this.workId = haveWork.workId;
        this.ip = haveWork.ip;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isHaveWork() {
        return true;
    }

    @Override
    public HaveWork asHaveWork() {
        return this;
    }
}
