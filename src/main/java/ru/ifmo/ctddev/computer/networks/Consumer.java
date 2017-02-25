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

    @Override
    protected String getType() {
        return Node.TYPE_CONSUMER;
    }

    private void initSend() {
        new Thread(() -> {
            Find find = new Find(Node.TYPE_CONSUMER, name, selfIP);
            send(find, MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        }).start();
    }

    private void initReceive() {
        new Thread(this::receiveMulticast).start();
        new Thread(this::receiveUnicast).start();
    }

    public static void main(String[] args) {
        Consumer consumer = new Consumer("printer");
        consumer.initSend();
        consumer.initReceive();
    }
}
