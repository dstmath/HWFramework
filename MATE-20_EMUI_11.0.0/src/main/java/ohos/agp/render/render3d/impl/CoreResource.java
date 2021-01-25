package ohos.agp.render.render3d.impl;

import java.math.BigInteger;

class CoreResource {
    private transient long agpCptrCoreResource;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CoreResource(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreResource = j;
    }

    static long getCptr(CoreResource coreResource) {
        if (coreResource == null) {
            return 0;
        }
        return coreResource.agpCptrCoreResource;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreResource != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrCoreResource = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreResource coreResource, boolean z) {
        if (coreResource != null) {
            coreResource.isAgpCmemOwn = z;
        }
        return getCptr(coreResource);
    }

    /* access modifiers changed from: package-private */
    public CorePropertyHandle getProperties() {
        long propertiesInCoreResource = CoreJni.getPropertiesInCoreResource(this.agpCptrCoreResource, this);
        if (propertiesInCoreResource == 0) {
            return null;
        }
        return new CorePropertyHandle(propertiesInCoreResource, false);
    }

    /* access modifiers changed from: package-private */
    public void setProperties(CorePropertyHandle corePropertyHandle) {
        CoreJni.setPropertiesInCoreResource(this.agpCptrCoreResource, this, CorePropertyHandle.getCptr(corePropertyHandle), corePropertyHandle);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getType() {
        return CoreJni.getTypeInCoreResource(this.agpCptrCoreResource, this);
    }

    static void destroy(CoreResource coreResource) {
        CoreJni.destroyInCoreResource(getCptr(coreResource), coreResource);
    }
}
