package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreVersionInfo {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreVersionInfo(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreVersionInfo obj) {
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
                CoreJni.deleteCoreVersionInfo(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreVersionInfo(String name, int versionMajor, int versionMinor, int versionPatch) {
        this(CoreJni.newCoreVersionInfo(name, versionMajor, versionMinor, versionPatch), true);
    }
}
