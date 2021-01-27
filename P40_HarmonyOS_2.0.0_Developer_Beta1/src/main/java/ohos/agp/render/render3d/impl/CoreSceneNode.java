package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreSceneNode {
    private transient long agpCptrCoreSceneNode;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CoreSceneNode(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreSceneNode = j;
    }

    static long getCptr(CoreSceneNode coreSceneNode) {
        if (coreSceneNode == null) {
            return 0;
        }
        return coreSceneNode.agpCptrCoreSceneNode;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreSceneNode != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreSceneNode(this.agpCptrCoreSceneNode);
                }
                this.agpCptrCoreSceneNode = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreSceneNode coreSceneNode, boolean z) {
        if (coreSceneNode != null) {
            synchronized (coreSceneNode.delLock) {
                coreSceneNode.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreSceneNode);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return CoreJni.getNameInCoreSceneNode(this.agpCptrCoreSceneNode, this);
    }

    /* access modifiers changed from: package-private */
    public void setName(String str) {
        CoreJni.setNameInCoreSceneNode(this.agpCptrCoreSceneNode, this, str);
    }

    /* access modifiers changed from: package-private */
    public void setEnabled(boolean z) {
        CoreJni.setEnabledInCoreSceneNode(this.agpCptrCoreSceneNode, this, z);
    }

    /* access modifiers changed from: package-private */
    public boolean getEnabled() {
        return CoreJni.getEnabledInCoreSceneNode(this.agpCptrCoreSceneNode, this);
    }

    /* access modifiers changed from: package-private */
    public boolean getEffectivelyEnabled() {
        return CoreJni.getEffectivelyEnabledInCoreSceneNode(this.agpCptrCoreSceneNode, this);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode getParent() {
        long parentInCoreSceneNode = CoreJni.getParentInCoreSceneNode(this.agpCptrCoreSceneNode, this);
        if (parentInCoreSceneNode == 0) {
            return null;
        }
        return new CoreSceneNode(parentInCoreSceneNode, false);
    }

    /* access modifiers changed from: package-private */
    public void setParent(CoreSceneNode coreSceneNode) {
        CoreJni.setParentInCoreSceneNode(this.agpCptrCoreSceneNode, this, getCptr(coreSceneNode), coreSceneNode);
    }

    /* access modifiers changed from: package-private */
    public boolean isAncestorOf(CoreSceneNode coreSceneNode) {
        return CoreJni.isAncestorOfInCoreSceneNode(this.agpCptrCoreSceneNode, this, getCptr(coreSceneNode), coreSceneNode);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNodeArrayView getChildren() {
        return new CoreSceneNodeArrayView(CoreJni.getChildrenInCoreSceneNode(this.agpCptrCoreSceneNode, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreEntity getEntity() {
        return new CoreEntity(CoreJni.getEntityInCoreSceneNode(this.agpCptrCoreSceneNode, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode getChild(String str) {
        long childInCoreSceneNode = CoreJni.getChildInCoreSceneNode(this.agpCptrCoreSceneNode, this, str);
        if (childInCoreSceneNode == 0) {
            return null;
        }
        return new CoreSceneNode(childInCoreSceneNode, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode lookupNodeByPath(String str) {
        long lookupNodeByPathInCoreSceneNode = CoreJni.lookupNodeByPathInCoreSceneNode(this.agpCptrCoreSceneNode, this, str);
        if (lookupNodeByPathInCoreSceneNode == 0) {
            return null;
        }
        return new CoreSceneNode(lookupNodeByPathInCoreSceneNode, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode lookupNodeByName(String str) {
        long lookupNodeByNameInCoreSceneNode = CoreJni.lookupNodeByNameInCoreSceneNode(this.agpCptrCoreSceneNode, this, str);
        if (lookupNodeByNameInCoreSceneNode == 0) {
            return null;
        }
        return new CoreSceneNode(lookupNodeByNameInCoreSceneNode, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode lookupNodeByComponent(CoreComponentManager coreComponentManager) {
        long lookupNodeByComponentInCoreSceneNode = CoreJni.lookupNodeByComponentInCoreSceneNode(this.agpCptrCoreSceneNode, this, CoreComponentManager.getCptr(coreComponentManager), coreComponentManager);
        if (lookupNodeByComponentInCoreSceneNode == 0) {
            return null;
        }
        return new CoreSceneNode(lookupNodeByComponentInCoreSceneNode, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNodeArray lookupNodesByComponent(CoreComponentManager coreComponentManager) {
        return new CoreSceneNodeArray(CoreJni.lookupNodesByComponentInCoreSceneNode(this.agpCptrCoreSceneNode, this, CoreComponentManager.getCptr(coreComponentManager), coreComponentManager), true);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getPosition() {
        return new CoreVec3(CoreJni.getPositionInCoreSceneNode(this.agpCptrCoreSceneNode, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreQuat getRotation() {
        return new CoreQuat(CoreJni.getRotationInCoreSceneNode(this.agpCptrCoreSceneNode, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getScale() {
        return new CoreVec3(CoreJni.getScaleInCoreSceneNode(this.agpCptrCoreSceneNode, this), true);
    }

    /* access modifiers changed from: package-private */
    public void setScale(CoreVec3 coreVec3) {
        CoreJni.setScaleInCoreSceneNode(this.agpCptrCoreSceneNode, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public void setPosition(CoreVec3 coreVec3) {
        CoreJni.setPositionInCoreSceneNode(this.agpCptrCoreSceneNode, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public void setRotation(CoreQuat coreQuat) {
        CoreJni.setRotationInCoreSceneNode(this.agpCptrCoreSceneNode, this, CoreQuat.getCptr(coreQuat), coreQuat);
    }
}
