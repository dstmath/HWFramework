package com.android.systemui.shared.recents.view;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.AppTransitionAnimationSpec;

public class AppTransitionAnimationSpecCompat {
    private Bitmap mBuffer;
    private Rect mRect;
    private int mTaskId;

    public AppTransitionAnimationSpecCompat(int taskId, Bitmap buffer, Rect rect) {
        this.mTaskId = taskId;
        this.mBuffer = buffer;
        this.mRect = rect;
    }

    public AppTransitionAnimationSpec toAppTransitionAnimationSpec() {
        return new AppTransitionAnimationSpec(this.mTaskId, this.mBuffer != null ? this.mBuffer.createGraphicBufferHandle() : null, this.mRect);
    }
}
