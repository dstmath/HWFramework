package android.media;

import android.Manifest.permission;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.StaleDataException;
import android.media.IAudioService.Stub;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.database.SortCursor;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import libcore.io.Streams;

public class RingtoneManager {
    public static final String ACTION_RINGTONE_PICKER = "android.intent.action.RINGTONE_PICKER";
    public static final String EXTRA_RINGTONE_AUDIO_ATTRIBUTES_FLAGS = "android.intent.extra.ringtone.AUDIO_ATTRIBUTES_FLAGS";
    public static final String EXTRA_RINGTONE_DEFAULT_URI = "android.intent.extra.ringtone.DEFAULT_URI";
    public static final String EXTRA_RINGTONE_EXISTING_URI = "android.intent.extra.ringtone.EXISTING_URI";
    @Deprecated
    public static final String EXTRA_RINGTONE_INCLUDE_DRM = "android.intent.extra.ringtone.INCLUDE_DRM";
    public static final String EXTRA_RINGTONE_PICKED_URI = "android.intent.extra.ringtone.PICKED_URI";
    public static final String EXTRA_RINGTONE_SHOW_DEFAULT = "android.intent.extra.ringtone.SHOW_DEFAULT";
    public static final String EXTRA_RINGTONE_SHOW_SILENT = "android.intent.extra.ringtone.SHOW_SILENT";
    public static final String EXTRA_RINGTONE_TITLE = "android.intent.extra.ringtone.TITLE";
    public static final String EXTRA_RINGTONE_TYPE = "android.intent.extra.ringtone.TYPE";
    private static final int[] HW_RINGTONE_TYPES = new int[]{1, 2, 4, 8};
    public static final int HW_TYPE_ALL = 15;
    public static final int ID_COLUMN_INDEX = 0;
    private static final String[] INTERNAL_COLUMNS = new String[]{DownloadManager.COLUMN_ID, "title", "\"" + Media.INTERNAL_CONTENT_URI + "\"", "title_key"};
    private static final String[] MEDIA_COLUMNS = new String[]{DownloadManager.COLUMN_ID, "title", "\"" + Media.EXTERNAL_CONTENT_URI + "\"", "title_key"};
    private static final int[] RINGTONE_TYPES = new int[]{1, 2, 4};
    private static final String TAG = "RingtoneManager";
    public static final int TITLE_COLUMN_INDEX = 1;
    public static final int TYPE_ALARM = 4;
    public static final int TYPE_ALL = 7;
    public static final int TYPE_NOTIFICATION = 2;
    public static final int TYPE_RINGTONE = 1;
    public static final int TYPE_RINGTONE2 = 8;
    public static final int URI_COLUMN_INDEX = 2;
    private static boolean mSetUriStat = false;
    private final Activity mActivity;
    private final Context mContext;
    private Cursor mCursor;
    private final List<String> mFilterColumns;
    private boolean mIncludeParentRingtones;
    private Ringtone mPreviousRingtone;
    private boolean mStopPreviousRingtone;
    private int mType;

    private class NewRingtoneScanner implements Closeable, MediaScannerConnectionClient {
        private File mFile;
        private MediaScannerConnection mMediaScannerConnection;
        private LinkedBlockingQueue<Uri> mQueue = new LinkedBlockingQueue(1);

        public NewRingtoneScanner(File file) {
            this.mFile = file;
            this.mMediaScannerConnection = new MediaScannerConnection(RingtoneManager.this.mContext, this);
            this.mMediaScannerConnection.connect();
        }

        public void close() {
            this.mMediaScannerConnection.disconnect();
        }

        public void onMediaScannerConnected() {
            this.mMediaScannerConnection.scanFile(this.mFile.getAbsolutePath(), null);
        }

        public void onScanCompleted(String path, Uri uri) {
            if (uri == null) {
                if (!this.mFile.delete()) {
                    Log.w(RingtoneManager.TAG, "Delete copied file failed when scan completed");
                }
                return;
            }
            try {
                this.mQueue.put(uri);
            } catch (InterruptedException e) {
                Log.e(RingtoneManager.TAG, "Unable to put new ringtone Uri in queue", e);
            }
        }

