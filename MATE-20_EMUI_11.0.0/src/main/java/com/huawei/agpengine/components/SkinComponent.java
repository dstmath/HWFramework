package com.huawei.agpengine.components;

import com.huawei.agpengine.Component;
import com.huawei.agpengine.resources.ResourceHandle;

public class SkinComponent implements Component {
    private ResourceHandle mHandle;

    public ResourceHandle getHandle() {
        return this.mHandle;
    }

    public void setHandle(ResourceHandle handle) {
        this.mHandle = handle;
    }
}
