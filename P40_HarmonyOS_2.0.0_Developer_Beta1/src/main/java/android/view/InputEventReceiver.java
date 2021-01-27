package android.view;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.os.IBlockMonitor;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.HwDynBufManager;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;

public abstract class InputEventReceiver {
    private static final boolean IS_DEBUG_VERSION;
    private static final String TAG = "InputEventReceiver";
    private static IBlockMonitor mBlockMonitor = HwFrameworkFactory.getBlockMonitor();
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private InputChannel mInputChannel;
    private MessageQueue mMessageQueue;
    private long mReceiverPtr;
    private final SparseIntArray mSeqMap = new SparseIntArray();
    private final SparseLongArray mStartTimeMap = new SparseLongArray();

    public interface Factory {
        InputEventReceiver createInputEventReceiver(InputChannel inputChannel, Looper looper);
    }

    private static native boolean nativeConsumeBatchedInputEvents(long j, long j2);

    private static native void nativeDispose(long j);

    private static native void nativeFinishInputEvent(long j, int i, boolean z);

    private static native long nativeInit(WeakReference<InputEventReceiver> weakReference, InputChannel inputChannel, MessageQueue messageQueue);

    private static native boolean nativePeekEvent(long j);

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    public InputEventReceiver(InputChannel inputChannel, Looper looper) {
        if (inputChannel == null) {
            throw new IllegalArgumentException("inputChannel must not be null");
        } else if (looper != null) {
            this.mInputChannel = inputChannel;
            this.mMessageQueue = looper.getQueue();
            this.mReceiverPtr = nativeInit(new WeakReference(this), inputChannel, this.mMessageQueue);
            this.mCloseGuard.open("dispose");
        } else {
            throw new IllegalArgumentException("looper must not be null");
        }
    }

    public boolean peekEvent() {
        return nativePeekEvent(this.mReceiverPtr);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
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
        CloseGuard closeGuard = this.mCloseGuard;
        if (closeGuard != null) {
            if (finalized) {
                closeGuard.warnIfOpen();
            }
            this.mCloseGuard.close();
        }
        long j = this.mReceiverPtr;
        if (j != 0) {
            nativeDispose(j);
            this.mReceiverPtr = 0;
        }
        this.mInputChannel = null;
        this.mMessageQueue = null;
    }

    @UnsupportedAppUsage
    public void onInputEvent(InputEvent event) {
        finishInputEvent(event, false);
    }

    public void onBatchedInputEventPending() {
        consumeBatchedInputEvents(-1);
    }

    public final void finishInputEvent(InputEvent event, boolean handled) {
        int index;
        if (event != null) {
            if (this.mReceiverPtr == 0) {
                Log.w(TAG, "Attempted to finish an input event but the input event receiver has already been disposed.");
            } else {
                int seqNumber = event.getSequenceNumber();
                if (IS_DEBUG_VERSION && (index = this.mStartTimeMap.indexOfKey(seqNumber)) > 0) {
                    mBlockMonitor.checkInputTime(this.mStartTimeMap.valueAt(index));
                    this.mStartTimeMap.removeAt(index);
                }
                int index2 = this.mSeqMap.indexOfKey(seqNumber);
                if (index2 < 0) {
                    Log.w(TAG, "Attempted to finish an input event that is not in progress.");
                } else {
                    int seq = this.mSeqMap.valueAt(index2);
                    this.mSeqMap.removeAt(index2);
                    nativeFinishInputEvent(this.mReceiverPtr, seq, handled);
                }
            }
            event.recycleIfNeededAfterDispatch();
            return;
        }
        throw new IllegalArgumentException("event must not be null");
    }

    public final boolean consumeBatchedInputEvents(long frameTimeNanos) {
        long j = this.mReceiverPtr;
        if (j != 0) {
            return nativeConsumeBatchedInputEvents(j, frameTimeNanos);
        }
        Log.w(TAG, "Attempted to consume batched input events but the input event receiver has already been disposed.");
        return false;
    }

    @UnsupportedAppUsage
    private void dispatchInputEvent(int seq, InputEvent event) {
        int seqNumber = event.getSequenceNumber();
        this.mSeqMap.put(seqNumber, seq);
        if (IS_DEBUG_VERSION) {
            this.mStartTimeMap.put(seqNumber, SystemClock.uptimeMillis());
            mBlockMonitor.checkInputReceiveTime(seq, event.getEventTime());
        }
        onInputEvent(event);
        mBlockMonitor.notifyInputEvent(event);
        HwDynBufManager.getImpl().notifyInputEvent(event);
    }

    @UnsupportedAppUsage
    private void dispatchBatchedInputEventPending() {
        onBatchedInputEventPending();
    }
}
