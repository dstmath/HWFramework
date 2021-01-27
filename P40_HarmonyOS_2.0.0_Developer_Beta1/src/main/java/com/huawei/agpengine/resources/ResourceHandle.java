package com.huawei.agpengine.resources;

import com.huawei.agpengine.Engine;

public interface ResourceHandle {
    Engine getEngine();

    boolean isValid();

    void release();

    static boolean isValid(ResourceHandle handle) {
        if (handle == null) {
            return false;
        }
        return handle.isValid();
    }
}
