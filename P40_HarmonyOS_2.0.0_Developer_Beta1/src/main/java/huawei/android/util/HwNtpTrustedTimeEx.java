package huawei.android.util;

import android.content.Context;
import android.util.NtpTrustedTime;

public class HwNtpTrustedTimeEx {
    private static HwNtpTrustedTimeEx sInstance;
    private NtpTrustedTime mNtpTrustedTime;

    public static synchronized HwNtpTrustedTimeEx getInstance(Context context) {
        HwNtpTrustedTimeEx hwNtpTrustedTimeEx;
        synchronized (HwNtpTrustedTimeEx.class) {
            if (sInstance == null) {
                sInstance = new HwNtpTrustedTimeEx(NtpTrustedTime.getInstance(context));
            }
            hwNtpTrustedTimeEx = sInstance;
        }
        return hwNtpTrustedTimeEx;
    }

    private HwNtpTrustedTimeEx(NtpTrustedTime ntpTrustedTime) {
        this.mNtpTrustedTime = ntpTrustedTime;
    }

    public boolean forceRefresh() {
        NtpTrustedTime ntpTrustedTime = this.mNtpTrustedTime;
        if (ntpTrustedTime != null) {
            return ntpTrustedTime.forceRefresh();
        }
        return false;
    }

    public boolean hasCache() {
        NtpTrustedTime ntpTrustedTime = this.mNtpTrustedTime;
        if (ntpTrustedTime != null) {
            return ntpTrustedTime.hasCache();
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
}
