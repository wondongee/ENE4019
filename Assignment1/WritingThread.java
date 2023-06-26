import java.io.*;
import java.net.*;

public class WritingThread extends Thread{ // 서버로 메세지 보내는 Thread
    MulticastSocket clientSocket;
    InetAddress IPAddress;
    int port;
    String userName;

    public WritingThread(MulticastSocket clientSocket, InetAddress IPAddress, int port, String userName) {
        this.clientSocket = clientSocket;
        this.IPAddress = IPAddress;
        this.port = port;
        this.userName = userName;
    }

    public void run() {
        try {
            while(true) {
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                String sentence = inFromUser.readLine();
                if (sentence.equals("#EXIT")) {
                    clientSocket.leaveGroup(IPAddress);
                    clientSocket.close();
                } else if ((sentence.substring(0,1)).equals("#")) {
                    continue;
                }
                sentence = userName + " : " + sentence;
                byte[] sendData = new byte[512];
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                clientSocket.send(sendPacket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
