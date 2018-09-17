package android.media.midi;

import android.preference.Preference;
import java.io.IOException;

public abstract class MidiReceiver {
    private final int mMaxMessageSize;

    public abstract void onSend(byte[] bArr, int i, int i2, long j) throws IOException;

    public MidiReceiver() {
        this.mMaxMessageSize = Preference.DEFAULT_ORDER;
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
            int length;
            if (count > messageSize) {
                length = messageSize;
            } else {
                length = count;
            }
            onSend(msg, offset, length, timestamp);
            offset += length;
            count -= length;
        }
    }
}
