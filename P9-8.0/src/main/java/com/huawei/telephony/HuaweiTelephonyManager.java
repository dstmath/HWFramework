package com.huawei.telephony;

import android.content.Context;
import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import com.android.internal.telephony.VirtualNet;
import com.huawei.android.util.NoExtAPIException;

public class HuaweiTelephonyManager {
    public static final int CT_NATIONAL_ROAMING_CARD = 41;
    public static final int CU_DUAL_MODE_CARD = 42;
    public static final int DUAL_MODE_CG_CARD = 40;
    public static final int DUAL_MODE_UG_CARD = 50;
    public static final int SINGLE_MODE_RUIM_CARD = 30;
    public static final int SINGLE_MODE_SIM_CARD = 10;
    public static final int SINGLE_MODE_USIM_CARD = 20;
    public static final int UNKNOWN_CARD = -1;
    private static HuaweiTelephonyManager mInstance = new HuaweiTelephonyManager();

    public static HuaweiTelephonyManager getDefault() {
        return mInstance;
    }

    public boolean isCTCdmaCardInGsmMode() {
        return false;
    }

    public boolean isCardPresent(int slotId) {
        return true;
    }

    public int getSlotIdFromSubId(int subId) {
        return subId;
    }

    public int getCardType(int slotId) {
        return -1;
    }

    public boolean isCTNationRoamingEnable() {
        return false;
    }

    public boolean isSubActive(int subId) {
        return true;
    }

    public int getDualCardMode() {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean getDSDARadioState(int subId) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getSubidFromSlotId(int slotId) {
        return slotId;
    }

    public boolean setDualCardMode(int nMode) {
        return true;
    }

    public int getDefault4GSlotId() {
        return HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
    }

    public String getIccATR() {
        return HwTelephonyManagerInner.getDefault().getIccATR();
    }

    public void waitingSetDefault4GSlotDone(boolean waiting) {
        HwTelephonyManagerInner.getDefault().waitingSetDefault4GSlotDone(waiting);
    }

    public boolean isSetDefault4GSlotIdEnabled() {
        return HwTelephonyManagerInner.getDefault().isSetDefault4GSlotIdEnabled();
    }

    public void setDefault4GSlotId(int slotId, Message msg) {
        HwTelephonyManagerInner.getDefault().setDefault4GSlotId(slotId, msg);
    }

    public String getOperatorKey(Context context) {
        return VirtualNet.getOperatorKey(context);
    }

    public String getOperatorKey(Context context, int slotId) {
        return VirtualNet.getOperatorKey(context, slotId);
    }
}
