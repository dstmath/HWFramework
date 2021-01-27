package ohos.agp.vsync;

import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import java.util.ArrayList;
import java.util.Iterator;
import ohos.agp.vsync.VsyncScheduler;

public class VsyncScheduler {
    private static final String TAG = "VsyncScheduler";
    private final Choreographer.FrameCallback mFrameCallback;
    private final ArrayList<FrameCallback> mFrameCallbackList;
    private Handler mHandler;
    private final ArrayList<FrameCallback> mSystemFrameCallbackList;

    public interface FrameCallback {
        void doFrame(long j);
    }

    public /* synthetic */ void lambda$new$0$VsyncScheduler(long j) {
        ArrayList arrayList;
        synchronized (this.mFrameCallbackList) {
            arrayList = new ArrayList(this.mFrameCallbackList);
            this.mFrameCallbackList.clear();
        }
        synchronized (this.mSystemFrameCallbackList) {
            Iterator<FrameCallback> it = this.mSystemFrameCallbackList.iterator();
            while (it.hasNext()) {
                it.next().doFrame(j);
            }
        }
        Iterator it2 = arrayList.iterator();
        while (it2.hasNext()) {
            ((FrameCallback) it2.next()).doFrame(j);
        }
    }

    private VsyncScheduler() {
        this.mFrameCallbackList = new ArrayList<>();
        this.mSystemFrameCallbackList = new ArrayList<>();
        this.mFrameCallback = new Choreographer.FrameCallback() {
            /* class ohos.agp.vsync.$$Lambda$VsyncScheduler$WKOzHr1ZieuLysOC_jZevipZI98 */

            @Override // android.view.Choreographer.FrameCallback
            public final void doFrame(long j) {
                VsyncScheduler.this.lambda$new$0$VsyncScheduler(j);
            }
        };
        if (Looper.getMainLooper() != null) {
            this.mHandler = new Handler(Looper.getMainLooper());
        }
    }

    /* access modifiers changed from: private */
    public static class SingletonHolder {
        private static final VsyncScheduler INSTANCE = new VsyncScheduler();

        private SingletonHolder() {
        }
    }

    public static VsyncScheduler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addFrameCallbackForSystem(FrameCallback frameCallback) {
        synchronized (this.mSystemFrameCallbackList) {
            this.mSystemFrameCallbackList.add(frameCallback);
        }
    }

    /* renamed from: requestVsync */
    public void lambda$postRequestVsync$1$VsyncScheduler(FrameCallback frameCallback) {
        boolean isEmpty;
        synchronized (this.mFrameCallbackList) {
            isEmpty = this.mFrameCallbackList.isEmpty();
            if (frameCallback != null && !this.mFrameCallbackList.contains(frameCallback)) {
                this.mFrameCallbackList.add(frameCallback);
            }
        }
        if (isEmpty) {
            Choreographer.getInstance().postFrameCallback(this.mFrameCallback);
        }
    }

    public void postRequestVsync(FrameCallback frameCallback) {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.post(new Runnable(frameCallback) {
                /* class ohos.agp.vsync.$$Lambda$VsyncScheduler$YcDJFl1yu2fIhbYfzk1XJZH6so */
                private final /* synthetic */ VsyncScheduler.FrameCallback f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    VsyncScheduler.this.lambda$postRequestVsync$1$VsyncScheduler(this.f$1);
                }
            });
        }
    }
}
