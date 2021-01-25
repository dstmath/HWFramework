package ohos.agp.render.render3d.impl;

class CoreAnimationSystem extends CoreSystem {
    private transient long agpCptr;
    private final Object lock = new Object();

    CoreAnimationSystem(long j, boolean z) {
        super(CoreJni.classUpcastCoreAnimationSystem(j), z);
        this.agpCptr = j;
    }

    static long getCptr(CoreAnimationSystem coreAnimationSystem) {
        if (coreAnimationSystem == null) {
            return 0;
        }
        return coreAnimationSystem.agpCptr;
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
        synchronized (this.lock) {
            if (this.agpCptr != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreAnimationSystem(this.agpCptr);
                }
                this.agpCptr = 0;
            }
            super.delete();
        }
    }

    static long getCptrAndSetMemOwn(CoreAnimationSystem coreAnimationSystem, boolean z) {
        if (coreAnimationSystem != null) {
            coreAnimationSystem.isAgpCmemOwn = z;
        }
        return getCptr(coreAnimationSystem);
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationPlayback createPlayback(CoreResourceHandle coreResourceHandle, CoreSceneNode coreSceneNode) {
        long createPlaybackInCoreAnimationSystem = CoreJni.createPlaybackInCoreAnimationSystem(this.agpCptr, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle, CoreSceneNode.getCptr(coreSceneNode), coreSceneNode);
        if (createPlaybackInCoreAnimationSystem == 0) {
            return null;
        }
        return new CoreAnimationPlayback(createPlaybackInCoreAnimationSystem, false);
    }

    /* access modifiers changed from: package-private */
    public void destroyPlayback(CoreAnimationPlayback coreAnimationPlayback) {
        CoreJni.destroyPlaybackInCoreAnimationSystem(this.agpCptr, this, CoreAnimationPlayback.getCptr(coreAnimationPlayback), coreAnimationPlayback);
    }

    /* access modifiers changed from: package-private */
    public long getPlaybackCount() {
        return CoreJni.getPlaybackCountInCoreAnimationSystem(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationPlayback getPlayback(long j) {
        long playbackInCoreAnimationSystem = CoreJni.getPlaybackInCoreAnimationSystem(this.agpCptr, this, j);
        if (playbackInCoreAnimationSystem == 0) {
            return null;
        }
        return new CoreAnimationPlayback(playbackInCoreAnimationSystem, false);
    }
}
