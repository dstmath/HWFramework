package com.android.internal.telephony;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.Rlog;

public class HwUiccSmsControllerEx implements IHwUiccSmsControllerEx {
    private static final int CB_RANGE_START_END_STEP = 2;
    private static final String LOG_TAG = "HwUiccSmsControllerEx";
    private IHwUiccSmsControllerInner mInner;

    public HwUiccSmsControllerEx(IHwUiccSmsControllerInner hwUiccSmsControllerInner) {
        this.mInner = hwUiccSmsControllerInner;
    }

    private void loge(String log) {
        Rlog.e(LOG_TAG, log);
    }

    public String getSmscAddrForSubscriber(int subId) {
        IccSmsInterfaceManager iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getSmscAddr();
        }
        loge("getSmscAddr iccSmsIntMgr is null for Subscription: " + subId);
        return null;
    }

    public boolean setSmscAddrForSubscriber(int subId, String smscAddr) {
        IccSmsInterfaceManager iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.setSmscAddr(smscAddr);
        }
        loge("setSmscAddr iccSmsIntMgr is null for sub: " + subId);
        return false;
    }

    public void setEnabledSingleShiftTables(Context context, int[] tables) {
        if (context == null || tables == null) {
            loge("setEnabledSingleShiftTables context or tables is null, return");
            return;
        }
        context.enforceCallingPermission("android.permission.SEND_SMS", "Requires send sms permission for setEnabledSingleShiftTables");
        GsmAlphabet.setEnabledSingleShiftTables(tables);
    }

    public void setSmsCodingNationalCode(Context context, String code) {
        if (context == null) {
            loge("setSmsCodingNationalCode context null, return");
            return;
        }
        context.enforceCallingPermission("android.permission.SEND_SMS", "Requires send sms permission for setSmsCodingNationalCode");
        SystemProperties.set("gsm.sms.coding.national", code);
    }

    public boolean isUimSupportMeid(int subId) {
        IccSmsInterfaceManager iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.isUimSupportMeid();
        }
        loge("isUimSupportMeid iccSmsIntMgr is null for sub: " + subId);
        return false;
    }

    public String getMeidOrPesn(int subId) {
        IccSmsInterfaceManager iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getMeidOrPesn();
        }
        loge("getMeidOrPesn iccSmsIntMgr is null for sub: " + subId);
        return "";
    }

    public boolean setMeidOrPesn(int subId, String meid, String pesn) {
        IccSmsInterfaceManager iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.setMeidOrPesn(meid, pesn);
        }
        loge("setMeidOrPesn iccSmsIntMgr is null for sub: " + subId);
        return false;
    }

    public boolean setCellBroadcastRangeListForSubscriber(int subId, int[] messageIds, int ranType) {
        IccSmsInterfaceManager iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
        if (!isValidCellBroadcastRange(messageIds)) {
            throw new IllegalArgumentException("endMessageId < startMessageId");
        } else if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.setCellBroadcastRangeList(messageIds, ranType);
        } else {
            loge("setCellBroadcastRangeListForSubscriber iccSmsIntMgr is null for sub: " + subId);
            return false;
        }
    }

    private boolean isValidCellBroadcastRange(int[] messageIds) {
        if (messageIds == null || messageIds.length % 2 != 0) {
            return false;
        }
        int len = messageIds.length;
        for (int i = 0; i < len; i += 2) {
            if (messageIds[i + 1] < messageIds[i]) {
                return false;
            }
        }
        return true;
    }
}
