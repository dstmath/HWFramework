package com.huawei.agpengine;

public interface TargetBuffer {
    int getHeight();

    int getWidth();

    boolean isBufferAvailable();

    boolean isSrgbConversionRequired();

    void release();
}
