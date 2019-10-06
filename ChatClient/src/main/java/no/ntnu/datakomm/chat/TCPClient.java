package no.ntnu.datakomm.chat;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

public class TCPClient {
    private PrintWriter toServer;
    private BufferedReader fromServer;
    private Socket connection;

    // Hint: if you want to store a message for the last error, store it here
    private String lastError = null;

    private final List<ChatListener> listeners = new LinkedList<>();

    /**
     * Connect to a chat server.
     *
     * @param host host name or IP address of the chat server
     * @param port TCP port of the chat server
     * @return True on success, false otherwise
     */
    public boolean connect(String host, int port) {
        // TODO Step 1: implement this method ----- DONE!
        // Hint: Remember to process all exceptions and return false on error
        // Hint: Remember to set up all the necessary input/output stream variables
        boolean connected = false;

        try
        {
            // Setup the socket.
            this.connection = new Socket(host, port); // Setup socket.

            // Setup the socket writer.
            OutputStream outputStream = this.connection.getOutputStream(); // Returns the output stream for given socket.
            this.toServer = new PrintWriter(outputStream, true);

            //Setup the socket reader.
            InputStream inputStream = this.connection.getInputStream(); // Returns the input stream for the given socket.
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream); // Bridge from byte streams to characters.
            this.fromServer = new BufferedReader(inputStreamReader); // Reads text from character input stream.

            // Return true if connected.
            connected = true;
        } catch (IOException socketConnectError)
        {
            System.out.println("Error: " + socketConnectError.getMessage());
        }

