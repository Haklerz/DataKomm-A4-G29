package no.ntnu.datakomm;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SimpleTcpClientHandler extends Thread
{
    private final Socket clientSocket;
    private Scanner inFromClient;
    private PrintWriter outToClient;

    /**
     * Instantiates the simple TCP client handler for multi-threaded server.
     *
     * @param clientSocket the client socket that will be added to the thread pool.
     */
    public SimpleTcpClientHandler(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }

    /**
     * Runs the simple TCP client handler for the multi-threaded server.
     *
     * @Override overrides the run method in Thread class.
     */
    @Override
    public void run()
    {
        log("Client connected.");
        this.openStreams();
        while (this.streamsOpen() && this.inFromClient.hasNextLine())
        {
            String request = this.inFromClient.nextLine();
            if ("game over".equals(request))
            {
                this.closeStreams();
            }
            else
            {
                String response = null;
                String[] numberString = request.split("\\+");
                if ( numberString.length == 2 )
                {
                    try
                    {
                        int firstInt = Integer.parseInt(numberString[0].trim());
                        int secondInt = Integer.parseInt(numberString[1].trim());
                        response = "" + (firstInt + secondInt);
                    }
                    catch (NumberFormatException formatError)
                    {
                        System.out.println("Error: " + formatError.getMessage());
                    }
                }
                this.outToClient.println(response);
                log("Responded to request < " + request + " > with < " + response + " >");
//                if (response == null)
//                {
//                    log("Response is 'null' and therefore invalid.");
//                }
//                else
//                {
//                    this.outToClient.println(response);
//                    log("Responded to request < " + request + " > with < " + response + " >");
//                }
            }
        }

        this.closeConnection();
        log("Client disconnected.");
    }

    /**
     * Opens input and output streams.
     */
    private void openStreams()
    {
        try
        {
            this.inFromClient = new Scanner(this.clientSocket.getInputStream());
            this.outToClient = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException inputOutputError)
        {
            System.out.println("Error: " + inputOutputError.getMessage());
        }
    }

    /**
     * Closes the input and output streams.
     */
    private void closeStreams()
    {
        // Closes the streams.
        this.inFromClient.close();
        this.outToClient.close();

        // Initiate the streams as null.
        this.inFromClient = null;
        this.outToClient = null;
    }

    /**
     * Close the TCP connection to the remote server.
     *
     * @return true if connection closed, else return false.
     */
    private void closeConnection()
    {
        try
        {
            this.clientSocket.close();
        } catch (IOException socketCloseError)
        {
            System.out.println("Error: " + socketCloseError.getMessage());
        }
    }

    /**
     * Checks if the input and output streams are open.
     * @return true if streams are open, else return false.
     */
    private boolean streamsOpen()
    {
        boolean streamsOpen = false;
        if ( inFromClient != null && outToClient != null )
        {
            streamsOpen = true;
        }
        return streamsOpen;
    }

    /**
     * Log a message to the system console.
     *
     * @param message The message to be logged (printed).
     */
    private static void log(String message)
    {
        String threadId = "THREAD #" + Thread.currentThread().getId() + ": ";
        System.out.println(threadId + message);
    }
}