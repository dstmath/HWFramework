package ohos.miscservices.download;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.system.ErrnoException;
import android.text.TextUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import libcore.io.Libcore;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.photokit.metadata.AVStorage;
import ohos.miscservices.download.DownloadSession;
import ohos.miscservices.download.DownloadSessionManager;
import ohos.net.UriConverter;
import ohos.rpc.MessageParcel;
import ohos.utils.Environment;

public class DownloadSessionProxy {
    static final int SESSION_REMOVED = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "DownloadSessionProxy");
    private final Uri allUri = Uri.parse("content://downloads/all_downloads");
    private final Uri baseUri = Uri.parse("content://downloads/my_downloads");
    private Context context;
    private DownloadManager downloadManager;
    private Map<Long, ContentObserver> downloadSessionObserverMap = new HashMap();
    private ContentResolver resolver;
    private final Object sessionLock = new Object();
    private ohos.app.Context zosContext;

    public interface OnDownloadChangedProbe {
        void onCompleted(long j);

        void onFailed(long j, int i);

        void onPaused(long j);

        void onProgress(long j, long j2, long j3);

        void onRemoved(long j);
    }

    public boolean addAllSessionsListener(OnDownloadChangedProbe onDownloadChangedProbe) {
        return true;
    }

    public String getMimeTypeForDownloadedFile(long j) {
        return "";
    }

    public boolean removeAllSessionsListener() {
        return true;
    }

    public DownloadSessionProxy(ohos.app.Context context2) {
        this.zosContext = context2;
        this.context = DownloadUtils.getAPlatformContext(context2);
        Context context3 = this.context;
        if (context3 != null) {
            this.resolver = context3.getContentResolver();
            Object systemService = this.context.getSystemService("download");
            if (systemService instanceof DownloadManager) {
                this.downloadManager = (DownloadManager) systemService;
            }
        }
    }

    /* access modifiers changed from: private */
    public class DownloadChangedObserver extends ContentObserver {
        int prevStatus;
        OnDownloadChangedProbe probe;
        long sessionId;

        public DownloadChangedObserver(long j, int i, OnDownloadChangedProbe onDownloadChangedProbe) {
            super(DownloadUtils.getAsyncHandler());
            this.probe = onDownloadChangedProbe;
            this.sessionId = j;
            this.prevStatus = i;
        }

        /* JADX WARNING: Removed duplicated region for block: B:52:0x011a A[DONT_GENERATE] */
        /* JADX WARNING: Removed duplicated region for block: B:61:? A[RETURN, SYNTHETIC] */
        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (uri == null) {
                HiLog.error(DownloadSessionProxy.TAG, "uri is null!", new Object[0]);
                return;
            }
            try {
                this.sessionId = Long.parseLong(uri.getPathSegments().get(1));
            } catch (NumberFormatException unused) {
                HiLog.error(DownloadSessionProxy.TAG, "parse id NumberFormatException!", new Object[0]);
                return;
            } catch (IndexOutOfBoundsException unused2) {
                HiLog.error(DownloadSessionProxy.TAG, "parse id null!", new Object[0]);
                if (uri.toString().equals(DownloadSessionProxy.this.allUri.toString())) {
                    return;
                }
            }
            if (this.probe != null && DownloadSessionProxy.this.downloadManager != null) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(this.sessionId);
                Cursor cursor = null;
                try {
                    cursor = DownloadSessionProxy.this.downloadManager.query(query);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            int i = cursor.getInt(cursor.getColumnIndex("status"));
                            if (this.prevStatus == i && i != 2) {
                                cursor.close();
                                return;
                            } else if (i == 2) {
                                long j = cursor.getLong(cursor.getColumnIndexOrThrow("bytes_so_far"));
                                long j2 = cursor.getLong(cursor.getColumnIndexOrThrow("total_size"));
                                if (j <= 0 || j2 <= 0) {
                                    HiLog.error(DownloadSessionProxy.TAG, "meet error during query downloaded file size!", new Object[0]);
                                    cursor.close();
                                    return;
                                }
                                this.probe.onProgress(this.sessionId, j, j2);
                                this.prevStatus = i;
                                cursor.close();
                                return;
                            } else {
                                if (this.prevStatus != i) {
                                    if (i == 4) {
                                        this.probe.onPaused(this.sessionId);
                                        this.prevStatus = i;
                                    } else if (i == 8) {
                                        this.probe.onCompleted(this.sessionId);
                                        this.prevStatus = i;
                                    } else if (i == 16) {
                                        this.probe.onFailed(this.sessionId, cursor.getColumnIndex("reason"));
                                        this.prevStatus = i;
                                    }
                                }
                                if (cursor != null) {
                                    return;
                                }
                                return;
                            }
                        }
                    }
                    HiLog.info(DownloadSessionProxy.TAG, "prevStatus is %d", Integer.valueOf(this.prevStatus));
                    if (this.prevStatus != 0) {
                        this.probe.onRemoved(this.sessionId);
                        this.prevStatus = 0;
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    public boolean addSessionListener(long j, OnDownloadChangedProbe onDownloadChangedProbe) {
        if (this.downloadManager == null || this.resolver == null) {
            return false;
        }
        synchronized (this.sessionLock) {
            Cursor cursor = null;
            try {
                cursor = this.downloadManager.query(new DownloadManager.Query().setFilterById(j));
                if (cursor == null || !cursor.moveToFirst()) {
                    HiLog.error(TAG, "addSessionListener error, download does not exsist!", new Object[0]);
                    return false;
                }
                int i = cursor.getInt(cursor.getColumnIndex("status"));
                if (i == 4) {
                    onDownloadChangedProbe.onPaused(j);
                } else if (i == 8) {
                    onDownloadChangedProbe.onCompleted(j);
                } else if (i == 16) {
                    onDownloadChangedProbe.onFailed(j, cursor.getColumnIndex("reason"));
                }
                cursor.close();
                DownloadChangedObserver downloadChangedObserver = new DownloadChangedObserver(j, i, onDownloadChangedProbe);
                this.resolver.registerContentObserver(ContentUris.withAppendedId(this.baseUri, j), true, downloadChangedObserver);
                this.downloadSessionObserverMap.put(Long.valueOf(j), downloadChangedObserver);
                HiLog.debug(TAG, "addSessionListener success!", new Object[0]);
                return true;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public void removeSessionListener(long j) {
        if (this.resolver != null) {
            synchronized (this.sessionLock) {
                ContentObserver contentObserver = this.downloadSessionObserverMap.get(Long.valueOf(j));
                if (contentObserver == null) {
                    HiLog.error(TAG, "register listener is null!", new Object[0]);
                    return;
                }
                this.resolver.unregisterContentObserver(contentObserver);
                this.downloadSessionObserverMap.remove(Long.valueOf(j));
                HiLog.debug(TAG, "removeSessionListener success!", new Object[0]);
            }
        }
    }

    public long start(DownloadConfig downloadConfig) {
        if (this.downloadManager == null) {
            return -1;
        }
        return this.downloadManager.enqueue(buildRequest(downloadConfig));
    }

    private DownloadManager.Request buildRequest(DownloadConfig downloadConfig) {
        ohos.utils.net.Uri uri;
        DownloadManager.Request request = new DownloadManager.Request(UriConverter.convertToAndroidUri(downloadConfig.getDownloadUri()));
        int i = 1;
        if (downloadConfig.getDownloadStoragePath() != null) {
            uri = downloadConfig.getDownloadStoragePath();
        } else {
            String uri2 = downloadConfig.getDownloadUri().toString();
            uri = DownloadUtils.createDownloadPathInPrivateDir(this.zosContext, Environment.DIRECTORY_DOWNLOAD, uri2.length() > 0 ? uri2.substring(uri2.lastIndexOf("/") + 1) : "");
        }
        request.setDestinationUri(UriConverter.convertToAndroidUri(uri));
        request.setTitle(downloadConfig.getTitle());
        request.setDescription(downloadConfig.getDescription());
        request.setAllowedNetworkTypes(downloadConfig.getNetworkRestriction());
        request.setAllowedOverRoaming(downloadConfig.isRoamingAllowed());
        request.setAllowedOverMetered(downloadConfig.isMeteredAllowed());
        request.setRequiresCharging(downloadConfig.isRequiresCharging());
        if (!downloadConfig.isShowNotify()) {
            i = 2;
        }
        request.setNotificationVisibility(i);
        request.setRequiresDeviceIdle(downloadConfig.isDownloadInIdle());
        for (Map.Entry<String, String> entry : downloadConfig.getHttpHeaders().entrySet()) {
            request.addRequestHeader(entry.getKey(), entry.getValue());
        }
        return request;
    }

    public MessageParcel openDownloadedFile(long j) throws FileNotFoundException {
        DownloadManager downloadManager2 = this.downloadManager;
        MessageParcel messageParcel = null;
        if (downloadManager2 == null) {
            return null;
        }
        ParcelFileDescriptor openDownloadedFile = downloadManager2.openDownloadedFile(j);
        if (openDownloadedFile == null) {
            HiLog.info(TAG, "get the ParcelFileDescriptor is null ,return", new Object[0]);
            return null;
        }
        try {
            FileDescriptor dup = Libcore.os.dup(openDownloadedFile.getFileDescriptor());
            messageParcel = MessageParcel.obtain();
            messageParcel.writeFileDescriptor(dup);
        } catch (ErrnoException unused) {
            HiLog.info(TAG, "dup FileDescriptor errno exception", new Object[0]);
        } catch (Throwable th) {
            try {
                openDownloadedFile.close();
            } catch (IOException unused2) {
                HiLog.info(TAG, "close FileDescriptor exception", new Object[0]);
            }
            throw th;
        }
        try {
            openDownloadedFile.close();
        } catch (IOException unused3) {
            HiLog.info(TAG, "close FileDescriptor exception", new Object[0]);
        }
        return messageParcel;
    }

    public int remove(long... jArr) {
        DownloadManager downloadManager2 = this.downloadManager;
        if (downloadManager2 == null) {
            return 0;
        }
        return downloadManager2.remove(jArr);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ee, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ef, code lost:
        if (r0 != null) goto L_0x00f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00f5, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f6, code lost:
        r7.addSuppressed(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f9, code lost:
        throw r8;
     */
    public boolean rename(long j, String str) {
        if (this.downloadManager == null || this.resolver == null) {
            return false;
        }
        if (!FileUtils.isValidFatFilename(str)) {
            HiLog.error(TAG, "It is not a valid filename", new Object[0]);
            return false;
        }
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(j);
        Cursor query2 = this.downloadManager.query(query);
        if (query2 == null) {
            throw new IllegalStateException("Missing cursor for download id=" + j);
        } else if (!query2.moveToFirst()) {
            throw new IllegalStateException("Missing download id=" + j);
        } else if (getInt(query2, "status").intValue() == 8) {
            String string = getString(query2, "local_uri");
            if (string != null) {
                String replace = string.replace("file://", "");
                if (replace == null) {
                    throw new IllegalStateException("Download doesn't have a valid file path");
                } else if (new File(replace).exists()) {
                    HiLog.debug(TAG, "occurred other occasions", new Object[0]);
                    query2.close();
                    File file = new File(new File(replace).getParentFile(), str);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("title", str);
                    contentValues.put(AVStorage.AVBaseColumns.DATA, file.toString());
                    if (this.resolver.update(ContentUris.withAppendedId(this.baseUri, j), contentValues, null, null) == 1) {
                        return true;
                    }
                    return false;
                } else {
                    throw new IllegalStateException("Downloaded file doesn't exist anymore");
                }
            } else {
                throw new IllegalStateException("Download doesn't have a valid file path");
            }
        } else {
            throw new IllegalStateException("Download is not completed yet");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0036  */
    public DownloadSession.DownloadInfo query(long j) {
        Cursor cursor;
        Throwable th;
        if (this.downloadManager == null) {
            return null;
        }
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(j);
        try {
            cursor = this.downloadManager.query(query);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        DownloadSession.DownloadInfo fromCursorToDownload = fromCursorToDownload(cursor);
                        cursor.close();
                        return fromCursorToDownload;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (cursor != null) {
                    }
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (Throwable th3) {
            th = th3;
            cursor = null;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public String queryMimeType(long j) {
        DownloadManager downloadManager2 = this.downloadManager;
        if (downloadManager2 == null) {
            return null;
        }
        return downloadManager2.getMimeTypeForDownloadedFile(j);
    }

    public List<DownloadSession.DownloadInfo> batchQueryByStatus(int i) {
        ArrayList arrayList = new ArrayList();
        if (this.downloadManager == null) {
            return arrayList;
        }
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(i);
        Cursor cursor = null;
        try {
            cursor = this.downloadManager.query(query);
            if (cursor == null) {
                HiLog.info(TAG, "cursor is null,just return", new Object[0]);
                return arrayList;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DownloadSession.DownloadInfo fromCursorToDownload = fromCursorToDownload(cursor);
                if (fromCursorToDownload != null) {
                    arrayList.add(fromCursorToDownload);
                }
                cursor.moveToNext();
            }
            cursor.close();
            return arrayList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<DownloadSession.DownloadInfo> batchQuery(DownloadSessionManager.QueryArgs queryArgs) {
        ArrayList arrayList = new ArrayList();
        if (this.downloadManager == null) {
            return arrayList;
        }
        long[] queryIds = queryArgs.getQueryIds();
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(queryArgs.getQueryStatus());
        query.setFilterById(queryIds);
        Cursor cursor = null;
        try {
            cursor = this.downloadManager.query(query);
            if (cursor == null) {
                HiLog.info(TAG, "cursor is null,just return", new Object[0]);
                return arrayList;
            }
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DownloadSession.DownloadInfo fromCursorToDownload = fromCursorToDownload(cursor);
                if (fromCursorToDownload != null) {
                    arrayList.add(fromCursorToDownload);
                }
                cursor.moveToNext();
            }
            cursor.close();
            return arrayList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<DownloadSession.DownloadInfo> batchQueryById(long... jArr) {
        ArrayList arrayList = new ArrayList();
        for (long j : jArr) {
            DownloadSession.DownloadInfo query = query(j);
            if (query != null) {
                arrayList.add(query);
            }
        }
        return arrayList;
    }

    private DownloadSession.DownloadInfo fromCursorToDownload(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        DownloadSession.DownloadInfo downloadInfo = new DownloadSession.DownloadInfo();
        downloadInfo.setDownloadId(getLong(cursor, "_id").longValue());
        downloadInfo.setTitle(getString(cursor, "title"));
        downloadInfo.setTotalBytes(getLong(cursor, "total_size").longValue());
        downloadInfo.setDownloadBytes(getLong(cursor, "bytes_so_far").longValue());
        String string = getString(cursor, "hint");
        if (string != null) {
            String[] split = string.split("/");
            downloadInfo.setFileName(split.length > 0 ? split[split.length - 1] : "");
            downloadInfo.setPath(UriConverter.convertToZidaneUri(Uri.parse(string)));
        }
        String string2 = getString(cursor, "uri");
        if (string2 != null) {
            downloadInfo.setTargetUri(UriConverter.convertToZidaneUri(Uri.parse(string2)));
        }
        int intValue = getInt(cursor, "status").intValue();
        long j = cursor.getLong(cursor.getColumnIndexOrThrow("reason"));
        if ((intValue & 4) != 0) {
            downloadInfo.setPausedReason(DownloadUtils.getReasonCode(j));
        }
        if ((intValue & 16) != 0) {
            downloadInfo.setFailedReason(DownloadUtils.getReasonCode(j));
        }
        downloadInfo.setStatus(intValue);
        downloadInfo.setDescription(getString(cursor, "description"));
        return downloadInfo;
    }

    public static Long getMaxBytesOverDevice(ohos.app.Context context2) {
        Context aPlatformContext = DownloadUtils.getAPlatformContext(context2);
        if (aPlatformContext == null) {
            return null;
        }
        try {
            return Long.valueOf(Settings.Global.getLong(aPlatformContext.getContentResolver(), "download_manager_max_bytes_over_mobile"));
        } catch (Settings.SettingNotFoundException unused) {
            return null;
        }
    }

    public static Long getRecommendedMaxBytesOverDevice(ohos.app.Context context2) {
        Context aPlatformContext = DownloadUtils.getAPlatformContext(context2);
        if (aPlatformContext == null) {
            return null;
        }
        try {
            return Long.valueOf(Settings.Global.getLong(aPlatformContext.getContentResolver(), "download_manager_recommended_max_bytes_over_mobile"));
        } catch (Settings.SettingNotFoundException unused) {
            return null;
        }
    }

    public int pause(long j) {
        if (this.resolver == null) {
            return 0;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("control", (Integer) 1);
        HiLog.debug(TAG, "pause : %{public}ld", Long.valueOf(j));
        return this.resolver.update(ContentUris.withAppendedId(this.baseUri, j), contentValues, null, null);
    }

    public int pauseByManager(long j) {
        if (this.resolver == null) {
            return 0;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("control", (Integer) 1);
        HiLog.debug(TAG, "pause by manager, id : %{public}ld", Long.valueOf(j));
        return this.resolver.update(ContentUris.withAppendedId(this.allUri, j), contentValues, null, null);
    }

    public int resume(long j) {
        if (this.resolver == null) {
            return 0;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("control", (Integer) 0);
        HiLog.debug(TAG, "resume : %{public}ld", Long.valueOf(j));
        return this.resolver.update(ContentUris.withAppendedId(this.baseUri, j), contentValues, null, null);
    }

    public int resumeByManager(long j) {
        if (this.resolver == null) {
            return 0;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("control", (Integer) 0);
        HiLog.debug(TAG, "resume by manager, id : %{public}ld", Long.valueOf(j));
        return this.resolver.update(ContentUris.withAppendedId(this.allUri, j), contentValues, null, null);
    }

    private Long getLong(Cursor cursor, String str) {
        return Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(str)));
    }

    private String getString(Cursor cursor, String str) {
        String string = cursor.getString(cursor.getColumnIndexOrThrow(str));
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        return string;
    }

    private Integer getInt(Cursor cursor, String str) {
        return Integer.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(str)));
    }
}
