package com.android.server.accessibility;

import android.content.Context;
import com.android.server.accessibility.HwMagnificationFactory;
import com.huawei.server.HwBasicPlatformFactory;

public class HwMagnificationFactoryImpl implements HwMagnificationFactory.Factory {
    public HwMagnificationFactory.IMagnificationGestureHandler getHwMagnificationGestureHandler() {
        return new HwMagnificationGestureHandlerImpl();
    }

    public static class HwMagnificationGestureHandlerImpl implements HwMagnificationFactory.IMagnificationGestureHandler {
        public MagnificationGestureHandler getInstance(Context context, MagnificationController controller, boolean isDetectControlGestures, boolean isTriggerable, int displayId) {
            MagnificationControllerEx controllerEx = new MagnificationControllerEx();
            controllerEx.setMagnificationController(controller);
            return HwBasicPlatformFactory.loadFactory(HwBasicPlatformFactory.HW_PART_BASIC_PLATFORM_SERVICES_FACTORY_IMPL).getHwMagnificationGestureHandler(context, controllerEx, isDetectControlGestures, isTriggerable, displayId).getMagnificationGestureHandler();
        }
    }
}
