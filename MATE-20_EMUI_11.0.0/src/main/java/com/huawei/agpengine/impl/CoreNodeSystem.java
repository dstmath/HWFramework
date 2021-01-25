package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreNodeSystem extends CoreSystem {
    private transient long agpCptr;

    CoreNodeSystem(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreNodeSystem(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreNodeSystem obj) {
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
    @Override // com.huawei.agpengine.impl.CoreSystem
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (this.isAgpCmemOwn) {
                this.isAgpCmemOwn = false;
                CoreJni.deleteCoreNodeSystem(this.agpCptr);
            }
            this.agpCptr = 0;
        }
        super.delete();
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode getRootNode() {
        return new CoreSceneNode(CoreJni.getRootNodeInCoreNodeSystem(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode getNode(int entity) {
        long cptr = CoreJni.getNodeInCoreNodeSystem(this.agpCptr, this, entity);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode createNode() {
        long cptr = CoreJni.createNodeInCoreNodeSystem(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode cloneNode(CoreSceneNode node, boolean recursive) {
        long cptr = CoreJni.cloneNodeInCoreNodeSystem(this.agpCptr, this, CoreSceneNode.getCptr(node), node, recursive);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void destroyNode(CoreSceneNode node) {
        CoreJni.destroyNodeInCoreNodeSystem(this.agpCptr, this, CoreSceneNode.getCptr(node), node);
    }
}
