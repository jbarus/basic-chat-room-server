import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static ServerSocket serverSocket;
    private static int port = 55555;


    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(55555);
        while(!serverSocket.isClosed()){
            Socket socket = serverSocket.accept();
            System.out.println("A new client has connected");
            ClientHandler clientHandler = new ClientHandler(socket);
            clientHandler.listenForMsg();
        }
    }
}