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
public class WorkResult extends Message {

    public static final String HEADER = "WorkResult";

    private String name;
    private String workId;
    private Boolean result;
    private Long sumTime;

    public WorkResult(String json) {
        _decode(json);
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public void _decode(String s) {
        WorkResult workResult = gson.fromJson(s, WorkResult.class);
        this.name = workResult.name;
        this.workId = workResult.workId;
        this.result = workResult.result;
        this.sumTime = workResult.sumTime;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isWorkResult() {
        return true;
    }

    @Override
    public WorkResult asWorkResult() {
        return this;
    }
}
