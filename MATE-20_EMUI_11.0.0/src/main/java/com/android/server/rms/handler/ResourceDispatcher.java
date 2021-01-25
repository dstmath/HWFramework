package com.android.server.rms.handler;

import android.content.Context;
import java.util.Optional;

public class ResourceDispatcher {
    private static final String TAG = "RMS.ResourceDispatcher";

    private ResourceDispatcher() {
    }

    public static Optional<HwSysResHandler> dispath(int resourceType, Context context) {
        if (resourceType != 18) {
            return Optional.empty();
        }
        return Optional.of(AppHandler.getInstance(context));
    }
}
