package Client.UI;

import Basic.Connection;
import Basic.Encryption;
import Basic.Operations;
import Basic.Response;
import Encryp.CSR;
import Encryp.PGP;
import Encryp.Symmetric;
import Encryp.digitalSignature;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Home extends Application {
    public static String IP_ADDRESS;
    public static DataInputStream dataInputStream;
    public static DataOutputStream dataOutputStream;
    public Socket socket;
    public static Encryption encryption=Encryption.Symmetric;
    private String key;
    Map<String, PublicKey> keys=new HashMap<>();
    boolean sign=false;
    boolean authinticated=false;

    @FXML
    Label PK;
    @FXML
    Label PPK;
    @FXML
    Label SK;
    @FXML
    Label Cert;
    @FXML
    TextField fn;
    @FXML
    TextArea tf;


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        try {
            this.socket=new Socket(Connection.IP,Connection.PORT);
            dataInputStream=new DataInputStream(socket.getInputStream());
            dataOutputStream=new DataOutputStream(socket.getOutputStream());
            IP_ADDRESS= InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println("Network Error");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Connect();
    }



    public void Connect() {
        try {
            dataOutputStream.writeUTF(encryption.toString());
            dataOutputStream.writeUTF(IP_ADDRESS);
            Response response= Response.valueOf(decrypt(dataInputStream.readUTF()));
            if(!response.equals(Response.Ok)) throw new IOException("Opening Connection Error"+response.toString());
           /* else {

                Scanner s=new Scanner(new InputStreamReader(System.in)).useDelimiter("\n");
                int choice=-1;
                while (choice!=0)
                {
                    System.out.println("1-HandShake\n2-View\n3-Edit\n4-Use Signature\n5-Verify Identity\n6-GenerateCertificate\n0-Exit");
                    choice=s.nextInt();
                    switch (choice)
                    {
                        case 1:
                            HandShake(Connection.IP);
                            break;
                        case 2:
                            System.out.println("File Name:\n");
                            String fileName=s.next();
                            View(fileName);
                            break;
                        case 3:
                            System.out.println("File Name:\n");
                            String fileName1=s.next();
                            System.out.println("Text:\n");
                            String txt=s.next();
                            s.nextLine();
                            Edit(fileName1,txt);
                            break;
                        case 4:
                            useSignature();
                            break;
                        case 5:
                            verifyIdentitiy();
                            break;
                        case 6:
                            createCert();
                            break;
                        case 0:
                            dataOutputStream.writeUTF(Operations.Terminate.toString());
                            break;
                    }


                }
            }*/
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
    public void verifyIdentitiy(){
        if(!authinticated)
            HandShake();
        try {
            dataOutputStream.writeUTF(Operations.verifyIdentity.toString());
            String temp=dataInputStream.readUTF();
            System.out.println("Identity verified: "+ CSR.verifyIdentitiy(temp,keys.get(Connection.IP)));
            CertificateFactory certificateFactory= null;
            try {
                certificateFactory = CertificateFactory.getInstance("X.509");
                Certificate certificate1=certificateFactory.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(temp)));
                Cert.setText(certificate1.toString());

            } catch (CertificateException e) {
                e.printStackTrace();
            }

            dataOutputStream.writeUTF(Base64.getEncoder().encodeToString(createCert().getEncoded()));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
    }
    public X509Certificate createCert(){
        CSR csr=new CSR("Client","CleintCert");
        try {
            return csr.createCertificate(new PGP(IP_ADDRESS).getPublicKey(),new PGP(IP_ADDRESS).getPrivateKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void HandShake(){
        try {
            PGP pgp=new PGP(this.IP_ADDRESS);
            dataOutputStream.writeUTF(Operations.Auth.toString());
            dataOutputStream.writeUTF(Operations.HandShake.toString());
            dataOutputStream.writeUTF(Base64.getEncoder().encodeToString(pgp.getPublicKey().getEncoded()));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(dataInputStream.readUTF()));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            keys.put(Connection.IP,kf.generatePublic(spec));
            key=Base64.getEncoder().encodeToString( Symmetric.createAESKey().getEncoded());
            SK.setText("AES KEY: "+key);
            dataOutputStream.writeUTF(pgp.encryptText(key,pgp.getPrivateKey()));
            PK.setText("My Public Key: "+Base64.getEncoder().encodeToString(pgp.getPublicKey().getEncoded()));
            System.out.println("____________________________________________________________________________________");
            PPK.setText("My Private Key: "+Base64.getEncoder().encodeToString(pgp.getPrivateKey().getEncoded()));
            System.out.println("____________________________________________________________________________________");
            System.out.println(Connection.IP+" Public Key: "+Base64.getEncoder().encodeToString(keys.get(Connection.IP).getEncoded()));
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
        authinticated=true;
    }
    public void NoHandShake(String IP_ADDRESS){
        try {
            PGP pgp=new PGP(this.IP_ADDRESS);
            dataOutputStream.writeUTF(Operations.Auth.toString());
            dataOutputStream.writeUTF(Operations.NoHandShake.toString());
            key=Base64.getEncoder().encodeToString( Symmetric.createAESKey().getEncoded());
            dataOutputStream.writeUTF(pgp.encryptText(key,pgp.getPrivateKey()));
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
                    return new String(Base64.getEncoder().encodeToString(Symmetric.do_AESEncryption(txt, key==null?Symmetric.getDefault():Symmetric.getkeys(key),Connection.IV)));
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
                    return Symmetric.do_AESDecryption(Base64.getDecoder().decode(txt),key==null?Symmetric.getDefault():Symmetric.getkeys(key),Connection.IV);
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
    public void View(){
        String file=fn.getText();
        try {
            dataOutputStream.writeUTF(Operations.View.toString());
            dataOutputStream.writeUTF(Encrypt(file));
            if(sign)
            {
                dataOutputStream.writeUTF(Base64.getEncoder().encodeToString(digitalSignature.Create_Digital_Signature(file.getBytes(),new PGP(IP_ADDRESS).getPrivateKey())));
            }
            System.out.println("Encrypted file name: "+Encrypt(file));
            String txt=dataInputStream.readUTF();
            if(sign)
            {
                String temp=dataInputStream.readUTF();
                byte[] signature=Base64.getDecoder().decode(temp);
                boolean verified=digitalSignature.Verify_Digital_Signature(decrypt(txt).getBytes(),signature,keys.get(Connection.IP));
                System.out.println("verified: "+verified);

            }
            System.out.println("Encrypted Text: "+txt);
            tf.setText(decrypt(txt));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void Edit(){
        try {
            String file=fn.getText();
            String txt=tf.getText();
            dataOutputStream.writeUTF(Operations.Edit.toString());
            dataOutputStream.writeUTF(Encrypt(file));
            if(sign)
            {
                dataOutputStream.writeUTF(Base64.getEncoder().encodeToString(digitalSignature.Create_Digital_Signature(file.getBytes(),new PGP(IP_ADDRESS).getPrivateKey())));
            }
            System.out.println("Encrypted file name: "+Encrypt(file));
            dataOutputStream.writeUTF(Encrypt(txt));
            if(sign)
            {
                dataOutputStream.writeUTF(Base64.getEncoder().encodeToString(digitalSignature.Create_Digital_Signature(txt.getBytes(),new PGP(IP_ADDRESS).getPrivateKey())));
            }
            System.out.println("Encrypted Text: "+Encrypt(txt));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void useSignature()
    {
        if(!authinticated)
            HandShake();
        sign=true;
        try {
            dataOutputStream.writeUTF(Operations.UseSignature.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}
