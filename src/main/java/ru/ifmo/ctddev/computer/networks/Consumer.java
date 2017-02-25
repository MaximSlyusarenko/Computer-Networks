package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.Find;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
public class Consumer extends Node {

    Consumer(String name) {
        super(name);
    }

    @Override
    protected void getConsumerResult() {
        throw new UnsupportedOperationException("Producer operation for Consumer");
    }

    @Override
    protected void getFile() {

    }

    private void initSend() {
        new Thread(() -> {
            Find find = new Find("consumer", "printer");
            send(find, MULTICAST_ADDRESS, RECEIVE_PORT);
        }).start();
    }

    private void initReceive() {
        new Thread(this::receive).start();
    }

    public static void main(String[] args) {
        Consumer consumer = new Consumer("printer");
        consumer.initSend();
        consumer.initReceive();
    }
}
