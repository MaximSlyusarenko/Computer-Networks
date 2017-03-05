package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.containers.NodeInfo;
import ru.ifmo.ctddev.computer.networks.messages.Acknowledgement;
import ru.ifmo.ctddev.computer.networks.messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by vi34 on 25/02/2017.
 */
public abstract class Node {
    static final String MULTICAST_ADDRESS = "225.4.5.6";
    static final int RECEIVE_MULTICAST_PORT = 8080;
    static final int RECEIVE_UNICAST_PORT = 8081;
    static final int RECEIVE_FILE_PORT = 8082;
    static final int RECEIVE_WORK_PORT = 8083;
    static final int BUFFER_SIZE = 2048;
    static final String TYPE_CONSUMER = "consumer";
    static final String TYPE_PRODUCER = "producer";
    static final String TYPE_EXECUTOR = "executor";

    private final Map<String, NodeInfo> producers = new ConcurrentHashMap<>();
    private final Map<String, NodeInfo> consumers = new ConcurrentHashMap<>();
    private final Map<String, NodeInfo> executors = new ConcurrentHashMap<>();

    public String name;
    InetAddress selfIP;
    MulticastSocket socket;
    DatagramSocket uSocket;

    Node(String name) {
        this.name = name;
        try {
            selfIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Can't get selfIP");
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

    protected abstract String getType();

    protected abstract void processMessage(Message message);

    protected void findFileAndSend(String fileName, String address) {
        throw new UnsupportedOperationException("Consumer can't perform file send operation");
    }

    private Message receiveMessage(DatagramSocket socket) throws IOException {
        byte[] receiveData = new byte[BUFFER_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        String packet = new String(receiveData, Charset.forName("UTF-8")).substring(0, receivePacket.getLength());
        Message message = Message.decode(packet);

        if (Objects.equals(name, message.getName())) {
            return message;
        }

        System.out.println("Got packet: " + packet);

        if (TYPE_CONSUMER.equals(getType())) {
            System.out.print("> ");
        }

        return message;
    }

    void receiveUnicast() {
        try {
            uSocket = new DatagramSocket(RECEIVE_UNICAST_PORT);
            while (true) {
                Message message = receiveMessage(uSocket);
                if (message.isAcknowledgement()) {
                    Acknowledgement ack = message.asAcknowledgement();
                    addToSomeMap(ack.getType(), ack.getName());
                } else {
                    processMessage(message);
                }
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

                if (Objects.equals(message.getName(), name)) {
                    continue;
                }

                processMessage(message);
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

    protected void addToSomeMap(String name, InetAddress ip) {
        producers.computeIfPresent(name, ($, $$) -> new NodeInfo(ip, 0, TimeUnit.SECONDS)); // TODO: add normal ttl
        consumers.computeIfPresent(name, ($, $$) -> new NodeInfo(ip, 0, TimeUnit.SECONDS)); // TODO: add normal ttl
    }

    protected void addToSomeMap(String type, String name) {
        if (TYPE_PRODUCER.equals(type)) {
            producers.put(name, new NodeInfo());
        } else if (TYPE_CONSUMER.equals(type)) {
            consumers.put(name, new NodeInfo());
        } else if (TYPE_EXECUTOR.equals(type)) {
            executors.put(name, new NodeInfo());
        } else {
            throw new IllegalArgumentException("Incorrect type: type must be " + TYPE_PRODUCER + ", " + TYPE_CONSUMER + " or " + TYPE_EXECUTOR);
        }
    }

    public String info() {
        return "producers: " + producers.toString() + "\nconsumers:" + consumers.toString() + "\nexecutors:" + executors.toString() + "\n";
    }
}
