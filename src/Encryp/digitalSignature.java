package Encryp;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;
import java.util.Scanner;

//import javax.xml.bind.DatatypeConverter;

public class digitalSignature {

    // Signing Algorithm
    private static final String
            SIGNING_ALGORITHM
            = "SHA256withRSA";
    private static final String RSA = "RSA";

    // Function to implement Digital signature
    // using SHA256 and RSA algorithm
    // by passing private key.
    public static byte[] Create_Digital_Signature(
            byte[] input,
            PrivateKey Key)
            throws Exception
    {
        Signature signature
                = Signature.getInstance(
                SIGNING_ALGORITHM);
        signature.initSign(Key);
        signature.update(input);
        return signature.sign();
    }

    public static boolean
    Verify_Digital_Signature(
            byte[] input,
            byte[] signatureToVerify,
            PublicKey key)
            throws Exception
    {
        Signature signature
                = Signature.getInstance(
                SIGNING_ALGORITHM);
        signature.initVerify(key);
        signature.update(input);
        return signature
                .verify(signatureToVerify);
    }

   /* public static void main(String args[])
            throws Exception
    {

        String input
                = "GEEKSFORGEEKS IS A"
                + " COMPUTER SCIENCE PORTAL";
        KeyPair keyPair
                = Generate_RSA_KeyPair();

        // Function Call
        byte[] signature
                = Create_Digital_Signature(
                input.getBytes(),
                keyPair.getPrivate());

        System.out.println(
                "Signature Value:\n "
                        + Base64.getEncoder().encodeToString(signature));

        System.out.println(
                "Verification: "
                        + Verify_Digital_Signature(
                        input.getBytes(),
                        signature, keyPair.getPublic()));
    }*/
}