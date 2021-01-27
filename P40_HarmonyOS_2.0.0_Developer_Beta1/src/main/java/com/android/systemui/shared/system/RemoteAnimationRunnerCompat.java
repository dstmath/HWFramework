package com.android.systemui.shared.system;

public interface RemoteAnimationRunnerCompat {
    void onAnimationCancelled();

    void onAnimationStart(RemoteAnimationTargetCompat[] remoteAnimationTargetCompatArr, Runnable runnable);
}
