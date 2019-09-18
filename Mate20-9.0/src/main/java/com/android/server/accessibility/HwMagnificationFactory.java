package com.android.server.accessibility;

import android.content.Context;
import android.util.Log;

public class HwMagnificationFactory {
    private static final String TAG = "HwMagnificationFactory";
    private static final Object mLock = new Object();
    private static volatile Factory obj = null;

    public interface Factory {
        IMagnificationGestureHandler getHwMagnificationGestureHandler();
    }

    public interface IMagnificationGestureHandler {
        MagnificationGestureHandler getInstance(Context context, MagnificationController magnificationController, boolean z, boolean z2);
    }

    private static Factory getImplObject() {
        if (obj == null) {
            synchronized (mLock) {
                if (obj == null) {
                    try {
                        obj = (Factory) Class.forName("com.android.server.accessibility.HwMagnificationFactoryImpl").newInstance();
                    } catch (Exception e) {
                        Log.e(TAG, ": reflection exception is " + e);
                    }
                }
            }
            Log.v(TAG, "get allimpl object = " + obj);
        }
        return obj;
    }

    public static IMagnificationGestureHandler getHwMagnificationGestureHandler() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwMagnificationGestureHandler();
        }
        return null;
    }
}
