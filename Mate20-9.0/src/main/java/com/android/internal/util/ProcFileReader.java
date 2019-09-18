package com.android.internal.util;

import com.android.internal.midi.MidiConstants;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;

public class ProcFileReader implements Closeable {
    private final byte[] mBuffer;
    private boolean mLineFinished;
    private final InputStream mStream;
    private int mTail;

    public ProcFileReader(InputStream stream) throws IOException {
        this(stream, 4096);
    }

    public ProcFileReader(InputStream stream, int bufferSize) throws IOException {
        this.mStream = stream;
        this.mBuffer = new byte[bufferSize];
        fillBuf();
    }

    private int fillBuf() throws IOException {
        int length = this.mBuffer.length - this.mTail;
        if (length != 0) {
            int read = this.mStream.read(this.mBuffer, this.mTail, length);
            if (read != -1) {
                this.mTail += read;
            }
            return read;
        }
        throw new IOException("attempting to fill already-full buffer");
    }

    private void consumeBuf(int count) throws IOException {
        System.arraycopy(this.mBuffer, count, this.mBuffer, 0, this.mTail - count);
        this.mTail -= count;
        if (this.mTail == 0) {
            fillBuf();
        }
    }

    private int nextTokenIndex() throws IOException {
        if (this.mLineFinished) {
            return -1;
        }
        int i = 0;
        while (true) {
            if (i < this.mTail) {
                byte b = this.mBuffer[i];
                if (b == 10) {
                    this.mLineFinished = true;
                    return i;
                } else if (b == 32) {
                    return i;
                } else {
                    i++;
                }
            } else if (fillBuf() <= 0) {
                throw new ProtocolException("End of stream while looking for token boundary");
            }
        }
    }

    public boolean hasMoreData() {
        return this.mTail > 0;
    }

    public void finishLine() throws IOException {
        int i = 0;
        if (this.mLineFinished) {
            this.mLineFinished = false;
            return;
        }
        while (true) {
            if (i < this.mTail) {
                if (this.mBuffer[i] == 10) {
                    consumeBuf(i + 1);
                    return;
                }
                i++;
            } else if (fillBuf() <= 0) {
                throw new ProtocolException("End of stream while looking for line boundary");
            }
        }
    }

    public String nextString() throws IOException {
        int tokenIndex = nextTokenIndex();
        if (tokenIndex != -1) {
            return parseAndConsumeString(tokenIndex);
        }
        throw new ProtocolException("Missing required string");
    }

    public long nextLong() throws IOException {
        int tokenIndex = nextTokenIndex();
        if (tokenIndex != -1) {
            return parseAndConsumeLong(tokenIndex);
        }
        throw new ProtocolException("Missing required long");
    }

    public long nextOptionalLong(long def) throws IOException {
        int tokenIndex = nextTokenIndex();
        if (tokenIndex == -1) {
            return def;
        }
        return parseAndConsumeLong(tokenIndex);
    }

    private String parseAndConsumeString(int tokenIndex) throws IOException {
        String s = new String(this.mBuffer, 0, tokenIndex, StandardCharsets.US_ASCII);
        consumeBuf(tokenIndex + 1);
        return s;
    }

    private long parseAndConsumeLong(int tokenIndex) throws IOException {
        int i = 0;
        boolean negative = this.mBuffer[0] == 45;
        long result = 0;
        if (negative) {
            i = 1;
        }
        while (i < tokenIndex) {
            int digit = this.mBuffer[i] + MidiConstants.STATUS_CHANNEL_PRESSURE;
            if (digit < 0 || digit > 9) {
                throw invalidLong(tokenIndex);
            }
            long next = (10 * result) - ((long) digit);
            if (next <= result) {
                result = next;
                i++;
            } else {
                throw invalidLong(tokenIndex);
            }
        }
        consumeBuf(tokenIndex + 1);
        return negative ? result : -result;
    }

    private NumberFormatException invalidLong(int tokenIndex) {
        return new NumberFormatException("invalid long: " + new String(this.mBuffer, 0, tokenIndex, StandardCharsets.US_ASCII));
    }

    public int nextInt() throws IOException {
        long value = nextLong();
        if (value <= 2147483647L && value >= -2147483648L) {
            return (int) value;
        }
        throw new NumberFormatException("parsed value larger than integer");
    }

    public void close() throws IOException {
        this.mStream.close();
    }
}
