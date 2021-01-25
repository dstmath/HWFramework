package ohos.media.common;

public class BufferInfo {
    public static final int BUFFER_TYPE_CODEC_CONFIG = 2;
    public static final int BUFFER_TYPE_END_OF_STREAM = 4;
    public static final int BUFFER_TYPE_KEY_FRAME = 1;
    public static final int BUFFER_TYPE_MUXER_DATA = 16;
    public static final int BUFFER_TYPE_PARTIAL_FRAME = 8;
    public int bufferType;
    public int offset;
    public int size;
    public long timeStamp;

    public void setInfo(int i, int i2, long j, int i3) {
        this.offset = i;
        this.size = i2;
        this.timeStamp = j;
        this.bufferType = i3;
    }
}
