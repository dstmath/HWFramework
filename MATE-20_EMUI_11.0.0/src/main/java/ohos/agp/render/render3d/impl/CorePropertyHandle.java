package ohos.agp.render.render3d.impl;

class CorePropertyHandle {
    private transient long agpCptrCorePropertyHandle;
    transient boolean isAgpCmemOwn;

    CorePropertyHandle(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCorePropertyHandle = j;
    }

    static long getCptr(CorePropertyHandle corePropertyHandle) {
        if (corePropertyHandle == null) {
            return 0;
        }
        return corePropertyHandle.agpCptrCorePropertyHandle;
    }

    static long getCptrAndSetMemOwn(CorePropertyHandle corePropertyHandle, boolean z) {
        if (corePropertyHandle != null) {
            corePropertyHandle.isAgpCmemOwn = z;
        }
        return getCptr(corePropertyHandle);
    }
}
