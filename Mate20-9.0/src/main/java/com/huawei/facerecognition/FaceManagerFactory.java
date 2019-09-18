package com.huawei.facerecognition;

import android.content.Context;

public class FaceManagerFactory {
    private static volatile FaceManager mInstance;

    public static synchronized FaceManager getFaceManager(Context context) {
        FaceManager faceManager;
        synchronized (FaceManagerFactory.class) {
            if (mInstance == null) {
                mInstance = new HwFaceManager(context);
            }
            faceManager = mInstance;
        }
        return faceManager;
    }
}
