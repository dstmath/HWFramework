package android.app;

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
import android.provider.Downloads.Impl.RequestHeaders;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.VoicemailContract.Voicemails;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    public static final String ACTION_DOWNLOAD_COMPLETE = "android.intent.action.DOWNLOAD_COMPLETE";
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
    public static final String[] UNDERLYING_COLUMNS = null;
    private boolean mAccessFilename;
    private Uri mBaseUri;
    private final String mPackageName;
    private final ContentResolver mResolver;

    private static class CursorTranslator extends CursorWrapper {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private final boolean mAccessFilename;
        private final Uri mBaseUri;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.DownloadManager.CursorTranslator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.DownloadManager.CursorTranslator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.DownloadManager.CursorTranslator.<clinit>():void");
        }

        public CursorTranslator(Cursor cursor, Uri baseUri, boolean accessFilename) {
            super(cursor);
            this.mBaseUri = baseUri;
            this.mAccessFilename = accessFilename;
        }

        public int getInt(int columnIndex) {
            return (int) getLong(columnIndex);
        }

        public long getLong(int columnIndex) {
            if (getColumnName(columnIndex).equals(DownloadManager.COLUMN_REASON)) {
                return getReason(super.getInt(getColumnIndex(DownloadManager.COLUMN_STATUS)));
            }
            if (getColumnName(columnIndex).equals(DownloadManager.COLUMN_STATUS)) {
                return (long) translateStatus(super.getInt(getColumnIndex(DownloadManager.COLUMN_STATUS)));
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
            long destinationType = getLong(getColumnIndex(Impl.COLUMN_DESTINATION));
            if (destinationType == 4 || destinationType == 0 || destinationType == 6) {
                String localPath = super.getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                if (localPath == null) {
                    return null;
                }
                return Uri.fromFile(new File(localPath)).toString();
            }
            return ContentUris.withAppendedId(this.mBaseUri, getLong(getColumnIndex(DownloadManager.COLUMN_ID))).toString();
        }

        private long getReason(int status) {
            switch (translateStatus(status)) {
                case DownloadManager.STATUS_PAUSED /*4*/:
                    return getPausedReason(status);
                case DownloadManager.STATUS_FAILED /*16*/:
                    return getErrorCode(status);
                default:
                    return 0;
            }
        }

        private long getPausedReason(int status) {
            switch (status) {
                case Impl.STATUS_WAITING_TO_RETRY /*194*/:
                    return 1;
                case Impl.STATUS_WAITING_FOR_NETWORK /*195*/:
                    return 2;
                case Impl.STATUS_QUEUED_FOR_WIFI /*196*/:
                    return 3;
                default:
                    return 4;
            }
        }

        private long getErrorCode(int status) {
            if ((Voice.QUALITY_HIGH <= status && status < Impl.STATUS_FILE_ALREADY_EXISTS_ERROR) || (Voice.QUALITY_VERY_HIGH <= status && status < CalendarColumns.CAL_ACCESS_EDITOR)) {
                return (long) status;
            }
            switch (status) {
                case Impl.STATUS_INSUFFICIENT_SPACE_ERROR /*198*/:
                    return 1006;
                case Impl.STATUS_DEVICE_NOT_FOUND_ERROR /*199*/:
                    return 1007;
                case Impl.STATUS_FILE_ALREADY_EXISTS_ERROR /*488*/:
                    return 1009;
                case Impl.STATUS_CANNOT_RESUME /*489*/:
                    return 1008;
                case Impl.STATUS_FILE_ERROR /*492*/:
                    return 1001;
                case Impl.STATUS_UNHANDLED_REDIRECT /*493*/:
                case Impl.STATUS_UNHANDLED_HTTP_CODE /*494*/:
                    return 1002;
                case Impl.STATUS_HTTP_DATA_ERROR /*495*/:
                    return 1004;
                case Impl.STATUS_TOO_MANY_REDIRECTS /*497*/:
                    return 1005;
                default:
                    return 1000;
            }
        }

        private int translateStatus(int status) {
            switch (status) {
                case Impl.STATUS_PENDING /*190*/:
                    return DownloadManager.STATUS_PENDING;
                case Impl.STATUS_RUNNING /*192*/:
                    return DownloadManager.STATUS_RUNNING;
                case Impl.STATUS_PAUSED_BY_APP /*193*/:
                case Impl.STATUS_WAITING_TO_RETRY /*194*/:
                case Impl.STATUS_WAITING_FOR_NETWORK /*195*/:
                case Impl.STATUS_QUEUED_FOR_WIFI /*196*/:
                    return DownloadManager.STATUS_PAUSED;
                case Voice.QUALITY_LOW /*200*/:
                    return DownloadManager.STATUS_SUCCESSFUL;
                default:
                    if (-assertionsDisabled || Impl.isStatusError(status)) {
                        return DownloadManager.STATUS_FAILED;
                    }
                    throw new AssertionError();
            }
        }
    }

    public static class Query {
        public static final int ORDER_ASCENDING = 1;
        public static final int ORDER_DESCENDING = 2;
        private long[] mIds;
        private boolean mOnlyIncludeVisibleInDownloadsUi;
        private String mOrderByColumn;
        private int mOrderDirection;
        private Integer mStatusFlags;

        public Query() {
            this.mIds = null;
            this.mStatusFlags = null;
            this.mOrderByColumn = Impl.COLUMN_LAST_MODIFICATION;
            this.mOrderDirection = ORDER_DESCENDING;
            this.mOnlyIncludeVisibleInDownloadsUi = false;
        }

        public Query setFilterById(long... ids) {
            this.mIds = ids;
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
            if (direction == ORDER_ASCENDING || direction == ORDER_DESCENDING) {
                if (column.equals(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)) {
                    this.mOrderByColumn = Impl.COLUMN_LAST_MODIFICATION;
                } else if (column.equals(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)) {
                    this.mOrderByColumn = Impl.COLUMN_TOTAL_BYTES;
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
            String[] selectionArgs = null;
            if (this.mIds != null) {
                selectionParts.add(DownloadManager.getWhereClauseForIds(this.mIds));
                selectionArgs = DownloadManager.getWhereArgsForIds(this.mIds);
            }
            if (this.mStatusFlags != null) {
                List<String> parts = new ArrayList();
                if ((this.mStatusFlags.intValue() & ORDER_ASCENDING) != 0) {
                    parts.add(statusClause("=", Impl.STATUS_PENDING));
                }
                if ((this.mStatusFlags.intValue() & ORDER_DESCENDING) != 0) {
                    parts.add(statusClause("=", Impl.STATUS_RUNNING));
                }
                if ((this.mStatusFlags.intValue() & DownloadManager.STATUS_PAUSED) != 0) {
                    parts.add(statusClause("=", Impl.STATUS_PAUSED_BY_APP));
                    parts.add(statusClause("=", Impl.STATUS_WAITING_TO_RETRY));
                    parts.add(statusClause("=", Impl.STATUS_WAITING_FOR_NETWORK));
                    parts.add(statusClause("=", Impl.STATUS_QUEUED_FOR_WIFI));
                }
                if ((this.mStatusFlags.intValue() & DownloadManager.STATUS_SUCCESSFUL) != 0) {
                    parts.add(statusClause("=", Voice.QUALITY_LOW));
                }
                if ((this.mStatusFlags.intValue() & DownloadManager.STATUS_FAILED) != 0) {
                    parts.add("(" + statusClause(">=", Voice.QUALITY_HIGH) + " AND " + statusClause("<", CalendarColumns.CAL_ACCESS_EDITOR) + ")");
                }
                selectionParts.add(joinStrings(" OR ", parts));
            }
            if (this.mOnlyIncludeVisibleInDownloadsUi) {
                selectionParts.add("is_visible_in_downloads_ui != '0'");
            }
            selectionParts.add("deleted != '1'");
            return resolver.query(baseUri, projection, joinStrings(" AND ", selectionParts), selectionArgs, this.mOrderByColumn + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + (this.mOrderDirection == ORDER_ASCENDING ? "ASC" : "DESC"));
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
            return DownloadManager.COLUMN_STATUS + operator + "'" + value + "'";
        }
    }

    public static class Request {
        static final /* synthetic */ boolean -assertionsDisabled = false;
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
        private int mAllowedNetworkTypes;
        private CharSequence mDescription;
        private Uri mDestinationUri;
        private int mFlags;
        private boolean mIsVisibleInDownloadsUi;
        private boolean mMeteredAllowed;
        private String mMimeType;
        private int mNotificationVisibility;
        private List<Pair<String, String>> mRequestHeaders;
        private boolean mRoamingAllowed;
        private boolean mScannable;
        private CharSequence mTitle;
        private Uri mUri;
        private boolean mUseSystemCache;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.DownloadManager.Request.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.DownloadManager.Request.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.DownloadManager.Request.<clinit>():void");
        }

        public Request(Uri uri) {
            this.mRequestHeaders = new ArrayList();
            this.mAllowedNetworkTypes = -1;
            this.mRoamingAllowed = true;
            this.mMeteredAllowed = true;
            this.mFlags = VISIBILITY_VISIBLE;
            this.mIsVisibleInDownloadsUi = true;
            this.mScannable = -assertionsDisabled;
            this.mUseSystemCache = -assertionsDisabled;
            this.mNotificationVisibility = VISIBILITY_VISIBLE;
            if (uri == null) {
                throw new NullPointerException();
            }
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equals(IntentFilter.SCHEME_HTTP) || scheme.equals(IntentFilter.SCHEME_HTTPS))) {
                throw new IllegalArgumentException("Can only download HTTP/HTTPS URIs: " + uri);
            }
            this.mUri = uri;
        }

        Request(String uriString) {
            this.mRequestHeaders = new ArrayList();
            this.mAllowedNetworkTypes = -1;
            this.mRoamingAllowed = true;
            this.mMeteredAllowed = true;
            this.mFlags = VISIBILITY_VISIBLE;
            this.mIsVisibleInDownloadsUi = true;
            this.mScannable = -assertionsDisabled;
            this.mUseSystemCache = -assertionsDisabled;
            this.mNotificationVisibility = VISIBILITY_VISIBLE;
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
                if (value == null) {
                    value = ProxyInfo.LOCAL_EXCL_LIST;
                }
                this.mRequestHeaders.add(Pair.create(header, value));
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
                return setNotificationVisibility(VISIBILITY_VISIBLE);
            }
            return setNotificationVisibility(VISIBILITY_HIDDEN);
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
                this.mFlags |= VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
            } else {
                this.mFlags &= -2;
            }
            return this;
        }

        public Request setRequiresDeviceIdle(boolean requiresDeviceIdle) {
            if (requiresDeviceIdle) {
                this.mFlags |= VISIBILITY_HIDDEN;
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
            int i = VISIBILITY_HIDDEN;
            ContentValues values = new ContentValues();
            if (!-assertionsDisabled) {
                if (!(this.mUri != null ? true : VISIBILITY_VISIBLE)) {
                    throw new AssertionError();
                }
            }
            values.put(DownloadManager.COLUMN_URI, this.mUri.toString());
            values.put(Impl.COLUMN_IS_PUBLIC_API, Boolean.valueOf(true));
            values.put(Impl.COLUMN_NOTIFICATION_PACKAGE, packageName);
            if (this.mDestinationUri != null) {
                values.put(Impl.COLUMN_DESTINATION, Integer.valueOf(NETWORK_BLUETOOTH));
                values.put(Impl.COLUMN_FILE_NAME_HINT, this.mDestinationUri.toString());
            } else {
                int i2;
                String str = Impl.COLUMN_DESTINATION;
                if (this.mUseSystemCache) {
                    i2 = 5;
                } else {
                    i2 = VISIBILITY_HIDDEN;
                }
                values.put(str, Integer.valueOf(i2));
            }
            String str2 = Impl.COLUMN_MEDIA_SCANNED;
            if (this.mScannable) {
                i = VISIBILITY_VISIBLE;
            }
            values.put(str2, Integer.valueOf(i));
            if (!this.mRequestHeaders.isEmpty()) {
                encodeHttpHeaders(values);
            }
            putIfNonNull(values, DownloadManager.COLUMN_TITLE, this.mTitle);
            putIfNonNull(values, DownloadManager.COLUMN_DESCRIPTION, this.mDescription);
            putIfNonNull(values, Impl.COLUMN_MIME_TYPE, this.mMimeType);
            values.put(Impl.COLUMN_VISIBILITY, Integer.valueOf(this.mNotificationVisibility));
            values.put(Impl.COLUMN_ALLOWED_NETWORK_TYPES, Integer.valueOf(this.mAllowedNetworkTypes));
            values.put(Impl.COLUMN_ALLOW_ROAMING, Boolean.valueOf(this.mRoamingAllowed));
            values.put(Impl.COLUMN_ALLOW_METERED, Boolean.valueOf(this.mMeteredAllowed));
            values.put(Impl.COLUMN_FLAGS, Integer.valueOf(this.mFlags));
            values.put(Impl.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, Boolean.valueOf(this.mIsVisibleInDownloadsUi));
            return values;
        }

        private void encodeHttpHeaders(ContentValues values) {
            int index = VISIBILITY_VISIBLE;
            for (Pair<String, String> header : this.mRequestHeaders) {
                values.put(RequestHeaders.INSERT_KEY_PREFIX + index, ((String) header.first) + ": " + ((String) header.second));
                index += VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
            }
        }

        private void putIfNonNull(ContentValues contentValues, String key, Object value) {
            if (value != null) {
                contentValues.put(key, value.toString());
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.DownloadManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.DownloadManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.DownloadManager.<clinit>():void");
    }

    public DownloadManager(Context context) {
        boolean z;
        this.mBaseUri = Impl.CONTENT_URI;
        this.mResolver = context.getContentResolver();
        this.mPackageName = context.getPackageName();
        if (context.getApplicationInfo().targetSdkVersion < 24) {
            z = true;
        } else {
            z = false;
        }
        this.mAccessFilename = z;
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
        Query query = new Query();
        long[] jArr = new long[STATUS_PENDING];
        jArr[0] = id;
        Cursor cursor = null;
        try {
            cursor = query(query.setFilterById(jArr));
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst() && STATUS_SUCCESSFUL == cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS))) {
                Uri withAppendedId = ContentUris.withAppendedId(Impl.CONTENT_URI, id);
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
        Query query = new Query();
        long[] jArr = new long[STATUS_PENDING];
        jArr[0] = id;
        Cursor cursor = null;
        try {
            cursor = query(query.setFilterById(jArr));
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
        String str = null;
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
                if (status == STATUS_SUCCESSFUL || status == STATUS_FAILED) {
                    str = cursor.getString(cursor.getColumnIndex(COLUMN_URI));
                    cursor.moveToNext();
                } else {
                    throw new IllegalArgumentException("Cannot restart incomplete download: " + cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                }
            }
            ContentValues values = new ContentValues();
            values.put(Impl.COLUMN_CURRENT_BYTES, Integer.valueOf(0));
            values.put(Impl.COLUMN_TOTAL_BYTES, Integer.valueOf(-1));
            values.putNull(Voicemails._DATA);
            values.put(COLUMN_STATUS, Integer.valueOf(Impl.STATUS_PENDING));
            values.put(Impl.COLUMN_FAILED_CONNECTIONS, Integer.valueOf(0));
            HwFrameworkFactory.getHwDrmManager().updateOmaMimeType(str, values);
            this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
        } finally {
            cursor.close();
        }
    }

    public void forceDownload(long... ids) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, Integer.valueOf(Impl.STATUS_PENDING));
        values.put(Impl.COLUMN_CONTROL, Integer.valueOf(0));
        values.put(Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT, Integer.valueOf(STATUS_PENDING));
        this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
    }

    public static Long getMaxBytesOverMobile(Context context) {
        try {
            return Long.valueOf(Global.getLong(context.getContentResolver(), Global.DOWNLOAD_MAX_BYTES_OVER_MOBILE));
        } catch (SettingNotFoundException e) {
            return null;
        }
    }

    public boolean rename(Context context, long id, String displayName) {
        if (FileUtils.isValidFatFilename(displayName)) {
            Query query = new Query();
            long[] jArr = new long[STATUS_PENDING];
            jArr[0] = id;
            Cursor cursor = null;
            String str = null;
            String mimeType = null;
            try {
                cursor = query(query.setFilterById(jArr));
                if (cursor == null) {
                    return false;
                }
                if (cursor.moveToFirst()) {
                    if (STATUS_SUCCESSFUL != cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS))) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    }
                    str = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEDIA_TYPE));
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (str == null || mimeType == null) {
                    throw new IllegalStateException("Document with id " + id + " does not exist");
                }
                File parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File before = new File(parent, str);
                File after = new File(parent, displayName);
                if (after.exists()) {
                    throw new IllegalStateException("Already exists " + after);
                } else if (before.renameTo(after)) {
                    if (mimeType.startsWith("image/")) {
                        String[] strArr = new String[STATUS_PENDING];
                        strArr[0] = before.getAbsolutePath();
                        context.getContentResolver().delete(Media.EXTERNAL_CONTENT_URI, "_data=?", strArr);
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(after));
                        context.sendBroadcast(intent);
                    }
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_TITLE, displayName);
                    values.put(Voicemails._DATA, after.toString());
                    values.putNull(COLUMN_MEDIAPROVIDER_URI);
                    long[] ids = new long[STATUS_PENDING];
                    ids[0] = id;
                    return this.mResolver.update(this.mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids)) == STATUS_PENDING;
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
            return Long.valueOf(Global.getLong(context.getContentResolver(), Global.DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE));
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
        validateArgumentIsNonEmpty(COLUMN_TITLE, title);
        validateArgumentIsNonEmpty(COLUMN_DESCRIPTION, description);
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
        values.put(Impl.COLUMN_DESTINATION, Integer.valueOf(6));
        values.put(Voicemails._DATA, path);
        values.put(COLUMN_STATUS, Integer.valueOf(Voice.QUALITY_LOW));
        values.put(Impl.COLUMN_TOTAL_BYTES, Long.valueOf(length));
        String str = Impl.COLUMN_MEDIA_SCANNED;
        if (isMediaScannerScannable) {
            i = 0;
        } else {
            i = STATUS_RUNNING;
        }
        values.put(str, Integer.valueOf(i));
        values.put(Impl.COLUMN_VISIBILITY, Integer.valueOf(showNotification ? PAUSED_QUEUED_FOR_WIFI : STATUS_RUNNING));
        values.put(COLUMN_ALLOW_WRITE, Integer.valueOf(allowWrite ? STATUS_PENDING : 0));
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
        return ContentUris.withAppendedId(this.mBaseUri, id);
    }

    static String getWhereClauseForIds(long[] ids) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        for (int i = 0; i < ids.length; i += STATUS_PENDING) {
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
        String[] whereArgs = new String[ids.length];
        for (int i = 0; i < ids.length; i += STATUS_PENDING) {
            whereArgs[i] = Long.toString(ids[i]);
        }
        return whereArgs;
    }
}
