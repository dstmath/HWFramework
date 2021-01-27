package ohos.agp.render.render3d.impl;

class CoreGpuResourceHandleUtil {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreGpuResourceHandleUtil(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreGpuResourceHandleUtil coreGpuResourceHandleUtil) {
        if (coreGpuResourceHandleUtil == null) {
            return 0;
        }
        return coreGpuResourceHandleUtil.agpCptr;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptr != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreGpuResourceHandleUtil(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreGpuResourceHandleUtil coreGpuResourceHandleUtil, boolean z) {
        if (coreGpuResourceHandleUtil != null) {
            synchronized (coreGpuResourceHandleUtil.lock) {
                coreGpuResourceHandleUtil.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreGpuResourceHandleUtil);
    }

    static long getUniqueId(CoreGpuResourceHandle coreGpuResourceHandle) {
        return CoreJni.getUniqueIdInCoreGpuResourceHandleUtil(CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    static boolean isValid(CoreGpuResourceHandle coreGpuResourceHandle) {
        return CoreJni.isValidInCoreGpuResourceHandleUtil(CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    static boolean isGpuBuffer(CoreGpuResourceHandle coreGpuResourceHandle) {
        return CoreJni.isGpuBufferInCoreGpuResourceHandleUtil(CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    static boolean isGpuImage(CoreGpuResourceHandle coreGpuResourceHandle) {
        return CoreJni.isGpuImageInCoreGpuResourceHandleUtil(CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    static boolean isComputeShader(CoreGpuResourceHandle coreGpuResourceHandle) {
        return CoreJni.isComputeShaderInCoreGpuResourceHandleUtil(CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    static boolean isShader(CoreGpuResourceHandle coreGpuResourceHandle) {
        return CoreJni.isShaderInCoreGpuResourceHandleUtil(CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    CoreGpuResourceHandleUtil() {
        this(CoreJni.newCoreGpuResourceHandleUtil(), true);
    }
}
