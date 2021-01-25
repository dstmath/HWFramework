package com.huawei.server;

import com.android.server.AnimationThread;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class AnimationThreadEx {
    private static AnimationThread sAnimationThread;

    private AnimationThreadEx() {
        sAnimationThread = AnimationThread.get();
    }

    public static AnimationThreadEx get() {
        return new AnimationThreadEx();
    }

    public static int getThreadId() {
        return sAnimationThread.getThreadId();
    }
}
