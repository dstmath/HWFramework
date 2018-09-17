package com.android.internal.telephony.msim;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.UiccController;
import com.huawei.android.util.NoExtAPIException;

public class SubscriptionManager extends Handler {
    public static int NUM_SUBSCRIPTIONS = 2;
    public static final String SUB_ACTIVATE_FAILED = "ACTIVATE FAILED";
    public static final String SUB_ACTIVATE_NOT_SUPPORTED = "ACTIVATE NOT SUPPORTED";
    public static final String SUB_ACTIVATE_SUCCESS = "ACTIVATE SUCCESS";
    public static final String SUB_DEACTIVATE_FAILED = "DEACTIVATE FAILED";
    public static final String SUB_DEACTIVATE_NOT_SUPPORTED = "DEACTIVATE NOT SUPPORTED";
    public static final String SUB_DEACTIVATE_SUCCESS = "DEACTIVATE SUCCESS";
    public static final String SUB_NOT_CHANGED = "NO CHANGE IN SUBSCRIPTION";

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
