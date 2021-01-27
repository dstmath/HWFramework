package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreTransformComponent {
    private transient long agpCptrCoreTransformComponent;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreTransformComponent(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreTransformComponent = j;
    }

    static long getCptr(CoreTransformComponent coreTransformComponent) {
        if (coreTransformComponent == null) {
            return 0;
        }
        return coreTransformComponent.agpCptrCoreTransformComponent;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreTransformComponent != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreTransformComponent(this.agpCptrCoreTransformComponent);
                }
                this.agpCptrCoreTransformComponent = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreTransformComponent coreTransformComponent, boolean z) {
        if (coreTransformComponent != null) {
            synchronized (coreTransformComponent.delLock) {
                coreTransformComponent.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreTransformComponent);
    }

    /* access modifiers changed from: package-private */
    public void setPosition(CoreVec3 coreVec3) {
        CoreJni.setVarpositionCoreTransformComponent(this.agpCptrCoreTransformComponent, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getPosition() {
        long varpositionCoreTransformComponent = CoreJni.getVarpositionCoreTransformComponent(this.agpCptrCoreTransformComponent, this);
        if (varpositionCoreTransformComponent == 0) {
            return null;
        }
        return new CoreVec3(varpositionCoreTransformComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setRotation(CoreQuat coreQuat) {
        CoreJni.setVarrotationCoreTransformComponent(this.agpCptrCoreTransformComponent, this, CoreQuat.getCptr(coreQuat), coreQuat);
    }

    /* access modifiers changed from: package-private */
    public CoreQuat getRotation() {
        long varrotationCoreTransformComponent = CoreJni.getVarrotationCoreTransformComponent(this.agpCptrCoreTransformComponent, this);
        if (varrotationCoreTransformComponent == 0) {
            return null;
        }
        return new CoreQuat(varrotationCoreTransformComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setScale(CoreVec3 coreVec3) {
        CoreJni.setVarscaleCoreTransformComponent(this.agpCptrCoreTransformComponent, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getScale() {
        long varscaleCoreTransformComponent = CoreJni.getVarscaleCoreTransformComponent(this.agpCptrCoreTransformComponent, this);
        if (varscaleCoreTransformComponent == 0) {
            return null;
        }
        return new CoreVec3(varscaleCoreTransformComponent, false);
    }

    CoreTransformComponent() {
        this(CoreJni.newCoreTransformComponent(), true);
    }
}
