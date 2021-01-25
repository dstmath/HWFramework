package com.tencent.soter.core.biometric;

import android.content.Context;

public class SoterFaceManagerFactory {
    private static volatile FaceManager sInstance;

    public static synchronized FaceManager getFaceManager(Context context) {
        FaceManager faceManager;
        synchronized (SoterFaceManagerFactory.class) {
            if (sInstance == null) {
                sInstance = new HwFaceManager(context);
            }
            faceManager = sInstance;
        }
        return faceManager;
    }
}
