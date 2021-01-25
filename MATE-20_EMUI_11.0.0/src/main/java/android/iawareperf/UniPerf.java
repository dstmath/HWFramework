package android.iawareperf;

import android.util.Slog;

public final class UniPerf extends UniPerfIntf {
    private static final Object LOCK = new Object();
    private static final int REQUEST_FAILED = -1;
    private static final int REQUEST_SUCCEEDED = 0;
    private static final String TAG = "uniperf";
    private static boolean sJniReady;
    private static UniPerf sUniPerf;

    private native int nativeUniperfEvent(int i, String str, int[] iArr);

    private native int nativeUniperfGetConfig(int[] iArr, int[] iArr2);

    private native int nativeUniperfSetConfig(int i, int[] iArr, int[] iArr2);

    static {
        sJniReady = true;
        try {
            System.loadLibrary("iawareperf_jni");
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libiawareperf_jni library not found!");
            sJniReady = false;
        }
    }

    private UniPerf() {
    }

    public static UniPerf getInstance() {
        UniPerf uniPerf;
        synchronized (LOCK) {
            if (sUniPerf == null) {
                sUniPerf = new UniPerf();
            }
            uniPerf = sUniPerf;
        }
        return uniPerf;
    }

    public int uniPerfEvent(int cmdId, String pkgName, int... payload) {
        int ret;
        if (sJniReady && (ret = nativeUniperfEvent(cmdId, pkgName, payload)) == 0) {
            return ret;
        }
        return -1;
    }

    public int uniPerfSetConfig(int clientId, int[] tags, int[] values) {
        if (!sJniReady) {
            return -1;
        }
        if (tags == null || values == null) {
            Slog.e(TAG, "set config tags or values is null");
            return -1;
        }
        int tagLen = tags.length;
        if (tagLen <= 0 || tagLen > 61) {
            Slog.e(TAG, "set config tag's length is invalid");
            return -1;
        }
        int ret = nativeUniperfSetConfig(clientId, tags, values);
        if (ret != 0) {
            return -1;
        }
        return ret;
    }

    public int uniPerfGetConfig(int[] tags, int[] values) {
        if (!sJniReady) {
            return -1;
        }
        if (tags == null || values == null) {
            Slog.e(TAG, "get config tags or values is null");
            return -1;
        }
        int tagLen = tags.length;
        if (tagLen <= 0 || tagLen > 61) {
            Slog.e(TAG, "get config tag's length is invalid");
            return -1;
        }
        int ret = nativeUniperfGetConfig(tags, values);
        if (ret != 0) {
            return -1;
        }
        return ret;
    }
}
