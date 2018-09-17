package android.os;

import android.os.ICancellationSignal.Stub;

public final class CancellationSignal {
    private boolean mCancelInProgress;
    private boolean mIsCanceled;
    private OnCancelListener mOnCancelListener;
    private ICancellationSignal mRemote;

    public interface OnCancelListener {
        void onCancel();
    }

    private static final class Transport extends Stub {
        final CancellationSignal mCancellationSignal;

        /* synthetic */ Transport(Transport -this0) {
            this();
        }

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

    /* JADX WARNING: Missing block: B:10:0x0012, code:
            if (r1 == null) goto L_0x0017;
     */
    /* JADX WARNING: Missing block: B:12:?, code:
            r1.onCancel();
     */
    /* JADX WARNING: Missing block: B:13:0x0017, code:
            if (r2 == null) goto L_0x001c;
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            r2.cancel();
     */
    /* JADX WARNING: Missing block: B:30:0x002e, code:
            monitor-enter(r5);
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            r5.mCancelInProgress = false;
            notifyAll();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancel() {
        synchronized (this) {
            if (this.mIsCanceled) {
                return;
            }
            this.mIsCanceled = true;
            this.mCancelInProgress = true;
            OnCancelListener listener = this.mOnCancelListener;
            ICancellationSignal remote = this.mRemote;
        }
        synchronized (this) {
            this.mCancelInProgress = false;
            notifyAll();
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0013, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setOnCancelListener(OnCancelListener listener) {
        synchronized (this) {
            waitForCancelFinishedLocked();
            if (this.mOnCancelListener == listener) {
                return;
            }
            this.mOnCancelListener = listener;
            if (!this.mIsCanceled || listener == null) {
            } else {
                listener.onCancel();
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0013, code:
            return;
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            r3.cancel();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRemote(ICancellationSignal remote) {
        synchronized (this) {
            waitForCancelFinishedLocked();
            if (this.mRemote == remote) {
                return;
            }
            this.mRemote = remote;
            if (!this.mIsCanceled || remote == null) {
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
