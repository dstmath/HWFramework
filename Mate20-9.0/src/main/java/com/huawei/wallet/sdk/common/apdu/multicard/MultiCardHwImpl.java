package com.huawei.wallet.sdk.common.apdu.multicard;

import com.huawei.wallet.sdk.common.log.LogC;
import java.lang.reflect.InvocationTargetException;

public class MultiCardHwImpl extends MultiCard {
    private static final int SUB0 = -1;
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile MultiCardHwImpl instance;

    public static MultiCardHwImpl getInstance() {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new MultiCardHwImpl();
                }
            }
        }
        return instance;
    }

    public int getDefaultSubscription() {
        try {
            Object object = getDefaultMSimTelephonyManager();
            if (object != null) {
                return ((Integer) object.getClass().getMethod("getDefaultSubscription", new Class[0]).invoke(object, new Object[0])).intValue();
            }
            return 0;
        } catch (IllegalAccessException e) {
            LogC.v(LogC.LOG_HWSDK_TAG, " getDefaultSubscription wrong IllegalAccessException ", false);
            return -1;
        } catch (IllegalArgumentException e2) {
            LogC.v(LogC.LOG_HWSDK_TAG, " getDefaultSubscription wrong IllegalArgumentException", false);
            return -1;
        } catch (InvocationTargetException e3) {
            LogC.v(LogC.LOG_HWSDK_TAG, " getDefaultSubscription wrong InvocationTargetException", false);
            return -1;
        } catch (NoSuchMethodException e4) {
            LogC.v(LogC.LOG_HWSDK_TAG, " getDefaultSubscription wrong NoSuchMethodException", false);
            return -1;
        }
    }

    public int getSimState(int subscrption) {
        if (subscrption == -1) {
            return 5;
        }
        return super.getSimState(subscrption);
    }

    /* access modifiers changed from: package-private */
    public String getTelephoneManagerName() {
        return "android.telephony.MSimTelephonyManager";
    }

    public Object getDefaultMSimTelephonyManager() {
        return getDefaultTelephonyManagerEx(getTelephoneManagerName());
    }
}
