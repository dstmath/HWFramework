package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreGltf2Importer {
    private transient long agpCptrCoreGltf2Importer;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreGltf2Importer(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreGltf2Importer = j;
    }

    static long getCptr(CoreGltf2Importer coreGltf2Importer) {
        if (coreGltf2Importer == null) {
            return 0;
        }
        return coreGltf2Importer.agpCptrCoreGltf2Importer;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreGltf2Importer != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrCoreGltf2Importer = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreGltf2Importer coreGltf2Importer, boolean z) {
        if (coreGltf2Importer != null) {
            synchronized (coreGltf2Importer.lock) {
                coreGltf2Importer.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreGltf2Importer);
    }

    static class CoreListener {
        private transient long agpCptrGltf2ImporterCoreListener;
        transient boolean isAgpCmemOwn;
        private final Object lock2 = new Object();

        CoreListener(long j, boolean z) {
            this.isAgpCmemOwn = z;
            this.agpCptrGltf2ImporterCoreListener = j;
        }

        static long getCptr(CoreListener coreListener) {
            if (coreListener == null) {
                return 0;
            }
            return coreListener.agpCptrGltf2ImporterCoreListener;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.lock2) {
                if (this.agpCptrGltf2ImporterCoreListener != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreGltf2ImporterCoreListener(this.agpCptrGltf2ImporterCoreListener);
                    }
                    this.agpCptrGltf2ImporterCoreListener = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CoreListener coreListener, boolean z) {
            if (coreListener != null) {
                synchronized (coreListener.lock2) {
                    coreListener.isAgpCmemOwn = z;
                }
            }
            return getCptr(coreListener);
        }

        /* access modifiers changed from: package-private */
        public void onImportStarted() {
            CoreJni.onImportStartedInCoreGltf2ImporterCoreListener(this.agpCptrGltf2ImporterCoreListener, this);
        }

        /* access modifiers changed from: package-private */
        public void onImportFinished() {
            CoreJni.onImportFinishedInCoreGltf2ImporterCoreListener(this.agpCptrGltf2ImporterCoreListener, this);
        }

        /* access modifiers changed from: package-private */
        public void onImportProgressed(long j, long j2) {
            CoreJni.onImportProgressedInCoreGltf2ImporterCoreListener(this.agpCptrGltf2ImporterCoreListener, this, j, j2);
        }
    }

    /* access modifiers changed from: package-private */
    public void importGltf(CoreGltfData coreGltfData, long j) {
        CoreJni.importGltfInCoreGltf2Importer(this.agpCptrCoreGltf2Importer, this, CoreGltfData.getCptr(coreGltfData), coreGltfData, j);
    }

    /* access modifiers changed from: package-private */
    public void importGltfAsync(CoreGltfData coreGltfData, long j, CoreListener coreListener) {
        CoreJni.importGltfAsyncInCoreGltf2Importer(this.agpCptrCoreGltf2Importer, this, CoreGltfData.getCptr(coreGltfData), coreGltfData, j, CoreListener.getCptr(coreListener), coreListener);
    }

    /* access modifiers changed from: package-private */
    public boolean execute(long j) {
        return CoreJni.executeInCoreGltf2Importer(this.agpCptrCoreGltf2Importer, this, j);
    }

    /* access modifiers changed from: package-private */
    public void cancel() {
        CoreJni.cancelInCoreGltf2Importer(this.agpCptrCoreGltf2Importer, this);
    }

    /* access modifiers changed from: package-private */
    public boolean isCompleted() {
        return CoreJni.isCompletedInCoreGltf2Importer(this.agpCptrCoreGltf2Importer, this);
    }

    /* access modifiers changed from: package-private */
    public CoreGltfImportResult getResult() {
        return new CoreGltfImportResult(CoreJni.getResultInCoreGltf2Importer(this.agpCptrCoreGltf2Importer, this), false);
    }

    static class CoreDeleter {
        private transient long agpCptrGltf2ImporterCoreDeleter;
        transient boolean isAgpCmemOwn;
        private final Object lock2;

        CoreDeleter(long j, boolean z) {
            this.lock2 = new Object();
            this.isAgpCmemOwn = z;
            this.agpCptrGltf2ImporterCoreDeleter = j;
        }

        static long getCptr(CoreDeleter coreDeleter) {
            if (coreDeleter == null) {
                return 0;
            }
            return coreDeleter.agpCptrGltf2ImporterCoreDeleter;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.lock2) {
                if (this.agpCptrGltf2ImporterCoreDeleter != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreGltf2ImporterCoreDeleter(this.agpCptrGltf2ImporterCoreDeleter);
                    }
                    this.agpCptrGltf2ImporterCoreDeleter = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CoreDeleter coreDeleter, boolean z) {
            if (coreDeleter != null) {
                synchronized (coreDeleter.lock2) {
                    coreDeleter.isAgpCmemOwn = z;
                }
            }
            return getCptr(coreDeleter);
        }

        CoreDeleter() {
            this(CoreJni.newCoreGltf2ImporterCoreDeleter(), true);
        }
    }
}
