package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreRenderDataStorePod {
    private final transient long agpCptrRenderDataStorePod;
    transient boolean isAgpCmemOwn;

    CoreRenderDataStorePod(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrRenderDataStorePod = j;
    }

    static long getCptr(CoreRenderDataStorePod coreRenderDataStorePod) {
        if (coreRenderDataStorePod == null) {
            return 0;
        }
        return coreRenderDataStorePod.agpCptrRenderDataStorePod;
    }

    static long getCptrAndSetMemOwn(CoreRenderDataStorePod coreRenderDataStorePod, boolean z) {
        if (coreRenderDataStorePod != null) {
            coreRenderDataStorePod.isAgpCmemOwn = z;
        }
        return getCptr(coreRenderDataStorePod);
    }
}
