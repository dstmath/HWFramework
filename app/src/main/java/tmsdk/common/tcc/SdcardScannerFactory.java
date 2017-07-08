package tmsdk.common.tcc;

import tmsdk.common.tcc.DeepCleanEngine.Callback;
import tmsdkobf.qv;
import tmsdkobf.qv.a;
import tmsdkobf.qx;

/* compiled from: Unknown */
public class SdcardScannerFactory {
    public static final long FLAG_GET_ALL_FILE = 8;
    public static final long FLAG_NEED_BASIC_INFO = 2;
    public static final long FLAG_NEED_EXTRA_INFO = 4;
    public static final long FLAG_SCAN_WIDE = 16;
    public static final int TYPE_QSCANNER = 1;
    public static boolean isLoadNativeOK;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.tcc.SdcardScannerFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.tcc.SdcardScannerFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.tcc.SdcardScannerFactory.<clinit>():void");
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

    public static QSdcardScanner getQSdcardScanner(long j, a aVar, qx qxVar) {
        QSdcardScanner qSdcardScanner = (QSdcardScanner) getScanner(TYPE_QSCANNER, j, qxVar);
        if (qSdcardScanner == null) {
            return null;
        }
        qSdcardScanner.setListener(aVar);
        return qSdcardScanner;
    }

    private static qv getScanner(int i, long j, Object obj) {
        switch (i) {
            case TYPE_QSCANNER /*1*/:
                long nativeAllocate = nativeAllocate(i, j);
                return nativeAllocate == 0 ? null : new QSdcardScanner(nativeAllocate, i, j, obj);
            default:
                return null;
        }
    }

    private static native long nativeAllocate(int i, long j);
}
