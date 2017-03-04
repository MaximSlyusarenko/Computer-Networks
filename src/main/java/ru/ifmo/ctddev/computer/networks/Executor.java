package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Executor extends Node {

    Executor(String name) {
        super(name);
    }

    @Override
    protected String getType() {
        return Node.TYPE_PRODUCER;
    }

    @Override
    protected void processMessage(Message message) {
        if (message.isFind()) {
            Find find = message.asFind();
            addToSomeMap(find.getType(), find.getName());
            System.out.println("Got Find request from " + find.getName());
            send(new Acknowledgement(name, getType()), find.getIp().getHostName(), RECEIVE_UNICAST_PORT);
        }
    }

    private void initSend() {
        new Thread(() -> {
            Find find = new Find(Node.TYPE_PRODUCER, name, selfIP);
            send(find, MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        }).start();
    }

    private void initListFiles() {
        String availableFiles = "";

        try {
            availableFiles = Files
                    .list(new File(".").toPath())
                    .map(Path::toString)
                    .collect(Collectors.joining(";"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        InfoMessage infoMessage = new InfoMessage(name, availableFiles, selfIP);

        send(infoMessage, MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
    }

    private void initReceive() {
        new Thread(this::receiveMulticast).start();
        new Thread(this::receiveUnicast).start();
    }

    public static void main(String[] args) {
        Executor producer = new Executor("Alex");
        producer.initSend();
        producer.initReceive();
        new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(producer::initListFiles, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    protected void findFileAndSend(String fileName, String address) {
        if (fileName == null) {
            throw new IllegalArgumentException("File name can not be null");
        }

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(new File(fileName)));
             Socket socket = new Socket(address, RECEIVE_FILE_PORT);
             DataOutputStream socketOutputStream = new DataOutputStream(socket.getOutputStream())) {

            int bytesReadNow;
            byte[] buffer = new byte[BUFFER_SIZE];
            socket.setSendBufferSize(BUFFER_SIZE);
            String generatedFileName = UUID.randomUUID().toString() + " " + fileName;
            System.out.printf(Locale.ENGLISH, "Sending file \"%s\" from \"%s\" to \"%s\"", generatedFileName, selfIP, address);
            String message = String.format(Locale.ENGLISH, "Receiving file \"%s\" from \"%s\" with address \"%s\"", generatedFileName, name, selfIP);
            socketOutputStream.writeUTF(message);
            socketOutputStream.writeUTF(generatedFileName);

            do {
                bytesReadNow = bufferedInputStream.read(buffer, 0, BUFFER_SIZE);

                if (bytesReadNow > 0) {
                    socketOutputStream.write(buffer, 0, bytesReadNow);
                }
            } while (bytesReadNow > -1);

            System.out.printf(Locale.ENGLISH, "File \"%s\" sent", generatedFileName);
        } catch (FileNotFoundException e) {
            String message = String.format(Locale.ENGLISH, "File %s doesn't exist", fileName);
            send(new InfoMessage(name, message, selfIP), address, RECEIVE_UNICAST_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
