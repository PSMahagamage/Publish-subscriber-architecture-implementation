import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientFiltered {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java ClientFiltered <Server IP> <Server PORT> <CLIENT_TYPE> <TOPIC_TYPE>");
            System.exit(1);
        }

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String clientType = args[2].toUpperCase();
        String topicType = args[3].toUpperCase();

        try (
                Socket socket = new Socket(serverIP, serverPort);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            // Send the client type to the server
            writer.println(clientType);
            writer.println(topicType);

            System.out.println("Connected to server. Type 'terminate' to disconnect.");

            if ("PUBLISHER".equals(clientType)) {
                handlePublisher(consoleReader, writer);
            } else if ("SUBSCRIBER".equals(clientType)) {
                handleSubscriber(socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handlePublisher(BufferedReader consoleReader, PrintWriter writer) {
        try {
            String userInput;
            while ((userInput = consoleReader.readLine()) != null) {
                writer.println(userInput);

                if (userInput.equals("terminate")) {
                    System.out.println("Disconnected from the server.");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleSubscriber(Socket subscriberSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(subscriberSocket.getInputStream()))) {
            String serverMessage;
            while ((serverMessage = reader.readLine()) != null) {
                System.out.println(serverMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
