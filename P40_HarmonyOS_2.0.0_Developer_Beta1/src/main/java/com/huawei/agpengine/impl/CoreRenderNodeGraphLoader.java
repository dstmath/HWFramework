package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreRenderNodeGraphLoader {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeGraphLoader(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRenderNodeGraphLoader obj) {
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
                    CoreJni.deleteCoreRenderNodeGraphLoaderCoreLoadResult(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }

        CoreLoadResult() {
            this(CoreJni.newCoreRenderNodeGraphLoaderCoreLoadResult0(), true);
        }

        CoreLoadResult(String error) {
            this(CoreJni.newCoreRenderNodeGraphLoaderCoreLoadResult1(error), true);
        }

        /* access modifiers changed from: package-private */
        public boolean getSuccess() {
            return CoreJni.getVarsuccessCoreRenderNodeGraphLoaderCoreLoadResult(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public String getError() {
            return CoreJni.getVarerrorCoreRenderNodeGraphLoaderCoreLoadResult(this.agpCptr, this);
        }
    }

    /* access modifiers changed from: package-private */
    public CoreLoadResult load(String uri) {
        return new CoreLoadResult(CoreJni.loadInCoreRenderNodeGraphLoader(this.agpCptr, this, uri), true);
    }

    /* access modifiers changed from: package-private */
    public CoreLoadResult loadString(String json) {
        return new CoreLoadResult(CoreJni.loadStringInCoreRenderNodeGraphLoader(this.agpCptr, this, json), true);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderNodeGraphDesc getNodeGraphDescription() {
        return new CoreRenderNodeGraphDesc(CoreJni.getNodeGraphDescriptionInCoreRenderNodeGraphLoader(this.agpCptr, this), false);
    }
}
