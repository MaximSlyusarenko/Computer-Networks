package ru.ifmo.ctddev.computer.networks.messages.work;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.ctddev.computer.networks.messages.Message;

import java.net.InetAddress;

/**
 * @author Maxim Slyusarenko
 * @since 04.03.17
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Work extends Message {
    public static final String HEADER = "Work";
    private String name;
    private String workId;
    private InetAddress ip;
    private int sleep;
    private String result;

    public Work(String json) {
        _decode(json);
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public String encode() {
        String res = super.encode();
        return res + "#";
    }

    @Override
    public void _decode(String s) {
        Work work = gson.fromJson(s, Work.class);
        this.name = work.name;
        this.workId = work.workId;
        this.ip = work.ip;
        this.sleep = work.sleep;
        this.result = work.result;
    }

    @Override
    public String getName() {
        return name;
    }
}
