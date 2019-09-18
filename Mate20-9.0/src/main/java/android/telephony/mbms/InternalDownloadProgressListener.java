package android.telephony.mbms;

import android.os.Binder;
import android.os.RemoteException;
import android.telephony.mbms.IDownloadProgressListener;
import java.util.concurrent.Executor;

public class InternalDownloadProgressListener extends IDownloadProgressListener.Stub {
    /* access modifiers changed from: private */
    public final DownloadProgressListener mAppListener;
    private final Executor mExecutor;
    private volatile boolean mIsStopped = false;

    public InternalDownloadProgressListener(DownloadProgressListener appListener, Executor executor) {
        this.mAppListener = appListener;
        this.mExecutor = executor;
    }

    public void onProgressUpdated(DownloadRequest request, FileInfo fileInfo, int currentDownloadSize, int fullDownloadSize, int currentDecodedSize, int fullDecodedSize) throws RemoteException {
        if (!this.mIsStopped) {
            Executor executor = this.mExecutor;
            final DownloadRequest downloadRequest = request;
            final FileInfo fileInfo2 = fileInfo;
            final int i = currentDownloadSize;
            final int i2 = fullDownloadSize;
            final int i3 = currentDecodedSize;
            final int i4 = fullDecodedSize;
            AnonymousClass1 r0 = new Runnable() {
                public void run() {
                    long token = Binder.clearCallingIdentity();
                    try {
                        InternalDownloadProgressListener.this.mAppListener.onProgressUpdated(downloadRequest, fileInfo2, i, i2, i3, i4);
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            };
            executor.execute(r0);
        }
    }

    public void stop() {
        this.mIsStopped = true;
    }
}
