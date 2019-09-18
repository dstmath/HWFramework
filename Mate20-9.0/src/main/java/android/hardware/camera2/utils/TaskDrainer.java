package android.hardware.camera2.utils;

import com.android.internal.util.Preconditions;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

public class TaskDrainer<T> {
    private static final String TAG = "TaskDrainer";
    private final boolean DEBUG = false;
    private boolean mDrainFinished = false;
    private boolean mDraining = false;
    private final Set<T> mEarlyFinishedTaskSet = new HashSet();
    private final Executor mExecutor;
    private final DrainListener mListener;
    private final Object mLock = new Object();
    private final String mName;
    private final Set<T> mTaskSet = new HashSet();

    public interface DrainListener {
        void onDrained();
    }

    public TaskDrainer(Executor executor, DrainListener listener) {
        this.mExecutor = (Executor) Preconditions.checkNotNull(executor, "executor must not be null");
        this.mListener = (DrainListener) Preconditions.checkNotNull(listener, "listener must not be null");
        this.mName = null;
    }

    public TaskDrainer(Executor executor, DrainListener listener, String name) {
        this.mExecutor = (Executor) Preconditions.checkNotNull(executor, "executor must not be null");
        this.mListener = (DrainListener) Preconditions.checkNotNull(listener, "listener must not be null");
        this.mName = name;
    }

    public void taskStarted(T task) {
        synchronized (this.mLock) {
            if (this.mDraining) {
                throw new IllegalStateException("Can't start more tasks after draining has begun");
            } else if (!this.mEarlyFinishedTaskSet.remove(task)) {
                if (!this.mTaskSet.add(task)) {
                    throw new IllegalStateException("Task " + task + " was already started");
                }
            }
        }
    }

    public void taskFinished(T task) {
        synchronized (this.mLock) {
            if (!this.mTaskSet.remove(task)) {
                if (!this.mEarlyFinishedTaskSet.add(task)) {
                    throw new IllegalStateException("Task " + task + " was already finished");
                }
            }
            checkIfDrainFinished();
        }
    }

    public void beginDrain() {
        synchronized (this.mLock) {
            if (!this.mDraining) {
                this.mDraining = true;
                checkIfDrainFinished();
            }
        }
    }

    private void checkIfDrainFinished() {
        if (this.mTaskSet.isEmpty() && this.mDraining && !this.mDrainFinished) {
            this.mDrainFinished = true;
            postDrained();
        }
    }

    private void postDrained() {
        this.mExecutor.execute(new Runnable() {
            public final void run() {
                TaskDrainer.this.mListener.onDrained();
            }
        });
    }
}
