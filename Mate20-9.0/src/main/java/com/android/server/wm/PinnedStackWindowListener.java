package com.android.server.wm;

import android.graphics.Rect;

public interface PinnedStackWindowListener extends StackWindowListener {
    void updatePictureInPictureModeForPinnedStackAnimation(Rect targetStackBounds, boolean forceUpdate) {
    }
}
