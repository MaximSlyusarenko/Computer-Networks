package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.*;
import ru.ifmo.ctddev.computer.networks.messages.work.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Executor extends Node {
    private static final int WORK_THREADS = 10;
    private static final int LOAD_TIME_IF_NOT_WORK = 10000;

    private ServerSocket serverSocket;

    private Set<String> worksWeAreReadyFor = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private Map<String, String> workNameToCurrentWork = new ConcurrentHashMap<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(WORK_THREADS + 3); //3 for receive

    Executor(String name) {
        super(name);
    }

    private AtomicInteger load = new AtomicInteger(0); // count load manually

    @Override
    protected String getType() {
        return Node.TYPE_EXECUTOR;
    }

    @Override
    protected void processMessage(Message message) {
        if (message.isFind()) {
            Find find = message.asFind();
            addToSomeMap(find.getType(), find.getName());
            System.out.println("Got Find request from " + find.getName());
            send(new Acknowledgement(name, getType()), find.getIp().getHostName(), RECEIVE_UNICAST_PORT);
        } else if (message instanceof HaveWork) {
            HaveWork haveWork = (HaveWork) message;
            worksWeAreReadyFor.remove(haveWork.getWorkId());
            int prevLoad = load.getAndUpdate(operand -> {
                if (operand < WORK_THREADS) {
                    send(new Ready(name, haveWork.getWorkId(), selfIP), haveWork.getIp().getHostName(), RECEIVE_UNICAST_PORT);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (worksWeAreReadyFor.contains(haveWork.getWorkId())) {
                                worksWeAreReadyFor.remove(haveWork.getWorkId());
                                load.decrementAndGet();
                            }
                        }
                    }, LOAD_TIME_IF_NOT_WORK);
                    return operand + 1;
                }
                return operand;
            });
            if (prevLoad < WORK_THREADS) {
                worksWeAreReadyFor.add(haveWork.getWorkId());
            }
        } else if (message instanceof WorkDeclined) {
            WorkDeclined workDeclined = (WorkDeclined) message;
            worksWeAreReadyFor.remove(workDeclined.getWorkId());
            load.decrementAndGet();
        }
    }

    private void receiveWork() {
        try {
            serverSocket = new ServerSocket(RECEIVE_WORK_PORT);

            while (true) {
                Socket workSocket = null;
                DataInputStream socketInputStream = null;

                try {
                    workSocket = serverSocket.accept();
                    socketInputStream = new DataInputStream(workSocket.getInputStream());
                    System.out.println(socketInputStream.readUTF());
                    String workName = socketInputStream.readUTF();
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesReadNow;

                    do {
                        bytesReadNow = socketInputStream.read(buffer, 0, BUFFER_SIZE);

                        if (bytesReadNow > 0) {
                            workNameToCurrentWork.compute(workName, (key, value) -> {
                                String currentWork;
                                if (value == null) {
                                    currentWork = new String(buffer);
                                } else {
                                    currentWork = value + new String(buffer);
                                }
                                if (currentWork.endsWith("#")) {
                                    worksWeAreReadyFor.remove(currentWork.substring(0, currentWork.length() - 1));
                                    executorService.submit(() -> {
                                        Work work = new Work();
                                        work._decode(currentWork.substring(0, currentWork.length() - 1));
                                        try {
                                            Thread.sleep(work.getSleep() * 1000);
                                        } catch (InterruptedException ignored) {
                                        }
                                        send(new WorkResult(name, work.getWorkId(), work.getResult()), work.getIp().getHostName(), RECEIVE_UNICAST_PORT);
                                    });
                                }
                                return currentWork;
                            });
                        }
                    } while (bytesReadNow > -1);

                    System.out.printf(Locale.ENGLISH, "Received work \"%s\"\n> ", workName);
                } finally {
                    if (workSocket != null) {
                        workSocket.close();
                    }
                    if (socketInputStream != null) {
                        socketInputStream.close();
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


    private void initSend() {
        executorService.submit(() -> {
            Find find = new Find(Node.TYPE_PRODUCER, name, selfIP);
            send(find, MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        });
    }

    private void initReceive() {
        executorService.submit(this::receiveMulticast);
        executorService.submit(this::receiveUnicast);
        executorService.submit(this::receiveWork);
    }

    public static void main(String[] args) {
        Executor producer = new Executor("Executor");
        producer.initSend();
        producer.initReceive();
    }

}
