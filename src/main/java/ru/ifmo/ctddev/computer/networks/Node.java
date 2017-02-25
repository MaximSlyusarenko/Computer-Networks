package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.containers.NodeInfo;
import ru.ifmo.ctddev.computer.networks.messages.*;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by vi34 on 25/02/2017.
 */
public abstract class Node {
    static final String MULTICAST_ADDRESS = "225.4.5.6";
    static final int RECEIVE_MULTICAST_PORT = 8080;
    static final int RECEIVE_UNICAST_PORT = 8081;
    static final int BUFFER_SIZE = 2048;
    static final String TYPE_CONSUMER = "consumer";
    static final String TYPE_PRODUCER = "producer";

    private final Map<String, NodeInfo> producers = new ConcurrentHashMap<>();
    private final Map<String, NodeInfo> consumers = new ConcurrentHashMap<>();

    public String name;
    InetAddress selfIP;
    MulticastSocket socket;
    DatagramSocket uSocket;

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
    protected abstract String getType();
    protected abstract void getFile(String name);

    private Message receiveMessage(DatagramSocket socket) throws IOException {
        byte[] receiveData = new byte[BUFFER_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        String packet = new String(receiveData).substring(0, receivePacket.getLength());
        System.out.println("Got packet: " + packet);
        return Message.decode(packet);
    }

    void receiveUnicast() {
        try {
            uSocket = new DatagramSocket(RECEIVE_UNICAST_PORT);
            while (true) {
                Message message = receiveMessage(uSocket);
                if (message instanceof Acknowledgement) {
                    Acknowledgement ack = (Acknowledgement) message;
                    addToSomeMap(ack.getType(), ack.getName());
                } /*else if (message instanceof ConsumerRequest) {
                    getFile();
                } else if (message instanceof ConsumerResponse) {
                    getConsumerResult();
                } */ // TODO
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (uSocket != null) {
                uSocket.close();
            }
        }
    }

    void receiveMulticast() {
        try {
            socket = new MulticastSocket(RECEIVE_MULTICAST_PORT);
            socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
            while (true) {
                Message message = receiveMessage(socket);
                if (message instanceof Find) {
                    Find find = (Find) message;
                    if (find.getName().equals(name)) {
                        continue;
                    }
                    addToSomeMap(find.getType(), find.getName());
                    System.out.println("got Find request from " + ((Find) message).getName());
                    send(new Acknowledgement(name, getType()), find.getIp().getHostName(), RECEIVE_UNICAST_PORT);
                } else if (message instanceof Resolve) {
                    Resolve resolve = (Resolve) message;
                    if (resolve.getName().equals(name)) {
                        continue;
                    }
                    send(new ResolveResponse(name, selfIP), MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
                } else if (message instanceof ResolveResponse) {
                    ResolveResponse resolveResponse = (ResolveResponse) message;
                    if (resolveResponse.getName().equals(name)) {
                        continue;
                    }
                    addToSomeMap(resolveResponse.getName(), resolveResponse.getIp());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }


    void close() {
        socket.close();
        uSocket.close();
    }

    private void addToSomeMap(String name, InetAddress ip) {
        producers.computeIfPresent(name, ($, $$) -> new NodeInfo(ip, 0)); // TODO: add normal ttl
        consumers.computeIfPresent(name, ($, $$) -> new NodeInfo(ip, 0));
    }

    private void addToSomeMap(String type, String name) {
        if (TYPE_PRODUCER.equals(type)) {
            producers.put(name, new NodeInfo(null, -1));
        } else if (TYPE_CONSUMER.equals(type)) {
            consumers.put(name, new NodeInfo(null, -1));
        } else {
            throw new IllegalArgumentException("Incorrect type: type must be " + TYPE_PRODUCER + " or " + TYPE_CONSUMER);
        }
    }
}
