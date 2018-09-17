package java.security;

import java.nio.ByteBuffer;
import sun.security.jca.JCAUtil;

public abstract class MessageDigestSpi {
    private byte[] tempArray;

    protected abstract byte[] engineDigest();

    protected abstract void engineReset();

    protected abstract void engineUpdate(byte b);

    protected abstract void engineUpdate(byte[] bArr, int i, int i2);

    protected int engineGetDigestLength() {
        return 0;
    }

    protected void engineUpdate(ByteBuffer input) {
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
                int n = JCAUtil.getTempArraySize(len);
                if (this.tempArray == null || n > this.tempArray.length) {
                    this.tempArray = new byte[n];
                }
                while (len > 0) {
                    int chunk = Math.min(len, this.tempArray.length);
                    input.get(this.tempArray, 0, chunk);
                    engineUpdate(this.tempArray, 0, chunk);
                    len -= chunk;
                }
            }
        }
    }

    protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
        byte[] digest = engineDigest();
        if (len < digest.length) {
            throw new DigestException("partial digests not returned");
        } else if (buf.length - offset < digest.length) {
            throw new DigestException("insufficient space in the output buffer to store the digest");
        } else {
            System.arraycopy(digest, 0, buf, offset, digest.length);
            return digest.length;
        }
    }

    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }
}