        public Uri take() throws InterruptedException {
            return (Uri) this.mQueue.take();
        }
    }

    public RingtoneManager(Activity activity) {
        this(activity, false);
    }

    public RingtoneManager(Activity activity, boolean includeParentRingtones) {
        this.mType = 1;
        this.mFilterColumns = new ArrayList();
        this.mStopPreviousRingtone = true;
        this.mActivity = activity;
        this.mContext = activity;
        setType(this.mType);
        this.mIncludeParentRingtones = includeParentRingtones;
    }

    public RingtoneManager(Context context) {
        this(context, false);
    }

    public RingtoneManager(Context context, boolean includeParentRingtones) {
        this.mType = 1;
        this.mFilterColumns = new ArrayList();
        this.mStopPreviousRingtone = true;
        this.mActivity = null;
        this.mContext = context;
        setType(this.mType);
        this.mIncludeParentRingtones = includeParentRingtones;
    }

    public void setType(int type) {
        if (this.mCursor != null) {
            throw new IllegalStateException("Setting filter columns should be done before querying for ringtones.");
        }
        this.mType = type;
        setFilterColumnsList(type);
    }

    public int inferStreamType() {
        switch (this.mType) {
            case 2:
                return 5;
            case 4:
                return 4;
            default:
                return 2;
        }
    }

    public void setStopPreviousRingtone(boolean stopPreviousRingtone) {
        this.mStopPreviousRingtone = stopPreviousRingtone;
    }

    public boolean getStopPreviousRingtone() {
        return this.mStopPreviousRingtone;
    }

    public void stopPreviousRingtone() {
        if (this.mPreviousRingtone != null) {
            this.mPreviousRingtone.stop();
        }
    }

    @Deprecated
    public boolean getIncludeDrm() {
        return false;
    }

    @Deprecated
    public void setIncludeDrm(boolean includeDrm) {
        if (includeDrm) {
            Log.w(TAG, "setIncludeDrm no longer supported");
        }
    }

    public Cursor getCursor() {
        try {
            if (!(this.mCursor == null || (this.mCursor.isClosed() ^ 1) == 0 || !this.mCursor.requery())) {
                return this.mCursor;
            }
        } catch (StaleDataException e) {
            Log.w(TAG, "requery failded: ");
            this.mCursor = null;
        }
        ArrayList<Cursor> ringtoneCursors = new ArrayList();
        ringtoneCursors.add(getInternalRingtones());
        ringtoneCursors.add(getMediaRingtones());
        if (this.mIncludeParentRingtones) {
            Cursor parentRingtonesCursor = getParentProfileRingtones();
            if (parentRingtonesCursor != null) {
                ringtoneCursors.add(parentRingtonesCursor);
            }
        }
        Cursor sortCursor = new SortCursor((Cursor[]) ringtoneCursors.toArray(new Cursor[ringtoneCursors.size()]), "title_key");
        this.mCursor = sortCursor;
        return sortCursor;
    }

    private Cursor getParentProfileRingtones() {
        UserInfo parentInfo = UserManager.get(this.mContext).getProfileParent(this.mContext.getUserId());
        if (!(parentInfo == null || parentInfo.id == this.mContext.getUserId())) {
            Context parentContext = createPackageContextAsUser(this.mContext, parentInfo.id);
            if (parentContext != null) {
                return new ExternalRingtonesCursorWrapper(getMediaRingtones(parentContext), parentInfo.id);
            }
        }
        return null;
    }

    public Ringtone getRingtone(int position) {
        if (this.mStopPreviousRingtone && this.mPreviousRingtone != null) {
            this.mPreviousRingtone.stop();
        }
        this.mPreviousRingtone = getRingtone(this.mContext, getRingtoneUri(position), inferStreamType());
        return this.mPreviousRingtone;
    }

    public Uri getRingtoneUri(int position) {
        try {
            if (this.mCursor == null || (this.mCursor.moveToPosition(position) ^ 1) != 0) {
                return null;
            }
            return getUriFromCursor(this.mCursor);
        } catch (IllegalStateException e) {
            Log.e(TAG, "attempt to re-open an already-closed object");
            return null;
        } catch (StaleDataException staleDataException) {
            Log.e(TAG, "getRingtoneUri -- " + position, staleDataException);
            return null;
        }
    }

