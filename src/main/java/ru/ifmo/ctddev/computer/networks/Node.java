package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.Acknowledgement;
import ru.ifmo.ctddev.computer.networks.messages.Find;
import ru.ifmo.ctddev.computer.networks.messages.Message;

import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by vi34 on 25/02/2017.
 */
public abstract class Node {
    static final String MULTICAST_ADDRESS = "225.4.5.6";
    static final int RECEIVE_PORT = 8080;
    static final int BUFFER_SIZE = 2048;
    static final String TYPE_CONSUMER = "consumer";
    static final String TYPE_PRODUCER = "producer";

    private final Set<String> producers = new ConcurrentSkipListSet<>();
    private final Set<String> consumers = new ConcurrentSkipListSet<>();

    public String name;
    InetAddress selfIP;


    Node(String name) {
        this.name = name;
        try {
            selfIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("can't get selfIP");
            e.printStackTrace();
        }
    }

    void send(Message message, String address, int port) {
        try (MulticastSocket socket = new MulticastSocket()) {
            byte[] content = message.encode().getBytes();
            socket.send(new DatagramPacket(content, content.length, InetAddress.getByName(address), port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void getConsumerResult();
    protected abstract void getFile();
    protected abstract String getType();

    private Message receiveMessage(DatagramSocket socket) throws IOException {
        byte[] receiveData = new byte[BUFFER_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        String packet = new String(receiveData).substring(0, receivePacket.getLength());
        System.out.println("Got packet: " + packet);
        return Message.decode(packet);
    }

    void receiveUnicast() {
        try (DatagramSocket socket = new DatagramSocket(RECEIVE_PORT)) {
            while (true) {
                Message message = receiveMessage(socket);
                if (message instanceof Acknowledgement) {
                    Acknowledgement ack = (Acknowledgement) message;
                    addToSomeSet(ack.getType(), ack.getName());
                } /*else if (message instanceof ConsumerRequest) {
                    getFile();
                } else if (message instanceof ConsumerResponse) {
                    getConsumerResult();
                } */ // TODO
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void receiveMulticast() {
        try (MulticastSocket socket = new MulticastSocket(RECEIVE_PORT)) {
            socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
            while (true) {
                Message message = receiveMessage(socket);
                if (message instanceof Find) {
                    Find find = (Find) message;
                    if (find.getName().equals(name)) {
                        continue;
                    }
                    addToSomeSet(find.getType(), find.getName());
                    System.out.println("got Find request from " + ((Find) message).getName());
                    send(new Acknowledgement(name, getType()), find.getIp().getHostName(), RECEIVE_PORT);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToSomeSet(String type, String name) {
        if (TYPE_PRODUCER.equals(type)) {
            producers.add(name);
        } else if (TYPE_CONSUMER.equals(type)) {
            consumers.add(name);
        } else {
            throw new IllegalArgumentException("Incorrect type: type must be " + TYPE_PRODUCER + " or " + TYPE_CONSUMER);
        }
    }
}
