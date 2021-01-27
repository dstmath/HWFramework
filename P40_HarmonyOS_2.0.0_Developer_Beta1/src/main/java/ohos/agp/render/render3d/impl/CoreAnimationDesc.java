package ohos.agp.render.render3d.impl;

class CoreAnimationDesc {
    private transient long agpCptrCoreAnimationDesc;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreAnimationDesc(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreAnimationDesc = j;
    }

    static long getCptr(CoreAnimationDesc coreAnimationDesc) {
        if (coreAnimationDesc == null) {
            return 0;
        }
        return coreAnimationDesc.agpCptrCoreAnimationDesc;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreAnimationDesc != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreAnimationDesc(this.agpCptrCoreAnimationDesc);
                }
                this.agpCptrCoreAnimationDesc = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreAnimationDesc coreAnimationDesc, boolean z) {
        if (coreAnimationDesc != null) {
            synchronized (coreAnimationDesc.lock) {
                coreAnimationDesc.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreAnimationDesc);
    }

    /* access modifiers changed from: package-private */
    public void setName(String str) {
        CoreJni.setVarnameCoreAnimationDesc(this.agpCptrCoreAnimationDesc, this, str);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return CoreJni.getVarnameCoreAnimationDesc(this.agpCptrCoreAnimationDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setTracks(CoreResourceDataHandle coreResourceDataHandle) {
        CoreJni.setVartracksCoreAnimationDesc(this.agpCptrCoreAnimationDesc, this, CoreResourceDataHandle.getCptr(coreResourceDataHandle), coreResourceDataHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceDataHandle getTracks() {
        long vartracksCoreAnimationDesc = CoreJni.getVartracksCoreAnimationDesc(this.agpCptrCoreAnimationDesc, this);
        if (vartracksCoreAnimationDesc == 0) {
            return null;
        }
        return new CoreResourceDataHandle(vartracksCoreAnimationDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setTrackCount(long j) {
        CoreJni.setVartrackCountCoreAnimationDesc(this.agpCptrCoreAnimationDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getTrackCount() {
        return CoreJni.getVartrackCountCoreAnimationDesc(this.agpCptrCoreAnimationDesc, this);
    }

    CoreAnimationDesc() {
        this(CoreJni.newCoreAnimationDesc(), true);
    }
}
