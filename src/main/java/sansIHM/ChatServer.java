package sansIHM;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 1235;
    private Set<String> usernames = new HashSet<>();
    private Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Error starting the Chat Server: " + e.getMessage());
        }
    }

    void broadcast(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clientHandlers) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    void removeClient(String username, ClientHandler client) {
        boolean removed = usernames.remove(username);
        if (removed) {
            clientHandlers.remove(client);
            System.out.println("User " + username + " has left the chat.");
        }
    }

    boolean addUsername(String username) {
        return usernames.add(username);
    }
}

