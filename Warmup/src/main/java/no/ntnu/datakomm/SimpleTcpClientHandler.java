package no.ntnu.datakomm;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SimpleTcpClientHandler implements Runnable {
    private Socket clientSocket;
    private boolean finishedHandling;

    public SimpleTcpClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        log("Client Connected");
        Scanner inFromClient = null;
        PrintWriter outToClient = null;
        try {
            inFromClient = new Scanner(this.clientSocket.getInputStream());
            outToClient = new PrintWriter(this.clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            log("ERROR: Connection to client has been lost");
        }
        if (inFromClient != null && outToClient != null) {
            while (inFromClient.hasNextLine() && !finishedHandling) {
                String request = inFromClient.nextLine();
                if (request.equalsIgnoreCase("game over")) {
                    this.finishedHandling = true;
                } else {
                    String response = "error";
                    String[] numberStrings = request.split("\\+");
                    if (numberStrings.length == 2) {
                        try {
                            response = "" + (Integer.parseInt(numberStrings[0]) + Integer.parseInt(numberStrings[1]));
                        } catch (NumberFormatException e) {
                        }
                    }
                    outToClient.println(response);
                }
            }
            try {
                inFromClient.close();
                outToClient.close();
                clientSocket.close();
            } catch (IOException e) {
                log("ERROR: An I/O error occured when closing connection");
            }
            
        }
        log("Client disconnected");
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