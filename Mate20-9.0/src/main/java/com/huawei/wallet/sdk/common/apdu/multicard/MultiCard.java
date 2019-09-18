package com.huawei.wallet.sdk.common.apdu.multicard;

import com.huawei.wallet.sdk.common.log.LogC;
import java.lang.reflect.InvocationTargetException;

public abstract class MultiCard {
    public static final int CARD_INVALID = 0;
    public static final int CARD_NOT_INSERT = 2;
    public static final int CARD_VALID = 1;
    public static final int DSDS_MODE_CDMA_GSM = 1;
    public static final int DSDS_MODE_SINGLE = 0;
    public static final int DSDS_MODE_TDSCDMA_GSM = 3;
    public static final int DSDS_MODE_UMTS_GSM = 2;
    public static final String FEATURE_ENABLE_MMS = "enableMMS";

    public enum SupportMode {
        MODE_SUPPORT_UNKNOWN,
        MODE_NOT_SUPPORT_GEMINI,
        MODE_SUPPORT_HW_GEMINI,
        MODE_SUPPORT_MTK_GEMINI
    }

    /* access modifiers changed from: package-private */
    public abstract int getDefaultSubscription();

    /* access modifiers changed from: package-private */
    public abstract String getTelephoneManagerName();

    public String getDeviceId(int sub) {
        String deviceId = "";
        Class<?>[] claArray = {Integer.TYPE};
        Object[] objArray = {Integer.valueOf(sub)};
        try {
            Object object = getDefaultTelephonyManagerEx(getTelephoneManagerName());
            if (object != null) {
                deviceId = (String) object.getClass().getMethod("getDeviceId", claArray).invoke(object, objArray);
            }
        } catch (NoSuchMethodException e) {
            LogC.w("MultiCard", "getDeviceId NoSuchMethodException", false);
        } catch (IllegalAccessException e2) {
            LogC.w("MultiCard", "getDeviceId IllegalAccessException", false);
        } catch (IllegalArgumentException e3) {
            LogC.w("MultiCard", "getDeviceId IllegalArgumentException", false);
        } catch (InvocationTargetException e4) {
            LogC.w("MultiCard", "getDeviceId InvocationTargetException", false);
        }
        if (deviceId == null) {
            return "";
        }
        return deviceId;
    }

    public int getSimState(int sub) {
        Class<?>[] clsArray = {Integer.TYPE};
        Object[] objArray = {Integer.valueOf(sub)};
        try {
            Object object = getDefaultTelephonyManagerEx(getTelephoneManagerName());
            if (object != null) {
                return ((Integer) object.getClass().getDeclaredMethod("getSimState", clsArray).invoke(object, objArray)).intValue();
            }
            return 0;
        } catch (NoSuchMethodException e) {
            LogC.w("MultiCard", "getSimState NoSuchMethodException", false);
            return 0;
        } catch (IllegalAccessException e2) {
            LogC.w("MultiCard", "getSimState IllegalAccessException", false);
            return 0;
        } catch (IllegalArgumentException e3) {
            LogC.w("MultiCard", "getSimState IllegalArgumentException", false);
            return 0;
        } catch (InvocationTargetException e4) {
            LogC.w("MultiCard", "getSimState InvocationTargetException", false);
            return 0;
        }
    }

    public Object getDefaultTelephonyManagerEx(String className) {
        try {
            Class<?> TelephonyManagerExClass = Class.forName(className);
            return TelephonyManagerExClass.getDeclaredMethod("getDefault", new Class[0]).invoke(TelephonyManagerExClass, new Object[0]);
        } catch (ClassNotFoundException e) {
            LogC.w("MultiCard", "getDefaultTelephonyManagerEx ClassNotFoundException has error", false);
            return null;
        } catch (NoSuchMethodException e2) {
            LogC.w("MultiCard", "getDefaultTelephonyManagerEx NoSuchMethodException", false);
            return null;
        } catch (IllegalAccessException e3) {
            LogC.w("MultiCard", "getDefaultTelephonyManagerEx IllegalAccessException", false);
            return null;
        } catch (IllegalArgumentException e4) {
            LogC.w("MultiCard", "getDefaultTelephonyManagerEx IllegalArgumentException", false);
            return null;
        } catch (InvocationTargetException e5) {
            LogC.w("MultiCard", "getDefaultTelephonyManagerEx InvocationTargetException", false);
            return null;
        }
    }
}
