package com.android.internal.telephony.msim;

import android.content.Context;
import com.android.internal.telephony.Phone;
import com.huawei.android.util.NoExtAPIException;

public class MSimPhoneFactory {
    public MSimPhoneFactory() {
        throw new NoExtAPIException("method not supported.");
    }

    public static void makeMultiSimDefaultPhones(Context context) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void makeMultiSimDefaultPhone(Context context) {
        throw new NoExtAPIException("method not supported.");
    }

    public static Phone getMSimCdmaPhone(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static Phone getMSimGsmPhone(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static Phone getPhone(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void setDefaultSubscription(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getDefaultSubscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getVoiceSubscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean isPromptEnabled() {
        throw new NoExtAPIException("method not supported.");
    }

    public static void setPromptEnabled(boolean enabled) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getDataSubscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getSMSSubscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public static void setVoiceSubscription(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void setDataSubscription(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void setSMSSubscription(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public static Phone getDefaultPhone() {
        throw new NoExtAPIException("method not supported.");
    }
}
