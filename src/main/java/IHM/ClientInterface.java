package IHM;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientInterface extends JFrame {
    private JPanel mainPanel;
    private JTextField usernameTextField;
    private JButton connectButton;
    private JTextArea chatTextArea;
    private JTextField messageTextField;
    private JButton sendButton;
    private JScrollPane chatScrollPane;
    private JButton disconnectButton;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    private String serverIP = "127.0.0.1";
    private int serverPort = 1235;
    public ClientInterface() {
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        setSize(800, 600); // définir la taille de la fenêtre à 800x600
        setLocationRelativeTo(null);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        messageTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectFromServer();
            }
        });
    }

    private void connectToServer() {

        if (socket != null && socket.isConnected()) {
            JOptionPane.showMessageDialog(this, "You are already connected to the server.");
            return;
        }
        try {
            socket = new Socket(serverIP, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            JOptionPane.showMessageDialog(this, "Connected to the chat server.");

            Thread serverListener = new Thread(() -> {
                try {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        appendMessage(inputLine);
                    }
                } catch (IOException e) {
                    appendMessage("Error receiving messages from server: " + e.getMessage());
                }
            });
            serverListener.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error connecting to the chat server: " + e.getMessage());
        }
    }

    private void disconnectFromServer() {
        if (socket == null || !socket.isConnected()) {
            JOptionPane.showMessageDialog(this, "You are not connected to the server.");
            return;
        }

        String username = usernameTextField.getText().trim();
        if (!username.isEmpty()) {
            String date = new SimpleDateFormat("HH:mm").format(new Date());
            out.println(username + " has disconnected at " + date);
        }

        try {
            socket.close();
            out.close();
            in.close();
            socket = null;
            out = null;
            in = null;
            usernameTextField.setText("");
            JOptionPane.showMessageDialog(this, "Disconnected from the chat server.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error disconnecting from the chat server: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String message = messageTextField.getText().trim();
        if (!message.isEmpty()) {
            String username = usernameTextField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username.");
                return;
            }

            String date = new SimpleDateFormat("HH:mm").format(new Date()); // Changer le format de l'heure
            String formattedMessage = date + " " + username + ": " + message;
            out.println(formattedMessage);
            appendMessage(formattedMessage); // Afficher le message du client dans la JTextArea après l'envoi
            messageTextField.setText("");
        }
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public void appendMessage(String message) {
        chatTextArea.append(message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientInterface().setVisible(true);
            }
        });
    }
}
