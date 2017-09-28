package chatsystem.gui;

import chatsystem.model.Network;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.*;

/**
 * The ChatSystemFrame is the main frame of the GUI (graphical user interface)
 * of the Chatsystem.
 *
 * @version 1.0
 */
public final class ChatSystemFrame extends JFrame {

    private JButton sendButton;
    private JTextField textField;
    private JTextArea textArea;
    private Thread networkThread;
    private Thread receiveMessagesThread;
    private Network network;

    /**
     * The main method of the Chatsystem. Creates a ChatSystemFrame
     * which is a JFrame.
     *
     * @param args
     *        the arguments/start parameters of this program which will not
     *        be used
     */
    public static void main(String[] args) throws IOException{
        new ChatSystemFrame();
    }

    /**
     * Creates a new ChatSystemFrame which is a JFrame and initializes it. This
     * ChatSystemFrame visualizes the Chatsystem.
     */
    private ChatSystemFrame() throws IOException{
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        setTitle("Chatsystem");
        setSize(getScreenSize().width / 2, getScreenSize().height / 2);
        setLocation(getScreenSize().width / 4, getScreenSize().height / 4);
        setLayout(new BorderLayout());
        addTextArea();
        addTextBox();
        addButtonListener();
        addTextBoxListener();
        revalidate();
        setupNetwork();
        if(!network.isServer()){
            receiveMessages();
        }
        addWindowListenerToFrame();
    }

    /**
     * Add window listener to stop the thread and close the sockets
     * if the window is closed.
     */
    private void addWindowListenerToFrame() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                if (network.isServer()) {
                    networkThread.stop();
                } else {
                    receiveMessagesThread.stop();
                }
                try {
                    network.disconnect();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    private void addTextBox() {
        textField = new JTextField();
        textField.setColumns(70);
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(textField, BorderLayout.WEST);
        sendButton = new JButton("Send");
        messagePanel.add(sendButton);
        getContentPane().add(messagePanel);
    }

    private void addTextArea() {
        textArea = new JTextArea();
        textArea.setRows(29);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        getContentPane().add(scrollPane, BorderLayout.NORTH);
    }

    private void addButtonListener() {
        sendButton.addActionListener(event -> {
            if(event.getSource() == sendButton){
                sendMessage(textField.getText());
            }
        });
    }

    private void addTextBoxListener() {
        textField.addActionListener(event -> {
            if(event.getSource() == textField){
                sendMessage(textField.getText());
            }
        });
    }

    private void setupNetwork () throws IOException {
        network = new Network(this);
        if(network.isServer()) {
            networkThread = new Thread(() -> {
                try {
                    while (true) {
                        network.acceptConnection();
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                    System.exit(1);
                }
            });
            networkThread.start();
        }
    }

    /**
     * Returns the screen-size (amount of pixels) of the main monitor.
     *
     * @return the screen-size as a {@code Dimension}
     */
    private Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

    /**
     * Adds the given text to the textArea.
     * This method is synchronized because it will be used in the networkThread.
     * @param text
     *        The text to append
     */
    public synchronized void appendToTextArea(String text){
        textArea.append("\n" + text);
        textArea.revalidate();
    }

    /**
     *  Sends the given message to the network. Uses a extra thread to improve
     *  smoothness of the gui.
     *
     * @param message
     *        the message that should be sent to the network
     */
    private void sendMessage(String message){
        appendToTextArea("Me: " + message);
        Thread messageThread = new Thread(() -> {
            try {
                network.sendMessage(message);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
        messageThread.start();
        textField.setText("");
    }

    private void receiveMessages(){
        receiveMessagesThread = new Thread(() -> {
            try {
                network.receiveMessage();
            } catch (IOException exception) {
                exception.printStackTrace();
                System.exit(1);
            }
        });
        receiveMessagesThread.start();
    }
}