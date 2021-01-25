package ohos.miscservices.download;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.download.DownloadConfig;
import ohos.miscservices.download.DownloadSession;
import ohos.miscservices.download.DownloadSessionProxy;
import ohos.rpc.MessageParcel;
import ohos.utils.net.Uri;

public class DownloadSession {
    private static final int CORRECT_DEL_NUMBER = 1;
    public static final int ERROR_CANNOT_RESUME = 506;
    public static final int ERROR_DEVICE_NOT_FOUND = 502;
    public static final int ERROR_FILE_ALREADY_EXISTS = 507;
    public static final int ERROR_FILE_ERROR = 508;
    public static final int ERROR_HTTP_DATA_ERROR = 501;
    public static final int ERROR_INSUFFICIENT_SPACE = 504;
    public static final int ERROR_TOO_MANY_REDIRECTS = 505;
    public static final int ERROR_UNHANDLED_HTTP_CODE = 503;
    public static final int ERROR_UNKNOWN = 509;
    private static final long INVALID_DOWNLOAD_ID = -1;
    public static final int PAUSED_QUEUED_FOR_WIFI = 303;
    public static final int PAUSED_UNKNOWN = 304;
    public static final int PAUSED_WAITING_FOR_NETWORK = 302;
    public static final int PAUSED_WAITING_TO_RETRY = 301;
    public static final int SESSION_FAILED = 16;
    private static final Object SESSION_LOCK = new Object();
    public static final int SESSION_PAUSED = 4;
    public static final int SESSION_PENDING = 1;
    public static final int SESSION_RUNNING = 2;
    public static final int SESSION_SUCCESSFUL = 8;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "DownloadSession");
    private Context context;
    private long currentSessionId;
    private DownloadConfig downloadConfig;
    private final List<IDownloadListener> downloadListeners;
    private DownloadSessionProxy downloadSessionProxy;

    public long restart() {
        return 0;
    }

    public DownloadSession(Context context2, DownloadConfig downloadConfig2) {
        this.currentSessionId = -1;
        this.downloadConfig = null;
        this.downloadListeners = new ArrayList();
        this.context = context2;
        this.downloadConfig = downloadConfig2;
        this.downloadSessionProxy = new DownloadSessionProxy(context2);
    }

    public DownloadSession(Context context2, Uri uri) {
        this(context2, new DownloadConfig.Builder(context2, uri).build());
    }

    public boolean attach(long j) {
        synchronized (SESSION_LOCK) {
            boolean z = false;
            if (this.currentSessionId != -1) {
                return false;
            }
            if (this.downloadSessionProxy.query(j) != null) {
                z = true;
            }
            if (z) {
                this.currentSessionId = j;
            }
            return z;
        }
    }

    public long start() {
        synchronized (SESSION_LOCK) {
            if (this.currentSessionId == -1) {
                if (this.downloadConfig != null) {
                    this.currentSessionId = this.downloadSessionProxy.start(this.downloadConfig);
                    return this.currentSessionId;
                }
            }
            HiLog.error(TAG, "current id is not invalid or req is null!", new Object[0]);
            return -1;
        }
    }

    public boolean remove() {
        long j = this.currentSessionId;
        if (j == -1) {
            HiLog.error(TAG, "id is invalid!", new Object[0]);
            return false;
        }
        boolean z = this.downloadSessionProxy.remove(new long[]{j}) == 1;
        HiLog.debug(TAG, "remove : %{public}ld", Long.valueOf(this.currentSessionId));
        return z;
    }

    public boolean rename(String str) {
        long j = this.currentSessionId;
        if (j != -1) {
            return this.downloadSessionProxy.rename(j, str);
        }
        HiLog.error(TAG, "id is invalid!", new Object[0]);
        return false;
    }

    public boolean pause() {
        long j = this.currentSessionId;
        if (j == -1) {
            HiLog.error(TAG, "id is invalid!", new Object[0]);
            return false;
        }
        HiLog.debug(TAG, "pause : %{public}ld", Long.valueOf(j));
        if (this.downloadSessionProxy.pause(this.currentSessionId) == 1) {
            return true;
        }
        return false;
    }

    public boolean resume() {
        long j = this.currentSessionId;
        if (j == -1) {
            HiLog.error(TAG, "id is invalid!", new Object[0]);
            return false;
        }
        HiLog.debug(TAG, "resume : %{public}ld", Long.valueOf(j));
        if (this.downloadSessionProxy.resume(this.currentSessionId) == 1) {
            return true;
        }
        return false;
    }

    public DownloadInfo query() {
        long j = this.currentSessionId;
        if (j != -1) {
            return this.downloadSessionProxy.query(j);
        }
        HiLog.error(TAG, "id is invalid!", new Object[0]);
        return null;
    }

    public boolean addListener(IDownloadListener iDownloadListener) {
        boolean z;
        if (this.context == null || iDownloadListener == null) {
            HiLog.error(TAG, "context or listener is null!", new Object[0]);
            return false;
        }
        synchronized (this.downloadListeners) {
            if (this.downloadListeners.isEmpty()) {
                final TaskDispatcher mainTaskDispatcher = this.context.getMainTaskDispatcher();
                if (mainTaskDispatcher == null) {
                    HiLog.error(TAG, "dispatcher is invalid!", new Object[0]);
                    return false;
                }
                z = this.downloadSessionProxy.addSessionListener(this.currentSessionId, new DownloadSessionProxy.OnDownloadChangedProbe() {
                    /* class ohos.miscservices.download.DownloadSession.AnonymousClass1 */

                    @Override // ohos.miscservices.download.DownloadSessionProxy.OnDownloadChangedProbe
                    public void onRemoved(long j) {
                        if (j != DownloadSession.this.currentSessionId) {
                            HiLog.error(DownloadSession.TAG, "id is not equal to current id!", new Object[0]);
                            return;
                        }
                        ArrayList arrayList = new ArrayList();
                        synchronized (DownloadSession.this.downloadListeners) {
                            arrayList.addAll(DownloadSession.this.downloadListeners);
                        }
                        mainTaskDispatcher.asyncDispatch(new Runnable(arrayList) {
                            /* class ohos.miscservices.download.$$Lambda$DownloadSession$1$yH1dnqCN1dX8hMVG7PrcGOOzU */
                            private final /* synthetic */ List f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                DownloadSession.AnonymousClass1.lambda$onRemoved$0(this.f$0);
                            }
                        });
                    }

                    static /* synthetic */ void lambda$onRemoved$0(List list) {
                        Iterator it = list.iterator();
                        while (it.hasNext()) {
                            ((IDownloadListener) it.next()).onRemoved();
                        }
                    }

                    @Override // ohos.miscservices.download.DownloadSessionProxy.OnDownloadChangedProbe
                    public void onCompleted(long j) {
                        if (j != DownloadSession.this.currentSessionId) {
                            HiLog.error(DownloadSession.TAG, "id is not equal to current id!", new Object[0]);
                            return;
                        }
                        ArrayList arrayList = new ArrayList();
                        synchronized (DownloadSession.this.downloadListeners) {
                            arrayList.addAll(DownloadSession.this.downloadListeners);
                        }
                        mainTaskDispatcher.asyncDispatch(new Runnable(arrayList) {
                            /* class ohos.miscservices.download.$$Lambda$DownloadSession$1$r46IxMhfdZTrl819RTEvkj296ng */
                            private final /* synthetic */ List f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                DownloadSession.AnonymousClass1.lambda$onCompleted$1(this.f$0);
                            }
                        });
                    }

                    static /* synthetic */ void lambda$onCompleted$1(List list) {
                        Iterator it = list.iterator();
                        while (it.hasNext()) {
                            ((IDownloadListener) it.next()).onCompleted();
                        }
                    }

                    @Override // ohos.miscservices.download.DownloadSessionProxy.OnDownloadChangedProbe
                    public void onFailed(long j, int i) {
                        if (j != DownloadSession.this.currentSessionId) {
                            HiLog.error(DownloadSession.TAG, "id is not equal to current id!", new Object[0]);
                            return;
                        }
                        ArrayList arrayList = new ArrayList();
                        synchronized (DownloadSession.this.downloadListeners) {
                            arrayList.addAll(DownloadSession.this.downloadListeners);
                        }
                        mainTaskDispatcher.asyncDispatch(new Runnable(arrayList, i) {
                            /* class ohos.miscservices.download.$$Lambda$DownloadSession$1$dM8c4Z4_jB7n2q7ahwd3ReoLkk */
                            private final /* synthetic */ List f$0;
                            private final /* synthetic */ int f$1;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                DownloadSession.AnonymousClass1.lambda$onFailed$2(this.f$0, this.f$1);
                            }
                        });
                    }

                    static /* synthetic */ void lambda$onFailed$2(List list, int i) {
                        Iterator it = list.iterator();
                        while (it.hasNext()) {
                            ((IDownloadListener) it.next()).onFailed(i);
                        }
                    }

                    @Override // ohos.miscservices.download.DownloadSessionProxy.OnDownloadChangedProbe
                    public void onPaused(long j) {
                        if (j != DownloadSession.this.currentSessionId) {
                            HiLog.error(DownloadSession.TAG, "id is not equal to current id!", new Object[0]);
                            return;
                        }
                        ArrayList arrayList = new ArrayList();
                        synchronized (DownloadSession.this.downloadListeners) {
                            arrayList.addAll(DownloadSession.this.downloadListeners);
                        }
                        mainTaskDispatcher.asyncDispatch(new Runnable(arrayList) {
                            /* class ohos.miscservices.download.$$Lambda$DownloadSession$1$Zb2it8P_Bn_cHt39uJMLvCUxvzQ */
                            private final /* synthetic */ List f$0;

                            {
                                this.f$0 = r1;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                DownloadSession.AnonymousClass1.lambda$onPaused$3(this.f$0);
                            }
                        });
                    }

                    static /* synthetic */ void lambda$onPaused$3(List list) {
                        Iterator it = list.iterator();
                        while (it.hasNext()) {
                            ((IDownloadListener) it.next()).onPaused();
                        }
                    }

                    @Override // ohos.miscservices.download.DownloadSessionProxy.OnDownloadChangedProbe
                    public void onProgress(long j, long j2, long j3) {
                        if (j != DownloadSession.this.currentSessionId) {
                            HiLog.error(DownloadSession.TAG, "id is not equal to current id!", new Object[0]);
                            return;
                        }
                        ArrayList arrayList = new ArrayList();
                        synchronized (DownloadSession.this.downloadListeners) {
                            arrayList.addAll(DownloadSession.this.downloadListeners);
                        }
                        mainTaskDispatcher.asyncDispatch(new Runnable(arrayList, j2, j3) {
                            /* class ohos.miscservices.download.$$Lambda$DownloadSession$1$oQG3Dc920ZiNZcDOA0IA61ycdjQ */
                            private final /* synthetic */ List f$0;
                            private final /* synthetic */ long f$1;
                            private final /* synthetic */ long f$2;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                                this.f$2 = r4;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                DownloadSession.AnonymousClass1.lambda$onProgress$4(this.f$0, this.f$1, this.f$2);
                            }
                        });
                    }

                    static /* synthetic */ void lambda$onProgress$4(List list, long j, long j2) {
                        Iterator it = list.iterator();
                        while (it.hasNext()) {
                            ((IDownloadListener) it.next()).onProgress(j, j2);
                        }
                    }
                });
            } else {
                z = true;
            }
            if (z) {
                this.downloadListeners.add(iDownloadListener);
                HiLog.debug(TAG, "add listener success, listener size :  %{public}d", Integer.valueOf(this.downloadListeners.size()));
            }
            return z;
        }
    }

    public boolean removeListener(IDownloadListener iDownloadListener) {
        if (iDownloadListener == null) {
            HiLog.error(TAG, "listener is null!", new Object[0]);
            return false;
        }
        synchronized (this.downloadListeners) {
            if (!this.downloadListeners.remove(iDownloadListener)) {
                HiLog.info(TAG, "listener does not registered", new Object[0]);
                return false;
            }
            HiLog.debug(TAG, "remove listener success, listener size : %{public}d", Integer.valueOf(this.downloadListeners.size()));
            if (this.downloadListeners.isEmpty()) {
                this.downloadSessionProxy.removeSessionListener(this.currentSessionId);
            }
            return true;
        }
    }

    public DownloadConfig getConfig() {
        DownloadConfig downloadConfig2;
        synchronized (SESSION_LOCK) {
            downloadConfig2 = this.downloadConfig;
        }
        return downloadConfig2;
    }

    public MessageParcel openDownloadedFile() {
        long j = this.currentSessionId;
        if (j != -1) {
            return this.downloadSessionProxy.openDownloadedFile(j);
        }
        HiLog.error(TAG, "id is invalid!", new Object[0]);
        return null;
    }

    public static class DownloadInfo {
        private String description;
        private long downloadBytes;
        private long downloadId;
        private int failedReason;
        private String fileName;
        private Uri path;
        private int pausedReason;
        private int status;
        private Uri targetUri;
        private String title;
        private long totalBytes;

        public Uri getPath() {
            return this.path;
        }

        public Uri getTargetUri() {
            return this.targetUri;
        }

        public int getStatus() {
            return this.status;
        }

        public int getFailedReason() {
            return this.failedReason;
        }

        public int getPausedReason() {
            return this.pausedReason;
        }

        public long getDownloadId() {
            return this.downloadId;
        }

        public long getDownloadedBytes() {
            return this.downloadBytes;
        }

        public long getTotalBytes() {
            return this.totalBytes;
        }

        public String getDescription() {
            return this.description;
        }

        public String getFileName() {
            return this.fileName;
        }

        public String getTitle() {
            return this.title;
        }

        public void setPath(Uri uri) {
            this.path = uri;
        }

        public void setTargetUri(Uri uri) {
            this.targetUri = uri;
        }

        public void setStatus(int i) {
            this.status = i;
        }

        public void setFailedReason(int i) {
            this.failedReason = i;
        }

        public void setPausedReason(int i) {
            this.pausedReason = i;
        }

        public void setDownloadId(long j) {
            this.downloadId = j;
        }

        public void setDownloadBytes(long j) {
            this.downloadBytes = j;
        }

        public void setTotalBytes(long j) {
            this.totalBytes = j;
        }

        public void setDescription(String str) {
            this.description = str;
        }

        public void setFileName(String str) {
            this.fileName = str;
        }

        public void setTitle(String str) {
            this.title = str;
        }

        public String toString() {
            return "DownloadInfo.title:" + getTitle() + "DownloadInfo.description:" + getDescription() + "DownloadInfo.totalbytes:" + getTotalBytes() + "DownloadInfo.sofarbytes:" + getDownloadedBytes() + "DownloadInfo.status:" + getStatus() + "DownloadInfo.filePath:" + getPath() + "DownloadInfo.downloadUri:" + getTargetUri() + "DownloadInfo.getPausedReason" + getPausedReason() + "DownloadInfo.getErrorCode: " + getFailedReason() + "DownloadInfo.filName:" + getFileName();
        }
    }
}
