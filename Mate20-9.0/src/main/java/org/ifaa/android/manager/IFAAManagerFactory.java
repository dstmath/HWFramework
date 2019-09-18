package org.ifaa.android.manager;

import android.content.Context;
import android.util.Log;

public class IFAAManagerFactory {
    private static final String LOG_TAG = "IFAAManagerFactory";

    public static synchronized IFAAManager getIFAAManager(Context context, int authType) {
        IFAAManagerV4Impl iFAAManagerV4Impl;
        synchronized (IFAAManagerFactory.class) {
            Log.i(LOG_TAG, "IFAAManager getIFAAManager returning v4");
            iFAAManagerV4Impl = new IFAAManagerV4Impl(context);
        }
        return iFAAManagerV4Impl;
    }
}
