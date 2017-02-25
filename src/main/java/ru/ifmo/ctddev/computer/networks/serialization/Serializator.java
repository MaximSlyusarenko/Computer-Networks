package ru.ifmo.ctddev.computer.networks.serialization;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
public interface Serializator {

    Object decode();
    Object encode();
}
