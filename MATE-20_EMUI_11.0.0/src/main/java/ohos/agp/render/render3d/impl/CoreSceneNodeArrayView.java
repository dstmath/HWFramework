package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

/* access modifiers changed from: package-private */
public class CoreSceneNodeArrayView {
    private transient long agpCptrCoreSceneNodeArrayView;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreSceneNodeArrayView(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreSceneNodeArrayView = j;
    }

    static long getCptr(CoreSceneNodeArrayView coreSceneNodeArrayView) {
        if (coreSceneNodeArrayView == null) {
            return 0;
        }
        return coreSceneNodeArrayView.agpCptrCoreSceneNodeArrayView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreSceneNodeArrayView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreSceneNodeArrayView(this.agpCptrCoreSceneNodeArrayView);
                }
                this.agpCptrCoreSceneNodeArrayView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreSceneNodeArrayView coreSceneNodeArrayView, boolean z) {
        if (coreSceneNodeArrayView != null) {
            synchronized (coreSceneNodeArrayView.delLock) {
                coreSceneNodeArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreSceneNodeArrayView);
    }

    CoreSceneNodeArrayView(Buffer buffer) {
        this(CoreJni.newCoreSceneNodeArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreSceneNodeArrayView(this.agpCptrCoreSceneNodeArrayView, this);
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode get(long j) {
        long inCoreSceneNodeArrayView = CoreJni.getInCoreSceneNodeArrayView(this.agpCptrCoreSceneNodeArrayView, this, j);
        if (inCoreSceneNodeArrayView == 0) {
            return null;
        }
        return new CoreSceneNode(inCoreSceneNodeArrayView, false);
    }
}
