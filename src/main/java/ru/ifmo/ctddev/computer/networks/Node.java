package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.containers.NodeInfo;
import ru.ifmo.ctddev.computer.networks.messages.Acknowledgement;
import ru.ifmo.ctddev.computer.networks.messages.ConsumerRequest;
import ru.ifmo.ctddev.computer.networks.messages.Find;
import ru.ifmo.ctddev.computer.networks.messages.Message;
import ru.ifmo.ctddev.computer.networks.messages.Resolve;
import ru.ifmo.ctddev.computer.networks.messages.ResolveResponse;

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

    protected abstract void getConsumerResult();

    protected abstract String getType();

    protected abstract void getFile(String fileName);

    protected void findFileAndSend(String fileName, String address) {
        throw new UnsupportedOperationException("Consumer can't perform file send operation");
    }

    private Message receiveMessage(DatagramSocket socket) throws IOException {
        byte[] receiveData = new byte[BUFFER_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        String packet = new String(receiveData, Charset.forName("UTF-8")).substring(0, receivePacket.getLength());
        System.out.println("Got packet: " + packet);

        if (TYPE_CONSUMER.equals(getType())) {
            System.out.print("> ");
        }

        return Message.decode(packet);
    }

    void receiveUnicast() {
        try {
            uSocket = new DatagramSocket(RECEIVE_UNICAST_PORT);
            while (true) {
                Message message = receiveMessage(uSocket);
                if (message.isAcknowledgement()) {
                    Acknowledgement ack = message.asAcknowledgement();
                    addToSomeMap(ack.getType(), ack.getName());
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
                if (message.isFind()) {
                    Find find = message.asFind();
                    if (Objects.equals(find.getName(), name)) {
                        continue;
                    }
                    addToSomeMap(find.getType(), find.getName());
                    System.out.println("Got Find request from " + find.getName());

                    if (TYPE_CONSUMER.equals(getType())) {
                        System.out.print("> ");
                    }

                    send(new Acknowledgement(name, getType()), find.getIp().getHostName(), RECEIVE_UNICAST_PORT);
                } else if (message.isResolve()) {
                    Resolve resolve = message.asResolve();
                    if (Objects.equals(resolve.getName(), name)) {
                        continue;
                    }
                    send(new ResolveResponse(name, selfIP), MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
                } else if (message.isResolveResponse()) {
                    ResolveResponse resolveResponse = message.asResolveResponse();
                    if (Objects.equals(resolveResponse.getName(), name)) {
                        continue;
                    }
                    addToSomeMap(resolveResponse.getName(), resolveResponse.getIp());
                } else if (message.isConsumerRequest()) {
                    ConsumerRequest consumerRequest = message.asConsumerRequest();

                    if (TYPE_CONSUMER.equals(getType()) || Objects.equals(consumerRequest.getName(), name)) {
                        continue;
                    }

                    findFileAndSend(consumerRequest.getFileName(), consumerRequest.getIp().getHostAddress());
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
        producers.computeIfPresent(name, ($, $$) -> new NodeInfo(ip, 0, TimeUnit.SECONDS)); // TODO: add normal ttl
        consumers.computeIfPresent(name, ($, $$) -> new NodeInfo(ip, 0, TimeUnit.SECONDS)); // TODO: add normal ttl
    }

    private void addToSomeMap(String type, String name) {
        if (TYPE_PRODUCER.equals(type)) {
            producers.put(name, new NodeInfo());
        } else if (TYPE_CONSUMER.equals(type)) {
            consumers.put(name, new NodeInfo());
        } else {
            throw new IllegalArgumentException("Incorrect type: type must be " + TYPE_PRODUCER + " or " + TYPE_CONSUMER);
        }
    }

    public String info() {
        return "producers: " + producers.toString() + "\nconsumers:" + consumers.toString() + "\n";
    }
}
