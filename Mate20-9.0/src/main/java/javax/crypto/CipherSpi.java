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
    /* access modifiers changed from: protected */
    public abstract int engineDoFinal(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException;

    /* access modifiers changed from: protected */
    public abstract byte[] engineDoFinal(byte[] bArr, int i, int i2) throws IllegalBlockSizeException, BadPaddingException;

    /* access modifiers changed from: protected */
    public abstract int engineGetBlockSize();

    /* access modifiers changed from: protected */
    public abstract byte[] engineGetIV();

    /* access modifiers changed from: protected */
    public abstract int engineGetOutputSize(int i);

    /* access modifiers changed from: protected */
    public abstract AlgorithmParameters engineGetParameters();

    /* access modifiers changed from: protected */
    public abstract void engineInit(int i, Key key, AlgorithmParameters algorithmParameters, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException;

    /* access modifiers changed from: protected */
    public abstract void engineInit(int i, Key key, SecureRandom secureRandom) throws InvalidKeyException;

    /* access modifiers changed from: protected */
    public abstract void engineInit(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException;

    /* access modifiers changed from: protected */
    public abstract void engineSetMode(String str) throws NoSuchAlgorithmException;

    /* access modifiers changed from: protected */
    public abstract void engineSetPadding(String str) throws NoSuchPaddingException;

    /* access modifiers changed from: protected */
    public abstract int engineUpdate(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException;

    /* access modifiers changed from: protected */
    public abstract byte[] engineUpdate(byte[] bArr, int i, int i2);

    /* access modifiers changed from: protected */
    public int engineUpdate(ByteBuffer input, ByteBuffer output) throws ShortBufferException {
        try {
            return bufferCrypt(input, output, true);
        } catch (IllegalBlockSizeException e) {
            throw new ProviderException("Internal error in update()");
        } catch (BadPaddingException e2) {
            throw new ProviderException("Internal error in update()");
        }
    }

    /* access modifiers changed from: protected */
    public int engineDoFinal(ByteBuffer input, ByteBuffer output) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        return bufferCrypt(input, output, false);
    }

    static int getTempArraySize(int totalSize) {
        return Math.min(4096, totalSize);
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00d3 A[LOOP:0: B:21:0x008a->B:34:0x00d3, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0162  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x016e  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x017d  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0194 A[LOOP:1: B:39:0x00fb->B:79:0x0194, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x00cd A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x018e A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0198 A[SYNTHETIC] */
    private int bufferCrypt(ByteBuffer input, ByteBuffer output, boolean isUpdate) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        byte[] inArray;
        int inOfs;
        int inOfs2;
        byte[] inArray2;
        int inLen;
        int chunk;
        byte[] outArray;
        int chunk2;
        int n;
        byte[] outArray2;
        byte[] inArray3;
        int inLen2;
        int chunk3;
        int n2;
        int n3;
        ByteBuffer byteBuffer = input;
        ByteBuffer byteBuffer2 = output;
        if (byteBuffer == null || byteBuffer2 == null) {
            throw new NullPointerException("Input and output buffers must not be null");
        }
        int inPos = input.position();
        int inLimit = input.limit();
        int inLen3 = inLimit - inPos;
        if (isUpdate && inLen3 == 0) {
            return 0;
        }
        int outLenNeeded = engineGetOutputSize(inLen3);
        if (output.remaining() >= outLenNeeded) {
            boolean a1 = input.hasArray();
            boolean a2 = output.hasArray();
            if (a1 && a2) {
                byte[] inArray4 = input.array();
                int inOfs3 = input.arrayOffset() + inPos;
                byte[] outArray3 = output.array();
                int outPos = output.position();
                int outOfs = output.arrayOffset() + outPos;
                if (isUpdate) {
                    n3 = engineUpdate(inArray4, inOfs3, inLen3, outArray3, outOfs);
                } else {
                    n3 = engineDoFinal(inArray4, inOfs3, inLen3, outArray3, outOfs);
                }
                byteBuffer.position(inLimit);
                byteBuffer2.position(outPos + n3);
                return n3;
            } else if (a1 || !a2) {
                if (a1) {
                    byte[] inArray5 = input.array();
                    inOfs = input.arrayOffset() + inPos;
                    inArray = inArray5;
                } else {
                    inArray = new byte[getTempArraySize(inLen3)];
                    inOfs = 0;
                }
                int chunk4 = inOfs;
                byte[] outArray4 = new byte[getTempArraySize(outLenNeeded)];
                int inLen4 = inLen3;
                int outSize = outArray4.length;
                int total = 0;
                byte[] outArray5 = null;
                byte[] outArray6 = outArray4;
                while (true) {
                    byte[] resized = outArray5;
                    int chunk5 = Math.min(inLen4, outSize == 0 ? inArray.length : outSize);
                    if (a1 || resized != null || chunk5 <= 0) {
                        inOfs2 = chunk4;
                    } else {
                        byteBuffer.get(inArray, 0, chunk5);
                        inOfs2 = 0;
                    }
                    if (isUpdate) {
                        chunk2 = chunk5;
                        outArray = outArray6;
                        inLen = inLen4;
                        inArray2 = inArray;
                    } else if (inLen4 != chunk5) {
                        chunk2 = chunk5;
                        outArray = outArray6;
                        inLen = inLen4;
                        inArray2 = inArray;
                    } else {
                        chunk2 = chunk5;
                        outArray = outArray6;
                        inLen = inLen4;
                        inArray2 = inArray;
                        try {
                            n = engineDoFinal(inArray, inOfs2, chunk2, outArray, 0);
                            resized = null;
                            chunk = chunk2;
                            inOfs2 += chunk;
                            inLen4 = inLen - chunk;
                            if (n <= 0) {
                                outArray2 = outArray;
                                try {
                                    byteBuffer2.put(outArray2, 0, n);
                                    total += n;
                                } catch (ShortBufferException e) {
                                    e = e;
                                    inLen = inLen4;
                                    if (resized == null) {
                                    }
                                }
                            } else {
                                outArray2 = outArray;
                            }
                            outArray6 = outArray2;
                            outArray5 = null;
                            chunk4 = inOfs2;
                        } catch (ShortBufferException e2) {
                            e = e2;
                            chunk = chunk2;
                            byte[] bArr = outArray;
                            if (resized == null) {
                            }
                        }
                        if (inLen4 <= 0) {
                            if (a1) {
                                byteBuffer.position(inLimit);
                            }
                            return total;
                        }
                        inArray = inArray2;
                    }
                    try {
                        n = engineUpdate(inArray2, inOfs2, chunk2, outArray, 0);
                        resized = null;
                        chunk = chunk2;
                        inOfs2 += chunk;
                        inLen4 = inLen - chunk;
                        if (n <= 0) {
                        }
                        outArray6 = outArray2;
                        outArray5 = null;
                        chunk4 = inOfs2;
                    } catch (ShortBufferException e3) {
                        e = e3;
                        chunk = chunk2;
                        byte[] bArr2 = outArray;
                        if (resized == null) {
                            int outSize2 = engineGetOutputSize(chunk);
                            outSize = outSize2;
                            chunk4 = inOfs2;
                            inLen4 = inLen;
                            outArray6 = new byte[outSize2];
                            outArray5 = 1;
                            if (inLen4 <= 0) {
                            }
                        } else {
                            throw ((ProviderException) new ProviderException("Could not determine buffer size").initCause(e));
                        }
                    }
                    if (inLen4 <= 0) {
                    }
                }
            } else {
                int outPos2 = output.position();
                byte[] outArray7 = output.array();
                byte[] inArray6 = new byte[getTempArraySize(inLen3)];
                int inLen5 = inLen3;
                int outOfs2 = output.arrayOffset() + outPos2;
                int total2 = 0;
                while (true) {
                    int chunk6 = Math.min(inLen5, inArray6.length);
                    if (chunk6 > 0) {
                        byteBuffer.get(inArray6, 0, chunk6);
                    }
                    if (isUpdate) {
                        chunk3 = chunk6;
                        inLen2 = inLen5;
                        inArray3 = inArray6;
                    } else if (inLen5 != chunk6) {
                        chunk3 = chunk6;
                        inLen2 = inLen5;
                        inArray3 = inArray6;
                    } else {
                        chunk3 = chunk6;
                        inLen2 = inLen5;
                        inArray3 = inArray6;
                        n2 = engineDoFinal(inArray6, 0, chunk6, outArray7, outOfs2);
                        total2 += n2;
                        outOfs2 += n2;
                        inLen5 = inLen2 - chunk3;
                        if (inLen5 > 0) {
                            byteBuffer2.position(outPos2 + total2);
                            return total2;
                        }
                        inArray6 = inArray3;
                    }
                    n2 = engineUpdate(inArray3, 0, chunk3, outArray7, outOfs2);
                    total2 += n2;
                    outOfs2 += n2;
                    inLen5 = inLen2 - chunk3;
                    if (inLen5 > 0) {
                    }
                }
            }
        } else {
            throw new ShortBufferException("Need at least " + outLenNeeded + " bytes of space in output buffer");
        }
    }

    /* access modifiers changed from: protected */
    public byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException, NoSuchAlgorithmException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public int engineGetKeySize(Key key) throws InvalidKeyException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public void engineUpdateAAD(byte[] src, int offset, int len) {
        throw new UnsupportedOperationException("The underlying Cipher implementation does not support this method");
    }

    /* access modifiers changed from: protected */
    public void engineUpdateAAD(ByteBuffer src) {
        throw new UnsupportedOperationException("The underlying Cipher implementation does not support this method");
    }
}
