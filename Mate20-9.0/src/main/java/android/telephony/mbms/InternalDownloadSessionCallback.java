package android.telephony.mbms;

import android.os.Binder;
import android.telephony.mbms.IMbmsDownloadSessionCallback;
import java.util.List;
import java.util.concurrent.Executor;

public class InternalDownloadSessionCallback extends IMbmsDownloadSessionCallback.Stub {
    /* access modifiers changed from: private */
    public final MbmsDownloadSessionCallback mAppCallback;
    private final Executor mExecutor;
    private volatile boolean mIsStopped = false;

    public InternalDownloadSessionCallback(MbmsDownloadSessionCallback appCallback, Executor executor) {
        this.mAppCallback = appCallback;
        this.mExecutor = executor;
    }

    public void onError(final int errorCode, final String message) {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalDownloadSessionCallback.this.mAppCallback.onError(errorCode, message);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            });
        }
    }

    public void onFileServicesUpdated(final List<FileServiceInfo> services) {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalDownloadSessionCallback.this.mAppCallback.onFileServicesUpdated(services);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            });
        }
    }

    public void onMiddlewareReady() {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalDownloadSessionCallback.this.mAppCallback.onMiddlewareReady();
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
