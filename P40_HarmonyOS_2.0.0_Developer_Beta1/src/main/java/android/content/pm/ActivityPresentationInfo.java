package android.content.pm;

import android.content.ComponentName;

public final class ActivityPresentationInfo {
    public final ComponentName componentName;
    public final int displayId;
    public final int taskId;

    public ActivityPresentationInfo(int taskId2, int displayId2, ComponentName componentName2) {
        this.taskId = taskId2;
        this.displayId = displayId2;
        this.componentName = componentName2;
    }
}
