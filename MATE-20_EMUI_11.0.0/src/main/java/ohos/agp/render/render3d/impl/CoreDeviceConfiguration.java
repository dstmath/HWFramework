package ohos.agp.render.render3d.impl;

class CoreDeviceConfiguration {
    private transient long agpCptrCoreDeviceCfg;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreDeviceConfiguration(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreDeviceCfg = j;
    }

    static long getCptr(CoreDeviceConfiguration coreDeviceConfiguration) {
        if (coreDeviceConfiguration == null) {
            return 0;
        }
        return coreDeviceConfiguration.agpCptrCoreDeviceCfg;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreDeviceCfg != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreDeviceConfiguration(this.agpCptrCoreDeviceCfg);
                }
                this.agpCptrCoreDeviceCfg = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreDeviceConfiguration coreDeviceConfiguration, boolean z) {
        if (coreDeviceConfiguration != null) {
            synchronized (coreDeviceConfiguration.lock) {
                coreDeviceConfiguration.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreDeviceConfiguration);
    }

    /* access modifiers changed from: package-private */
    public void setBufferingCount(long j) {
        CoreJni.setVarbufferingCountCoreDeviceConfiguration(this.agpCptrCoreDeviceCfg, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getBufferingCount() {
        return CoreJni.getVarbufferingCountCoreDeviceConfiguration(this.agpCptrCoreDeviceCfg, this);
    }

    /* access modifiers changed from: package-private */
    public void setRequiredIntegratedMemoryFlags(long j) {
        CoreJni.setVarrequiredIntegratedMemoryFlagsCoreDeviceConfiguration(this.agpCptrCoreDeviceCfg, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getRequiredIntegratedMemoryFlags() {
        return CoreJni.getVarrequiredIntegratedMemoryFlagsCoreDeviceConfiguration(this.agpCptrCoreDeviceCfg, this);
    }

    CoreDeviceConfiguration() {
        this(CoreJni.newCoreDeviceConfiguration(), true);
    }
}
