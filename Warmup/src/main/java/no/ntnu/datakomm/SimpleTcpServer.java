package no.ntnu.datakomm;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Simple TCP client, used as a warm-up exercise for assignment A4.
 */
public class SimpleTcpServer {
    // The max number of threads
    private static final int THREAD_COUNT = 32;
    // The TCP port
    private static final int PORT = 1301;
    // The welcoming socket
    private ServerSocket serverSocket;
    // The thread pool
    private ExecutorService threadPool;

    /**
     * Instanziates a new server.
     */
    public SimpleTcpServer() {
        this.threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    public static void main(String[] args) {
        SimpleTcpServer server = new SimpleTcpServer();
        log("Simple TCP server starting");
        server.run();
        log("ERROR: the server should never go out of the run() method! After handling one client");
    }

    /**
     * Runs the server.
     */
    public void run() {
        openServer();
        while (serverIsOpen())
            acceptClient();
        closeServer();
    }

    /**
     * Returns wether the server is open.
     * 
     * @return wether the server is open
     */
    private boolean serverIsOpen() {
        return !serverSocket.isClosed();
    }

    /**
     * Closes the server socket.
     */
    private void closeServer() {
        try {
            log("Closing server");
            serverSocket.close();
        } catch (IOException e) {
            log("ERROR: An I/O error occured when closing server");
        }
    }

    /**
     * Accepts a client and handles the client in a seperate thread. Method blocks
     * and waits until a client connects.
     */
    private void acceptClient() {
        try {
            log("Waiting for new client to connect");
            threadPool.execute(new SimpleTcpClientHandler(serverSocket.accept()));
        } catch (IOException e) {
            log("ERROR: An I/O error occured while waiting for client");
        }
    }

    /**
     * Opens the server socket to a port.
     */
    private void openServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            log("Server opened on port: " + PORT);
        } catch (IOException e) {
            log("ERROR: An I/O error occured when opening server");
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
