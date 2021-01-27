package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreEngineCreateInfo {
    private transient long agpCptrCoreEngineCreateInfo;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreEngineCreateInfo(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreEngineCreateInfo = j;
    }

    static long getCptr(CoreEngineCreateInfo coreEngineCreateInfo) {
        if (coreEngineCreateInfo == null) {
            return 0;
        }
        return coreEngineCreateInfo.agpCptrCoreEngineCreateInfo;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreEngineCreateInfo != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreEngineCreateInfo(this.agpCptrCoreEngineCreateInfo);
                }
                this.agpCptrCoreEngineCreateInfo = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreEngineCreateInfo coreEngineCreateInfo, boolean z) {
        if (coreEngineCreateInfo != null) {
            synchronized (coreEngineCreateInfo.lock) {
                coreEngineCreateInfo.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreEngineCreateInfo);
    }

    CoreEngineCreateInfo(CorePlatform corePlatform, CoreVersionInfo coreVersionInfo, CoreContextInfo coreContextInfo) {
        this(CoreJni.newCoreEngineCreateInfo(CorePlatform.getCptr(corePlatform), corePlatform, CoreVersionInfo.getCptr(coreVersionInfo), coreVersionInfo, CoreContextInfo.getCptr(coreContextInfo), coreContextInfo), true);
    }
}
