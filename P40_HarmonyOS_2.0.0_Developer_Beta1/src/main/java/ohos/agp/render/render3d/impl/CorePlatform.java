package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CorePlatform {
    private transient long agpCptrCorePlatform;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CorePlatform(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCorePlatform = j;
    }

    static long getCptr(CorePlatform corePlatform) {
        if (corePlatform == null) {
            return 0;
        }
        return corePlatform.agpCptrCorePlatform;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCorePlatform != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCorePlatform(this.agpCptrCorePlatform);
                }
                this.agpCptrCorePlatform = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CorePlatform corePlatform, boolean z) {
        if (corePlatform != null) {
            synchronized (corePlatform.delLock) {
                corePlatform.isAgpCmemOwn = z;
            }
        }
        return getCptr(corePlatform);
    }
}
