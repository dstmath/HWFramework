package android.util.proto;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.BatteryStats;
import android.util.Log;
import com.android.internal.midi.MidiConstants;
import java.util.ArrayList;

public final class EncodedBuffer {
    private static final String TAG = "EncodedBuffer";
    private int mBufferCount;
    private final ArrayList<byte[]> mBuffers;
    private final int mChunkSize;
    private int mReadBufIndex;
    private byte[] mReadBuffer;
    private int mReadIndex;
    private int mReadLimit;
    private int mReadableSize;
    private int mWriteBufIndex;
    private byte[] mWriteBuffer;
    private int mWriteIndex;

    public EncodedBuffer() {
        this(0);
    }

    public EncodedBuffer(int chunkSize) {
        this.mBuffers = new ArrayList<>();
        this.mReadLimit = -1;
        this.mReadableSize = -1;
        this.mChunkSize = chunkSize <= 0 ? 8192 : chunkSize;
        this.mWriteBuffer = new byte[this.mChunkSize];
        this.mBuffers.add(this.mWriteBuffer);
        this.mBufferCount = 1;
    }

    public void startEditing() {
        int i = this.mWriteBufIndex * this.mChunkSize;
        int i2 = this.mWriteIndex;
        this.mReadableSize = i + i2;
        this.mReadLimit = i2;
        this.mWriteBuffer = this.mBuffers.get(0);
        this.mWriteIndex = 0;
        this.mWriteBufIndex = 0;
        this.mReadBuffer = this.mWriteBuffer;
        this.mReadBufIndex = 0;
        this.mReadIndex = 0;
    }

    public void rewindRead() {
        this.mReadBuffer = this.mBuffers.get(0);
        this.mReadBufIndex = 0;
        this.mReadIndex = 0;
    }

    public int getReadableSize() {
        return this.mReadableSize;
    }

    public int getSize() {
        return ((this.mBufferCount - 1) * this.mChunkSize) + this.mWriteIndex;
    }

    public int getReadPos() {
        return (this.mReadBufIndex * this.mChunkSize) + this.mReadIndex;
    }

    public void skipRead(int amount) {
        if (amount < 0) {
            throw new RuntimeException("skipRead with negative amount=" + amount);
        } else if (amount != 0) {
            int i = this.mChunkSize;
            int i2 = this.mReadIndex;
            if (amount <= i - i2) {
                this.mReadIndex = i2 + amount;
                return;
            }
            int amount2 = amount - (i - i2);
            this.mReadIndex = amount2 % i;
            if (this.mReadIndex == 0) {
                this.mReadIndex = i;
                this.mReadBufIndex += amount2 / i;
            } else {
                this.mReadBufIndex += (amount2 / i) + 1;
            }
            this.mReadBuffer = this.mBuffers.get(this.mReadBufIndex);
        }
    }

    public byte readRawByte() {
        int i = this.mReadBufIndex;
        int i2 = this.mBufferCount;
        if (i > i2 || (i == i2 - 1 && this.mReadIndex >= this.mReadLimit)) {
            throw new IndexOutOfBoundsException("Trying to read too much data mReadBufIndex=" + this.mReadBufIndex + " mBufferCount=" + this.mBufferCount + " mReadIndex=" + this.mReadIndex + " mReadLimit=" + this.mReadLimit);
        }
        if (this.mReadIndex >= this.mChunkSize) {
            this.mReadBufIndex++;
            this.mReadBuffer = this.mBuffers.get(this.mReadBufIndex);
            this.mReadIndex = 0;
        }
        byte[] bArr = this.mReadBuffer;
        int i3 = this.mReadIndex;
        this.mReadIndex = i3 + 1;
        return bArr[i3];
    }

    public long readRawUnsigned() {
        int bits = 0;
        long result = 0;
        do {
            byte b = readRawByte();
            result |= ((long) (b & Byte.MAX_VALUE)) << bits;
            if ((b & 128) == 0) {
                return result;
            }
            bits += 7;
        } while (bits <= 64);
        throw new ProtoParseException("Varint too long -- " + getDebugString());
    }

