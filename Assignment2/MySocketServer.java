import java.io.*;
import java.net.*;
import java.util.*;

public class MySocketServer extends Thread {
    static ArrayList<Socket> list = new ArrayList<Socket>();
    static HashMap<Socket, String> socketToChat = new HashMap<>();
    static HashMap<Socket, String> socketToUser = new HashMap<>();

    Socket serverSocket = null;
    static ServerSocket fileServerSocket;

    public MySocketServer(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() {
        try {

            String readValue = null;
            String userName = null;
            String chatRoom = null;
            boolean status = false;
            Socket receiveSocket = null;
            Socket sendSocket = null;

            // client에서 보낸 메세지 읽기
            BufferedReader inFromClient = new BufferedReader(new
                    InputStreamReader(serverSocket.getInputStream()));

            // server에서 client로 메세지 보내기
            OutputStream out = serverSocket.getOutputStream();
            PrintWriter outToClient = new PrintWriter(out, true);
            outToClient.println("서버에 연결되었습니다! 명령어를 입력해주세요.");

            while((readValue = inFromClient.readLine()) != null) {

                /* client로 부터 "#"으로 시작하는 명령어가 입력될 때 */
                if (readValue.substring(0,1).equals("#")) {
                    String[] id = readValue.split(" ");

                    if(status == true && id[0].equals("#PUT")) {
                        receiveSocket = fileServerSocket.accept();   // file 송수신을 위한 새로운 TCP 연결
                        System.out.println("Server : 파일수신 소켓 연결 완료");

                        byte[] buffer = new byte[4096];
                        int readBytes;

                        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream("output.txt"));
                        BufferedInputStream is = new BufferedInputStream(
                                new DataInputStream(receiveSocket.getInputStream()));

                        while((readBytes = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, readBytes);
                        }
                        System.out.println("Server : 파일 수신완료");

                        for(int i=0; i<list.size(); i++) {
                            if (status == true && (chatRoom.equals(socketToChat.get(list.get(i))))) {
                                if (list.get(i) == serverSocket) {
                                    continue;
                                }
                                out = list.get(i).getOutputStream();
                                outToClient = new PrintWriter(out, true);
                                outToClient.println("채팅방에 파일이 업로드되었습니다. 업로드된 파일의 이름은 [output.txt] 입니다.");
                            }
                        }
                        fos.flush();
                        fos.close();
                        is.close();
                        receiveSocket.close();

                    }  else if (status == true && id[0].equals("#GET")) {
                        sendSocket = fileServerSocket.accept();
                        System.out.println("Server : 파일전송 소켓 연결 완료");
                        int sendedBytes = 0;

                        DataOutputStream os = new DataOutputStream(sendSocket.getOutputStream());
                        FileInputStream fis = new FileInputStream("output.txt");
                        byte[] buffer = new byte[4096];
                        int readBytes = 0;
                        while ((readBytes = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, readBytes);
                            sendedBytes = sendedBytes + readBytes;
                            if ((sendedBytes / 64000) == 1) {
                                outToClient.println("#");
                                sendedBytes = sendedBytes - 64000;
                            }
                        }
                        System.out.println("Server : 파일 송신완료");
                        os.flush();
                        os.close();
                        fis.close();
                        sendSocket.close();


                    } else if ((!status && id[0].equals("#PUT")) || (!status && id[0].equals("#GET"))) {
                        System.out.println("채팅방에 참여한 후 파일을 주고받으세요! 프로그램을 종료합니다.");
                        System.exit(0);
                    } else if (id[0].equals("#CREATE")) {
                        if(socketToChat.containsValue(id[1])) {
                            outToClient.println("[실패] 이미 존재하고 있는 채팅방입니다.");
                            continue;
                        }
                        chatRoom = id[1];
                        userName = id[2];
                        socketToChat.put(serverSocket, chatRoom);
                        socketToUser.put(serverSocket, userName);
                        list.add(serverSocket);
                        outToClient.println("[성공] 채팅방을 생성하였습니다 : " + chatRoom);
                        status = true;

                    } else if (id[0].equals("#JOIN")) {
                        if(!socketToChat.containsValue(id[1])) {
                            outToClient.println("[실패] 존재하지 않는 채팅방입니다.");
                            continue;
                        }
                        chatRoom = id[1];
                        userName = id[2];
                        socketToChat.put(serverSocket, chatRoom);
                        socketToUser.put(serverSocket, userName);
                        list.add(serverSocket);
                        outToClient.println("[성공] 채팅방에 참여하였습니다 : " + chatRoom);
                        status = true;

                    } else if (status == true && id[0].equals("#EXIT")) {
                        list.remove(serverSocket);
                        socketToChat.remove(serverSocket);
                        socketToUser.remove(serverSocket);
                        status = false;

                    } else if (status == true && id[0].equals("#STATUS")) {

                        outToClient.println("현재 참여하신 채팅방은 [" + chatRoom + "] 입니다.");
                        for(int i=0; i<list.size(); i++) {
                            if(chatRoom.equals( socketToChat.get(list.get(i)) )) {
                                outToClient.print("(" + socketToUser.get(list.get(i)) + ") ");
                            }
                        }
                        outToClient.println("이 대화에 참여중입니다.");
                    }
                    continue;
                }

                /* client로 부터 명령어가 아닌 대화가 입력될 때 */
                for(int i=0; i<list.size(); i++) {
                    if( status == true && (chatRoom.equals( socketToChat.get(list.get(i)))) ) {
                        if (list.get(i) == serverSocket) {
                            continue;
                        }
                        OutputStream outMessage = list.get(i).getOutputStream();
                        PrintWriter outToClients = new PrintWriter(outMessage, true);
                        outToClients.println("FROM " + userName + " : " + readValue);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("java Server ");
            int portNum1 = scanner.nextInt();
            int portNum2 = scanner.nextInt();
            ServerSocket welcomeSocket = new ServerSocket(portNum1);
            fileServerSocket = new ServerSocket(portNum2);  // file전송 server socket

            // 서버 소켓이 종료될 때 까지 무한루프
            while(true) {
                Socket connectionSocket = welcomeSocket.accept();
                Thread thd = new MySocketServer(connectionSocket);
                thd.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
