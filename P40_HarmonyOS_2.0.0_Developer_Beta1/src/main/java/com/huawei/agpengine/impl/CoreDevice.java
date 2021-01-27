package com.huawei.agpengine.impl;

import com.huawei.agpengine.impl.CoreDeviceCreateInfo;

/* access modifiers changed from: package-private */
public class CoreDevice {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreDevice(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreDevice obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long getInstance() {
        return CoreJni.getInstanceInCoreDevice(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreDeviceCreateInfo.CoreBackend getBackendType() {
        return CoreDeviceCreateInfo.CoreBackend.swigToEnum(CoreJni.getBackendTypeInCoreDevice(this.agpCptr, this));
    }

    /* access modifiers changed from: package-private */
    public CoreDevicePlatformData getPlatformData() {
        return new CoreDevicePlatformData(CoreJni.getPlatformDataInCoreDevice(this.agpCptr, this), false);
    }
}
