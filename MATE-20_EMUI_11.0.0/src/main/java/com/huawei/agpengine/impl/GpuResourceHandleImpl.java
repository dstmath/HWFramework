package com.huawei.agpengine.impl;

import com.huawei.agpengine.Engine;
import com.huawei.agpengine.resources.GpuResourceHandle;

/* access modifiers changed from: package-private */
public class GpuResourceHandleImpl implements GpuResourceHandle {
    private static final long INVALID_HANDLE = 4294967295L;
    private final EngineImpl mEngineImpl;
    private long mNativeHandle;

    GpuResourceHandleImpl(EngineImpl engine) {
        this(engine, INVALID_HANDLE);
    }

    GpuResourceHandleImpl(EngineImpl engine, long nativeHandle) {
        this.mNativeHandle = INVALID_HANDLE;
        this.mEngineImpl = engine;
        this.mNativeHandle = nativeHandle;
    }

    @Override // com.huawei.agpengine.resources.GpuResourceHandle
    public long getNativeHandle() {
        return this.mNativeHandle;
    }

    @Override // com.huawei.agpengine.resources.GpuResourceHandle
    public Engine getEngine() {
        return this.mEngineImpl;
    }

    @Override // com.huawei.agpengine.resources.GpuResourceHandle
    public void release() {
        this.mEngineImpl.requireRenderThread();
        if (isValid()) {
            this.mEngineImpl.getAgpContext().getEngine().getGpuResourceManager().destroy(this.mNativeHandle);
            this.mNativeHandle = INVALID_HANDLE;
        }
    }

    @Override // com.huawei.agpengine.resources.GpuResourceHandle
    public boolean isValid() {
        long j = this.mNativeHandle;
        if (j == INVALID_HANDLE) {
            return false;
        }
        return Core.isGpuResourceHandleValid(j);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof GpuResourceHandle)) {
            return false;
        }
        if (this.mNativeHandle == ((GpuResourceHandle) obj).getNativeHandle()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (int) this.mNativeHandle;
    }

    public String toString() {
        return Long.toString(this.mNativeHandle);
    }
}
