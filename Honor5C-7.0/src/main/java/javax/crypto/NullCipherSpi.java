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

    protected int engineGetBlockSize() {
        return 1;
    }

    protected int engineGetOutputSize(int inputLen) {
        return inputLen;
    }

    protected byte[] engineGetIV() {
        return new byte[8];
    }

    protected AlgorithmParameters engineGetParameters() {
        return null;
    }

    protected void engineInit(int mode, Key key, SecureRandom random) {
    }

    protected void engineInit(int mode, Key key, AlgorithmParameterSpec params, SecureRandom random) {
    }

    protected void engineInit(int mode, Key key, AlgorithmParameters params, SecureRandom random) {
    }

    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        if (input == null) {
            return null;
        }
        byte[] x = new byte[inputLen];
        System.arraycopy(input, inputOffset, x, 0, inputLen);
        return x;
    }

    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) {
        if (input == null) {
            return 0;
        }
        System.arraycopy(input, inputOffset, output, outputOffset, inputLen);
        return inputLen;
    }

    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) {
        return engineUpdate(input, inputOffset, inputLen);
    }

    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) {
        return engineUpdate(input, inputOffset, inputLen, output, outputOffset);
    }

    protected int engineGetKeySize(Key key) {
        return 0;
    }
}
