package javax.crypto;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

public abstract class MacSpi {
    /* access modifiers changed from: protected */
    public abstract byte[] engineDoFinal();

    /* access modifiers changed from: protected */
    public abstract int engineGetMacLength();

    /* access modifiers changed from: protected */
    public abstract void engineInit(Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException;

    /* access modifiers changed from: protected */
    public abstract void engineReset();

    /* access modifiers changed from: protected */
    public abstract void engineUpdate(byte b);

    /* access modifiers changed from: protected */
    public abstract void engineUpdate(byte[] bArr, int i, int i2);

    /* access modifiers changed from: protected */
    public void engineUpdate(ByteBuffer input) {
        if (input.hasRemaining()) {
            if (input.hasArray()) {
                byte[] b = input.array();
                int ofs = input.arrayOffset();
                int pos = input.position();
                int lim = input.limit();
                engineUpdate(b, ofs + pos, lim - pos);
                input.position(lim);
            } else {
                int len = input.remaining();
                byte[] b2 = new byte[CipherSpi.getTempArraySize(len)];
                while (len > 0) {
                    int chunk = Math.min(len, b2.length);
                    input.get(b2, 0, chunk);
                    engineUpdate(b2, 0, chunk);
                    len -= chunk;
                }
            }
        }
    }

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }
}
