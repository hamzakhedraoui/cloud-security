import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class GCMBlockCipher extends BufferedBlockCipher {

    private org.bouncycastle.crypto.modes.GCMBlockCipher internalCipher;


    public GCMBlockCipher()
            throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
        this.internalCipher = new org.bouncycastle.crypto.modes.GCMBlockCipher(new AESEngine());
    }


    @Override
    public void init(boolean forEncryption, CipherParameters params) {
        internalCipher.init(forEncryption, params);
    }


    @Override
    public int getOutputSize(int len) {
        return internalCipher.getOutputSize(len);
    }


    @Override
    public int doFinal(byte[] out, int outOff) throws InvalidCipherTextException {
        return internalCipher.doFinal(out, outOff);
    }


    @Override
    public int processBytes(byte[] in, int inOff, int len, byte[] out, int outOff) {
        return internalCipher.processBytes(in, inOff, len, out, outOff);
    }
}