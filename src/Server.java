import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
            ) {
                while (true) {
                    String inputMessage = in.readLine();
                    if (inputMessage == null || inputMessage.equalsIgnoreCase("bye")) {
                        System.out.println("Client disconnected");
                        break;
                    }
                    System.out.println("Received message from client: " + inputMessage);
                    String res = handleOperation(inputMessage); // Use handleOperation method to process the message
                    out.write(res + "\n");
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }

        private String handleOperation(String message) {
            StringTokenizer st = new StringTokenizer(message, " ");
            if (st.countTokens() > 3) {
                return "Too many arguments";
            }
            if (st.countTokens() < 3) {
                return "Insufficient arguments";
            }
            String operation = st.nextToken();
            int operand1, operand2;
            try {
                operand1 = Integer.parseInt(st.nextToken());
                operand2 = Integer.parseInt(st.nextToken());
            } catch (NumberFormatException e) {
                return "Invalid operands";
            }

            switch (operation) {
                case "+":
                    return Integer.toString(operand1 + operand2);
                case "-":
                    return Integer.toString(operand1 - operand2);
                case "*":
                    return Integer.toString(operand1 * operand2);
                case "/":
                    if (operand2 == 0) {
                        return "Division by zero error";
                    } else {
                        return Integer.toString(operand1 / operand2);
                    }
                default:
                    return "Unsupported operation";
            }
        }

    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10); // Pool of 10 threads
        String filePath = "server_info.dat"; // Path to the server details file

        // Read server details from the file
        try (BufferedReader fileReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String ipAddress = null;
            int port = -1;

            while ((line = fileReader.readLine()) != null) {
                if (line.startsWith("Server IP Address: ")) {
                    ipAddress = line.substring("Server IP Address: ".length());
                } else if (line.startsWith("Server Port: ")) {
                    port = Integer.parseInt(line.substring("Server Port: ".length()));
                }
            }

            if (ipAddress != null && port != -1) {
                try (ServerSocket listener = new ServerSocket(port, 50, InetAddress.getByName(ipAddress))) {
                    System.out.println("Server is running on " + ipAddress + ":" + port);
                    while (true) {
                        Socket socket = listener.accept();
                        System.out.println("New client connected");
                        executor.submit(new ClientHandler(socket));
                    }
                } catch (IOException e) {
                    System.out.println("Server exception: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid server details in the file.");
            }
        } catch (IOException e) {
            System.out.println("Error reading server details from file: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

}
