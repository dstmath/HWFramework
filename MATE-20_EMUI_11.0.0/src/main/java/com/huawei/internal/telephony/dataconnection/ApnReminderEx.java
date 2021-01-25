package com.huawei.internal.telephony.dataconnection;

import android.content.Context;
import com.android.internal.telephony.dataconnection.ApnReminder;
import com.android.internal.telephony.dataconnection.HwCustApnReminder;
import com.huawei.annotation.HwSystemApi;

public class ApnReminderEx {
    private static final ApnReminderEx sInstance = new ApnReminderEx();

    public static synchronized ApnReminderEx getInstance() {
        ApnReminderEx apnReminderEx;
        synchronized (ApnReminderEx.class) {
            apnReminderEx = sInstance;
        }
        return apnReminderEx;
    }

    public void restoreApn(Context context, String plmn, String imsi) {
        ApnReminder.getInstance(context).restoreApn(plmn, imsi);
    }

    public void restoreApn(Context context, String plmn, String imsi, int subId) {
        ApnReminder.getInstance(context, subId).restoreApn(plmn, imsi);
    }

    @HwSystemApi
    public static boolean isPopupApnSettingsEmpty(Context context) {
        if (context == null) {
            return false;
        }
        return ApnReminder.getInstance(context).isPopupApnSettingsEmpty();
    }

    @HwSystemApi
    public static boolean isPopupApnSettingsEmpty(Context context, int slotId) {
        if (context == null) {
            return false;
        }
        return ApnReminder.getInstance(context, slotId).isPopupApnSettingsEmpty();
    }

    @HwSystemApi
    public static String getOnsNameByPreferedApn(Context context, int apnId, String queryValue) {
        if (context == null) {
            return queryValue;
        }
        return ApnReminder.getInstance(context).getOnsNameByPreferedApn(apnId, queryValue);
    }

    @HwSystemApi
    public static String getOnsNameByPreferedApn(Context context, int slotId, int apnId, String queryValue) {
        if (context == null) {
            return queryValue;
        }
        return ApnReminder.getInstance(context, slotId).getOnsNameByPreferedApn(apnId, queryValue);
    }

    @HwSystemApi
    public static void setGID1(Context context, byte[] gid1) {
        if (context != null) {
            ApnReminder.getInstance(context).setGID1(gid1);
        }
    }

    @HwSystemApi
    public static void setGID1(Context context, int slotId, byte[] gid1) {
        if (context != null) {
            ApnReminder.getInstance(context, slotId).setGID1(gid1);
        }
    }

    @HwSystemApi
    public static void setPlmnAndImsi(Context context, String plmn, String imsi) {
        if (context != null) {
            ApnReminder.getInstance(context).setPlmnAndImsi(plmn, imsi);
        }
    }

    @HwSystemApi
    public static void setPlmnAndImsi(Context context, int slotId, String plmn, String imsi) {
        if (context != null) {
            ApnReminder.getInstance(context, slotId).setPlmnAndImsi(plmn, imsi);
        }
    }

    @HwSystemApi
    public static void setSimRefreshingState(Context context, boolean isSimRefreshing) {
        HwCustApnReminder cust;
        if (context != null && (cust = ApnReminder.getInstance(context).getCust()) != null) {
            cust.setSimRefreshingState(isSimRefreshing);
        }
    }

    @HwSystemApi
    public static void setSimRefreshingState(Context context, int slotId, boolean isSimRefreshing) {
        HwCustApnReminder cust;
        if (context != null && (cust = ApnReminder.getInstance(context, slotId).getCust()) != null) {
            cust.setSimRefreshingState(isSimRefreshing);
        }
    }

    @HwSystemApi
    public static String getVoiceMailNumberByPreferedApn(Context context, int slotId, int apnId, String vmNumber) {
        if (context == null) {
            return vmNumber;
        }
        return ApnReminder.getInstance(context, slotId).getVoiceMailNumberByPreferedApn(apnId, vmNumber);
    }

    @HwSystemApi
    public static String getVoiceMailTagByPreferedApn(Context context, int slotId, int apnId, String vmTag) {
        if (context == null) {
            return vmTag;
        }
        return ApnReminder.getInstance(context, slotId).getVoiceMailTagByPreferedApn(apnId, vmTag);
    }
}
