package com.huawei.wallet.sdk.common.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.UserHandle;
import com.huawei.wallet.sdk.common.log.LogC;
import java.lang.reflect.InvocationTargetException;

public class ProductConfigUtil {
    private static final String EMUI50_ROOT_PROPERTY = "0";
    public static final String ESE_MANUFACTURER_HISEE = "02";
    public static final String ESE_MANUFACTURER_NXP = "01";
    private static boolean isChecked = false;
    private static boolean isRooted = false;

    public static String[] getProductConfig() {
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            String value = (String) classType.getDeclaredMethod("get", new Class[]{String.class}).invoke(classType, new Object[]{"ro.product.wallet.nfc"});
            LogC.i("getProductConfig, product config info: " + value, false);
            if (!StringUtil.isEmpty(value, true)) {
                return value.split("\\|");
            }
            return null;
        } catch (ClassNotFoundException e) {
            LogC.e("getProductConfig, ClassNotFoundException.", false);
            return null;
        } catch (NoSuchMethodException e2) {
            LogC.e("getProductConfig NoSuchMethodException.", false);
            return null;
        } catch (IllegalAccessException e3) {
            LogC.e("getProductConfig IllegalAccessException.", false);
            return null;
        } catch (IllegalArgumentException e4) {
            LogC.e("getProductConfig IllegalArgumentException.", false);
            return null;
        } catch (InvocationTargetException e5) {
            LogC.e("getProductConfig InvocationTargetException.", false);
            return null;
        }
    }

    public static boolean isPhoneSupportSE() {
        boolean isSupportSE = false;
        String[] configs = getProductConfig();
        if (configs == null || configs.length == 0) {
            LogC.d("isPhoneSupportSE, no product config exist.", false);
            return false;
        }
        if ("01".equals(configs[0])) {
            isSupportSE = true;
        }
        return isSupportSE;
    }

    public static String geteSEManufacturer() {
        String[] configs = getProductConfig();
        if (configs == null || configs.length < 2) {
            LogC.d("geteSEManufacturer, no product config exist.", false);
            return null;
        } else if ("01".equals(configs[0])) {
            return configs[1];
        } else {
            return null;
        }
    }

    public static String getRootProperty() {
        String isRootSystemPropString = null;
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            isRootSystemPropString = (String) classType.getDeclaredMethod("get", new Class[]{String.class}).invoke(classType, new Object[]{"ro.secure"});
            LogC.i("getRootProperty, Root Property info: " + isRootSystemPropString, false);
            return isRootSystemPropString;
        } catch (ClassNotFoundException e) {
            LogC.e("getProductConfig, ClassNotFoundException.", false);
            return isRootSystemPropString;
        } catch (NoSuchMethodException e2) {
            LogC.e("getProductConfig NoSuchMethodException.", false);
            return isRootSystemPropString;
        } catch (IllegalArgumentException e3) {
            LogC.e("getProductConfig IllegalArgumentException.", false);
            return isRootSystemPropString;
        } catch (IllegalAccessException e4) {
            LogC.e("getProductConfig IllegalAccessException.", false);
            return isRootSystemPropString;
        } catch (InvocationTargetException e5) {
            LogC.e("getProductConfig InvocationTargetException.", false);
            return isRootSystemPropString;
        }
    }

    @SuppressLint({"NewApi"})
    public static boolean isOwner() {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                if (((Integer) UserHandle.class.getDeclaredMethod("myUserId", new Class[0]).invoke(null, new Object[0])).intValue() != 0) {
                    return false;
                }
            } catch (NoSuchMethodException e) {
                LogC.e("isOwner NoSuchMethodException.", false);
            } catch (SecurityException e2) {
                LogC.e("isOwner SecurityException.", false);
            } catch (IllegalAccessException e3) {
                LogC.e("isOwner IllegalAccessException.", false);
            } catch (IllegalArgumentException e4) {
                LogC.e("isOwner IllegalArgumentException.", false);
            } catch (InvocationTargetException e5) {
                LogC.e("isOwner InvocationTargetException.", false);
            }
        }
        return true;
    }
}
