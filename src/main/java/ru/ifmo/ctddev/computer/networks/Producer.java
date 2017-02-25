package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.Find;
import ru.ifmo.ctddev.computer.networks.messages.InfoMessage;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Locale;
import java.util.UUID;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
public class Producer extends Node {

    Producer(String name) {
        super(name);
    }

    @Override
    protected void getConsumerResult() {

    }

    @Override
    protected void getFile(String fileName) {
        throw new UnsupportedOperationException("Consumer operation for Producer");
    }

    @Override
    protected String getType() {
        return Node.TYPE_PRODUCER;
    }

    private void initSend() {
        new Thread(() -> {
            Find find = new Find(Node.TYPE_PRODUCER, name, selfIP);
            send(find, MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        }).start();
    }

    private void initReceive() {
        new Thread(this::receiveMulticast).start();
        new Thread(this::receiveUnicast).start();
    }

    public static void main(String[] args) {
        Producer producer = new Producer("not printer");
        producer.initSend();
        producer.initReceive();
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
            String message = String.format(Locale.ENGLISH, "Receiving file \"%s\" from \"%s\" with address \"%s\"", generatedFileName, name, address);
            socketOutputStream.writeUTF(message);
            socketOutputStream.writeUTF(generatedFileName);

            do {
                bytesReadNow = bufferedInputStream.read(buffer, 0, BUFFER_SIZE);

                if (bytesReadNow > 0) {
                    socketOutputStream.write(buffer, 0, bytesReadNow);
                }
            } while (bytesReadNow > -1);
        } catch (FileNotFoundException e) {
            String message = String.format(Locale.ENGLISH, "File %s doesn't exist", fileName);
            send(new InfoMessage(name, message, selfIP), address, RECEIVE_UNICAST_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
