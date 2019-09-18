package com.android.server.rms.handler;

import android.content.Context;

public class ResourceDispatcher {
    private static final String TAG = "RMS.ResourceDispatcher";

    public static HwSysResHandler dispath(int resourceType, Context context) {
        if (resourceType != 18) {
            return null;
        }
        return AppHandler.getInstance(context);
    }
}
