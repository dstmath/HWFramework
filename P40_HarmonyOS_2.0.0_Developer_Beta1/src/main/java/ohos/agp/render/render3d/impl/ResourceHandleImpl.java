package ohos.agp.render.render3d.impl;

import ohos.agp.render.render3d.Engine;
import ohos.agp.render.render3d.resources.ResourceHandle;

class ResourceHandleImpl implements ResourceHandle {
    private final EngineImpl mEngineImpl;
    private CoreResourceHandle mResourceHandle;

    ResourceHandleImpl(EngineImpl engineImpl, CoreResourceHandle coreResourceHandle) {
        this.mEngineImpl = engineImpl;
        this.mResourceHandle = coreResourceHandle;
    }

    static CoreResourceHandle getNativeHandle(ResourceHandle resourceHandle) {
        if (resourceHandle instanceof ResourceHandleImpl) {
            return ((ResourceHandleImpl) resourceHandle).getNativeHandle();
        }
        return getNativeHandle(resourceHandle.getEngine().getResourceManager().getEmptyResourceHandle());
    }

    private CoreResourceHandle getNativeHandle() {
        return this.mResourceHandle;
    }

    @Override // ohos.agp.render.render3d.resources.ResourceHandle
    public Engine getEngine() {
        return this.mEngineImpl;
    }

    @Override // ohos.agp.render.render3d.resources.ResourceHandle
    public void release() {
        this.mEngineImpl.requireRenderThread();
        if (isValid()) {
            this.mEngineImpl.getAgpContext().getEngine().getResourceManager().erase(this.mResourceHandle);
            this.mResourceHandle = null;
        }
    }

    @Override // ohos.agp.render.render3d.resources.ResourceHandle
    public boolean isValid() {
        if (this.mResourceHandle == null) {
            return false;
        }
        return this.mEngineImpl.getAgpContext().getEngine().getResourceManager().isValid(this.mResourceHandle);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof ResourceHandleImpl)) {
            return false;
        }
        ResourceHandleImpl resourceHandleImpl = (ResourceHandleImpl) obj;
        CoreResourceHandle coreResourceHandle = this.mResourceHandle;
        return coreResourceHandle == resourceHandleImpl.mResourceHandle || coreResourceHandle.getId() == resourceHandleImpl.mResourceHandle.getId();
    }

    public int hashCode() {
        CoreResourceHandle coreResourceHandle = this.mResourceHandle;
        if (coreResourceHandle == null) {
            return 0;
        }
        return (int) coreResourceHandle.getId();
    }
}
