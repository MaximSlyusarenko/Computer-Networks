package ru.ifmo.ctddev.computer.networks.messages.work;

import ru.ifmo.ctddev.computer.networks.messages.Message;

/**
 * Created by vi34 on 04/03/2017.
 */
public class HaveWork extends Message {
    public static final String HEADER = "HAVE_WORK";
    private String name;

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public void _decode(String s) {
        HaveWork haveWork = gson.fromJson(s, HaveWork.class);
        this.name = haveWork.name;
    }

    @Override
    public String getName() {
        return name;
    }
}
