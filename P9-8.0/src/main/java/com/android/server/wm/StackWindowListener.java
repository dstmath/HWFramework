package com.android.server.wm;

import android.graphics.Rect;

public interface StackWindowListener extends WindowContainerListener {
    void requestResize(Rect rect);
}
