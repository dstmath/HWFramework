package com.huawei.internal.telephony.msim;

import android.os.Handler;
import android.os.Message;
import android.telephony.SubscriptionManager;
import com.huawei.android.util.NoExtAPIException;

public class SubscriptionManagerEx extends Handler {
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;
    public static final int INVALID_PHONE_INDEX = -1;
    public static final int NUM_SUBSCRIPTIONS = 1;
    public static final String SUB_ACTIVATE_FAILED = "ACTIVATE FAILED";
    public static final String SUB_ACTIVATE_NOT_SUPPORTED = "ACTIVATE NOT SUPPORTED";
    public static final String SUB_ACTIVATE_SUCCESS = "ACTIVATE SUCCESS";
    public static final String SUB_DEACTIVATE_FAILED = "DEACTIVATE FAILED";
    public static final String SUB_DEACTIVATE_NOT_SUPPORTED = "DEACTIVATE NOT SUPPORTED";
    public static final String SUB_DEACTIVATE_SUCCESS = "DEACTIVATE SUCCESS";
    public static final String SUB_GLOBAL_ACTIVATE_FAILED = "GLOBAL ACTIVATE FAILED";
    public static final String SUB_GLOBAL_DEACTIVATE_FAILED = "GLOBAL DEACTIVATE FAILED";
    public static final String SUB_NOT_CHANGED = "NO CHANGE IN SUBSCRIPTION";
    private static final int SUB_STATUS_ACTIVATED = 1;
    private static final int SUB_STATUS_DEACTIVATED = 0;

    public static SubscriptionManagerEx getInstance() {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isSubActive(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getActiveSubscriptionsCount() {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isCardPresent(int slotId) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void resumeSubscriptionDSDA() {
        throw new NoExtAPIException("method not supported.");
    }

    public static void setDataSubscription(int subscription, Message onCompleteMsg) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isSetSubscriptionInProgress() {
        throw new NoExtAPIException("method not supported.");
    }

    public static void registerForSetSubscriptionCompleted(Handler h, int what, Object obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void unRegisterForSetSubscriptionCompleted(Handler h) {
        throw new NoExtAPIException("method not supported.");
    }

    public static SubscriptionEx getCurrentSubscription(int subId) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void registerForSubscriptionDeactivated(int subId, Handler h, int what, Object obj) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean setSubscription(SubscriptionDataEx subDataEx) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getPhoneId(int subId) {
        return SubscriptionManager.getPhoneId(subId);
    }

    public static int[] getSubId(int slotId) {
        return SubscriptionManager.getSubId(slotId);
    }
}
