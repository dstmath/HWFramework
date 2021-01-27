package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreGltf2Importer {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGltf2Importer(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGltf2Importer obj) {
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

    static class CoreListener {
        private transient long agpCptr;
        transient boolean isAgpCmemOwn;

        CoreListener(long cptr, boolean isCmemoryOwn) {
            this.isAgpCmemOwn = isCmemoryOwn;
            this.agpCptr = cptr;
        }

        static long getCptr(CoreListener obj) {
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
                    CoreJni.deleteCoreGltf2ImporterCoreListener(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void onImportStarted() {
            CoreJni.onImportStartedInCoreGltf2ImporterCoreListener(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void onImportFinished() {
            CoreJni.onImportFinishedInCoreGltf2ImporterCoreListener(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void onImportProgressed(long taskIndex, long taskCount) {
            CoreJni.onImportProgressedInCoreGltf2ImporterCoreListener(this.agpCptr, this, taskIndex, taskCount);
        }
    }

    /* access modifiers changed from: package-private */
    public void importGltf(CoreGltfData data, long flags) {
        CoreJni.importGltfInCoreGltf2Importer(this.agpCptr, this, CoreGltfData.getCptr(data), data, flags);
    }

    /* access modifiers changed from: package-private */
    public void importGltfAsync(CoreGltfData data, long flags, CoreListener listener) {
        CoreJni.importGltfAsyncInCoreGltf2Importer(this.agpCptr, this, CoreGltfData.getCptr(data), data, flags, CoreListener.getCptr(listener), listener);
    }

    /* access modifiers changed from: package-private */
    public boolean execute(long timeBudget) {
        return CoreJni.executeInCoreGltf2Importer(this.agpCptr, this, timeBudget);
    }

    /* access modifiers changed from: package-private */
    public void cancel() {
        CoreJni.cancelInCoreGltf2Importer(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public boolean isCompleted() {
        return CoreJni.isCompletedInCoreGltf2Importer(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreGltfImportResult getResult() {
        return new CoreGltfImportResult(CoreJni.getResultInCoreGltf2Importer(this.agpCptr, this), false);
    }
}
