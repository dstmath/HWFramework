package com.android.internal.telephony;

import android.content.Context;
import android.os.Message;

public interface IHwUiccSmsControllerEx {
    default String getSmscAddrForSubscriber(int subId) {
        return null;
    }

    default boolean setSmscAddrForSubscriber(int subId, String smscAddr) {
        return false;
    }

    default boolean isUimSupportMeid(int subId) {
        return false;
    }

    default String getMeidOrPesn(int subId) {
        return PhoneConfigurationManager.SSSS;
    }

    default boolean setMeidOrPesn(int subId, String meid, String pesn) {
        return false;
    }

    default boolean setCellBroadcastRangeListForSubscriber(int subId, int[] messageIds, int ranType) {
        return false;
    }

    default void setEnabledSingleShiftTables(Context context, int[] tables) {
    }

    default void setSmsCodingNationalCode(Context context, String code) {
    }

    default void processSmsAntiAttack(int serviceType, int smsType, int slotId, Message msg) {
    }
}
