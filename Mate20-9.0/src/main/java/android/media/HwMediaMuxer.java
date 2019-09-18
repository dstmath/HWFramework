package android.media;

import android.media.MediaCodec;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public final class HwMediaMuxer {
    private static final String TAG = "HwMediaMuxer";
    private long mNativeObject = getFieldValueByFieldName("mNativeObject", this.mediaMuxer);
    private MediaMuxer mediaMuxer;

    private static native void nativeSetUserTag(long j, String str);

    static {
        System.loadLibrary("media_jni.huawei");
    }

    public HwMediaMuxer(String path, int format) throws IOException {
        this.mediaMuxer = new MediaMuxer(path, format);
    }

    public HwMediaMuxer(FileDescriptor fd, int format) throws IOException {
        this.mediaMuxer = new MediaMuxer(fd, format);
    }

    private long getFieldValueByFieldName(String fieldName, Object object) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return ((Long) field.get(object)).longValue();
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            Log.e(TAG, "GET mNativeObject Failed");
            return -1;
        }
    }

    public void setOrientationHint(int degrees) {
        this.mediaMuxer.setOrientationHint(degrees);
    }

    public void setLocation(float latitude, float longitude) {
        this.mediaMuxer.setLocation(latitude, longitude);
    }

    public void setUserTag(String userTag) {
        Log.i(TAG, "setUserTag() = " + userTag);
        if (userTag != null) {
            nativeSetUserTag(this.mNativeObject, userTag);
            return;
        }
        throw new IllegalStateException("Can't set null String");
    }

    public void start() {
        this.mediaMuxer.start();
    }

    public void stop() {
        this.mediaMuxer.stop();
    }

    public int addTrack(MediaFormat format) {
        return this.mediaMuxer.addTrack(format);
    }

    public void writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        this.mediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
    }

    public void release() {
        this.mediaMuxer.release();
    }
}
