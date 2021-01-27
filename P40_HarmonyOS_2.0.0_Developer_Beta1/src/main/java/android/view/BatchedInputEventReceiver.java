package android.view;

import android.annotation.UnsupportedAppUsage;
import android.os.Looper;

public class BatchedInputEventReceiver extends InputEventReceiver {
    private final BatchedInputRunnable mBatchedInputRunnable = new BatchedInputRunnable();
    private boolean mBatchedInputScheduled;
    Choreographer mChoreographer;

    @UnsupportedAppUsage
    public BatchedInputEventReceiver(InputChannel inputChannel, Looper looper, Choreographer choreographer) {
        super(inputChannel, looper);
        this.mChoreographer = choreographer;
    }

    @Override // android.view.InputEventReceiver
    public void onBatchedInputEventPending() {
        scheduleBatchedInput();
    }

    @Override // android.view.InputEventReceiver
    public void dispose() {
        unscheduleBatchedInput();
        super.dispose();
    }

    /* access modifiers changed from: package-private */
    public void doConsumeBatchedInput(long frameTimeNanos) {
        if (this.mBatchedInputScheduled) {
            this.mBatchedInputScheduled = false;
            if (consumeBatchedInputEvents(frameTimeNanos) && frameTimeNanos != -1) {
                scheduleBatchedInput();
            }
        }
    }

    private void scheduleBatchedInput() {
        if (!this.mBatchedInputScheduled) {
            this.mBatchedInputScheduled = true;
            this.mChoreographer.postCallback(0, this.mBatchedInputRunnable, null);
        }
    }

    private void unscheduleBatchedInput() {
        if (this.mBatchedInputScheduled) {
            this.mBatchedInputScheduled = false;
            this.mChoreographer.removeCallbacks(0, this.mBatchedInputRunnable, null);
        }
    }

    /* access modifiers changed from: private */
    public final class BatchedInputRunnable implements Runnable {
        private BatchedInputRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            BatchedInputEventReceiver batchedInputEventReceiver = BatchedInputEventReceiver.this;
            batchedInputEventReceiver.doConsumeBatchedInput(batchedInputEventReceiver.mChoreographer.getFrameTimeNanos());
        }
    }
}
