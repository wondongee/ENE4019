import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;

public class UDPMulticast {
    MulticastSocket socket = null;
    InetAddress group = null;
    int port;
    String address;
    String userName;

    public UDPMulticast(int port, String address, String userName) {
        this.port = port;
        try {
            socket = new MulticastSocket(port);
            group = InetAddress.getByName(address);
            socket.joinGroup(group);

            ListeningThread t1 = new ListeningThread(socket);
            WritingThread t2 = new WritingThread(socket, group, port, userName);
            t1.start();
            t2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String addrHashing(String address) {
        try {
            MessageDigest mdSHA256 = MessageDigest.getInstance("SHA-256");
            mdSHA256.update(address.getBytes("UTF-8"));
            byte[] sha256Hash = mdSHA256.digest();
            address = "225";
            for(int i=1; i<=3; i++) {
                address = address +  "." + (sha256Hash[sha256Hash.length - i] + 128);
            }
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return address;
    }

    public static void main(String args[]) throws IOException {
        int port;
        String address;
        String userName;
        Scanner sc = new Scanner(System.in);
        System.out.print("Java Peer ");
        port = sc.nextInt();
        System.out.println("참여하고자 하는 채팅방 이름과 별명을 설정해주세요.");
        System.out.println("예시 : #JOIN (채팅방 이름) (사용자 이름)");

        String command = sc.next();
        if (command.equals("#JOIN")) {
            address = sc.next();
            userName = sc.next();
            address = addrHashing(address);

            new UDPMulticast(port, address, userName);
        } else {
            System.out.println("잘못 입력하셨습니다.");
            System.out.println("프로그램을 종료합니다.");
        }
    }
}
