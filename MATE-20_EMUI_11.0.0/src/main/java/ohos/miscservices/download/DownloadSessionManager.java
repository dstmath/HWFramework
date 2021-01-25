package ohos.miscservices.download;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.download.DownloadConfig;
import ohos.miscservices.download.DownloadSession;
import ohos.rpc.MessageParcel;
import ohos.utils.net.Uri;

public class DownloadSessionManager {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "DownloadSessionManager");
    private Context context;
    private DownloadSessionProxy downloadSessionProxy;

    public boolean addListener(IDownloadManagerListener iDownloadManagerListener) {
        return true;
    }

    public MessageParcel openDownloadedFile(long j) {
        return null;
    }

    public boolean removedListener(IDownloadManagerListener iDownloadManagerListener) {
        return true;
    }

    public boolean updateDescription(long j, String str, String str2) {
        return false;
    }

    public DownloadSessionManager(Context context2) {
        this.context = context2;
        this.downloadSessionProxy = new DownloadSessionProxy(context2);
    }

    public long start(DownloadConfig downloadConfig) {
        if (downloadConfig != null) {
            return this.downloadSessionProxy.start(downloadConfig);
        }
        HiLog.error(TAG, "req is null!", new Object[0]);
        return -1;
    }

    public long start(Uri uri) {
        return this.downloadSessionProxy.start(new DownloadConfig.Builder(this.context, uri).build());
    }

    public boolean pause(long j) {
        if (j < 0 || j >= Long.MAX_VALUE) {
            HiLog.error(TAG, "id is invalid!", new Object[0]);
            return false;
        }
        HiLog.debug(TAG, "pause : %{public}ld", Long.valueOf(j));
        return this.downloadSessionProxy.pauseByManager(j) == 1;
    }

    public int remove(long... jArr) {
        if (jArr == null) {
            HiLog.error(TAG, "id is null!", new Object[0]);
            return -1;
        }
        HiLog.debug(TAG, "remove size is : %{public}d", Integer.valueOf(jArr.length));
        return this.downloadSessionProxy.remove(jArr);
    }

    public boolean resume(long j) {
        if (j < 0 || j >= Long.MAX_VALUE) {
            HiLog.error(TAG, "id is invalid!", new Object[0]);
            return false;
        }
        HiLog.debug(TAG, "resume : %{public}ld", Long.valueOf(j));
        return this.downloadSessionProxy.resumeByManager(j) == 1;
    }

    public List<DownloadSession.DownloadInfo> batchQueryById(long... jArr) {
        return this.downloadSessionProxy.batchQueryById(jArr);
    }

    public List<DownloadSession.DownloadInfo> batchQueryByStatus(int i) {
        return this.downloadSessionProxy.batchQueryByStatus(i);
    }

    public List<DownloadSession.DownloadInfo> batchQuery(QueryArgs queryArgs) {
        if (queryArgs != null) {
            return this.downloadSessionProxy.batchQuery(queryArgs);
        }
        HiLog.error(TAG, "args is null!", new Object[0]);
        return Collections.emptyList();
    }

    public static class QueryArgs {
        private long[] ids = null;
        private int status = 0;

        public void setIds(long[] jArr) {
            if (jArr == null || jArr.length == 0) {
                this.ids = new long[0];
            } else {
                this.ids = Arrays.copyOf(jArr, jArr.length);
            }
        }

        public void setStatus(int i) {
            this.status = i;
        }

        /* access modifiers changed from: package-private */
        public long[] getQueryIds() {
            long[] jArr = this.ids;
            return (jArr == null || jArr.length == 0) ? new long[0] : Arrays.copyOf(jArr, jArr.length);
        }

        /* access modifiers changed from: package-private */
        public int getQueryStatus() {
            return this.status;
        }
    }

    public Uri getFileUri(long j) {
        return Uri.parse("");
    }

    public static Long getMaxBytesOverDataConnection(Context context2) {
        return DownloadSessionProxy.getMaxBytesOverDevice(context2);
    }

    public static Long getRecommendedMaxBytesOverDataConnection(Context context2) {
        return DownloadSessionProxy.getRecommendedMaxBytesOverDevice(context2);
    }
}
