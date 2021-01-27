package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreResourceManager {
    private transient long agpCptrCoreResourceManager;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CoreResourceManager(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreResourceManager = j;
    }

    static long getCptr(CoreResourceManager coreResourceManager) {
        if (coreResourceManager == null) {
            return 0;
        }
        return coreResourceManager.agpCptrCoreResourceManager;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreResourceManager != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreResourceManager(this.agpCptrCoreResourceManager);
                }
                this.agpCptrCoreResourceManager = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreResourceManager coreResourceManager, boolean z) {
        if (coreResourceManager != null) {
            synchronized (coreResourceManager.delLock) {
                coreResourceManager.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreResourceManager);
    }

    /* access modifiers changed from: package-private */
    public void erase(CoreResourceHandle coreResourceHandle) {
        CoreJni.eraseInCoreResourceManager(this.agpCptrCoreResourceManager, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getHandle(String str) {
        return new CoreResourceHandle(CoreJni.getHandleInCoreResourceManager(this.agpCptrCoreResourceManager, this, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResource getResourceFromHandle(CoreResourceHandle coreResourceHandle) {
        long resourceFromHandleInCoreResourceManager = CoreJni.getResourceFromHandleInCoreResourceManager(this.agpCptrCoreResourceManager, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
        if (resourceFromHandleInCoreResourceManager == 0) {
            return null;
        }
        return new CoreResource(resourceFromHandleInCoreResourceManager, false);
    }

    static class CoreResourceInfo {
        private transient long agpCptr;
        private final Object delLock;
        transient boolean isAgpCmemOwn;

        CoreResourceInfo(long j, boolean z) {
            this.delLock = new Object();
            this.isAgpCmemOwn = z;
            this.agpCptr = j;
        }

        static long getCptr(CoreResourceInfo coreResourceInfo) {
            if (coreResourceInfo == null) {
                return 0;
            }
            return coreResourceInfo.agpCptr;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.delLock) {
                if (this.agpCptr != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreResourceManagerCoreResourceInfo(this.agpCptr);
                    }
                    this.agpCptr = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CoreResourceInfo coreResourceInfo, boolean z) {
            if (coreResourceInfo != null) {
                synchronized (coreResourceInfo.delLock) {
                    coreResourceInfo.isAgpCmemOwn = z;
                }
            }
            return getCptr(coreResourceInfo);
        }

        /* access modifiers changed from: package-private */
        public void setUri(String str) {
            CoreJni.setVaruriCoreResourceManagerCoreResourceInfo(this.agpCptr, this, str);
        }

        /* access modifiers changed from: package-private */
        public String getUri() {
            return CoreJni.getVaruriCoreResourceManagerCoreResourceInfo(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setName(String str) {
            CoreJni.setVarnameCoreResourceManagerCoreResourceInfo(this.agpCptr, this, str);
        }

        /* access modifiers changed from: package-private */
        public String getName() {
            return CoreJni.getVarnameCoreResourceManagerCoreResourceInfo(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setHandle(CoreResourceHandle coreResourceHandle) {
            CoreJni.setVarhandleCoreResourceManagerCoreResourceInfo(this.agpCptr, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
        }

        /* access modifiers changed from: package-private */
        public CoreResourceHandle getHandle() {
            long varhandleCoreResourceManagerCoreResourceInfo = CoreJni.getVarhandleCoreResourceManagerCoreResourceInfo(this.agpCptr, this);
            if (varhandleCoreResourceManagerCoreResourceInfo == 0) {
                return null;
            }
            return new CoreResourceHandle(varhandleCoreResourceManagerCoreResourceInfo, false);
        }

        CoreResourceInfo() {
            this(CoreJni.newCoreResourceInfo(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public CoreResourceInfoArray getResources() {
        return new CoreResourceInfoArray(CoreJni.getResourcesInCoreResourceManager0(this.agpCptrCoreResourceManager, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceInfoArray getResources(long j) {
        return new CoreResourceInfoArray(CoreJni.getResourcesInCoreResourceManager1(this.agpCptrCoreResourceManager, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public boolean isValid(CoreResourceHandle coreResourceHandle) {
        return CoreJni.isValidInCoreResourceManager(this.agpCptrCoreResourceManager, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreShaderManager getShaderManager() {
        return new CoreShaderManager(CoreJni.getShaderManagerInCoreResourceManager(this.agpCptrCoreResourceManager, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderDataStoreManager getRenderDataStoreManager() {
        return new CoreRenderDataStoreManager(CoreJni.getRenderDataStoreManagerInCoreResourceManager(this.agpCptrCoreResourceManager, this), false);
    }
}
