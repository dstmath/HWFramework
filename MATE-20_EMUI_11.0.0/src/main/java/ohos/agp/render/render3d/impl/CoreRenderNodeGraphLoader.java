package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreRenderNodeGraphLoader {
    private transient long agpCptrRenderNodeGraphLoader;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CoreRenderNodeGraphLoader(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrRenderNodeGraphLoader = j;
    }

    static long getCptr(CoreRenderNodeGraphLoader coreRenderNodeGraphLoader) {
        if (coreRenderNodeGraphLoader == null) {
            return 0;
        }
        return coreRenderNodeGraphLoader.agpCptrRenderNodeGraphLoader;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrRenderNodeGraphLoader != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrRenderNodeGraphLoader = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderNodeGraphLoader coreRenderNodeGraphLoader, boolean z) {
        if (coreRenderNodeGraphLoader != null) {
            synchronized (coreRenderNodeGraphLoader.delLock) {
                coreRenderNodeGraphLoader.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRenderNodeGraphLoader);
    }

    /* access modifiers changed from: package-private */
    public static class CoreLoadResult {
        private transient long agpCptrRenderNodeGraphLoaderResult;
        private final Object delLock;
        transient boolean isAgpCmemOwn;

        CoreLoadResult(long j, boolean z) {
            this.delLock = new Object();
            this.isAgpCmemOwn = z;
            this.agpCptrRenderNodeGraphLoaderResult = j;
        }

        static long getCptr(CoreLoadResult coreLoadResult) {
            if (coreLoadResult == null) {
                return 0;
            }
            return coreLoadResult.agpCptrRenderNodeGraphLoaderResult;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.delLock) {
                if (this.agpCptrRenderNodeGraphLoaderResult != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreRenderNodeGraphLoaderCoreLoadResult(this.agpCptrRenderNodeGraphLoaderResult);
                    }
                    this.agpCptrRenderNodeGraphLoaderResult = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CoreLoadResult coreLoadResult, boolean z) {
            if (coreLoadResult != null) {
                synchronized (coreLoadResult.delLock) {
                    coreLoadResult.isAgpCmemOwn = z;
                }
            }
            return getCptr(coreLoadResult);
        }

        CoreLoadResult() {
            this(CoreJni.newCoreRenderNodeGraphLoaderCoreLoadResult0(), true);
        }

        CoreLoadResult(String str) {
            this(CoreJni.newCoreRenderNodeGraphLoaderCoreLoadResult1(str), true);
        }

        /* access modifiers changed from: package-private */
        public boolean getSuccess() {
            return CoreJni.getVarsuccessCoreRenderNodeGraphLoaderCoreLoadResult(this.agpCptrRenderNodeGraphLoaderResult, this);
        }

        /* access modifiers changed from: package-private */
        public String getError() {
            return CoreJni.getVarerrorCoreRenderNodeGraphLoaderCoreLoadResult(this.agpCptrRenderNodeGraphLoaderResult, this);
        }
    }

    /* access modifiers changed from: package-private */
    public CoreLoadResult load(CoreFileManager coreFileManager, String str) {
        return new CoreLoadResult(CoreJni.loadInCoreRenderNodeGraphLoader0(this.agpCptrRenderNodeGraphLoader, this, CoreFileManager.getCptr(coreFileManager), coreFileManager, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreLoadResult load(String str) {
        return new CoreLoadResult(CoreJni.loadInCoreRenderNodeGraphLoader1(this.agpCptrRenderNodeGraphLoader, this, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderNodeGraphDesc getNodeGraphDescription() {
        return new CoreRenderNodeGraphDesc(CoreJni.getNodeGraphDescriptionInCoreRenderNodeGraphLoader(this.agpCptrRenderNodeGraphLoader, this), false);
    }

    static class CoreDeleter {
        private transient long agpCptrRenderNodeGraphLoaderDeleter;
        private final Object delLock;
        transient boolean isAgpCmemOwn;

        CoreDeleter(long j, boolean z) {
            this.delLock = new Object();
            this.isAgpCmemOwn = z;
            this.agpCptrRenderNodeGraphLoaderDeleter = j;
        }

        static long getCptr(CoreDeleter coreDeleter) {
            if (coreDeleter == null) {
                return 0;
            }
            return coreDeleter.agpCptrRenderNodeGraphLoaderDeleter;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.delLock) {
                if (this.agpCptrRenderNodeGraphLoaderDeleter != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreRenderNodeGraphLoaderCoreDeleter(this.agpCptrRenderNodeGraphLoaderDeleter);
                    }
                    this.agpCptrRenderNodeGraphLoaderDeleter = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CoreDeleter coreDeleter, boolean z) {
            if (coreDeleter != null) {
                synchronized (coreDeleter.delLock) {
                    coreDeleter.isAgpCmemOwn = z;
                }
            }
            return getCptr(coreDeleter);
        }

        CoreDeleter() {
            this(CoreJni.newCoreRenderNodeGraphLoaderCoreDeleter(), true);
        }
    }
}
