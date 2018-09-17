package tmsdk.common.tcc;

import tmsdk.common.TMSDKContext;
import tmsdk.common.tcc.DeepCleanEngine.Callback;
import tmsdkobf.ma;
import tmsdkobf.py;
import tmsdkobf.py.a;
import tmsdkobf.qa;

public class SdcardScannerFactory {
    public static final long FLAG_GET_ALL_FILE = 8;
    public static final long FLAG_NEED_BASIC_INFO = 2;
    public static final long FLAG_NEED_EXTRA_INFO = 4;
    public static final long FLAG_SCAN_WIDE = 16;
    public static final int TYPE_QSCANNER = 1;
    public static boolean isLoadNativeOK;

    static {
        isLoadNativeOK = false;
        isLoadNativeOK = ma.f(TMSDKContext.getApplicaionContext(), "dce-1.1.17-mfr");
    }

    public static DeepCleanEngine getDeepCleanEngine(Callback callback) {
        return getDeepCleanEngine(callback, 0);
    }

    public static DeepCleanEngine getDeepCleanEngine(Callback callback, int i) {
        if (!isLoadNativeOK) {
            return null;
        }
        DeepCleanEngine deepCleanEngine = new DeepCleanEngine(callback);
        return !deepCleanEngine.init(i) ? null : deepCleanEngine;
    }

    public static QSdcardScanner getQSdcardScanner(long j, a aVar, qa qaVar) {
        QSdcardScanner qSdcardScanner = (QSdcardScanner) getScanner(1, j, qaVar);
        if (qSdcardScanner == null) {
            return null;
        }
        qSdcardScanner.setListener(aVar);
        return qSdcardScanner;
    }

    private static py getScanner(int i, long j, Object obj) {
        switch (i) {
            case 1:
                long nativeAllocate = nativeAllocate(i, j);
                return nativeAllocate == 0 ? null : new QSdcardScanner(nativeAllocate, i, j, obj);
            default:
                return null;
        }
    }

    private static native long nativeAllocate(int i, long j);
}
