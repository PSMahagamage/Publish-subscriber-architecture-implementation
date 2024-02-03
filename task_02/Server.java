import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server{
    private static final List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Handle client communication in a separate thread
                Thread clientHandlerThread = new Thread(() -> handleClient(clientSocket));
                clientHandlerThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            clientWriters.add(writer);

            String clientType = reader.readLine();
            if ("PUBLISHER".equals(clientType)) {
                handlePublisher(clientSocket, reader);
            } else if ("SUBSCRIBER".equals(clientType)) {
                handleSubscriber(clientSocket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handlePublisher(Socket publisherSocket, BufferedReader publisherReader) {
        try {
            String inputLine;

            while ((inputLine = publisherReader.readLine()) != null) {
                System.out.println("Publisher: " + inputLine);

                // Send the message to all subscribers
                for (PrintWriter subscriberWriter : clientWriters) {
                    // if (subscriberWriter != publisherSocket) {
                        subscriberWriter.println("Publisher: " + inputLine);
                    // }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleSubscriber(Socket subscriberSocket) {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(subscriberSocket.getInputStream()))
        ) {
            String inputLine;

            while ((inputLine = reader.readLine()) != null) {
                System.out.println("Client: " + inputLine);
                
                if (inputLine.equals("terminate")) {
                    System.out.println("Client disconnected: " + subscriberSocket.getInetAddress());
                    subscriberSocket.close();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
