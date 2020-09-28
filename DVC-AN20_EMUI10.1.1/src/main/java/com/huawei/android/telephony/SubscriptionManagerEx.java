package com.huawei.android.telephony;

import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionManagerEx {
    @HwSystemApi
    public static final int ACTIVE = 1;
    @HwSystemApi
    public static final int INACTIVE = 0;
    @HwSystemApi
    public static final int INVALID_PHONE_INDEX = -1;
    @HwSystemApi
    public static final int INVALID_SLOT_ID = -1000;
    @HwSystemApi
    public static final int PROFILE_CLASS_UNSET = -1;
    @HwSystemApi
    public static final String SUB_STATE = "sub_state";
    @HwSystemApi
    public static final String WFC_IMS_ENABLED = "wfc_ims_enabled";
    @HwSystemApi
    public static final String WFC_IMS_MODE = "wfc_ims_mode";
    @HwSystemApi
    public static final String WFC_IMS_ROAMING_ENABLED = "wfc_ims_roaming_enabled";
    @HwSystemApi
    public static final String WFC_IMS_ROAMING_MODE = "wfc_ims_roaming_mode";

    public static int[] getSubId(int slotIndex) {
        return SubscriptionManager.getSubId(slotIndex);
    }

    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId) {
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId);
    }

    public static int getSlotIndex(int subId) {
        return SubscriptionManager.getSlotIndex(subId);
    }

    public static int getPhoneId(int subId) {
        return SubscriptionManager.getPhoneId(subId);
    }

    public static int getAllSubscriptionInfoCount(Context context) {
        SubscriptionManager sm = SubscriptionManager.from(context);
        if (sm != null) {
            return sm.getAllSubscriptionInfoCount();
        }
        return 0;
    }

    public static List<SubscriptionInfo> getAllSubscriptionInfoList(Context context) {
        SubscriptionManager sm = SubscriptionManager.from(context);
        if (sm != null) {
            return sm.getAllSubscriptionInfoList();
        }
        return new ArrayList();
    }

    public static boolean isValidSubscriptionId(int subId) {
        return SubscriptionManager.isValidSubscriptionId(subId);
    }

    public static int getSimStateForSlotIndex(int slotIndex) {
        return SubscriptionManager.getSimStateForSlotIndex(slotIndex);
    }

    @HwSystemApi
    public static void setSubscriptionProperty(int subId, String propKey, String propValue) {
        SubscriptionManager.setSubscriptionProperty(subId, propKey, propValue);
    }

    @HwSystemApi
    public static int getIntegerSubscriptionProperty(int subId, String propKey, int defValue, Context context) {
        return SubscriptionManager.getIntegerSubscriptionProperty(subId, propKey, defValue, context);
    }

    @HwSystemApi
    public static int getSubIdUsingSlotId(int slotId) {
        int[] subIds = SubscriptionManager.getSubId(slotId);
        if (subIds == null || subIds.length <= 0) {
            return -1;
        }
        return subIds[0];
    }

    @HwSystemApi
    public static boolean isValidSlotIndex(int slotId) {
        return SubscriptionManager.isValidSlotIndex(slotId);
    }
}
