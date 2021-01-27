package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreNodeSystem extends CoreSystem {
    private transient long agpCptrCoreNodeSystem;
    private final Object delLock = new Object();

    CoreNodeSystem(long j, boolean z) {
        super(CoreJni.classUpcastCoreNodeSystem(j), z);
        this.agpCptrCoreNodeSystem = j;
    }

    static long getCptr(CoreNodeSystem coreNodeSystem) {
        if (coreNodeSystem == null) {
            return 0;
        }
        return coreNodeSystem.agpCptrCoreNodeSystem;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.render.render3d.impl.CoreSystem
    public void finalize() {
        delete();
        super.finalize();
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreSystem
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreNodeSystem != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreNodeSystem(this.agpCptrCoreNodeSystem);
                }
                this.agpCptrCoreNodeSystem = 0;
            }
            super.delete();
        }
    }

    static long getCptrAndSetMemOwn(CoreNodeSystem coreNodeSystem, boolean z) {
        if (coreNodeSystem != null) {
            coreNodeSystem.isAgpCmemOwn = z;
        }
        return getCptr(coreNodeSystem);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode getRootNode() {
        return new CoreSceneNode(CoreJni.getRootNodeInCoreNodeSystem(this.agpCptrCoreNodeSystem, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode getNode(CoreEntity coreEntity) {
        long nodeInCoreNodeSystem = CoreJni.getNodeInCoreNodeSystem(this.agpCptrCoreNodeSystem, this, CoreEntity.getCptr(coreEntity), coreEntity);
        if (nodeInCoreNodeSystem == 0) {
            return null;
        }
        return new CoreSceneNode(nodeInCoreNodeSystem, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode createNode() {
        long createNodeInCoreNodeSystem = CoreJni.createNodeInCoreNodeSystem(this.agpCptrCoreNodeSystem, this);
        if (createNodeInCoreNodeSystem == 0) {
            return null;
        }
        return new CoreSceneNode(createNodeInCoreNodeSystem, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode cloneNode(CoreSceneNode coreSceneNode, boolean z) {
        long cloneNodeInCoreNodeSystem = CoreJni.cloneNodeInCoreNodeSystem(this.agpCptrCoreNodeSystem, this, CoreSceneNode.getCptr(coreSceneNode), coreSceneNode, z);
        if (cloneNodeInCoreNodeSystem == 0) {
            return null;
        }
        return new CoreSceneNode(cloneNodeInCoreNodeSystem, false);
    }

    /* access modifiers changed from: package-private */
    public void destroyNode(CoreSceneNode coreSceneNode) {
        CoreJni.destroyNodeInCoreNodeSystem(this.agpCptrCoreNodeSystem, this, CoreSceneNode.getCptr(coreSceneNode), coreSceneNode);
    }
}
