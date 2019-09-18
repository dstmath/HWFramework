package java.security;

import java.nio.ByteBuffer;
import java.security.spec.AlgorithmParameterSpec;
import sun.security.jca.JCAUtil;

public abstract class SignatureSpi {
    protected SecureRandom appRandom = null;

    /* access modifiers changed from: protected */
    @Deprecated
    public abstract Object engineGetParameter(String str) throws InvalidParameterException;

    /* access modifiers changed from: protected */
    public abstract void engineInitSign(PrivateKey privateKey) throws InvalidKeyException;

    /* access modifiers changed from: protected */
    public abstract void engineInitVerify(PublicKey publicKey) throws InvalidKeyException;

    /* access modifiers changed from: protected */
    @Deprecated
    public abstract void engineSetParameter(String str, Object obj) throws InvalidParameterException;

    /* access modifiers changed from: protected */
    public abstract byte[] engineSign() throws SignatureException;

    /* access modifiers changed from: protected */
    public abstract void engineUpdate(byte b) throws SignatureException;

    /* access modifiers changed from: protected */
    public abstract void engineUpdate(byte[] bArr, int i, int i2) throws SignatureException;

    /* access modifiers changed from: protected */
    public abstract boolean engineVerify(byte[] bArr) throws SignatureException;

    /* access modifiers changed from: protected */
    public void engineInitSign(PrivateKey privateKey, SecureRandom random) throws InvalidKeyException {
        this.appRandom = random;
        engineInitSign(privateKey);
    }

    /* access modifiers changed from: protected */
    public void engineUpdate(ByteBuffer input) {
        if (input.hasRemaining()) {
            try {
                if (input.hasArray()) {
                    byte[] b = input.array();
                    int ofs = input.arrayOffset();
                    int pos = input.position();
                    int lim = input.limit();
                    engineUpdate(b, ofs + pos, lim - pos);
                    input.position(lim);
                } else {
                    int len = input.remaining();
                    byte[] b2 = new byte[JCAUtil.getTempArraySize(len)];
                    while (len > 0) {
                        int chunk = Math.min(len, b2.length);
                        input.get(b2, 0, chunk);
                        engineUpdate(b2, 0, chunk);
                        len -= chunk;
                    }
                }
            } catch (SignatureException e) {
                throw new ProviderException("update() failed", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int engineSign(byte[] outbuf, int offset, int len) throws SignatureException {
        byte[] sig = engineSign();
        if (len < sig.length) {
            throw new SignatureException("partial signatures not returned");
        } else if (outbuf.length - offset >= sig.length) {
            System.arraycopy(sig, 0, outbuf, offset, sig.length);
            return sig.length;
        } else {
            throw new SignatureException("insufficient space in the output buffer to store the signature");
        }
    }

    /* access modifiers changed from: protected */
    public boolean engineVerify(byte[] sigBytes, int offset, int length) throws SignatureException {
        byte[] sigBytesCopy = new byte[length];
        System.arraycopy(sigBytes, offset, sigBytesCopy, 0, length);
        return engineVerify(sigBytesCopy);
    }

    /* access modifiers changed from: protected */
    public void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public AlgorithmParameters engineGetParameters() {
        throw new UnsupportedOperationException();
    }

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }
}
