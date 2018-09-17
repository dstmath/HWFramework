package android.media;

import java.io.IOException;
import java.io.InputStream;

public final class ResampleInputStream extends InputStream {
    private static final String TAG = "ResampleInputStream";
    private static final int mFirLength = 29;
    private byte[] mBuf;
    private int mBufCount;
    private InputStream mInputStream;
    private final byte[] mOneByte = new byte[1];
    private final int mRateIn;
    private final int mRateOut;

    private static native void fir21(byte[] bArr, int i, byte[] bArr2, int i2, int i3);

    static {
        System.loadLibrary("media_jni");
    }

    public ResampleInputStream(InputStream inputStream, int rateIn, int rateOut) {
        if (rateIn != rateOut * 2) {
            throw new IllegalArgumentException("only support 2:1 at the moment");
        }
        this.mInputStream = inputStream;
        this.mRateIn = 2;
        this.mRateOut = 1;
    }

    public int read() throws IOException {
        return read(this.mOneByte, 0, 1) == 1 ? this.mOneByte[0] & 255 : -1;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int offset, int length) throws IOException {
        if (this.mInputStream == null) {
            throw new IllegalStateException("not open");
        }
        int nIn = ((((length / 2) * this.mRateIn) / this.mRateOut) + 29) * 2;
        if (this.mBuf == null) {
            this.mBuf = new byte[nIn];
        } else if (nIn > this.mBuf.length) {
            byte[] bf = new byte[nIn];
            System.arraycopy(this.mBuf, 0, bf, 0, this.mBufCount);
            this.mBuf = bf;
        }
        while (true) {
            int len = ((((this.mBufCount / 2) - 29) * this.mRateOut) / this.mRateIn) * 2;
            if (len > 0) {
                length = len < length ? len : (length / 2) * 2;
                fir21(this.mBuf, 0, b, offset, length / 2);
                int nFwd = (this.mRateIn * length) / this.mRateOut;
                this.mBufCount -= nFwd;
                if (this.mBufCount > 0) {
                    System.arraycopy(this.mBuf, nFwd, this.mBuf, 0, this.mBufCount);
                }
                return length;
            }
            int n = this.mInputStream.read(this.mBuf, this.mBufCount, this.mBuf.length - this.mBufCount);
            if (n == -1) {
                return -1;
            }
            this.mBufCount += n;
        }
    }

    public void close() throws IOException {
        try {
            if (this.mInputStream != null) {
                this.mInputStream.close();
            }
            this.mInputStream = null;
        } catch (Throwable th) {
            this.mInputStream = null;
        }
    }

    protected void finalize() throws Throwable {
        if (this.mInputStream != null) {
            close();
            throw new IllegalStateException("someone forgot to close ResampleInputStream");
        }
    }
}
