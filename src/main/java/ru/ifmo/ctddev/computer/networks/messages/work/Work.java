package ru.ifmo.ctddev.computer.networks.messages.work;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.ifmo.ctddev.computer.networks.messages.Message;

import java.math.BigInteger;
import java.net.InetAddress;

/**
 * @author Maxim Slyusarenko
 * @since 04.03.17
 */

@Getter
@Setter
@AllArgsConstructor
public class Work extends Message {

    public static final String HEADER = "Work";

    private String name;
    private String workId;
    private InetAddress ip;
    private BigInteger number;
    private BigInteger start;
    private BigInteger finish;

    public Work(String json) {
        _decode(json);
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public void _decode(String s) {
        Work work = gson.fromJson(s, Work.class);
        this.name = work.name;
        this.workId = work.workId;
        this.ip = work.ip;
        this.number = work.number;
        this.start = work.start;
        this.finish = work.finish;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isWork() {
        return true;
    }

    @Override
    public Work asWork() {
        return this;
    }
}
