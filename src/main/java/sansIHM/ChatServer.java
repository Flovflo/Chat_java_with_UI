package sansIHM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private int serverPort = 6666;
    private List<ClientHandler> clients = new ArrayList<>();
    private ExecutorService pool = Executors.newFixedThreadPool(10);

    public ChatServer() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Le serveur de chat est démarré et en attente de clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.out.println("Erreur du serveur de chat : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ChatServer();
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String[] parts = inputLine.split(":", 4);
                    if (parts.length == 4) {
                        username = parts[0];
                        String recipient = parts[1];
                        String date = parts[2];
                        String message = parts[3];

                        if (recipient.equals("all")) {
                            broadcastMessage(inputLine);
                        } else {
                            for (ClientHandler client : clients) {
                                if (client.username == null || client.username.equals(recipient) || client.username.equals(username)) {
                                    client.out.println(inputLine);
                                }
                            }
                        }
                    } else {
                        broadcastMessage(inputLine);
                    }
                }
            } catch (IOException e) {
                System.out.println("Erreur du gestionnaire de client : " + e.getMessage());
            } finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (socket != null) socket.close();

                    clients.remove(this);
                } catch (IOException e) {
                    System.out.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
                }
            }
        }

        private void broadcastMessage(String message) {
            String[] parts = message.split(":", 4);
            if (parts.length == 4) {
                String sender = parts[0];
                String recipient = parts[1];
                String date = parts[2];
                String content = parts[3];

                username = sender; // Mise à jour du nom d'utilisateur

                for (ClientHandler client : clients) {
                    if (client.username.equalsIgnoreCase(recipient)) {
                        client.out.println(sender + ":" + date + ":" + content);
                        // Envoyer le message également à l'expéditeur pour confirmer l'envoi
                        if (!client.username.equalsIgnoreCase(username)) {
                            out.println("Message envoyé à " + recipient + ":" + date + ":" + content);
                        }
                        break;
                    }
                }
            }
        }
    }
}
