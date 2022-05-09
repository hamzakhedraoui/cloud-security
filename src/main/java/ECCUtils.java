import com.codahale.shamir.Scheme;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.IESParameterSpec;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

public class ECCUtils {
    final Scheme scheme = new Scheme(new SecureRandom(), 4, 4);
    public void ECCtest() {
        try {
            ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
            KeyPairGenerator g = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
            g.initialize(ecSpec, new SecureRandom());
            KeyPair pair = g.generateKeyPair();
            ECPrivateKey privateKey1 = (ECPrivateKey) pair.getPrivate();
            ECPublicKey publicKey1 = (ECPublicKey) pair.getPublic();
            System.out.println("======== Generated public key:  " + Base64.toBase64String(publicKey1.getEncoded()));
            System.out.println("======== Generated private key: " + Base64.toBase64String(privateKey1.getEncoded()));
            String plaintext = "test";
            System.out.println("======== Encrypting message:    " + plaintext);
            String encryptedPlaintext = textEncrypt(plaintext, Base64.toBase64String(publicKey1.getEncoded()));

            System.out.println("======== Ciphertext:            " + encryptedPlaintext);
            System.out.println("======== Decrypting message:    " + encryptedPlaintext);
            String decryptedCiphertext = textDecrypt(encryptedPlaintext, Base64.toBase64String(privateKey1.getEncoded()));
            System.out.println("======== Plaintext:             " + decryptedCiphertext);
            splitKey(Base64.toBase64String(privateKey1.getEncoded()));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public String[] generateKeyPairs(){
        String keys[] = {"",""};
        try {
            ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
            KeyPairGenerator g = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
            g.initialize(ecSpec, new SecureRandom());
            KeyPair pair = g.generateKeyPair();
            ECPrivateKey privateKey = (ECPrivateKey) pair.getPrivate();
            ECPublicKey publicKey = (ECPublicKey) pair.getPublic();
            keys[0] = Base64.toBase64String(publicKey.getEncoded());
            keys[1] = Base64.toBase64String(privateKey.getEncoded());
        }catch (Exception e){
            e.printStackTrace();
        }
        return keys;
    }
    public Map<Integer,byte[]> splitKey(String key) {
        final byte[] secret = key.getBytes(StandardCharsets.UTF_8);
        final Map<Integer, byte[]> parts = scheme.split(secret);
        return  parts;
    }
    public String joinKey(Map<Integer,byte[]> parts){
        return new String(scheme.join(parts),StandardCharsets.UTF_8);
    }

    public void printParts(Map<Integer,byte[]> parts){
        for (Map.Entry<Integer, byte[]> entry : parts.entrySet()) {
            System.out.println("Key = " + entry.getKey() +
                    ", Value = " + Base64.toBase64String(entry.getValue()));
        }
    }

    public String textEncrypt(String plaintext, String peerPublicKey) throws Exception {

        byte[] publicKey = Base64.decode(peerPublicKey);
        return encrypt(plaintext, publicKey);

    }

    public String textDecrypt(String ciphertext, String ownPrivateKey) throws Exception {

        byte[] privateKey = Base64.decode(ownPrivateKey);
        return decrypt(ciphertext, privateKey);

    }

    private String encrypt(String plaintext, byte[] publicKeyBytes) throws Exception {

        java.security.spec.X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        org.bouncycastle.jce.interfaces.ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(encodedKeySpec);
        byte[] inputBytes = plaintext.getBytes();

        org.bouncycastle.jce.spec.IESParameterSpec params = new IESParameterSpec(null, null, 256, 256, null);
        IESCipher cipher = new IESCipher(
                new IESEngineGCM(
                        new ECDHBasicAgreement(),
                        new KDF2BytesGenerator(new SHA256Digest()),
                        new GCMBlockCipher()), 16);

        cipher.engineInit(Cipher.ENCRYPT_MODE, publicKey, params, new SecureRandom());

        byte[] cipherResult = cipher.engineDoFinal(inputBytes, 0, inputBytes.length);
        return Base64.toBase64String(cipherResult);
    }

    private String encryptDec(String plaintext, byte[] publicKeyBytes, String curveName) throws Exception {

        org.bouncycastle.jce.spec.ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(curveName);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        org.bouncycastle.jce.spec.ECNamedCurveSpec curvedParams = new ECNamedCurveSpec(curveName, spec.getCurve(), spec.getG(), spec.getN());
        java.security.spec.ECPoint point = org.bouncycastle.jce.ECPointUtil.decodePoint(curvedParams.getCurve(), publicKeyBytes);
        java.security.spec.ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, curvedParams);
        org.bouncycastle.jce.interfaces.ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(pubKeySpec);
        System.out.println("public Key 2" + Base64.toBase64String(publicKey.getEncoded()));

        byte[] inputBytes = plaintext.getBytes();

        org.bouncycastle.jce.spec.IESParameterSpec params = new IESParameterSpec(null, null, 256, 256, null);
        IESCipher cipher = new IESCipher(
                new IESEngineGCM(
                        new ECDHBasicAgreement(),
                        new KDF2BytesGenerator(new SHA256Digest()),
                        new GCMBlockCipher()), 16);

        cipher.engineInit(Cipher.ENCRYPT_MODE, publicKey, params, new SecureRandom());

        byte[] cipherResult = cipher.engineDoFinal(inputBytes, 0, inputBytes.length);
        return Base64.toBase64String(cipherResult);
    }


    private String decrypt(String ciphertext, byte[] privateKeyBytes) throws Exception {

        java.security.spec.PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        org.bouncycastle.jce.interfaces.ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(encodedKeySpec);

        byte[] inputBytes = Base64.decode(ciphertext);

        IESParameterSpec params = new IESParameterSpec(null, null, 256, 256, null);
        IESCipher cipher = new IESCipher(
                new IESEngineGCM(
                        new ECDHBasicAgreement(),
                        new KDF2BytesGenerator(new SHA256Digest()),
                        new GCMBlockCipher()), 16);

        cipher.engineInit(Cipher.DECRYPT_MODE, privateKey, params, new SecureRandom());

        byte[] cipherResult = cipher.engineDoFinal(inputBytes, 0, inputBytes.length);
        return new String(cipherResult);

    }
}
