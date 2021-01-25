package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreSceneNode {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSceneNode(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSceneNode obj) {
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
                CoreJni.deleteCoreSceneNode(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return CoreJni.getNameInCoreSceneNode(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setName(String name) {
        CoreJni.setNameInCoreSceneNode(this.agpCptr, this, name);
    }

    /* access modifiers changed from: package-private */
    public void setEnabled(boolean isEnabled) {
        CoreJni.setEnabledInCoreSceneNode(this.agpCptr, this, isEnabled);
    }

    /* access modifiers changed from: package-private */
    public boolean getEnabled() {
        return CoreJni.getEnabledInCoreSceneNode(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public boolean getEffectivelyEnabled() {
        return CoreJni.getEffectivelyEnabledInCoreSceneNode(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode getParent() {
        long cptr = CoreJni.getParentInCoreSceneNode(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setParent(CoreSceneNode node) {
        CoreJni.setParentInCoreSceneNode(this.agpCptr, this, getCptr(node), node);
    }

    /* access modifiers changed from: package-private */
    public boolean isAncestorOf(CoreSceneNode node) {
        return CoreJni.isAncestorOfInCoreSceneNode(this.agpCptr, this, getCptr(node), node);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNodeArrayView getChildren() {
        return new CoreSceneNodeArrayView(CoreJni.getChildrenInCoreSceneNode(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public int getEntity() {
        return CoreJni.getEntityInCoreSceneNode(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode getChild(String name) {
        long cptr = CoreJni.getChildInCoreSceneNode(this.agpCptr, this, name);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode lookupNodeByPath(String path) {
        long cptr = CoreJni.lookupNodeByPathInCoreSceneNode(this.agpCptr, this, path);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode lookupNodeByName(String name) {
        long cptr = CoreJni.lookupNodeByNameInCoreSceneNode(this.agpCptr, this, name);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode lookupNodeByComponent(CoreComponentManager componentManager) {
        long cptr = CoreJni.lookupNodeByComponentInCoreSceneNode(this.agpCptr, this, CoreComponentManager.getCptr(componentManager), componentManager);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNodeArray lookupNodesByComponent(CoreComponentManager componentManager) {
        return new CoreSceneNodeArray(CoreJni.lookupNodesByComponentInCoreSceneNode(this.agpCptr, this, CoreComponentManager.getCptr(componentManager), componentManager), true);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getPosition() {
        return new CoreVec3(CoreJni.getPositionInCoreSceneNode(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreQuat getRotation() {
        return new CoreQuat(CoreJni.getRotationInCoreSceneNode(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getScale() {
        return new CoreVec3(CoreJni.getScaleInCoreSceneNode(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public void setScale(CoreVec3 scale) {
        CoreJni.setScaleInCoreSceneNode(this.agpCptr, this, CoreVec3.getCptr(scale), scale);
    }

    /* access modifiers changed from: package-private */
    public void setPosition(CoreVec3 position) {
        CoreJni.setPositionInCoreSceneNode(this.agpCptr, this, CoreVec3.getCptr(position), position);
    }

    /* access modifiers changed from: package-private */
    public void setRotation(CoreQuat rotation) {
        CoreJni.setRotationInCoreSceneNode(this.agpCptr, this, CoreQuat.getCptr(rotation), rotation);
    }
}
