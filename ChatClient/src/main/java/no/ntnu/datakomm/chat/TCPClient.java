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
        boolean success = false;
        try {
            connection = new Socket(host, port);
            toServer = new PrintWriter(connection.getOutputStream(), true);
            fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            success = true;
        } catch (IOException e) {
            System.out.println("ERROR: An I/O error occured when connecting to server");
        }
        return success;
    }

    /**
     * Close the socket. This method must be synchronized, because several threads
     * may try to call it. For example: When "Disconnect" button is pressed in the
     * GUI thread, the connection will get closed. Meanwhile, the background thread
     * trying to read server's response will get error in the input stream and may
     * try to call this method when the socket is already in the process of being
     * closed. with "synchronized" keyword we make sure that no two threads call
     * this method in parallel.
     */
    public synchronized void disconnect() {
        if (connection != null && !connection.isClosed()) {
            try {
                connection.close();
                
            } catch (IOException e) {
                System.out.println("ERROR: An I/O error occured when closing this socket");
            }
            connection = null;
        }
        onDisconnect();
    }

    /**
     * @return true if the connection is active (opened), false if not.
     */
    public boolean isConnectionActive() {
        return connection != null;
    }

    /**
     * Send a command to server.
     *
     * @param cmd A command. It should include the command word and optional
     *            attributes, according to the protocol.
     * @return true on success, false otherwise
     */
    private boolean sendCommand(String cmd) {
        boolean success = false;
        if (connection.isClosed() || toServer == null) {
            System.out.println("ERROR: Connection has been lost");
        } else if (cmd == null) {
            System.out.println("ERROR: Command was null");
        } else if (cmd.trim().length() == 0) {
            System.out.println("ERROR: Command was empty");
        } else {
            toServer.println(cmd);
            success = true;
        }
        return success;
    }

    /**
     * Send a public message to all the recipients.
     *
     * @param message Message to send
     * @return true if message sent, false on error
     */
    public boolean sendPublicMessage(String message) {
        boolean success = sendCommand("msg " + message);
        if (!success) {
            lastError = "ERROR: Message could not be sent";
        }
        return success;
    }

    /**
     * Send a login request to the chat server.
     *
     * @param username Username to use
     */
    public void tryLogin(String username) {
        sendCommand("login " + username);
    }

    /**
     * Send a request for latest user list to the server. To get the new users,
     * clear your current user list and use events in the listener.
     */
    public void refreshUserList() {
        sendCommand("users");
    }

    /**
     * Send a private message to a single recipient.
     *
     * @param recipient username of the chat user who should receive the message
     * @param message   Message to send
     * @return true if message sent, false on error
     */
    public boolean sendPrivateMessage(String recipient, String message) {
        boolean success = sendCommand("privmsg " + recipient + " " + message);
        if (!success) {
            lastError = "ERROR: Private message could not be sent";
        }
        return false;
    }

    /**
     * Send a request for the list of commands that server supports.
     */
    public void askSupportedCommands() {
        sendCommand("help");
    }

    /**
     * Wait for chat server's response
     *
     * @return one line of text (one command) received from the server
     */
    private String waitServerResponse() {
        String response = null;
        try {
            response = fromServer.readLine();
        } catch (IOException e) {
            System.out.println("ERROR: An I/O error occured while waiting for server response");
            disconnect();
        }
        return response;
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
     * Read incoming messages one by one, generate events for the listeners. A loop
     * that runs until the connection is closed.
     */
    private void parseIncomingCommands() {
        while (isConnectionActive()) {
            String serverResponse = waitServerResponse();
            if (serverResponse == null) {
                disconnect();
            } else {
                String[] commandArgument = serverResponse.split(" ", 2);
                String command = commandArgument[0];
                String argument = (commandArgument.length == 2) ? commandArgument[1] : null;

                switch (command) {
                case "loginok":
                    onLoginResult(true, null);
                    break;

                case "loginerr":
                    onLoginResult(false, argument);
                    break;

                case "users":
                    onUsersList(argument.split(" "));
                    break;

                case "msg": {
                    String[] senderMessage = argument.split(" ", 2);
                    String sender = senderMessage[0];
                    String message = (senderMessage.length == 2) ? senderMessage[1] : "";
                    onMsgReceived(false, sender, message);
                }
                    break;

                case "privmsg": {
                    String[] senderMessage = argument.split(" ", 2);
                    String sender = senderMessage[0];
                    String message = (senderMessage.length == 2) ? senderMessage[1] : "";
                    onMsgReceived(true, sender, message);
                }
                    break;

                case "msgerr":
                    onMsgError(argument);
                    break;

                case "cmderr":
                    onCmdError(argument);
                    break;

                case "supported":
                    onSupported(argument.split(" "));
                    break;

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
    // The following methods are all event-notificators - notify all the listeners
    /////////////////////////////////////////////////////////////////////////////////////////////////////////// about
    /////////////////////////////////////////////////////////////////////////////////////////////////////////// a
    /////////////////////////////////////////////////////////////////////////////////////////////////////////// specific
    /////////////////////////////////////////////////////////////////////////////////////////////////////////// event.
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
     * Notify listeners that socket was closed by the remote end (server or Internet
     * error)
     */
    private void onDisconnect() {
        listeners.forEach(listener -> listener.onDisconnect());
    }

    /**
     * Notify listeners that server sent us a list of currently connected users
     *
     * @param users List with usernames
     */
    private void onUsersList(String[] users) {
        listeners.forEach(l -> l.onUserList(users));
    }

    /**
     * Notify listeners that a message is received from the server
     *
     * @param priv   When true, this is a private message
     * @param sender Username of the sender
     * @param text   Message text
     */
    private void onMsgReceived(boolean priv, String sender, String text) {
        listeners.forEach(l -> l.onMessageReceived(new TextMessage(sender, priv, text)));
    }

    /**
     * Notify listeners that our message was not delivered
     *
     * @param errMsg Error description returned by the server
     */
    private void onMsgError(String errMsg) {
        listeners.forEach(l -> l.onMessageError(errMsg));
    }

    /**
     * Notify listeners that command was not understood by the server.
     *
     * @param errMsg Error message
     */
    private void onCmdError(String errMsg) {
        listeners.forEach(l -> l.onCommandError(errMsg));
    }

    /**
     * Notify listeners that a help response (supported commands) was received from
     * the server
     *
     * @param commands Commands supported by the server
     */
    private void onSupported(String[] commands) {
        listeners.forEach(l -> l.onSupportedCommands(commands));
    }
}
