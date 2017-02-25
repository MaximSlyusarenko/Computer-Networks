package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.Find;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
public class Producer extends Node {

    Producer(String name) {
        super(name);
    }

    @Override
    protected void getConsumerResult() {

    }

    @Override
    protected void getFile(String name) {
        throw new UnsupportedOperationException("Consumer operation for Producer");
    }

    @Override
    protected String getType() {
        return Node.TYPE_PRODUCER;
    }

    private void initSend() {
        new Thread(() -> {
            Find find = new Find(Node.TYPE_PRODUCER, name, selfIP);
            send(find, MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        }).start();
    }

    private void initReceive() {
        new Thread(this::receiveMulticast).start();
        new Thread(this::receiveUnicast).start();
    }

    public static void main(String[] args) {
        Producer producer = new Producer("not printer");
        producer.initSend();
        producer.initReceive();
    }
}
