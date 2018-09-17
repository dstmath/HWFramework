package android.view;

import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;

public abstract class DisplayEventReceiver {
    private static final String TAG = "DisplayEventReceiver";
    public static final int VSYNC_SOURCE_APP = 0;
    public static final int VSYNC_SOURCE_SURFACE_FLINGER = 1;
    private final CloseGuard mCloseGuard;
    private MessageQueue mMessageQueue;
    private long mReceiverPtr;

    private static native void nativeDispose(long j);

    private static native long nativeInit(WeakReference<DisplayEventReceiver> weakReference, MessageQueue messageQueue, int i);

    private static native void nativeScheduleVsync(long j);

    public DisplayEventReceiver(Looper looper) {
        this(looper, 0);
    }

    public DisplayEventReceiver(Looper looper, int vsyncSource) {
        this.mCloseGuard = CloseGuard.get();
        if (looper == null) {
            throw new IllegalArgumentException("looper must not be null");
        }
        this.mMessageQueue = looper.getQueue();
        this.mReceiverPtr = nativeInit(new WeakReference(this), this.mMessageQueue, vsyncSource);
        this.mCloseGuard.open("dispose");
    }

    protected void finalize() throws Throwable {
        try {
            dispose(true);
        } finally {
            super.finalize();
        }
    }

    public void dispose() {
        dispose(false);
    }

    private void dispose(boolean finalized) {
        if (this.mCloseGuard != null) {
            if (finalized) {
                this.mCloseGuard.warnIfOpen();
            }
            this.mCloseGuard.close();
        }
        if (this.mReceiverPtr != 0) {
            nativeDispose(this.mReceiverPtr);
            this.mReceiverPtr = 0;
        }
        this.mMessageQueue = null;
    }

    public void onVsync(long timestampNanos, int builtInDisplayId, int frame) {
    }

    public void onHotplug(long timestampNanos, int builtInDisplayId, boolean connected) {
    }

    public void scheduleVsync() {
        if (this.mReceiverPtr == 0) {
            Log.w(TAG, "Attempted to schedule a vertical sync pulse but the display event receiver has already been disposed.");
        } else {
            nativeScheduleVsync(this.mReceiverPtr);
        }
    }

    private void dispatchVsync(long timestampNanos, int builtInDisplayId, int frame) {
        onVsync(timestampNanos, builtInDisplayId, frame);
    }

    private void dispatchHotplug(long timestampNanos, int builtInDisplayId, boolean connected) {
        onHotplug(timestampNanos, builtInDisplayId, connected);
    }
}
