package com.android.server.display;

public interface IHwColorFadeEx {
    void calculateFeatherAreas(float f, int i);

    void destroyDawnAnimationGLResources();

    void drawDawnAnimationFaded(float f, float[] fArr);

    boolean initDwanAnimationGLBuffers(int i, int i2);

    boolean initDwanAnimationGLShaders();
}
