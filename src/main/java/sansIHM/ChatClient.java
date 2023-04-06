package sansIHM;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 1235;

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }

    public void start() {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to Chat Server.");

            // Listen for server messages in a separate thread
            new Thread(() -> {
                try {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println(inputLine);
                    }
                } catch (IOException e) {
                    System.out.println("Error receiving messages from server: " + e.getMessage());
                }
            }).start();

            // Send messages from standard input
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your username: ");
            String username = scanner.nextLine().trim();

            String message;
            while (true) {
                System.out.print("Enter your message: ");
                message = scanner.nextLine().trim();
                if (message.equalsIgnoreCase("quit")) {
                    break;
                }

                String date = new SimpleDateFormat("HH-mm").format(new Date());
                out.println(username + ":" + message + ":" + date);
            }
        } catch (IOException e) {
            System.out.println("Error connecting to Chat Server: " + e.getMessage());
        }
    }
}

