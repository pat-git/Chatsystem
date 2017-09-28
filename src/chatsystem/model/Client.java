package chatsystem.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

/**
 * The client of the chatsystem.
 */
public class Client {

    private Socket clientSocket;
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private Network network;

    /**
     * Tries to create a client which connects to the server which should be
     * running on the host with the given hostname (here: localhost) and
     * the given port.
     *
     * @param hostName
     *        the hostname of the server
     * @param port
     *        the port of the server
     * @param network
     *        will be used to interact with the network instance
     * @return true if the client could be created and connected
     * @throws IOException
     *         throws this exception because this exception can not
     *         be handled by the network
     */
    public boolean createClient(String hostName, int port, Network network)
                   throws IOException {
        this.network = network;
        try {
            clientSocket = new Socket(hostName, port);
            outputStream = new PrintWriter(clientSocket.getOutputStream(), true);
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        } catch (ConnectException ex) {
            return false;
        }
        if (clientSocket.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Receives messages of the connected client/server and appends them to the
     * textArea using the appendMessage method of the network class.
     *
     * @throws IOException
     *         throws this exception because this exception can not
     *         be handled by the network
     */
    public void receiveMessage() throws IOException {
        String inputLine;
        while (true) {
            inputLine = inputStream.readLine();
            if (inputLine != null && !inputLine.equals("")){
                if (!inputLine.equals("Host: closing")) {
                    network.appendMessage(inputLine);
                } else {
                    network.appendMessage("Server has been closed! Restart the "
                                          + "server!");
                    waitForServer();
                }
            }
        }
    }

    private void waitForServer() throws IOException{
        while(!createClient(clientSocket.getInetAddress().getHostName(),
                clientSocket.getPort(), network)){
            // Waiting for successful connection
        }
        network.appendMessage("Reconnected to server!");
    }

    /**
     * Sends a message to the server/client.
     *
     * @param message
     *        the message that should be sent
     */
    public void sendMessage (String message) {
        if(outputStream != null){
            outputStream.println(message);
        } else {
            System.out.println("No OutputStream!");
        }
    }

    /**
     * Closes all streams and the socket.
     *
     * @throws IOException
     *         throws this exception because this exception can not
     *         be handled by the network
     */
    public void disconnect () throws IOException {
        sendMessage("#Close connection#");
        outputStream.close();
        inputStream.close();
        clientSocket.close();
    }
}
