package ru.ifmo.ctddev.computer.networks;

import ru.ifmo.ctddev.computer.networks.messages.Acknowledgement;
import ru.ifmo.ctddev.computer.networks.messages.Find;
import ru.ifmo.ctddev.computer.networks.messages.Message;
import ru.ifmo.ctddev.computer.networks.messages.work.HaveWork;
import ru.ifmo.ctddev.computer.networks.messages.work.Ready;
import ru.ifmo.ctddev.computer.networks.messages.work.Work;
import ru.ifmo.ctddev.computer.networks.messages.work.WorkDeclined;
import ru.ifmo.ctddev.computer.networks.messages.work.WorkResult;

import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Executor extends Node {
    private static final int WORK_THREADS = 5;
    private static final int LOAD_TIME_IF_NOT_WORK = 10000;

    private ServerSocket serverSocket;

    private Set<String> worksWeAreReadyFor = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private ExecutorService executorService = Executors.newFixedThreadPool(WORK_THREADS + 3); //3 for receive
    private AtomicInteger load = new AtomicInteger(0); // count load manually

    Executor(String name) {
        super(name);
    }

    public static void main(String[] args) {
        String name = args.length == 0 ? "Executor" : args[0];
        Executor executor = new Executor(name);
        executor.initSend();
        executor.initReceive();
    }

    private void initSend() {
        executorService.submit(() -> {
            Find find = new Find(Node.TYPE_EXECUTOR, name, selfIP);
            send(find, MULTICAST_ADDRESS, RECEIVE_MULTICAST_PORT);
        });
    }

    private void initReceive() {
        executorService.submit(this::receiveMulticast);
        executorService.submit(this::receiveUnicast);
        executorService.submit(this::receiveWork);
    }

    @Override
    protected void processMessage(Message message) {
        if (message.isFind()) {
            Find find = message.asFind();
            addToSomeMap(find.getType(), find.getName());
            System.out.printf(Locale.ENGLISH, "Got Find request from %s\n", find.getName());
            send(new Acknowledgement(name, getType()), find.getIp().getHostName(), RECEIVE_UNICAST_PORT);
        } else if (message.isHaveWork()) {
            HaveWork haveWork = message.asHaveWork();
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
        } else if (message.isWorkDeclined()) {
            WorkDeclined workDeclined = message.asWorkDeclined();
            worksWeAreReadyFor.remove(workDeclined.getWorkId());
            load.decrementAndGet();
        }
    }

    @Override
    protected String getType() {
        return Node.TYPE_EXECUTOR;
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
                    System.out.printf(Locale.ENGLISH, "Received work \"%s\"\n> ", workName);
                    String currentWork = socketInputStream.readUTF();

                    executorService.submit(() -> {
                        worksWeAreReadyFor.remove(currentWork);
                        System.out.printf(Locale.ENGLISH, "Before work %s\n", currentWork);
                        Work work = new Work(currentWork);
                        System.out.printf(Locale.ENGLISH, "Work \"%s\" started\n", work.getWorkId());
                        long resultTime = System.currentTimeMillis();
                        boolean result = doWork(work.getNumber(), work.getStart(), work.getFinish());
                        resultTime = System.currentTimeMillis() - resultTime;
                        System.out.printf(Locale.ENGLISH, "Work \"%s\" was successfully executed!\n", work.getWorkId());
                        send(new WorkResult(name, work.getWorkId(), result, resultTime), work.getIp().getHostName(), RECEIVE_UNICAST_PORT);
                    });
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

    private boolean doWork(BigInteger number, BigInteger start, BigInteger finish) {
        start = start.add(BigInteger.ONE);

        while (!start.equals(finish)) {
            if (number.divideAndRemainder(start)[1].equals(BigInteger.ZERO)) {
                return false;
            }

            start = start.add(BigInteger.ONE);
        }

        return true;
    }
}
