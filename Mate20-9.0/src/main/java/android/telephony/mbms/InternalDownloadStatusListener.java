package android.telephony.mbms;

import android.os.Binder;
import android.os.RemoteException;
import android.telephony.mbms.IDownloadStatusListener;
import java.util.concurrent.Executor;

public class InternalDownloadStatusListener extends IDownloadStatusListener.Stub {
    /* access modifiers changed from: private */
    public final DownloadStatusListener mAppListener;
    private final Executor mExecutor;
    private volatile boolean mIsStopped = false;

    public InternalDownloadStatusListener(DownloadStatusListener appCallback, Executor executor) {
        this.mAppListener = appCallback;
        this.mExecutor = executor;
    }

    public void onStatusUpdated(final DownloadRequest request, final FileInfo fileInfo, final int status) throws RemoteException {
        if (!this.mIsStopped) {
            this.mExecutor.execute(new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalDownloadStatusListener.this.mAppListener.onStatusUpdated(request, fileInfo, status);
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
