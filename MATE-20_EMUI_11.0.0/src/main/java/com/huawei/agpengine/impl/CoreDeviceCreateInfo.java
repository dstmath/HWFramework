package com.huawei.agpengine.impl;

class CoreDeviceCreateInfo {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreDeviceCreateInfo(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreDeviceCreateInfo obj) {
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
                CoreJni.deleteCoreDeviceCreateInfo(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setDeviceConfiguration(CoreDeviceConfiguration value) {
        CoreJni.setVardeviceConfigurationCoreDeviceCreateInfo(this.agpCptr, this, CoreDeviceConfiguration.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreDeviceConfiguration getDeviceConfiguration() {
        long cptr = CoreJni.getVardeviceConfigurationCoreDeviceCreateInfo(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreDeviceConfiguration(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setBackendConfiguration(CoreBackendExtra value) {
        CoreJni.setVarbackendConfigurationCoreDeviceCreateInfo(this.agpCptr, this, CoreBackendExtra.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreBackendExtra getBackendConfiguration() {
        long cptr = CoreJni.getVarbackendConfigurationCoreDeviceCreateInfo(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreBackendExtra(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public enum CoreBackend {
        VULKAN,
        OPENGLES,
        OPENGL;
        
        private final int swigValue;

        /* access modifiers changed from: package-private */
        public final int swigValue() {
            return this.swigValue;
        }

        static CoreBackend swigToEnum(int swigValue2) {
            CoreBackend[] swigValues = (CoreBackend[]) CoreBackend.class.getEnumConstants();
            if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
                return swigValues[swigValue2];
            }
            for (CoreBackend swigEnum : swigValues) {
                if (swigEnum.swigValue == swigValue2) {
                    return swigEnum;
                }
            }
            throw new IllegalArgumentException("No enum " + CoreBackend.class + " with value " + swigValue2);
        }

        private CoreBackend() {
            this.swigValue = SwigNext.next;
            SwigNext.access$008();
        }

        private CoreBackend(int swigValue2) {
            this.swigValue = swigValue2;
            int unused = SwigNext.next = swigValue2 + 1;
        }

        private CoreBackend(CoreBackend swigEnum) {
            this.swigValue = swigEnum.swigValue;
            int unused = SwigNext.next = this.swigValue + 1;
        }

        private static class SwigNext {
            private static int next = 0;

            private SwigNext() {
            }

            static /* synthetic */ int access$008() {
                int i = next;
                next = i + 1;
                return i;
            }
        }
    }
}
