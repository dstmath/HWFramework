package com.android.internal.telephony;

import android.content.Context;
import android.os.Message;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.GsmAlphabetEx;
import com.huawei.internal.telephony.SmsConstantEx;

public class HwUiccSmsControllerEx implements IHwUiccSmsControllerEx {
    private static final int CB_RANGE_START_END_STEP = 2;
    private static final String LOG_TAG = "HwUiccSmsControllerEx";
    private IHwUiccSmsControllerInner mInner;

    public HwUiccSmsControllerEx(IHwUiccSmsControllerInner hwUiccSmsControllerInner) {
        this.mInner = hwUiccSmsControllerInner;
    }

    private void loge(String log) {
        RlogEx.e(LOG_TAG, log);
    }

    public String getSmscAddrForSubscriber(int subId) {
        IHwIccSmsInterfaceManagerEx iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getSmscAddr();
        }
        loge("getSmscAddr iccSmsIntMgr is null for Subscription: " + subId);
        return null;
    }

    public boolean setSmscAddrForSubscriber(int subId, String smscAddr) {
        IHwIccSmsInterfaceManagerEx iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
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
        GsmAlphabetEx.setEnabledSingleShiftTables(tables);
    }

    public void setSmsCodingNationalCode(Context context, String code) {
        if (context == null) {
            loge("setSmsCodingNationalCode context null, return");
            return;
        }
        context.enforceCallingPermission("android.permission.SEND_SMS", "Requires send sms permission for setSmsCodingNationalCode");
        if (code == null || code.length() <= SmsConstantEx.PROP_VALUE_MAX) {
            SystemPropertiesEx.set("gsm.sms.coding.national", code);
            return;
        }
        loge("setSmsCodingNationalCode code is not null and length is " + code.length() + ", return.");
    }

    public boolean isUimSupportMeid(int subId) {
        IHwIccSmsInterfaceManagerEx iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.isUimSupportMeid();
        }
        loge("isUimSupportMeid iccSmsIntMgr is null for sub: " + subId);
        return false;
    }

    public String getMeidOrPesn(int subId) {
        IHwIccSmsInterfaceManagerEx iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.getMeidOrPesn();
        }
        loge("getMeidOrPesn iccSmsIntMgr is null for sub: " + subId);
        return BuildConfig.FLAVOR;
    }

    public boolean setMeidOrPesn(int subId, String meid, String pesn) {
        IHwIccSmsInterfaceManagerEx iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
        if (iccSmsIntMgr != null) {
            return iccSmsIntMgr.setMeidOrPesn(meid, pesn);
        }
        loge("setMeidOrPesn iccSmsIntMgr is null for sub: " + subId);
        return false;
    }

    public boolean setCellBroadcastRangeListForSubscriber(int subId, int[] messageIds, int ranType) {
        IHwIccSmsInterfaceManagerEx iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(subId);
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

    public void processSmsAntiAttack(int serviceType, int smsType, int slotId, Message msg) {
        IHwIccSmsInterfaceManagerEx iccSmsIntMgr = this.mInner.getIccSmsInterfaceManagerEx(SubscriptionManagerEx.getSubIdUsingSlotId(slotId));
        if (iccSmsIntMgr != null) {
            iccSmsIntMgr.processSmsAntiAttack(serviceType, smsType, slotId, msg);
            return;
        }
        loge("processSmsAntiAttack  sub: " + slotId);
    }
}
