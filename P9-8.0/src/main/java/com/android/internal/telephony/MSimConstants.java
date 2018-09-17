package com.android.internal.telephony;

public class MSimConstants {
    public static final int DEFAULT_CARD_INDEX = 0;
    public static final int DEFAULT_SUBSCRIPTION = 0;
    public static final int EVENT_SUBSCRIPTION_ACTIVATED = 500;
    public static final int EVENT_SUBSCRIPTION_DEACTIVATED = 501;
    public static final int MAX_PHONE_COUNT_DS = 2;
    public static final int NUM_SUBSCRIPTIONS = 2;
    public static final int RIL_CARD_MAX_APPS = 8;
    public static final int RIL_MAX_CARDS = 2;
    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    public static final String SUBSCRIPTION_KEY = "subscription";

    public enum CardUnavailableReason {
        REASON_CARD_REMOVED,
        REASON_RADIO_UNAVAILABLE,
        REASON_SIM_REFRESH_RESET
    }
}
