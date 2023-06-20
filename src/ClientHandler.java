import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    Socket socket;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;
    String username;
    EncryptionHandler encryptionHandler;


    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            encryptionHandler = new EncryptionHandler(socket, bufferedReader);
            String temp = bufferedReader.readLine();
            if(temp == null){temp = "not stated";}
            broadcastMessage(temp+" has connected");
            this.username = temp;
            clientHandlers.add(this);
        } catch (Exception e) {
            closeConnection(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMsg(){
        new Thread(() -> {
            String msgToSend;
            while (socket.isConnected()){
                try {
                    msgToSend = bufferedReader.readLine();
                    if(msgToSend == null){
                        closeConnection(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                    msgToSend = encryptionHandler.decryptData(msgToSend);
                    broadcastMessage(username + ": " + msgToSend);
                } catch (IOException e) {
                    closeConnection(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();
    }

    public void broadcastMessage(String msgToSend){

        for (ClientHandler clientHandler : clientHandlers){

            try {
                if(!clientHandler.username.equals(this.username)){
                    msgToSend = clientHandler.encryptionHandler.encryptData(msgToSend);
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
        broadcastMessage(this.username+" has disconnected");
        System.out.println(this.username+" has disconnected");
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
