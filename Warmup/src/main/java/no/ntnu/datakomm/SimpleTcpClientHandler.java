package no.ntnu.datakomm;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SimpleTcpClientHandler implements Runnable {
    private Socket clientSocket;
    private Scanner inFromClient;
    private PrintWriter outToClient;

    public SimpleTcpClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        log("Client connected");
        openStreams();
        while (streamsAreOpen() && inFromClient.hasNextLine()) {
            String request = inFromClient.nextLine();
            if ("game over".equals(request)) {
                closeStreams();
            } else {
                String response = "error";
                String[] numberStrings = request.split("\\+");
                if (numberStrings.length == 2) {
                    try {
                        response = "" + (Integer.parseInt(numberStrings[0].trim()) + Integer.parseInt(numberStrings[1].trim()));
                    } catch (NumberFormatException e) {
                    }
                }
                outToClient.println(response);
                log("Responded to request < " + request + " > with < " + response + " >");
            }
        }

        closeSocket();
        log("Client disconnected");
    }

    private void closeSocket() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            log("ERROR: An I/O error occured when closing socket");
        }
    }

    private void closeStreams() {
        inFromClient.close();
        outToClient.close();
        inFromClient = null;
        outToClient = null;
    }

    private boolean streamsAreOpen() {
        return (inFromClient != null && outToClient != null);
    }

    private void openStreams() {
        try {
            inFromClient = new Scanner(clientSocket.getInputStream());
            outToClient = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            log("ERROR: Connection to client has been lost");
        }
    }

    /**
     * Log a message to the system console.
     *
     * @param message The message to be logged (printed).
     */
    private void log(String message) {
        String threadId = "THREAD #" + Thread.currentThread().getId() + ": ";
        System.out.println(threadId + message);
    }
}