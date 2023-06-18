import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    Socket socket;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;
    String username;

    public ClientHandler(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientHandlers.add(this);
        } catch (Exception e) {
            closeConnection();
        }
    }

    public void listenForMsg(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgToSend;
                while (socket.isConnected()){
                    try {
                        msgToSend = bufferedReader.readLine();
                        System.out.println(msgToSend);
                        broadcastMessage(msgToSend);

                    } catch (IOException e) {
                        closeConnection();
                    }
                }
            }
        }).start();
    }

    public void broadcastMessage(String msgToSend){
        try {
            for (ClientHandler clientHandler : clientHandlers){
                if(!clientHandler.username.equals(this.username)){
                    bufferedWriter.write(msgToSend);
                    System.out.println("dziala");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        } catch (IOException e) {
            closeConnection();
        }
    }

    public void removeClient(){
        clientHandlers.remove(this);
        broadcastMessage("Client disconnected");
        System.out.println("Client disconnected");
    }

    public void closeConnection(){
        removeClient();
        try{
            if(socket != null){
                socket.close();
            }
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
        }catch (Exception e){
            e.getMessage();
        }
    }
}
