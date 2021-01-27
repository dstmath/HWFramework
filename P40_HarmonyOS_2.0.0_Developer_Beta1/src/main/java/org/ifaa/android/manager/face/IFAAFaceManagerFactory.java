package org.ifaa.android.manager.face;

import android.content.Context;
import android.util.Log;

public class IFAAFaceManagerFactory {
    private static final String LOG_TAG = "IFAAFaceManagerFactory";
    private static IFAAFaceManagerV1Impl faceImplV1;

    public static synchronized IFAAFaceManager getIFAAFaceManager(Context context) {
        IFAAFaceManagerV1Impl iFAAFaceManagerV1Impl;
        synchronized (IFAAFaceManagerFactory.class) {
            Log.i(LOG_TAG, "IFAAManager getIFAAFaceManager");
            if (faceImplV1 == null) {
                faceImplV1 = new IFAAFaceManagerV1Impl(context);
            }
            iFAAFaceManagerV1Impl = faceImplV1;
        }
        return iFAAFaceManagerV1Impl;
    }
}
