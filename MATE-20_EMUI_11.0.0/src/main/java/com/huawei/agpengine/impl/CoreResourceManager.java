package com.huawei.agpengine.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreResourceManager extends CoreInterface {
    private transient long agpCptr;

    CoreResourceManager(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreResourceManager(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreResourceManager obj) {
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
    @Override // com.huawei.agpengine.impl.CoreInterface
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
        super.delete();
    }

    /* access modifiers changed from: package-private */
    public void erase(CoreResourceHandle handle) {
        CoreJni.eraseInCoreResourceManager(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getHandle(String uri) {
        return new CoreResourceHandle(CoreJni.getHandleInCoreResourceManager(this.agpCptr, this, uri), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResource getResourceFromHandle(CoreResourceHandle handle) {
        long cptr = CoreJni.getResourceFromHandleInCoreResourceManager(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
        if (cptr == 0) {
            return null;
        }
        return new CoreResource(cptr, false);
    }

    static class CoreResourceInfo {
        private transient long agpCptr;
        transient boolean isAgpCmemOwn;

        CoreResourceInfo(long cptr, boolean isCmemoryOwn) {
            this.isAgpCmemOwn = isCmemoryOwn;
            this.agpCptr = cptr;
        }

        static long getCptr(CoreResourceInfo obj) {
            long j;
            if (obj == null) {
                return 0;
            }
            synchronized (obj) {
                j = obj.agpCptr;
            }
            return j;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public synchronized void delete() {
            if (this.agpCptr != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreResourceManagerCoreResourceInfo(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void setUri(String value) {
            CoreJni.setVaruriCoreResourceManagerCoreResourceInfo(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public String getUri() {
            return CoreJni.getVaruriCoreResourceManagerCoreResourceInfo(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setName(String value) {
            CoreJni.setVarnameCoreResourceManagerCoreResourceInfo(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public String getName() {
            return CoreJni.getVarnameCoreResourceManagerCoreResourceInfo(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setHandle(CoreResourceHandle value) {
            CoreJni.setVarhandleCoreResourceManagerCoreResourceInfo(this.agpCptr, this, CoreResourceHandle.getCptr(value), value);
        }

        /* access modifiers changed from: package-private */
        public CoreResourceHandle getHandle() {
            long cptr = CoreJni.getVarhandleCoreResourceManagerCoreResourceInfo(this.agpCptr, this);
            if (cptr == 0) {
                return null;
            }
            return new CoreResourceHandle(cptr, false);
        }

        CoreResourceInfo() {
            this(CoreJni.newCoreResourceInfo(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public CoreResourceInfoArray getResources() {
        return new CoreResourceInfoArray(CoreJni.getResourcesInCoreResourceManager0(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceInfoArray getResources(BigInteger resourceType) {
        return new CoreResourceInfoArray(CoreJni.getResourcesInCoreResourceManager1(this.agpCptr, this, resourceType), true);
    }

    /* access modifiers changed from: package-private */
    public boolean isValid(CoreResourceHandle handle) {
        return CoreJni.isValidInCoreResourceManager(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceManager getGpuResourceManager() {
        return new CoreGpuResourceManager(CoreJni.getGpuResourceManagerInCoreResourceManager(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreShaderManager getShaderManager() {
        return new CoreShaderManager(CoreJni.getShaderManagerInCoreResourceManager(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderDataStoreManager getRenderDataStoreManager() {
        return new CoreRenderDataStoreManager(CoreJni.getRenderDataStoreManagerInCoreResourceManager(this.agpCptr, this), false);
    }
}
