package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.Find;
import ru.ifmo.ctddev.computer.networks.messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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

    protected abstract void getConsumerResult();
    protected abstract void getFile();

    void receive() {
        try (MulticastSocket socket = new MulticastSocket(RECEIVE_PORT)) {
            socket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
            while (true) {
                StringBuilder messageString = new StringBuilder();
                byte[] receiveData = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                messageString.append(new String(receiveData).substring(0, receivePacket.getLength()));
                String packet = messageString.toString();
                System.out.println("Got packet: " + packet);
                Message message = Message.decode(packet);
                if (message instanceof Find) {
                    Find find = (Find) message;
                    if (TYPE_PRODUCER.equals(find.getType())) {
                        producers.add(find.getName());
                    } else if (TYPE_CONSUMER.equals(find.getType())) {
                        consumers.add(find.getName());
                    } else {
                        throw new IllegalArgumentException("Incorrect type: type must be " + TYPE_PRODUCER + " or " + TYPE_CONSUMER);
                    } /*else if (message instanceof ConsumerRequest) {
                        getFile();
                    } else if (message instanceof ConsumerResponse) {
                        getConsumerResult();
                    }*/ // TODO
                    System.out.println("got Find request from " + ((Find) message).getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
