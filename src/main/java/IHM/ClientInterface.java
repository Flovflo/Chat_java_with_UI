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
    private JTextField recipientTextField;
    private JLabel recipientLabel;// Ajout d'un champ pour le destinataire
    private JTextArea chatTextArea;
    private JTextField messageTextField;
    private JButton sendButton;
    private JScrollPane chatScrollPane;
    private JButton disconnectButton;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    private String serverIP = "127.0.0.1";
    private int serverPort = 6666;
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
                envoyerMessage();
            }
        });

        messageTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                envoyerMessage();
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
            JOptionPane.showMessageDialog(this, "Vous êtes déjà connecté au serveur.");
            return;
        }
        try {
            socket = new Socket(serverIP, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            JOptionPane.showMessageDialog(this, "Connexion au serveur de chat.");

            Thread serverListener = new Thread(() -> {
                try {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        appendMessage(inputLine);
                    }
                } catch (IOException e) {
                    appendMessage("Erreur de réception des messages du serveur : " + e.getMessage());
                }
            });
            serverListener.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion au serveur de chat : " + e.getMessage());
        }
    }

    private void disconnectFromServer() {
        if (socket == null || !socket.isConnected()) {
            JOptionPane.showMessageDialog(this, "Vous n'êtes pas connecté au serveur.");
            return;
        }

        String username = usernameTextField.getText().trim();
        if (!username.isEmpty()) {
            String date = new SimpleDateFormat("HH-mm").format(new Date());
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
            JOptionPane.showMessageDialog(this, "Déconnecté du serveur de chat.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur de déconnexion du serveur de chat : " + e.getMessage());
        }
    }

    private void envoyerMessage() {
        String message = messageTextField.getText().trim();
        if (!message.isEmpty()) {
            String username = usernameTextField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez saisir un nom d'utilisateur.");
                return;
            }

            // Ajouter le destinataire (remplacez "recipient" par le nom d'utilisateur du destinataire souhaité)
            String recipient = "recipient";
            String date = new SimpleDateFormat("HH-mm").format(new Date()); // Changer le format de l'heure
            String formattedMessage = username + ":" + recipient + ":" + date + ":" + message;
            out.println(formattedMessage);
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
        String[] parts = message.split(":", 4);
        if (parts.length == 4) {
            String sender = parts[0];
            String recipient = parts[1];
            String date = parts[2];
            String content = parts[3];

            try {
                Date parsedDate = new SimpleDateFormat("HH-mm").parse(date);
                String formattedDate = new SimpleDateFormat("HH:mm").format(parsedDate);
                chatTextArea.append(sender + " à " + recipient + " (" + formattedDate + "): " + content + "\n");
            } catch (java.text.ParseException e) {
                chatTextArea.append(message + "\n");
            }
        } else {
            chatTextArea.append(message + "\n");
        }
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
