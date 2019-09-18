package org.ifaa.android.manager.face;

import android.content.Context;
import android.util.Log;

public class IFAAFaceManagerFactory {
    private static final String LOG_TAG = "IFAAFaceManagerFactory";
    private static IFAAFaceManagerV1Impl mFaceImplV1;

    public static synchronized IFAAFaceManager getIFAAFaceManager(Context context) {
        IFAAFaceManagerV1Impl iFAAFaceManagerV1Impl;
        synchronized (IFAAFaceManagerFactory.class) {
            Log.i(LOG_TAG, "IFAAManager getIFAAFaceManager");
            if (mFaceImplV1 == null) {
                mFaceImplV1 = new IFAAFaceManagerV1Impl(context);
            }
            iFAAFaceManagerV1Impl = mFaceImplV1;
        }
        return iFAAFaceManagerV1Impl;
    }
}
