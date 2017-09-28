package chatsystem.model;

import chatsystem.gui.ChatSystemFrame;

import java.io.IOException;

/**
 * This class manages the network of the chatsystem, it will create the client
 * and server to provide a chatsystem.
 */
public class Network {

    private final int PORT = 12345;
    private final String HOSTNAME = "127.0.0.1";
    private ChatSystemFrame chatSystemFrame;
    private Server server;
    private Client client;
    private boolean isServer;

    /**
     * Setups a new network. This Network will create a server which can be used
     * for other instances of ths Chatsystem to provide a chat-server.
     * If the server is already running this network will create a client which
     * will connect to the running (chat-)server.
     *
     * @param chatSystemFrame
     *        the main frame of the chatsystem will be used to
     *        interact with it
     * @throws IOException
     *         throws this exception because this exception can not
     *         be handled by the network
     */
    public Network(ChatSystemFrame chatSystemFrame) throws IOException{
        server = new Server();
        client = new Client();
        this.chatSystemFrame = chatSystemFrame;
        if (!server.createServer(PORT, this)) {
            if (!client.createClient(HOSTNAME, PORT, this)) {
                chatSystemFrame.appendToTextArea("Error: Can not connect or "
                                                 + "create Server; Port may "
                                                 + "already be bound");
            } else {
                chatSystemFrame.appendToTextArea("Successfully connected to "
                                                 + "server");
            }
        } else {
            chatSystemFrame.appendToTextArea("Successfully started server; "
                                             + "start another instance of this "
                                             + "program to add the client to "
                                             + "the network!");
            isServer = true;
        }
    }

    /**
     * If this instance is a server it will open the server to accept client
     * connections.
     *
     * @throws IOException
     *         throws this exception because this exception can not
     *         be handled by the network
     */
    public void acceptConnection () throws IOException {
        if(isServer) {
            server.acceptConnections();
        }
    }

    /**
     * Uses the method of the main frame to append a message to the textArea.
     *
     * @param message
     *        the message to be appended
     */
    public void appendMessage(String message) {
        if(message != null){
            chatSystemFrame.appendToTextArea(message);
        }
    }

    /**
     * Closes the connection of the client to the server or rather server to
     * client.
     *
     * @throws IOException
     *         throws this exception because this exception can not
     *         be handled by the network
     */
    public void disconnect() throws IOException {
        if (isServer) {
            server.disconnect();
        } else {
            client.disconnect();
        }
    }

    /**
     * Sends the message from the server to the client or rather client to
     * server.
     *
     * @param message
     *        the message to be sent
     * @throws IOException
     *         throws this exception because this exception can not
     *         be handled by the network
     */
    public synchronized void sendMessage(String message) throws IOException{
        if (isServer) {
            server.sendMessageToAllClients(message, -1);
        } else {
            client.sendMessage(message);
        }
    }

    /**
     * Calls the receiveMessage method in the client class.
     *
     * @throws IOException
     *         throws this exception because this exception can not
     *         be handled by the network
     */
    public void receiveMessage() throws IOException{
            client.receiveMessage();
    }

    /**
     * Checks if the server is running.
     * @return true if the server is running.
     */
    public boolean isServer() {
        return isServer;
    }
}
