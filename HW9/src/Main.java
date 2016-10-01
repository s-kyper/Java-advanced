import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.Util;
import org.junit.Assert;
import ru.ifmo.ctddev.kupriyanov.helloudp.HelloUDPClient;
import ru.ifmo.ctddev.kupriyanov.helloudp.HelloUDPServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicInteger;

import static info.kgeorgiy.java.advanced.hello.HelloClientTest.PREFIX;

/**
 * test
 */
public class Main {

    /**
     * Entry point for {@link Main}.
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        HelloUDPServer server = new HelloUDPServer();
        server.start(3338, 3);
        Thread.sleep(500);
        HelloUDPClient client = new HelloUDPClient();
        client.start("127.0.0.1", 3338, "bla", 3, 3);
        server.close();
    }
}