import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class EncryptionHandler {

    PublicKey serverPublicKey;
    PublicKey clientPublicKey;
    PrivateKey serverPrivateKey;
    Cipher encryptCipher;
    Cipher decryptCipher;

    public EncryptionHandler(Socket socket, BufferedReader bufferedReader) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            serverPublicKey = keyPair.getPublic();
            serverPrivateKey = keyPair.getPrivate();
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.putInt(serverPublicKey.getEncoded().length);
            socket.getOutputStream().write(byteBuffer.array());
            socket.getOutputStream().write(serverPublicKey.getEncoded());
            socket.getOutputStream().flush();

            byte[] lenb = new byte[4];
            socket.getInputStream().read(lenb,0,4);
            ByteBuffer bb = ByteBuffer.wrap(lenb);
            int len = bb.getInt();
            byte[] clientPubKeyBytes = new byte[len];
            socket.getInputStream().read(clientPubKeyBytes);
            X509EncodedKeySpec ks = new X509EncodedKeySpec(clientPubKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            clientPublicKey = kf.generatePublic(ks);

            encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, clientPublicKey);
            decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, serverPrivateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String encryptData(String data){
        try {
            byte[] messageBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] encryptMessageBytes = encryptCipher.doFinal(messageBytes);
            return Base64.getEncoder().encodeToString(encryptMessageBytes);
        } catch (Exception e) {
            e.getMessage();
        }
        return null;
    }

    public String decryptData(String data){
        try {
            byte[] decodedMessage = Base64.getDecoder().decode(data);
            byte[] decryptedMessageBytes = decryptCipher.doFinal(decodedMessage);
            return new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.getMessage();
        }
        return null;
    }
}