    public int readRawFixed32() {
        return (readRawByte() & 255) | ((readRawByte() & 255) << 8) | ((readRawByte() & 255) << 16) | ((readRawByte() & 255) << 24);
    }

    private void nextWriteBuffer() {
        this.mWriteBufIndex++;
        int i = this.mWriteBufIndex;
        if (i >= this.mBufferCount) {
            this.mWriteBuffer = new byte[this.mChunkSize];
            this.mBuffers.add(this.mWriteBuffer);
            this.mBufferCount++;
        } else {
            this.mWriteBuffer = this.mBuffers.get(i);
        }
        this.mWriteIndex = 0;
    }

    public void writeRawByte(byte val) {
        if (this.mWriteIndex >= this.mChunkSize) {
            nextWriteBuffer();
        }
        byte[] bArr = this.mWriteBuffer;
        int i = this.mWriteIndex;
        this.mWriteIndex = i + 1;
        bArr[i] = val;
    }

    public static int getRawVarint32Size(int val) {
        if ((val & -128) == 0) {
            return 1;
        }
        if ((val & -16384) == 0) {
            return 2;
        }
        if ((-2097152 & val) == 0) {
            return 3;
        }
        if ((-268435456 & val) == 0) {
            return 4;
        }
        return 5;
    }

    public void writeRawVarint32(int val) {
        while ((val & -128) != 0) {
            writeRawByte((byte) ((val & 127) | 128));
            val >>>= 7;
        }
        writeRawByte((byte) val);
    }

    public static int getRawZigZag32Size(int val) {
        return getRawVarint32Size(zigZag32(val));
    }

    public void writeRawZigZag32(int val) {
        writeRawVarint32(zigZag32(val));
    }

    public static int getRawVarint64Size(long val) {
        if ((-128 & val) == 0) {
            return 1;
        }
        if ((-16384 & val) == 0) {
            return 2;
        }
        if ((-2097152 & val) == 0) {
            return 3;
        }
        if ((-268435456 & val) == 0) {
            return 4;
        }
        if ((-34359738368L & val) == 0) {
            return 5;
        }
        if ((-4398046511104L & val) == 0) {
            return 6;
        }
        if ((-562949953421312L & val) == 0) {
            return 7;
        }
        if ((BatteryStats.STEP_LEVEL_MODIFIED_MODE_MASK & val) == 0) {
            return 8;
        }
        if ((Long.MIN_VALUE & val) == 0) {
            return 9;
        }
        return 10;
    }

    public void writeRawVarint64(long val) {
        while ((-128 & val) != 0) {
            writeRawByte((byte) ((int) ((127 & val) | 128)));
            val >>>= 7;
        }
        writeRawByte((byte) ((int) val));
    }

    public static int getRawZigZag64Size(long val) {
        return getRawVarint64Size(zigZag64(val));
    }

    public void writeRawZigZag64(long val) {
        writeRawVarint64(zigZag64(val));
    }

    public void writeRawFixed32(int val) {
        writeRawByte((byte) val);
        writeRawByte((byte) (val >> 8));
        writeRawByte((byte) (val >> 16));
        writeRawByte((byte) (val >> 24));
    }

    public void writeRawFixed64(long val) {
        writeRawByte((byte) ((int) val));
        writeRawByte((byte) ((int) (val >> 8)));
        writeRawByte((byte) ((int) (val >> 16)));
        writeRawByte((byte) ((int) (val >> 24)));
        writeRawByte((byte) ((int) (val >> 32)));
        writeRawByte((byte) ((int) (val >> 40)));
        writeRawByte((byte) ((int) (val >> 48)));
        writeRawByte((byte) ((int) (val >> 56)));
    }

    public void writeRawBuffer(byte[] val) {
        if (val != null && val.length > 0) {
            writeRawBuffer(val, 0, val.length);
        }
    }

