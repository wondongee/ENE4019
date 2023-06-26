import java.io.*;
import java.net.*;

public class ListeningThread extends Thread {
    MulticastSocket clientSocket;
    public ListeningThread(MulticastSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            while (true) {
                byte[] receiveData = new byte[512];
                DatagramPacket receivePacket =
                        new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                System.out.println(sentence);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
