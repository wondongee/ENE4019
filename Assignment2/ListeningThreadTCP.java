import java.io.*;
import java.net.Socket;

public class ListeningThreadTCP extends Thread {
    Socket socket = null;
    public ListeningThreadTCP(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader inFromServer = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            while(true) {
                System.out.println( inFromServer.readLine() );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
