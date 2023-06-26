import java.io.*;
import java.net.Socket;
import java.util.*;

public class MySocketClient {
    public static void main(String[] args) {
        try {
            Socket clientSocket = null;

            Scanner scanner = new Scanner(System.in);
            System.out.print("java Client ");
            String hostName = scanner.next();
            int portNum1 = scanner.nextInt();
            int portNum2 = scanner.nextInt();

            clientSocket = new Socket(hostName, portNum1);

            ListeningThreadTCP t1 = new ListeningThreadTCP(clientSocket);
            WritingThreadTCP t2 = new WritingThreadTCP(clientSocket, hostName, portNum2);

            t1.start();
            t2.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
