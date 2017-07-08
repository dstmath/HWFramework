package huawei.android.telephony.wrapper;

import android.telephony.Rlog;
import com.huawei.utils.reflect.HwReflectUtils;
import java.lang.reflect.Method;

public class DummyHuaweiTelephonyManagerWrapper implements HuaweiTelephonyManagerWrapper {
    private static final Class<?> CLASS_HuaweiTelephonyManager = null;
    private static final String LOG_TAG = "DummyHuaweiTelephonyManagerWrapper";
    private static final Method METHOD_STATIC_getDefault = null;
    private static final Method METHOD_getCardType = null;
    private static final Method METHOD_getDualCardMode = null;
    private static final Method METHOD_getSubidFromSlotId = null;
    private static DummyHuaweiTelephonyManagerWrapper mInstance;
    private Object mHuaweiTelephonyManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.telephony.wrapper.DummyHuaweiTelephonyManagerWrapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.telephony.wrapper.DummyHuaweiTelephonyManagerWrapper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.telephony.wrapper.DummyHuaweiTelephonyManagerWrapper.<clinit>():void");
    }

    public static DummyHuaweiTelephonyManagerWrapper getInstance() {
        try {
            mInstance.mHuaweiTelephonyManager = HwReflectUtils.invoke(null, METHOD_STATIC_getDefault, new Object[0]);
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_STATIC_getDefault cause exception!" + e.toString());
            mInstance.mHuaweiTelephonyManager = null;
        }
        return mInstance;
    }

    public int getSubidFromSlotId(int slotId) {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mHuaweiTelephonyManager, METHOD_getSubidFromSlotId, Integer.valueOf(slotId))).intValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_getSubidFromSlotId cause exception!" + e.toString());
            return slotId;
        }
    }

    public int getDualCardMode() {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mHuaweiTelephonyManager, METHOD_getDualCardMode, new Object[0])).intValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_getDualCardMode cause exception!" + e.toString());
            return -1;
        }
    }

    public int getCardType(int i) {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mHuaweiTelephonyManager, METHOD_getCardType, Integer.valueOf(i))).intValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_getCardType cause exception!" + e.toString());
            return -1;
        }
    }
}
