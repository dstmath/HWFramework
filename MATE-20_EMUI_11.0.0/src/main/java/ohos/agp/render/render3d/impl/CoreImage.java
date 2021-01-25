package ohos.agp.render.render3d.impl;

class CoreImage {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreImage(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreImage coreImage) {
        if (coreImage == null) {
            return 0;
        }
        return coreImage.agpCptr;
    }

    static long getCptrAndSetMemOwn(CoreImage coreImage, boolean z) {
        if (coreImage != null) {
            coreImage.isAgpCmemOwn = z;
        }
        return getCptr(coreImage);
    }
}
