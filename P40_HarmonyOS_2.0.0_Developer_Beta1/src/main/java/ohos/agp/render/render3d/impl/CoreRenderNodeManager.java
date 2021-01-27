package ohos.agp.render.render3d.impl;

class CoreRenderNodeManager {
    private final transient long agpCptrRenderNodeManager;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeManager(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrRenderNodeManager = j;
    }

    static long getCptr(CoreRenderNodeManager coreRenderNodeManager) {
        if (coreRenderNodeManager == null) {
            return 0;
        }
        return coreRenderNodeManager.agpCptrRenderNodeManager;
    }

    static long getCptrAndSetMemOwn(CoreRenderNodeManager coreRenderNodeManager, boolean z) {
        if (coreRenderNodeManager != null) {
            coreRenderNodeManager.isAgpCmemOwn = z;
        }
        return getCptr(coreRenderNodeManager);
    }
}