    public void writeRawBuffer(byte[] val, int offset, int length) {
        if (val != null) {
            int i = this.mChunkSize;
            int i2 = this.mWriteIndex;
            int amt = length < i - i2 ? length : i - i2;
            if (amt > 0) {
                System.arraycopy(val, offset, this.mWriteBuffer, this.mWriteIndex, amt);
                this.mWriteIndex += amt;
                length -= amt;
                offset += amt;
            }
            while (length > 0) {
                nextWriteBuffer();
                int amt2 = this.mChunkSize;
                if (length < amt2) {
                    amt2 = length;
                }
                System.arraycopy(val, offset, this.mWriteBuffer, this.mWriteIndex, amt2);
                this.mWriteIndex += amt2;
                length -= amt2;
                offset += amt2;
            }
        }
    }

    public void writeFromThisBuffer(int srcOffset, int size) {
        if (this.mReadLimit < 0) {
            throw new IllegalStateException("writeFromThisBuffer before startEditing");
        } else if (srcOffset < getWritePos()) {
            throw new IllegalArgumentException("Can only move forward in the buffer -- srcOffset=" + srcOffset + " size=" + size + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + getDebugString());
        } else if (srcOffset + size > this.mReadableSize) {
            throw new IllegalArgumentException("Trying to move more data than there is -- srcOffset=" + srcOffset + " size=" + size + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + getDebugString());
        } else if (size != 0) {
            int i = this.mWriteBufIndex;
            int i2 = this.mChunkSize;
            int i3 = this.mWriteIndex;
            if (srcOffset != (i * i2) + i3) {
                int readBufIndex = srcOffset / i2;
                byte[] readBuffer = this.mBuffers.get(readBufIndex);
                int readIndex = srcOffset % this.mChunkSize;
                while (size > 0) {
                    if (this.mWriteIndex >= this.mChunkSize) {
                        nextWriteBuffer();
                    }
                    if (readIndex >= this.mChunkSize) {
                        readBufIndex++;
                        readBuffer = this.mBuffers.get(readBufIndex);
                        readIndex = 0;
                    }
                    int i4 = this.mChunkSize;
                    int amt = Math.min(size, Math.min(i4 - this.mWriteIndex, i4 - readIndex));
                    System.arraycopy(readBuffer, readIndex, this.mWriteBuffer, this.mWriteIndex, amt);
                    this.mWriteIndex += amt;
                    readIndex += amt;
                    size -= amt;
                }
            } else if (size <= i2 - i3) {
                this.mWriteIndex = i3 + size;
            } else {
                int size2 = size - (i2 - i3);
                this.mWriteIndex = size2 % i2;
                if (this.mWriteIndex == 0) {
                    this.mWriteIndex = i2;
                    this.mWriteBufIndex = i + (size2 / i2);
                } else {
                    this.mWriteBufIndex = i + (size2 / i2) + 1;
                }
                this.mWriteBuffer = this.mBuffers.get(this.mWriteBufIndex);
            }
        }
    }

    public int getWritePos() {
        return (this.mWriteBufIndex * this.mChunkSize) + this.mWriteIndex;
    }

    public void rewindWriteTo(int writePos) {
        int i;
        if (writePos <= getWritePos()) {
            int i2 = this.mChunkSize;
            this.mWriteBufIndex = writePos / i2;
            this.mWriteIndex = writePos % i2;
            if (this.mWriteIndex == 0 && (i = this.mWriteBufIndex) != 0) {
                this.mWriteIndex = i2;
                this.mWriteBufIndex = i - 1;
            }
            this.mWriteBuffer = this.mBuffers.get(this.mWriteBufIndex);
            return;
        }
        throw new RuntimeException("rewindWriteTo only can go backwards" + writePos);
    }

