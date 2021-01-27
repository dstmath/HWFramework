package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreResourceCreator {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreResourceCreator(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreResourceCreator obj) {
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
    public CoreResourceHandle create(String uri, String name, CoreImageCreateInfo info) {
        return new CoreResourceHandle(CoreJni.createInCoreResourceCreator0(this.agpCptr, this, uri, name, CoreImageCreateInfo.getCptr(info), info), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle create(String uri, String name, CoreMaterialCreateInfo info) {
        return new CoreResourceHandle(CoreJni.createInCoreResourceCreator1(this.agpCptr, this, uri, name, CoreMaterialCreateInfo.getCptr(info), info), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle create(String uri, String name, CoreMeshCreateInfo info) {
        return new CoreResourceHandle(CoreJni.createInCoreResourceCreator2(this.agpCptr, this, uri, name, CoreMeshCreateInfo.getCptr(info), info), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle create(String uri, String name, CoreSkinCreateInfo info) {
        return new CoreResourceHandle(CoreJni.createInCoreResourceCreator3(this.agpCptr, this, uri, name, CoreSkinCreateInfo.getCptr(info), info), true);
    }

    /* access modifiers changed from: package-private */
    public void erase(CoreResourceHandle handle) {
        CoreJni.eraseInCoreResourceCreator(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getImageHandle(String uri) {
        return new CoreResourceHandle(CoreJni.getImageHandleInCoreResourceCreator(this.agpCptr, this, uri), true);
    }

    /* access modifiers changed from: package-private */
    public CoreImage getImageFromHandle(CoreResourceHandle handle) {
        long cptr = CoreJni.getImageFromHandleInCoreResourceCreator(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
        if (cptr == 0) {
            return null;
        }
        return new CoreImage(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getMaterialHandle(String uri) {
        return new CoreResourceHandle(CoreJni.getMaterialHandleInCoreResourceCreator(this.agpCptr, this, uri), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMaterial getMaterialFromHandle(CoreResourceHandle handle) {
        long cptr = CoreJni.getMaterialFromHandleInCoreResourceCreator(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
        if (cptr == 0) {
            return null;
        }
        return new CoreMaterial(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getMeshHandle(String uri) {
        return new CoreResourceHandle(CoreJni.getMeshHandleInCoreResourceCreator(this.agpCptr, this, uri), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMesh getMeshFromHandle(CoreResourceHandle handle) {
        long cptr = CoreJni.getMeshFromHandleInCoreResourceCreator(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
        if (cptr == 0) {
            return null;
        }
        return new CoreMesh(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getAnimationHandle(String uri) {
        return new CoreResourceHandle(CoreJni.getAnimationHandleInCoreResourceCreator(this.agpCptr, this, uri), true);
    }

    /* access modifiers changed from: package-private */
    public CoreAnimation getAnimationFromHandle(CoreResourceHandle handle) {
        long cptr = CoreJni.getAnimationFromHandleInCoreResourceCreator(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
        if (cptr == 0) {
            return null;
        }
        return new CoreAnimation(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getSkinHandle(String uri) {
        return new CoreResourceHandle(CoreJni.getSkinHandleInCoreResourceCreator(this.agpCptr, this, uri), true);
    }

    /* access modifiers changed from: package-private */
    public CoreSkin getSkinFromHandle(CoreResourceHandle handle) {
        long cptr = CoreJni.getSkinFromHandleInCoreResourceCreator(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
        if (cptr == 0) {
            return null;
        }
        return new CoreSkin(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getHandle(String uri) {
        return new CoreResourceHandle(CoreJni.getHandleInCoreResourceCreator(this.agpCptr, this, uri), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResource getResourceFromHandle(CoreResourceHandle handle) {
        long cptr = CoreJni.getResourceFromHandleInCoreResourceCreator(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
        if (cptr == 0) {
            return null;
        }
        return new CoreResource(cptr, false);
    }

    static class CoreInfo {
        private transient long agpCptr;
        transient boolean isAgpCmemOwn;

        CoreInfo(long cptr, boolean isCmemoryOwn) {
            this.isAgpCmemOwn = isCmemoryOwn;
            this.agpCptr = cptr;
        }

        static long getCptr(CoreInfo obj) {
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
                    CoreJni.deleteCoreResourceCreatorCoreInfo(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void setUri(String value) {
            CoreJni.setVaruriCoreResourceCreatorCoreInfo(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public String getUri() {
            return CoreJni.getVaruriCoreResourceCreatorCoreInfo(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setName(String value) {
            CoreJni.setVarnameCoreResourceCreatorCoreInfo(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public String getName() {
            return CoreJni.getVarnameCoreResourceCreatorCoreInfo(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setHandle(CoreResourceHandle value) {
            CoreJni.setVarhandleCoreResourceCreatorCoreInfo(this.agpCptr, this, CoreResourceHandle.getCptr(value), value);
        }

        /* access modifiers changed from: package-private */
        public CoreResourceHandle getHandle() {
            long cptr = CoreJni.getVarhandleCoreResourceCreatorCoreInfo(this.agpCptr, this);
            if (cptr == 0) {
                return null;
            }
            return new CoreResourceHandle(cptr, false);
        }

        CoreInfo() {
            this(CoreJni.newCoreInfo(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreatorInfoArray getMaterials() {
        return new CoreResourceCreatorInfoArray(CoreJni.getMaterialsInCoreResourceCreator(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreatorInfoArray getMeshes() {
        return new CoreResourceCreatorInfoArray(CoreJni.getMeshesInCoreResourceCreator(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreatorInfoArray getAnimations() {
        return new CoreResourceCreatorInfoArray(CoreJni.getAnimationsInCoreResourceCreator(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreatorInfoArray getSkins() {
        return new CoreResourceCreatorInfoArray(CoreJni.getSkinsInCoreResourceCreator(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreatorInfoArray getImages() {
        return new CoreResourceCreatorInfoArray(CoreJni.getImagesInCoreResourceCreator(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreShaderManager getShaderManager() {
        return new CoreShaderManager(CoreJni.getShaderManagerInCoreResourceCreator(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderDataStoreManager getRenderDataStoreManager() {
        return new CoreRenderDataStoreManager(CoreJni.getRenderDataStoreManagerInCoreResourceCreator(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceManager getResourceManager() {
        return new CoreResourceManager(CoreJni.getResourceManagerInCoreResourceCreator(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceManager getGpuResourceManager() {
        return new CoreGpuResourceManager(CoreJni.getGpuResourceManagerInCoreResourceCreator(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public boolean isValid(CoreResourceHandle handle) {
        return CoreJni.isValidInCoreResourceCreator0(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
    }

    /* access modifiers changed from: package-private */
    public boolean isValid(long handle) {
        return CoreJni.isValidInCoreResourceCreator1(this.agpCptr, this, handle);
    }

    /* access modifiers changed from: package-private */
    public boolean isMaterial(CoreResourceHandle handle) {
        return CoreJni.isMaterialInCoreResourceCreator(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
    }

    /* access modifiers changed from: package-private */
    public boolean isMesh(CoreResourceHandle handle) {
        return CoreJni.isMeshInCoreResourceCreator(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimation(CoreResourceHandle handle) {
        return CoreJni.isAnimationInCoreResourceCreator(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
    }

    /* access modifiers changed from: package-private */
    public boolean isSkin(CoreResourceHandle handle) {
        return CoreJni.isSkinInCoreResourceCreator(this.agpCptr, this, CoreResourceHandle.getCptr(handle), handle);
    }
}
