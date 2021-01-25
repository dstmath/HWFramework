package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

class CoreAnimationTrackDescArrayView {
    private transient long agpCptrCoreAnimTrackDescArrayView;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreAnimationTrackDescArrayView(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreAnimTrackDescArrayView = j;
    }

    static long getCptr(CoreAnimationTrackDescArrayView coreAnimationTrackDescArrayView) {
        if (coreAnimationTrackDescArrayView == null) {
            return 0;
        }
        return coreAnimationTrackDescArrayView.agpCptrCoreAnimTrackDescArrayView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreAnimTrackDescArrayView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreAnimationTrackDescArrayView(this.agpCptrCoreAnimTrackDescArrayView);
                }
                this.agpCptrCoreAnimTrackDescArrayView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreAnimationTrackDescArrayView coreAnimationTrackDescArrayView, boolean z) {
        if (coreAnimationTrackDescArrayView != null) {
            synchronized (coreAnimationTrackDescArrayView.lock) {
                coreAnimationTrackDescArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreAnimationTrackDescArrayView);
    }

    CoreAnimationTrackDescArrayView(Buffer buffer) {
        this(CoreJni.newCoreAnimationTrackDescArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreAnimationTrackDescArrayView(this.agpCptrCoreAnimTrackDescArrayView, this);
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationTrackDesc get(long j) {
        return new CoreAnimationTrackDesc(CoreJni.getInCoreAnimationTrackDescArrayView(this.agpCptrCoreAnimTrackDescArrayView, this, j), true);
    }
}
