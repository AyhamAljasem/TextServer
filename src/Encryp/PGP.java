package Encryp;

import Basic.Connection;
import Basic.Response;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PGP {
    private  KeyPairGenerator keyGen;
    private  KeyPair pair;
    private  PrivateKey privateKey;
    public  PublicKey publicKey;
    private Cipher cipher;
    String Name;
    public PGP(String Name) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        this.Name=Name;
        this.cipher = Cipher.getInstance("RSA");
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(Connection.KEY_LEGNTH);
        File puKey=new File("KeyPair/"+this.Name+"/publicKey");
        File prKey=new File("KeyPair/"+this.Name+"/privateKey");
        if(puKey.exists()&&prKey.exists())
        {
            try {
                privateKey=getPrivate("KeyPair/"+this.Name+"/privateKey");
                publicKey=getPublic("KeyPair/"+this.Name+"/publicKey");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            createKeys();
            try {
                writeToFile("KeyPair/"+this.Name+"/publicKey", getPublicKey().getEncoded());
                writeToFile("KeyPair/"+this.Name+"/privateKey", getPrivateKey().getEncoded());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public  void createKeys() {
        pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }

    public  PrivateKey getPrivateKey() {
        return privateKey;
    }

    public  PublicKey getPublicKey() {
        return publicKey;
    }

    public static void writeToFile(String path, byte[] key) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(key);
        fos.flush();
        fos.close();
    }

    public static PrivateKey getPrivate(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static PublicKey getPublic(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public String encryptText(String msg, PrivateKey key)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            UnsupportedEncodingException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        this.cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(msg.getBytes("UTF-8")));
    }

    public String decryptText(String msg, PublicKey key)
            throws InvalidKeyException, UnsupportedEncodingException,
            IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(msg)), "UTF-8");
    }

    public void main(String[] args) {
        try {
            PGP p=new PGP("test");
            System.out.println(publicKey);
            System.out.println(privateKey);
            String s=p.encryptText(Response.NotFound.toString(),privateKey);
            System.out.println(s);
            System.out.println(p.decryptText(s,publicKey));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
