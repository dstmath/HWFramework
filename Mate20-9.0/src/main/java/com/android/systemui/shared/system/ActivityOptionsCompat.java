package com.android.systemui.shared.system;

import android.app.ActivityOptions;

public abstract class ActivityOptionsCompat {
    public static ActivityOptions makeSplitScreenOptions(boolean dockTopLeft) {
        int i;
        ActivityOptions options = ActivityOptions.makeBasic();
        options.setLaunchWindowingMode(3);
        if (dockTopLeft) {
            i = 0;
        } else {
            i = 1;
        }
        options.setSplitScreenCreateMode(i);
        return options;
    }

    public static ActivityOptions makeRemoteAnimation(RemoteAnimationAdapterCompat remoteAnimationAdapter) {
        return ActivityOptions.makeRemoteAnimation(remoteAnimationAdapter.getWrapped());
    }
}
