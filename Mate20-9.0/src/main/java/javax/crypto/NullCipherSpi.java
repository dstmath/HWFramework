package javax.crypto;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

final class NullCipherSpi extends CipherSpi {
    protected NullCipherSpi() {
    }

    public void engineSetMode(String mode) {
    }

    public void engineSetPadding(String padding) {
    }

    /* access modifiers changed from: protected */
    public int engineGetBlockSize() {
        return 1;
    }

    /* access modifiers changed from: protected */
    public int engineGetOutputSize(int inputLen) {
        return inputLen;
    }

    /* access modifiers changed from: protected */
    public byte[] engineGetIV() {
        return new byte[8];
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameters engineGetParameters() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void engineInit(int mode, Key key, SecureRandom random) {
    }

    /* access modifiers changed from: protected */
    public void engineInit(int mode, Key key, AlgorithmParameterSpec params, SecureRandom random) {
    }

    /* access modifiers changed from: protected */
    public void engineInit(int mode, Key key, AlgorithmParameters params, SecureRandom random) {
    }

    /* access modifiers changed from: protected */
    public byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        if (input == null) {
            return null;
        }
        byte[] x = new byte[inputLen];
        System.arraycopy(input, inputOffset, x, 0, inputLen);
        return x;
    }

    /* access modifiers changed from: protected */
    public int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) {
        if (input == null) {
            return 0;
        }
        System.arraycopy(input, inputOffset, output, outputOffset, inputLen);
        return inputLen;
    }

    /* access modifiers changed from: protected */
    public byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) {
        return engineUpdate(input, inputOffset, inputLen);
    }

    /* access modifiers changed from: protected */
    public int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) {
        return engineUpdate(input, inputOffset, inputLen, output, outputOffset);
    }

    /* access modifiers changed from: protected */
    public int engineGetKeySize(Key key) {
        return 0;
    }
}
