package com.android.server.intellicom.common;

import android.telephony.HwTelephonyManagerInner;
import android.util.Slog;
import com.huawei.android.telephony.SubscriptionManagerEx;

public class SmartDualCardUtil {
    private static final String TAG = "SmartDualCardUtil";

    private SmartDualCardUtil() {
    }

    public static int convertSlotIdToSubId(int slotId) {
        if (!isValidSlotId(slotId)) {
            Slog.e(TAG, "convertSlotIdToSubId, Invalid slotId:" + slotId);
            return -1;
        } else if (slotId == 2) {
            Slog.i(TAG, "convertSlotIdToSubId, vsim slotId:" + slotId + " to VSIM_SubId");
            return 999999;
        } else {
            int[] subIds = SubscriptionManagerEx.getSubId(slotId);
            if (subIds == null || subIds.length <= 0) {
                return -1;
            }
            return subIds[0];
        }
    }

    public static int convertSubIdToSlotId(int subId) {
        if (!isValidSubId(subId)) {
            Slog.e(TAG, "convertSubIdToSlotId, subId invalid: " + subId);
            return -1;
        } else if (subId == 999999) {
            Slog.e(TAG, "convertSubIdToSlotId, Vsim SubId to Vsim SlotId");
            return 2;
        } else {
            int slotId = SubscriptionManagerEx.getSlotIndex(subId);
            if (!(slotId == 0 || slotId == 1)) {
                Slog.e(TAG, "convertSubIdToSlotId failed, subId:" + subId + ", slotId:" + slotId);
            }
            return slotId;
        }
    }

    public static int getMasterCardSlotId() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    public static int getSlaveCardSlotId() {
        int masterCardSlotId = getMasterCardSlotId();
        if (masterCardSlotId == 0) {
            return 1;
        }
        if (masterCardSlotId == 1) {
            return 0;
        }
        return -1;
    }

    public static int getMasterCardSubId() {
        return convertSlotIdToSubId(getMasterCardSlotId());
    }

    public static int getSlaveCardSubId() {
        return convertSlotIdToSubId(getSlaveCardSlotId());
    }

    public static boolean isValidSubId(int subId) {
        return subId >= 0 && subId != 999999;
    }

    public static boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId <= 2;
    }
}
