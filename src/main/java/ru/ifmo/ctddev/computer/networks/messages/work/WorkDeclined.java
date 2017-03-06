package ru.ifmo.ctddev.computer.networks.messages.work;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.ifmo.ctddev.computer.networks.messages.Message;

/**
 * @author Maxim Slyusarenko
 * @since 04.03.17
 */

@Getter
@Setter
@AllArgsConstructor
public class WorkDeclined extends Message {

    public static final String HEADER = "WorkDeclined";

    private String name;
    private String workId;

    public WorkDeclined(String json) {
        _decode(json);
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public void _decode(String s) {
        WorkDeclined workDeclined = gson.fromJson(s, WorkDeclined.class);
        this.name = workDeclined.name;
        this.workId = workDeclined.workId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isWorkDeclined() {
        return true;
    }

    @Override
    public WorkDeclined asWorkDeclined() {
        return this;
    }
}
