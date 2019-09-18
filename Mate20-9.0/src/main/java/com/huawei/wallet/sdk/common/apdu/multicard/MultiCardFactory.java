package com.huawei.wallet.sdk.common.apdu.multicard;

import com.huawei.wallet.sdk.common.apdu.multicard.MultiCard;
import com.huawei.wallet.sdk.common.log.LogC;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class MultiCardFactory {
    private static MultiCard.SupportMode IS_Multi_SIM_ENABLED = MultiCard.SupportMode.MODE_SUPPORT_UNKNOWN;
    private static MultiCard instance;

    public static MultiCard createIfGemini() {
        isMultiSimEnabled();
        if (IS_Multi_SIM_ENABLED == MultiCard.SupportMode.MODE_SUPPORT_MTK_GEMINI) {
            instance = MultiCardMTKImpl.getInstance();
        } else {
            instance = MultiCardHwImpl.getInstance();
        }
        return instance;
    }

    public static boolean isMultiSimEnabled() {
        if (IS_Multi_SIM_ENABLED != MultiCard.SupportMode.MODE_SUPPORT_UNKNOWN) {
            if (IS_Multi_SIM_ENABLED == MultiCard.SupportMode.MODE_SUPPORT_HW_GEMINI || IS_Multi_SIM_ENABLED == MultiCard.SupportMode.MODE_SUPPORT_MTK_GEMINI) {
                return true;
            }
            return false;
        } else if (isMtkGeminiSupport()) {
            IS_Multi_SIM_ENABLED = MultiCard.SupportMode.MODE_SUPPORT_MTK_GEMINI;
            return true;
        } else if (isHwGeminiSupport()) {
            IS_Multi_SIM_ENABLED = MultiCard.SupportMode.MODE_SUPPORT_HW_GEMINI;
            return true;
        } else {
            IS_Multi_SIM_ENABLED = MultiCard.SupportMode.MODE_NOT_SUPPORT_GEMINI;
            return false;
        }
    }

    public static boolean isHwGeminiSupport() {
        boolean support = false;
        try {
            Object o = MultiCardHwImpl.getInstance().getDefaultMSimTelephonyManager();
            if (o != null) {
                support = ((Boolean) o.getClass().getMethod("isMultiSimEnabled", new Class[0]).invoke(o, new Object[0])).booleanValue();
            }
        } catch (NoSuchMethodException e) {
            LogC.w(LogC.LOG_HWSDK_TAG, "MSimTelephonyManager.getDefault().isMultiSimEnabled()? isHwGeminiSupport NoSuchMethodException", false);
        } catch (IllegalAccessException e2) {
            LogC.w(LogC.LOG_HWSDK_TAG, "MSimTelephonyManager.getDefault().isMultiSimEnabled()? isHwGeminiSupport IllegalAccessException", false);
        } catch (IllegalArgumentException e3) {
            LogC.w(LogC.LOG_HWSDK_TAG, "MSimTelephonyManager.getDefault().isMultiSimEnabled()? isHwGeminiSupport IllegalArgumentException", false);
        } catch (InvocationTargetException e4) {
            LogC.w(LogC.LOG_HWSDK_TAG, "MSimTelephonyManager.getDefault().isMultiSimEnabled()? isHwGeminiSupport InvocationTargetException", false);
        }
        LogC.i(LogC.LOG_HWSDK_TAG, "isHwGeminiSupport1 " + support, false);
        return support;
    }

    private static boolean isMtkGeminiSupport() {
        boolean support = false;
        try {
            Field field = Class.forName("com.mediatek.common.featureoption.FeatureOption").getDeclaredField("MTK_GEMINI_SUPPORT");
            field.setAccessible(true);
            support = field.getBoolean(null);
        } catch (ClassNotFoundException e) {
            LogC.w(LogC.LOG_HWSDK_TAG, "FeatureOption.MTK_GEMINI_SUPPORT ClassNotFoundException has error", false);
        } catch (NoSuchFieldException e2) {
            LogC.w(LogC.LOG_HWSDK_TAG, "FeatureOption.MTK_GEMINI_SUPPORT NoSuchMethodException", false);
        } catch (IllegalAccessException e3) {
            LogC.w(LogC.LOG_HWSDK_TAG, "FeatureOption.MTK_GEMINI_SUPPORT IllegalAccessException", false);
        } catch (IllegalArgumentException e4) {
            LogC.w(LogC.LOG_HWSDK_TAG, "FeatureOption.MTK_GEMINI_SUPPORT IllegalArgumentException", false);
        } catch (Exception e5) {
            LogC.w(LogC.LOG_HWSDK_TAG, "FeatureOption.MTK_GEMINI_SUPPORT UnKnownError", false);
        }
        LogC.i(LogC.LOG_HWSDK_TAG, "isMtkGeminiSupport " + support, false);
        return support;
    }
}
