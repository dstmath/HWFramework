package ohos.agp.render.render3d.components;

import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.resources.ResourceHandle;

public class SkinComponent implements Component {
    private ResourceHandle mHandle;

    public ResourceHandle getHandle() {
        return this.mHandle;
    }

    public void setHandle(ResourceHandle resourceHandle) {
        this.mHandle = resourceHandle;
    }
}
