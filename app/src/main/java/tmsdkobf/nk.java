package tmsdkobf;

import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.Buffalo;
import tmsdk.common.module.intelli_sms.DecomposeResult;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class nk {
    private static nk Cu;
    private Buffalo Ct;
    private int mRefCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.nk.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.nk.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.nk.<clinit>():void");
    }

    private nk() {
        this.mRefCount = 0;
        this.Ct = null;
        d.g("QQPimSecure", "BuffaloImpl 00");
    }

    public static nk fk() {
        if (Cu == null) {
            synchronized (nk.class) {
                if (Cu == null) {
                    Cu = new nk();
                }
            }
        }
        return Cu;
    }

    public String a(String str, String str2, int i) {
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                Buffalo buffalo = this.Ct;
                DecomposeResult decomposeResult = new DecomposeResult();
                if (buffalo.nativeCheckSmsHash_c(str, str2, i, decomposeResult) != 0 || decomposeResult.strResult == null) {
                    return null;
                }
                mb.cq(decomposeResult.strResult);
                return decomposeResult.strResult;
            }
            d.e("QQPimSecure", "BumbleBeeImpl checkSms mRefCount==0");
            return null;
        }
    }

    public void fl() {
        d.g("QQPimSecure", "BuffaloImpl 01");
        String a = ms.a(TMSDKContext.getApplicaionContext(), "rule.dat", null);
        if (a != null) {
            synchronized (nl.class) {
                if (this.mRefCount <= 0) {
                    this.Ct = new Buffalo();
                    this.Ct.nativeInitHashChecker_c(a);
                    this.mRefCount = 1;
                    d.g("QQPimSecure", "BuffaloImpl 02");
                    return;
                }
                this.mRefCount++;
            }
        }
    }

    public void fm() {
        d.g("QQPimSecure", "BuffaloImpl 03");
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                this.mRefCount--;
                if (this.mRefCount <= 0) {
                    if (this.Ct != null) {
                        this.Ct.nativeFinishHashChecker_c();
                    }
                    Cu = null;
                }
                d.g("QQPimSecure", "BuffaloImpl 04");
                return;
            }
            return;
        }
    }
}
