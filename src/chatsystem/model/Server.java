package chatsystem.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The Server of the chatsystem.
 */
public class Server {

    private ServerSocket serverSocket;
    private List<PrintWriter> outputStreams;
    private List<BufferedReader> inputStreams;
    private Network network;
    private List<Thread> receiveMessagesThreads;

    /**
     * Creates a server on this machine on the given port.
     *
     * @param port
     *        the port on which the server should run
     * @param network
     *        will be used to interact with it
     * @return true if the server could be created
     * @throws IOException
     *         throws this exception because this exception can not
     *         be handled by the network
     */
    public boolean createServer(int port, Network network) throws IOException {
        this.network = network;
        outputStreams = new ArrayList<>();
        inputStreams = new ArrayList<>();
        receiveMessagesThreads = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(port);
            return true;
        } catch (BindException exception) {
            System.out.println("Server already running on local machine, "
                               + "starting as client...");
        }
        return false;
    }

    /**
     * Opens the server for clients which want connect to the server.
     *
     * @throws IOException
     *         throws this exception because this exception can not
     *         be handled by the network
     */
    public void acceptConnections() throws IOException {
        Socket clientSocket = serverSocket.accept();
        PrintWriter outputStream = new PrintWriter(
                                                 clientSocket.getOutputStream(),
                                                true);
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(
                                                clientSocket.getInputStream()));
        outputStreams.add(outputStream);
        inputStreams.add(inputStream);
        receiveMessageFromClient(inputStream);
    }

    /**
     * Receives all messages from the client with the given input-stream.
     *
     * @param inputStream
     *        the input-stream of the client
     */
    public void receiveMessageFromClient(BufferedReader inputStream) {
        Thread receiveMessagesThread = new Thread(() -> {
            String inputLine;
            while (true) {
                try {
                    inputLine = inputStream.readLine();
                    int clientID = inputStreams.indexOf(inputStream);
                    if (inputLine != null && !inputLine.equals("")) {
                        if (!inputLine.equals("#Close connection#")) {
                            network.appendMessage("Client " + (clientID + 1)
                                                  + ": " + inputLine);
                            sendMessageToAllClients(inputLine, clientID);
                        } else {
                            removeClient(inputStream, clientID);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        receiveMessagesThread.start();
        receiveMessagesThreads.add(receiveMessagesThread);
    }

    /**
     * This method is synchronized to solve the problem that two or more clients
     * get removed at the same time causing a false removal of other client(s).
     *
     * @param inputStream
     *        the input-stream of the client
     * @param clientID
     *        the id of the client (=id of the input-stream in the list
     *        input-streams)
     */
    private synchronized void removeClient(BufferedReader inputStream,
                                           int clientID) {
        inputStreams.remove(inputStream);
        outputStreams.remove(outputStreams.get(clientID));
        network.appendMessage("Client " + (clientID + 1) + " disconnected");
        receiveMessagesThreads.get(clientID).stop();
        receiveMessagesThreads.remove(receiveMessagesThreads.get(clientID));
    }

    /**
     * Sends a message to all connected clients. Adds a prefix to the message to
     * identify the sender. This method is synchronized to prevent sending more
     * messages at once to the same client.
     *
     * @param message
     *        the message which should be sent
     * @param clientID
     *        the id of the client (=id of the input-stream in the list
     *        input-streams), server has the id {@code -1}
     */
    public synchronized void sendMessageToAllClients(String message,
                                                     int clientID) {
        for (PrintWriter out : outputStreams) {
            // Don't send to null and don't (re-)send it to the client that sent
            // the message to this server
            if (out != null && outputStreams.indexOf(out) != clientID) {
                if (clientID == -1) {
                    out.println("Host: " + message);
                } else {
                    out.println("Client " + (clientID + 1) + ": " + message);
                }
            }
        }
    }


    /**
     * Closes all streams, threads and the socket.
     *
     * @throws IOException
     *         throws this exception because this exception can not
     *         be handled by the network
     */
    public void disconnect() throws IOException{
        sendMessageToAllClients("closing", -1);
        for (Thread thread : receiveMessagesThreads) {
            thread.stop();
        }
        for (PrintWriter output : outputStreams) {
            output.close();
        }
        for (BufferedReader input : inputStreams) {
            input.close();
        }
        serverSocket.close();
    }
}
