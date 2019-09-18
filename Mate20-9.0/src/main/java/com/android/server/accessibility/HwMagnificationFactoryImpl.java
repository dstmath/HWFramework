package com.android.server.accessibility;

import android.content.Context;
import com.android.server.accessibility.HwMagnificationFactory;

public class HwMagnificationFactoryImpl implements HwMagnificationFactory.Factory {
    private static final String TAG = "HwMagnificationFactoryImpl";

    public static class HwMagnificationGestureHandlerImpl implements HwMagnificationFactory.IMagnificationGestureHandler {
        public MagnificationGestureHandler getInstance(Context context, MagnificationController magnificationController, boolean detectControlGestures, boolean triggerable) {
            return new HwMagnificationGestureHandler(context, magnificationController, detectControlGestures, triggerable);
        }
    }

    public HwMagnificationFactory.IMagnificationGestureHandler getHwMagnificationGestureHandler() {
        return new HwMagnificationGestureHandlerImpl();
    }
}
