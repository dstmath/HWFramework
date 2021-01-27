package com.android.internal.telephony;

import android.app.PendingIntent;
import android.os.Message;
import java.util.List;

public interface IHwIccSmsInterfaceManagerEx {
    default String getSmscAddr() {
        return null;
    }

    default boolean setSmscAddr(String smscAddr) {
        return false;
    }

    default void authenticateSmsSend(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int uid) {
    }

    default void authenticateSmsSends(String destAddr, String scAddr, List<String> list, List<PendingIntent> list2, List<PendingIntent> list3, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int uid) {
    }

    default boolean setCellBroadcastRangeList(int[] messageIds, int ranType) {
        return false;
    }

    default boolean isUimSupportMeid() {
        return false;
    }

    default String getMeidOrPesn() {
        return null;
    }

    default boolean setMeidOrPesn(String meid, String pesn) {
        return false;
    }

    default void processSmsAntiAttack(int serviceType, int smsType, int slotId, Message msg) {
    }
}
