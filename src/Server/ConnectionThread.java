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
            Operations operations= Operations.valueOf((dataInputStream.readUTF()));
            while (operations!=Operations.Terminate||operations!=null)
            {
                if(operations.equals(Operations.Terminate))
                {
                    System.out.println("Exiting....");
                    socket.close();
                    break;
                }
                switch (operations)
                {
                    case List:
                        System.out.println("List");
                        break;
                    case Edit:
                        Edit();
                        break;
                    case Delete:
                        System.out.println("delete");
                        break;
                    case View:
                        view();
                        break;
                    case Auth:
                        if(Operations.valueOf(dataInputStream.readUTF()).equals(Operations.HandShake))
                            HandShake();
                        else
                            NoHandShake();
                        break;
                    case Terminate:
                        break;

                }
                operations= Operations.valueOf((dataInputStream.readUTF()));


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
   /* public void Auth() {
        PGP pgp= null;
        try {
            pgp = new PGP("Server");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        Response op=null;
        try {
            op= Response.valueOf(dataInputStream.readUTF());

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(main.keys.get(IP_Address)==null&&op.equals(Response.NoHandShake))
        {
            try {
                dataOutputStream.writeUTF(Response.Denied.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        else if(op.equals(Response.HandShake))
        {
            try {
                dataOutputStream.writeUTF(Base64.getEncoder().encodeToString(pgp.getPublicKey().getEncoded()));
                X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(dataInputStream.readUTF()));
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
    }*/
    public void HandShake(){
        try {
            PGP pgp=new PGP("Server");
            //dataOutputStream.writeUTF(Operations.Auth.toString());
           // dataOutputStream.writeUTF(Operations.HandShake.toString());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(dataInputStream.readUTF()));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            main.keys.put(IP_Address,kf.generatePublic(spec));
            dataOutputStream.writeUTF(Base64.getEncoder().encodeToString(pgp.getPublicKey().getEncoded()));
            //dataOutputStream.writeUTF(pgp.encryptText(Base64.getEncoder().encodeToString(Symmetric.createAESKey().getEncoded()),pgp.getPrivateKey()));
            key=pgp.decryptText(dataInputStream.readUTF(),main.keys.get(IP_Address));
            System.out.println("My Public Key: "+Base64.getEncoder().encodeToString(pgp.getPublicKey().getEncoded()));
            System.out.println("____________________________________________________________________________________");
            System.out.println("My Private Key: "+Base64.getEncoder().encodeToString(pgp.getPrivateKey().getEncoded()));
            System.out.println("____________________________________________________________________________________");
            System.out.println(IP_Address+" Public Key: "+Base64.getEncoder().encodeToString(main.keys.get(IP_Address).getEncoded()));
            System.out.println("____________________________________________________________________________________");
            System.out.println("Secret Key: "+key);
            System.out.println("____________________________________________________________________________________");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
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
    }
    public void NoHandShake(){
        try {
            PGP pgp=new PGP("Server");
            key=pgp.decryptText(dataInputStream.readUTF(),main.keys.get(IP_Address));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
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
    }
    public String Encrypt(String txt) {
        switch (encryption)
        {
            case non:
                return txt;
            case PGP:
                break;
            case Symmetric:
                try {
                    return new String(Base64.getEncoder().encodeToString(Symmetric.do_AESEncryption(txt,key==null?Symmetric.getDefault():Symmetric.getkeys(key),Connection.IV)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }
    public String decrypt(String txt) {
        switch (encryption)
        {
            case non:
                return txt;
            case PGP:
                break;
            case Symmetric:
                try {
                    return Symmetric.do_AESDecryption(Base64.getDecoder().decode(txt),key==null?Symmetric.getDefault():Symmetric.getkeys(key),Connection.IV);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }
    public void view() {
        String fileName= null;
        try {
            fileName = decrypt(dataInputStream.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file= new File("TextFiles/"+fileName+".txt");
        if(!file.exists())
        {
            try {
                dataOutputStream.writeUTF(Encrypt("File Not Found"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            String text;
            try {
                text = new String(Files.readAllBytes(Paths.get("TextFiles/"+fileName+".txt")));
                dataOutputStream.writeUTF(Encrypt(text));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void Edit() {
        String fileName= null;
        String txt=null;
        try {
            fileName = decrypt(dataInputStream.readUTF());
            txt = decrypt(dataInputStream.readUTF());

        } catch (IOException e) {
            e.printStackTrace();
        }
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
            fileWriter.write(txt);
            fileWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            e.printStackTrace();
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
            view();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
