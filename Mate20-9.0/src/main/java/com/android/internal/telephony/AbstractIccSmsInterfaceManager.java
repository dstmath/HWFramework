package com.android.internal.telephony;

import android.app.PendingIntent;
import java.util.List;

public abstract class AbstractIccSmsInterfaceManager {
    /* access modifiers changed from: protected */
    public byte[] getNewbyte() {
        return new byte[]{0};
    }

    /* access modifiers changed from: protected */
    public int getRecordLength() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public boolean isHwMmsUid(int uid) {
        return false;
    }

    public String getSmscAddr() {
        return null;
    }

    public boolean setSmscAddr(String smscAddr) {
        return false;
    }

    public void authenticateSmsSend(String destAddr, String scAddr, String text, PendingIntent sentIntent, PendingIntent deliveryIntent, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int uid) {
    }

    public void authenticateSmsSends(String destAddr, String scAddr, List<String> list, List<PendingIntent> list2, List<PendingIntent> list3, String callingPackage, boolean persistMessageForNonDefaultSmsApp, int priority, boolean expectMore, int validityPeriod, int uid) {
    }

    public boolean setCellBroadcastRangeList(int[] messageIds, int ranType) {
        return false;
    }

    public boolean isUimSupportMeid() {
        return false;
    }

    public String getMeidOrPesn() {
        return null;
    }

    public boolean setMeidOrPesn(String meid, String pesn) {
        return false;
    }
}
