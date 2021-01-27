package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreVersionInfo {
    private transient long agpCptrCoreVersionInfo;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreVersionInfo(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreVersionInfo = j;
    }

    static long getCptr(CoreVersionInfo coreVersionInfo) {
        if (coreVersionInfo == null) {
            return 0;
        }
        return coreVersionInfo.agpCptrCoreVersionInfo;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreVersionInfo != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreVersionInfo(this.agpCptrCoreVersionInfo);
                }
                this.agpCptrCoreVersionInfo = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreVersionInfo coreVersionInfo, boolean z) {
        if (coreVersionInfo != null) {
            synchronized (coreVersionInfo.delLock) {
                coreVersionInfo.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreVersionInfo);
    }

    CoreVersionInfo(String str, int i, int i2, int i3) {
        this(CoreJni.newCoreVersionInfo(str, i, i2, i3), true);
    }
}
