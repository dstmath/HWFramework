package com.huawei.server.wm;

import com.android.server.wm.SurfaceAnimationThread;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class SurfaceAnimationThreadEx {
    private static SurfaceAnimationThread sSurfaceAnimationThread;

    private SurfaceAnimationThreadEx() {
        sSurfaceAnimationThread = SurfaceAnimationThread.get();
    }

    public static SurfaceAnimationThreadEx get() {
        return new SurfaceAnimationThreadEx();
    }

    public static int getThreadId() {
        return sSurfaceAnimationThread.getThreadId();
    }
}
