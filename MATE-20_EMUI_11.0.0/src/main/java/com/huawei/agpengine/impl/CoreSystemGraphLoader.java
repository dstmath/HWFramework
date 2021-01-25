package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreSystemGraphLoader {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSystemGraphLoader(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSystemGraphLoader obj) {
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
            if (this.isAgpCmemOwn) {
                this.isAgpCmemOwn = false;
                free(this);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public static class CoreLoadResult {
        private transient long agpCptr;
        transient boolean isAgpCmemOwn;

        CoreLoadResult(long cptr, boolean isCmemoryOwn) {
            this.isAgpCmemOwn = isCmemoryOwn;
            this.agpCptr = cptr;
        }

        static long getCptr(CoreLoadResult obj) {
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
                    CoreJni.deleteCoreSystemGraphLoaderCoreLoadResult(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }

        CoreLoadResult() {
            this(CoreJni.newCoreSystemGraphLoaderCoreLoadResult(), true);
        }

        /* access modifiers changed from: package-private */
        public boolean getSuccess() {
            return CoreJni.getVarsuccessCoreSystemGraphLoaderCoreLoadResult(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public String getError() {
            return CoreJni.getVarerrorCoreSystemGraphLoaderCoreLoadResult(this.agpCptr, this);
        }
    }

    /* access modifiers changed from: package-private */
    public CoreLoadResult load(String uri, CoreEcs ecs) {
        return new CoreLoadResult(CoreJni.loadInCoreSystemGraphLoader(this.agpCptr, this, uri, CoreEcs.getCptr(ecs), ecs), true);
    }

    /* access modifiers changed from: package-private */
    public CoreLoadResult loadString(String json, CoreEcs ecs) {
        return new CoreLoadResult(CoreJni.loadStringInCoreSystemGraphLoader(this.agpCptr, this, json, CoreEcs.getCptr(ecs), ecs), true);
    }

    static void free(CoreSystemGraphLoader v) {
        CoreJni.freeInCoreSystemGraphLoader(getCptr(v), v);
    }
}
