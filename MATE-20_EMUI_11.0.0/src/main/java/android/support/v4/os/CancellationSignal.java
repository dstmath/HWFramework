package android.support.v4.os;

import android.os.Build;

public final class CancellationSignal {
    private boolean mCancelInProgress;
    private Object mCancellationSignalObj;
    private boolean mIsCanceled;
    private OnCancelListener mOnCancelListener;

    public interface OnCancelListener {
        void onCancel();
    }

    public boolean isCanceled() {
        boolean z;
        synchronized (this) {
            z = this.mIsCanceled;
        }
        return z;
    }

    public void throwIfCanceled() {
        if (isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    public void cancel() {
        Throwable th;
        OnCancelListener listener;
        Object obj;
        synchronized (this) {
            try {
                if (!this.mIsCanceled) {
                    this.mIsCanceled = true;
                    this.mCancelInProgress = true;
                    listener = this.mOnCancelListener;
                    try {
                        obj = this.mCancellationSignalObj;
                    } catch (Throwable th2) {
                        th = th2;
                        while (true) {
                            try {
                                break;
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        throw th;
                    }
                } else {
                    return;
                }
            } catch (Throwable th4) {
                th = th4;
                while (true) {
                    break;
                }
                throw th;
            }
        }
        if (listener != null) {
            try {
                listener.onCancel();
            } catch (Throwable th5) {
                synchronized (this) {
                    this.mCancelInProgress = false;
                    notifyAll();
                    throw th5;
                }
            }
        }
        if (obj != null && Build.VERSION.SDK_INT >= 16) {
            ((android.os.CancellationSignal) obj).cancel();
        }
        synchronized (this) {
            this.mCancelInProgress = false;
            notifyAll();
        }
    }

    public void setOnCancelListener(OnCancelListener listener) {
        synchronized (this) {
            waitForCancelFinishedLocked();
            if (this.mOnCancelListener != listener) {
                this.mOnCancelListener = listener;
                if (this.mIsCanceled) {
                    if (listener != null) {
                        listener.onCancel();
                    }
                }
            }
        }
    }

    public Object getCancellationSignalObject() {
        Object obj;
        if (Build.VERSION.SDK_INT < 16) {
            return null;
        }
        synchronized (this) {
            if (this.mCancellationSignalObj == null) {
                this.mCancellationSignalObj = new android.os.CancellationSignal();
                if (this.mIsCanceled) {
                    ((android.os.CancellationSignal) this.mCancellationSignalObj).cancel();
                }
            }
            obj = this.mCancellationSignalObj;
        }
        return obj;
    }

    private void waitForCancelFinishedLocked() {
        while (this.mCancelInProgress) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }
}
