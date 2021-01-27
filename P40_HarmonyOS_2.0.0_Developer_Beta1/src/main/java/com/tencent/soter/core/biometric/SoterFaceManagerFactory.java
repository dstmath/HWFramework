package com.tencent.soter.core.biometric;

import android.content.Context;

public class SoterFaceManagerFactory {
    public static synchronized FaceManager getFaceManager(Context context) {
        FaceManager faceManager;
        synchronized (SoterFaceManagerFactory.class) {
            faceManager = null;
            if (context != null) {
                faceManager = new HwFaceManager(context);
            }
        }
        return faceManager;
    }
}
