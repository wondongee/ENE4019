import java.io.*;
import java.net.*;

public class WritingThreadTCP extends Thread{
    Socket socket = null;
    private Socket sendSocket = null;
    private Socket receiveSocket = null;
    String sentence;
    String hostName;
    int portNum2;

    public WritingThreadTCP(Socket socket, String hostName, int portNum2) {
        this.socket = socket;
        this.hostName = hostName;
        this.portNum2 = portNum2;
    }

    public void run() {
        try {
            BufferedReader inFromUser = new BufferedReader(new
                    InputStreamReader(System.in));
            OutputStream out = socket.getOutputStream();
            PrintWriter outToServer = new PrintWriter(out, true);

            while(true) {

                sentence = inFromUser.readLine();
                outToServer.println(sentence);
                String[] id = sentence.split(" ");

                /* client가 파일을 전송하거나 수신할 때 */
                if(id[0].equals("#PUT")) {

                    sendSocket = new Socket(hostName, portNum2); // server에게 파일전송 소켓 연결 요청

                    String fileName = id[1];

                    DataOutputStream os = new DataOutputStream(sendSocket.getOutputStream());
                    FileInputStream fis = new FileInputStream(fileName);
                    byte[] buffer = new byte[4096];
                    int readBytes = 0;
                    int sendedBytes = 0;


                    while ((readBytes= fis.read(buffer)) != -1) {
                        os.write(buffer, 0, readBytes);
                        sendedBytes = sendedBytes + readBytes;
                        if ((sendedBytes / 64000) == 1) {
                            System.out.println("#");
                            sendedBytes = sendedBytes - 64000;
                        }
                    }
                    os.flush();
                    os.close();
                    fis.close();
                    sendSocket.close();

                } else if (id[0].equals("#GET")) {

                    receiveSocket = new Socket(hostName, portNum2); // server에게 파일수신 소켓 연결 요청
                    String fileName = id[1];
                    File file = new File(fileName);
                    byte[] buffer = new byte[4096];
                    int readBytes;

                    BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream("receive.txt"));
                    BufferedInputStream is = new BufferedInputStream(
                            new DataInputStream(receiveSocket.getInputStream()));
                    while((readBytes = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, readBytes);
                    }
                    System.out.println("다운로드 하신 파일 이름은 receive.txt 입니다.");
                    fos.flush();
                    fos.close();
                    is.close();
                    receiveSocket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
