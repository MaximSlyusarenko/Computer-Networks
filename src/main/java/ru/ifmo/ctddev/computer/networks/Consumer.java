package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.Find;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Set;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
public class Consumer extends  Node {

    private Set<String> producers;

    Consumer(String name) {
        super(name);
    }

    private void initSend() {
        new Thread(() -> {
            Find find = new Find("consumer", "printer");
            send(find, MULTICAST_ADDRESS, RECEIVE_PORT);
        }).start();
    }

    private void initReceive() {
        new Thread(() -> {

        }).start();
    }

    public static void main(String[] args) {
        Consumer consumer = new Consumer("printer");
        consumer.initSend();
        consumer.initReceive();
    }
}
