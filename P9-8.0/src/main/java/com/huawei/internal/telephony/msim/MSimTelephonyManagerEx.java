package com.huawei.internal.telephony.msim;

import android.content.ContentResolver;
import android.provider.Settings.SettingNotFoundException;
import com.huawei.android.util.NoExtAPIException;

public class MSimTelephonyManagerEx {
    private static MSimTelephonyManagerEx sInstance = new MSimTelephonyManagerEx();

    public enum MultiSimVariants {
        DSDS,
        DSDA,
        UNKNOWN
    }

    private MSimTelephonyManagerEx() {
    }

    public static MSimTelephonyManagerEx getDefault() {
        return sInstance;
    }

    public MultiSimVariants getMultiSimConfiguration() {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getIntAtIndex(ContentResolver cr, String name, int index) throws SettingNotFoundException {
        return -1;
    }

    public static boolean putIntAtIndex(ContentResolver cr, String name, int index, int value) {
        return false;
    }

    public boolean isMultiSimEnabled() {
        throw new NoExtAPIException("method not supported.");
    }

    public int getMmsAutoSetDataSubscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public String getPesn(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getPreferredVoiceSubscription(ContentResolver contentResolver) {
        throw new NoExtAPIException("method not supported.");
    }

    public String getIccCardType(int subscription) {
        throw new NoExtAPIException("method not supported.");
    }
}
