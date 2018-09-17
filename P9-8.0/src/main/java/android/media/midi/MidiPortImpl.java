package android.media.midi;

class MidiPortImpl {
    private static final int DATA_PACKET_OVERHEAD = 9;
    public static final int MAX_PACKET_DATA_SIZE = 1015;
    public static final int MAX_PACKET_SIZE = 1024;
    public static final int PACKET_TYPE_DATA = 1;
    public static final int PACKET_TYPE_FLUSH = 2;
    private static final String TAG = "MidiPort";
    private static final int TIMESTAMP_SIZE = 8;

    MidiPortImpl() {
    }

    public static int packData(byte[] message, int offset, int size, long timestamp, byte[] dest) {
        if (size > MAX_PACKET_DATA_SIZE) {
            size = MAX_PACKET_DATA_SIZE;
        }
        dest[0] = (byte) 1;
        System.arraycopy(message, offset, dest, 1, size);
        int i = 0;
        int length = size + 1;
        while (i < 8) {
            int length2 = length + 1;
            dest[length] = (byte) ((int) timestamp);
            timestamp >>= 8;
            i++;
            length = length2;
        }
        return length;
    }

    public static int packFlush(byte[] dest) {
        dest[0] = (byte) 2;
        return 1;
    }

    public static int getPacketType(byte[] buffer, int bufferLength) {
        return buffer[0];
    }

    public static int getDataOffset(byte[] buffer, int bufferLength) {
        return 1;
    }

    public static int getDataSize(byte[] buffer, int bufferLength) {
        return bufferLength - 9;
    }

    public static long getPacketTimestamp(byte[] buffer, int bufferLength) {
        int offset = bufferLength;
        long timestamp = 0;
        for (int i = 0; i < 8; i++) {
            offset--;
            timestamp = (timestamp << 8) | ((long) (buffer[offset] & 255));
        }
        return timestamp;
    }
}
