package ohos.agp.render.render3d.resources;

import ohos.agp.render.render3d.Engine;

public interface GpuResourceHandle {
    Engine getEngine();

    boolean isValid();

    void release();

    static boolean isValid(GpuResourceHandle gpuResourceHandle) {
        if (gpuResourceHandle == null) {
            return false;
        }
        return gpuResourceHandle.isValid();
    }
}
