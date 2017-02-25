package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.Find;
import ru.ifmo.ctddev.computer.networks.messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by vi34 on 25/02/2017.
 */
public class Node {
    static final String MULTICAST_ADDRESS = "225.4.5.6";
    static final int RECEIVE_PORT = 8080;

    public String name;

    Node(String name) {
        this.name = name;
    }

    void send(Message message, String address, int port) {
        try (MulticastSocket socket = new MulticastSocket()) {
            byte[] content = message.encode().getBytes();
            socket.send(new DatagramPacket(content, content.length, InetAddress.getByName(address), port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void receive() {
        try (MulticastSocket socket = new MulticastSocket(RECEIVE_PORT)) {
            socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
            while (true) {
                byte[] receiveData = new byte[2048];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String packet = new String(receiveData).substring(0, receivePacket.getLength());
                System.out.println("Got packet: " + packet);
                Message message = Message.decode(packet);
                if (message instanceof Find) {
                    System.out.println("got Find request from " + ((Find) message).getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
