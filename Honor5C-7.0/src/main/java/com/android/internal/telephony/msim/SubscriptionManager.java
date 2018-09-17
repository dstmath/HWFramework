package com.android.internal.telephony.msim;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.UiccController;
import com.huawei.android.util.NoExtAPIException;

public class SubscriptionManager extends Handler {
    public static int NUM_SUBSCRIPTIONS = 0;
    public static final String SUB_ACTIVATE_FAILED = "ACTIVATE FAILED";
    public static final String SUB_ACTIVATE_NOT_SUPPORTED = "ACTIVATE NOT SUPPORTED";
    public static final String SUB_ACTIVATE_SUCCESS = "ACTIVATE SUCCESS";
    public static final String SUB_DEACTIVATE_FAILED = "DEACTIVATE FAILED";
    public static final String SUB_DEACTIVATE_NOT_SUPPORTED = "DEACTIVATE NOT SUPPORTED";
    public static final String SUB_DEACTIVATE_SUCCESS = "DEACTIVATE SUCCESS";
    public static final String SUB_NOT_CHANGED = "NO CHANGE IN SUBSCRIPTION";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.msim.SubscriptionManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.msim.SubscriptionManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.msim.SubscriptionManager.<clinit>():void");
    }

    public static SubscriptionManager getInstance(Context context, UiccController uiccController, CommandsInterface[] ci) {
        throw new NoExtAPIException("method not supported.");
    }

    public static SubscriptionManager getInstance() {
        throw new NoExtAPIException("method not supported.");
    }

    public void handleMessage(Message msg) {
        throw new NoExtAPIException("method not supported.");
    }

    public Subscription getCurrentSubscription(int subId) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isSubActive(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean setSubscription(SubscriptionData subData) {
        throw new NoExtAPIException("method not supported.");
    }

    public void setDataSubscription(int subscription, Message onCompleteMsg) {
        throw new NoExtAPIException("method not supported.");
    }

    public void registerForSubscriptionDeactivated(int subId, Handler h, int what, Object obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public void unregisterForSubscriptionDeactivated(int subId, Handler h) {
        throw new NoExtAPIException("method not supported.");
    }

    public void registerForSubscriptionActivated(int subId, Handler h, int what, Object obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public void unregisterForSubscriptionActivated(int subId, Handler h) {
        throw new NoExtAPIException("method not supported.");
    }

    public synchronized void registerForSetSubscriptionCompleted(Handler h, int what, Object obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public synchronized void unRegisterForSetSubscriptionCompleted(Handler h) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getActiveSubscriptionsCount() {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isSetSubscriptionInProgress() {
        throw new NoExtAPIException("method not supported.");
    }

    public void resumeSubscriptionDSDA() {
        throw new NoExtAPIException("method not supported.");
    }

    public int getSubidFromSlotId(int slotId) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isCardPresent(int slotId) {
        throw new NoExtAPIException("method not supported.");
    }
}
