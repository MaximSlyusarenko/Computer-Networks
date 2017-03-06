package ru.ifmo.ctddev.computer.networks.messages;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */

@Getter
@Setter
public class Resolve extends Message {

    public static final String HEADER = "Resolve";

    private String name;

    public Resolve(String json) {
        _decode(json);
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public void _decode(String s) {
        Resolve resolve = gson.fromJson(s, Resolve.class);
        this.name = resolve.name;
    }

    @Override
    public boolean isResolve() {
        return true;
    }

    @Override
    public Resolve asResolve() {
        return this;
    }
}
