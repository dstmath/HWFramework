package com.android.internal.telephony;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class AbstractSubscriptionInfoUpdater extends Handler {
    public static final boolean IS_MODEM_CAPABILITY_SUPPORT = false;
    public static final int SIM_WITH_SAME_ICCID1 = 1;
    public static final int SIM_WITH_SAME_ICCID2 = 2;
    protected static boolean mNeedUpdate;
    SubscriptionInfoUpdaterReference mReference;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.AbstractSubscriptionInfoUpdater.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.AbstractSubscriptionInfoUpdater.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.AbstractSubscriptionInfoUpdater.<clinit>():void");
    }

    public AbstractSubscriptionInfoUpdater() {
        this.mReference = HwTelephonyFactory.getHwUiccManager().createHwSubscriptionInfoUpdaterReference(this);
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