    private static Uri getExistingRingtoneUriFromPath(Context context, String path) {
        Throwable th;
        Throwable th2;
        Uri th3 = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{DownloadManager.COLUMN_ID}, "_data=? ", new String[]{path}, null);
            if (cursor == null || (cursor.moveToFirst() ^ 1) != 0) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable th4) {
                        th = th4;
                    }
                }
                th = null;
                if (th == null) {
                    return null;
                }
                throw th;
            }
            int id = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
            if (id == -1) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Throwable th5) {
                        th = th5;
                    }
                }
                th = null;
                if (th == null) {
                    return null;
                }
                throw th;
            }
            Uri withAppendedPath = Uri.withAppendedPath(Media.EXTERNAL_CONTENT_URI, ProxyInfo.LOCAL_EXCL_LIST + id);
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable th6) {
                    th3 = th6;
                }
            }
            if (th3 == null) {
                return withAppendedPath;
            }
            throw th3;
        } catch (Throwable th22) {
            Throwable th7 = th22;
            th22 = th;
            th = th7;
        }
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable th8) {
                if (th22 == null) {
                    th22 = th8;
                } else if (th22 != th8) {
                    th22.addSuppressed(th8);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }

    private static Uri getUriFromCursor(Cursor cursor) {
        Uri uri = null;
        try {
            return ContentUris.withAppendedId(Uri.parse(cursor.getString(2)), cursor.getLong(0));
        } catch (Exception e) {
            Log.e(TAG, "Failed to get uri from cursor!!!!!!!!!!!!");
            return uri;
        }
    }

    public int getRingtonePosition(Uri ringtoneUri) {
        if (ringtoneUri == null) {
            return -1;
        }
        Cursor cursor = getCursor();
        int cursorCount = cursor.getCount();
        if (!cursor.moveToFirst()) {
            return -1;
        }
        Uri currentUri = null;
        Object previousUriString = null;
        for (int i = 0; i < cursorCount; i++) {
            String uriString = cursor.getString(2);
            if (currentUri == null || (uriString.equals(previousUriString) ^ 1) != 0) {
                currentUri = Uri.parse(uriString);
            }
            if (ringtoneUri.equals(ContentUris.withAppendedId(currentUri, cursor.getLong(0)))) {
                return i;
            }
            cursor.move(1);
            String previousUriString2 = uriString;
        }
        return -1;
    }

    public static Uri getValidRingtoneUri(Context context) {
        RingtoneManager rm = new RingtoneManager(context);
        Uri uri = getValidRingtoneUriFromCursorAndClose(context, rm.getInternalRingtones());
        if (uri == null) {
            return getValidRingtoneUriFromCursorAndClose(context, rm.getMediaRingtones());
        }
        return uri;
    }

    private static Uri getValidRingtoneUriFromCursorAndClose(Context context, Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        Uri uri = null;
        if (cursor.moveToFirst()) {
            uri = getUriFromCursor(cursor);
        }
        cursor.close();
        return uri;
    }

    private Cursor getInternalRingtones() {
        return query(Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS, constructBooleanTrueWhereClause(this.mFilterColumns), null, "title_key");
    }

    private Cursor getMediaRingtones() {
        return getMediaRingtones(this.mContext);
    }

    private Cursor getMediaRingtones(Context context) {
        Cursor cursor = null;
        if (context.checkPermission(permission.READ_EXTERNAL_STORAGE, Process.myPid(), Process.myUid()) != 0) {
            Log.w(TAG, "No READ_EXTERNAL_STORAGE permission, ignoring ringtones on ext storage");
            return null;
        }
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED) || status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            cursor = query(Media.EXTERNAL_CONTENT_URI, MEDIA_COLUMNS, constructBooleanTrueWhereClause(this.mFilterColumns), null, "title_key", context);
        }
        return cursor;
    }

    private void setFilterColumnsList(int type) {
        List<String> columns = this.mFilterColumns;
        columns.clear();
        if ((type & 1) != 0) {
            columns.add("is_ringtone");
        }
        if ((type & 8) != 0) {
            columns.add("is_ringtone");
        }
        if ((type & 2) != 0) {
            columns.add("is_notification");
        }
        if ((type & 4) != 0) {
            columns.add("is_alarm");
        }
    }

    private static String constructBooleanTrueWhereClause(List<String> columns) {
        if (columns == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = columns.size() - 1; i >= 0; i--) {
            sb.append((String) columns.get(i)).append("=1 or ");
        }
        if (columns.size() > 0) {
            sb.setLength(sb.length() - 4);
        }
        sb.append(" and is_drm =0)");
        return sb.toString();
    }

    private Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return query(uri, projection, selection, selectionArgs, sortOrder, this.mContext);
    }

    private Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, Context context) {
        if (this.mActivity != null) {
            return this.mActivity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
        }
        return context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public static Ringtone getRingtone(Context context, Uri ringtoneUri) {
        return getRingtone(context, ringtoneUri, -1);
    }

    private static Ringtone getRingtone(Context context, Uri ringtoneUri, int streamType) {
        try {
            mSetUriStat = false;
            Ringtone r = new Ringtone(context, true);
            if (streamType >= 0) {
                r.setStreamType(streamType);
            }
            if (ringtoneUri == null) {
                Log.w(TAG, "ringtoneUri is null ...");
                return null;
            }
            r.setUri(ringtoneUri);
            if (r.getPrepareStat()) {
                Log.w(TAG, "prepare failed ......");
                mSetUriStat = true;
            }
            return r;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to open ringtone " + ringtoneUri + ": " + ex);
            return null;
        }
    }

    private File getRingtonePathFromUri(Uri uri) {
        Throwable th;
        Throwable th2;
        String[] projection = new String[]{"_data"};
        setFilterColumnsList(7);
        String path = null;
        Cursor cursor = null;
        try {
            cursor = query(uri, projection, constructBooleanTrueWhereClause(this.mFilterColumns), null, null);
            if (cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex("_data"));
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable th3) {
                    th = th3;
                }
            }
            th = null;
            if (th != null) {
                throw th;
            } else if (path != null) {
                return new File(path);
            } else {
                return null;
            }
        } catch (Throwable th22) {
            Throwable th4 = th22;
            th22 = th;
            th = th4;
        }
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable th5) {
                if (th22 == null) {
                    th22 = th5;
                } else if (th22 != th5) {
                    th22.addSuppressed(th5);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }

    public static boolean getSetUriStat() {
        return mSetUriStat;
    }

    public static void disableSyncFromParent(Context userContext) {
        try {
            Stub.asInterface(ServiceManager.getService("audio")).disableRingtoneSync(userContext.getUserId());
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to disable ringtone sync.");
        }
    }

    public static void enableSyncFromParent(Context userContext) {
        Secure.putIntForUser(userContext.getContentResolver(), "sync_parent_sounds", 1, userContext.getUserId());
    }

    public static Uri getActualDefaultRingtoneUri(Context context, int type) {
        String setting = getSettingForType(type);
        if (setting == null) {
            return null;
        }
        String uriString = System.getStringForUser(context.getContentResolver(), setting, context.getUserId());
        Uri ringtoneUri = uriString != null ? Uri.parse(uriString) : null;
        if (ringtoneUri != null && ContentProvider.getUserIdFromUri(ringtoneUri) == context.getUserId()) {
            ringtoneUri = ContentProvider.getUriWithoutUserId(ringtoneUri);
        }
        return ringtoneUri;
    }

    public static void setActualDefaultRingtoneUri(Context context, int type, Uri ringtoneUri) {
        InputStream inputStream;
        OutputStream outputStream;
        Throwable th;
        Throwable th2;
        Throwable th3 = null;
        String setting = getSettingForType(type);
        if (setting != null) {
            String uri;
            ContentResolver resolver = context.getContentResolver();
            if (Secure.getIntForUser(resolver, "sync_parent_sounds", 0, context.getUserId()) == 1) {
                disableSyncFromParent(context);
            }
            if (!isInternalRingtoneUri(ringtoneUri)) {
                ringtoneUri = ContentProvider.maybeAddUserId(ringtoneUri, context.getUserId());
            }
            if (ringtoneUri != null) {
                uri = ringtoneUri.toString();
            } else {
                uri = null;
            }
            System.putStringForUser(resolver, setting, uri, context.getUserId());
            if (ringtoneUri != null) {
                String actualUri = MediaStore.getPath(context, ringtoneUri);
                if (actualUri == null || !actualUri.endsWith(".isma")) {
                    Uri cacheUri = getCacheForType(type, context.getUserId());
                    inputStream = null;
                    outputStream = null;
                    try {
                        inputStream = openRingtone(context, ringtoneUri);
                        outputStream = resolver.openOutputStream(cacheUri);
                        Streams.copy(inputStream, outputStream);
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (Throwable th4) {
                                th3 = th4;
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th5) {
                                th = th5;
                                if (th3 != null) {
                                    if (th3 != th) {
                                        th3.addSuppressed(th);
                                        th = th3;
                                    }
                                }
                            }
                        }
                        th = th3;
                        if (th != null) {
                            try {
                                throw th;
                            } catch (IOException e) {
                                Log.w(TAG, "Failed to cache ringtone: " + e);
                            }
                        }
                    } catch (Throwable th32) {
                        Throwable th6 = th32;
                        th32 = th;
                        th = th6;
                    }
                } else {
                    Log.d(TAG, "setActualDefaultRingtoneUri actualUri = " + actualUri);
                    Toast.makeText(context, 17040905, 1).show();
                    return;
                }
            }
            return;
        }
        return;
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Throwable th7) {
                th2 = th7;
                if (th32 != null) {
                    if (th32 != th2) {
                        th32.addSuppressed(th2);
                        th2 = th32;
                    }
                }
            }
        }
        th2 = th32;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable th8) {
                th32 = th8;
                if (th2 != null) {
                    if (th2 != th32) {
                        th2.addSuppressed(th32);
                        th32 = th2;
                    }
                }
            }
        }
        th32 = th2;
        if (th32 != null) {
            throw th32;
        }
        throw th;
    }

    private static boolean isInternalRingtoneUri(Uri uri) {
        return isRingtoneUriInStorage(uri, Media.INTERNAL_CONTENT_URI);
    }

    private static boolean isExternalRingtoneUri(Uri uri) {
        return isRingtoneUriInStorage(uri, Media.EXTERNAL_CONTENT_URI);
    }

    private static boolean isRingtoneUriInStorage(Uri ringtone, Uri storage) {
        Uri uriWithoutUserId = ContentProvider.getUriWithoutUserId(ringtone);
        if (uriWithoutUserId == null) {
            return false;
        }
        return uriWithoutUserId.toString().startsWith(storage.toString());
    }

    public boolean isCustomRingtone(Uri uri) {
        if (!isExternalRingtoneUri(uri)) {
            return false;
        }
        File ringtoneFile = uri == null ? null : getRingtonePathFromUri(uri);
        File parent = ringtoneFile == null ? null : ringtoneFile.getParentFile();
        if (parent == null) {
            return false;
        }
        for (String directory : new String[]{Environment.DIRECTORY_RINGTONES, Environment.DIRECTORY_NOTIFICATIONS, Environment.DIRECTORY_ALARMS}) {
            if (parent.equals(Environment.getExternalStoragePublicDirectory(directory))) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x00a1 A:{SYNTHETIC, Splitter: B:40:0x00a1} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a7 A:{SYNTHETIC, Splitter: B:44:0x00a7} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00c2  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x00eb A:{SYNTHETIC, Splitter: B:81:0x00eb} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x00fe A:{Catch:{ InterruptedException -> 0x00f1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x00f0 A:{SYNTHETIC, Splitter: B:84:0x00f0} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Uri addCustomExternalRingtone(Uri fileUri, int type) throws FileNotFoundException, IllegalArgumentException, IOException {
        Throwable th;
        Throwable th2;
        InterruptedException e;
        Throwable th3;
        Throwable th4 = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String mimeType = this.mContext.getContentResolver().getType(fileUri);
            if (mimeType != null) {
                if (((!mimeType.startsWith("audio/") ? mimeType.equals("application/ogg") : 1) ^ 1) == 0) {
                    File outFile = Utils.getUniqueExternalFile(this.mContext, getExternalDirectoryForType(type), Utils.getFileDisplayNameFromUri(this.mContext, fileUri), mimeType);
                    InputStream inputStream = null;
                    OutputStream output = null;
                    try {
                        inputStream = this.mContext.getContentResolver().openInputStream(fileUri);
                        OutputStream output2 = new FileOutputStream(outFile);
                        try {
                            Streams.copy(inputStream, output2);
                            if (output2 != null) {
                                try {
                                    output2.close();
                                } catch (Throwable th5) {
                                    th = th5;
                                }
                            }
                            th = null;
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (Throwable th6) {
                                    th2 = th6;
                                    if (th != null) {
                                        if (th != th2) {
                                            th.addSuppressed(th2);
                                            th2 = th;
                                        }
                                    }
                                }
                            }
                            th2 = th;
                            if (th2 != null) {
                                throw th2;
                            }
                            NewRingtoneScanner scanner = null;
                            try {
                                NewRingtoneScanner scanner2 = new NewRingtoneScanner(outFile);
                                try {
                                    Uri take = scanner2.take();
                                    if (scanner2 != null) {
                                        try {
                                            scanner2.close();
                                        } catch (Throwable th7) {
                                            th4 = th7;
                                        }
                                    }
                                    if (th4 == null) {
                                        return take;
                                    }
                                    try {
                                        throw th4;
                                    } catch (InterruptedException e2) {
                                        e = e2;
                                        scanner = scanner2;
                                    }
                                } catch (Throwable th8) {
                                    th2 = th8;
                                    scanner = scanner2;
                                    if (scanner != null) {
                                        try {
                                            scanner.close();
                                        } catch (Throwable th9) {
                                            if (th4 == null) {
                                                th4 = th9;
                                            } else if (th4 != th9) {
                                                th4.addSuppressed(th9);
                                            }
                                        }
                                    }
                                    if (th4 == null) {
                                        try {
                                            throw th4;
                                        } catch (InterruptedException e3) {
                                            e = e3;
                                            throw new IOException("Audio file failed to scan as a ringtone", e);
                                        }
                                    }
                                    throw th2;
                                }
                            } catch (Throwable th10) {
                                th2 = th10;
                                if (scanner != null) {
                                }
                                if (th4 == null) {
                                }
                            }
                        } catch (Throwable th11) {
                            th2 = th11;
                            output = output2;
                            if (output != null) {
                            }
                            th9 = th4;
                            if (inputStream != null) {
                            }
                            th4 = th9;
                            if (th4 == null) {
                            }
                        }
                    } catch (Throwable th12) {
                        th2 = th12;
                        if (output != null) {
                            try {
                                output.close();
                            } catch (Throwable th13) {
                                th9 = th13;
                                if (th4 != null) {
                                    if (th4 != th9) {
                                        th4.addSuppressed(th9);
                                        th9 = th4;
                                    }
                                }
                            }
                        }
                        th9 = th4;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th14) {
                                th4 = th14;
                                if (th9 != null) {
                                    if (th9 != th4) {
                                        th9.addSuppressed(th4);
                                        th4 = th9;
                                    }
                                }
                            }
                        }
                        th4 = th9;
                        if (th4 == null) {
                            throw th4;
                        }
                        throw th2;
                    }
                }
            }
            throw new IllegalArgumentException("Ringtone file must have MIME type \"audio/*\". Given file has MIME type \"" + mimeType + "\"");
        }
        throw new IOException("External storage is not mounted. Unable to install ringtones.");
    }

    private static final String getExternalDirectoryForType(int type) {
        switch (type) {
            case 1:
                return Environment.DIRECTORY_RINGTONES;
            case 2:
                return Environment.DIRECTORY_NOTIFICATIONS;
            case 4:
                return Environment.DIRECTORY_ALARMS;
            default:
                throw new IllegalArgumentException("Unsupported ringtone type: " + type);
        }
    }

    public boolean deleteExternalRingtone(Uri uri) {
        if (!isCustomRingtone(uri)) {
            return false;
        }
        File ringtoneFile = getRingtonePathFromUri(uri);
        if (ringtoneFile != null) {
            try {
                if (this.mContext.getContentResolver().delete(uri, null, null) > 0) {
                    return ringtoneFile.delete();
                }
            } catch (SecurityException e) {
                Log.d(TAG, "Unable to delete custom ringtone", e);
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0009 A:{ExcHandler: java.lang.SecurityException (r0_0 'e' java.lang.Exception), Splitter: B:1:0x0004} */
    /* JADX WARNING: Missing block: B:4:0x0009, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:5:0x000a, code:
            android.util.Log.w(TAG, "Failed to open directly; attempting failover: " + r0);
     */
    /* JADX WARNING: Missing block: B:8:0x0039, code:
            return new android.os.ParcelFileDescriptor.AutoCloseInputStream(((android.media.AudioManager) r7.getSystemService(android.media.AudioManager.class)).getRingtonePlayer().openRingtone(r8));
     */
    /* JADX WARNING: Missing block: B:9:0x003a, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x0040, code:
            throw new java.io.IOException(r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static InputStream openRingtone(Context context, Uri uri) throws IOException {
        try {
            return context.getContentResolver().openInputStream(uri);
        } catch (Exception e) {
        }
    }

    private static String getSettingForType(int type) {
        if ((type & 1) != 0) {
            return "ringtone";
        }
        if ((type & 8) != 0) {
            return "ringtone2";
        }
        if ((type & 2) != 0) {
            return "notification_sound";
        }
        if ((type & 4) != 0) {
            return "alarm_alert";
        }
        return null;
    }

    public static Uri getCacheForType(int type) {
        return getCacheForType(type, UserHandle.getCallingUserId());
    }

    public static Uri getCacheForType(int type, int userId) {
        if ((type & 1) != 0) {
            return ContentProvider.maybeAddUserId(System.RINGTONE_CACHE_URI, userId);
        }
        if ((type & 8) != 0) {
            return ContentProvider.maybeAddUserId(System.RINGTONE2_CACHE_URI, userId);
        }
        if ((type & 2) != 0) {
            return ContentProvider.maybeAddUserId(System.NOTIFICATION_SOUND_CACHE_URI, userId);
        }
        if ((type & 4) != 0) {
            return ContentProvider.maybeAddUserId(System.ALARM_ALERT_CACHE_URI, userId);
        }
        return null;
    }

    public static boolean isDefault(Uri ringtoneUri) {
        return getDefaultType(ringtoneUri) != -1;
    }

    public static int getDefaultType(Uri defaultRingtoneUri) {
        defaultRingtoneUri = ContentProvider.getUriWithoutUserId(defaultRingtoneUri);
        if (defaultRingtoneUri == null) {
            return -1;
        }
        if (defaultRingtoneUri.equals(System.HUAWEI_RINGTONE2_URI)) {
            return 8;
        }
        if (defaultRingtoneUri.equals(System.DEFAULT_RINGTONE_URI)) {
            return 1;
        }
        if (defaultRingtoneUri.equals(System.DEFAULT_NOTIFICATION_URI)) {
            return 2;
        }
        if (defaultRingtoneUri.equals(System.DEFAULT_ALARM_ALERT_URI)) {
            return 4;
        }
        return -1;
    }

    public static Uri getDefaultUri(int type) {
        if ((type & 1) != 0) {
            return System.DEFAULT_RINGTONE_URI;
        }
        if ((type & 8) != 0) {
            return System.HUAWEI_RINGTONE2_URI;
        }
        if ((type & 2) != 0) {
            return System.DEFAULT_NOTIFICATION_URI;
        }
        if ((type & 4) != 0) {
            return System.DEFAULT_ALARM_ALERT_URI;
        }
        return null;
    }

    private static Context createPackageContextAsUser(Context context, int userId) {
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, UserHandle.of(userId));
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unable to create package context", e);
            return null;
        }
    }
}
