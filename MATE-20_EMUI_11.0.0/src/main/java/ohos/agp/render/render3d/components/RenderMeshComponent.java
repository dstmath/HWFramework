package ohos.agp.render.render3d.components;

import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.resources.ResourceHandle;

public class RenderMeshComponent implements Component {
    private boolean mIsCastShadowsEnabled;
    private ResourceHandle mMaterial;
    private ResourceHandle mMesh;

    public ResourceHandle getMesh() {
        return this.mMesh;
    }

    public void setMesh(ResourceHandle resourceHandle) {
        this.mMesh = resourceHandle;
    }

    public ResourceHandle getMaterial() {
        return this.mMaterial;
    }

    public void setMaterial(ResourceHandle resourceHandle) {
        this.mMaterial = resourceHandle;
    }

    public boolean isCastShadowsEnabled() {
        return this.mIsCastShadowsEnabled;
    }

    public void setCastShadowsEnabled(boolean z) {
        this.mIsCastShadowsEnabled = z;
    }
}
