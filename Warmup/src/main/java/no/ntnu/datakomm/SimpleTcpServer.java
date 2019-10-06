package no.ntnu.datakomm;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Simple TCP client, used as a warm-up exercise for assignment A4.
 */
public class SimpleTcpServer {
    // Stores the max number of threads.
    private static final int THREAD_COUNT = 8;

    // The TCP port for the server.
    private static final int PORT = 1301;

    // Holds the welcome socket.
    private ServerSocket welcomeSocket;

    // Holds the thread pool.
    private ExecutorService threadPool;

    /**
     * Instantiate a new server with a number of maximum allocated threads.
     */
    public SimpleTcpServer()
    {
        this.threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    public static void main(String[] args) {
        SimpleTcpServer server = new SimpleTcpServer();
        log("Simple TCP server starting");
        server.run();
        log("ERROR: the server should never go out of the run() method! After handling one client");
    }

    public void run() {
        // TODO - implement the logic of the server, according to the protocol.
        // Take a look at the tutorial to understand the basic blocks: creating a listening socket,
        // accepting the next client connection, sending and receiving messages and closing the connection

        this.createWelcomeSocket(); // Creates a welcome socket for the client.
        while ( this.serverRunning() ) // Continuously runs while server is running.
        {
            this.acceptClient(); // Accepts the client to the server.
        }
        this.closeWelcomeSocket(); // Closes the welcome socket when no longer in use.
    }

    /**
     * Accept the client to the server.
     */
    private void acceptClient()
    {
        try
        {
            log("Waiting for new client to connect.");
            Socket clientSocket = this.welcomeSocket.accept(); // Waits for a connection to be made to this socket and accepts.
            SimpleTcpClientHandler simpleTcpClientHandler = new SimpleTcpClientHandler(clientSocket); // Creates a handler for the TCP client.
            this.threadPool.execute(simpleTcpClientHandler); // Execute a given command at some time in the future.
            log("Client successfully connected.");
        } catch (IOException clientCreatedError)
        {
            System.out.println("Error: " + clientCreatedError.getMessage());
        }
    }

    /**
     * Checks if the server is running.
     * @return true if server is running, else return false.
     */
    private boolean serverRunning()
    {
        boolean serverRunning = false;

        if ( !this.welcomeSocket.isClosed() ) // Checks if the welcome socket is closed.
        {
            serverRunning = true;
        }

        return serverRunning;
    }

    /**
     * Create a welcome socket.
     * //@return true if welcome socket created, otherwise return false.
     */
    private void createWelcomeSocket()
    {
        //boolean welcomeSocketCreated = false;
        try
        {
            this.welcomeSocket = new ServerSocket(this.PORT); // Tries to create the welcome socket.
            log("Welcome socket created for server on port: " + this.PORT);
            //welcomeSocketCreated = true;
        } catch (IOException welcomeSocketError)
        {
            System.out.println("Error: " + welcomeSocketError.getMessage());
        }

        //return welcomeSocketCreated;
    }

    /**
     * Closes the welcome socket.
     * @return true if welcome socket closed, otherwise returns false.
     */
    private void closeWelcomeSocket()
    {
        //boolean welcomeSocketClosed = false;

        try
        {
            this.welcomeSocket.close(); // Tries to close the welcome socket.
            log("Closing welcome socket for server on port " + this.PORT);
            //welcomeSocketClosed = true;
        } catch (IOException welcomeSocketError)
        {
            System.out.println("Error: " + welcomeSocketError.getMessage());
        }

        //return welcomeSocketClosed;
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