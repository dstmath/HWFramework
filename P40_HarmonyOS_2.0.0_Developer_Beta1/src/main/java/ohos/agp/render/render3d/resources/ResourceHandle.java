package ohos.agp.render.render3d.resources;

import ohos.agp.render.render3d.Engine;

public interface ResourceHandle {
    Engine getEngine();

    boolean isValid();

    void release();

    static boolean isValid(ResourceHandle resourceHandle) {
        if (resourceHandle == null) {
            return false;
        }
        return resourceHandle.isValid();
    }
}
