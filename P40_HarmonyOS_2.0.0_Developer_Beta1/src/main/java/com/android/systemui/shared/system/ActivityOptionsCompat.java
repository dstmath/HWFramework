package com.android.systemui.shared.system;

import android.app.ActivityOptions;
import android.content.Context;
import android.os.Handler;

public abstract class ActivityOptionsCompat {
    public static ActivityOptions makeSplitScreenOptions(boolean dockTopLeft) {
        return makeSplitScreenOptions(dockTopLeft, true);
    }

    public static ActivityOptions makeSplitScreenOptions(boolean dockTopLeft, boolean isPrimary) {
        int i;
        int i2;
        ActivityOptions options = ActivityOptions.makeBasic();
        if (isPrimary) {
            i = 3;
        } else {
            i = 4;
        }
        options.setLaunchWindowingMode(i);
        if (dockTopLeft) {
            i2 = 0;
        } else {
            i2 = 1;
        }
        options.setSplitScreenCreateMode(i2);
        return options;
    }

    public static ActivityOptions makeFreeformOptions() {
        ActivityOptions options = ActivityOptions.makeBasic();
        options.setLaunchWindowingMode(5);
        return options;
    }

    public static ActivityOptions makeRemoteAnimation(RemoteAnimationAdapterCompat remoteAnimationAdapter) {
        return ActivityOptions.makeRemoteAnimation(remoteAnimationAdapter.getWrapped());
    }

    public static ActivityOptions makeCustomAnimation(Context context, int enterResId, int exitResId, final Runnable callback, final Handler callbackHandler) {
        return ActivityOptions.makeCustomAnimation(context, enterResId, exitResId, callbackHandler, new ActivityOptions.OnAnimationStartedListener() {
            /* class com.android.systemui.shared.system.ActivityOptionsCompat.AnonymousClass1 */

            public void onAnimationStarted() {
                Runnable runnable = callback;
                if (runnable != null) {
                    callbackHandler.post(runnable);
                }
            }
        });
    }

    public static ActivityOptions setFreezeRecentTasksList(ActivityOptions opts) {
        opts.setFreezeRecentTasksReordering();
        return opts;
    }
}
