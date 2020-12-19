package Encryp;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.crypto.NoSuchPaddingException;

public class CSR {


    private   String CERTIFICATE_ALIAS = "FITE";
    private   String CERTIFICATE_DN = "CN=cn, O=o, L=L, ST=il, C= c";
    private   String CERTIFICATE_NAME = "FITE.test";

    public CSR(String CERTIFICATE_ALIAS, String CERTIFICATE_NAME) {
        this.CERTIFICATE_ALIAS = CERTIFICATE_ALIAS;
        CERTIFICATE_DN = "CN="+CERTIFICATE_ALIAS+", O=o, L=L, ST=il, C= c";
        this.CERTIFICATE_NAME = CERTIFICATE_NAME;
    }

    static {
        // adds the Bouncy castle provider to java security
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
      //  CSR signedCertificate = new CSR();
       // System.out.println(signedCertificate.createCertificate().toString());
    }

    public X509Certificate createCertificate(PublicKey publicKey,PrivateKey privateKey) throws Exception{
        X509Certificate cert = null;

        // GENERATE THE X509 CERTIFICATE
        X509V3CertificateGenerator v3CertGen =  new X509V3CertificateGenerator();
        v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        v3CertGen.setIssuerDN(new X509Principal(CERTIFICATE_DN));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)));
        v3CertGen.setSubjectDN(new X509Principal(CERTIFICATE_DN));
        v3CertGen.setPublicKey(publicKey);
        v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        cert = v3CertGen.generateX509Certificate(privateKey);
        saveCert(cert,privateKey);
        return cert;
    }

    public static boolean verifyIdentitiy(String certificate,PublicKey publicKey){
        try {
            CertificateFactory certificateFactory=CertificateFactory.getInstance("X.509");
            Certificate certificate1=certificateFactory.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(certificate)));
            System.out.println(certificate1.toString());
            System.out.println("-----------------------------------------");
            certificate1.verify(publicKey);
            if(certificate1.getPublicKey().equals(publicKey))
                return true;
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveCert(X509Certificate cert, PrivateKey key) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setKeyEntry(CERTIFICATE_ALIAS, key, "password".toCharArray(),  new java.security.cert.Certificate[]{cert});
        File file = new File(".", CERTIFICATE_NAME);
        keyStore.store( new FileOutputStream(file), "password".toCharArray() );
    }

}
