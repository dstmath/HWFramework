package ohos.agp.render.render3d.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreSystem {
    private transient long agpCptrCoreSystem;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CoreSystem(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreSystem = j;
    }

    static long getCptr(CoreSystem coreSystem) {
        if (coreSystem == null) {
            return 0;
        }
        return coreSystem.agpCptrCoreSystem;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreSystem != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreSystem(this.agpCptrCoreSystem);
                }
                this.agpCptrCoreSystem = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreSystem coreSystem, boolean z) {
        if (coreSystem != null) {
            coreSystem.isAgpCmemOwn = z;
        }
        return getCptr(coreSystem);
    }

    /* access modifiers changed from: package-private */
    public String name() {
        return CoreJni.nameInCoreSystem(this.agpCptrCoreSystem, this);
    }

    /* access modifiers changed from: package-private */
    public void setProps(CorePropertyHandle corePropertyHandle) {
        CoreJni.setPropsInCoreSystem(this.agpCptrCoreSystem, this, CorePropertyHandle.getCptr(corePropertyHandle), corePropertyHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean isActive() {
        return CoreJni.isActiveInCoreSystem(this.agpCptrCoreSystem, this);
    }

    /* access modifiers changed from: package-private */
    public void setActive(boolean z) {
        CoreJni.setActiveInCoreSystem(this.agpCptrCoreSystem, this, z);
    }

    /* access modifiers changed from: package-private */
    public void initialize() {
        CoreJni.initializeInCoreSystem(this.agpCptrCoreSystem, this);
    }

    /* access modifiers changed from: package-private */
    public boolean update(boolean z, BigInteger bigInteger, BigInteger bigInteger2) {
        return CoreJni.updateInCoreSystem(this.agpCptrCoreSystem, this, z, bigInteger, bigInteger2);
    }

    /* access modifiers changed from: package-private */
    public void uninitialize() {
        CoreJni.uninitializeInCoreSystem(this.agpCptrCoreSystem, this);
    }

    /* access modifiers changed from: package-private */
    public CoreEcs getEcs() {
        return new CoreEcs(CoreJni.getEcsInCoreSystem(this.agpCptrCoreSystem, this), false);
    }
}
