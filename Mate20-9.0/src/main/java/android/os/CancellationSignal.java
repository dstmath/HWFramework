package android.os;

import android.os.ICancellationSignal;

public final class CancellationSignal {
    private boolean mCancelInProgress;
    private boolean mIsCanceled;
    private OnCancelListener mOnCancelListener;
    private ICancellationSignal mRemote;

    public interface OnCancelListener {
        void onCancel();
    }

    private static final class Transport extends ICancellationSignal.Stub {
        final CancellationSignal mCancellationSignal;

        private Transport() {
            this.mCancellationSignal = new CancellationSignal();
        }

        public void cancel() throws RemoteException {
            this.mCancellationSignal.cancel();
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r0.onCancel();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0018, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001a, code lost:
        if (r1 == null) goto L_0x002c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r1.cancel();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0020, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r4.mCancelInProgress = false;
        notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0027, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        if (r0 == null) goto L_0x001a;
     */
    public void cancel() {
        synchronized (this) {
            if (!this.mIsCanceled) {
                this.mIsCanceled = true;
                this.mCancelInProgress = true;
                OnCancelListener listener = this.mOnCancelListener;
                ICancellationSignal remote = this.mRemote;
            } else {
                return;
            }
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

    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r2.cancel();
     */
    public void setRemote(ICancellationSignal remote) {
        synchronized (this) {
            waitForCancelFinishedLocked();
            if (this.mRemote != remote) {
                this.mRemote = remote;
                if (this.mIsCanceled) {
                    if (remote == null) {
                    }
                }
            }
        }
    }

    private void waitForCancelFinishedLocked() {
        while (this.mCancelInProgress) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public static ICancellationSignal createTransport() {
        return new Transport();
    }

    public static CancellationSignal fromTransport(ICancellationSignal transport) {
        if (transport instanceof Transport) {
            return ((Transport) transport).mCancellationSignal;
        }
        return null;
    }
}
