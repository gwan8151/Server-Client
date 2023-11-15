import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);
        try {
            socket = new Socket("localhost", 1234);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while (true) {
                System.out.print("Enter arithmetic expression (e.g., '+ 24 42', 'bye' to exit): ");
                String operation = scanner.nextLine(); 
                if (operation.equalsIgnoreCase("bye")) {
                    out.write("bye\n");
                    out.flush();
                    break;
                }
                out.write(operation + "\n"); 
                out.flush();
                String result = in.readLine(); 
                System.out.println("Result from server: " + result);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                scanner.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                System.out.println("Error occurred while closing the socket.");
            }
        }
    }
}
