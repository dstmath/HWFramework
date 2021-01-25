package ohos.agp.vsync;

import android.view.Choreographer;
import java.util.ArrayList;
import java.util.Iterator;

public class VsyncScheduler {
    private static final String TAG = "VsyncScheduler";
    private final ArrayList<FrameCallback> mFrameCallbackList;
    private final ArrayList<FrameCallback> mSystemFrameCallbackList;

    public interface FrameCallback {
        void doFrame(long j);
    }

    private VsyncScheduler() {
        this.mFrameCallbackList = new ArrayList<>();
        this.mSystemFrameCallbackList = new ArrayList<>();
    }

    /* access modifiers changed from: private */
    public static class SingletonHolder {
        private static VsyncScheduler instance = new VsyncScheduler();

        private SingletonHolder() {
        }
    }

    public static VsyncScheduler getInstance() {
        return SingletonHolder.instance;
    }

    public void addFrameCallbackForSystem(FrameCallback frameCallback) {
        synchronized (this.mSystemFrameCallbackList) {
            this.mSystemFrameCallbackList.add(frameCallback);
        }
    }

    public void requestVsync(FrameCallback frameCallback) {
        boolean isEmpty;
        synchronized (this.mFrameCallbackList) {
            isEmpty = this.mFrameCallbackList.isEmpty();
            if (!this.mFrameCallbackList.contains(frameCallback)) {
                this.mFrameCallbackList.add(frameCallback);
            }
        }
        if (isEmpty) {
            Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
                /* class ohos.agp.vsync.VsyncScheduler.AnonymousClass1 */

                @Override // android.view.Choreographer.FrameCallback
                public void doFrame(long j) {
                    ArrayList arrayList;
                    synchronized (VsyncScheduler.this.mFrameCallbackList) {
                        arrayList = new ArrayList(VsyncScheduler.this.mFrameCallbackList);
                        VsyncScheduler.this.mFrameCallbackList.clear();
                    }
                    synchronized (VsyncScheduler.this.mSystemFrameCallbackList) {
                        Iterator it = VsyncScheduler.this.mSystemFrameCallbackList.iterator();
                        while (it.hasNext()) {
                            ((FrameCallback) it.next()).doFrame(j);
                        }
                    }
                    Iterator it2 = arrayList.iterator();
                    while (it2.hasNext()) {
                        ((FrameCallback) it2.next()).doFrame(j);
                    }
                }
            });
        }
    }
}
