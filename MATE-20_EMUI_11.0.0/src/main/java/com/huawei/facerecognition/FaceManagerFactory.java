package com.huawei.facerecognition;

import android.content.Context;

public class FaceManagerFactory {
    private static volatile FaceManager sInstance;

    public static synchronized FaceManager getFaceManager(Context context) {
        FaceManager faceManager;
        synchronized (FaceManagerFactory.class) {
            if (sInstance == null) {
                sInstance = new HwFaceManager(context);
            }
            faceManager = sInstance;
        }
        return faceManager;
    }
}
