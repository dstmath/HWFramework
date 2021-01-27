package ohos.agp.render.render3d.impl;

class CoreRenderHandleUtil {
    private transient long agpCptrCoreRenderHandleUtil;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreRenderHandleUtil(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreRenderHandleUtil = j;
    }

    static long getCptr(CoreRenderHandleUtil coreRenderHandleUtil) {
        if (coreRenderHandleUtil == null) {
            return 0;
        }
        return coreRenderHandleUtil.agpCptrCoreRenderHandleUtil;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreRenderHandleUtil != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreRenderHandleUtil(this.agpCptrCoreRenderHandleUtil);
                }
                this.agpCptrCoreRenderHandleUtil = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderHandleUtil coreRenderHandleUtil, boolean z) {
        if (coreRenderHandleUtil != null) {
            synchronized (coreRenderHandleUtil.delLock) {
                coreRenderHandleUtil.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRenderHandleUtil);
    }

    static long getUniqueId(CoreRenderHandle coreRenderHandle) {
        return CoreJni.getUniqueIdInCoreRenderHandleUtil(CoreRenderHandle.getCptr(coreRenderHandle), coreRenderHandle);
    }

    static boolean isValid(CoreRenderHandle coreRenderHandle) {
        return CoreJni.isValidInCoreRenderHandleUtil(CoreRenderHandle.getCptr(coreRenderHandle), coreRenderHandle);
    }

    CoreRenderHandleUtil() {
        this(CoreJni.newCoreRenderHandleUtil(), true);
    }
}
