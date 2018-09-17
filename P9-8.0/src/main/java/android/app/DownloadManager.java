package android.app;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.backup.FullBackup;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.provider.Downloads.Impl;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    static final /* synthetic */ boolean -assertionsDisabled = (DownloadManager.class.desiredAssertionStatus() ^ 1);
    public static final String ACTION_DOWNLOAD_COMPLETE = "android.intent.action.DOWNLOAD_COMPLETE";
    public static final String ACTION_DOWNLOAD_COMPLETED = "android.intent.action.DOWNLOAD_COMPLETED";
    public static final String ACTION_NOTIFICATION_CLICKED = "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";
    public static final String ACTION_VIEW_DOWNLOADS = "android.intent.action.VIEW_DOWNLOADS";
    public static final String COLUMN_ALLOW_WRITE = "allow_write";
    public static final String COLUMN_BYTES_DOWNLOADED_SO_FAR = "bytes_so_far";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LAST_MODIFIED_TIMESTAMP = "last_modified_timestamp";
    @Deprecated
    public static final String COLUMN_LOCAL_FILENAME = "local_filename";
    public static final String COLUMN_LOCAL_URI = "local_uri";
    public static final String COLUMN_MEDIAPROVIDER_URI = "mediaprovider_uri";
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
    public static final String[] UNDERLYING_COLUMNS = new String[]{COLUMN_ID, "_data AS local_filename", COLUMN_MEDIAPROVIDER_URI, "destination", "title", "description", COLUMN_URI, "status", "hint", "mimetype AS media_type", "total_bytes AS total_size", "lastmod AS last_modified_timestamp", "current_bytes AS bytes_so_far", COLUMN_ALLOW_WRITE, "'placeholder' AS local_uri", "'placeholder' AS reason"};
    private boolean mAccessFilename;
    private Uri mBaseUri = Impl.CONTENT_URI;
    private final String mPackageName;
    private final ContentResolver mResolver;

    private static class CursorTranslator extends CursorWrapper {
        static final /* synthetic */ boolean -assertionsDisabled = (CursorTranslator.class.desiredAssertionStatus() ^ 1);
        private final boolean mAccessFilename;
        private final Uri mBaseUri;

        public CursorTranslator(Cursor cursor, Uri baseUri, boolean accessFilename) {
            super(cursor);
            this.mBaseUri = baseUri;
            this.mAccessFilename = accessFilename;
        }

        public int getInt(int columnIndex) {
            return (int) getLong(columnIndex);
        }

        public long getLong(int columnIndex) {
            if (getColumnName(columnIndex).equals("reason")) {
                return getReason(super.getInt(getColumnIndex("status")));
            }
            if (getColumnName(columnIndex).equals("status")) {
                return (long) translateStatus(super.getInt(getColumnIndex("status")));
            }
            return super.getLong(columnIndex);
        }

        public String getString(int columnIndex) {
            String columnName = getColumnName(columnIndex);
            if (columnName.equals(DownloadManager.COLUMN_LOCAL_URI)) {
                return getLocalUri();
            }
            if (!columnName.equals(DownloadManager.COLUMN_LOCAL_FILENAME) || this.mAccessFilename) {
                return super.getString(columnIndex);
            }
            throw new SecurityException("COLUMN_LOCAL_FILENAME is deprecated; use ContentResolver.openFileDescriptor() instead");
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
            return ContentUris.withAppendedId(Impl.ALL_DOWNLOADS_CONTENT_URI, getLong(getColumnIndex(DownloadManager.COLUMN_ID))).toString();
        }

        private long getReason(int status) {
            switch (translateStatus(status)) {
                case 4:
                    return getPausedReason(status);
                case 16:
                    return getErrorCode(status);
                default:
                    return 0;
            }
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
            if ((400 <= status && status < 488) || (RunningAppProcessInfo.IMPORTANCE_EMPTY <= status && status < 600)) {
                return (long) status;
            }
            switch (status) {
                case 198:
                    return 1006;
                case 199:
                    return 1007;
                case 488:
                    return 1009;
                case 489:
                    return 1008;
                case 492:
                    return 1001;
                case 493:
                case 494:
                    return 1002;
                case 495:
                    return 1004;
                case 497:
                    return 1005;
                default:
                    return 1000;
            }
        }

        private int translateStatus(int status) {
            switch (status) {
                case 190:
                    return 1;
                case 192:
                    return 2;
                case 193:
                case 194:
                case 195:
                case 196:
                    return 4;
                case 200:
                    return 8;
                default:
                    if (-assertionsDisabled || Impl.isStatusError(status)) {
                        return 16;
                    }
                    throw new AssertionError();
            }
        }
    }

    public static class Query {
        public static final int ORDER_ASCENDING = 1;
        public static final int ORDER_DESCENDING = 2;
        private String mFilterString = null;
        private long[] mIds = null;
        private boolean mOnlyIncludeVisibleInDownloadsUi = false;
        private String mOrderByColumn = "lastmod";
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

        public Query setOnlyIncludeVisibleInDownloadsUi(boolean value) {
            this.mOnlyIncludeVisibleInDownloadsUi = value;
            return this;
        }

        public Query orderBy(String column, int direction) {
            if (direction == 1 || direction == 2) {
                if (column.equals(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)) {
                    this.mOrderByColumn = "lastmod";
                } else if (column.equals(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)) {
                    this.mOrderByColumn = "total_bytes";
                } else {
                    throw new IllegalArgumentException("Cannot order by " + column);
                }
                this.mOrderDirection = direction;
                return this;
            }
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }

        Cursor runQuery(ContentResolver resolver, String[] projection, Uri baseUri) {
            Uri uri = baseUri;
            List<String> selectionParts = new ArrayList();
            int whereArgsCount = this.mIds == null ? 0 : this.mIds.length;
            if (this.mFilterString != null) {
                whereArgsCount++;
            }
            String[] selectionArgs = new String[whereArgsCount];
            if (whereArgsCount > 0) {
                if (this.mIds != null) {
                    selectionParts.add(DownloadManager.getWhereClauseForIds(this.mIds));
                    DownloadManager.getWhereArgsForIds(this.mIds, selectionArgs);
                }
                if (this.mFilterString != null) {
                    selectionParts.add("title LIKE ?");
                    selectionArgs[selectionArgs.length - 1] = "%" + this.mFilterString + "%";
                }
            }
            if (this.mStatusFlags != null) {
                List<String> parts = new ArrayList();
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
            return resolver.query(baseUri, projection, joinStrings(" AND ", selectionParts), selectionArgs, this.mOrderByColumn + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + (this.mOrderDirection == 1 ? "ASC" : "DESC"));
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

    public static class Request {
        static final /* synthetic */ boolean -assertionsDisabled = (Request.class.desiredAssertionStatus() ^ 1);
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
        private Uri mUri;
        private boolean mUseSystemCache = false;

        public Request(Uri uri) {
            if (uri == null) {
                throw new NullPointerException();
            }
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equals(IntentFilter.SCHEME_HTTP) || (scheme.equals(IntentFilter.SCHEME_HTTPS) ^ 1) == 0)) {
                throw new IllegalArgumentException("Can only download HTTP/HTTPS URIs: " + uri);
            }
            this.mUri = uri;
        }

        Request(String uriString) {
            this.mUri = Uri.parse(uriString);
        }

        public Request setDestinationUri(Uri uri) {
            this.mDestinationUri = uri;
            return this;
        }

        public Request setDestinationToSystemCache() {
            this.mUseSystemCache = true;
            return this;
        }

        public Request setDestinationInExternalFilesDir(Context context, String dirType, String subPath) {
            File file = context.getExternalFilesDir(dirType);
            if (file == null) {
                throw new IllegalStateException("Failed to get external storage files directory");
            }
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

        public Request setDestinationInExternalPublicDir(String dirType, String subPath) {
            File file = Environment.getExternalStoragePublicDirectory(dirType);
            if (file == null) {
                throw new IllegalStateException("Failed to get external storage public directory");
            }
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

        private void setDestinationFromBase(File base, String subPath) {
            if (subPath == null) {
                throw new NullPointerException("subPath cannot be null");
            }
            this.mDestinationUri = Uri.withAppendedPath(Uri.fromFile(base), subPath);
        }

        public void allowScanningByMediaScanner() {
            this.mScannable = true;
        }

        public Request addRequestHeader(String header, String value) {
            if (header == null) {
                throw new NullPointerException("header cannot be null");
            } else if (header.contains(":")) {
                throw new IllegalArgumentException("header may not contain ':'");
            } else {
                Object value2;
                if (value2 == null) {
                    value2 = ProxyInfo.LOCAL_EXCL_LIST;
                }
                this.mRequestHeaders.add(Pair.create(header, value2));
                return this;
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

        public Request setVisibleInDownloadsUi(boolean isVisible) {
            this.mIsVisibleInDownloadsUi = isVisible;
            return this;
        }

        ContentValues toContentValues(String packageName) {
            int i = 2;
            ContentValues values = new ContentValues();
            if (-assertionsDisabled || this.mUri != null) {
                values.put(DownloadManager.COLUMN_URI, this.mUri.toString());
                values.put("is_public_api", Boolean.valueOf(true));
                values.put("notificationpackage", packageName);
                if (this.mDestinationUri != null) {
                    values.put("destination", Integer.valueOf(4));
                    values.put("hint", this.mDestinationUri.toString());
                } else {
                    int i2;
                    String str = "destination";
                    if (this.mUseSystemCache) {
                        i2 = 5;
                    } else {
                        i2 = 2;
                    }
                    values.put(str, Integer.valueOf(i2));
                }
                String str2 = "scanned";
                if (this.mScannable) {
                    i = 0;
                }
                values.put(str2, Integer.valueOf(i));
                if (!this.mRequestHeaders.isEmpty()) {
                    encodeHttpHeaders(values);
                }
                putIfNonNull(values, "title", this.mTitle);
                putIfNonNull(values, "description", this.mDescription);
                putIfNonNull(values, "mimetype", this.mMimeType);
                values.put("visibility", Integer.valueOf(this.mNotificationVisibility));
                values.put("allowed_network_types", Integer.valueOf(this.mAllowedNetworkTypes));
                values.put("allow_roaming", Boolean.valueOf(this.mRoamingAllowed));
                values.put(ContentResolver.SYNC_EXTRAS_DISALLOW_METERED, Boolean.valueOf(this.mMeteredAllowed));
                values.put("flags", Integer.valueOf(this.mFlags));
                values.put("is_visible_in_downloads_ui", Boolean.valueOf(this.mIsVisibleInDownloadsUi));
                return values;
            }
            throw new AssertionError();
        }

        private void encodeHttpHeaders(ContentValues values) {
            int index = 0;
            for (Pair<String, String> header : this.mRequestHeaders) {
                values.put("http_header_" + index, ((String) header.first) + ": " + ((String) header.second));
                index++;
            }
        }

        private void putIfNonNull(ContentValues contentValues, String key, Object value) {
            if (value != null) {
                contentValues.put(key, value.toString());
            }
        }
    }

    public DownloadManager(Context context) {
        this.mResolver = context.getContentResolver();
        this.mPackageName = context.getPackageName();
        this.mAccessFilename = context.getApplicationInfo().targetSdkVersion < 24;
    }

    public void setAccessAllDownloads(boolean accessAllDownloads) {
        if (accessAllDownloads) {
            this.mBaseUri = Impl.ALL_DOWNLOADS_CONTENT_URI;
        } else {
            this.mBaseUri = Impl.CONTENT_URI;
        }
    }

    public void setAccessFilename(boolean accessFilename) {
        this.mAccessFilename = accessFilename;
    }

    public long enqueue(Request request) {
        return Long.parseLong(this.mResolver.insert(Impl.CONTENT_URI, request.toContentValues(this.mPackageName)).getLastPathSegment());
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
        Cursor underlyingCursor = query.runQuery(this.mResolver, UNDERLYING_COLUMNS, this.mBaseUri);
        if (underlyingCursor == null) {
            return null;
        }
        return new CursorTranslator(underlyingCursor, this.mBaseUri, this.mAccessFilename);
    }

    public ParcelFileDescriptor openDownloadedFile(long id) throws FileNotFoundException {
        return this.mResolver.openFileDescriptor(getDownloadUri(id), FullBackup.ROOT_TREE_TOKEN);
    }

    public Uri getUriForDownloadedFile(long id) {
        Cursor cursor = null;
        try {
            cursor = query(new Query().setFilterById(id));
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst() && 8 == cursor.getInt(cursor.getColumnIndexOrThrow("status"))) {
                Uri withAppendedId = ContentUris.withAppendedId(Impl.ALL_DOWNLOADS_CONTENT_URI, id);
                if (cursor != null) {
                    cursor.close();
                }
                return withAppendedId;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
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
                String string = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEDIA_TYPE));
                if (cursor != null) {
                    cursor.close();
                }
                return string;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void restartDownload(long... ids) {
        Cursor cursor = query(new Query().setFilterById(ids));
        String uri = null;
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int status = cursor.getInt(cursor.getColumnIndex("status"));
                if (status == 8 || status == 16) {
                    uri = cursor.getString(cursor.getColumnIndex(COLUMN_URI));
                    cursor.moveToNext();
                } else {
                    throw new IllegalArgumentException("Cannot restart incomplete download: " + cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                }
            }
            ContentValues values = new ContentValues();
            values.put("current_bytes", Integer.valueOf(0));
            values.put("total_bytes", Integer.valueOf(-1));
            values.putNull("_data");
            values.put("status", Integer.valueOf(190));
            values.put("numfailed", Integer.valueOf(0));
            HwFrameworkFactory.getHwDrmManager().updateOmaMimeType(uri, values);
            this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
        } finally {
            cursor.close();
        }
    }

    public void forceDownload(long... ids) {
        ContentValues values = new ContentValues();
        values.put("status", Integer.valueOf(190));
        values.put("control", Integer.valueOf(0));
        values.put("bypass_recommended_size_limit", Integer.valueOf(1));
        this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
    }

    public static Long getMaxBytesOverMobile(Context context) {
        try {
            return Long.valueOf(Global.getLong(context.getContentResolver(), "download_manager_max_bytes_over_mobile"));
        } catch (SettingNotFoundException e) {
            return null;
        }
    }

    public boolean rename(Context context, long id, String displayName) {
        if (FileUtils.isValidFatFilename(displayName)) {
            Cursor cursor = null;
            String oldDisplayName = null;
            String mimeType = null;
            try {
                cursor = query(new Query().setFilterById(id));
                if (cursor == null) {
                    return false;
                }
                if (cursor.moveToFirst()) {
                    if (8 != cursor.getInt(cursor.getColumnIndexOrThrow("status"))) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    }
                    oldDisplayName = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEDIA_TYPE));
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (oldDisplayName == null || mimeType == null) {
                    throw new IllegalStateException("Document with id " + id + " does not exist");
                }
                File parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File before = new File(parent, oldDisplayName);
                File after = new File(parent, displayName);
                if (after.exists()) {
                    throw new IllegalStateException("Already exists " + after);
                } else if (before.renameTo(after)) {
                    if (mimeType.startsWith("image/")) {
                        context.getContentResolver().delete(Media.EXTERNAL_CONTENT_URI, "_data=?", new String[]{before.getAbsolutePath()});
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(after));
                        context.sendBroadcast(intent);
                    }
                    ContentValues values = new ContentValues();
                    values.put("title", displayName);
                    values.put("_data", after.toString());
                    values.putNull(COLUMN_MEDIAPROVIDER_URI);
                    long[] ids = new long[]{id};
                    return this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids)) == 1;
                } else {
                    throw new IllegalStateException("Failed to rename to " + after);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            throw new SecurityException(displayName + " is not a valid filename");
        }
    }

    public static Long getRecommendedMaxBytesOverMobile(Context context) {
        try {
            return Long.valueOf(Global.getLong(context.getContentResolver(), "download_manager_recommended_max_bytes_over_mobile"));
        } catch (SettingNotFoundException e) {
            return null;
        }
    }

    public static boolean isActiveNetworkExpensive(Context context) {
        return false;
    }

    public static long getActiveNetworkWarningBytes(Context context) {
        return -1;
    }

    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification) {
        return addCompletedDownload(title, description, isMediaScannerScannable, mimeType, path, length, showNotification, false, null, null);
    }

    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification, Uri uri, Uri referer) {
        return addCompletedDownload(title, description, isMediaScannerScannable, mimeType, path, length, showNotification, false, uri, referer);
    }

    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification, boolean allowWrite) {
        return addCompletedDownload(title, description, isMediaScannerScannable, mimeType, path, length, showNotification, allowWrite, null, null);
    }

    public long addCompletedDownload(String title, String description, boolean isMediaScannerScannable, String mimeType, String path, long length, boolean showNotification, boolean allowWrite, Uri uri, Uri referer) {
        validateArgumentIsNonEmpty("title", title);
        validateArgumentIsNonEmpty("description", description);
        validateArgumentIsNonEmpty("path", path);
        validateArgumentIsNonEmpty("mimeType", mimeType);
        if (length < 0) {
            throw new IllegalArgumentException(" invalid value for param: totalBytes");
        }
        Request request;
        int i;
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
        values.put("destination", Integer.valueOf(6));
        values.put("_data", path);
        values.put("status", Integer.valueOf(200));
        values.put("total_bytes", Long.valueOf(length));
        String str = "scanned";
        if (isMediaScannerScannable) {
            i = 0;
        } else {
            i = 2;
        }
        values.put(str, Integer.valueOf(i));
        values.put("visibility", Integer.valueOf(showNotification ? 3 : 2));
        values.put(COLUMN_ALLOW_WRITE, Integer.valueOf(allowWrite ? 1 : 0));
        Uri downloadUri = this.mResolver.insert(Impl.CONTENT_URI, values);
        if (downloadUri == null) {
            return -1;
        }
        return Long.parseLong(downloadUri.getLastPathSegment());
    }

    private static void validateArgumentIsNonEmpty(String paramName, String val) {
        if (TextUtils.isEmpty(val)) {
            throw new IllegalArgumentException(paramName + " can't be null");
        }
    }

    public Uri getDownloadUri(long id) {
        return ContentUris.withAppendedId(Impl.ALL_DOWNLOADS_CONTENT_URI, id);
    }

    static String getWhereClauseForIds(long[] ids) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                whereClause.append("OR ");
            }
            whereClause.append(COLUMN_ID);
            whereClause.append(" = ? ");
        }
        whereClause.append(")");
        return whereClause.toString();
    }

    static String[] getWhereArgsForIds(long[] ids) {
        return getWhereArgsForIds(ids, new String[ids.length]);
    }

    static String[] getWhereArgsForIds(long[] ids, String[] args) {
        if (-assertionsDisabled || args.length >= ids.length) {
            for (int i = 0; i < ids.length; i++) {
                args[i] = Long.toString(ids[i]);
            }
            return args;
        }
        throw new AssertionError();
    }
}
