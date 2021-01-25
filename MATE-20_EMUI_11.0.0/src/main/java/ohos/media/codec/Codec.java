package ohos.media.codec;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import ohos.agp.graphics.Surface;
import ohos.media.common.BufferInfo;
import ohos.media.common.Format;
import ohos.media.common.Source;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class Codec {
    private static final int DEFAULT_TRACK_ID = 0;
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(Codec.class);
    private static final int SUCCESS = 0;
    public static final int VIDEO_SCALE_TYPE_CROP = 2;
    public static final int VIDEO_SCALE_TYPE_FIT = 1;
    private ICodecListener codecListener = null;
    private long nativeCodec = 0;
    private long nativeListener = 0;

    public interface ICodecListener {
        void onError(int i, int i2, int i3);

        void onReadBuffer(ByteBuffer byteBuffer, BufferInfo bufferInfo, int i);
    }

    private native ByteBuffer nativeGetAvailableBuffer(long j);

    private native Map<String, Object> nativeGetBufferFormat(ByteBuffer byteBuffer);

    private static native void nativeInit();

    private native int nativeRegisterCodecListener(ICodecListener iCodecListener);

    private native int nativeRelease();

    private native int nativeSetCodecFormat(String[] strArr, Object[] objArr);

    private native int nativeSetProperty(String[] strArr, Object[] objArr);

    private native int nativeSetSource(Source source, TrackInfo trackInfo);

    private native int nativeSetSourceFormat(int i, String[] strArr, Object[] objArr);

    private native int nativeSetVideoScaleType(int i);

    private native int nativeSetVideoSurface(Surface surface);

    private native int nativeSetup(boolean z);

    private native int nativeSetupByName(String str);

    private native int nativeStart();

    private native int nativeStop();

    private native int nativeWriteBuffer(ByteBuffer byteBuffer, BufferInfo bufferInfo);

    static {
        System.loadLibrary("zcodec_jni.z");
        nativeInit();
    }

    private Codec(boolean z) {
        nativeSetup(z);
    }

    private Codec(String str) {
        nativeSetupByName(str);
    }

    public static Codec createDecoder() {
        return new Codec(true);
    }

    public static Codec createEncoder() {
        return new Codec(false);
    }

    public static Codec createCodecByName(String str) {
        if (str != null) {
            return new Codec(str);
        }
        LOGGER.error("createCodecByName failed, name is null", new Object[0]);
        return null;
    }

    public boolean registerCodecListener(ICodecListener iCodecListener) {
        int nativeRegisterCodecListener = nativeRegisterCodecListener(iCodecListener);
        if (nativeRegisterCodecListener != 0) {
            LOGGER.error("registerCodecListener failed, error code is %{public}d", Integer.valueOf(nativeRegisterCodecListener));
            return false;
        }
        this.codecListener = iCodecListener;
        return true;
    }

    public boolean setSource(Source source, TrackInfo trackInfo) {
        if (source != null) {
            int nativeSetSource = nativeSetSource(source, trackInfo);
            if (nativeSetSource == 0) {
                return true;
            }
            LOGGER.error("set source failed, error code is %{public}d", Integer.valueOf(nativeSetSource));
            return false;
        }
        throw new IllegalArgumentException("source should not be null");
    }

    public boolean setSourceFormat(Format format) {
        return setSourceFormat(0, format);
    }

    private boolean setSourceFormat(int i, Format format) {
        if (format == null) {
            LOGGER.error("setSourceFormat failed, format is null.", new Object[0]);
            return false;
        }
        Format.FormatArrays formatArrays = format.getFormatArrays();
        int nativeSetSourceFormat = nativeSetSourceFormat(i, formatArrays.keys, formatArrays.values);
        if (nativeSetSourceFormat == 0) {
            return true;
        }
        LOGGER.error("setSourceFormat failed, error code is %{public}d", Integer.valueOf(nativeSetSourceFormat));
        return false;
    }

    public boolean setCodecFormat(Format format) {
        if (format == null) {
            LOGGER.error("setCodecFormat failed, format is null.", new Object[0]);
            return false;
        }
        Format.FormatArrays formatArrays = format.getFormatArrays();
        int nativeSetCodecFormat = nativeSetCodecFormat(formatArrays.keys, formatArrays.values);
        if (nativeSetCodecFormat == 0) {
            return true;
        }
        LOGGER.error("setCodecFormat failed, error code is %{public}d", Integer.valueOf(nativeSetCodecFormat));
        return false;
    }

    public boolean setProperty(Format format) {
        if (format == null) {
            LOGGER.error("setProperty failed, format is null", new Object[0]);
            return false;
        }
        Format.FormatArrays formatArrays = format.getFormatArrays();
        int nativeSetProperty = nativeSetProperty(formatArrays.keys, formatArrays.values);
        if (nativeSetProperty == 0) {
            return true;
        }
        LOGGER.error("setProperty failed, error code is %{public}d", Integer.valueOf(nativeSetProperty));
        return false;
    }

    public boolean setVideoSurface(Surface surface) {
        if (surface == null) {
            LOGGER.error("setVideoSurface failed, surface is null", new Object[0]);
            return false;
        }
        int nativeSetVideoSurface = nativeSetVideoSurface(surface);
        if (nativeSetVideoSurface == 0) {
            return true;
        }
        LOGGER.error("setVideoSurface failed, error code is %{public}d", Integer.valueOf(nativeSetVideoSurface));
        return false;
    }

    public boolean setVideoScaleType(int i) {
        if (i == 1 || i == 2) {
            int nativeSetVideoScaleType = nativeSetVideoScaleType(i);
            if (nativeSetVideoScaleType == 0) {
                return true;
            }
            LOGGER.error("setVideoScaleType failed, error code is %{public}d", Integer.valueOf(nativeSetVideoScaleType));
            return false;
        }
        LOGGER.error("setVideoScaleType failed, mode:%{public}d is invalid", Integer.valueOf(i));
        return false;
    }

    public ByteBuffer getAvailableBuffer(long j) {
        return nativeGetAvailableBuffer(j);
    }

    public boolean writeBuffer(ByteBuffer byteBuffer, BufferInfo bufferInfo) {
        if (byteBuffer == null || bufferInfo == null) {
            LOGGER.error("writeBuffer failed, parameter is null", new Object[0]);
            return false;
        }
        int nativeWriteBuffer = nativeWriteBuffer(byteBuffer, bufferInfo);
        if (nativeWriteBuffer == 0) {
            return true;
        }
        LOGGER.error("writeBuffer failed, error code is %{public}d", Integer.valueOf(nativeWriteBuffer));
        return false;
    }

    public Format getBufferFormat(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            LOGGER.error("getBufferFormat failed, parameter is null", new Object[0]);
            return null;
        }
        HashMap hashMap = (HashMap) nativeGetBufferFormat(byteBuffer);
        if (hashMap == null) {
            return null;
        }
        return new Format((HashMap<String, Object>) hashMap);
    }

    public boolean start() {
        int nativeStart = nativeStart();
        if (nativeStart == 0) {
            return true;
        }
        LOGGER.error("start failed, error code is %{public}d", Integer.valueOf(nativeStart));
        return false;
    }

    public boolean stop() {
        int nativeStop = nativeStop();
        if (nativeStop == 0) {
            return true;
        }
        LOGGER.error("stop failed, error code is %{public}d", Integer.valueOf(nativeStop));
        return false;
    }

    public boolean release() {
        int nativeRelease = nativeRelease();
        if (nativeRelease == 0) {
            return true;
        }
        LOGGER.error("release failed, error code is %{public}d", Integer.valueOf(nativeRelease));
        return false;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        release();
        super.finalize();
    }
}
