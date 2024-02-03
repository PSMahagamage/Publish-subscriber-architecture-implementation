import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServerFiltered {
    // private static final List<PrintWriter> clientWriters = new ArrayList<>();
    private static final Map<String, Set<PrintWriter>> topicSubscribers = new HashMap<>();

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
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
            // clientWriters.add(writer);

            String clientType = reader.readLine();
            String topicType = reader.readLine();

            PrintWriter newSubscriber = writer;

            // Retrieve the set of subscribers for the topic, or create a new set if it
            // doesn't exist
            Set<PrintWriter> subscribers = topicSubscribers.getOrDefault(topicType, new HashSet<>());

            // Add the new subscriber to the set
            subscribers.add(newSubscriber);

            // Update the map with the modified set of subscribers
            topicSubscribers.put(topicType, subscribers);

            System.out.println("Client Topic Type: " + topicType);

            if ("PUBLISHER".equals(clientType)) {
                handlePublisher(clientSocket, reader, topicType);
            } else if ("SUBSCRIBER".equals(clientType)) {
                handleSubscriber(clientSocket, topicType);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handlePublisher(Socket publisherSocket, BufferedReader publisherReader, String topic) {
        try {
            String inputLine;

            while ((inputLine = publisherReader.readLine()) != null) {
                System.out.println("Publisher on topic " + topic + ": " + inputLine);

                // Send the message to all subscribers interested in the topic
                Set<PrintWriter> subscribers = topicSubscribers.getOrDefault(topic, new HashSet<>());
                for (PrintWriter subscriberWriter : subscribers) {
                    subscriberWriter.println("Publisher on topic " + topic + ": " + inputLine);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleSubscriber(Socket subscriberSocket, String topicType) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(subscriberSocket.getInputStream()))) {
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
