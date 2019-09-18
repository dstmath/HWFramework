package android.media;

import android.media.MediaCodec;
import android.util.Log;
import android.view.Surface;
import java.io.IOException;
import java.io.InputStream;

public final class AmrInputStream extends InputStream {
    private static final int SAMPLES_PER_FRAME = 160;
    private static final String TAG = "AmrInputStream";
    private final byte[] mBuf = new byte[320];
    private int mBufIn = 0;
    private int mBufOut = 0;
    MediaCodec mCodec;
    MediaCodec.BufferInfo mInfo;
    private InputStream mInputStream;
    private byte[] mOneByte = new byte[1];
    boolean mSawInputEOS;
    boolean mSawOutputEOS;

    public AmrInputStream(InputStream inputStream) {
        Log.w(TAG, "@@@@ AmrInputStream is not a public API @@@@");
        this.mInputStream = inputStream;
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AMR_NB);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 8000);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 12200);
        String name = new MediaCodecList(0).findEncoderForFormat(format);
        if (name != null) {
            try {
                this.mCodec = MediaCodec.createByCodecName(name);
                this.mCodec.configure(format, (Surface) null, (MediaCrypto) null, 1);
                this.mCodec.start();
            } catch (IOException e) {
                if (this.mCodec != null) {
                    this.mCodec.release();
                }
                this.mCodec = null;
            }
        }
        this.mInfo = new MediaCodec.BufferInfo();
    }

    public int read() throws IOException {
        if (read(this.mOneByte, 0, 1) == 1) {
            return 255 & this.mOneByte[0];
        }
        return -1;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int offset, int length) throws IOException {
        int length2;
        if (this.mCodec != null) {
            if (this.mBufOut >= this.mBufIn && !this.mSawOutputEOS) {
                this.mBufOut = 0;
                this.mBufIn = 0;
                while (!this.mSawInputEOS) {
                    int index = this.mCodec.dequeueInputBuffer(0);
                    if (index < 0) {
                        break;
                    }
                    int numRead = 0;
                    while (true) {
                        if (numRead >= 320) {
                            break;
                        }
                        int n = this.mInputStream.read(this.mBuf, numRead, 320 - numRead);
                        if (n == -1) {
                            this.mSawInputEOS = true;
                            break;
                        }
                        numRead += n;
                    }
                    this.mCodec.getInputBuffer(index).put(this.mBuf, 0, numRead);
                    this.mCodec.queueInputBuffer(index, 0, numRead, 0, this.mSawInputEOS ? 4 : 0);
                }
                int index2 = this.mCodec.dequeueOutputBuffer(this.mInfo, 0);
                if (index2 >= 0) {
                    this.mBufIn = this.mInfo.size;
                    this.mCodec.getOutputBuffer(index2).get(this.mBuf, 0, this.mBufIn);
                    this.mCodec.releaseOutputBuffer(index2, false);
                    if ((4 & this.mInfo.flags) != 0) {
                        this.mSawOutputEOS = true;
                    }
                }
            }
            if (this.mBufOut < this.mBufIn) {
                int i = length;
                if (i > this.mBufIn - this.mBufOut) {
                    length2 = this.mBufIn - this.mBufOut;
                } else {
                    length2 = i;
                }
                System.arraycopy(this.mBuf, this.mBufOut, b, offset, length2);
                this.mBufOut += length2;
                return length2;
            }
            byte[] bArr = b;
            int i2 = offset;
            int i3 = length;
            return (!this.mSawInputEOS || !this.mSawOutputEOS) ? 0 : -1;
        }
        byte[] bArr2 = b;
        int i4 = offset;
        int i5 = length;
        throw new IllegalStateException("not open");
    }

    public void close() throws IOException {
        try {
            if (this.mInputStream != null) {
                this.mInputStream.close();
            }
            this.mInputStream = null;
            try {
                if (this.mCodec != null) {
                    this.mCodec.release();
                }
            } finally {
                this.mCodec = null;
            }
        } catch (Throwable th) {
            this.mInputStream = null;
            if (this.mCodec != null) {
                this.mCodec.release();
            }
            throw th;
        } finally {
            this.mCodec = null;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        if (this.mCodec != null) {
            Log.w(TAG, "AmrInputStream wasn't closed");
            this.mCodec.release();
        }
    }
}
