package com.android.server.wm;

import android.app.ActivityManager.TaskSnapshot;
import android.graphics.Rect;

public interface TaskWindowContainerListener extends WindowContainerListener {
    void onSnapshotChanged(TaskSnapshot taskSnapshot);

    void requestResize(Rect rect, int i);
}
