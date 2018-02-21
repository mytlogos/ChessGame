import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 */
public class ClientTester {
    public static void main(String[] args) {
        client();
    }

    static void testSockets() {
        Thread client = new Thread(ClientTester::client);
        client.setDaemon(false);
        client.setName("Client");
        client.start();
    }

    private static void client() {
        try {
            Thread.sleep(2000);
            Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(), 5000);

            System.out.println("connected to " + socket);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                System.out.println("reading");

                while ((line = reader.readLine()) != null) {
                    System.out.println("read sth");
                    System.out.println(line);
                }
                System.out.println("stopped reading");
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        System.out.println("stopped listening");
    }

}
