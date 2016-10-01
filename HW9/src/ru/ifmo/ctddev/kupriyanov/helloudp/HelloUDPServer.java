package ru.ifmo.ctddev.kupriyanov.helloudp;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *  Crawler that recursively walks the websites and downloads pages
 *
 *  @author pinkdonut
 */
public class HelloUDPServer implements HelloServer {
    private static final int MAX_PACKET_SIZE = 4096;
    private DatagramSocket serverSocket;

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            return;
        }
        try {
            int portNumber = Integer.parseInt(args[0]);
            int threadNumber = Integer.parseInt(args[1]);
            new HelloUDPServer().start(portNumber, threadNumber);
        } catch (NumberFormatException e) {
            printUsage();
        }
    }

    private static void printUsage() {
        System.err.println("Wrong usage of the server!");
        System.out.println("Usage:\n\tHelloUDPServer <port number> <number of threads to start>");
    }

    @Override
    public void start(int portNumber, int threadNumber) {
        try {
            serverSocket = new DatagramSocket(portNumber);
            for (int i = 0; i < threadNumber; i++) {
                new Thread(new UDPResolver(serverSocket), "Worker thread #" + i).start();
            }
        } catch (SocketException e) {
            System.err.println("Error occurred while creating HelloUDPServer on the given port, cause: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    private static class UDPResolver implements Runnable {
        static final String RESPONSE_PREFIX = "Hello, ";
        private final DatagramSocket serverSocket;

        UDPResolver(DatagramSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            while (!serverSocket.isClosed()) {
                byte[] receiveData = new byte[MAX_PACKET_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                    String request = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
                    String response = RESPONSE_PREFIX + request;

                    byte[] responseBytes = response.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, receivePacket.getSocketAddress());

                    serverSocket.send(responsePacket);
                } catch (SocketException e) {
                    // socket closed. work finished
                } catch (IOException e) {
                    System.err.println("Exception while working on " + Thread.currentThread().getName() + ", cause: " + e.getMessage());
                }
            }
        }
    }
}
