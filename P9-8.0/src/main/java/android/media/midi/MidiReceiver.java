package android.media.midi;

import java.io.IOException;

public abstract class MidiReceiver {
    private final int mMaxMessageSize;

    public abstract void onSend(byte[] bArr, int i, int i2, long j) throws IOException;

    public MidiReceiver() {
        this.mMaxMessageSize = Integer.MAX_VALUE;
    }

    public MidiReceiver(int maxMessageSize) {
        this.mMaxMessageSize = maxMessageSize;
    }

    public void flush() throws IOException {
        onFlush();
    }

    public void onFlush() throws IOException {
    }

    public final int getMaxMessageSize() {
        return this.mMaxMessageSize;
    }

    public void send(byte[] msg, int offset, int count) throws IOException {
        send(msg, offset, count, 0);
    }

    public void send(byte[] msg, int offset, int count, long timestamp) throws IOException {
        int messageSize = getMaxMessageSize();
        while (count > 0) {
            int length = count > messageSize ? messageSize : count;
            onSend(msg, offset, length, timestamp);
            offset += length;
            count -= length;
        }
    }
}
