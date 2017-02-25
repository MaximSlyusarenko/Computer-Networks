package ru.ifmo.ctddev.computer.networks;

import java.io.IOException;
import java.net.*;
import java.util.Set;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
public class Consumer {

    private static final String MULTICAST_ADDRESS = "225.4.5.6";
    private static final int RECEIVE_PORT = 8080;

    private Set<String> producers;

    private static void initSend() {
        new Thread(() -> {
            try (MulticastSocket socket = new MulticastSocket()) {
                byte[] content = "Consumer".getBytes();
                socket.send(new DatagramPacket(content, content.length, InetAddress.getByName(MULTICAST_ADDRESS), RECEIVE_PORT));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void initReceive() {
        new Thread(() -> {
            try (MulticastSocket socket = new MulticastSocket(RECEIVE_PORT)) {
                socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
                while (true) {
                    byte[] receiveData = new byte[256];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    System.out.println("Got packet: " + new String(receiveData).substring(0, receivePacket.getLength()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        initReceive();
        initSend();
    }
}
