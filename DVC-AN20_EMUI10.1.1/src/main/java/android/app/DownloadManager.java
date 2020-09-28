package android.app;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.Downloads;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final String ACTION_DOWNLOAD_COMPLETE = "android.intent.action.DOWNLOAD_COMPLETE";
    @SystemApi
    public static final String ACTION_DOWNLOAD_COMPLETED = "android.intent.action.DOWNLOAD_COMPLETED";
    public static final String ACTION_NOTIFICATION_CLICKED = "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";
    public static final String ACTION_VIEW_DOWNLOADS = "android.intent.action.VIEW_DOWNLOADS";
    public static final String COLUMN_ALLOW_WRITE = "allow_write";
    public static final String COLUMN_BYTES_DOWNLOADED_SO_FAR = "bytes_so_far";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DESTINATION = "destination";
    public static final String COLUMN_FILE_NAME_HINT = "hint";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LAST_MODIFIED_TIMESTAMP = "last_modified_timestamp";
    @Deprecated
    public static final String COLUMN_LOCAL_FILENAME = "local_filename";
    public static final String COLUMN_LOCAL_URI = "local_uri";
    public static final String COLUMN_MEDIAPROVIDER_URI = "mediaprovider_uri";
    public static final String COLUMN_MEDIASTORE_URI = "mediastore_uri";
    public static final String COLUMN_MEDIA_TYPE = "media_type";
    public static final String COLUMN_REASON = "reason";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TOTAL_SIZE_BYTES = "total_size";
    public static final String COLUMN_URI = "uri";
    public static final int ERROR_BLOCKED = 1010;
    public static final int ERROR_CANNOT_RESUME = 1008;
    public static final int ERROR_DEVICE_NOT_FOUND = 1007;
    public static final int ERROR_FILE_ALREADY_EXISTS = 1009;
    public static final int ERROR_FILE_ERROR = 1001;
    public static final int ERROR_HTTP_DATA_ERROR = 1004;
    public static final int ERROR_INSUFFICIENT_SPACE = 1006;
    public static final int ERROR_TOO_MANY_REDIRECTS = 1005;
    public static final int ERROR_UNHANDLED_HTTP_CODE = 1002;
    public static final int ERROR_UNKNOWN = 1000;
    public static final String EXTRA_DOWNLOAD_ID = "extra_download_id";
    public static final String EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS = "extra_click_download_ids";
    public static final String INTENT_EXTRAS_SORT_BY_SIZE = "android.app.DownloadManager.extra_sortBySize";
    private static final String NON_DOWNLOADMANAGER_DOWNLOAD = "non-dwnldmngr-download-dont-retry2download";
    public static final int PAUSED_QUEUED_FOR_WIFI = 3;
    public static final int PAUSED_UNKNOWN = 4;
    public static final int PAUSED_WAITING_FOR_NETWORK = 2;
    public static final int PAUSED_WAITING_TO_RETRY = 1;
    public static final int STATUS_FAILED = 16;
    public static final int STATUS_PAUSED = 4;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_RUNNING = 2;
    public static final int STATUS_SUCCESSFUL = 8;
    @UnsupportedAppUsage
    public static final String[] UNDERLYING_COLUMNS = {"_id", COLUMN_LOCAL_FILENAME, "mediaprovider_uri", "destination", "title", "description", "uri", "status", "hint", "media_type", COLUMN_TOTAL_SIZE_BYTES, COLUMN_LAST_MODIFIED_TIMESTAMP, COLUMN_BYTES_DOWNLOADED_SO_FAR, "allow_write", COLUMN_LOCAL_URI, "reason"};
    private boolean mAccessFilename;
    private Uri mBaseUri = Downloads.Impl.CONTENT_URI;
    private final String mPackageName;
    private final ContentResolver mResolver;

    public static class Request {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        @Deprecated
        public static final int NETWORK_BLUETOOTH = 4;
        public static final int NETWORK_MOBILE = 1;
        public static final int NETWORK_WIFI = 2;
        private static final int SCANNABLE_VALUE_NO = 2;
        private static final int SCANNABLE_VALUE_YES = 0;
        public static final int VISIBILITY_HIDDEN = 2;
        public static final int VISIBILITY_VISIBLE = 0;
        public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;
        public static final int VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION = 3;
        private int mAllowedNetworkTypes = -1;
        private CharSequence mDescription;
        private Uri mDestinationUri;
        private int mFlags = 0;
        private boolean mIsVisibleInDownloadsUi = true;
        private boolean mMeteredAllowed = true;
        private String mMimeType;
        private int mNotificationVisibility = 0;
        private List<Pair<String, String>> mRequestHeaders = new ArrayList();
        private boolean mRoamingAllowed = true;
        private boolean mScannable = false;
        private CharSequence mTitle;
        @UnsupportedAppUsage
        private Uri mUri;

        public Request(Uri uri) {
            if (uri != null) {
                String scheme = uri.getScheme();
                if (scheme == null || (!scheme.equals(IntentFilter.SCHEME_HTTP) && !scheme.equals(IntentFilter.SCHEME_HTTPS))) {
                    throw new IllegalArgumentException("Can only download HTTP/HTTPS URIs: " + uri);
                }
                this.mUri = uri;
                return;
            }
            throw new NullPointerException();
        }

        Request(String uriString) {
            this.mUri = Uri.parse(uriString);
        }

        public Request setDestinationUri(Uri uri) {
            this.mDestinationUri = uri;
            return this;
        }

        public Request setDestinationInExternalFilesDir(Context context, String dirType, String subPath) {
            File file = context.getExternalFilesDir(dirType);
            if (file != null) {
                if (file.exists()) {
                    if (!file.isDirectory()) {
                        throw new IllegalStateException(file.getAbsolutePath() + " already exists and is not a directory");
                    }
                } else if (!file.mkdirs()) {
                    throw new IllegalStateException("Unable to create directory: " + file.getAbsolutePath());
                }
                setDestinationFromBase(file, subPath);
                return this;
            }
            throw new IllegalStateException("Failed to get external storage files directory");
        }

        /* JADX WARNING: Code restructure failed: missing block: B:26:0x008b, code lost:
            r5 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x008c, code lost:
            if (r2 != null) goto L_0x008e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
            r2.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x0092, code lost:
            r6 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x0093, code lost:
            r3.addSuppressed(r6);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0096, code lost:
            throw r5;
         */
        public Request setDestinationInExternalPublicDir(String dirType, String subPath) {
            File file = Environment.getExternalStoragePublicDirectory(dirType);
            if (file != null) {
                Context context = AppGlobals.getInitialApplication();
                if (context.getApplicationInfo().targetSdkVersion >= 29 || !Environment.isExternalStorageLegacy()) {
                    try {
                        ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(Downloads.Impl.AUTHORITY);
                        Bundle extras = new Bundle();
                        extras.putString(Downloads.DIR_TYPE, dirType);
                        client.call(Downloads.CALL_CREATE_EXTERNAL_PUBLIC_DIR, null, extras);
                        client.close();
                    } catch (RemoteException e) {
                        throw new IllegalStateException("Unable to create directory: " + file.getAbsolutePath());
                    }
                } else if (file.exists()) {
                    if (!file.isDirectory()) {
                        throw new IllegalStateException(file.getAbsolutePath() + " already exists and is not a directory");
                    }
                } else if (!file.mkdirs()) {
                    throw new IllegalStateException("Unable to create directory: " + file.getAbsolutePath());
                }
                setDestinationFromBase(file, subPath);
                return this;
            }
            throw new IllegalStateException("Failed to get external storage public directory");
        }

        private void setDestinationFromBase(File base, String subPath) {
            if (subPath != null) {
                this.mDestinationUri = Uri.withAppendedPath(Uri.fromFile(base), subPath);
                return;
            }
            throw new NullPointerException("subPath cannot be null");
        }

        @Deprecated
        public void allowScanningByMediaScanner() {
            this.mScannable = true;
        }

        public Request addRequestHeader(String header, String value) {
            if (header == null) {
                throw new NullPointerException("header cannot be null");
            } else if (!header.contains(SettingsStringUtil.DELIMITER)) {
                if (value == null) {
                    value = "";
                }
                this.mRequestHeaders.add(Pair.create(header, value));
                return this;
            } else {
                throw new IllegalArgumentException("header may not contain ':'");
            }
        }

        public Request setTitle(CharSequence title) {
            this.mTitle = title;
            return this;
        }

        public Request setDescription(CharSequence description) {
            this.mDescription = description;
            return this;
        }

        public Request setMimeType(String mimeType) {
            this.mMimeType = mimeType;
            return this;
        }

        @Deprecated
        public Request setShowRunningNotification(boolean show) {
            if (show) {
                return setNotificationVisibility(0);
            }
            return setNotificationVisibility(2);
        }

        public Request setNotificationVisibility(int visibility) {
            this.mNotificationVisibility = visibility;
            return this;
        }

        public Request setAllowedNetworkTypes(int flags) {
            this.mAllowedNetworkTypes = flags;
            return this;
        }

        public Request setAllowedOverRoaming(boolean allowed) {
            this.mRoamingAllowed = allowed;
            return this;
        }

        public Request setAllowedOverMetered(boolean allow) {
            this.mMeteredAllowed = allow;
            return this;
        }

        public Request setRequiresCharging(boolean requiresCharging) {
            if (requiresCharging) {
                this.mFlags |= 1;
            } else {
                this.mFlags &= -2;
            }
            return this;
        }

        public Request setRequiresDeviceIdle(boolean requiresDeviceIdle) {
            if (requiresDeviceIdle) {
                this.mFlags |= 2;
            } else {
                this.mFlags &= -3;
            }
            return this;
        }

        @Deprecated
        public Request setVisibleInDownloadsUi(boolean isVisible) {
            this.mIsVisibleInDownloadsUi = isVisible;
            return this;
        }

        /* access modifiers changed from: package-private */
        public ContentValues toContentValues(String packageName) {
            ContentValues values = new ContentValues();
            values.put("uri", this.mUri.toString());
            values.put(Downloads.Impl.COLUMN_IS_PUBLIC_API, (Boolean) true);
            values.put(Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE, packageName);
            int i = 2;
            if (this.mDestinationUri != null) {
                values.put("destination", (Integer) 4);
                values.put("hint", this.mDestinationUri.toString());
            } else {
                values.put("destination", (Integer) 2);
            }
            if (this.mScannable) {
                i = 0;
            }
            values.put(Downloads.Impl.COLUMN_MEDIA_SCANNED, Integer.valueOf(i));
            if (!this.mRequestHeaders.isEmpty()) {
                encodeHttpHeaders(values);
            }
            putIfNonNull(values, "title", this.mTitle);
            putIfNonNull(values, "description", this.mDescription);
            putIfNonNull(values, "mimetype", this.mMimeType);
            values.put(Downloads.Impl.COLUMN_VISIBILITY, Integer.valueOf(this.mNotificationVisibility));
            values.put(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES, Integer.valueOf(this.mAllowedNetworkTypes));
            values.put(Downloads.Impl.COLUMN_ALLOW_ROAMING, Boolean.valueOf(this.mRoamingAllowed));
            values.put("allow_metered", Boolean.valueOf(this.mMeteredAllowed));
            values.put("flags", Integer.valueOf(this.mFlags));
            values.put(Downloads.Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, Boolean.valueOf(this.mIsVisibleInDownloadsUi));
            return values;
        }

        private void encodeHttpHeaders(ContentValues values) {
            int index = 0;
            for (Pair<String, String> header : this.mRequestHeaders) {
                values.put(Downloads.Impl.RequestHeaders.INSERT_KEY_PREFIX + index, ((String) header.first) + ": " + ((String) header.second));
                index++;
            }
        }

        private void putIfNonNull(ContentValues contentValues, String key, Object value) {
            if (value != null) {
                contentValues.put(key, value.toString());
            }
        }
    }

    public static class Query {
        public static final int ORDER_ASCENDING = 1;
        public static final int ORDER_DESCENDING = 2;
        private String mFilterString = null;
        private long[] mIds = null;
        private boolean mOnlyIncludeVisibleInDownloadsUi = false;
        private String mOrderByColumn = Downloads.Impl.COLUMN_LAST_MODIFICATION;
        private int mOrderDirection = 2;
        private Integer mStatusFlags = null;

        public Query setFilterById(long... ids) {
            this.mIds = ids;
            return this;
        }

        public Query setFilterByString(String filter) {
            this.mFilterString = filter;
            return this;
        }

        public Query setFilterByStatus(int flags) {
            this.mStatusFlags = Integer.valueOf(flags);
            return this;
        }

        @UnsupportedAppUsage
        public Query setOnlyIncludeVisibleInDownloadsUi(boolean value) {
            this.mOnlyIncludeVisibleInDownloadsUi = value;
            return this;
        }

        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public Query orderBy(String column, int direction) {
            if (direction == 1 || direction == 2) {
                if (column.equals(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)) {
                    this.mOrderByColumn = Downloads.Impl.COLUMN_LAST_MODIFICATION;
                } else if (column.equals(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)) {
                    this.mOrderByColumn = Downloads.Impl.COLUMN_TOTAL_BYTES;
                } else {
                    throw new IllegalArgumentException("Cannot order by " + column);
                }
                this.mOrderDirection = direction;
                return this;
            }
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }

        /* access modifiers changed from: package-private */
        public Cursor runQuery(ContentResolver resolver, String[] projection, Uri baseUri) {
            List<String> selectionParts = new ArrayList<>();
            long[] jArr = this.mIds;
            int whereArgsCount = jArr == null ? 0 : jArr.length;
            int whereArgsCount2 = this.mFilterString == null ? whereArgsCount : whereArgsCount + 1;
            String[] selectionArgs = new String[whereArgsCount2];
            if (whereArgsCount2 > 0) {
                long[] jArr2 = this.mIds;
                if (jArr2 != null) {
                    selectionParts.add(DownloadManager.getWhereClauseForIds(jArr2));
                    DownloadManager.getWhereArgsForIds(this.mIds, selectionArgs);
                }
                if (this.mFilterString != null) {
                    selectionParts.add("title LIKE ?");
                    selectionArgs[selectionArgs.length - 1] = "%" + this.mFilterString + "%";
                }
            }
            if (this.mStatusFlags != null) {
                List<String> parts = new ArrayList<>();
                if ((this.mStatusFlags.intValue() & 1) != 0) {
                    parts.add(statusClause("=", 190));
                }
                if ((this.mStatusFlags.intValue() & 2) != 0) {
                    parts.add(statusClause("=", 192));
                }
                if ((this.mStatusFlags.intValue() & 4) != 0) {
                    parts.add(statusClause("=", 193));
                    parts.add(statusClause("=", 194));
                    parts.add(statusClause("=", 195));
                    parts.add(statusClause("=", 196));
                }
                if ((this.mStatusFlags.intValue() & 8) != 0) {
                    parts.add(statusClause("=", 200));
                }
                if ((this.mStatusFlags.intValue() & 16) != 0) {
                    parts.add("(" + statusClause(">=", 400) + " AND " + statusClause("<", 600) + ")");
                }
                selectionParts.add(joinStrings(" OR ", parts));
            }
            if (this.mOnlyIncludeVisibleInDownloadsUi) {
                selectionParts.add("is_visible_in_downloads_ui != '0'");
            }
            selectionParts.add("deleted != '1'");
            String selection = joinStrings(" AND ", selectionParts);
            String orderDirection = this.mOrderDirection == 1 ? "ASC" : "DESC";
            return resolver.query(baseUri, projection, selection, selectionArgs, this.mOrderByColumn + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + orderDirection);
        }

        private String joinStrings(String joiner, Iterable<String> parts) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (String part : parts) {
                if (!first) {
                    builder.append(joiner);
                }
                builder.append(part);
                first = false;
            }
            return builder.toString();
        }

        private String statusClause(String operator, int value) {
            return "status" + operator + "'" + value + "'";
        }
    }

    public DownloadManager(Context context) {
        this.mResolver = context.getContentResolver();
        this.mPackageName = context.getPackageName();
        this.mAccessFilename = context.getApplicationInfo().targetSdkVersion < 24;
    }

    @UnsupportedAppUsage
    public void setAccessAllDownloads(boolean accessAllDownloads) {
        if (accessAllDownloads) {
            this.mBaseUri = Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI;
        } else {
            this.mBaseUri = Downloads.Impl.CONTENT_URI;
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void setAccessFilename(boolean accessFilename) {
        this.mAccessFilename = accessFilename;
    }

    public long enqueue(Request request) {
        return Long.parseLong(this.mResolver.insert(Downloads.Impl.CONTENT_URI, request.toContentValues(this.mPackageName)).getLastPathSegment());
    }

    public int markRowDeleted(long... ids) {
        if (ids != null && ids.length != 0) {
            return this.mResolver.delete(this.mBaseUri, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
        }
        throw new IllegalArgumentException("input param 'ids' can't be null");
    }

    public int remove(long... ids) {
        return markRowDeleted(ids);
    }

    public Cursor query(Query query) {
        return query(query, UNDERLYING_COLUMNS);
    }

    public Cursor query(Query query, String[] projection) {
        Cursor underlyingCursor = query.runQuery(this.mResolver, projection, this.mBaseUri);
        if (underlyingCursor == null) {
            return null;
        }
        return new CursorTranslator(underlyingCursor, this.mBaseUri, this.mAccessFilename);
    }

    public ParcelFileDescriptor openDownloadedFile(long id) throws FileNotFoundException {
        return this.mResolver.openFileDescriptor(getDownloadUri(id), "r");
    }

    public Uri getUriForDownloadedFile(long id) {
        Cursor cursor = null;
        try {
            cursor = query(new Query().setFilterById(id));
            if (cursor == null) {
                return null;
            }
            if (!cursor.moveToFirst() || 8 != cursor.getInt(cursor.getColumnIndexOrThrow("status"))) {
                cursor.close();
                return null;
            }
            Uri withAppendedId = ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, id);
            cursor.close();
            return withAppendedId;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getMimeTypeForDownloadedFile(long id) {
        Cursor cursor = null;
        try {
            cursor = query(new Query().setFilterById(id));
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                String string = cursor.getString(cursor.getColumnIndexOrThrow("media_type"));
                cursor.close();
                return string;
            }
            cursor.close();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    @UnsupportedAppUsage
    public void restartDownload(long... ids) {
        Cursor cursor = query(new Query().setFilterById(ids));
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int status = cursor.getInt(cursor.getColumnIndex("status"));
                if (status == 8 || status == 16) {
                    cursor.moveToNext();
                } else {
                    throw new IllegalArgumentException("Cannot restart incomplete download: " + cursor.getLong(cursor.getColumnIndex("_id")));
                }
            }
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, (Integer) 0);
            values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, (Integer) -1);
            values.putNull("_data");
            values.put("status", (Integer) 190);
            values.put(Downloads.Impl.COLUMN_FAILED_CONNECTIONS, (Integer) 0);
            this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
        } catch (Throwable th) {
            cursor.close();
            throw th;
        }
    }

    public void forceDownload(long... ids) {
        ContentValues values = new ContentValues();
        values.put("status", (Integer) 190);
        values.put(Downloads.Impl.COLUMN_CONTROL, (Integer) 0);
        values.put(Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT, (Integer) 1);
        this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
    }

    public static Long getMaxBytesOverMobile(Context context) {
        try {
            return Long.valueOf(Settings.Global.getLong(context.getContentResolver(), Settings.Global.DOWNLOAD_MAX_BYTES_OVER_MOBILE));
        } catch (Settings.SettingNotFoundException e) {
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0161, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0163, code lost:
        if (r9 != null) goto L_0x0165;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0169, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x016a, code lost:
        r0.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x016e, code lost:
        throw r0;
     */
    public boolean rename(Context context, long id, String displayName) {
        if (FileUtils.isValidFatFilename(displayName)) {
            Cursor cursor = query(new Query().setFilterById(id));
            if (cursor == null) {
                throw new IllegalStateException("Missing cursor for download id=" + id);
            } else if (!cursor.moveToFirst()) {
                throw new IllegalStateException("Missing download id=" + id);
            } else if (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == 8) {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCAL_FILENAME));
                if (filePath == null) {
                    throw new IllegalStateException("Download doesn't have a valid file path: " + DatabaseUtils.dumpCurrentRowToString(cursor));
                } else if (new File(filePath).exists()) {
                    cursor.close();
                    File before = new File(filePath);
                    File after = new File(before.getParentFile(), displayName);
                    if (after.exists()) {
                        throw new IllegalStateException("File already exists: " + after);
                    } else if (before.renameTo(after)) {
                        MediaStore.scanFile(context, before);
                        MediaStore.scanFile(context, after);
                        ContentValues values = new ContentValues();
                        values.put("title", displayName);
                        values.put("_data", after.toString());
                        values.putNull("mediaprovider_uri");
                        long[] ids = {id};
                        if (this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids)) == 1) {
                            return true;
                        }
                        return false;
                    } else {
                        throw new IllegalStateException("Failed to rename file from " + before + " to " + after);
                    }
                } else {
                    throw new IllegalStateException("Downloaded file doesn't exist anymore: " + DatabaseUtils.dumpCurrentRowToString(cursor));
                }
            } else {
                throw new IllegalStateException("Download is not completed yet: " + DatabaseUtils.dumpCurrentRowToString(cursor));
            }
        } else {
            throw new SecurityException(displayName + " is not a valid filename");
        }
    }

    public static Long getRecommendedMaxBytesOverMobile(Context context) {
        try {
            return Long.valueOf(Settings.Global.getLong(context.getContentResolver(), Settings.Global.DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE));
        } catch (Settings.SettingNotFoundException e) {
            return null;
        }
    }

    public static boolean isActiveNetworkExpensive(Context context) {
        return false;
    }

    public static long getActiveNetworkWarningBytes(Context context) {
        return -1;
    }

    @Deprecated
    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification) {
        return addCompletedDownload(title, description, isMediaScannerScannable, mimeType, path, length, showNotification, false, null, null);
    }

    @Deprecated
    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification, Uri uri, Uri referer) {
        return addCompletedDownload(title, description, isMediaScannerScannable, mimeType, path, length, showNotification, false, uri, referer);
    }

    @Deprecated
    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification, boolean allowWrite) {
        return addCompletedDownload(title, description, isMediaScannerScannable, mimeType, path, length, showNotification, allowWrite, null, null);
    }

    @Deprecated
    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification, boolean allowWrite, Uri uri, Uri referer) {
        Request request;
        int i;
        validateArgumentIsNonEmpty("title", title);
        validateArgumentIsNonEmpty("description", description);
        validateArgumentIsNonEmpty("path", path);
        validateArgumentIsNonEmpty("mimeType", mimeType);
        if (length >= 0) {
            if (uri != null) {
                request = new Request(uri);
            } else {
                request = new Request(NON_DOWNLOADMANAGER_DOWNLOAD);
            }
            request.setTitle(title).setDescription(description).setMimeType(mimeType);
            if (referer != null) {
                request.addRequestHeader("Referer", referer.toString());
            }
            ContentValues values = request.toContentValues(null);
            values.put("destination", (Integer) 6);
            values.put("_data", path);
            values.put("status", (Integer) 200);
            values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, Long.valueOf(length));
            int i2 = 2;
            if (isMediaScannerScannable) {
                i = 0;
            } else {
                i = 2;
            }
            values.put(Downloads.Impl.COLUMN_MEDIA_SCANNED, Integer.valueOf(i));
            if (showNotification) {
                i2 = 3;
            }
            values.put(Downloads.Impl.COLUMN_VISIBILITY, Integer.valueOf(i2));
            values.put("allow_write", Integer.valueOf(allowWrite ? 1 : 0));
            Uri downloadUri = this.mResolver.insert(Downloads.Impl.CONTENT_URI, values);
            if (downloadUri == null) {
                return -1;
            }
            return Long.parseLong(downloadUri.getLastPathSegment());
        }
        throw new IllegalArgumentException(" invalid value for param: totalBytes");
    }

    private static void validateArgumentIsNonEmpty(String paramName, String val) {
        if (TextUtils.isEmpty(val)) {
            throw new IllegalArgumentException(paramName + " can't be null");
        }
    }

    public Uri getDownloadUri(long id) {
        return ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, id);
    }

    @UnsupportedAppUsage
    static String getWhereClauseForIds(long[] ids) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                whereClause.append("OR ");
            }
            whereClause.append("_id");
            whereClause.append(" = ? ");
        }
        whereClause.append(")");
        return whereClause.toString();
    }

    @UnsupportedAppUsage
    static String[] getWhereArgsForIds(long[] ids) {
        return getWhereArgsForIds(ids, new String[ids.length]);
    }

    static String[] getWhereArgsForIds(long[] ids, String[] args) {
        for (int i = 0; i < ids.length; i++) {
            args[i] = Long.toString(ids[i]);
        }
        return args;
    }

    /* access modifiers changed from: private */
    public static class CursorTranslator extends CursorWrapper {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final boolean mAccessFilename;
        private final Uri mBaseUri;

        public CursorTranslator(Cursor cursor, Uri baseUri, boolean accessFilename) {
            super(cursor);
            this.mBaseUri = baseUri;
            this.mAccessFilename = accessFilename;
        }

        @Override // android.database.CursorWrapper, android.database.Cursor
        public int getInt(int columnIndex) {
            return (int) getLong(columnIndex);
        }

        @Override // android.database.CursorWrapper, android.database.Cursor
        public long getLong(int columnIndex) {
            if (getColumnName(columnIndex).equals("reason")) {
                return getReason(super.getInt(getColumnIndex("status")));
            }
            if (getColumnName(columnIndex).equals("status")) {
                return (long) translateStatus(super.getInt(getColumnIndex("status")));
            }
            return super.getLong(columnIndex);
        }

        /* JADX WARNING: Removed duplicated region for block: B:12:0x002b  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x003f  */
        @Override // android.database.CursorWrapper, android.database.Cursor
        public String getString(int columnIndex) {
            char c;
            String columnName = getColumnName(columnIndex);
            int hashCode = columnName.hashCode();
            if (hashCode != -1204869480) {
                if (hashCode == 22072411 && columnName.equals(DownloadManager.COLUMN_LOCAL_FILENAME)) {
                    c = 1;
                    if (c == 0) {
                        return getLocalUri();
                    }
                    if (c != 1 || this.mAccessFilename) {
                        return super.getString(columnIndex);
                    }
                    throw new SecurityException("COLUMN_LOCAL_FILENAME is deprecated; use ContentResolver.openFileDescriptor() instead");
                }
            } else if (columnName.equals(DownloadManager.COLUMN_LOCAL_URI)) {
                c = 0;
                if (c == 0) {
                }
            }
            c = 65535;
            if (c == 0) {
            }
        }

        private String getLocalUri() {
            long destinationType = getLong(getColumnIndex("destination"));
            if (destinationType == 4 || destinationType == 0 || destinationType == 6) {
                String localPath = super.getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                if (localPath == null) {
                    return null;
                }
                return Uri.fromFile(new File(localPath)).toString();
            }
            return ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, getLong(getColumnIndex("_id"))).toString();
        }

        private long getReason(int status) {
            int translateStatus = translateStatus(status);
            if (translateStatus == 4) {
                return getPausedReason(status);
            }
            if (translateStatus != 16) {
                return 0;
            }
            return getErrorCode(status);
        }

        private long getPausedReason(int status) {
            switch (status) {
                case 194:
                    return 1;
                case 195:
                    return 2;
                case 196:
                    return 3;
                default:
                    return 4;
            }
        }

        private long getErrorCode(int status) {
            if ((400 <= status && status < 488) || (500 <= status && status < 600)) {
                return (long) status;
            }
            if (status == 198) {
                return 1006;
            }
            if (status == 199) {
                return 1007;
            }
            if (status == 488) {
                return 1009;
            }
            if (status == 489) {
                return 1008;
            }
            if (status == 497) {
                return 1005;
            }
            switch (status) {
                case 492:
                    return 1001;
                case 493:
                case 494:
                    return 1002;
                case 495:
                    return 1004;
                default:
                    return 1000;
            }
        }

        private int translateStatus(int status) {
            if (status == 190) {
                return 1;
            }
            if (status == 200) {
                return 8;
            }
            switch (status) {
                case 192:
                    return 2;
                case 193:
                case 194:
                case 195:
                case 196:
                    return 4;
                default:
                    return 16;
            }
        }
    }
}
