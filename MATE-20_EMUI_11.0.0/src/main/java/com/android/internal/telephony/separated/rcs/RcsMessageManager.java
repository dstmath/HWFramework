package com.android.internal.telephony.separated.rcs;

import android.content.Context;
import android.telephony.Rlog;
import com.android.internal.telephony.separated.DefaultTelephonySeparatedFactory;
import com.android.internal.telephony.separated.TelephonySeparatedFactory;

public class RcsMessageManager {
    private static final String TAG = "RcsMessageManager";
    private static RcsMessageManager sInstance;
    private DefaultRcsMessageManager mRcsMessageManager;

    private RcsMessageManager() {
        DefaultTelephonySeparatedFactory factory = TelephonySeparatedFactory.getTelephonyFactory().getTelephonySeparatedFactory();
        this.mRcsMessageManager = factory.getRcsMessageManager();
        Rlog.i(TAG, "factory:" + factory.getClass().getCanonicalName() + ", instance:" + this.mRcsMessageManager.getClass().getCanonicalName());
    }

    public static synchronized RcsMessageManager getInstance() {
        RcsMessageManager rcsMessageManager;
        synchronized (RcsMessageManager.class) {
            if (sInstance == null) {
                sInstance = new RcsMessageManager();
            }
            rcsMessageManager = sInstance;
        }
        return rcsMessageManager;
    }

    public void initRcsMessageStoreController(Context context) {
        this.mRcsMessageManager.initRcsMessageStoreController(context);
    }
}
