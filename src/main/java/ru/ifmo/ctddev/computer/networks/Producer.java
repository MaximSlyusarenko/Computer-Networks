package ru.ifmo.ctddev.computer.networks;

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
    protected void getFile() {
        throw new UnsupportedOperationException("Consumer operation for Producer");
    }

    public static void main(String[] args) {

    }
}
