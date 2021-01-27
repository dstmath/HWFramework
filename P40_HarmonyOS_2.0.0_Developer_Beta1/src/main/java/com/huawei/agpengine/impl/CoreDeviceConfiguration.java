package com.huawei.agpengine.impl;

class CoreDeviceConfiguration {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreDeviceConfiguration(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreDeviceConfiguration obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (this.isAgpCmemOwn) {
                this.isAgpCmemOwn = false;
                CoreJni.deleteCoreDeviceConfiguration(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setBufferingCount(long value) {
        CoreJni.setVarbufferingCountCoreDeviceConfiguration(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getBufferingCount() {
        return CoreJni.getVarbufferingCountCoreDeviceConfiguration(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setRequiredIntegratedMemoryFlags(long value) {
        CoreJni.setVarrequiredIntegratedMemoryFlagsCoreDeviceConfiguration(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getRequiredIntegratedMemoryFlags() {
        return CoreJni.getVarrequiredIntegratedMemoryFlagsCoreDeviceConfiguration(this.agpCptr, this);
    }

    CoreDeviceConfiguration() {
        this(CoreJni.newCoreDeviceConfiguration(), true);
    }
}
