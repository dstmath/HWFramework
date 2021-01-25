package com.huawei.agpengine.impl;

import com.huawei.agpengine.Engine;
import com.huawei.agpengine.resources.ResourceHandle;
import java.math.BigInteger;

class ResourceHandleImpl implements ResourceHandle {
    private static final int INVALID_HANDLE = -1;
    private static final long INVALID_TYPE = -1;
    private EngineImpl mEngineImpl;
    private int mNativeId = -1;
    private long mNativeType = INVALID_TYPE;

    ResourceHandleImpl(EngineImpl engineImpl, CoreResourceHandle nativeHandle) {
        if (nativeHandle != null) {
            this.mEngineImpl = engineImpl;
            this.mNativeId = (int) nativeHandle.getId();
            this.mNativeType = nativeHandle.getType().longValue();
            return;
        }
        throw new IllegalStateException("Internal graphics engine error");
    }

    static CoreResourceHandle getNativeHandle(ResourceHandle handle) {
        if (handle instanceof ResourceHandleImpl) {
            return ((ResourceHandleImpl) handle).getNativeHandle();
        }
        return getNativeHandle(handle.getEngine().getResourceManager().getEmptyResourceHandle());
    }

    private CoreResourceHandle getNativeHandle() {
        CoreResourceHandle nativeHandle = new CoreResourceHandle();
        nativeHandle.setId((long) this.mNativeId);
        nativeHandle.setType(BigInteger.valueOf(this.mNativeType));
        return nativeHandle;
    }

    @Override // com.huawei.agpengine.resources.ResourceHandle
    public Engine getEngine() {
        return this.mEngineImpl;
    }

    @Override // com.huawei.agpengine.resources.ResourceHandle
    public void release() {
        this.mEngineImpl.requireRenderThread();
        if (isValid()) {
            this.mEngineImpl.getAgpContext().getEngine().getResourceManager().erase(getNativeHandle());
            this.mNativeId = -1;
            this.mNativeType = INVALID_TYPE;
        }
    }

    @Override // com.huawei.agpengine.resources.ResourceHandle
    public boolean isValid() {
        if (this.mNativeId == -1) {
            return false;
        }
        return this.mEngineImpl.getAgpContext().getEngine().getResourceManager().isValid(getNativeHandle());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof ResourceHandleImpl)) {
            return false;
        }
        ResourceHandleImpl handle = (ResourceHandleImpl) obj;
        if (this.mNativeType != handle.mNativeType) {
            return false;
        }
        if (this.mNativeId == handle.mNativeId) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (int) ((this.mNativeType * 31) + ((long) this.mNativeId));
    }

    public String toString() {
        return "id=" + this.mNativeId + " type=" + this.mNativeType;
    }
}