    public int getRawFixed32At(int pos) {
        int i = this.mChunkSize;
        int i2 = this.mChunkSize;
        int i3 = this.mChunkSize;
        return (this.mBuffers.get(pos / this.mChunkSize)[pos % i] & 255) | ((this.mBuffers.get((pos + 1) / i)[(pos + 1) % i2] & 255) << 8) | ((this.mBuffers.get((pos + 2) / i2)[(pos + 2) % i3] & 255) << 16) | ((this.mBuffers.get((pos + 3) / i3)[(pos + 3) % this.mChunkSize] & 255) << 24);
    }

    public void editRawFixed32(int pos, int val) {
        int i = this.mChunkSize;
        this.mBuffers.get(pos / this.mChunkSize)[pos % i] = (byte) val;
        int i2 = this.mChunkSize;
        this.mBuffers.get((pos + 1) / i)[(pos + 1) % i2] = (byte) (val >> 8);
        int i3 = this.mChunkSize;
        this.mBuffers.get((pos + 2) / i2)[(pos + 2) % i3] = (byte) (val >> 16);
        this.mBuffers.get((pos + 3) / i3)[(pos + 3) % this.mChunkSize] = (byte) (val >> 24);
    }

    private static int zigZag32(int val) {
        return (val << 1) ^ (val >> 31);
    }

    private static long zigZag64(long val) {
        return (val << 1) ^ (val >> 63);
    }

    public byte[] getBytes(int size) {
        byte[] result = new byte[size];
        int bufCount = size / this.mChunkSize;
        int writeIndex = 0;
        int bufIndex = 0;
        while (bufIndex < bufCount) {
            System.arraycopy(this.mBuffers.get(bufIndex), 0, result, writeIndex, this.mChunkSize);
            writeIndex += this.mChunkSize;
            bufIndex++;
        }
        int lastSize = size - (this.mChunkSize * bufCount);
        if (lastSize > 0) {
            System.arraycopy(this.mBuffers.get(bufIndex), 0, result, writeIndex, lastSize);
        }
        return result;
    }

    public int getChunkCount() {
        return this.mBuffers.size();
    }

    public int getWriteIndex() {
        return this.mWriteIndex;
    }

    public int getWriteBufIndex() {
        return this.mWriteBufIndex;
    }

    public String getDebugString() {
        return "EncodedBuffer( mChunkSize=" + this.mChunkSize + " mBuffers.size=" + this.mBuffers.size() + " mBufferCount=" + this.mBufferCount + " mWriteIndex=" + this.mWriteIndex + " mWriteBufIndex=" + this.mWriteBufIndex + " mReadBufIndex=" + this.mReadBufIndex + " mReadIndex=" + this.mReadIndex + " mReadableSize=" + this.mReadableSize + " mReadLimit=" + this.mReadLimit + " )";
    }

    public void dumpBuffers(String tag) {
        int N = this.mBuffers.size();
        int start = 0;
        for (int i = 0; i < N; i++) {
            start += dumpByteString(tag, "{" + i + "} ", start, this.mBuffers.get(i));
        }
    }

    public static void dumpByteString(String tag, String prefix, byte[] buf) {
        dumpByteString(tag, prefix, 0, buf);
    }

    private static int dumpByteString(String tag, String prefix, int start, byte[] buf) {
        StringBuffer sb = new StringBuffer();
        int length = buf.length;
        for (int i = 0; i < length; i++) {
            if (i % 16 == 0) {
                if (i != 0) {
                    Log.d(tag, sb.toString());
                    sb = new StringBuffer();
                }
                sb.append(prefix);
                sb.append('[');
                sb.append(start + i);
                sb.append(']');
                sb.append(' ');
            } else {
                sb.append(' ');
            }
            byte b = buf[i];
            byte c = (byte) ((b >> 4) & 15);
            if (c < 10) {
                sb.append((char) (c + 48));
            } else {
                sb.append((char) (c + 87));
            }
            byte d = (byte) (b & MidiConstants.STATUS_CHANNEL_MASK);
            if (d < 10) {
                sb.append((char) (d + 48));
            } else {
                sb.append((char) (d + 87));
            }
        }
        Log.d(tag, sb.toString());
        return length;
    }
}
