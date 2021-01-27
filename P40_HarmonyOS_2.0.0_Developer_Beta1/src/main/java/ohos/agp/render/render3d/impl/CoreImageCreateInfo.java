package ohos.agp.render.render3d.impl;

class CoreImageCreateInfo {
    private final transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreImageCreateInfo(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreImageCreateInfo coreImageCreateInfo) {
        if (coreImageCreateInfo == null) {
            return 0;
        }
        return coreImageCreateInfo.agpCptr;
    }

    static long getCptrAndSetMemOwn(CoreImageCreateInfo coreImageCreateInfo, boolean z) {
        if (coreImageCreateInfo != null) {
            coreImageCreateInfo.isAgpCmemOwn = z;
        }
        return getCptr(coreImageCreateInfo);
    }
}
