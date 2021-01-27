package ohos.agp.render.render3d.impl;

class CoreRenderDataStoreManager {
    private final transient long agpCptrRenderDataStoreMgr;
    transient boolean isAgpCmemOwn;

    CoreRenderDataStoreManager(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrRenderDataStoreMgr = j;
    }

    static long getCptr(CoreRenderDataStoreManager coreRenderDataStoreManager) {
        if (coreRenderDataStoreManager == null) {
            return 0;
        }
        return coreRenderDataStoreManager.agpCptrRenderDataStoreMgr;
    }

    static long getCptrAndSetMemOwn(CoreRenderDataStoreManager coreRenderDataStoreManager, boolean z) {
        if (coreRenderDataStoreManager != null) {
            coreRenderDataStoreManager.isAgpCmemOwn = z;
        }
        return getCptr(coreRenderDataStoreManager);
    }
}
