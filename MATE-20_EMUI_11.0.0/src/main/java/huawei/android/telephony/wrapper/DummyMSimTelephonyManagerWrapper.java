package huawei.android.telephony.wrapper;

import com.huawei.android.telephony.RlogEx;
import com.huawei.utils.reflect.HwReflectUtils;
import java.lang.reflect.Method;

public class DummyMSimTelephonyManagerWrapper implements MSimTelephonyManagerWrapper {
    private static final Class<?> CLASS_MSimTelephonyManager = HwReflectUtils.getClass("android.telephony.MSimTelephonyManager");
    private static final String LOG_TAG = "DummyMSimTelephonyManagerWrapper";
    private static final Method METHOD_STATIC_getDefault = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "getDefault", new Class[0]);
    private static final Method METHOD_getCurrentPhoneType = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "getCurrentPhoneType", new Class[]{Integer.TYPE});
    private static final Method METHOD_getDefaultSubscription = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "getDefaultSubscription", new Class[0]);
    private static final Method METHOD_getMmsAutoSetDataSubscription = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "getMmsAutoSetDataSubscription", new Class[0]);
    private static final Method METHOD_getNetworkType = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "getNetworkType", new Class[]{Integer.TYPE});
    private static final Method METHOD_getPhoneCount = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "getPhoneCount", new Class[0]);
    private static final Method METHOD_getPreferredDataSubscription = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "getPreferredDataSubscription", new Class[0]);
    private static final Method METHOD_getVoiceMailNumber = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "getVoiceMailNumber", new Class[]{Integer.TYPE});
    private static final Method METHOD_hasIccCard = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "hasIccCard", new Class[]{Integer.TYPE});
    private static final Method METHOD_isMultiSimEnabled = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "isMultiSimEnabled", new Class[0]);
    private static final Method METHOD_isNetworkRoaming = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "isNetworkRoaming", new Class[]{Integer.TYPE});
    private static final Method METHOD_setMmsAutoSetDataSubscription = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "setMmsAutoSetDataSubscription", new Class[]{Integer.TYPE});
    private static final Method METHOD_setPreferredDataSubscription = HwReflectUtils.getMethod(CLASS_MSimTelephonyManager, "setPreferredDataSubscription", new Class[]{Integer.TYPE});
    private static DummyMSimTelephonyManagerWrapper mInstance = new DummyMSimTelephonyManagerWrapper();
    private Object mMSimTelephonyManager;

    public static DummyMSimTelephonyManagerWrapper getInstance() {
        try {
            mInstance.mMSimTelephonyManager = HwReflectUtils.invoke((Object) null, METHOD_STATIC_getDefault, new Object[0]);
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_STATIC_getDefault cause exception!");
            mInstance.mMSimTelephonyManager = null;
        }
        return mInstance;
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public boolean isMultiSimEnabled() {
        try {
            return ((Boolean) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_isMultiSimEnabled, new Object[0])).booleanValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_isMultiSimEnabled cause exception!");
            return false;
        }
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public int getDefaultSubscription() {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getDefaultSubscription, new Object[0])).intValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_getDefaultSubscription cause exception!");
            return 0;
        }
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public int getPhoneCount() {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getPhoneCount, new Object[0])).intValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_getPhoneCount cause exception!");
            return 1;
        }
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public boolean hasIccCard(int subscription) {
        try {
            return ((Boolean) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_hasIccCard, new Object[]{Integer.valueOf(subscription)})).booleanValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_hasIccCard cause exception!");
            return false;
        }
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public boolean setMmsAutoSetDataSubscription(int subscription) {
        try {
            return ((Boolean) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_setMmsAutoSetDataSubscription, new Object[]{Integer.valueOf(subscription)})).booleanValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_setMmsAutoSetDataSubscription cause exception!");
            return false;
        }
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public boolean setPreferredDataSubscription(int subscription) {
        try {
            return ((Boolean) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_setPreferredDataSubscription, new Object[]{Integer.valueOf(subscription)})).booleanValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_setPreferredDataSubscription cause exception!");
            return false;
        }
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public int getPreferredDataSubscription() {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getPreferredDataSubscription, new Object[0])).intValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_getPreferredDataSubscription cause exception!");
            return 0;
        }
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public int getMmsAutoSetDataSubscription() {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getMmsAutoSetDataSubscription, new Object[0])).intValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_getMmsAutoSetDataSubscription cause exception!");
            return 0;
        }
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public boolean isNetworkRoaming(int subscription) {
        try {
            return ((Boolean) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_isNetworkRoaming, new Object[]{Integer.valueOf(subscription)})).booleanValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_isNetworkRoaming cause exception!");
            return false;
        }
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public int getNetworkType(int subscription) {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getNetworkType, new Object[]{Integer.valueOf(subscription)})).intValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_getNetworkType cause exception!");
            return 0;
        }
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public String getVoiceMailNumber(int subsription) {
        try {
            return (String) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getVoiceMailNumber, new Object[]{Integer.valueOf(subsription)});
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_getVoiceMailNumber cause exception!");
            return null;
        }
    }

    @Override // huawei.android.telephony.wrapper.MSimTelephonyManagerWrapper
    public int getCurrentPhoneType(int subscription) {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mMSimTelephonyManager, METHOD_getCurrentPhoneType, new Object[]{Integer.valueOf(subscription)})).intValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_getCurrentPhoneType cause exception!");
            return 0;
        }
    }
}
