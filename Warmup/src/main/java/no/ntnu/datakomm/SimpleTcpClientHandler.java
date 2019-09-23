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
        openStreams();
        boolean finishedHandling = false;
        while (streamsAreOpen() && inFromClient.hasNextLine() && !finishedHandling) {
            String request = inFromClient.nextLine();
            if ("game over".equals(request)) {
                finishedHandling = true;
            } else {
                String response = "error";
                String[] numberStrings = request.split("\\+");
                if (numberStrings.length == 2) {
                    try {
                        response = "" + (Integer.parseInt(numberStrings[0]) + Integer.parseInt(numberStrings[1]));
                    } finally {}
                }
                outToClient.println(response);
            }
        }
        closeStreams();
    }

    private void closeStreams() {
        inFromClient.close();
        outToClient.close();
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