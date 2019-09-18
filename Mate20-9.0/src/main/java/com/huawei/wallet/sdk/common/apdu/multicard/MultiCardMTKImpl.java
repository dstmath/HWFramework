package com.huawei.wallet.sdk.common.apdu.multicard;

import com.huawei.wallet.sdk.common.log.LogC;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class MultiCardMTKImpl extends MultiCard {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile MultiCardMTKImpl instance;

    public static MultiCardMTKImpl getInstance() {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new MultiCardMTKImpl();
                }
            }
        }
        return instance;
    }

    private MultiCardMTKImpl() {
    }

    public int getDefaultSubscription() {
        return getDefaultSimMTK();
    }

    private static int getDefaultSimMTK() {
        try {
            Class<?> mSimTelephonyManagerClazz = Class.forName("android.telephony.TelephonyManager");
            Method getDefaultSim = mSimTelephonyManagerClazz.getDeclaredMethod("getDefaultSim", new Class[]{null});
            Object instance2 = mSimTelephonyManagerClazz.getDeclaredMethod("getDefault", new Class[]{null}).invoke(null, null);
            getDefaultSim.setAccessible(true);
            return ((Integer) getDefaultSim.invoke(instance2, null)).intValue();
        } catch (ClassNotFoundException e) {
            LogC.w(LogC.LOG_HWSDK_TAG, "getDefaultSimMTK ClassNotFoundException has error", false);
            return -1;
        } catch (NoSuchMethodException e2) {
            LogC.w(LogC.LOG_HWSDK_TAG, "getDefaultSimMTK NoSuchMethodException", false);
            return -1;
        } catch (IllegalAccessException e3) {
            LogC.w(LogC.LOG_HWSDK_TAG, "getDefaultSimMTK IllegalAccessException", false);
            return -1;
        } catch (IllegalArgumentException e4) {
            LogC.w(LogC.LOG_HWSDK_TAG, "getDefaultSimMTK IllegalArgumentException", false);
            return -1;
        } catch (InvocationTargetException e5) {
            LogC.w(LogC.LOG_HWSDK_TAG, "getDefaultSimMTK InvocationTargetException", false);
            return -1;
        } catch (NoClassDefFoundError e6) {
            LogC.w(LogC.LOG_HWSDK_TAG, "getDefaultSimMTK has error", false);
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public String getTelephoneManagerName() {
        return "com.mediatek.telephony.TelephonyManagerEx";
    }
}
