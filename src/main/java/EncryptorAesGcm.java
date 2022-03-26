import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class EncryptorAesGcm {
    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";

    private static final int TAG_LENGTH_BIT = 128; // must be one of {128, 120, 112, 104, 96}
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16;
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    // AES-GCM needs GCMParameterSpec
    public static byte[] encrypt(byte[] pText, SecretKey secret, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        byte[] encryptedText = cipher.doFinal(pText);
        //return new String(encryptedText,UTF_8);
        return encryptedText;
    }

    // prefix IV length + IV bytes to cipher text
    public static byte[] encryptWithPrefixIV(byte[] pText, SecretKey secret, byte[] iv) throws Exception {

        byte[] cipherText = encrypt(pText, secret, iv);

        byte[] cipherTextWithIv = ByteBuffer.allocate(iv.length + cipherText.length)
                .put(iv)
                .put(cipherText)
                .array();
        return cipherTextWithIv;

    }

    public static String decrypt(byte[] cText, SecretKey secret, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        byte[] plainText = cipher.doFinal(cText);
        return new String(plainText, UTF_8);

    }

    public static String decryptWithPrefixIV(byte[] cText, SecretKey secret) throws Exception {

        ByteBuffer bb = ByteBuffer.wrap(cText);

        byte[] iv = new byte[IV_LENGTH_BYTE];
        bb.get(iv);
        //bb.get(iv, 0, iv.length);

        byte[] cipherText = new byte[bb.remaining()];
        bb.get(cipherText);

        String plainText = decrypt(cipherText, secret, iv);
        return plainText;

    }

    ////// generate AES Key with password
    // return a base64 encoded AES encrypted text
    public static byte[] encryptWithPassword(byte[] pText, String password) throws Exception {
        // 16 bytes salt
        byte[] salt = AES.getRandomNonce(SALT_LENGTH_BYTE);
        // GCM recommended 12 bytes iv?
        byte[] iv = AES.getRandomNonce(IV_LENGTH_BYTE);
        // secret key from password
        SecretKey aesKeyFromPassword = AES.getAESKeyFromPassword(password.toCharArray(), salt);
        //System.out.println("secretKey : ");
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        // ASE-GCM needs GCMParameterSpec
        cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        byte[] cipherText = cipher.doFinal(pText);
        // prefix IV and Salt to cipher text
        byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + cipherText.length)
                .put(iv)
                .put(salt)
                .put(cipherText)
                .array();
        // string representation, base64, send this string to other for decryption.
        //return Base64.getEncoder().encodeToString(cipherTextWithIvSalt);
        return cipherTextWithIvSalt;
    }

    // we need the same password, salt and iv to decrypt it
    public static byte[] decryptWithPassord(byte[] cText, String password) throws Exception {
        //byte[] decode = Base64.getDecoder().decode(cText.getBytes(UTF_8));
        // get back the iv and salt from the cipher textdecode
        ByteBuffer bb = ByteBuffer.wrap(cText);
        byte[] iv = new byte[IV_LENGTH_BYTE];
        bb.get(iv);
        byte[] salt = new byte[SALT_LENGTH_BYTE];
        bb.get(salt);
        byte[] cipherText = new byte[bb.remaining()];
        bb.get(cipherText);
        // get back the aes key from the same password and salt
        SecretKey aesKeyFromPassword = AES.getAESKeyFromPassword(password.toCharArray(), salt);
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        byte[] plainText = cipher.doFinal(cipherText);
        //return new String(plainText, UTF_8);
        return plainText;
    }
    public static byte[] encryptFile(String fromFile, String toFile,String secretKey) throws Exception {
        // read a normal txt file
        //byte[] fileContent = Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(fromFile).toURI()));
        byte[] fileContent = Files.readAllBytes(Paths.get(fromFile));
        // encrypt with a password
        SecretKey key = AES.convertStringToSecretKeyto(secretKey);
        byte[] encryptedText = EncryptorAesGcm.encryptWithPrefixIV(fileContent,key,AES.getRandomNonce(12));
        // save a file
        File file = new File(toFile);
        file.createNewFile();
        Path path = Paths.get(toFile);

        Files.write(path, encryptedText);
        //return the hash sha3-256 of the encrypted file
        return ShaUtils.digest(encryptedText);
    }

    public static void decryptFile(String fromEncryptedFile,String toFile, String secretKey) throws Exception {
        // read a file
        byte[] fileContent = Files.readAllBytes(Paths.get(fromEncryptedFile));
        SecretKey key = AES.convertStringToSecretKeyto(secretKey);
        byte[] decryptedFile = EncryptorAesGcm.decryptWithPrefixIV(fileContent, key).getBytes(StandardCharsets.UTF_8);
        File file = new File(toFile);
        file.createNewFile();
        Path path = Paths.get(toFile);
        Files.write(path, decryptedFile);
    }
}
