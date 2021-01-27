package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMorphingSystem extends CoreSystem {
    private transient long agpCptrCoreMorphingSystem;
    private final Object delLock = new Object();

    CoreMorphingSystem(long j, boolean z) {
        super(CoreJni.classUpcastCoreMorphingSystem(j), z);
        this.agpCptrCoreMorphingSystem = j;
    }

    static long getCptr(CoreMorphingSystem coreMorphingSystem) {
        if (coreMorphingSystem == null) {
            return 0;
        }
        return coreMorphingSystem.agpCptrCoreMorphingSystem;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.render.render3d.impl.CoreSystem
    public void finalize() {
        delete();
        super.finalize();
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreSystem
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreMorphingSystem != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreMorphingSystem(this.agpCptrCoreMorphingSystem);
                }
                this.agpCptrCoreMorphingSystem = 0;
            }
            super.delete();
        }
    }

    static long getCptrAndSetMemOwn(CoreMorphingSystem coreMorphingSystem, boolean z) {
        if (coreMorphingSystem != null) {
            coreMorphingSystem.isAgpCmemOwn = z;
        }
        return getCptr(coreMorphingSystem);
    }

    static long getMaxCharTableSize() {
        return CoreJni.getCoreMorphingSystemMaxCharTableSize();
    }

    /* access modifiers changed from: package-private */
    public long createHandle() {
        return CoreJni.createHandleInCoreMorphingSystem(this.agpCptrCoreMorphingSystem, this);
    }

    /* access modifiers changed from: package-private */
    public void releaseHandle(long j) {
        CoreJni.releaseHandleInCoreMorphingSystem(this.agpCptrCoreMorphingSystem, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getTargetCount(long j) {
        return CoreJni.getTargetCountInCoreMorphingSystem(this.agpCptrCoreMorphingSystem, this, j);
    }

    /* access modifiers changed from: package-private */
    public CoreStringViewArrayView getTargetNames(long j) {
        return new CoreStringViewArrayView(CoreJni.getTargetNamesInCoreMorphingSystem(this.agpCptrCoreMorphingSystem, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public CoreFloatArrayView getTargetWeights(long j) {
        return new CoreFloatArrayView(CoreJni.getTargetWeightsInCoreMorphingSystem(this.agpCptrCoreMorphingSystem, this, j), true);
    }

    /* access modifiers changed from: package-private */
    public void setDataStoreManager(CoreRenderDataStoreManager coreRenderDataStoreManager) {
        CoreJni.setDataStoreManagerInCoreMorphingSystem(this.agpCptrCoreMorphingSystem, this, CoreRenderDataStoreManager.getCptr(coreRenderDataStoreManager), coreRenderDataStoreManager);
    }

    /* access modifiers changed from: package-private */
    public void setDataStoreName(String str) {
        CoreJni.setDataStoreNameInCoreMorphingSystem(this.agpCptrCoreMorphingSystem, this, str);
    }

    static class CoreProperties {
        private transient long agpCptrCoreMorphingSystemProperties;
        private final Object delLock = new Object();
        transient boolean isAgpCmemOwn;

        CoreProperties(long j, boolean z) {
            this.isAgpCmemOwn = z;
            this.agpCptrCoreMorphingSystemProperties = j;
        }

        static long getCptr(CoreProperties coreProperties) {
            if (coreProperties == null) {
                return 0;
            }
            return coreProperties.agpCptrCoreMorphingSystemProperties;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.delLock) {
                if (this.agpCptrCoreMorphingSystemProperties != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreMorphingSystemCoreProperties(this.agpCptrCoreMorphingSystemProperties);
                    }
                    this.agpCptrCoreMorphingSystemProperties = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CoreProperties coreProperties, boolean z) {
            if (coreProperties != null) {
                synchronized (coreProperties.delLock) {
                    coreProperties.isAgpCmemOwn = z;
                }
            }
            return getCptr(coreProperties);
        }

        /* access modifiers changed from: package-private */
        public void setDataStoreManager(CoreRenderDataStoreManager coreRenderDataStoreManager) {
            CoreJni.setVardataStoreManagerCoreMorphingSystemCoreProperties(this.agpCptrCoreMorphingSystemProperties, this, CoreRenderDataStoreManager.getCptr(coreRenderDataStoreManager), coreRenderDataStoreManager);
        }

        /* access modifiers changed from: package-private */
        public CoreRenderDataStoreManager getDataStoreManager() {
            long vardataStoreManagerCoreMorphingSystemCoreProperties = CoreJni.getVardataStoreManagerCoreMorphingSystemCoreProperties(this.agpCptrCoreMorphingSystemProperties, this);
            if (vardataStoreManagerCoreMorphingSystemCoreProperties == 0) {
                return null;
            }
            return new CoreRenderDataStoreManager(vardataStoreManagerCoreMorphingSystemCoreProperties, false);
        }

        /* access modifiers changed from: package-private */
        public void setDataStoreName(String str) {
            CoreJni.setVardataStoreNameCoreMorphingSystemCoreProperties(this.agpCptrCoreMorphingSystemProperties, this, str);
        }

        /* access modifiers changed from: package-private */
        public String getDataStoreName() {
            return CoreJni.getVardataStoreNameCoreMorphingSystemCoreProperties(this.agpCptrCoreMorphingSystemProperties, this);
        }
    }

    /* access modifiers changed from: package-private */
    public void setTargetNamesArray(long j, String[] strArr, int i) {
        CoreJni.setTargetNamesArrayInCoreMorphingSystem(this.agpCptrCoreMorphingSystem, this, j, strArr, i);
    }

    /* access modifiers changed from: package-private */
    public void setTargetWeightsArray(long j, float[] fArr, int i) {
        CoreJni.setTargetWeightsArrayInCoreMorphingSystem(this.agpCptrCoreMorphingSystem, this, j, fArr, i);
    }
}
