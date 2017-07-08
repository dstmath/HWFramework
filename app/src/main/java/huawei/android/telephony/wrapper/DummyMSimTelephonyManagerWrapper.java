package huawei.android.telephony.wrapper;

import android.telephony.Rlog;
import com.huawei.utils.reflect.HwReflectUtils;
import java.lang.reflect.Method;

public class DummyMSimTelephonyManagerWrapper implements MSimTelephonyManagerWrapper {
    private static final Class<?> CLASS_MSimTelephonyManager = null;
    private static final String LOG_TAG = "DummyMSimTelephonyManagerWrapper";
    private static final Method METHOD_STATIC_getDefault = null;
    private static final Method METHOD_getCurrentPhoneType = null;
    private static final Method METHOD_getDefaultSubscription = null;
    private static final Method METHOD_getMmsAutoSetDataSubscription = null;
    private static final Method METHOD_getNetworkType = null;
    private static final Method METHOD_getPhoneCount = null;
    private static final Method METHOD_getPreferredDataSubscription = null;
    private static final Method METHOD_getVoiceMailNumber = null;
    private static final Method METHOD_hasIccCard = null;
    private static final Method METHOD_isMultiSimEnabled = null;
    private static final Method METHOD_isNetworkRoaming = null;
    private static final Method METHOD_setMmsAutoSetDataSubscription = null;
    private static final Method METHOD_setPreferredDataSubscription = null;
    private static DummyMSimTelephonyManagerWrapper mInstance;
    private Object mMSimTelephonyManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.telephony.wrapper.DummyMSimTelephonyManagerWrapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.telephony.wrapper.DummyMSimTelephonyManagerWrapper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.telephony.wrapper.DummyMSimTelephonyManagerWrapper.<clinit>():void");
    }

    public static DummyMSimTelephonyManagerWrapper getInstance() {
        try {
            mInstance.mMSimTelephonyManager = HwReflectUtils.invoke(null, METHOD_STATIC_getDefault, new Object[0]);
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_STATIC_getDefault cause exception!" + e.toString());
            mInstance.mMSimTelephonyManager = null;
        }
        return mInstance;
    }

    public boolean isMultiSimEnabled() {
        try {
            return ((Boolean) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_isMultiSimEnabled, new Object[0])).booleanValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_isMultiSimEnabled cause exception!" + e.toString());
            return false;
        }
    }

    public int getDefaultSubscription() {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getDefaultSubscription, new Object[0])).intValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_getDefaultSubscription cause exception!" + e.toString());
            return 0;
        }
    }

    public int getPhoneCount() {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getPhoneCount, new Object[0])).intValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_getPhoneCount cause exception!" + e.toString());
            return 1;
        }
    }

    public boolean hasIccCard(int subscription) {
        try {
            return ((Boolean) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_hasIccCard, Integer.valueOf(subscription))).booleanValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_hasIccCard cause exception!" + e.toString());
            return false;
        }
    }

    public boolean setMmsAutoSetDataSubscription(int subscription) {
        try {
            return ((Boolean) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_setMmsAutoSetDataSubscription, Integer.valueOf(subscription))).booleanValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_setMmsAutoSetDataSubscription cause exception!" + e.toString());
            return false;
        }
    }

    public boolean setPreferredDataSubscription(int subscription) {
        try {
            return ((Boolean) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_setPreferredDataSubscription, Integer.valueOf(subscription))).booleanValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_setPreferredDataSubscription cause exception!" + e.toString());
            return false;
        }
    }

    public int getPreferredDataSubscription() {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getPreferredDataSubscription, new Object[0])).intValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_getPreferredDataSubscription cause exception!" + e.toString());
            return 0;
        }
    }

    public int getMmsAutoSetDataSubscription() {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getMmsAutoSetDataSubscription, new Object[0])).intValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_getMmsAutoSetDataSubscription cause exception!" + e.toString());
            return 0;
        }
    }

    public boolean isNetworkRoaming(int subscription) {
        try {
            return ((Boolean) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_isNetworkRoaming, Integer.valueOf(subscription))).booleanValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_isNetworkRoaming cause exception!" + e.toString());
            return false;
        }
    }

    public int getNetworkType(int subscription) {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getNetworkType, Integer.valueOf(subscription))).intValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_getNetworkType cause exception!" + e.toString());
            return 0;
        }
    }

    public String getVoiceMailNumber(int subsription) {
        try {
            return (String) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getVoiceMailNumber, Integer.valueOf(subsription));
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_getVoiceMailNumber cause exception!" + e.toString());
            return null;
        }
    }

    public int getCurrentPhoneType(int subscription) {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getCurrentPhoneType, Integer.valueOf(subscription))).intValue();
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "METHOD_getCurrentPhoneType cause exception!" + e.toString());
            return 0;
        }
    }
}
