package android.media;

import android.annotation.UnsupportedAppUsage;
import android.media.MediaCodec;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.Map;

public final class MediaMuxer {
    private static final int MUXER_STATE_INITIALIZED = 0;
    @UnsupportedAppUsage
    private static final int MUXER_STATE_STARTED = 1;
    @UnsupportedAppUsage
    private static final int MUXER_STATE_STOPPED = 2;
    @UnsupportedAppUsage
    private static final int MUXER_STATE_UNINITIALIZED = -1;
    @UnsupportedAppUsage
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private boolean mIsHeifFormat;
    private int mLastTrackIndex = -1;
    @UnsupportedAppUsage
    private long mNativeObject;
    @UnsupportedAppUsage
    private int mState = -1;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Format {
    }

    private static native int nativeAddTrack(long j, String[] strArr, Object[] objArr);

    @UnsupportedAppUsage
    private static native void nativeRelease(long j);

    private static native void nativeSetLocation(long j, int i, int i2);

    private static native void nativeSetOrientationHint(long j, int i);

    @UnsupportedAppUsage
    private static native long nativeSetup(FileDescriptor fileDescriptor, int i) throws IllegalArgumentException, IOException;

    private static native void nativeStart(long j);

    private static native void nativeStop(long j);

    private static native void nativeWriteSampleData(long j, int i, ByteBuffer byteBuffer, int i2, int i3, long j2, int i4);

    static {
        System.loadLibrary("media_jni");
    }

    public static final class OutputFormat {
        public static final int MUXER_OUTPUT_3GPP = 2;
        public static final int MUXER_OUTPUT_FIRST = 0;
        public static final int MUXER_OUTPUT_HEIF = 3;
        public static final int MUXER_OUTPUT_LAST = 4;
        public static final int MUXER_OUTPUT_MPEG_4 = 0;
        public static final int MUXER_OUTPUT_OGG = 4;
        public static final int MUXER_OUTPUT_WEBM = 1;

        private OutputFormat() {
        }
    }

    public MediaMuxer(String path, int format) throws IOException {
        boolean z = false;
        this.mIsHeifFormat = false;
        this.mIsHeifFormat = format == 3 ? true : z;
        if (path != null) {
            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(path, "rws");
                file.setLength(0);
                setUpMediaMuxer(file.getFD(), format);
                file.close();
            } catch (Throwable th) {
                if (file != null) {
                    file.close();
                }
                throw th;
            }
        } else {
            throw new IllegalArgumentException("path must not be null");
        }
    }

    public MediaMuxer(FileDescriptor fd, int format) throws IOException {
        boolean z = false;
        this.mIsHeifFormat = false;
        this.mIsHeifFormat = format == 3 ? true : z;
        setUpMediaMuxer(fd, format);
    }

    private void setUpMediaMuxer(FileDescriptor fd, int format) throws IOException {
        if (format < 0 || format > 4) {
            throw new IllegalArgumentException("format: " + format + " is invalid");
        }
        this.mNativeObject = nativeSetup(fd, format);
        this.mState = 0;
        this.mCloseGuard.open("release");
    }

    public void setOrientationHint(int degrees) {
        if (degrees != 0 && degrees != 90 && degrees != 180 && degrees != 270) {
            throw new IllegalArgumentException("Unsupported angle: " + degrees);
        } else if (this.mState == 0) {
            nativeSetOrientationHint(this.mNativeObject, degrees);
        } else {
            throw new IllegalStateException("Can't set rotation degrees due to wrong state.");
        }
    }

    public void setLocation(float latitude, float longitude) {
        int latitudex10000 = (int) (((double) (latitude * 10000.0f)) + 0.5d);
        int longitudex10000 = (int) (((double) (10000.0f * longitude)) + 0.5d);
        if (latitudex10000 > 900000 || latitudex10000 < -900000) {
            throw new IllegalArgumentException("Latitude: " + latitude + " out of range.");
        } else if (longitudex10000 > 1800000 || longitudex10000 < -1800000) {
            throw new IllegalArgumentException("Longitude: " + longitude + " out of range");
        } else {
            if (this.mState == 0) {
                long j = this.mNativeObject;
                if (j != 0) {
                    nativeSetLocation(j, latitudex10000, longitudex10000);
                    return;
                }
            }
            throw new IllegalStateException("Can't set location due to wrong state.");
        }
    }

    public void start() {
        long j = this.mNativeObject;
        if (j == 0) {
            throw new IllegalStateException("Muxer has been released!");
        } else if (this.mState == 0) {
            nativeStart(j);
            this.mState = 1;
        } else {
            throw new IllegalStateException("Can't start due to wrong state.");
        }
    }

    public void stop() {
        if (this.mState == 1) {
            nativeStop(this.mNativeObject);
            this.mState = 2;
            return;
        }
        throw new IllegalStateException("Can't stop due to wrong state.");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            if (this.mNativeObject != 0) {
                nativeRelease(this.mNativeObject);
                this.mNativeObject = 0;
            }
        } finally {
            super.finalize();
        }
    }

    public int addTrack(MediaFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null.");
        } else if (this.mState != 0) {
            throw new IllegalStateException("Muxer is not initialized.");
        } else if (this.mNativeObject != 0) {
            Map<String, Object> formatMap = format.getMap();
            int mapSize = formatMap.size();
            if (mapSize > 0) {
                String[] keys = new String[mapSize];
                Object[] values = new Object[mapSize];
                int i = 0;
                for (Map.Entry<String, Object> entry : formatMap.entrySet()) {
                    keys[i] = entry.getKey();
                    values[i] = entry.getValue();
                    i++;
                }
                int trackIndex = nativeAddTrack(this.mNativeObject, keys, values);
                if (this.mLastTrackIndex < trackIndex) {
                    this.mLastTrackIndex = trackIndex;
                    return trackIndex;
                }
                throw new IllegalArgumentException("Invalid format.");
            }
            throw new IllegalArgumentException("format must not be empty.");
        } else {
            throw new IllegalStateException("Muxer has been released!");
        }
    }

    public void writeSampleData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
        if (trackIndex < 0 || trackIndex > this.mLastTrackIndex) {
            throw new IllegalArgumentException("trackIndex is invalid");
        } else if (byteBuf == null) {
            throw new IllegalArgumentException("byteBuffer must not be null");
        } else if (bufferInfo == null) {
            throw new IllegalArgumentException("bufferInfo must not be null");
        } else if (bufferInfo.size < 0 || bufferInfo.offset < 0 || bufferInfo.offset + bufferInfo.size > byteBuf.capacity() || bufferInfo.presentationTimeUs < 0) {
            throw new IllegalArgumentException("bufferInfo must specify a valid buffer offset, size and presentation time");
        } else {
            long j = this.mNativeObject;
            if (j == 0) {
                throw new IllegalStateException("Muxer has been released!");
            } else if (this.mState == 1) {
                nativeWriteSampleData(j, trackIndex, byteBuf, bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
            } else {
                throw new IllegalStateException("Can't write, muxer is not started");
            }
        }
    }

    public void release() {
        if (this.mState == 1) {
            stop();
        }
        long j = this.mNativeObject;
        if (j != 0) {
            nativeRelease(j);
            this.mNativeObject = 0;
            this.mCloseGuard.close();
        }
        this.mState = -1;
        if (this.mIsHeifFormat) {
            HwMediaMonitorManager.writeBigData(HwMediaMonitorUtils.BD_MEDIA_HEIF, HwMediaMonitorUtils.M_HEIF_ENCODE);
        }
    }
}
