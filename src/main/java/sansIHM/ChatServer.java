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
                    broadcastMessage(inputLine);
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
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }
    }
}
