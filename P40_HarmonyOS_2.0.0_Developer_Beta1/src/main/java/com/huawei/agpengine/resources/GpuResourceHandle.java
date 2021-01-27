package com.huawei.agpengine.resources;

import com.huawei.agpengine.Engine;

public interface GpuResourceHandle {
    Engine getEngine();

    long getNativeHandle();

    boolean isValid();

    void release();

    static boolean isValid(GpuResourceHandle handle) {
        if (handle == null) {
            return false;
        }
        return handle.isValid();
    }
}
