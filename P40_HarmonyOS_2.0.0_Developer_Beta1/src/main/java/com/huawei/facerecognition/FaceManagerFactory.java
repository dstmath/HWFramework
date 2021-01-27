package com.huawei.facerecognition;

import android.content.Context;

public class FaceManagerFactory {
    public static synchronized FaceManager getFaceManager(Context context) {
        FaceManager faceManager;
        synchronized (FaceManagerFactory.class) {
            faceManager = null;
            if (context != null) {
                faceManager = new HwFaceManager(context);
            }
        }
        return faceManager;
    }
}
