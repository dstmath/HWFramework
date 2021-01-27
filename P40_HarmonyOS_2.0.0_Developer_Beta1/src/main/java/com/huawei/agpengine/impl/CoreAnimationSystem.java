package com.huawei.agpengine.impl;

class CoreAnimationSystem extends CoreSystem {
    private transient long agpCptr;

    CoreAnimationSystem(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreAnimationSystem(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreAnimationSystem obj) {
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
                CoreJni.deleteCoreAnimationSystem(this.agpCptr);
            }
            this.agpCptr = 0;
        }
        super.delete();
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationPlayback createPlayback(CoreResourceHandle animationHandle, CoreSceneNode node) {
        long cptr = CoreJni.createPlaybackInCoreAnimationSystem(this.agpCptr, this, CoreResourceHandle.getCptr(animationHandle), animationHandle, CoreSceneNode.getCptr(node), node);
        if (cptr == 0) {
            return null;
        }
        return new CoreAnimationPlayback(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void destroyPlayback(CoreAnimationPlayback playback) {
        CoreJni.destroyPlaybackInCoreAnimationSystem(this.agpCptr, this, CoreAnimationPlayback.getCptr(playback), playback);
    }

    /* access modifiers changed from: package-private */
    public long getPlaybackCount() {
        return CoreJni.getPlaybackCountInCoreAnimationSystem(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationPlayback getPlayback(long index) {
        long cptr = CoreJni.getPlaybackInCoreAnimationSystem(this.agpCptr, this, index);
        if (cptr == 0) {
            return null;
        }
        return new CoreAnimationPlayback(cptr, false);
    }
}
