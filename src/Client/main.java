package Client;

import Basic.Connection;
import Basic.Encryption;
import Basic.Operations;
import Basic.Response;
import Encryp.PGP;
import Encryp.Symmetric;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class main {


    public static void main(String[] args) {
        clients();

    }
    public static void clients()
    {
        Socket socket=null;
        try {
            socket=new Socket(Connection.IP,Connection.PORT);
            DataInputStream inputStream=new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream=new DataOutputStream(socket.getOutputStream());
            Client client=new Client(outputStream,inputStream);
            client.Connect();
          /*  outputStream.writeUTF(( new Random().nextInt())+" oops");
            outputStream.writeUTF("testW");
            outputStream.writeUTF("Written from Client");
            outputStream.writeUTF("Exit");*/
            try{
            System.out.println(inputStream.readUTF());}
            catch (Exception e)
            {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
class Client {
    public static String IP_ADDRESS;
    public static DataInputStream dataInputStream;
    public static DataOutputStream dataOutputStream;
    public static Encryption encryption=Encryption.Symmetric;
    public boolean authinticated=false;
    private String key;
    Map<String, PublicKey> keys;
    public Client(DataOutputStream dataOutputStream,DataInputStream dataInputStream){
        this.dataInputStream=dataInputStream;
        this.dataOutputStream=dataOutputStream;
        try {
            IP_ADDRESS= InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println("Network Error");
        }
        keys=new HashMap<>();
    }
    public void Connect() {
        try {

            dataOutputStream.writeUTF(encryption.toString());
            dataOutputStream.writeUTF(IP_ADDRESS);
            Response response= Response.valueOf(decrypt(dataInputStream.readUTF()));
            if(!response.equals(Response.Ok)) throw new IOException("Opening Connection Error"+response.toString());
            else dataOutputStream.writeUTF(Encrypt("List"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
    public void Auth(String IP)
    {
        if(keys.get(IP)==null)
        {
            try {
                dataOutputStream.writeUTF(Operations.Auth.toString());
                X509EncodedKeySpec spec = new X509EncodedKeySpec(dataInputStream.readUTF().getBytes());
                KeyFactory kf = KeyFactory.getInstance("RSA");
                keys.put(IP,kf.generatePublic(spec));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

        }
        try {
            PGP pgp=new PGP(IP_ADDRESS);
            try {
                dataOutputStream.writeUTF(Base64.getEncoder().encodeToString(pgp.getPublicKey().getEncoded()));
                key=Base64.getEncoder().encodeToString( Symmetric.createAESKey().getEncoded());
                dataOutputStream.writeUTF(pgp.encryptText(key,pgp.getPrivateKey()));
                while(true)
                {
                    if(Response.Ok.equals(Response.valueOf(dataInputStream.readUTF()))&&pgp.decryptText(dataInputStream.readUTF(),keys.get(IP)).equals(key))
                        break;
                    dataOutputStream.writeUTF(pgp.encryptText(Base64.getEncoder().encodeToString( Symmetric.createAESKey().getEncoded()),pgp.getPrivateKey()));

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public String Encrypt(String txt)
    {
        switch (encryption)
        {
            case non:
                return txt;
            case PGP:
                break;
            case Symmetric:
                try {
                    return new String(Base64.getEncoder().encodeToString(Symmetric.do_AESEncryption(txt, Symmetric.getDefault(),Connection.IV)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }
    public String decrypt(String txt)
    {
        switch (encryption)
        {
            case non:
                return txt;
            case PGP:
                break;
            case Symmetric:
                try {
                    return Symmetric.do_AESDecryption(Base64.getDecoder().decode(txt),Symmetric.getDefault(),Connection.IV);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }
    public List<String> fileList()
    {
        List<String> file=null;
        try {
            dataOutputStream.writeUTF(Operations.List.toString());
            Response response= Response.valueOf(dataInputStream.readUTF());
            if(response.equals(Response.Ok))
            {
                ObjectInputStream objectInputStream=new ObjectInputStream(dataInputStream);
                file= (List<String>) objectInputStream.readObject();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return file;
    }
}