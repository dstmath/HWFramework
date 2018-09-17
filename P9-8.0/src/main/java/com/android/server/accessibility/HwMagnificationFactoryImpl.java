package com.android.server.accessibility;

import android.content.Context;
import com.android.server.accessibility.HwMagnificationFactory.Factory;
import com.android.server.accessibility.HwMagnificationFactory.IMagnificationGestureHandler;

public class HwMagnificationFactoryImpl implements Factory {
    private static final String TAG = "HwMagnificationFactoryImpl";

    public static class HwMagnificationGestureHandlerImpl implements IMagnificationGestureHandler {
        public MagnificationGestureHandler getInstance(Context context, AccessibilityManagerService service, boolean detectControlGestures, boolean triggerable) {
            return new HwMagnificationGestureHandler(context, service, detectControlGestures, triggerable);
        }
    }

    public IMagnificationGestureHandler getHwMagnificationGestureHandler() {
        return new HwMagnificationGestureHandlerImpl();
    }
}
