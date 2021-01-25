package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreRayCastResult {
    private transient long agpCptrRayCastResult;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreRayCastResult(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrRayCastResult = j;
    }

    static long getCptr(CoreRayCastResult coreRayCastResult) {
        if (coreRayCastResult == null) {
            return 0;
        }
        return coreRayCastResult.agpCptrRayCastResult;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrRayCastResult != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreRayCastResult(this.agpCptrRayCastResult);
                }
                this.agpCptrRayCastResult = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRayCastResult coreRayCastResult, boolean z) {
        if (coreRayCastResult != null) {
            synchronized (coreRayCastResult.delLock) {
                coreRayCastResult.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRayCastResult);
    }

    /* access modifiers changed from: package-private */
    public void setNode(CoreSceneNode coreSceneNode) {
        CoreJni.setVarnodeCoreRayCastResult(this.agpCptrRayCastResult, this, CoreSceneNode.getCptr(coreSceneNode), coreSceneNode);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode getNode() {
        long varnodeCoreRayCastResult = CoreJni.getVarnodeCoreRayCastResult(this.agpCptrRayCastResult, this);
        if (varnodeCoreRayCastResult == 0) {
            return null;
        }
        return new CoreSceneNode(varnodeCoreRayCastResult, false);
    }

    /* access modifiers changed from: package-private */
    public void setDistance(float f) {
        CoreJni.setVardistanceCoreRayCastResult(this.agpCptrRayCastResult, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getDistance() {
        return CoreJni.getVardistanceCoreRayCastResult(this.agpCptrRayCastResult, this);
    }

    CoreRayCastResult() {
        this(CoreJni.newCoreRayCastResult(), true);
    }
}
