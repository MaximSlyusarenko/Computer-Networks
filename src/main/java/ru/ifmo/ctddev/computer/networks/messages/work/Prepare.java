package ru.ifmo.ctddev.computer.networks.messages.work;

import lombok.Getter;
import lombok.Setter;
import ru.ifmo.ctddev.computer.networks.messages.Message;

/**
 * Created by vi34 on 04/03/2017.
 */
@Getter
@Setter
public class Prepare extends Message {
    public static final String HEADER = "PREPARE";
    private String name;

    public Prepare(String json) {
        _decode(json);
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public void _decode(String s) {
        Prepare prepare = gson.fromJson(s, Prepare.class);
        this.name = prepare.name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isPrepare() {
        return true;
    }

    @Override
    public Prepare asPrepare() {
        return this;
    }
}
