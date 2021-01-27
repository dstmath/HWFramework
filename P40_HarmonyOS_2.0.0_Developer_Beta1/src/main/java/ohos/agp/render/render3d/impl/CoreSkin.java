package ohos.agp.render.render3d.impl;

class CoreSkin {
    private final transient long agpCptrCoreSkin;
    transient boolean isAgpCmemOwn;

    CoreSkin(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreSkin = j;
    }

    static long getCptr(CoreSkin coreSkin) {
        if (coreSkin == null) {
            return 0;
        }
        return coreSkin.agpCptrCoreSkin;
    }

    static long getCptrAndSetMemOwn(CoreSkin coreSkin, boolean z) {
        if (coreSkin != null) {
            coreSkin.isAgpCmemOwn = z;
        }
        return getCptr(coreSkin);
    }
}
