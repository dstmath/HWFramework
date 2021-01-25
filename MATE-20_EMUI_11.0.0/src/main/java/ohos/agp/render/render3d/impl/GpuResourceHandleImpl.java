package ohos.agp.render.render3d.impl;

import ohos.agp.render.render3d.Engine;
import ohos.agp.render.render3d.resources.GpuResourceHandle;

/* access modifiers changed from: package-private */
public class GpuResourceHandleImpl implements GpuResourceHandle {
    private final EngineImpl mEngineImpl;
    private CoreGpuResourceHandle mGpuResourcehandle;

    GpuResourceHandleImpl(EngineImpl engineImpl, CoreGpuResourceHandle coreGpuResourceHandle) {
        this.mEngineImpl = engineImpl;
        this.mGpuResourcehandle = coreGpuResourceHandle;
    }

    static CoreGpuResourceHandle getNativeHandle(GpuResourceHandle gpuResourceHandle) {
        if (gpuResourceHandle instanceof GpuResourceHandleImpl) {
            return ((GpuResourceHandleImpl) gpuResourceHandle).getNativeHandle();
        }
        return getNativeHandle(gpuResourceHandle.getEngine().getResourceManager().getEmptyGpuResourceHandle());
    }

    private CoreGpuResourceHandle getNativeHandle() {
        return this.mGpuResourcehandle;
    }

    @Override // ohos.agp.render.render3d.resources.GpuResourceHandle
    public Engine getEngine() {
        return this.mEngineImpl;
    }

    @Override // ohos.agp.render.render3d.resources.GpuResourceHandle
    public void release() {
        this.mEngineImpl.requireRenderThread();
        if (isValid()) {
            this.mEngineImpl.getAgpContext().getEngine().getGpuResourceManager().destroy(this.mGpuResourcehandle);
            this.mGpuResourcehandle = null;
        }
    }

    @Override // ohos.agp.render.render3d.resources.GpuResourceHandle
    public boolean isValid() {
        CoreGpuResourceHandle coreGpuResourceHandle = this.mGpuResourcehandle;
        if (coreGpuResourceHandle == null) {
            return false;
        }
        return CoreGpuResourceHandleUtil.isValid(coreGpuResourceHandle);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof GpuResourceHandleImpl)) {
            return false;
        }
        GpuResourceHandleImpl gpuResourceHandleImpl = (GpuResourceHandleImpl) obj;
        CoreGpuResourceHandle coreGpuResourceHandle = this.mGpuResourcehandle;
        if (coreGpuResourceHandle == gpuResourceHandleImpl.mGpuResourcehandle) {
            return true;
        }
        return coreGpuResourceHandle.getId().equals(gpuResourceHandleImpl.mGpuResourcehandle.getId());
    }

    public int hashCode() {
        CoreGpuResourceHandle coreGpuResourceHandle = this.mGpuResourcehandle;
        if (coreGpuResourceHandle == null) {
            return 0;
        }
        return coreGpuResourceHandle.getId().intValue();
    }
}
