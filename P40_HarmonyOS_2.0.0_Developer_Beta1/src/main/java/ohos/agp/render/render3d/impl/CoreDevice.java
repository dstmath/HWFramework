package ohos.agp.render.render3d.impl;

import ohos.agp.render.render3d.impl.CoreDeviceCreateInfo;

/* access modifiers changed from: package-private */
public class CoreDevice {
    private transient long agpCptrCoreDevice;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreDevice(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreDevice = j;
    }

    static long getCptr(CoreDevice coreDevice) {
        if (coreDevice == null) {
            return 0;
        }
        return coreDevice.agpCptrCoreDevice;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreDevice != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrCoreDevice = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreDevice coreDevice, boolean z) {
        if (coreDevice != null) {
            synchronized (coreDevice.lock) {
                coreDevice.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreDevice);
    }

    /* access modifiers changed from: package-private */
    public long getInstance() {
        return CoreJni.getInstanceInCoreDevice(this.agpCptrCoreDevice, this);
    }

    /* access modifiers changed from: package-private */
    public CoreDeviceCreateInfo.CoreBackend getBackendType() {
        return CoreDeviceCreateInfo.CoreBackend.swigToEnum(CoreJni.getBackendTypeInCoreDevice(this.agpCptrCoreDevice, this));
    }

    /* access modifiers changed from: package-private */
    public CoreDevicePlatformData getPlatformData() {
        return new CoreDevicePlatformData(CoreJni.getPlatformDataInCoreDevice(this.agpCptrCoreDevice, this), false);
    }
}
