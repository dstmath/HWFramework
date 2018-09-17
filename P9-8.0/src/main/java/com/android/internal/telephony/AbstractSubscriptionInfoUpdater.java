package com.android.internal.telephony;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class AbstractSubscriptionInfoUpdater extends Handler {
    public static final boolean IS_MODEM_CAPABILITY_SUPPORT = HwModemCapability.isCapabilitySupport(9);
    public static final int SIM_WITH_SAME_ICCID1 = 1;
    public static final int SIM_WITH_SAME_ICCID2 = 2;
    protected static boolean mNeedUpdate = true;
    SubscriptionInfoUpdaterReference mReference = HwTelephonyFactory.getHwUiccManager().createHwSubscriptionInfoUpdaterReference(this);

    public interface SubscriptionInfoUpdaterReference {
        void broadcastSubinfoRecordUpdated(String[] strArr, String[] strArr2, int i, int i2, int i3);

        void handleMessageExtend(Message message);

        void queryIccId(int i);

        void resetIccid(int i);

        void setNeedUpdateIfNeed(int i, String str);

        void subscriptionInfoInit(Handler handler, Context context, CommandsInterface[] commandsInterfaceArr);

        void updateIccAvailability(int i);

        void updateSubActivation(int[] iArr, boolean z);

        void updateSubIdForNV(int i);
    }

    public void subscriptionInfoInit(Handler handler, Context context, CommandsInterface[] ci) {
        this.mReference.subscriptionInfoInit(handler, context, ci);
    }

    public void handleMessageExtend(Message msg) {
        this.mReference.handleMessageExtend(msg);
    }

    public void updateIccAvailability(int slotId) {
        this.mReference.updateIccAvailability(slotId);
    }

    public void queryIccId(int slotId) {
        this.mReference.queryIccId(slotId);
    }

    public void resetIccid(int slotId) {
        this.mReference.resetIccid(slotId);
    }

    public void updateSubIdForNV(int slotId) {
        this.mReference.updateSubIdForNV(slotId);
    }

    public void updateSubActivation(int[] simStatus, boolean isStackReadyEvent) {
        this.mReference.updateSubActivation(simStatus, isStackReadyEvent);
    }

    public void broadcastSubinfoRecordUpdated(String[] iccId, String[] oldIccId, int nNewCardCount, int nSubCount, int nNewSimStatus) {
        this.mReference.broadcastSubinfoRecordUpdated(iccId, oldIccId, nNewCardCount, nSubCount, nNewSimStatus);
    }

    public void setNeedUpdate(boolean needupdate) {
        mNeedUpdate = needupdate;
    }

    public void setNeedUpdateIfNeed(int slotId, String currentIccId) {
        this.mReference.setNeedUpdateIfNeed(slotId, currentIccId);
    }
}
