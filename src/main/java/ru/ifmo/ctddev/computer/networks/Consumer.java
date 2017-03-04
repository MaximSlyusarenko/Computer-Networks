package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.io.FastScanner;
import ru.ifmo.ctddev.computer.networks.messages.*;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
public class Consumer extends Node {

    private Thread multiReciever;
    private Thread uniReciever;
    private Thread fileReceiver;
    private Thread sender;

    private ServerSocket serverSocket;

    Consumer(String name) {
        super(name);
    }

    protected void getFile(String fileName) {
        send(new ConsumerRequest(this.name, fileName, selfIP), MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
    }

    @Override
    protected String getType() {
        return Node.TYPE_CONSUMER;
    }

    @Override
    protected void processMessage(Message message) {
        if (message.isFind()) {
            Find find = message.asFind();
            addToSomeMap(find.getType(), find.getName());
            System.out.println("Got Find request from " + find.getName());
            System.out.print("> ");
            send(new Acknowledgement(name, getType()), find.getIp().getHostName(), RECEIVE_UNICAST_PORT);
        } else if (message.isResolve()) {
            send(new ResolveResponse(name, selfIP), MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        } else if (message.isResolveResponse()) {
            ResolveResponse resolveResponse = message.asResolveResponse();
            addToSomeMap(resolveResponse.getName(), resolveResponse.getIp());
        }
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
        fileReceiver = new Thread(this::receiveFileConnection);
        multiReciever.start();
        uniReciever.start();
        fileReceiver.start();
    }

    private void receiveFileConnection() {
        try {
            serverSocket = new ServerSocket(RECEIVE_FILE_PORT);

            while (true) {
                Socket fileSocket = null;
                DataInputStream socketInputStream = null;
                BufferedOutputStream outputStream = null;

                try {
                    fileSocket = serverSocket.accept();
                    socketInputStream = new DataInputStream(fileSocket.getInputStream());
                    System.out.println(socketInputStream.readUTF());
                    String fileName = socketInputStream.readUTF();
                    outputStream = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesReadNow;

                    do {
                        bytesReadNow = socketInputStream.read(buffer, 0, BUFFER_SIZE);

                        if (bytesReadNow > 0) {
                            outputStream.write(buffer, 0, bytesReadNow);
                        }
                    } while (bytesReadNow > -1);

                    System.out.printf(Locale.ENGLISH, "Received file \"%s\"\n> ", fileName);
                } finally {
                    if (fileSocket != null) {
                        fileSocket.close();
                    }

                    if (socketInputStream != null) {
                        socketInputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    void close() {
        super.close();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Consumer consumer = new Consumer("Viktor");
        consumer.initSend();
        consumer.initReceive();

        FastScanner scanner = new FastScanner();
        while (true) {
            System.out.print("> ");
            switch (scanner.next().toLowerCase()) {
                case "get":
                    String name = scanner.next();
                    consumer.getFile(name);
                    break;
                case "i":
                    System.out.println(consumer.info());
                    break;
                case "exit": case "q":
                    scanner.close();
                    consumer.multiReciever.interrupt();
                    consumer.sender.interrupt();
                    consumer.uniReciever.interrupt();
                    consumer.fileReceiver.interrupt();
                    consumer.close();
                    System.out.println("bye");
                    return;
                case "help":
                    System.out.println("Print \"get <file name>\" to get file from producers");
                    System.out.println("Print \"exit\" or \"q\" to stop this consumer");
                    break;

                default:
                    System.out.println("Unknown command");
            }
        }
    }
}
