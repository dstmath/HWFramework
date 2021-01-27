package ohos.agp.render.render3d.impl;

class CoreSkinCreateInfo {
    private final transient long agpCptrCoreSkinCreateInfo;
    transient boolean isAgpCmemOwn;

    CoreSkinCreateInfo(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreSkinCreateInfo = j;
    }

    static long getCptr(CoreSkinCreateInfo coreSkinCreateInfo) {
        if (coreSkinCreateInfo == null) {
            return 0;
        }
        return coreSkinCreateInfo.agpCptrCoreSkinCreateInfo;
    }

    static long getCptrAndSetMemOwn(CoreSkinCreateInfo coreSkinCreateInfo, boolean z) {
        if (coreSkinCreateInfo != null) {
            coreSkinCreateInfo.isAgpCmemOwn = z;
        }
        return getCptr(coreSkinCreateInfo);
    }
}
