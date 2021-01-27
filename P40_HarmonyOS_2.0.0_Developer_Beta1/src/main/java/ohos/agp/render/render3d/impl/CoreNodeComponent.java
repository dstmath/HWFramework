package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreNodeComponent {
    private transient long agpCptrCoreNodeComponent;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreNodeComponent(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreNodeComponent = j;
    }

    static long getCptr(CoreNodeComponent coreNodeComponent) {
        if (coreNodeComponent == null) {
            return 0;
        }
        return coreNodeComponent.agpCptrCoreNodeComponent;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreNodeComponent != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreNodeComponent(this.agpCptrCoreNodeComponent);
                }
                this.agpCptrCoreNodeComponent = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreNodeComponent coreNodeComponent, boolean z) {
        if (coreNodeComponent != null) {
            synchronized (coreNodeComponent.delLock) {
                coreNodeComponent.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreNodeComponent);
    }

    /* access modifiers changed from: package-private */
    public void setName(String str) {
        CoreJni.setVarnameCoreNodeComponent(this.agpCptrCoreNodeComponent, this, str);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return CoreJni.getVarnameCoreNodeComponent(this.agpCptrCoreNodeComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setParent(CoreEntity coreEntity) {
        CoreJni.setVarparentCoreNodeComponent(this.agpCptrCoreNodeComponent, this, CoreEntity.getCptr(coreEntity), coreEntity);
    }

    /* access modifiers changed from: package-private */
    public CoreEntity getParent() {
        long varparentCoreNodeComponent = CoreJni.getVarparentCoreNodeComponent(this.agpCptrCoreNodeComponent, this);
        if (varparentCoreNodeComponent == 0) {
            return null;
        }
        return new CoreEntity(varparentCoreNodeComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setEnabled(boolean z) {
        CoreJni.setVarenabledCoreNodeComponent(this.agpCptrCoreNodeComponent, this, z);
    }

    /* access modifiers changed from: package-private */
    public boolean getEnabled() {
        return CoreJni.getVarenabledCoreNodeComponent(this.agpCptrCoreNodeComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setEffectivelyEnabled(boolean z) {
        CoreJni.setVareffectivelyEnabledCoreNodeComponent(this.agpCptrCoreNodeComponent, this, z);
    }

    /* access modifiers changed from: package-private */
    public boolean getEffectivelyEnabled() {
        return CoreJni.getVareffectivelyEnabledCoreNodeComponent(this.agpCptrCoreNodeComponent, this);
    }

    /* access modifiers changed from: package-private */
    public void setExported(boolean z) {
        CoreJni.setVarexportedCoreNodeComponent(this.agpCptrCoreNodeComponent, this, z);
    }

    /* access modifiers changed from: package-private */
    public boolean getExported() {
        return CoreJni.getVarexportedCoreNodeComponent(this.agpCptrCoreNodeComponent, this);
    }

    CoreNodeComponent() {
        this(CoreJni.newCoreNodeComponent(), true);
    }
}
