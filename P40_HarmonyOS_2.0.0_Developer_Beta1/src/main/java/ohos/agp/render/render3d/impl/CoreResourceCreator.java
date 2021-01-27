package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreResourceCreator {
    private transient long agpCptrCoreResourceCreator;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CoreResourceCreator(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreResourceCreator = j;
    }

    static long getCptr(CoreResourceCreator coreResourceCreator) {
        if (coreResourceCreator == null) {
            return 0;
        }
        return coreResourceCreator.agpCptrCoreResourceCreator;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreResourceCreator != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreResourceCreator(this.agpCptrCoreResourceCreator);
                }
                this.agpCptrCoreResourceCreator = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreResourceCreator coreResourceCreator, boolean z) {
        if (coreResourceCreator != null) {
            synchronized (coreResourceCreator.delLock) {
                coreResourceCreator.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreResourceCreator);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle create(String str, String str2, CoreImageCreateInfo coreImageCreateInfo) {
        return new CoreResourceHandle(CoreJni.createInCoreResourceCreator0(this.agpCptrCoreResourceCreator, this, str, str2, CoreImageCreateInfo.getCptr(coreImageCreateInfo), coreImageCreateInfo), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle create(String str, String str2, CoreMaterialCreateInfo coreMaterialCreateInfo) {
        return new CoreResourceHandle(CoreJni.createInCoreResourceCreator1(this.agpCptrCoreResourceCreator, this, str, str2, CoreMaterialCreateInfo.getCptr(coreMaterialCreateInfo), coreMaterialCreateInfo), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle create(String str, String str2, CoreMeshCreateInfo coreMeshCreateInfo) {
        return new CoreResourceHandle(CoreJni.createInCoreResourceCreator2(this.agpCptrCoreResourceCreator, this, str, str2, CoreMeshCreateInfo.getCptr(coreMeshCreateInfo), coreMeshCreateInfo), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle create(String str, String str2, CoreSkinCreateInfo coreSkinCreateInfo) {
        return new CoreResourceHandle(CoreJni.createInCoreResourceCreator3(this.agpCptrCoreResourceCreator, this, str, str2, CoreSkinCreateInfo.getCptr(coreSkinCreateInfo), coreSkinCreateInfo), true);
    }

    /* access modifiers changed from: package-private */
    public void erase(CoreResourceHandle coreResourceHandle) {
        CoreJni.eraseInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getImageHandle(String str) {
        return new CoreResourceHandle(CoreJni.getImageHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreImage getImageFromHandle(CoreResourceHandle coreResourceHandle) {
        long imageFromHandleInCoreResourceCreator = CoreJni.getImageFromHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
        if (imageFromHandleInCoreResourceCreator == 0) {
            return null;
        }
        return new CoreImage(imageFromHandleInCoreResourceCreator, false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getMaterialHandle(String str) {
        return new CoreResourceHandle(CoreJni.getMaterialHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMaterial getMaterialFromHandle(CoreResourceHandle coreResourceHandle) {
        long materialFromHandleInCoreResourceCreator = CoreJni.getMaterialFromHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
        if (materialFromHandleInCoreResourceCreator == 0) {
            return null;
        }
        return new CoreMaterial(materialFromHandleInCoreResourceCreator, false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getMeshHandle(String str) {
        return new CoreResourceHandle(CoreJni.getMeshHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMesh getMeshFromHandle(CoreResourceHandle coreResourceHandle) {
        long meshFromHandleInCoreResourceCreator = CoreJni.getMeshFromHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
        if (meshFromHandleInCoreResourceCreator == 0) {
            return null;
        }
        return new CoreMesh(meshFromHandleInCoreResourceCreator, false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getAnimationHandle(String str) {
        return new CoreResourceHandle(CoreJni.getAnimationHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreAnimation getAnimationFromHandle(CoreResourceHandle coreResourceHandle) {
        long animationFromHandleInCoreResourceCreator = CoreJni.getAnimationFromHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
        if (animationFromHandleInCoreResourceCreator == 0) {
            return null;
        }
        return new CoreAnimation(animationFromHandleInCoreResourceCreator, false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getSkinHandle(String str) {
        return new CoreResourceHandle(CoreJni.getSkinHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreSkin getSkinFromHandle(CoreResourceHandle coreResourceHandle) {
        long skinFromHandleInCoreResourceCreator = CoreJni.getSkinFromHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
        if (skinFromHandleInCoreResourceCreator == 0) {
            return null;
        }
        return new CoreSkin(skinFromHandleInCoreResourceCreator, false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getHandle(String str) {
        return new CoreResourceHandle(CoreJni.getHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResource getResourceFromHandle(CoreResourceHandle coreResourceHandle) {
        long resourceFromHandleInCoreResourceCreator = CoreJni.getResourceFromHandleInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
        if (resourceFromHandleInCoreResourceCreator == 0) {
            return null;
        }
        return new CoreResource(resourceFromHandleInCoreResourceCreator, false);
    }

    static class CoreInfo {
        private transient long agpCptrCoreResourceCreatorInfo;
        private final Object delLock;
        transient boolean isAgpCmemOwn;

        CoreInfo(long j, boolean z) {
            this.delLock = new Object();
            this.isAgpCmemOwn = z;
            this.agpCptrCoreResourceCreatorInfo = j;
        }

        static long getCptr(CoreInfo coreInfo) {
            if (coreInfo == null) {
                return 0;
            }
            return coreInfo.agpCptrCoreResourceCreatorInfo;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.delLock) {
                if (this.agpCptrCoreResourceCreatorInfo != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreResourceCreatorCoreInfo(this.agpCptrCoreResourceCreatorInfo);
                    }
                    this.agpCptrCoreResourceCreatorInfo = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CoreInfo coreInfo, boolean z) {
            if (coreInfo != null) {
                synchronized (coreInfo.delLock) {
                    coreInfo.isAgpCmemOwn = z;
                }
            }
            return getCptr(coreInfo);
        }

        /* access modifiers changed from: package-private */
        public void setUri(String str) {
            CoreJni.setVaruriCoreResourceCreatorCoreInfo(this.agpCptrCoreResourceCreatorInfo, this, str);
        }

        /* access modifiers changed from: package-private */
        public String getUri() {
            return CoreJni.getVaruriCoreResourceCreatorCoreInfo(this.agpCptrCoreResourceCreatorInfo, this);
        }

        /* access modifiers changed from: package-private */
        public void setName(String str) {
            CoreJni.setVarnameCoreResourceCreatorCoreInfo(this.agpCptrCoreResourceCreatorInfo, this, str);
        }

        /* access modifiers changed from: package-private */
        public String getName() {
            return CoreJni.getVarnameCoreResourceCreatorCoreInfo(this.agpCptrCoreResourceCreatorInfo, this);
        }

        /* access modifiers changed from: package-private */
        public void setHandle(CoreResourceHandle coreResourceHandle) {
            CoreJni.setVarhandleCoreResourceCreatorCoreInfo(this.agpCptrCoreResourceCreatorInfo, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
        }

        /* access modifiers changed from: package-private */
        public CoreResourceHandle getHandle() {
            long varhandleCoreResourceCreatorCoreInfo = CoreJni.getVarhandleCoreResourceCreatorCoreInfo(this.agpCptrCoreResourceCreatorInfo, this);
            if (varhandleCoreResourceCreatorCoreInfo == 0) {
                return null;
            }
            return new CoreResourceHandle(varhandleCoreResourceCreatorCoreInfo, false);
        }

        CoreInfo() {
            this(CoreJni.newCoreInfo(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreatorInfoArray getMaterials() {
        return new CoreResourceCreatorInfoArray(CoreJni.getMaterialsInCoreResourceCreator(this.agpCptrCoreResourceCreator, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreatorInfoArray getMeshes() {
        return new CoreResourceCreatorInfoArray(CoreJni.getMeshesInCoreResourceCreator(this.agpCptrCoreResourceCreator, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreatorInfoArray getAnimations() {
        return new CoreResourceCreatorInfoArray(CoreJni.getAnimationsInCoreResourceCreator(this.agpCptrCoreResourceCreator, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreatorInfoArray getSkins() {
        return new CoreResourceCreatorInfoArray(CoreJni.getSkinsInCoreResourceCreator(this.agpCptrCoreResourceCreator, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreatorInfoArray getImages() {
        return new CoreResourceCreatorInfoArray(CoreJni.getImagesInCoreResourceCreator(this.agpCptrCoreResourceCreator, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreShaderManager getShaderManager() {
        return new CoreShaderManager(CoreJni.getShaderManagerInCoreResourceCreator(this.agpCptrCoreResourceCreator, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderDataStoreManager getRenderDataStoreManager() {
        return new CoreRenderDataStoreManager(CoreJni.getRenderDataStoreManagerInCoreResourceCreator(this.agpCptrCoreResourceCreator, this), false);
    }

    /* access modifiers changed from: package-private */
    public boolean isValid(CoreResourceHandle coreResourceHandle) {
        return CoreJni.isValidInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean isMaterial(CoreResourceHandle coreResourceHandle) {
        return CoreJni.isMaterialInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean isMesh(CoreResourceHandle coreResourceHandle) {
        return CoreJni.isMeshInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimation(CoreResourceHandle coreResourceHandle) {
        return CoreJni.isAnimationInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean isSkin(CoreResourceHandle coreResourceHandle) {
        return CoreJni.isSkinInCoreResourceCreator(this.agpCptrCoreResourceCreator, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }
}
