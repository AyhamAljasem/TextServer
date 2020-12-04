package Server;

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
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

public class ConnectionThread {
    protected Socket socket;
    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;
    protected Encryption encryption;
    protected String IP_Address;
    protected String key;
    public ConnectionThread(Socket socket) {
        this.socket = socket;
        try {
            dataInputStream=new DataInputStream(socket.getInputStream());
            dataOutputStream=new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void run() {
        try {
            encryption= Encryption.valueOf(dataInputStream.readUTF());
            IP_Address=dataInputStream.readUTF();
            System.out.println(encryption);
            dataOutputStream.writeUTF(Encrypt(Response.Ok.toString()));
            Operations operations= Operations.valueOf(decrypt(dataInputStream.readUTF()));
            while (operations!=Operations.Terminate||operations!=null)
            {
                switch (operations)
                {
                    case List:
                        System.out.println("List");
                        break;
                    case Edit:
                        System.out.println("edit");
                        break;
                    case Delete:
                        System.out.println("delete");
                        break;
                    case Update:
                        System.out.println("update");
                        break;

                }
                operations= Operations.valueOf(decrypt(dataInputStream.readUTF()));

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        /*String source=null;
        String fileName=null;
        String text=null;
        try {
            String recived;
            while (true)
            {
                recived=dataInputStream.readUTF();
                System.out.println(recived);
                if(recived==null||recived.equalsIgnoreCase("EXIT"))
                {
                    if(source!=null&&fileName!=null)
                    {
                        if(text==null)
                        {
                            view(fileName);
                        }
                        else {
                            edit(fileName,text);
                        }
                    }
                    else
                    {
                        System.out.println("Incomplete data");
                    }
                    socket.close();
                    return;
                }
                else{
                    if(source==null)
                    {
                        source=recived;
                    }
                    else if(fileName==null){
                        fileName=recived;
                    }
                    else if(recived!=null){
                        text=recived;
                    }

                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
    /*public List<String> List_Files(){
        File dir=new File("TextFiles");

    }
    public String Encrypt(String s)
    {

    }*/
    public void Auth()
    {
        if(main.keys.get(IP_Address)==null)
        {
            try {
                dataOutputStream.writeUTF();
                X509EncodedKeySpec spec = new X509EncodedKeySpec(dataInputStream.readUTF().getBytes());
                KeyFactory kf = KeyFactory.getInstance("RSA");
                main.keys.put(IP_Address,kf.generatePublic(spec));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

        }
        try {
            PGP pgp=new PGP("Server");
            try {
                dataOutputStream.writeUTF(Base64.getEncoder().encodeToString(pgp.getPublicKey().getEncoded()));
                key=Base64.getEncoder().encodeToString( Symmetric.createAESKey().getEncoded());
                dataOutputStream.writeUTF(pgp.encryptText(key,pgp.getPrivateKey()));
                while(true)
                {
                    if(Response.Ok.equals(Response.valueOf(dataInputStream.readUTF()))&&pgp.decryptText(dataInputStream.readUTF(),main.keys.get(IP_Address)).equals(key))
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
    public void view(String fileName) {
        File file= new File("TextFiles/"+fileName+".txt");
        if(!file.exists())
        {
            try {
                dataOutputStream.writeUTF("File Not Found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            String text;
            try {
                text = new String(Files.readAllBytes(Paths.get("TextFiles/"+fileName+".txt")));
                dataOutputStream.writeUTF(text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void edit(String fileName,String text) {
        File file= new File("TextFiles/"+fileName+".txt");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file.getPath());
            fileWriter.write(text);
            fileWriter.close();
            System.out.println("Successfully wrote to the file.");
            view(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
