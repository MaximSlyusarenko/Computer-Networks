package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.Acknowledgement;
import ru.ifmo.ctddev.computer.networks.messages.ConsumerRequest;
import ru.ifmo.ctddev.computer.networks.messages.Find;
import ru.ifmo.ctddev.computer.networks.messages.InfoMessage;
import ru.ifmo.ctddev.computer.networks.messages.Message;
import ru.ifmo.ctddev.computer.networks.messages.ResolveResponse;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
public class Producer extends Node {

    Producer(String name) {
        super(name);
    }

    public static void main(String[] args) {
        String name = args.length == 0 ? "Producer" : args[0];
        Producer producer = new Producer(name);
        producer.initSend();
        producer.initReceive();
        new ScheduledThreadPoolExecutor(1).scheduleWithFixedDelay(producer::initListFiles, 0, 10, TimeUnit.SECONDS);
    }

    private void initSend() {
        new Thread(() -> {
            Find find = new Find(Node.TYPE_PRODUCER, name, selfIP);
            send(find, MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        }).start();
    }    @Override
    protected String getType() {
        return Node.TYPE_PRODUCER;
    }

    private void initReceive() {
        new Thread(this::receiveMulticast).start();
        new Thread(this::receiveUnicast).start();
    }    @Override
    protected void processMessage(Message message) {
        if (message.isFind()) {
            Find find = message.asFind();
            addToSomeMap(find.getType(), find.getName());
            System.out.println("Got Find request from " + find.getName());
            send(new Acknowledgement(name, getType()), find.getIp().getHostName(), RECEIVE_UNICAST_PORT);
        } else if (message.isResolve()) {
            send(new ResolveResponse(name, selfIP), MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        } else if (message.isResolveResponse()) {
            ResolveResponse resolveResponse = message.asResolveResponse();
            addToSomeMap(resolveResponse.getName(), resolveResponse.getIp());
        } else if (message.isConsumerRequest()) {
            ConsumerRequest consumerRequest = message.asConsumerRequest();
            findFileAndSend(consumerRequest.getFileName(), consumerRequest.getIp().getHostAddress());
        }
    }

    protected void getFile(String fileName) {
        throw new UnsupportedOperationException("Consumer operation for Producer");
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
