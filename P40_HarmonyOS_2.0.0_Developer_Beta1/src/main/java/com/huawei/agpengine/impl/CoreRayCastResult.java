package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreRayCastResult {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRayCastResult(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRayCastResult obj) {
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
                CoreJni.deleteCoreRayCastResult(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setNode(CoreSceneNode value) {
        CoreJni.setVarnodeCoreRayCastResult(this.agpCptr, this, CoreSceneNode.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode getNode() {
        long cptr = CoreJni.getVarnodeCoreRayCastResult(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setDistance(float value) {
        CoreJni.setVardistanceCoreRayCastResult(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public float getDistance() {
        return CoreJni.getVardistanceCoreRayCastResult(this.agpCptr, this);
    }

    CoreRayCastResult() {
        this(CoreJni.newCoreRayCastResult(), true);
    }
}
