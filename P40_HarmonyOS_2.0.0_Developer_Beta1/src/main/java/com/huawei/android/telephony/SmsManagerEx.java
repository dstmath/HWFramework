package com.huawei.android.telephony;

import android.os.Message;
import android.telephony.SmsManager;
import com.android.internal.telephony.HwUiccSmsManager;
import com.huawei.android.util.NoExtAPIException;

public final class SmsManagerEx {
    public static String getSmscAddr(SmsManager obj) {
        return HwUiccSmsManager.getSmscAddrForSubscriber(obj.getSubscriptionId());
    }

    public static boolean setSmscAddr(SmsManager obj, String smscAddr) {
        return HwUiccSmsManager.setSmscAddrForSubscriber(obj.getSubscriptionId(), smscAddr);
    }

    public static boolean enableCdmaBroadcast(SmsManager obj, int messageIdentifier) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean disableCdmaBroadcast(SmsManager obj, int messageIdentifier) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isUimSupportMeid(SmsManager smsManager) {
        if (smsManager == null) {
            return false;
        }
        return HwUiccSmsManager.isUimSupportMeid(smsManager.getSubscriptionId());
    }

    public static String getMeidOrPesn(SmsManager smsManager) {
        if (smsManager == null) {
            return null;
        }
        return HwUiccSmsManager.getMeidOrPesn(smsManager.getSubscriptionId());
    }

    public static boolean setMeidOrPesn(SmsManager smsManager, String meid, String pesn) {
        if (smsManager == null) {
            return false;
        }
        return HwUiccSmsManager.setMeidOrPesn(smsManager.getSubscriptionId(), meid, pesn);
    }

    public static void processSmsAntiAttack(int serviceType, int smsType, int slotId, Message msg) {
        HwUiccSmsManager.processSmsAntiAttack(serviceType, smsType, slotId, msg);
    }
}
