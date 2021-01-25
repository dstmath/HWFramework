package com.android.systemui.shared.system;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.RemoteAnimationTarget;
import android.view.SurfaceControl;

public class RemoteAnimationTargetCompat {
    public static final int ACTIVITY_TYPE_ASSISTANT = 4;
    public static final int ACTIVITY_TYPE_HOME = 2;
    public static final int ACTIVITY_TYPE_RECENTS = 3;
    public static final int ACTIVITY_TYPE_STANDARD = 1;
    public static final int ACTIVITY_TYPE_UNDEFINED = 0;
    public static final int MODE_CLOSING = 1;
    public static final int MODE_OPENING = 0;
    public final int activityType;
    public final Rect clipRect;
    public final Rect contentInsets;
    public final boolean isNotInRecents;
    public final boolean isTranslucent;
    public final SurfaceControlCompat leash;
    private final SurfaceControl mStartLeash;
    public final int mode;
    public final Point position;
    public final int prefixOrderIndex;
    public final Rect sourceContainerBounds;
    public final int taskId;

    public RemoteAnimationTargetCompat(RemoteAnimationTarget app) {
        this.taskId = app.taskId;
        this.mode = app.mode;
        this.leash = new SurfaceControlCompat(app.leash);
        this.isTranslucent = app.isTranslucent;
        this.clipRect = app.clipRect;
        this.position = app.position;
        this.sourceContainerBounds = app.sourceContainerBounds;
        this.prefixOrderIndex = app.prefixOrderIndex;
        this.isNotInRecents = app.isNotInRecents;
        this.contentInsets = app.contentInsets;
        this.activityType = app.windowConfiguration.getActivityType();
        this.mStartLeash = app.startLeash;
    }

    public static RemoteAnimationTargetCompat[] wrap(RemoteAnimationTarget[] apps) {
        RemoteAnimationTargetCompat[] appsCompat = new RemoteAnimationTargetCompat[apps.length];
        for (int i = 0; i < apps.length; i++) {
            appsCompat[i] = new RemoteAnimationTargetCompat(apps[i]);
        }
        return appsCompat;
    }

    public void release() {
        this.leash.mSurfaceControl.release();
        SurfaceControl surfaceControl = this.mStartLeash;
        if (surfaceControl != null) {
            surfaceControl.release();
        }
    }
}
