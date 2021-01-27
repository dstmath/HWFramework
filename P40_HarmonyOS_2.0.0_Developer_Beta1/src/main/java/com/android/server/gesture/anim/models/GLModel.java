package com.android.server.gesture.anim.models;

public interface GLModel {
    void drawSelf();

    void onSurfaceViewChanged(int i, int i2);

    void prepare();
}
