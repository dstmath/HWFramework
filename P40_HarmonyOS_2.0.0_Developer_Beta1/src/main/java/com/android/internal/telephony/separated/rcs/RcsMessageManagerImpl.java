package com.android.internal.telephony.separated.rcs;

import android.content.Context;
import android.telephony.Rlog;
import com.android.internal.telephony.ims.RcsMessageStoreController;

public class RcsMessageManagerImpl extends DefaultRcsMessageManager {
    private static final String TAG = "RcsMessageManagerImpl";
    private static RcsMessageManagerImpl sInstance = null;

    public static synchronized RcsMessageManagerImpl getInstance() {
        RcsMessageManagerImpl rcsMessageManagerImpl;
        synchronized (RcsMessageManagerImpl.class) {
            if (sInstance == null) {
                sInstance = new RcsMessageManagerImpl();
            }
            rcsMessageManagerImpl = sInstance;
        }
        return rcsMessageManagerImpl;
    }

    public void initRcsMessageStoreController(Context context) {
        Rlog.i(TAG, "initRcsMessageStoreController");
        RcsMessageStoreController.init(context);
    }
}
