package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.*;
import ru.ifmo.ctddev.computer.networks.messages.work.HaveWork;
import ru.ifmo.ctddev.computer.networks.messages.work.Prepare;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Executor extends Node {
    public static final int WORK_THREADS = 10;

    ExecutorService executorService = Executors.newFixedThreadPool(WORK_THREADS + 2); //2 for receive

    Executor(String name) {
        super(name);
    }

    private Integer load; // count load manually

    public int getLoad() {
        return load;
    }

    @Override
    protected String getType() {
        return Node.TYPE_PRODUCER;
    }

    @Override
    protected void processMessage(Message message) {
        if (message.isFind()) {
            Find find = message.asFind();
            addToSomeMap(find.getType(), find.getName());
            System.out.println("Got Find request from " + find.getName());
            send(new Acknowledgement(name, getType()), find.getIp().getHostName(), RECEIVE_UNICAST_PORT);
        } else if (message.getHeader().equals(HaveWork.HEADER)) {
            InfoMessage infoMessage = new InfoMessage(name, load.toString(), selfIP);
            //send(infoMessage,  ,RECEIVE_UNICAST_PORT);
        }
    }

    private void initSend() {
        executorService.submit(() -> {
            Find find = new Find(Node.TYPE_PRODUCER, name, selfIP);
            send(find, MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        });
    }

    private void initReceive() {
        executorService.submit(this::receiveMulticast);
        executorService.submit(this::receiveUnicast);
    }

    public static void main(String[] args) {
        Executor producer = new Executor("Alex");
        producer.initSend();
        producer.initReceive();
    }

}
