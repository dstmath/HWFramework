package ohos.media.muxer;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import ohos.media.common.BufferInfo;
import ohos.media.common.Format;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class Muxer {
    private static final int INVALID_TRACK_INDEX = -1;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(Muxer.class);
    private static final int SUCCESS = 0;
    private int lastTrackIndex = -1;
    private long nativeZMuxer = 0;

    private native int nativeAppendTrack(String[] strArr, Object[] objArr);

    private static native void nativeInit();

    private native int nativeRelease();

    private native int nativeSetMediaLocation(float f, float f2);

    private native int nativeSetMediaOrientation(int i);

    private native int nativeSetupByFd(int i, int i2);

    private native int nativeSetupByUri(String str, int i);

    private native int nativeStart();

    private native int nativeStop();

    private native int nativeWriteBuffer(long j, int i, ByteBuffer byteBuffer, int i2, int i3, long j2, int i4);

    static {
        System.loadLibrary("zmuxer_jni.z");
        nativeInit();
    }

    public static final class MediaFileFormat {
        public static final int FORMAT_3GPP = 2;
        private static final int FORMAT_FIRST = 0;
        public static final int FORMAT_HEIF = 3;
        private static final int FORMAT_LAST = 3;
        public static final int FORMAT_MPEG4 = 0;
        public static final int FORMAT_WEBM = 1;

        private MediaFileFormat() {
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0048, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004d, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004e, code lost:
        r5.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0051, code lost:
        throw r6;
     */
    public Muxer(String str, int i) {
        if (str == null) {
            throw new IllegalArgumentException("outputUri must not be null");
        } else if (i < 0 || i > 3) {
            throw new IllegalArgumentException("outputFormat: invalid value " + i);
        } else {
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(str, "rws");
                randomAccessFile.setLength(0);
                int nativeSetupByFd = nativeSetupByFd(randomAccessFile.getFD().getInt$(), i);
                if (nativeSetupByFd == 0) {
                    randomAccessFile.close();
                } else {
                    LOGGER.error("setup media muxer by url failed, error code is %{public}d", Integer.valueOf(nativeSetupByFd));
                    throw new IllegalArgumentException("setup media muxer by url failed");
                }
            } catch (IOException unused) {
                LOGGER.error("setup media muxer by url failed, catch IOException", new Object[0]);
            }
        }
    }

    public Muxer(FileDescriptor fileDescriptor, int i) {
        if (fileDescriptor == null) {
            throw new IllegalArgumentException("fd must not be null");
        } else if (i < 0 || i > 3) {
            throw new IllegalArgumentException("outputFormat: invalid value " + i);
        } else {
            int nativeSetupByFd = nativeSetupByFd(fileDescriptor.getInt$(), i);
            if (nativeSetupByFd != 0) {
                LOGGER.error("setup media muxer by fd failed, error code is %{public}d", Integer.valueOf(nativeSetupByFd));
                throw new IllegalArgumentException("setup media muxer by fd failed");
            }
        }
    }

    public int appendTrack(Format format) {
        if (format != null) {
            Format.FormatArrays formatArrays = format.getFormatArrays();
            int nativeAppendTrack = nativeAppendTrack(formatArrays.keys, formatArrays.values);
            if (this.lastTrackIndex >= nativeAppendTrack) {
                LOGGER.error("Invalid track index.", new Object[0]);
                return -1;
            }
            this.lastTrackIndex = nativeAppendTrack;
            return nativeAppendTrack;
        }
        throw new IllegalArgumentException("format is invalid");
    }

    public boolean start() {
        int nativeStart = nativeStart();
        if (nativeStart == 0) {
            return true;
        }
        LOGGER.error("start media muxer failed,error code is %{public}d", Integer.valueOf(nativeStart));
        return false;
    }

    public boolean stop() {
        int nativeStop = nativeStop();
        if (nativeStop == 0) {
            return true;
        }
        LOGGER.error("stop media muxer failed,error code is %{public}d", Integer.valueOf(nativeStop));
        return false;
    }

    public boolean release() {
        int nativeRelease = nativeRelease();
        if (nativeRelease == 0) {
            return true;
        }
        LOGGER.error("release media muxer failed,error code is %{public}d", Integer.valueOf(nativeRelease));
        return false;
    }

    public boolean writeBuffer(int i, ByteBuffer byteBuffer, BufferInfo bufferInfo) {
        if (i < 0 || i > this.lastTrackIndex) {
            LOGGER.error("trackIndex is invalid %{public}d", Integer.valueOf(i));
            return false;
        } else if (byteBuffer == null) {
            LOGGER.error("byteBuffer must not be null", new Object[0]);
            return false;
        } else if (bufferInfo == null) {
            LOGGER.error("bufferInfo must not be null", new Object[0]);
            return false;
        } else if (bufferInfo.size < 0 || bufferInfo.offset < 0 || bufferInfo.offset + bufferInfo.size > byteBuffer.capacity() || bufferInfo.timeStamp < 0) {
            LOGGER.error("bufferInfo must specify valid buffer offset, size and presentation time. current are %{public}d, %{public}d, %{public}d", Integer.valueOf(bufferInfo.size), Integer.valueOf(bufferInfo.offset), Long.valueOf(bufferInfo.timeStamp));
            return false;
        } else {
            int nativeWriteBuffer = nativeWriteBuffer(this.nativeZMuxer, i, byteBuffer, bufferInfo.offset, bufferInfo.size, bufferInfo.timeStamp, bufferInfo.bufferType);
            if (nativeWriteBuffer == 0) {
                return true;
            }
            LOGGER.error("write buffer failed,error code is %{public}d", Integer.valueOf(nativeWriteBuffer));
            return false;
        }
    }

    public boolean setMediaLocation(float f, float f2) {
        BigDecimal bigDecimal = new BigDecimal("-90");
        BigDecimal bigDecimal2 = new BigDecimal("90");
        BigDecimal bigDecimal3 = new BigDecimal("-180");
        BigDecimal bigDecimal4 = new BigDecimal("180");
        BigDecimal bigDecimal5 = new BigDecimal(String.valueOf(f));
        BigDecimal bigDecimal6 = new BigDecimal(String.valueOf(f2));
        if (bigDecimal5.compareTo(bigDecimal) < 0 || bigDecimal5.compareTo(bigDecimal2) > 0) {
            LOGGER.error("latitude :%{public}f is invalid", Float.valueOf(f));
            return false;
        } else if (bigDecimal6.compareTo(bigDecimal3) < 0 || bigDecimal6.compareTo(bigDecimal4) > 0) {
            LOGGER.error("longitude :%{public}f is invalid", Float.valueOf(f2));
            return false;
        } else {
            int nativeSetMediaLocation = nativeSetMediaLocation(f, f2);
            if (nativeSetMediaLocation == 0) {
                return true;
            }
            LOGGER.error("set media muxer location failed,error code is %{public}d", Integer.valueOf(nativeSetMediaLocation));
            return false;
        }
    }

    public boolean setMediaOrientation(int i) {
        if (i == 0 || i == 90 || i == 180 || i == 270) {
            int nativeSetMediaOrientation = nativeSetMediaOrientation(i);
            if (nativeSetMediaOrientation == 0) {
                return true;
            }
            LOGGER.error("set media muxer Orientation failed,error code is %{public}d", Integer.valueOf(nativeSetMediaOrientation));
            return false;
        }
        LOGGER.error("Unsupported angle: %{public}d", Integer.valueOf(i));
        return false;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }
}
