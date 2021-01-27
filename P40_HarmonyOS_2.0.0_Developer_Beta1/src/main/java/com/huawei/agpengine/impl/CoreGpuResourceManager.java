package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreGpuResourceManager {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGpuResourceManager(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGpuResourceManager obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long create(String name, CoreGpuBufferDesc desc) {
        return CoreJni.createInCoreGpuResourceManager0(this.agpCptr, this, name, CoreGpuBufferDesc.getCptr(desc), desc);
    }

    /* access modifiers changed from: package-private */
    public long create(String name, CoreGpuBufferDesc desc, CoreByteArrayView data) {
        return CoreJni.createInCoreGpuResourceManager1(this.agpCptr, this, name, CoreGpuBufferDesc.getCptr(desc), desc, CoreByteArrayView.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public long create(CoreGpuBufferDesc desc, CoreByteArrayView data) {
        return CoreJni.createInCoreGpuResourceManager2(this.agpCptr, this, CoreGpuBufferDesc.getCptr(desc), desc, CoreByteArrayView.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public long create(long replacedResourceHandle, CoreGpuBufferDesc desc) {
        return CoreJni.createInCoreGpuResourceManager3(this.agpCptr, this, replacedResourceHandle, CoreGpuBufferDesc.getCptr(desc), desc);
    }

    /* access modifiers changed from: package-private */
    public long create(CoreGpuBufferDesc desc) {
        return CoreJni.createInCoreGpuResourceManager4(this.agpCptr, this, CoreGpuBufferDesc.getCptr(desc), desc);
    }

    /* access modifiers changed from: package-private */
    public long create(String name, CoreGpuImageDesc desc) {
        return CoreJni.createInCoreGpuResourceManager5(this.agpCptr, this, name, CoreGpuImageDesc.getCptr(desc), desc);
    }

    /* access modifiers changed from: package-private */
    public long create(String name, CoreGpuImageDesc desc, CoreByteArrayView data) {
        return CoreJni.createInCoreGpuResourceManager6(this.agpCptr, this, name, CoreGpuImageDesc.getCptr(desc), desc, CoreByteArrayView.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public long create(String name, CoreGpuImageDesc desc, CoreByteArrayView data, CoreBufferImageCopyArrayView bufferImageCopies) {
        return CoreJni.createInCoreGpuResourceManager7(this.agpCptr, this, name, CoreGpuImageDesc.getCptr(desc), desc, CoreByteArrayView.getCptr(data), data, CoreBufferImageCopyArrayView.getCptr(bufferImageCopies), bufferImageCopies);
    }

    /* access modifiers changed from: package-private */
    public long create(long replacedResourceHandle, CoreGpuImageDesc desc) {
        return CoreJni.createInCoreGpuResourceManager8(this.agpCptr, this, replacedResourceHandle, CoreGpuImageDesc.getCptr(desc), desc);
    }

    /* access modifiers changed from: package-private */
    public long create(CoreGpuImageDesc desc) {
        return CoreJni.createInCoreGpuResourceManager9(this.agpCptr, this, CoreGpuImageDesc.getCptr(desc), desc);
    }

    /* access modifiers changed from: package-private */
    public long create(CoreGpuImageDesc desc, CoreByteArrayView data) {
        return CoreJni.createInCoreGpuResourceManager10(this.agpCptr, this, CoreGpuImageDesc.getCptr(desc), desc, CoreByteArrayView.getCptr(data), data);
    }

    /* access modifiers changed from: package-private */
    public long create(CoreGpuImageDesc desc, CoreByteArrayView data, CoreBufferImageCopyArrayView bufferImageCopies) {
        return CoreJni.createInCoreGpuResourceManager11(this.agpCptr, this, CoreGpuImageDesc.getCptr(desc), desc, CoreByteArrayView.getCptr(data), data, CoreBufferImageCopyArrayView.getCptr(bufferImageCopies), bufferImageCopies);
    }

    /* access modifiers changed from: package-private */
    public long create(String name, CoreGpuSamplerDesc desc) {
        return CoreJni.createInCoreGpuResourceManager12(this.agpCptr, this, name, CoreGpuSamplerDesc.getCptr(desc), desc);
    }

    /* access modifiers changed from: package-private */
    public void destroy(long handle) {
        CoreJni.destroyInCoreGpuResourceManager(this.agpCptr, this, handle);
    }

    /* access modifiers changed from: package-private */
    public long getBufferHandle(String name) {
        return CoreJni.getBufferHandleInCoreGpuResourceManager(this.agpCptr, this, name);
    }

    /* access modifiers changed from: package-private */
    public long getImageHandle(String name) {
        return CoreJni.getImageHandleInCoreGpuResourceManager(this.agpCptr, this, name);
    }

    /* access modifiers changed from: package-private */
    public long getSamplerHandle(String name) {
        return CoreJni.getSamplerHandleInCoreGpuResourceManager(this.agpCptr, this, name);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuBufferDesc getBufferDescriptor(long handle) {
        return new CoreGpuBufferDesc(CoreJni.getBufferDescriptorInCoreGpuResourceManager(this.agpCptr, this, handle), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuImageDesc getImageDescriptor(long handle) {
        return new CoreGpuImageDesc(CoreJni.getImageDescriptorInCoreGpuResourceManager(this.agpCptr, this, handle), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuSamplerDesc getSamplerDescriptor(long handle) {
        return new CoreGpuSamplerDesc(CoreJni.getSamplerDescriptorInCoreGpuResourceManager(this.agpCptr, this, handle), true);
    }

    /* access modifiers changed from: package-private */
    public void waitForIdleAndDestroyGpuResources() {
        CoreJni.waitForIdleAndDestroyGpuResourcesInCoreGpuResourceManager(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public boolean isValid(long handle) {
        return CoreJni.isValidInCoreGpuResourceManager(this.agpCptr, this, handle);
    }

    /* access modifiers changed from: package-private */
    public boolean isGpuBuffer(long handle) {
        return CoreJni.isGpuBufferInCoreGpuResourceManager(this.agpCptr, this, handle);
    }

    /* access modifiers changed from: package-private */
    public boolean isGpuImage(long handle) {
        return CoreJni.isGpuImageInCoreGpuResourceManager(this.agpCptr, this, handle);
    }
}
