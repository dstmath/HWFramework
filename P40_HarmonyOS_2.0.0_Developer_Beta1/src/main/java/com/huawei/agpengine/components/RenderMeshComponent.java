package com.huawei.agpengine.components;

import com.huawei.agpengine.Component;
import com.huawei.agpengine.resources.ResourceHandle;

public class RenderMeshComponent implements Component {
    private boolean mIsCastShadowsEnabled;
    private ResourceHandle mMaterial;
    private ResourceHandle mMesh;

    public ResourceHandle getMesh() {
        return this.mMesh;
    }

    public void setMesh(ResourceHandle mesh) {
        this.mMesh = mesh;
    }

    public ResourceHandle getMaterial() {
        return this.mMaterial;
    }

    public void setMaterial(ResourceHandle material) {
        this.mMaterial = material;
    }

    public boolean isCastShadowsEnabled() {
        return this.mIsCastShadowsEnabled;
    }

    public void setCastShadowsEnabled(boolean isCastShadowsEnabled) {
        this.mIsCastShadowsEnabled = isCastShadowsEnabled;
    }
}
