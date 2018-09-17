package com.android.internal.telephony.dataconnection;

import android.os.Handler;
import android.os.RegistrantList;
import android.telephony.Rlog;
import android.util.Pair;

public class DataEnabledSettings {
    private static String LOG_TAG = "DataEnabledSettings";
    public static final int REASON_DATA_ENABLED_BY_CARRIER = 4;
    public static final int REASON_INTERNAL_DATA_ENABLED = 1;
    public static final int REASON_POLICY_DATA_ENABLED = 3;
    public static final int REASON_REGISTERED = 0;
    public static final int REASON_USER_DATA_ENABLED = 2;
    private boolean mCarrierDataEnabled = true;
    private final RegistrantList mDataEnabledChangedRegistrants = new RegistrantList();
    private boolean mInternalDataEnabled = true;
    private boolean mPolicyDataEnabled = true;
    private boolean mUserDataEnabled = true;

    public synchronized void setInternalDataEnabled(boolean enabled) {
        boolean prevDataEnabled = isDataEnabled();
        this.mInternalDataEnabled = enabled;
        if (prevDataEnabled != isDataEnabled()) {
            notifyDataEnabledChanged(prevDataEnabled ^ 1, 1);
        }
    }

    public synchronized boolean isInternalDataEnabled() {
        return this.mInternalDataEnabled;
    }

    public synchronized void setUserDataEnabled(boolean enabled) {
        boolean prevDataEnabled = isDataEnabled();
        this.mUserDataEnabled = enabled;
        if (prevDataEnabled != isDataEnabled()) {
            notifyDataEnabledChanged(prevDataEnabled ^ 1, 2);
        }
    }

    public synchronized boolean isUserDataEnabled() {
        return this.mUserDataEnabled;
    }

    public synchronized void setPolicyDataEnabled(boolean enabled) {
        boolean prevDataEnabled = isDataEnabled();
        this.mPolicyDataEnabled = enabled;
        if (prevDataEnabled != isDataEnabled()) {
            notifyDataEnabledChanged(prevDataEnabled ^ 1, 3);
        }
    }

    public synchronized boolean isPolicyDataEnabled() {
        return this.mPolicyDataEnabled;
    }

    public synchronized void setCarrierDataEnabled(boolean enabled) {
        boolean prevDataEnabled = isDataEnabled();
        this.mCarrierDataEnabled = enabled;
        if (prevDataEnabled != isDataEnabled()) {
            notifyDataEnabledChanged(prevDataEnabled ^ 1, 4);
        }
    }

    public synchronized boolean isCarrierDataEnabled() {
        return this.mCarrierDataEnabled;
    }

    public synchronized boolean isDataEnabled() {
        boolean z;
        if (this.mInternalDataEnabled && this.mUserDataEnabled && this.mPolicyDataEnabled) {
            z = this.mCarrierDataEnabled;
        } else {
            z = false;
        }
        if (z) {
            return true;
        }
        Rlog.d(LOG_TAG, "isDataEnabled: false, mInternalDataEnabled = " + this.mInternalDataEnabled + ", mUserDataEnabled = " + this.mUserDataEnabled + ", mPolicyDataEnabled = " + this.mPolicyDataEnabled + ", mCarrierDataEnabled = " + this.mCarrierDataEnabled);
        return false;
    }

    private void notifyDataEnabledChanged(boolean enabled, int reason) {
        this.mDataEnabledChangedRegistrants.notifyResult(new Pair(Boolean.valueOf(enabled), Integer.valueOf(reason)));
    }

    public void registerForDataEnabledChanged(Handler h, int what, Object obj) {
        this.mDataEnabledChangedRegistrants.addUnique(h, what, obj);
        notifyDataEnabledChanged(isDataEnabled(), 0);
    }

    public void unregisterForDataEnabledChanged(Handler h) {
        this.mDataEnabledChangedRegistrants.remove(h);
    }
}
