package android.telephony.mbms;

import android.os.Binder;
import android.os.RemoteException;
import android.telephony.mbms.IMbmsStreamingSessionCallback;
import java.util.List;
import java.util.concurrent.Executor;

public class InternalStreamingSessionCallback extends IMbmsStreamingSessionCallback.Stub {
    /* access modifiers changed from: private */
    public final MbmsStreamingSessionCallback mAppCallback;
    private final Executor mExecutor;
    private volatile boolean mIsStopped = false;

    public InternalStreamingSessionCallback(MbmsStreamingSessionCallback appCallback, Executor executor) {
        this.mAppCallback = appCallback;
        this.mExecutor = executor;
    }

    public void onError(final int errorCode, final String message) throws RemoteException {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalStreamingSessionCallback.this.mAppCallback.onError(errorCode, message);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            });
        }
    }

    public void onStreamingServicesUpdated(final List<StreamingServiceInfo> services) throws RemoteException {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalStreamingSessionCallback.this.mAppCallback.onStreamingServicesUpdated(services);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            });
        }
    }

    public void onMiddlewareReady() throws RemoteException {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalStreamingSessionCallback.this.mAppCallback.onMiddlewareReady();
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
