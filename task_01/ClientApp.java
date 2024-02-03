import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientApp {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java MyClientApp <Server IP> <Server PORT>");
            System.exit(1);
        }

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);

        try (
            Socket socket = new Socket(serverIP, serverPort);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to server. Type 'terminate' to disconnect.");

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
}
