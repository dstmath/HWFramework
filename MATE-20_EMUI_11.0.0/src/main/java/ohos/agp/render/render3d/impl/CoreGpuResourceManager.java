package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreGpuResourceManager {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreGpuResourceManager(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreGpuResourceManager coreGpuResourceManager) {
        if (coreGpuResourceManager == null) {
            return 0;
        }
        return coreGpuResourceManager.agpCptr;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptr != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptr = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreGpuResourceManager coreGpuResourceManager, boolean z) {
        if (coreGpuResourceManager != null) {
            synchronized (coreGpuResourceManager.lock) {
                coreGpuResourceManager.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreGpuResourceManager);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle create(String str, CoreGpuBufferDesc coreGpuBufferDesc) {
        return new CoreGpuResourceHandle(CoreJni.createInCoreGpuResourceManager0(this.agpCptr, this, str, CoreGpuBufferDesc.getCptr(coreGpuBufferDesc), coreGpuBufferDesc), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle create(String str, CoreGpuBufferDesc coreGpuBufferDesc, CoreByteArrayView coreByteArrayView) {
        return new CoreGpuResourceHandle(CoreJni.createInCoreGpuResourceManager1(this.agpCptr, this, str, CoreGpuBufferDesc.getCptr(coreGpuBufferDesc), coreGpuBufferDesc, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle create(CoreGpuBufferDesc coreGpuBufferDesc, CoreByteArrayView coreByteArrayView) {
        return new CoreGpuResourceHandle(CoreJni.createInCoreGpuResourceManager2(this.agpCptr, this, CoreGpuBufferDesc.getCptr(coreGpuBufferDesc), coreGpuBufferDesc, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle create(CoreGpuBufferDesc coreGpuBufferDesc) {
        return new CoreGpuResourceHandle(CoreJni.createInCoreGpuResourceManager3(this.agpCptr, this, CoreGpuBufferDesc.getCptr(coreGpuBufferDesc), coreGpuBufferDesc), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle create(String str, CoreGpuImageDesc coreGpuImageDesc) {
        return new CoreGpuResourceHandle(CoreJni.createInCoreGpuResourceManager4(this.agpCptr, this, str, CoreGpuImageDesc.getCptr(coreGpuImageDesc), coreGpuImageDesc), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle create(String str, CoreGpuImageDesc coreGpuImageDesc, CoreByteArrayView coreByteArrayView) {
        return new CoreGpuResourceHandle(CoreJni.createInCoreGpuResourceManager5(this.agpCptr, this, str, CoreGpuImageDesc.getCptr(coreGpuImageDesc), coreGpuImageDesc, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle create(String str, CoreGpuImageDesc coreGpuImageDesc, CoreByteArrayView coreByteArrayView, CoreBufferImageCopyArrayView coreBufferImageCopyArrayView) {
        return new CoreGpuResourceHandle(CoreJni.createInCoreGpuResourceManager6(this.agpCptr, this, str, CoreGpuImageDesc.getCptr(coreGpuImageDesc), coreGpuImageDesc, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView, CoreBufferImageCopyArrayView.getCptr(coreBufferImageCopyArrayView), coreBufferImageCopyArrayView), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle create(CoreGpuImageDesc coreGpuImageDesc) {
        return new CoreGpuResourceHandle(CoreJni.createInCoreGpuResourceManager7(this.agpCptr, this, CoreGpuImageDesc.getCptr(coreGpuImageDesc), coreGpuImageDesc), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle create(CoreGpuImageDesc coreGpuImageDesc, CoreByteArrayView coreByteArrayView) {
        return new CoreGpuResourceHandle(CoreJni.createInCoreGpuResourceManager8(this.agpCptr, this, CoreGpuImageDesc.getCptr(coreGpuImageDesc), coreGpuImageDesc, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle create(CoreGpuImageDesc coreGpuImageDesc, CoreByteArrayView coreByteArrayView, CoreBufferImageCopyArrayView coreBufferImageCopyArrayView) {
        return new CoreGpuResourceHandle(CoreJni.createInCoreGpuResourceManager9(this.agpCptr, this, CoreGpuImageDesc.getCptr(coreGpuImageDesc), coreGpuImageDesc, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView, CoreBufferImageCopyArrayView.getCptr(coreBufferImageCopyArrayView), coreBufferImageCopyArrayView), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle create(String str, CoreGpuSamplerDesc coreGpuSamplerDesc) {
        return new CoreGpuResourceHandle(CoreJni.createInCoreGpuResourceManager10(this.agpCptr, this, str, CoreGpuSamplerDesc.getCptr(coreGpuSamplerDesc), coreGpuSamplerDesc), true);
    }

    /* access modifiers changed from: package-private */
    public void destroy(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.destroyInCoreGpuResourceManager(this.agpCptr, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getBufferHandle(String str) {
        return new CoreGpuResourceHandle(CoreJni.getBufferHandleInCoreGpuResourceManager(this.agpCptr, this, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getImageHandle(String str) {
        return new CoreGpuResourceHandle(CoreJni.getImageHandleInCoreGpuResourceManager(this.agpCptr, this, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getSamplerHandle(String str) {
        return new CoreGpuResourceHandle(CoreJni.getSamplerHandleInCoreGpuResourceManager(this.agpCptr, this, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuBufferDesc getBufferDescriptor(CoreGpuResourceHandle coreGpuResourceHandle) {
        return new CoreGpuBufferDesc(CoreJni.getBufferDescriptorInCoreGpuResourceManager(this.agpCptr, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuImageDesc getImageDescriptor(CoreGpuResourceHandle coreGpuResourceHandle) {
        return new CoreGpuImageDesc(CoreJni.getImageDescriptorInCoreGpuResourceManager(this.agpCptr, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuSamplerDesc getSamplerDescriptor(CoreGpuResourceHandle coreGpuResourceHandle) {
        return new CoreGpuSamplerDesc(CoreJni.getSamplerDescriptorInCoreGpuResourceManager(this.agpCptr, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle), true);
    }

    /* access modifiers changed from: package-private */
    public void waitForIdleAndDestroyGpuResources() {
        CoreJni.waitForIdleAndDestroyGpuResourcesInCoreGpuResourceManager(this.agpCptr, this);
    }
}
