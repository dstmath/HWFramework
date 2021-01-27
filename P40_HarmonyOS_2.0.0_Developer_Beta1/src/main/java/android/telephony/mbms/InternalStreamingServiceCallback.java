package android.telephony.mbms;

import android.os.Binder;
import android.os.RemoteException;
import android.telephony.mbms.IStreamingServiceCallback;
import java.util.concurrent.Executor;

public class InternalStreamingServiceCallback extends IStreamingServiceCallback.Stub {
    private final StreamingServiceCallback mAppCallback;
    private final Executor mExecutor;
    private volatile boolean mIsStopped = false;

    public InternalStreamingServiceCallback(StreamingServiceCallback appCallback, Executor executor) {
        this.mAppCallback = appCallback;
        this.mExecutor = executor;
    }

    @Override // android.telephony.mbms.IStreamingServiceCallback
    public void onError(final int errorCode, final String message) throws RemoteException {
        if (!this.mIsStopped) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.telephony.mbms.InternalStreamingServiceCallback.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        InternalStreamingServiceCallback.this.mAppCallback.onError(errorCode, message);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    @Override // android.telephony.mbms.IStreamingServiceCallback
    public void onStreamStateUpdated(final int state, final int reason) throws RemoteException {
        if (!this.mIsStopped) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.telephony.mbms.InternalStreamingServiceCallback.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        InternalStreamingServiceCallback.this.mAppCallback.onStreamStateUpdated(state, reason);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    @Override // android.telephony.mbms.IStreamingServiceCallback
    public void onMediaDescriptionUpdated() throws RemoteException {
        if (!this.mIsStopped) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.telephony.mbms.InternalStreamingServiceCallback.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        InternalStreamingServiceCallback.this.mAppCallback.onMediaDescriptionUpdated();
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    @Override // android.telephony.mbms.IStreamingServiceCallback
    public void onBroadcastSignalStrengthUpdated(final int signalStrength) throws RemoteException {
        if (!this.mIsStopped) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.telephony.mbms.InternalStreamingServiceCallback.AnonymousClass4 */

                    @Override // java.lang.Runnable
                    public void run() {
                        InternalStreamingServiceCallback.this.mAppCallback.onBroadcastSignalStrengthUpdated(signalStrength);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    @Override // android.telephony.mbms.IStreamingServiceCallback
    public void onStreamMethodUpdated(final int methodType) throws RemoteException {
        if (!this.mIsStopped) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.telephony.mbms.InternalStreamingServiceCallback.AnonymousClass5 */

                    @Override // java.lang.Runnable
                    public void run() {
                        InternalStreamingServiceCallback.this.mAppCallback.onStreamMethodUpdated(methodType);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public void stop() {
        this.mIsStopped = true;
    }
}
