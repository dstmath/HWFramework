package tmsdk.common.module.qscanner.impl;

import android.content.Context;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.utils.f;
import tmsdkobf.fd;
import tmsdkobf.ff;
import tmsdkobf.fg;
import tmsdkobf.fn;
import tmsdkobf.lu;
import tmsdkobf.ma;

public class AmScannerV2 {
    private static boolean BP;
    private long object = 0;

    static {
        BP = false;
        try {
            BP = ma.f(TMSDKContext.getApplicaionContext(), "ams-1.2.9-mfr");
            if (!BP) {
                f.g("QScannerMgr-AmScannerV2", "load ams so failed");
            }
        } catch (Throwable th) {
            f.b("QScannerMgr-AmScannerV2", "load ams so exception: " + th, th);
        }
    }

    protected AmScannerV2(Context context, String str) {
        this.object = newObject(context, str);
        f.d("QScannerMgr-AmScannerV2", "[native]newObject:[" + this.object + "]");
        if (this.object == 0) {
            throw new OutOfMemoryError();
        }
    }

    public static synchronized int a(Context context, String str, fg fgVar, List<ff> list) {
        synchronized (AmScannerV2.class) {
            if (fgVar == null || list == null) {
                f.g("QScannerMgr-AmScannerV2", "updateBase, virusServerInfo == null || virusInfoList == null");
                return -6;
            }
            fn fnVar = new fn();
            fnVar.B("UTF-8");
            fnVar.m();
            fnVar.put("vsi", fgVar);
            byte[] l = fnVar.l();
            fnVar.k();
            fnVar.put("vil", list);
            byte[] l2 = fnVar.l();
            f.d("QScannerMgr-AmScannerV2", "[native]nativeUpdateMalwareInfoBytes, amfFile:[" + str + "]");
            int nativeUpdateMalwareInfoBytes = nativeUpdateMalwareInfoBytes(context, str, l, l2);
            return nativeUpdateMalwareInfoBytes;
        }
    }

    public static synchronized d b(a aVar) {
        synchronized (AmScannerV2.class) {
            if (aVar != null) {
                fn fnVar = new fn();
                fnVar.B("UTF-8");
                fnVar.m();
                fnVar.put("ak", aVar);
                byte[] l = fnVar.l();
                AtomicReference atomicReference = new AtomicReference();
                f.d("QScannerMgr-AmScannerV2", "[native]extractApkInfo");
                int extractApkInfo = extractApkInfo(l, atomicReference);
                if (extractApkInfo == 0) {
                    byte[] bArr = (byte[]) atomicReference.get();
                    if (bArr != null) {
                        fnVar.k();
                        fnVar.b(bArr);
                        d dVar = (d) fnVar.a("qsr", new d());
                        return dVar;
                    }
                    f.g("QScannerMgr-AmScannerV2", "extractApkInfo(), return null");
                    return null;
                }
                f.g("QScannerMgr-AmScannerV2", "extractApkInfo(), err: " + extractApkInfo);
                return null;
            }
            f.g("QScannerMgr-AmScannerV2", "extractApkInfo, apkKey == null!");
            return null;
        }
    }

    private static native void deleteObject(long j);

    private static native int extractApkInfo(byte[] bArr, AtomicReference<byte[]> atomicReference);

    public static synchronized fd g(Context context, String str) {
        synchronized (AmScannerV2.class) {
            if (str != null) {
                fn fnVar = new fn();
                fnVar.B("UTF-8");
                fnVar.m();
                AtomicReference atomicReference = new AtomicReference();
                try {
                    f.d("QScannerMgr-AmScannerV2", "[native]nativeLoadAmfHeaderBytes, amfFile:[" + str + "]");
                    if (nativeLoadAmfHeaderBytes(context, str, atomicReference) == 0) {
                        byte[] bArr = (byte[]) atomicReference.get();
                        if (bArr != null) {
                            fnVar.b(bArr);
                            fd fdVar = (fd) fnVar.a("vci", new fd());
                            return fdVar;
                        }
                    }
                } catch (Throwable th) {
                    f.e("QScannerMgr-AmScannerV2", "loadAmfHeader, e:[" + th + "]");
                }
            }
        }
        return null;
    }

    public static native int getOpcode(byte[] bArr, AtomicReference<byte[]> atomicReference);

    private static native int initScanner(long j);

    public static boolean isSupported() {
        return BP;
    }

    private static native int nativeLoadAmfHeaderBytes(Context context, String str, AtomicReference<byte[]> atomicReference);

    private static native int nativeUpdateMalwareInfoBytes(Context context, String str, byte[] bArr, byte[] bArr2);

    private static native long newObject(Context context, String str);

    private static native int scanApkBytes(long j, byte[] bArr, AtomicReference<byte[]> atomicReference);

    public synchronized d a(a aVar) {
        d dVar;
        if (aVar != null) {
            f.d("QScannerMgr-AmScannerV2", "scanApk, [" + aVar.nf + "][" + aVar.softName + "][" + aVar.bZ + "][" + aVar.path + "]");
            fn fnVar = new fn();
            fnVar.B("UTF-8");
            fnVar.m();
            fnVar.put("ak", aVar);
            AtomicReference atomicReference = new AtomicReference();
            f.d("QScannerMgr-AmScannerV2", "[native]scanApkBytes, object:[" + this.object + "]");
            int scanApkBytes = scanApkBytes(this.object, fnVar.l(), atomicReference);
            if (scanApkBytes == 0) {
                byte[] bArr = (byte[]) atomicReference.get();
                if (bArr != null) {
                    fnVar.k();
                    fnVar.b(bArr);
                    dVar = null;
                    try {
                        dVar = (d) fnVar.a("qsr", new d());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    f.g("QScannerMgr-AmScannerV2", "scanApk, scanApkBytes() return null");
                    return null;
                }
            }
            f.g("QScannerMgr-AmScannerV2", "scanApk, native scanApkBytes() err: " + scanApkBytes);
            return null;
        }
        f.g("QScannerMgr-AmScannerV2", "scanApk, apkKey == null!");
        return null;
        return dVar;
    }

    protected synchronized void exit() {
        if (this.object != 0) {
            f.d("QScannerMgr-AmScannerV2", "[native]deleteObject, object:[" + this.object + "]");
            deleteObject(this.object);
            this.object = 0;
        }
    }

    protected synchronized boolean fl() {
        int initScanner = initScanner(this.object);
        f.d("QScannerMgr-AmScannerV2", "[native]initScanner:[" + initScanner + "]");
        if (initScanner == 0) {
            return true;
        }
        String b = lu.b(TMSDKContext.getApplicaionContext(), UpdateConfig.VIRUS_BASE_NAME, null);
        f.g("QScannerMgr-AmScannerV2", "amf file error, delete:[" + b + "]");
        lu.bK(b);
        return false;
    }
}
