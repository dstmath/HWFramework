package android.media;

import android.media.MediaCodec.BufferInfo;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

public final class AmrInputStream extends InputStream {
    private static final int SAMPLES_PER_FRAME = 160;
    private static final String TAG = "AmrInputStream";
    private final byte[] mBuf = new byte[320];
    private int mBufIn = 0;
    private int mBufOut = 0;
    MediaCodec mCodec;
    BufferInfo mInfo;
    private InputStream mInputStream;
    private byte[] mOneByte = new byte[1];
    boolean mSawInputEOS;
    boolean mSawOutputEOS;

    public AmrInputStream(InputStream inputStream) {
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
                this.mCodec.configure(format, null, null, 1);
                this.mCodec.start();
            } catch (IOException e) {
                if (this.mCodec != null) {
                    this.mCodec.release();
                }
                this.mCodec = null;
            }
        }
        this.mInfo = new BufferInfo();
    }

    public int read() throws IOException {
        return read(this.mOneByte, 0, 1) == 1 ? this.mOneByte[0] & 255 : -1;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int offset, int length) throws IOException {
        if (this.mCodec == null) {
            throw new IllegalStateException("not open");
        }
        if (this.mBufOut >= this.mBufIn && (this.mSawOutputEOS ^ 1) != 0) {
            int index;
            this.mBufOut = 0;
            this.mBufIn = 0;
            while (!this.mSawInputEOS) {
                index = this.mCodec.dequeueInputBuffer(0);
                if (index < 0) {
                    break;
                }
                int numRead = 0;
                while (numRead < 320) {
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
            do {
                index = this.mCodec.dequeueOutputBuffer(this.mInfo, -1);
            } while (index < 0);
            this.mBufIn = this.mInfo.size;
            this.mCodec.getOutputBuffer(index).get(this.mBuf, 0, this.mBufIn);
            this.mCodec.releaseOutputBuffer(index, false);
            if ((this.mInfo.flags & 4) != 0) {
                this.mSawOutputEOS = true;
            }
        }
        if (this.mBufOut < this.mBufIn) {
            if (length > this.mBufIn - this.mBufOut) {
                length = this.mBufIn - this.mBufOut;
            }
            System.arraycopy(this.mBuf, this.mBufOut, b, offset, length);
            this.mBufOut += length;
            return length;
        } else if (this.mSawInputEOS && this.mSawOutputEOS) {
            return -1;
        } else {
            return 0;
        }
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
                this.mCodec = null;
            } catch (Throwable th) {
                this.mCodec = null;
            }
        } catch (Throwable th2) {
            this.mCodec = null;
        }
    }

    protected void finalize() throws Throwable {
        if (this.mCodec != null) {
            Log.w(TAG, "AmrInputStream wasn't closed");
            this.mCodec.release();
        }
    }
}
