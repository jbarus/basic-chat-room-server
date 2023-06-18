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
            closeConnection(socket, bufferedReader, bufferedWriter);
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
                        if(msgToSend == null){
                            closeConnection(socket, bufferedReader, bufferedWriter);
                            break;
                        }
                        broadcastMessage(msgToSend);
                    } catch (IOException e) {
                        closeConnection(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                }
            }
        }).start();
    }

    public void broadcastMessage(String msgToSend){

        for (ClientHandler clientHandler : clientHandlers){

            try {
                if(!clientHandler.username.equals(this.username)){
                    clientHandler.bufferedWriter.write(msgToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (Exception e) {
                closeConnection(socket, bufferedReader, bufferedWriter);
            }
        }

    }

    public void removeClient(){
        clientHandlers.remove(this);
        broadcastMessage("Client disconnected");
        System.out.println("rozłączono");
    }

    public void closeConnection(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
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
