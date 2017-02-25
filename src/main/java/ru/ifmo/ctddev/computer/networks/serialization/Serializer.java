package ru.ifmo.ctddev.computer.networks.serialization;

/**
 * @author Maxim Slyusarenko
 * @since 25.02.17
 */
public interface Serializer {

    Object decode();
    Object encode();
}
