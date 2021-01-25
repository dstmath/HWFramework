package ohos.agp.render.render3d.impl;

class CoreDevicePlatformData {
    private transient long agpCptrCoreDevicePlatformData;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreDevicePlatformData(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreDevicePlatformData = j;
    }

    static long getCptr(CoreDevicePlatformData coreDevicePlatformData) {
        if (coreDevicePlatformData == null) {
            return 0;
        }
        return coreDevicePlatformData.agpCptrCoreDevicePlatformData;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreDevicePlatformData != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreDevicePlatformData(this.agpCptrCoreDevicePlatformData);
                }
                this.agpCptrCoreDevicePlatformData = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreDevicePlatformData coreDevicePlatformData, boolean z) {
        if (coreDevicePlatformData != null) {
            synchronized (coreDevicePlatformData.lock) {
                coreDevicePlatformData.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreDevicePlatformData);
    }

    CoreDevicePlatformData() {
        this(CoreJni.newCoreDevicePlatformData(), true);
    }
}
