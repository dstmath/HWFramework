package org.ifaa.android.manager;

import android.content.Context;
import android.util.Log;

public class IFAAManagerFactory {
    private static final String TAG = "IFAAManagerFactory";
    private static IFAAManagerV2Impl impl = new IFAAManagerV2Impl();

    public static IFAAManager getIFAAManager(Context context, int authType) {
        Log.d(TAG, "IFAAManager getIFAAManager");
        return impl;
    }
}