        return connected;
    }

    /**
     * Close the socket. This method must be synchronized, because several
     * threads may try to call it. For example: When "Disconnect" button is
     * pressed in the GUI thread, the connection will get closed. Meanwhile, the
     * background thread trying to read server's response will get error in the
     * input stream and may try to call this method when the socket is already
     * in the process of being closed. with "synchronized" keyword we make sure
     * that no two threads call this method in parallel.
     */
    public synchronized void disconnect() {
        // TODO Step 4: implement this method ----- DONE!
        // Hint: remember to check if connection is active
        if ( this.connection != null && !this.connection.isClosed())
        {
            // Close socket connection.
            try
            {
                this.connection.close();
            } catch (IOException closeConnectionError)
            {
                System.out.println("Error: " + closeConnectionError.getMessage());
            }
            this.connection = null;
        }

        // Write disconnect message.
        this.onDisconnect();
    }

    /**
     * @return true if the connection is active (opened), false if not.
     */
    public boolean isConnectionActive() {
        // TODO - Modified
        boolean connectionActive = false;

        if ( this.connection != null )
        {
            connectionActive = true;
        }

        return connectionActive;
    }

    /**
     * Send a command to server.
     *
     * @param cmd A command. It should include the command word and optional attributes, according to the protocol.
     * @return true on success, false otherwise
     */
    private boolean sendCommand(String cmd) {
        // TODO Step 2: Implement this method ----- DONE!
        // Hint: Remember to check if connection is active
        boolean commandSent = false;

        if ( cmd == null)
        {
            System.out.println("Error: Command was null");
        }

        else if ( cmd.trim().length() == 0 )
        {
            System.out.println("Error: Command was empty");
        }

        else if ( this.connection.isClosed() || this.toServer == null )
        {
            System.out.println("Error: Connection not established");
        }

        else
        {
            // Command word to contain a command word (msg, privmsg, login) following a message.
            this.toServer.println(cmd);
            commandSent = true;
        }

        return commandSent;
    }

    /**
     * Send a public message to all the recipients.
     *
     * @param message Message to send
     * @return true if message sent, false on error
     */
    public boolean sendPublicMessage(String message) {
        // TODO Step 2: implement this method ----- DONE!
        // Hint: Reuse sendCommand() method
        // Hint: update lastError if you want to store the reason for the error.
        boolean publicMessageSent = this.sendCommand("msg " + message);

        if ( !publicMessageSent )
        {
            this.lastError = "Error: Public message not sent";
        }

        return publicMessageSent;
    }

    /**
     * Send a login request to the chat server.
     *
     * @param username Username to use
     */
    public void tryLogin(String username) {
        // TODO Step 3: implement this method ----- DONE!
        // Hint: Reuse sendCommand() method
        boolean loginSuccessful = this.sendCommand("login " + username);

        if ( !loginSuccessful )
        {
            this.lastError = "Error: Login unsuccessful";
        }
    }

    /**
     * Send a request for latest user list to the server. To get the new users,
     * clear your current user list and use events in the listener.
     */
    public void refreshUserList() {
        // TODO Step 5: implement this method ----- DONE!
        // Hint: Use Wireshark and the provided chat client reference app to find out what commands the
        // client and server exchange for user listing.
        this.sendCommand("users");
    }

    /**
     * Send a private message to a single recipient.
     *
     * @param recipient username of the chat user who should receive the message
     * @param message   Message to send
     * @return true if message sent, false on error
     */
    public boolean sendPrivateMessage(String recipient, String message) {
        // TODO Step 6: Implement this method ----- DONE!
        // Hint: Reuse sendCommand() method
        // Hint: update lastError if you want to store the reason for the error.
        boolean privateMessageSent = this.sendCommand("privmsg " + recipient + " " + message);

        if ( !privateMessageSent )
        {
            this.lastError = "Error: Private message not sent";
        }

        return privateMessageSent;
    }


    /**
     * Send a request for the list of commands that server supports.
     */
    public void askSupportedCommands() {
        // TODO Step 8: Implement this method ----- DONE!
        // Hint: Reuse sendCommand() method
        this.sendCommand("help");
    }


    /**
     * Wait for chat server's response
     *
     * @return one line of text (one command) received from the server
     */
    private String waitServerResponse() {
        // TODO Step 3: Implement this method ----- DONE!
        // TODO Step 4: If you get I/O Exception or null from the stream, it means that something has gone wrong ----- DONE!
        // with the stream and hence the socket. Probably a good idea to close the socket in that case.
        String serverResponse = null;

        try
        {
            // Tries to read a line terminated by line feed (\n, \r, or carriage return)
            serverResponse = this.fromServer.readLine();
        } catch (IOException serverResponseError)
        {
            System.out.println("Error: " + serverResponseError.getMessage());
            this.disconnect();
        }

        return serverResponse;
    }

    /**
     * Get the last error message
     *
     * @return Error message or "" if there has been no error
     */
    public String getLastError() {
        if (lastError != null) {
            return lastError;
        } else {
            return "";
        }
    }

    /**
     * Start listening for incoming commands from the server in a new CPU thread.
     */
    public void startListenThread() {
        // Call parseIncomingCommands() in the new thread.
        Thread t = new Thread(() -> {
            parseIncomingCommands();
        });
        t.start();
    }

    /**
     * Read incoming messages one by one, generate events for the listeners. A loop that runs until
     * the connection is closed.
     */
    private void parseIncomingCommands() {
        while (isConnectionActive()) {
            // TODO Step 3: Implement this method ----- DONE!
            // Hint: Reuse waitServerResponse() method
            // Hint: Have a switch-case (or other way) to check what type of response is received from the server
            // and act on it.
            // Hint: In Step 3 you need to handle only login-related responses.
            // Hint: In Step 3 reuse onLoginResult() method

            // TODO Step 5: update this method, handle user-list response from the server ----- DONE!
            // Hint: In Step 5 reuse onUserList() method

            // TODO Step 7: add support for incoming chat messages from other users (types: msg, privmsg) ----- DONE!
            // TODO Step 7: add support for incoming message errors (type: msgerr)
            // TODO Step 7: add support for incoming command errors (type: cmderr)
            // Hint for Step 7: call corresponding onXXX() methods which will notify all the listeners

            // TODO Step 8: add support for incoming supported command list (type: supported) ----- DONE!

            // Waits for a response from the server.
            String serverResponse = this.waitServerResponse();
            if ( serverResponse == null )
            {
                // Disconnect connection.
                this.disconnect();
            }

            else
            {
                // serverResponse is not null. Check for command word.
                // First split response into segments, with segment 1 as command word, segment 2 as the argument.
                String[] serverResponseSplit = serverResponse.split(" ", 2);
                String command = serverResponseSplit[0]; // The command word.
                String argument = null; // The argument.
                if ( serverResponseSplit.length == 2 )
                {
                    argument = serverResponseSplit[1];
                }

                // Check each command word and do appropriate action.
                switch (command)
                {
                    // ------ Start of Step 3 ----
                    // TODO ----- DONE!
                    case "loginok":
                        this.onLoginResult(true, null);
                        break;

                    case "loginerr":
                        this.onLoginResult(false, argument);
                        break;
                    // ------ End of Step 3 ----

                    // ------ Start of Step 5 ----
                    case "users":
                        // TODO ----- DONE!
                        // As seen in Wireshark as list of all usernames (separated with space) are sent when
                        // "users" is called. Splitting this string into usernames.
                        this.onUsersList(argument.split(" "));
                        break;
                    // ------ End of Step 5 ----

                    // ------ Start of Step 7 ----
                    case "msg":
                        // TODO ----- DONE!
                        String[] pubmsgArgument = argument.split(" ", 2);
                        String pubUsername = pubmsgArgument[0];
                        String pubMessage = null;
                        if (pubmsgArgument.length == 2)
                        {
                            pubMessage = pubmsgArgument[1];
                        }
                        this.onMsgReceived(false, pubUsername, pubMessage);
                        break;

                    case "privmsg":
                        // TODO ----- DONE!
                        String[] privmsgArgument = argument.split(" ", 2);
                        String privUsername = privmsgArgument[0];
                        String privMessage = null;
                        if (privmsgArgument.length == 2)
                        {
                            privMessage = privmsgArgument[1];
                        }
                        this.onMsgReceived(true, privUsername, privMessage);
                        break;

                    case "msgerr":
                        // TODO ----- DONE!
                        this.onMsgError(argument);
                        break;

                    case "cmderr":
                        // TODO ----- DONE!
                        this.onCmdError(argument);
                        break;
                    // ------ End of Step 7 ----

                    // ------ Start of Step 8 ----
                    case "supported":
                        // TODO ----- DONE!
                        String[] commands = argument.split(" ");
                        this.onSupported(commands);
                        break;
                    // ------ End of Step 8 ----

                    default:
                        break;
                }
            }
        }
    }

    /**
     * Register a new listener for events (login result, incoming message, etc)
     *
     * @param listener
     */
    public void addListener(ChatListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregister an event listener
     *
     * @param listener
     */
    public void removeListener(ChatListener listener) {
        listeners.remove(listener);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // The following methods are all event-notificators - notify all the listeners about a specific event.
    // By "event" here we mean "information received from the chat server".
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Notify listeners that login operation is complete (either with success or
     * failure)
     *
     * @param success When true, login successful. When false, it failed
     * @param errMsg  Error message if any
     */
    private void onLoginResult(boolean success, String errMsg) {
        for (ChatListener l : listeners) {
            l.onLoginResult(success, errMsg);
        }
    }

    /**
     * Notify listeners that socket was closed by the remote end (server or
     * Internet error)
     */
    private void onDisconnect() {
        // TODO Step 4: Implement this method ----- DONE!
        // Hint: all the onXXX() methods will be similar to onLoginResult()
        for (ChatListener listener : listeners)
        {
            listener.onDisconnect();
        }
    }

    /**
     * Notify listeners that server sent us a list of currently connected users
     *
     * @param users List with usernames
     */
    private void onUsersList(String[] users) {
        // TODO Step 5: Implement this method ----- DONE!
        for (ChatListener listener : listeners)
        {
            listener.onUserList(users);
        }
    }

    /**
     * Notify listeners that a message is received from the server
     *
     * @param priv   When true, this is a private message
     * @param sender Username of the sender
     * @param text   Message text
     */
    private void onMsgReceived(boolean priv, String sender, String text) {
        // TODO Step 7: Implement this method ----- DONE!
        for (ChatListener listener : listeners)
        {
            listener.onMessageReceived(new TextMessage(sender, priv, text));
        }
    }

    /**
     * Notify listeners that our message was not delivered
     *
     * @param errMsg Error description returned by the server
     */
    private void onMsgError(String errMsg) {
        // TODO Step 7: Implement this method ----- DONE!
        for (ChatListener listener : listeners)
        {
            listener.onMessageError(errMsg);
        }
    }

    /**
     * Notify listeners that command was not understood by the server.
     *
     * @param errMsg Error message
     */
    private void onCmdError(String errMsg) {
        // TODO Step 7: Implement this method ----- DONE!
        for (ChatListener listener : listeners)
        {
            listener.onCommandError(errMsg);
        }
    }

    /**
     * Notify listeners that a help response (supported commands) was received
     * from the server
     *
     * @param commands Commands supported by the server
     */
    private void onSupported(String[] commands) {
        // TODO Step 8: Implement this method ----- DONE!
        for (ChatListener listener : listeners)
        {
            listener.onSupportedCommands(commands);
        }
    }
}
