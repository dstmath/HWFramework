package huawei.android.cover;

import huawei.cust.HwCustUtils;

public class HwCustCoverManagerUtils {
    private static HwCustCoverManagerUtils mHwCustCoverManagerUtils;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.cover.HwCustCoverManagerUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.cover.HwCustCoverManagerUtils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.cover.HwCustCoverManagerUtils.<clinit>():void");
    }

    public static synchronized HwCustCoverManagerUtils getDefault() {
        HwCustCoverManagerUtils hwCustCoverManagerUtils;
        synchronized (HwCustCoverManagerUtils.class) {
            if (mHwCustCoverManagerUtils == null) {
                mHwCustCoverManagerUtils = (HwCustCoverManagerUtils) HwCustUtils.createObj(HwCustCoverManagerUtils.class, new Object[0]);
            }
            hwCustCoverManagerUtils = mHwCustCoverManagerUtils;
        }
        return hwCustCoverManagerUtils;
    }

    public boolean isSupportSmartCover() {
        return false;
    }
}
