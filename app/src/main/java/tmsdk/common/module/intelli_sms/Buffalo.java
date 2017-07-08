package tmsdk.common.module.intelli_sms;

import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;
import tmsdkobf.mz;

/* compiled from: Unknown */
public class Buffalo {
    private static boolean isLoadNativeOK;
    private volatile boolean pk;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.module.intelli_sms.Buffalo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.module.intelli_sms.Buffalo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.module.intelli_sms.Buffalo.<clinit>():void");
    }

    public Buffalo() {
        this.pk = false;
    }

    public static boolean isLoadNative() {
        if (!isLoadNativeOK) {
            isLoadNativeOK = mz.e(TMSDKContext.getApplicaionContext(), TMSDKContext.getStrFromEnvMap(TMSDKContext.BUFFALO_LIBNAME));
        }
        d.e("QQPimSecure", "Buffalo isLoadNativeOK? " + isLoadNativeOK);
        return isLoadNativeOK;
    }

    public native int nativeCheckSmsHash(String str, String str2, int i, DecomposeResult decomposeResult);

    public synchronized int nativeCheckSmsHash_c(String str, String str2, int i, DecomposeResult decomposeResult) {
        if (!this.pk) {
            return -1;
        }
        return nativeCheckSmsHash(str, str2, i, decomposeResult);
    }

    public native void nativeFinishHashChecker();

    public synchronized void nativeFinishHashChecker_c() {
        nativeFinishHashChecker();
        this.pk = false;
    }

    public native int nativeInitHashChecker(String str);

    public synchronized boolean nativeInitHashChecker_c(String str) {
        if (nativeInitHashChecker(str) == 0) {
            this.pk = true;
        }
        return this.pk;
    }
}
