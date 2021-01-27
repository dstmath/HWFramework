package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CorePluginRegister {
    private transient long agpCptrCorePluginRegister;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CorePluginRegister(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCorePluginRegister = j;
    }

    static long getCptr(CorePluginRegister corePluginRegister) {
        if (corePluginRegister == null) {
            return 0;
        }
        return corePluginRegister.agpCptrCorePluginRegister;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCorePluginRegister != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCorePluginRegister(this.agpCptrCorePluginRegister);
                }
                this.agpCptrCorePluginRegister = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CorePluginRegister corePluginRegister, boolean z) {
        if (corePluginRegister != null) {
            synchronized (corePluginRegister.delLock) {
                corePluginRegister.isAgpCmemOwn = z;
            }
        }
        return getCptr(corePluginRegister);
    }

    /* access modifiers changed from: package-private */
    public CoreComponentManagerTypeInfoArray getComponentManagerMetadata() {
        return new CoreComponentManagerTypeInfoArray(CoreJni.getComponentManagerMetadataInCorePluginRegister(this.agpCptrCorePluginRegister, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreSystemTypeInfoArray getSystemMetadata() {
        return new CoreSystemTypeInfoArray(CoreJni.getSystemMetadataInCorePluginRegister(this.agpCptrCorePluginRegister, this), true);
    }
}
