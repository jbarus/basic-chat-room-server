import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    private static ServerSocket serverSocket;
    private static int port = 55555;
    private static int count = 1;
    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(55555);
        while(!serverSocket.isClosed()){
            Socket socket = serverSocket.accept();
            System.out.println("A new client has connected");
            ClientHandler clientHandler = new ClientHandler(socket,String.valueOf(count));
            count++;
            clientHandler.listenForMsg();
        }
    }
}