package android.view;

import android.os.Looper;
import android.os.MessageQueue;
import android.util.LongSparseArray;
import android.util.Pools.Pool;
import android.util.Pools.SimplePool;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;

public final class InputQueue {
    private final LongSparseArray<ActiveInputEvent> mActiveEventArray;
    private final Pool<ActiveInputEvent> mActiveInputEventPool;
    private final CloseGuard mCloseGuard;
    private long mPtr;

    private final class ActiveInputEvent {
        public FinishedInputEventCallback mCallback;
        public Object mToken;

        private ActiveInputEvent() {
        }

        public void recycle() {
            this.mToken = null;
            this.mCallback = null;
        }
    }

    public interface Callback {
        void onInputQueueCreated(InputQueue inputQueue);

        void onInputQueueDestroyed(InputQueue inputQueue);
    }

    public interface FinishedInputEventCallback {
        void onFinishedInputEvent(Object obj, boolean z);
    }

    private static native void nativeDispose(long j);

    private static native long nativeInit(WeakReference<InputQueue> weakReference, MessageQueue messageQueue);

    private static native long nativeSendKeyEvent(long j, KeyEvent keyEvent, boolean z);

    private static native long nativeSendMotionEvent(long j, MotionEvent motionEvent);

    public InputQueue() {
        this.mActiveEventArray = new LongSparseArray(20);
        this.mActiveInputEventPool = new SimplePool(20);
        this.mCloseGuard = CloseGuard.get();
        this.mPtr = nativeInit(new WeakReference(this), Looper.myQueue());
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

    public void dispose(boolean finalized) {
        if (this.mCloseGuard != null) {
            if (finalized) {
                this.mCloseGuard.warnIfOpen();
            }
            this.mCloseGuard.close();
        }
        if (this.mPtr != 0) {
            nativeDispose(this.mPtr);
            this.mPtr = 0;
        }
    }

    public long getNativePtr() {
        return this.mPtr;
    }

    public void sendInputEvent(InputEvent e, Object token, boolean predispatch, FinishedInputEventCallback callback) {
        long id;
        ActiveInputEvent event = obtainActiveInputEvent(token, callback);
        if (e instanceof KeyEvent) {
            id = nativeSendKeyEvent(this.mPtr, (KeyEvent) e, predispatch);
        } else {
            id = nativeSendMotionEvent(this.mPtr, (MotionEvent) e);
        }
        this.mActiveEventArray.put(id, event);
    }

    private void finishInputEvent(long id, boolean handled) {
        int index = this.mActiveEventArray.indexOfKey(id);
        if (index >= 0) {
            ActiveInputEvent e = (ActiveInputEvent) this.mActiveEventArray.valueAt(index);
            this.mActiveEventArray.removeAt(index);
            e.mCallback.onFinishedInputEvent(e.mToken, handled);
            recycleActiveInputEvent(e);
        }
    }

    private ActiveInputEvent obtainActiveInputEvent(Object token, FinishedInputEventCallback callback) {
        ActiveInputEvent e = (ActiveInputEvent) this.mActiveInputEventPool.acquire();
        if (e == null) {
            e = new ActiveInputEvent();
        }
        e.mToken = token;
        e.mCallback = callback;
        return e;
    }

    private void recycleActiveInputEvent(ActiveInputEvent e) {
        e.recycle();
        this.mActiveInputEventPool.release(e);
    }
}
