package com.android.server.rms.handler;

import android.content.Context;

public class ResourceDispatcher {
    private static final String TAG = "RMS.ResourceDispatcher";

    public static HwSysResHandler dispath(int resourceType, Context context) {
        switch (resourceType) {
            case 18:
                return AppHandler.getInstance(context);
            default:
                return null;
        }
    }
}
