package ohos.hiviewdfx;

import java.util.IllegalFormatException;
import java.util.Locale;

public final class HiTraceImpl {
    private static final HiLogLabel HITRACE_LABEL = new HiLogLabel(3, 11523, "HITRACE");

    private static native byte[] beginNative(String str, int i);

    private static native void clearIdNative();

    private static native byte[] createSpanNative();

    private static native void endNative(byte[] bArr);

    private static native byte[] getIdNative();

    private static native void setIdNative(byte[] bArr);

    private static native void tracePointNative(int i, byte[] bArr, String str);

    static {
        System.loadLibrary("hitrace_jni.z");
    }

    private HiTraceImpl() {
    }

    public static HiTraceId begin(String str, int i) {
        if (i < 0 || i >= 64) {
            return new HiTraceId();
        }
        byte[] beginNative = beginNative(str, i);
        if (beginNative == null) {
            return new HiTraceId();
        }
        return new HiTraceId(beginNative);
    }

    public static void end(HiTraceId hiTraceId) {
        if (hiTraceId != null) {
            byte[] bytes = hiTraceId.toBytes();
            if (bytes.length == 16) {
                endNative(bytes);
            }
        }
    }

    public static HiTraceId getId() {
        byte[] idNative = getIdNative();
        if (idNative == null) {
            return new HiTraceId();
        }
        return new HiTraceId(idNative);
    }

    public static void setId(HiTraceId hiTraceId) {
        if (hiTraceId != null) {
            byte[] bytes = hiTraceId.toBytes();
            if (bytes.length == 16) {
                setIdNative(bytes);
            }
        }
    }

    public static void clearId() {
        clearIdNative();
    }

    public static HiTraceId createSpan() {
        byte[] createSpanNative = createSpanNative();
        if (createSpanNative == null) {
            return new HiTraceId();
        }
        return new HiTraceId(createSpanNative);
    }

    public static void tracePoint(int i, HiTraceId hiTraceId, String str, Object... objArr) {
        if (str != null && i >= 0 && i <= 4) {
            String str2 = null;
            try {
                str2 = String.format(Locale.ENGLISH, str, objArr);
            } catch (IllegalFormatException unused) {
                HiLog.error(HITRACE_LABEL, "%{public}s is a invalid fmt", str);
            }
            if (str2 != null) {
                byte[] bytes = hiTraceId.toBytes();
                if (bytes.length == 16) {
                    tracePointNative(i, bytes, str2);
                }
            }
        }
    }
}
