package android.telephony.mbms;

import android.os.Binder;
import android.telephony.mbms.IMbmsDownloadSessionCallback;
import java.util.List;
import java.util.concurrent.Executor;

public class InternalDownloadSessionCallback extends IMbmsDownloadSessionCallback.Stub {
    private final MbmsDownloadSessionCallback mAppCallback;
    private final Executor mExecutor;
    private volatile boolean mIsStopped = false;

    public InternalDownloadSessionCallback(MbmsDownloadSessionCallback appCallback, Executor executor) {
        this.mAppCallback = appCallback;
        this.mExecutor = executor;
    }

    @Override // android.telephony.mbms.IMbmsDownloadSessionCallback
    public void onError(final int errorCode, final String message) {
        if (!this.mIsStopped) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.telephony.mbms.InternalDownloadSessionCallback.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        InternalDownloadSessionCallback.this.mAppCallback.onError(errorCode, message);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    @Override // android.telephony.mbms.IMbmsDownloadSessionCallback
    public void onFileServicesUpdated(final List<FileServiceInfo> services) {
        if (!this.mIsStopped) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.telephony.mbms.InternalDownloadSessionCallback.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        InternalDownloadSessionCallback.this.mAppCallback.onFileServicesUpdated(services);
                    }
                });
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    @Override // android.telephony.mbms.IMbmsDownloadSessionCallback
    public void onMiddlewareReady() {
        if (!this.mIsStopped) {
            long token = Binder.clearCallingIdentity();
            try {
                this.mExecutor.execute(new Runnable() {
                    /* class android.telephony.mbms.InternalDownloadSessionCallback.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        InternalDownloadSessionCallback.this.mAppCallback.onMiddlewareReady();
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
