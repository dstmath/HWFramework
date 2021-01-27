package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMorphComponent {
    private transient long agpCptrCoreMorphComponent;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreMorphComponent(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreMorphComponent = j;
    }

    static long getCptr(CoreMorphComponent coreMorphComponent) {
        if (coreMorphComponent == null) {
            return 0;
        }
        return coreMorphComponent.agpCptrCoreMorphComponent;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreMorphComponent != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreMorphComponent(this.agpCptrCoreMorphComponent);
                }
                this.agpCptrCoreMorphComponent = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreMorphComponent coreMorphComponent, boolean z) {
        if (coreMorphComponent != null) {
            synchronized (coreMorphComponent.delLock) {
                coreMorphComponent.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreMorphComponent);
    }

    /* access modifiers changed from: package-private */
    public void setMorphTargets(long j) {
        CoreJni.setVarmorphTargetsCoreMorphComponent(this.agpCptrCoreMorphComponent, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getMorphTargets() {
        return CoreJni.getVarmorphTargetsCoreMorphComponent(this.agpCptrCoreMorphComponent, this);
    }

    CoreMorphComponent() {
        this(CoreJni.newCoreMorphComponent(), true);
    }
}
