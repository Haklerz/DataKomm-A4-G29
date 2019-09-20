package no.ntnu.datakomm;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Simple TCP client, used as a warm-up exercise for assignment A4.
 */
public class SimpleTcpServer {
    // The welcoming socket
    private ServerSocket serverSocket;
    // The thread pool
    private ExecutorService threadPool;

    public static void main(String[] args) {
        SimpleTcpServer server = new SimpleTcpServer();
        log("Simple TCP server starting");
        server.run();
        log("ERROR: the server should never go out of the run() method! After handling one client");
    }

    public void run() {
        threadPool = Executors.newFixedThreadPool(18);
        try {
            serverSocket = new ServerSocket(1301);
        } catch (IOException e) {
            log("ERROR: An I/O error occured when opening server");
        }
        while (true) {
            try {
                log("Waiting for new client to connect");
                threadPool.execute(new SimpleTcpClientHandler(serverSocket.accept()));
            } catch (IOException e) {
                log("ERROR: An I/O error occured while waiting for client");
            }
        }
    }

    /**
     * Log a message to the system console.
     *
     * @param message The message to be logged (printed).
     */
    private static void log(String message) {
        System.out.println(message);
    }
}
