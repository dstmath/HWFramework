package javax.crypto;

import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public abstract class CipherSpi {
    protected abstract int engineDoFinal(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException;

    protected abstract byte[] engineDoFinal(byte[] bArr, int i, int i2) throws IllegalBlockSizeException, BadPaddingException;

    protected abstract int engineGetBlockSize();

    protected abstract byte[] engineGetIV();

    protected abstract int engineGetOutputSize(int i);

    protected abstract AlgorithmParameters engineGetParameters();

    protected abstract void engineInit(int i, Key key, AlgorithmParameters algorithmParameters, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException;

    protected abstract void engineInit(int i, Key key, SecureRandom secureRandom) throws InvalidKeyException;

    protected abstract void engineInit(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException;

    protected abstract void engineSetMode(String str) throws NoSuchAlgorithmException;

    protected abstract void engineSetPadding(String str) throws NoSuchPaddingException;

    protected abstract int engineUpdate(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException;

    protected abstract byte[] engineUpdate(byte[] bArr, int i, int i2);

    protected int engineUpdate(ByteBuffer input, ByteBuffer output) throws ShortBufferException {
        try {
            return bufferCrypt(input, output, true);
        } catch (IllegalBlockSizeException e) {
            throw new ProviderException("Internal error in update()");
        } catch (BadPaddingException e2) {
            throw new ProviderException("Internal error in update()");
        }
    }

    protected int engineDoFinal(ByteBuffer input, ByteBuffer output) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        return bufferCrypt(input, output, false);
    }

    static int getTempArraySize(int totalSize) {
        return Math.min(4096, totalSize);
    }

    private int bufferCrypt(ByteBuffer input, ByteBuffer output, boolean isUpdate) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        if (input == null || output == null) {
            throw new NullPointerException("Input and output buffers must not be null");
        }
        int inPos = input.position();
        int inLimit = input.limit();
        int inLen = inLimit - inPos;
        if (isUpdate && inLen == 0) {
            return 0;
        }
        int outLenNeeded = engineGetOutputSize(inLen);
        if (output.remaining() < outLenNeeded) {
            throw new ShortBufferException("Need at least " + outLenNeeded + " bytes of space in output buffer");
        }
        boolean a1 = input.hasArray();
        boolean a2 = output.hasArray();
        byte[] inArray;
        int inOfs;
        byte[] outArray;
        int outPos;
        int outOfs;
        int n;
        int total;
        int chunk;
        if (a1 && a2) {
            inArray = input.array();
            inOfs = input.arrayOffset() + inPos;
            outArray = output.array();
            outPos = output.position();
            outOfs = output.arrayOffset() + outPos;
            if (isUpdate) {
                n = engineUpdate(inArray, inOfs, inLen, outArray, outOfs);
            } else {
                n = engineDoFinal(inArray, inOfs, inLen, outArray, outOfs);
            }
            input.position(inLimit);
            output.position(outPos + n);
            return n;
        } else if (a1 || !a2) {
            if (a1) {
                inArray = input.array();
                inOfs = input.arrayOffset() + inPos;
            } else {
                inArray = new byte[getTempArraySize(inLen)];
                inOfs = 0;
            }
            outArray = new byte[getTempArraySize(outLenNeeded)];
            int outSize = outArray.length;
            total = 0;
            boolean resized = false;
            do {
                int length;
                if (outSize == 0) {
                    length = inArray.length;
                } else {
                    length = outSize;
                }
                chunk = Math.min(inLen, length);
                if (!(a1 || (resized ^ 1) == 0 || chunk <= 0)) {
                    input.get(inArray, 0, chunk);
                    inOfs = 0;
                }
                if (isUpdate || inLen != chunk) {
                    try {
                        n = engineUpdate(inArray, inOfs, chunk, outArray, 0);
                    } catch (Throwable e) {
                        if (resized) {
                            throw ((ProviderException) new ProviderException("Could not determine buffer size").initCause(e));
                        }
                        resized = true;
                        outSize = engineGetOutputSize(chunk);
                        outArray = new byte[outSize];
                        continue;
                    }
                } else {
                    n = engineDoFinal(inArray, inOfs, chunk, outArray, 0);
                }
                resized = false;
                inOfs += chunk;
                inLen -= chunk;
                if (n > 0) {
                    output.put(outArray, 0, n);
                    total += n;
                    continue;
                } else {
                    continue;
                }
            } while (inLen > 0);
            if (a1) {
                input.position(inLimit);
            }
            return total;
        } else {
            outPos = output.position();
            outArray = output.array();
            outOfs = output.arrayOffset() + outPos;
            inArray = new byte[getTempArraySize(inLen)];
            total = 0;
            do {
                chunk = Math.min(inLen, inArray.length);
                if (chunk > 0) {
                    input.get(inArray, 0, chunk);
                }
                if (isUpdate || inLen != chunk) {
                    n = engineUpdate(inArray, 0, chunk, outArray, outOfs);
                } else {
                    n = engineDoFinal(inArray, 0, chunk, outArray, outOfs);
                }
                total += n;
                outOfs += n;
                inLen -= chunk;
            } while (inLen > 0);
            output.position(outPos + total);
            return total;
        }
    }

    protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        throw new UnsupportedOperationException();
    }

    protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        throw new UnsupportedOperationException();
    }

    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        throw new UnsupportedOperationException();
    }

    protected void engineUpdateAAD(byte[] src, int offset, int len) {
        throw new UnsupportedOperationException("The underlying Cipher implementation does not support this method");
    }

    protected void engineUpdateAAD(ByteBuffer src) {
        throw new UnsupportedOperationException("The underlying Cipher implementation does not support this method");
    }
}
