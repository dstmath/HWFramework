package com.android.internal.telephony.separated;

import android.telephony.Rlog;
import com.android.internal.telephony.separated.metrics.DefaultTelephonyMetrics;
import com.android.internal.telephony.separated.rcs.DefaultRcsMessageManager;

public class DefaultTelephonySeparatedFactory {
    private static final String TAG = "DefaultTelephonySeparatedFactory";
    private static DefaultTelephonySeparatedFactory sInstance = null;

    public static synchronized DefaultTelephonySeparatedFactory getInstance() {
        DefaultTelephonySeparatedFactory defaultTelephonySeparatedFactory;
        synchronized (DefaultTelephonySeparatedFactory.class) {
            if (sInstance == null) {
                sInstance = new DefaultTelephonySeparatedFactory();
                Rlog.d(TAG, "getInstance: " + sInstance.getClass().getCanonicalName());
            }
            defaultTelephonySeparatedFactory = sInstance;
        }
        return defaultTelephonySeparatedFactory;
    }

    public DefaultRcsMessageManager getRcsMessageManager() {
        return DefaultRcsMessageManager.getInstance();
    }

    public DefaultTelephonyMetrics getTelephonyMetrics() {
        return DefaultTelephonyMetrics.getInstance();
    }
}
