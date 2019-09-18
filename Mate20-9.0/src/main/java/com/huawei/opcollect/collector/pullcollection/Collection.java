package com.huawei.opcollect.collector.pullcollection;

import android.content.Context;
import android.os.Build;
import android.telephony.MSimTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.opcollect.utils.OPCollectLog;
import java.util.Locale;

public final class Collection {
    private static final int FIRST_SLOT = 0;
    private static final int INVALID_SUBSCRIPTION_ID = -1;
    private static final int SECOND_SLOT = 1;
    private static final int SLOT_ONE = 0;
    private static final int SLOT_TWO = 1;
    private static final String TAG = "CollectionImp";
    private static final String UNKNOWN = "unknown";

    public String getLanguage() {
        Locale locale = Locale.getDefault();
        return "{language:" + locale.getLanguage() + ",country:" + locale.getCountry() + "}";
    }

    public String getDefaultIMEI(Context context) {
        if (context == null) {
            OPCollectLog.e(TAG, "context is null.");
            return "";
        }
        TelephonyManager manager = (TelephonyManager) context.getSystemService("phone");
        if (manager == null) {
            OPCollectLog.e(TAG, "Get TelephonyManager failed.");
            return "";
        }
        String deviceID = manager.getImei();
        return deviceID == null ? "" : deviceID;
    }

    public String getSecondaryIMEI(Context context) {
        String defaultID = getDefaultIMEI(context);
        String slot1ID = getIMEI(context, 0);
        return defaultID.equals(slot1ID) ? getIMEI(context, 1) : slot1ID;
    }

    private String getIMEI(Context context, int deviceID) {
        if (context == null) {
            OPCollectLog.e(TAG, "context is null.");
            return "";
        }
        TelephonyManager manager = (TelephonyManager) context.getSystemService("phone");
        if (manager != null) {
            return manager.getDeviceId(deviceID);
        }
        OPCollectLog.e(TAG, "Get TelephonyManager failed.");
        return "";
    }

    public String getHardwareVersion() {
        return SystemPropertiesEx.get("ro.hardware", UNKNOWN);
    }

    public String getSN() {
        return Build.getSerial();
    }

    public int getOpta() {
        return SystemPropertiesEx.getInt("ro.config.hw_opta", -1);
    }

    public int getOptb() {
        return SystemPropertiesEx.getInt("ro.config.hw_optb", -1);
    }

    public String getDeviceName() {
        return Build.MODEL;
    }

    public String getBuildNumber() {
        return Build.DISPLAY;
    }

    public String getSubIMSI(Context context) {
        if (context == null) {
            OPCollectLog.e(TAG, "context is null.");
            return "";
        }
        TelephonyManager manager = (TelephonyManager) context.getSystemService("phone");
        if (manager == null) {
            OPCollectLog.e(TAG, "Get TelephonyManager failed.");
            return "";
        }
        String imsi = manager.getSubscriberId();
        if (imsi == null || imsi.length() < 5) {
            return "";
        }
        return imsi.substring(0, 5);
    }

    public String getDefaultIMSI(Context context) {
        if (context == null) {
            OPCollectLog.e(TAG, "context is null.");
            return "";
        }
        TelephonyManager manager = (TelephonyManager) context.getSystemService("phone");
        if (manager == null) {
            OPCollectLog.e(TAG, "Get TelephonyManager failed.");
            return "";
        }
        String imsi = manager.getSubscriberId();
        return imsi == null ? "" : imsi;
    }

    public String getSecondaryIMSI(Context context) {
        String defaultImsi = getDefaultIMSI(context);
        String slot1Imsi = getIMSI(context, 0);
        return defaultImsi.equals(slot1Imsi) ? getIMSI(context, 1) : slot1Imsi;
    }

    public String getDefaultDataSlotIMSI(Context context) {
        int mainSlot = -1;
        if (Build.VERSION.SDK_INT > 23) {
            mainSlot = SubscriptionManager.getDefaultDataSubscriptionId();
        }
        if (mainSlot == -1) {
            mainSlot = 0;
        }
        OPCollectLog.r(TAG, "slot: " + mainSlot);
        return getIMSI(context, mainSlot);
    }

    private String getIMSI(Context context, int slotId) {
        if (context == null) {
            OPCollectLog.e(TAG, "context is null.");
            return "";
        }
        TelephonyManager manager = (TelephonyManager) context.getSystemService("phone");
        if (manager == null) {
            OPCollectLog.e(TAG, "Get TelephonyManager failed.");
            return "";
        }
        String imsi = TelephonyManagerEx.getSubscriberId(manager, slotId);
        return imsi == null ? "" : imsi;
    }

    public String getAllPhoneNumber(Context context) {
        if (context == null) {
            OPCollectLog.e(TAG, "context is null.");
            return "";
        }
        MSimTelephonyManager mSimTelephonyManager = new MSimTelephonyManager(context);
        String firstPhoneNumber = mSimTelephonyManager.getLine1Number(0);
        String secondPhoneNumber = mSimTelephonyManager.getLine1Number(1);
        if (firstPhoneNumber == null && secondPhoneNumber == null) {
            return "";
        }
        if (firstPhoneNumber == null) {
            return secondPhoneNumber;
        }
        if (secondPhoneNumber == null) {
            return firstPhoneNumber;
        }
        return firstPhoneNumber + "," + secondPhoneNumber;
    }
}
