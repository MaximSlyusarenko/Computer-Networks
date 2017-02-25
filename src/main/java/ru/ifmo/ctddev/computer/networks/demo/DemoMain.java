package ru.ifmo.ctddev.computer.networks.demo;

import ru.ifmo.ctddev.computer.networks.messages.ConsumerResponse;
import ru.ifmo.ctddev.computer.networks.messages.Message;

import java.net.InetAddress;

/**
 * @author Alexey Katsman
 * @since 25.02.17
 */

public class DemoMain {
    public static void main(String[] args) throws Exception {
        String s = "0hjfdё";
        ConsumerResponse consumerResponse = new ConsumerResponse("a", InetAddress.getLocalHost(), s);
        String json = consumerResponse.encode();
        System.out.println(json);
        Message message = Message.decode(json);

        if (message.isConsumerResponse()) {
            ConsumerResponse response = message.asConsumerResponse();
            System.out.println(response.getGeneratedFileName());
        }
    }
}
