package sansIHM;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] tokens = inputLine.split(":", 3);

                if (tokens.length == 3) {
                    String username = tokens[0].trim();
                    String message = tokens[1].trim();
                    String date = tokens[2].trim();

                    while (username == null) {
                        username = in.readLine().trim(); // Lire le nom d'utilisateur lors de la première connexion du client

                        if (server.addUsername(username)) {
                            server.broadcast(username + " has joined the chat.", this);
                        } else {
                            username = null;
                            out.println("ERROR: Username already exists.");
                        }
                    }

                    server.broadcast(username + ": " + message + " (" + date + ")", this);
                } else {
                    out.println("ERROR: Invalid message format.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error in ClientHandler: " + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
            if (username != null) {
                server.removeClient(username, this);
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    void sendMessage(String message) {
        out.println(message);
    }
}
