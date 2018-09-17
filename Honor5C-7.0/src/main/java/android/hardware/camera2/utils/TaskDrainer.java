package android.hardware.camera2.utils;

import android.os.Handler;
import com.android.internal.util.Preconditions;
import java.util.HashSet;
import java.util.Set;

public class TaskDrainer<T> {
    private static final String TAG = "TaskDrainer";
    private final boolean DEBUG;
    private boolean mDrainFinished;
    private boolean mDraining;
    private final Handler mHandler;
    private final DrainListener mListener;
    private final Object mLock;
    private final String mName;
    private final Set<T> mTaskSet;

    public interface DrainListener {
        void onDrained();
    }

    public TaskDrainer(Handler handler, DrainListener listener) {
        this.DEBUG = false;
        this.mTaskSet = new HashSet();
        this.mLock = new Object();
        this.mDraining = false;
        this.mDrainFinished = false;
        this.mHandler = (Handler) Preconditions.checkNotNull(handler, "handler must not be null");
        this.mListener = (DrainListener) Preconditions.checkNotNull(listener, "listener must not be null");
        this.mName = null;
    }

    public TaskDrainer(Handler handler, DrainListener listener, String name) {
        this.DEBUG = false;
        this.mTaskSet = new HashSet();
        this.mLock = new Object();
        this.mDraining = false;
        this.mDrainFinished = false;
        this.mHandler = (Handler) Preconditions.checkNotNull(handler, "handler must not be null");
        this.mListener = (DrainListener) Preconditions.checkNotNull(listener, "listener must not be null");
        this.mName = name;
    }

    public void taskStarted(T task) {
        synchronized (this.mLock) {
            if (this.mDraining) {
                throw new IllegalStateException("Can't start more tasks after draining has begun");
            } else if (this.mTaskSet.add(task)) {
            } else {
                throw new IllegalStateException("Task " + task + " was already started");
            }
        }
    }

    public void taskFinished(T task) {
        synchronized (this.mLock) {
            if (this.mTaskSet.remove(task)) {
                checkIfDrainFinished();
            } else {
                throw new IllegalStateException("Task " + task + " was already finished");
            }
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
        this.mHandler.post(new Runnable() {
            public void run() {
                TaskDrainer.this.mListener.onDrained();
            }
        });
    }
}
