package com.tencent.soter.core.biometric;

import android.content.Context;

public class SoterFaceManagerFactory {
    private static volatile FaceManager mInstance;

    public static synchronized FaceManager getFaceManager(Context context) {
        FaceManager faceManager;
        synchronized (SoterFaceManagerFactory.class) {
            if (mInstance == null) {
                mInstance = new HwFaceManager(context);
            }
            faceManager = mInstance;
        }
        return faceManager;
    }
}
