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
        OnCancelListener listener;
        ICancellationSignal remote;
        synchronized (this) {
            if (!this.mIsCanceled) {
                this.mIsCanceled = true;
                this.mCancelInProgress = true;
                listener = this.mOnCancelListener;
                remote = this.mRemote;
            } else {
                return;
            }
        }
        if (listener != null) {
            try {
                listener.onCancel();
            } catch (Throwable th) {
                synchronized (this) {
                    this.mCancelInProgress = false;
                    notifyAll();
                    throw th;
                }
            }
        }
        if (remote != null) {
            try {
                remote.cancel();
            } catch (RemoteException e) {
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

    public void setRemote(ICancellationSignal remote) {
        synchronized (this) {
            waitForCancelFinishedLocked();
            if (this.mRemote != remote) {
                this.mRemote = remote;
                if (this.mIsCanceled) {
                    if (remote != null) {
                        try {
                            remote.cancel();
                        } catch (RemoteException e) {
                        }
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

    private static final class Transport extends ICancellationSignal.Stub {
        final CancellationSignal mCancellationSignal;

        private Transport() {
            this.mCancellationSignal = new CancellationSignal();
        }

        @Override // android.os.ICancellationSignal
        public void cancel() throws RemoteException {
            this.mCancellationSignal.cancel();
        }
    }
}
