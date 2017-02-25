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
    protected void getFile() {
        throw new UnsupportedOperationException("Consumer operation for Producer");
    }

    private void initSend() {
        new Thread(() -> {
            Find find = new Find(Node.TYPE_PRODUCER, name);
            send(find, MULTICAST_ADDRESS, RECEIVE_PORT);
        }).start();
    }

    private void initReceive() {
        new Thread(this::receive).start();
    }

    public static void main(String[] args) {
        Producer producer = new Producer("not printer");
        producer.initSend();
        producer.initReceive();
    }
}
