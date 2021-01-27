package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreSystemGraphLoader {
    private transient long agpCptrSystemGraphLoader;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CoreSystemGraphLoader(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrSystemGraphLoader = j;
    }

    static long getCptr(CoreSystemGraphLoader coreSystemGraphLoader) {
        if (coreSystemGraphLoader == null) {
            return 0;
        }
        return coreSystemGraphLoader.agpCptrSystemGraphLoader;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrSystemGraphLoader != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrSystemGraphLoader = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreSystemGraphLoader coreSystemGraphLoader, boolean z) {
        if (coreSystemGraphLoader != null) {
            synchronized (coreSystemGraphLoader.delLock) {
                coreSystemGraphLoader.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreSystemGraphLoader);
    }

    /* access modifiers changed from: package-private */
    public static class CoreLoadResult {
        private transient long agpCptrSystemGraphLoaderResult;
        private final Object delLock;
        transient boolean isAgpCmemOwn;

        CoreLoadResult(long j, boolean z) {
            this.delLock = new Object();
            this.isAgpCmemOwn = z;
            this.agpCptrSystemGraphLoaderResult = j;
        }

        static long getCptr(CoreLoadResult coreLoadResult) {
            if (coreLoadResult == null) {
                return 0;
            }
            return coreLoadResult.agpCptrSystemGraphLoaderResult;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.delLock) {
                if (this.agpCptrSystemGraphLoaderResult != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreSystemGraphLoaderCoreLoadResult(this.agpCptrSystemGraphLoaderResult);
                    }
                    this.agpCptrSystemGraphLoaderResult = 0;
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
            this(CoreJni.newCoreSystemGraphLoaderCoreLoadResult0(), true);
        }

        CoreLoadResult(CoreString coreString) {
            this(CoreJni.newCoreSystemGraphLoaderCoreLoadResult1(CoreString.getCptr(coreString)), true);
        }

        /* access modifiers changed from: package-private */
        public boolean getSuccess() {
            return CoreJni.getVarsuccessCoreSystemGraphLoaderCoreLoadResult(this.agpCptrSystemGraphLoaderResult, this);
        }

        /* access modifiers changed from: package-private */
        public String getError() {
            return CoreJni.getVarerrorCoreSystemGraphLoaderCoreLoadResult(this.agpCptrSystemGraphLoaderResult, this);
        }
    }

    /* access modifiers changed from: package-private */
    public CoreLoadResult load(CoreFileManager coreFileManager, String str, CoreEcs coreEcs, CoreComponentManagerTypeInfoArray coreComponentManagerTypeInfoArray, CoreSystemTypeInfoArray coreSystemTypeInfoArray) {
        return new CoreLoadResult(CoreJni.loadInCoreSystemGraphLoader0(this.agpCptrSystemGraphLoader, this, CoreFileManager.getCptr(coreFileManager), coreFileManager, str, CoreEcs.getCptr(coreEcs), coreEcs, CoreComponentManagerTypeInfoArray.getCptr(coreComponentManagerTypeInfoArray), coreComponentManagerTypeInfoArray, CoreSystemTypeInfoArray.getCptr(coreSystemTypeInfoArray), coreSystemTypeInfoArray), true);
    }

    /* access modifiers changed from: package-private */
    public CoreLoadResult load(String str, CoreEcs coreEcs, CoreComponentManagerTypeInfoArray coreComponentManagerTypeInfoArray, CoreSystemTypeInfoArray coreSystemTypeInfoArray) {
        return new CoreLoadResult(CoreJni.loadInCoreSystemGraphLoader1(this.agpCptrSystemGraphLoader, this, str, CoreEcs.getCptr(coreEcs), coreEcs, CoreComponentManagerTypeInfoArray.getCptr(coreComponentManagerTypeInfoArray), coreComponentManagerTypeInfoArray, CoreSystemTypeInfoArray.getCptr(coreSystemTypeInfoArray), coreSystemTypeInfoArray), true);
    }

    static class CoreDeleter {
        private transient long agpCptrSystemGraphLoaderDeleter;
        private final Object delLock;
        transient boolean isAgpCmemOwn;

        CoreDeleter(long j, boolean z) {
            this.delLock = new Object();
            this.isAgpCmemOwn = z;
            this.agpCptrSystemGraphLoaderDeleter = j;
        }

        static long getCptr(CoreDeleter coreDeleter) {
            if (coreDeleter == null) {
                return 0;
            }
            return coreDeleter.agpCptrSystemGraphLoaderDeleter;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.delLock) {
                if (this.agpCptrSystemGraphLoaderDeleter != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreSystemGraphLoaderCoreDeleter(this.agpCptrSystemGraphLoaderDeleter);
                    }
                    this.agpCptrSystemGraphLoaderDeleter = 0;
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
            this(CoreJni.newCoreSystemGraphLoaderCoreDeleter(), true);
        }
    }
}
