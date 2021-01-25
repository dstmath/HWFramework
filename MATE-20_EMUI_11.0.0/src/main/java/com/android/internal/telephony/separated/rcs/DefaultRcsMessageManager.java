package com.android.internal.telephony.separated.rcs;

import android.content.Context;
import android.telephony.Rlog;

public class DefaultRcsMessageManager {
    private static final String TAG = "DefaultRcsMessageManager";
    private static DefaultRcsMessageManager sInstance;

    public static synchronized DefaultRcsMessageManager getInstance() {
        DefaultRcsMessageManager defaultRcsMessageManager;
        synchronized (DefaultRcsMessageManager.class) {
            if (sInstance == null) {
                sInstance = new DefaultRcsMessageManager();
                Rlog.i(TAG, "getInstance: " + sInstance.getClass().getCanonicalName());
            }
            defaultRcsMessageManager = sInstance;
        }
        return defaultRcsMessageManager;
    }

    public void initRcsMessageStoreController(Context context) {
        Rlog.i(TAG, "initRcsMessageStoreController");
    }
}
