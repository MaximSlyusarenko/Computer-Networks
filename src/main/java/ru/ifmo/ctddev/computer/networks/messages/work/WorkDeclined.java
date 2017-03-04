package ru.ifmo.ctddev.computer.networks.messages.work;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.ifmo.ctddev.computer.networks.messages.Message;

import java.net.InetAddress;

/**
 * @author Maxim Slyusarenko
 * @since 04.03.17
 */
@Getter
@Setter
public class WorkDeclined extends Message {
    public static final String HEADER = "WorkDeclined";
    private String name;
    private String workId;

    public WorkDeclined(String name, String workId) {
        this.name = name;
        this.workId = workId;
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
}
