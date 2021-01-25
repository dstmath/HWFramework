package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreFileManager {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreFileManager(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreFileManager coreFileManager) {
        if (coreFileManager == null) {
            return 0;
        }
        return coreFileManager.agpCptr;
    }

    static long getCptrAndSetMemOwn(CoreFileManager coreFileManager, boolean z) {
        if (coreFileManager != null) {
            coreFileManager.isAgpCmemOwn = z;
        }
        return getCptr(coreFileManager);
    }
}
