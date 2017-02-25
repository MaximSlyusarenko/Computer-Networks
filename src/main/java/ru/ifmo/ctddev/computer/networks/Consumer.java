package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.Find;

import java.util.Scanner;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
public class Consumer extends Node {

    private Thread multiReciever;
    private Thread uniReciever;
    private Thread sender;

    Consumer(String name) {
        super(name);
    }

    @Override
    protected void getConsumerResult() {
        throw new UnsupportedOperationException("Producer operation for Consumer");
    }

    @Override
    protected void getFile(String name) {

    }

    @Override
    protected String getType() {
        return Node.TYPE_CONSUMER;
    }

    private void initSend() {
        sender = new Thread(() -> {
            Find find = new Find(Node.TYPE_CONSUMER, name, selfIP);
            send(find, MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        });
        sender.start();
    }

    private void initReceive() {
        multiReciever = new Thread(this::receiveMulticast);
        uniReciever = new Thread(this::receiveUnicast);
        multiReciever.start();
        uniReciever.start();
    }

    public static void main(String[] args) {
        Consumer consumer = new Consumer("printer");
        consumer.initSend();
        consumer.initReceive();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            switch (scanner.next()) {
                case "get":
                    String name = scanner.next();
                    consumer.getFile(name);
                    break;
                case "exit": case "q":
                    scanner.close();
                    consumer.multiReciever.interrupt();
                    consumer.sender.interrupt();
                    consumer.uniReciever.interrupt();
                    consumer.close();
                    System.out.println("bye");
                    return;

                default:
                    System.out.println("unknown command");
            }
        }
    }
}
