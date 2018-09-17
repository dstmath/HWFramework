package android.iawareperf;

import android.util.Slog;

public final class UniPerf extends UniPerfIntf {
    private static final int REQUEST_FAILED = -1;
    private static final int REQUEST_SUCCEEDED = 0;
    private static final String TAG = "uniperf";
    private static UniPerf sUniPerf;

    private native int native_uniperf_event(int i, String str, int[] iArr);

    private native int native_uniperf_get_config(int[] iArr, int[] iArr2);

    private native int native_uniperf_set_config(int i, int[] iArr, int[] iArr2);

    private UniPerf() {
    }

    public static synchronized UniPerf getInstance() {
        UniPerf uniPerf;
        synchronized (UniPerf.class) {
            if (sUniPerf == null) {
                sUniPerf = new UniPerf();
            }
            uniPerf = sUniPerf;
        }
        return uniPerf;
    }

    public int uniPerfEvent(int cmdId, String pkgName, int... payload) {
        int ret = native_uniperf_event(cmdId, pkgName, payload);
        if (ret != 0) {
            return -1;
        }
        return ret;
    }

    public int uniPerfSetConfig(int clientId, int[] tags, int[] values) {
        if (tags == null || values == null) {
            Slog.e(TAG, "set config tags or values is null");
            return -1;
        }
        int tagLen = tags.length;
        if (tagLen <= 0 || tagLen > 43) {
            Slog.e(TAG, "set config tag's length is invalid");
            return -1;
        }
        int ret = native_uniperf_set_config(clientId, tags, values);
        if (ret != 0) {
            ret = -1;
        }
        return ret;
    }

    public int uniPerfGetConfig(int[] tags, int[] values) {
        if (tags == null || values == null) {
            Slog.e(TAG, "get config tags or values is null");
            return -1;
        }
        int tagLen = tags.length;
        if (tagLen <= 0 || tagLen > 43) {
            Slog.e(TAG, "get config tag's length is invalid");
            return -1;
        }
        int ret = native_uniperf_get_config(tags, values);
        if (ret != 0) {
            ret = -1;
        }
        return ret;
    }
}
