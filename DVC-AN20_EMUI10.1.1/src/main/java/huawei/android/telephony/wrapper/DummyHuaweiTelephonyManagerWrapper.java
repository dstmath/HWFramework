package huawei.android.telephony.wrapper;

import com.huawei.android.telephony.RlogEx;
import com.huawei.utils.reflect.HwReflectUtils;
import java.lang.reflect.Method;

public class DummyHuaweiTelephonyManagerWrapper implements HuaweiTelephonyManagerWrapper {
    private static final Class<?> CLASS_HuaweiTelephonyManager = HwReflectUtils.getClass("com.huawei.telephony.HuaweiTelephonyManager");
    private static final String LOG_TAG = "DummyHuaweiTelephonyManagerWrapper";
    private static final Method METHOD_STATIC_getDefault = HwReflectUtils.getMethod(CLASS_HuaweiTelephonyManager, "getDefault", new Class[0]);
    private static final Method METHOD_getCardType = HwReflectUtils.getMethod(CLASS_HuaweiTelephonyManager, "getCardType", new Class[]{Integer.TYPE});
    private static final Method METHOD_getDualCardMode = HwReflectUtils.getMethod(CLASS_HuaweiTelephonyManager, "getDualCardMode", new Class[0]);
    private static final Method METHOD_getSubidFromSlotId = HwReflectUtils.getMethod(CLASS_HuaweiTelephonyManager, "getSubidFromSlotId", new Class[]{Integer.TYPE});
    private static DummyHuaweiTelephonyManagerWrapper mInstance = new DummyHuaweiTelephonyManagerWrapper();
    private Object mHuaweiTelephonyManager;

    public static DummyHuaweiTelephonyManagerWrapper getInstance() {
        try {
            mInstance.mHuaweiTelephonyManager = HwReflectUtils.invoke((Object) null, METHOD_STATIC_getDefault, new Object[0]);
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_STATIC_getDefault cause exception!");
            mInstance.mHuaweiTelephonyManager = null;
        }
        return mInstance;
    }

    @Override // huawei.android.telephony.wrapper.HuaweiTelephonyManagerWrapper
    public int getSubidFromSlotId(int slotId) {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mHuaweiTelephonyManager, METHOD_getSubidFromSlotId, new Object[]{Integer.valueOf(slotId)})).intValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_getSubidFromSlotId cause exception!");
            return slotId;
        }
    }

    @Override // huawei.android.telephony.wrapper.HuaweiTelephonyManagerWrapper
    public int getDualCardMode() {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mHuaweiTelephonyManager, METHOD_getDualCardMode, new Object[0])).intValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_getDualCardMode cause exception!");
            return -1;
        }
    }

    @Override // huawei.android.telephony.wrapper.HuaweiTelephonyManagerWrapper
    public int getCardType(int i) {
        try {
            return ((Integer) HwReflectUtils.invoke(mInstance.mHuaweiTelephonyManager, METHOD_getCardType, new Object[]{Integer.valueOf(i)})).intValue();
        } catch (Exception e) {
            RlogEx.e(LOG_TAG, "METHOD_getCardType cause exception!");
            return -1;
        }
    }
}
