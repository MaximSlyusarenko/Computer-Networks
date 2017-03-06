package ru.ifmo.ctddev.computer.networks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.ifmo.ctddev.computer.networks.io.FastScanner;
import ru.ifmo.ctddev.computer.networks.messages.Acknowledgement;
import ru.ifmo.ctddev.computer.networks.messages.ConsumerRequest;
import ru.ifmo.ctddev.computer.networks.messages.Find;
import ru.ifmo.ctddev.computer.networks.messages.Message;
import ru.ifmo.ctddev.computer.networks.messages.ResolveResponse;
import ru.ifmo.ctddev.computer.networks.messages.work.HaveWork;
import ru.ifmo.ctddev.computer.networks.messages.work.Ready;
import ru.ifmo.ctddev.computer.networks.messages.work.Work;
import ru.ifmo.ctddev.computer.networks.messages.work.WorkDeclined;
import ru.ifmo.ctddev.computer.networks.messages.work.WorkResult;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
public class Consumer extends Node {

    private static final int WORK_PARTS = 2;
    private static final int ONE_NODE_EXECUTE_DELAY = 30000;
    private static final int ALL_NODES_EXECUTE_DELAY = 60000;

    private Thread multiReciever;
    private Thread uniReciever;
    private Thread fileReceiver;
    private Thread sender;

    private Map<String, Integer> executorsForWork = new ConcurrentHashMap<>();

    private Map<String, WorkInfo> workIdToWork = new ConcurrentHashMap<>();

    /**
     * Map from workId to names of executors which have finished this work
     */
    private Map<String, List<String>> worksInProgress = new ConcurrentHashMap<>();

    private Map<String, List<Boolean>> workResults = new ConcurrentHashMap<>();

    private ServerSocket serverSocket;

    Consumer(String name) {
        super(name);
    }

    public static void main(String[] args) {
        String name = args.length == 0 ? "Consumer" : args[0];
        Consumer consumer = new Consumer(name);
        consumer.initSend();
        consumer.initReceive();

        FastScanner scanner = new FastScanner();
        while (true) {
            System.out.print("> ");
            switch (scanner.next().toLowerCase()) {
                case "get":
                    String fileName = scanner.next();
                    consumer.getFile(fileName);
                    break;
                case "work":
                    BigInteger number = new BigInteger(scanner.next());
                    consumer.sendHaveWork(number);
                    break;
                case "i":
                    System.out.println(consumer.info());
                    break;
                case "exit":
                case "q":
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
                    System.out.println("Print \"work\" <number> to test if number is prime");
                    System.out.println("You could use number \"373587031\" to see performance on two executors");
                    System.out.println("Print \"i\" to get info about consumers and producers");
                    System.out.println("Print \"exit\" or \"q\" to stop this consumer");
                    break;

                default:
                    System.out.println("Unknown command");
            }
        }
    }

    private void getFile(String fileName) {
        send(new ConsumerRequest(this.name, fileName, selfIP), MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
    }

    private void sendHaveWork(String workId, BigInteger number) {
        workIdToWork.put(workId, new WorkInfo(number));
        executorsForWork.put(workId, 0);
        send(new HaveWork(name, workId, selfIP), MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        worksInProgress.put(workId, new ArrayList<>());
        workResults.put(workId, new ArrayList<>());
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (worksInProgress.containsKey(workId)) {
                    WorkInfo workInfo = workIdToWork.get(workId);
                    sendHaveWork(workId, workInfo.getNumber());
                }
            }
        }, ALL_NODES_EXECUTE_DELAY);
    }

    private void sendHaveWork(BigInteger number) {
        String workId = UUID.randomUUID().toString();
        sendHaveWork(workId, number);
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
        } else if (message.isReady()) {
            Ready ready = message.asReady();
            executorsForWork.compute(ready.getWorkId(), (key, prevValue) -> {
                if (prevValue == null) {
                    prevValue = 0;
                }
                if (prevValue == WORK_PARTS) {
                    send(new WorkDeclined(name, ready.getWorkId()), ready.getIp().getHostName(), RECEIVE_UNICAST_PORT);
                    return prevValue;
                } else {
                    sendWork(ready.getIp().getHostName(), ready.getWorkId(), workIdToWork.get(ready.getWorkId()), prevValue);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (worksInProgress.containsKey(ready.getWorkId())) {
                                WorkInfo workInfo = workIdToWork.get(ready.getWorkId());
                                sendHaveWork(ready.getWorkId(), workInfo.getNumber());
                            }
                        }
                    }, ONE_NODE_EXECUTE_DELAY);
                    return prevValue + 1;
                }
            });
        } else if (message.isWorkResult()) {
            WorkResult result = message.asWorkResult();
            workResults.compute(result.getWorkId(), (key, prevValue) -> {
                prevValue.add(result.getResult());
                return prevValue;
            });
            worksInProgress.compute(result.getWorkId(), (key, prevValue) -> {
                prevValue.add(result.getName());
                return prevValue;
            });

            System.out.printf(Locale.ENGLISH, "Get work result for work \"%s\" from \"%s\", result is \"%s\"\n", result.getWorkId(), result.getName(), String.valueOf(result.getResult()));

            if (worksInProgress.get(result.getWorkId()).size() == WORK_PARTS) {
                boolean finalResult = workResults.get(result.getWorkId())
                        .stream()
                        .reduce((b1, b2) -> b1 && b2)
                        .orElse(false);

                worksInProgress.remove(result.getWorkId());
                worksInProgress.remove(result.getWorkId());
                System.out.printf(Locale.ENGLISH, "Number is %s\n", finalResult ? "prime" : "not prime");
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

    private void sendWork(String address, String workName, WorkInfo workInfo, int part) {
        try (Socket socket = new Socket(address, RECEIVE_WORK_PORT);
             DataOutputStream socketOutputStream = new DataOutputStream(socket.getOutputStream())) {

            socket.setSendBufferSize(BUFFER_SIZE);
            System.out.printf(Locale.ENGLISH, "Sending work \"%s\" from \"%s\" to \"%s\"\n", workName, selfIP, address);
            String message = String.format(Locale.ENGLISH, "Receiving work \"%s\" from \"%s\" with address \"%s\"", workName, name, selfIP);
            socketOutputStream.writeUTF(message);
            socketOutputStream.writeUTF(workName);
            BigInteger start = BigInteger.ONE;
            BigInteger finish = workInfo.getNumber().divide(BigInteger.valueOf(2));

            if (part == 1) {
                start = finish;
                finish = workInfo.getNumber();
            }

            socketOutputStream.writeUTF(new Work(name, workName, selfIP, workInfo.getNumber(), start, finish).encode());

            System.out.printf(Locale.ENGLISH, "Work \"%s\" sent\n", workName);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @AllArgsConstructor
    @Getter
    private class WorkInfo {
        private BigInteger number;
    }
}
