package com.android.server;

import android.content.Context;
import android.util.Slog;
import huawei.android.os.HwProtectArea;

public class HwProtectAreaService {
    static final String TAG = "HwProtectAreaService";
    private static volatile HwProtectAreaService mInstance = null;
    private Context mContext;
    private final Object mLock = new Object();

    private static native void nativeProtectAreaClassInit();

    private static native int nativeReadProtectArea(String str, HwProtectArea hwProtectArea, int i);

    private static native int nativeWriteProtectArea(String str, HwProtectArea hwProtectArea, int i, String str2);

    public HwProtectAreaService(Context context) {
        this.mContext = context;
        try {
            synchronized (this.mLock) {
                nativeProtectAreaClassInit();
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary ClassInit failed >>>>>" + e);
        } catch (Exception e2) {
            Slog.e(TAG, "libarary ClassInit failed >>>>>" + e2);
        }
    }

    public static synchronized HwProtectAreaService getInstance(Context context) {
        HwProtectAreaService hwProtectAreaService;
        synchronized (HwProtectAreaService.class) {
            if (mInstance == null) {
                mInstance = new HwProtectAreaService(context);
            }
            hwProtectAreaService = mInstance;
        }
        return hwProtectAreaService;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004b, code lost:
        return -1;
     */
    private int nativeReadProtectAreaJava(String optItem, int readBufLen, String[] readBuf, int[] errno) {
        try {
            synchronized (this.mLock) {
                if (optItem == null || readBufLen < 0 || readBuf == null || errno == null) {
                    Slog.e(TAG, "nativeReadProtectAreaJava:parameter error !");
                    if (readBuf != null) {
                        readBuf[0] = "error";
                    }
                    if (errno != null) {
                        errno[0] = -1;
                    }
                } else {
                    HwProtectArea protectArea = new HwProtectArea(optItem);
                    int ret = nativeReadProtectArea(optItem, protectArea, readBufLen);
                    if (-1 == ret) {
                        Slog.e(TAG, "nativeReadProtectAreaJava:error ret is -1 !");
                        readBuf[0] = "error";
                        errno[0] = -1;
                        return ret;
                    }
                    readBuf[0] = protectArea.getReadBuf();
                    errno[0] = protectArea.getErrno();
                    return ret;
                }
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary ReadProtectArea failed >>>>>" + e);
        } catch (Exception e2) {
            Slog.e(TAG, "libarary ReadProtectArea failed >>>>>" + e2);
        }
        return -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0039, code lost:
        return -1;
     */
    private int nativeWriteProtectAreaJava(String optItem, int writeLen, String writeBuf, int[] errno) {
        try {
            synchronized (this.mLock) {
                if (optItem == null || writeBuf == null || errno == null) {
                    Slog.e(TAG, "nativeWriteProtectAreaJava:parameter error !");
                    if (errno != null) {
                        errno[0] = -1;
                    }
                } else {
                    HwProtectArea protectArea = new HwProtectArea(optItem);
                    int ret = nativeWriteProtectArea(optItem, protectArea, writeLen, writeBuf);
                    if (-1 == ret) {
                        Slog.e(TAG, "nativeWriteProtectArea:error ret is -1 !");
                        errno[0] = -1;
                        return ret;
                    }
                    errno[0] = protectArea.getErrno();
                    return ret;
                }
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libarary WriteProtectArea failed >>>>>" + e);
        } catch (Exception e2) {
            Slog.e(TAG, "libarary WriteProtectArea failed >>>>>" + e2);
        }
        return -1;
    }

    public int readProtectArea(String optItem, int readBufLen, String[] readBuf, int[] errorNum) {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.PROTECTAREA", null);
        return nativeReadProtectAreaJava(optItem, readBufLen, readBuf, errorNum);
    }

    public int writeProtectArea(String optItem, int writeLen, String writeBuf, int[] errorNum) {
        if (writeBuf == null || writeBuf.length() <= writeLen) {
            this.mContext.enforceCallingOrSelfPermission("com.huawei.android.permission.PROTECTAREA", null);
            return nativeWriteProtectAreaJava(optItem, writeLen, writeBuf, errorNum);
        }
        Slog.d(TAG, "writeProtectArea:writeBuf.length():" + writeBuf.length() + ", writeLen:" + writeLen);
        return -1;
    }
}
