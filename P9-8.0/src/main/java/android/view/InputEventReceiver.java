package android.view;

import android.os.BlockMonitor;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseIntArray;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;

public abstract class InputEventReceiver {
    private static final String TAG = "InputEventReceiver";
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private InputChannel mInputChannel;
    private MessageQueue mMessageQueue;
    private long mReceiverPtr;
    private final SparseIntArray mSeqMap = new SparseIntArray();

    public interface Factory {
        InputEventReceiver createInputEventReceiver(InputChannel inputChannel, Looper looper);
    }

    private static native boolean nativeConsumeBatchedInputEvents(long j, long j2);

    private static native void nativeDispose(long j);

    private static native void nativeFinishInputEvent(long j, int i, boolean z);

    private static native long nativeInit(WeakReference<InputEventReceiver> weakReference, InputChannel inputChannel, MessageQueue messageQueue);

    public InputEventReceiver(InputChannel inputChannel, Looper looper) {
        if (inputChannel == null) {
            throw new IllegalArgumentException("inputChannel must not be null");
        } else if (looper == null) {
            throw new IllegalArgumentException("looper must not be null");
        } else {
            this.mInputChannel = inputChannel;
            this.mMessageQueue = looper.getQueue();
            this.mReceiverPtr = nativeInit(new WeakReference(this), inputChannel, this.mMessageQueue);
            this.mCloseGuard.open("dispose");
        }
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
        this.mInputChannel = null;
        this.mMessageQueue = null;
    }

    public void onInputEvent(InputEvent event) {
        finishInputEvent(event, false);
    }

    public void onBatchedInputEventPending() {
        consumeBatchedInputEvents(-1);
    }

    public final void finishInputEvent(InputEvent event, boolean handled) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        if (this.mReceiverPtr == 0) {
            Log.w(TAG, "Attempted to finish an input event but the input event receiver has already been disposed.");
        } else {
            int index = this.mSeqMap.indexOfKey(event.getSequenceNumber());
            if (index < 0) {
                Log.w(TAG, "Attempted to finish an input event that is not in progress.");
            } else {
                int seq = this.mSeqMap.valueAt(index);
                this.mSeqMap.removeAt(index);
                nativeFinishInputEvent(this.mReceiverPtr, seq, handled);
            }
        }
        event.recycleIfNeededAfterDispatch();
    }

    public final boolean consumeBatchedInputEvents(long frameTimeNanos) {
        if (this.mReceiverPtr != 0) {
            return nativeConsumeBatchedInputEvents(this.mReceiverPtr, frameTimeNanos);
        }
        Log.w(TAG, "Attempted to consume batched input events but the input event receiver has already been disposed.");
        return false;
    }

    private void dispatchInputEvent(int seq, InputEvent event) {
        this.mSeqMap.put(event.getSequenceNumber(), seq);
        if (BlockMonitor.isNeedMonitor()) {
            long startTime = SystemClock.uptimeMillis();
            onInputEvent(event);
            BlockMonitor.checkInputTime(startTime);
            return;
        }
        onInputEvent(event);
    }

    private void dispatchBatchedInputEventPending() {
        onBatchedInputEventPending();
    }
}
