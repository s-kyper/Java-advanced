package ru.ifmo.ctddev.kupriyanov.helloudp;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 *  Crawler that recursively walks the websites and downloads pages
 *
 *  @author pinkdonut
 */
public class HelloUDPClient implements HelloClient {
    private static final int MAX_PACKET_SIZE = 4096;

    public static void main(String[] args) {
        if (args.length != 5) {
            printUsage();
        }
        try {
            String hostName = args[0];
            int portNumber = Integer.parseInt(args[1]);
            String queryPrefix = args[2];
            int threadNumber = Integer.parseInt(args[3]);
            int queryPerThread = Integer.parseInt(args[4]);

            new HelloUDPClient().start(hostName, portNumber, queryPrefix, queryPerThread, threadNumber);
        } catch (NumberFormatException e) {
            printUsage();
        }
    }

    private static void printUsage() {
        System.err.println("Wrong usage of the client!");
        System.out.println("Usage:\n\tHelloUDPClient <server name or ip-address> <server port number> <query prefix>" + " <number of parallel threads> <number of queries in each thread>");
    }

    @Override
    public void start(String hostName, int portNumber, String queryPrefix, int queryPerThread, int threadNumber) {
        final InetSocketAddress serverAddress = new InetSocketAddress(hostName, portNumber);
        ArrayList<Thread> threads = new ArrayList<>();

        for (int threadId = 0; threadId < threadNumber; threadId++) {
            Thread thread = new Thread(new ConnectResolver(serverAddress, queryPrefix, threadId, queryPerThread));
            thread.setName("Worker thread #" + threadId);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Exception while joining " + thread.getName() + ", cause:" + e.getMessage());
            }
        }
    }

    private static class ConnectResolver implements Runnable {
        private static final String RESPONSE_PREFIX = "Hello, ";
        private final InetSocketAddress serverAddress;
        private final String queryPrefix;
        private final int threadId;
        private final int queryPerThread;

        ConnectResolver(InetSocketAddress serverAddress, String queryPrefix, int threadId, int queryPerThread) {
            this.serverAddress = serverAddress;
            this.queryPrefix = queryPrefix;
            this.threadId = threadId;
            this.queryPerThread = queryPerThread;
        }

        @Override
        public void run() {
            try (DatagramSocket serverSocket = new DatagramSocket()) {
                serverSocket.setSoTimeout(5000);
                byte[] receiveBytes = new byte[MAX_PACKET_SIZE];
                DatagramPacket responsePacket = new DatagramPacket(receiveBytes, receiveBytes.length);

                for (int queryId = 0; queryId < queryPerThread; queryId++) {
                    String data = queryPrefix + threadId + "_" + queryId;
                    byte[] sendBytes = data.getBytes("UTF-8");
                    DatagramPacket queryPacket = new DatagramPacket(sendBytes, sendBytes.length, serverAddress);

                    while (true) {
                        try {
                            serverSocket.send(queryPacket);
                            serverSocket.receive(responsePacket);

                            String queryString = new String(queryPacket.getData(), queryPacket.getOffset(), queryPacket.getLength());
                            String responseString = new String(responsePacket.getData(), responsePacket.getOffset(), responsePacket.getLength());

                            int expectedResponseLength = queryString.length() + RESPONSE_PREFIX.length();
                            if (responseString.length() == expectedResponseLength && RESPONSE_PREFIX.equals(responseString.substring(0, RESPONSE_PREFIX.length())) && queryString.equals(responseString.substring(RESPONSE_PREFIX.length()))) {

                            /*    System.out.println(queryString);
                                System.out.println(responseString);*/
                                break;
                            }
                        } catch (SocketTimeoutException e) {
                            // repeat it
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Exception occurred while working on " + Thread.currentThread().getName() + ", cause: " + e.getMessage());
            }
        }
    }
}