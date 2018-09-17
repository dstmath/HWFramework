package defpackage;

import com.huawei.android.pushagent.utils.multicard.MultiCard;
import com.huawei.android.pushagent.utils.multicard.MultiCard.SupportMode;
import java.lang.reflect.Field;

/* renamed from: bn */
public class bn {
    private static SupportMode cf;
    private static MultiCard cg;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: bn.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: bn.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: bn.<clinit>():void");
    }

    public static MultiCard cf() {
        bn.isMultiSimEnabled();
        if (cf == SupportMode.cd) {
            cg = bp.ck();
        } else {
            cg = bo.ci();
        }
        return cg;
    }

    private static boolean cg() {
        boolean z = false;
        try {
            Object cj = bo.cj();
            z = cj != null ? ((Boolean) cj.getClass().getMethod("isMultiSimEnabled", new Class[0]).invoke(cj, new Object[0])).booleanValue() : false;
        } catch (Exception e) {
            aw.e("mutiCardFactory", "MSimTelephonyManager.getDefault().isMultiSimEnabled()?" + e.toString());
        } catch (Error e2) {
            aw.e("mutiCardFactory", "MSimTelephonyManager.getDefault().isMultiSimEnabled()" + e2.toString());
        }
        aw.i("mutiCardFactory", "isHwGeminiSupport1 " + z);
        return z;
    }

    private static boolean ch() {
        boolean z = false;
        try {
            Field declaredField = Class.forName("com.mediatek.common.featureoption.FeatureOption").getDeclaredField("MTK_GEMINI_SUPPORT");
            declaredField.setAccessible(true);
            z = declaredField.getBoolean(null);
        } catch (Exception e) {
            aw.e("mutiCardFactory", "FeatureOption.MTK_GEMINI_SUPPORT" + e.toString());
        } catch (Error e2) {
            aw.e("mutiCardFactory", "FeatureOption.MTK_GEMINI_SUPPORT" + e2.toString());
        }
        aw.i("mutiCardFactory", "isMtkGeminiSupport " + z);
        return z;
    }

    public static boolean isMultiSimEnabled() {
        boolean z = true;
        if (cf != SupportMode.ca) {
            return cf == SupportMode.cc || cf == SupportMode.cd;
        } else {
            try {
                if (bn.ch()) {
                    cf = SupportMode.cd;
                } else if (bn.cg()) {
                    cf = SupportMode.cc;
                } else {
                    cf = SupportMode.cb;
                    z = false;
                }
                return z;
            } catch (Exception e) {
                aw.e("mutiCardFactory", " " + e.toString());
                return false;
            } catch (Error e2) {
                aw.e("mutiCardFactory", "" + e2.toString());
                return false;
            }
        }
    }
}
