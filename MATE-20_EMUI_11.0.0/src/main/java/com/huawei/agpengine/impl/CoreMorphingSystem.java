package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMorphingSystem extends CoreSystem {
    private transient long agpCptr;

    CoreMorphingSystem(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreMorphingSystem(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMorphingSystem obj) {
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
    @Override // com.huawei.agpengine.impl.CoreSystem
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (this.isAgpCmemOwn) {
                this.isAgpCmemOwn = false;
                CoreJni.deleteCoreMorphingSystem(this.agpCptr);
            }
            this.agpCptr = 0;
        }
        super.delete();
    }

    static long getMAX_CHAR_TABLE_SIZE() {
        return CoreJni.CoreMorphingSystem_MAX_CHAR_TABLE_SIZE_get();
    }

    /* access modifiers changed from: package-private */
    public void setDataStoreManager(CoreRenderDataStoreManager manager) {
        CoreJni.setDataStoreManagerInCoreMorphingSystem(this.agpCptr, this, CoreRenderDataStoreManager.getCptr(manager), manager);
    }

    /* access modifiers changed from: package-private */
    public void setDataStoreName(String dataStoreName) {
        CoreJni.setDataStoreNameInCoreMorphingSystem(this.agpCptr, this, dataStoreName);
    }

    static class CoreProperties {
        private transient long agpCptr;
        transient boolean isAgpCmemOwn;

        CoreProperties(long cptr, boolean isCmemoryOwn) {
            this.isAgpCmemOwn = isCmemoryOwn;
            this.agpCptr = cptr;
        }

        static long getCptr(CoreProperties obj) {
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
                    CoreJni.deleteCoreMorphingSystemCoreProperties(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void setDataStoreManager(CoreRenderDataStoreManager value) {
            CoreJni.setVardataStoreManagerCoreMorphingSystemCoreProperties(this.agpCptr, this, CoreRenderDataStoreManager.getCptr(value), value);
        }

        /* access modifiers changed from: package-private */
        public CoreRenderDataStoreManager getDataStoreManager() {
            long cptr = CoreJni.getVardataStoreManagerCoreMorphingSystemCoreProperties(this.agpCptr, this);
            if (cptr == 0) {
                return null;
            }
            return new CoreRenderDataStoreManager(cptr, false);
        }

        /* access modifiers changed from: package-private */
        public void setDataStoreName(String value) {
            CoreJni.setVardataStoreNameCoreMorphingSystemCoreProperties(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public String getDataStoreName() {
            return CoreJni.getVardataStoreNameCoreMorphingSystemCoreProperties(this.agpCptr, this);
        }

        CoreProperties() {
            this(CoreJni.newCoreProperties(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public long rlock(long handle) {
        return CoreJni.rlockInCoreMorphingSystem(this.agpCptr, this, handle);
    }

    /* access modifiers changed from: package-private */
    public void runlock(long handle) {
        CoreJni.runlockInCoreMorphingSystem(this.agpCptr, this, handle);
    }

    /* access modifiers changed from: package-private */
    public long wlock(long handle) {
        return CoreJni.wlockInCoreMorphingSystem(this.agpCptr, this, handle);
    }

    /* access modifiers changed from: package-private */
    public void wunlock(long handle) {
        CoreJni.wunlockInCoreMorphingSystem(this.agpCptr, this, handle);
    }

    /* access modifiers changed from: package-private */
    public long getTargetCount(long dataHandle) {
        return CoreJni.getTargetCountInCoreMorphingSystem(this.agpCptr, this, dataHandle);
    }

    /* access modifiers changed from: package-private */
    public String getTargetName(long dataHandle, int index) {
        return CoreJni.getTargetNameInCoreMorphingSystem(this.agpCptr, this, dataHandle, index);
    }

    /* access modifiers changed from: package-private */
    public float getTargetWeight(long dataHandle, int index) {
        return CoreJni.getTargetWeightInCoreMorphingSystem(this.agpCptr, this, dataHandle, index);
    }

    /* access modifiers changed from: package-private */
    public void setTargetNamesArray(long dataHandle, String[] stringArray, int len) {
        CoreJni.setTargetNamesArrayInCoreMorphingSystem(this.agpCptr, this, dataHandle, stringArray, len);
    }

    /* access modifiers changed from: package-private */
    public void setTargetWeightsArray(long dataHandle, float[] data, int len) {
        CoreJni.setTargetWeightsArrayInCoreMorphingSystem(this.agpCptr, this, dataHandle, data, len);
    }
}
