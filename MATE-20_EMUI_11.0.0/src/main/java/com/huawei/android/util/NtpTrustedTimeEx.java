package com.huawei.android.util;

import android.content.Context;
import android.util.NtpTrustedTime;

public class NtpTrustedTimeEx {
    private static NtpTrustedTimeEx sSingleton;
    private NtpTrustedTime mNtpTrustedTime;

    public static synchronized NtpTrustedTimeEx getInstance(Context context) {
        NtpTrustedTimeEx ntpTrustedTimeEx;
        synchronized (NtpTrustedTimeEx.class) {
            if (sSingleton == null) {
                sSingleton = new NtpTrustedTimeEx();
                sSingleton.setNtpTrustedTime(NtpTrustedTime.getInstance(context));
            }
            ntpTrustedTimeEx = sSingleton;
        }
        return ntpTrustedTimeEx;
    }

    private void setNtpTrustedTime(NtpTrustedTime ntpTrustedTime) {
        this.mNtpTrustedTime = ntpTrustedTime;
    }

    public boolean hasCache() {
        NtpTrustedTime ntpTrustedTime = this.mNtpTrustedTime;
        if (ntpTrustedTime != null) {
            return ntpTrustedTime.hasCache();
        }
        return false;
    }

    public boolean forceRefresh() {
        NtpTrustedTime ntpTrustedTime = this.mNtpTrustedTime;
        if (ntpTrustedTime != null) {
            return ntpTrustedTime.forceRefresh();
        }
        return false;
    }

    public long currentTimeMillis() {
        NtpTrustedTime ntpTrustedTime = this.mNtpTrustedTime;
        if (ntpTrustedTime != null) {
            return ntpTrustedTime.currentTimeMillis();
        }
        return 0;
    }

    public long getCacheAge() {
        NtpTrustedTime ntpTrustedTime = this.mNtpTrustedTime;
        if (ntpTrustedTime != null) {
            return ntpTrustedTime.getCacheAge();
        }
        return 0;
    }

    public long getCachedNtpTime() {
        NtpTrustedTime ntpTrustedTime = this.mNtpTrustedTime;
        if (ntpTrustedTime != null) {
            return ntpTrustedTime.getCachedNtpTime();
        }
        return 0;
    }
}
