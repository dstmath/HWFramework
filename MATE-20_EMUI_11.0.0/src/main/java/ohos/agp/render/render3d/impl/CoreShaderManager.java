package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreShaderManager {
    private transient long agpCptrCoreShaderManager;
    transient boolean isAgpCmemOwn;

    CoreShaderManager(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreShaderManager = j;
    }

    static long getCptr(CoreShaderManager coreShaderManager) {
        if (coreShaderManager == null) {
            return 0;
        }
        return coreShaderManager.agpCptrCoreShaderManager;
    }

    static long getCptrAndSetMemOwn(CoreShaderManager coreShaderManager, boolean z) {
        if (coreShaderManager != null) {
            coreShaderManager.isAgpCmemOwn = z;
        }
        return getCptr(coreShaderManager);
    }
}
