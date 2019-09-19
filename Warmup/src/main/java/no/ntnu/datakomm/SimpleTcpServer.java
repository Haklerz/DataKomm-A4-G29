package no.ntnu.datakomm;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * A Simple TCP client, used as a warm-up exercise for assignment A4.
 */
public class SimpleTcpServer {
    // The listening wellcoming socket
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        SimpleTcpServer server = new SimpleTcpServer();
        log("Simple TCP server starting");
        server.run();
        log("ERROR: the server should never go out of the run() method! After handling one client");
    }

    /**
     * TODO: Multithreading and allowing multiple request/response cycles per session.
     */
    public void run() {
        while (!this.serverSocket.isClosed()) {
            log("Server waiting for connection");
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
                log("Client connected to server");
            } catch (IOException e) {
                log("ERROR: An I/O error occured while waiting for connection");
            }
            if (clientSocket != null) {
                Scanner inFromClient = null;
                PrintWriter outToClient = null;
                try {
                    inFromClient = new Scanner(clientSocket.getInputStream());
                    outToClient = new PrintWriter(clientSocket.getOutputStream(), true);
                } catch (IOException e) {
                    log("ERROR: Connection to client has been lost");
                }
                if (inFromClient != null && outToClient != null) {
                    String request = inFromClient.nextLine();
                    String response = "error";

                    if ("game over".equalsIgnoreCase(request)) {
                        try {
                            this.serverSocket.close();
                        } catch (IOException e) {
                            log("ERROR: An I/O error occured while closing the server");
                        }
                    } else {
                        String[] numberStrings = request.split("\\+");

                        if (numberStrings.length == 2) {
                            response = "" + (Integer.parseInt(numberStrings[0]) + Integer.parseInt(numberStrings[1]));
                        }
                        outToClient.println(response);
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            log("ERROR: An I/O error occured while closing connection");
                        }
                    }
                }
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
