package ru.ifmo.ctddev.computer.networks.messages.work;

import ru.ifmo.ctddev.computer.networks.messages.Message;

/**
 * Created by vi34 on 04/03/2017.
 */
public class Ready extends Message {
    public static final String HEADER = "READY";
    private String name;

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public void _decode(String s) {
        Ready haveWork = gson.fromJson(s, Ready.class);
        this.name = haveWork.name;
    }

    @Override
    public String getName() {
        return name;
    }
}
