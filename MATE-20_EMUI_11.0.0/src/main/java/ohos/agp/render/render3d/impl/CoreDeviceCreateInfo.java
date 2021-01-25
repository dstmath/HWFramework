package ohos.agp.render.render3d.impl;

class CoreDeviceCreateInfo {
    private transient long agpCptrCoreDeviceCreateInfo;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreDeviceCreateInfo(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreDeviceCreateInfo = j;
    }

    static long getCptr(CoreDeviceCreateInfo coreDeviceCreateInfo) {
        if (coreDeviceCreateInfo == null) {
            return 0;
        }
        return coreDeviceCreateInfo.agpCptrCoreDeviceCreateInfo;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreDeviceCreateInfo != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreDeviceCreateInfo(this.agpCptrCoreDeviceCreateInfo);
                }
                this.agpCptrCoreDeviceCreateInfo = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreDeviceCreateInfo coreDeviceCreateInfo, boolean z) {
        if (coreDeviceCreateInfo != null) {
            synchronized (coreDeviceCreateInfo.lock) {
                coreDeviceCreateInfo.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreDeviceCreateInfo);
    }

    /* access modifiers changed from: package-private */
    public void setDeviceConfiguration(CoreDeviceConfiguration coreDeviceConfiguration) {
        CoreJni.setVardeviceConfigurationCoreDeviceCreateInfo(this.agpCptrCoreDeviceCreateInfo, this, CoreDeviceConfiguration.getCptr(coreDeviceConfiguration), coreDeviceConfiguration);
    }

    /* access modifiers changed from: package-private */
    public CoreDeviceConfiguration getDeviceConfiguration() {
        long vardeviceConfigurationCoreDeviceCreateInfo = CoreJni.getVardeviceConfigurationCoreDeviceCreateInfo(this.agpCptrCoreDeviceCreateInfo, this);
        if (vardeviceConfigurationCoreDeviceCreateInfo == 0) {
            return null;
        }
        return new CoreDeviceConfiguration(vardeviceConfigurationCoreDeviceCreateInfo, false);
    }

    /* access modifiers changed from: package-private */
    public void setBackendConfiguration(CoreBackendExtra coreBackendExtra) {
        CoreJni.setVarbackendConfigurationCoreDeviceCreateInfo(this.agpCptrCoreDeviceCreateInfo, this, CoreBackendExtra.getCptr(coreBackendExtra), coreBackendExtra);
    }

    /* access modifiers changed from: package-private */
    public CoreBackendExtra getBackendConfiguration() {
        long varbackendConfigurationCoreDeviceCreateInfo = CoreJni.getVarbackendConfigurationCoreDeviceCreateInfo(this.agpCptrCoreDeviceCreateInfo, this);
        if (varbackendConfigurationCoreDeviceCreateInfo == 0) {
            return null;
        }
        return new CoreBackendExtra(varbackendConfigurationCoreDeviceCreateInfo, false);
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

        static CoreBackend swigToEnum(int i) {
            CoreBackend[] coreBackendArr = (CoreBackend[]) CoreBackend.class.getEnumConstants();
            if (i < coreBackendArr.length && i >= 0 && coreBackendArr[i].swigValue == i) {
                return coreBackendArr[i];
            }
            for (CoreBackend coreBackend : coreBackendArr) {
                if (coreBackend.swigValue == i) {
                    return coreBackend;
                }
            }
            throw new IllegalArgumentException("No enum " + CoreBackend.class + " with value " + i);
        }

        private CoreBackend() {
            this(SwigNext.next);
        }

        private CoreBackend(int i) {
            this.swigValue = i;
            int unused = SwigNext.next = i + 1;
        }

        private CoreBackend(CoreBackend coreBackend) {
            this(coreBackend.swigValue);
        }

        private static class SwigNext {
            private static int next;

            private SwigNext() {
            }
        }
    }
}
