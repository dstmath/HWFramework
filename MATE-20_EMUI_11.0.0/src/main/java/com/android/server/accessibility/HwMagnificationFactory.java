package com.android.server.accessibility;

import android.content.Context;
import android.util.Log;

public class HwMagnificationFactory {
    private static final Object LOCK = new Object();
    private static final String TAG = "HwMagnificationFactory";
    private static volatile Factory sObj = null;

    public interface Factory {
        IMagnificationGestureHandler getHwMagnificationGestureHandler();
    }

    public interface IMagnificationGestureHandler {
        MagnificationGestureHandler getInstance(Context context, MagnificationController magnificationController, boolean z, boolean z2, int i);
    }

    private static Factory getImplObject() {
        synchronized (LOCK) {
            if (sObj == null) {
                try {
                    Class allimpl = Class.forName("com.android.server.accessibility.HwMagnificationFactoryImpl");
                    if (allimpl.newInstance() instanceof Factory) {
                        sObj = (Factory) allimpl.newInstance();
                    }
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "ClassNotFoundException happened");
                } catch (IllegalAccessException e2) {
                    Log.e(TAG, "IllegalAccessException happened");
                } catch (InstantiationException e3) {
                    Log.e(TAG, "InstantiationException happened");
                } catch (Exception e4) {
                    Log.e(TAG, "Exception happened");
                }
            }
        }
        Log.v(TAG, "get allimpl object = " + sObj);
        return sObj;
    }

    public static IMagnificationGestureHandler getHwMagnificationGestureHandler() {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwMagnificationGestureHandler();
        }
        return null;
    }
}
