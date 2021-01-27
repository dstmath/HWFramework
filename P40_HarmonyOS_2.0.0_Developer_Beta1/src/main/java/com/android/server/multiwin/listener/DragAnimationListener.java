package com.android.server.multiwin.listener;

import android.graphics.Rect;
import android.view.DragEvent;
import android.view.View;

public interface DragAnimationListener {
    void onDragEnded(boolean z);

    void onDragEntered(View view, DragEvent dragEvent, int i, int i2);

    void onDragExited(View view);

    void onDragLocation();

    void onDragStarted();

    void onDrop(View view, DragEvent dragEvent, int i, Rect rect, int i2);
}
