package ohos.agp.render.render3d.impl;

class CoreBufferImageCopy {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreBufferImageCopy(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreBufferImageCopy coreBufferImageCopy) {
        if (coreBufferImageCopy == null) {
            return 0;
        }
        return coreBufferImageCopy.agpCptr;
    }

    static long getCptrAndSetMemOwn(CoreBufferImageCopy coreBufferImageCopy, boolean z) {
        if (coreBufferImageCopy != null) {
            coreBufferImageCopy.isAgpCmemOwn = z;
        }
        return getCptr(coreBufferImageCopy);
    }
}
