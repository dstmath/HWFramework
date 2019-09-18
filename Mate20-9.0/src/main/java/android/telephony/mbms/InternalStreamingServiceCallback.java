package android.telephony.mbms;

import android.os.Binder;
import android.os.RemoteException;
import android.telephony.mbms.IStreamingServiceCallback;
import java.util.concurrent.Executor;

public class InternalStreamingServiceCallback extends IStreamingServiceCallback.Stub {
    /* access modifiers changed from: private */
    public final StreamingServiceCallback mAppCallback;
    private final Executor mExecutor;
    private volatile boolean mIsStopped = false;

    public InternalStreamingServiceCallback(StreamingServiceCallback appCallback, Executor executor) {
        this.mAppCallback = appCallback;
        this.mExecutor = executor;
    }

    public void onError(final int errorCode, final String message) throws RemoteException {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalStreamingServiceCallback.this.mAppCallback.onError(errorCode, message);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            });
        }
    }

    public void onStreamStateUpdated(final int state, final int reason) throws RemoteException {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalStreamingServiceCallback.this.mAppCallback.onStreamStateUpdated(state, reason);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            });
        }
    }

    public void onMediaDescriptionUpdated() throws RemoteException {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalStreamingServiceCallback.this.mAppCallback.onMediaDescriptionUpdated();
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            });
        }
    }

    public void onBroadcastSignalStrengthUpdated(final int signalStrength) throws RemoteException {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalStreamingServiceCallback.this.mAppCallback.onBroadcastSignalStrengthUpdated(signalStrength);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            });
        }
    }

    public void onStreamMethodUpdated(final int methodType) throws RemoteException {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalStreamingServiceCallback.this.mAppCallback.onStreamMethodUpdated(methodType);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            });
        }
    }

    public void stop() {
        this.mIsStopped = true;
    }
}
