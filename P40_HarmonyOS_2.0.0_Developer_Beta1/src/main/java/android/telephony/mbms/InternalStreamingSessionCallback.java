package android.telephony.mbms;

import android.os.Binder;
import android.os.RemoteException;
import android.telephony.mbms.IMbmsStreamingSessionCallback;
import java.util.List;
import java.util.concurrent.Executor;

public class InternalStreamingSessionCallback extends IMbmsStreamingSessionCallback.Stub {
    private final MbmsStreamingSessionCallback mAppCallback;
    private final Executor mExecutor;
    private volatile boolean mIsStopped = false;

    public InternalStreamingSessionCallback(MbmsStreamingSessionCallback appCallback, Executor executor) {
        this.mAppCallback = appCallback;
        this.mExecutor = executor;
    }

    @Override // android.telephony.mbms.IMbmsStreamingSessionCallback
    public void onError(final int errorCode, final String message) throws RemoteException {
        if (!this.mIsStopped) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.telephony.mbms.InternalStreamingSessionCallback.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        InternalStreamingSessionCallback.this.mAppCallback.onError(errorCode, message);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    @Override // android.telephony.mbms.IMbmsStreamingSessionCallback
    public void onStreamingServicesUpdated(final List<StreamingServiceInfo> services) throws RemoteException {
        if (!this.mIsStopped) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.telephony.mbms.InternalStreamingSessionCallback.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        InternalStreamingSessionCallback.this.mAppCallback.onStreamingServicesUpdated(services);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    @Override // android.telephony.mbms.IMbmsStreamingSessionCallback
    public void onMiddlewareReady() throws RemoteException {
        if (!this.mIsStopped) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.telephony.mbms.InternalStreamingSessionCallback.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        InternalStreamingSessionCallback.this.mAppCallback.onMiddlewareReady();
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
