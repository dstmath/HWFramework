package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreTransformComponent {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreTransformComponent(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreTransformComponent obj) {
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
                CoreJni.deleteCoreTransformComponent(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setPosition(CoreVec3 value) {
        CoreJni.setVarpositionCoreTransformComponent(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getPosition() {
        long cptr = CoreJni.getVarpositionCoreTransformComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setRotation(CoreQuat value) {
        CoreJni.setVarrotationCoreTransformComponent(this.agpCptr, this, CoreQuat.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreQuat getRotation() {
        long cptr = CoreJni.getVarrotationCoreTransformComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreQuat(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setScale(CoreVec3 value) {
        CoreJni.setVarscaleCoreTransformComponent(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getScale() {
        long cptr = CoreJni.getVarscaleCoreTransformComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    CoreTransformComponent() {
        this(CoreJni.newCoreTransformComponent(), true);
    }
}
