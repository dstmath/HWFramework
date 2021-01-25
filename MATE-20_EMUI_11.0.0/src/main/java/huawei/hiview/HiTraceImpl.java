package huawei.hiview;

import java.util.IllegalFormatException;
import java.util.Locale;

public final class HiTraceImpl implements HiTrace {
    public static final int HITRACE_CM_MAX = 3;
    public static final int HITRACE_CM_MIN = 0;
    public static final int HITRACE_FLAG_MAX = 127;
    public static final int HITRACE_FLAG_MIN = 0;
    public static final int HITRACE_TP_MAX = 4;
    public static final int HITRACE_TP_MIN = 0;
    private static HiTraceImpl instance = new HiTraceImpl();

    private static native byte[] beginNative(String str, int i);

    private static native void clearIdNative();

    private static native byte[] createSpanNative();

    private static native void endNative(byte[] bArr);

    private static native byte[] getIdNative();

    private static native void setIdNative(byte[] bArr);

    private static native void tracePointNative(int i, int i2, byte[] bArr, String str);

    static {
        System.loadLibrary("hitrace_jni");
    }

    private HiTraceImpl() {
    }

    public static HiTrace getInstance() {
        return instance;
    }

    public HiTraceId begin(String name, int flags) {
        if (flags < 0 || flags > 127) {
            return new HiTraceIdImpl();
        }
        byte[] idArray = beginNative(name, flags);
        if (idArray == null) {
            return new HiTraceIdImpl();
        }
        return new HiTraceIdImpl(idArray);
    }

    public void end(HiTraceId hiTraceId) {
        if (hiTraceId != null) {
            byte[] idArray = hiTraceId.toBytes();
            if (idArray.length == 16) {
                endNative(idArray);
            }
        }
    }

    public HiTraceId getId() {
        byte[] idArray = getIdNative();
        if (idArray == null) {
            return new HiTraceIdImpl();
        }
        return new HiTraceIdImpl(idArray);
    }

    public void setId(HiTraceId id) {
        if (id != null) {
            byte[] idArray = id.toBytes();
            if (idArray.length == 16) {
                setIdNative(idArray);
            }
        }
    }

    public void clearId() {
        clearIdNative();
    }

    public HiTraceId createSpan() {
        byte[] idArray = createSpanNative();
        if (idArray == null) {
            return new HiTraceIdImpl();
        }
        return new HiTraceIdImpl(idArray);
    }

    public void tracePoint(int type, HiTraceId traceId, String fmt, Object... args) {
        tracePoint(0, type, traceId, fmt, args);
    }

    public void tracePoint(int mode, int type, HiTraceId traceId, String fmt, Object... args) {
        if (fmt != null && traceId != null && mode >= 0 && mode <= 3 && type >= 0 && type <= 4) {
            String traceInfo = null;
            try {
                traceInfo = String.format(Locale.ENGLISH, fmt, args);
            } catch (IllegalFormatException e) {
                if (0 == 0) {
                    return;
                }
            }
            if (traceInfo != null) {
                byte[] idArray = traceId.toBytes();
                if (idArray.length == 16) {
                    tracePointNative(mode, type, idArray, traceInfo);
                }
            }
        }
    }
}
