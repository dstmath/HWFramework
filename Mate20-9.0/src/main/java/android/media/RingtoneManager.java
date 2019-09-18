package android.media;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.StaleDataException;
import android.media.IAudioService;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.R;
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
    private static final int[] HW_RINGTONE_TYPES = {1, 2, 4, 8};
    public static final int HW_TYPE_ALL = 15;
    public static final int ID_COLUMN_INDEX = 0;
    private static final String[] INTERNAL_COLUMNS = {DownloadManager.COLUMN_ID, "title", "\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\"", "title_key"};
    private static final String[] MEDIA_COLUMNS = {DownloadManager.COLUMN_ID, "title", "\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\"", "title_key"};
    private static final int[] RINGTONE_TYPES = {1, 2, 4};
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
    /* access modifiers changed from: private */
    public final Context mContext;
    private Cursor mCursor;
    private final List<String> mFilterColumns;
    private boolean mIncludeParentRingtones;
    private Ringtone mPreviousRingtone;
    private boolean mStopPreviousRingtone;
    private int mType;

    private class NewRingtoneScanner implements Closeable, MediaScannerConnection.MediaScannerConnectionClient {
        private File mFile;
        private MediaScannerConnection mMediaScannerConnection;
        private LinkedBlockingQueue<Uri> mQueue = new LinkedBlockingQueue<>(1);

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
            return this.mQueue.take();
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
        if (this.mCursor == null) {
            this.mType = type;
            setFilterColumnsList(type);
            return;
        }
        throw new IllegalStateException("Setting filter columns should be done before querying for ringtones.");
    }

    public int inferStreamType() {
        int i = this.mType;
        if (i != 2) {
            return i != 4 ? 2 : 4;
        }
        return 5;
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
            if (this.mCursor != null && !this.mCursor.isClosed() && this.mCursor.requery()) {
                return this.mCursor;
            }
        } catch (StaleDataException e) {
            Log.w(TAG, "requery failded: ");
            this.mCursor = null;
        }
        ArrayList<Cursor> ringtoneCursors = new ArrayList<>();
        ringtoneCursors.add(getInternalRingtones());
        ringtoneCursors.add(getMediaRingtones());
        if (this.mIncludeParentRingtones) {
            Cursor parentRingtonesCursor = getParentProfileRingtones();
            if (parentRingtonesCursor != null) {
                ringtoneCursors.add(parentRingtonesCursor);
            }
        }
        SortCursor sortCursor = new SortCursor((Cursor[]) ringtoneCursors.toArray(new Cursor[ringtoneCursors.size()]), "title_key");
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
            if (this.mCursor == null || !this.mCursor.moveToPosition(position)) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0054, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0058, code lost:
        if (r0 != null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005a, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005d, code lost:
        throw r2;
     */
    private static Uri getExistingRingtoneUriFromPath(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{DownloadManager.COLUMN_ID}, "_data=? ", new String[]{path}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                if (id == -1) {
                    if (cursor != null) {
                        $closeResource(null, cursor);
                    }
                    return null;
                }
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                Uri withAppendedPath = Uri.withAppendedPath(uri, "" + id);
                if (cursor != null) {
                    $closeResource(null, cursor);
                }
                return withAppendedPath;
            }
        }
        if (cursor != null) {
            $closeResource(null, cursor);
        }
        return null;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private static Uri getUriFromCursor(Cursor cursor) {
        try {
            return ContentUris.withAppendedId(Uri.parse(cursor.getString(2)), cursor.getLong(0));
        } catch (Exception e) {
            Log.e(TAG, "Failed to get uri from cursor!!!!!!!!!!!!");
            return null;
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
        String previousUriString = null;
        Uri currentUri = null;
        for (int i = 0; i < cursorCount; i++) {
            String uriString = cursor.getString(2);
            if (currentUri == null || !uriString.equals(previousUriString)) {
                currentUri = Uri.parse(uriString);
            }
            if (ringtoneUri.equals(ContentUris.withAppendedId(currentUri, cursor.getLong(0)))) {
                return i;
            }
            cursor.move(1);
            previousUriString = uriString;
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
        return query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS, constructBooleanTrueWhereClause(this.mFilterColumns), null, "title_key");
    }

    private Cursor getMediaRingtones() {
        return getMediaRingtones(this.mContext);
    }

    private Cursor getMediaRingtones(Context context) {
        Cursor cursor = null;
        if (context.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Process.myPid(), Process.myUid()) != 0) {
            Log.w(TAG, "No READ_EXTERNAL_STORAGE permission, ignoring ringtones on ext storage");
            return null;
        }
        String status = Environment.getExternalStorageState();
        if (status.equals("mounted") || status.equals("mounted_ro")) {
            cursor = query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MEDIA_COLUMNS, constructBooleanTrueWhereClause(this.mFilterColumns), null, "title_key", context);
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
            sb.append(columns.get(i));
            sb.append("=1 or ");
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

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0032, code lost:
        if (r1 != null) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0034, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0037, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x002e, code lost:
        r4 = move-exception;
     */
    private File getRingtonePathFromUri(Uri uri) {
        setFilterColumnsList(7);
        String path = null;
        Uri uri2 = uri;
        Cursor cursor = query(uri2, new String[]{"_data"}, constructBooleanTrueWhereClause(this.mFilterColumns), null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex("_data"));
            }
        }
        if (cursor != null) {
            $closeResource(null, cursor);
        }
        if (path != null) {
            return new File(path);
        }
        return null;
    }

    public static boolean getSetUriStat() {
        return mSetUriStat;
    }

    public static void disableSyncFromParent(Context userContext) {
        try {
            IAudioService.Stub.asInterface(ServiceManager.getService("audio")).disableRingtoneSync(userContext.getUserId());
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to disable ringtone sync.");
        }
    }

    public static void enableSyncFromParent(Context userContext) {
        Settings.Secure.putIntForUser(userContext.getContentResolver(), "sync_parent_sounds", 1, userContext.getUserId());
    }

    public static Uri getActualDefaultRingtoneUri(Context context, int type) {
        String setting = getSettingForType(type);
        Uri ringtoneUri = null;
        if (setting == null) {
            return null;
        }
        String uriString = Settings.System.getStringForUser(context.getContentResolver(), setting, context.getUserId());
        if (uriString != null) {
            ringtoneUri = Uri.parse(uriString);
        }
        if (ringtoneUri != null && ContentProvider.getUserIdFromUri(ringtoneUri) == context.getUserId()) {
            ringtoneUri = ContentProvider.getUriWithoutUserId(ringtoneUri);
        }
        return ringtoneUri;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x008d, code lost:
        r7 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008e, code lost:
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0092, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0093, code lost:
        r9 = r8;
        r8 = r7;
        r7 = r9;
     */
    public static void setActualDefaultRingtoneUri(Context context, int type, Uri ringtoneUri) {
        InputStream in;
        OutputStream out;
        Throwable th;
        Throwable th2;
        String setting = getSettingForType(type);
        if (setting != null) {
            ContentResolver resolver = context.getContentResolver();
            if (Settings.Secure.getIntForUser(resolver, "sync_parent_sounds", 0, context.getUserId()) == 1) {
                disableSyncFromParent(context);
            }
            if (!isInternalRingtoneUri(ringtoneUri)) {
                ringtoneUri = ContentProvider.maybeAddUserId(ringtoneUri, context.getUserId());
            }
            Settings.System.putStringForUser(resolver, setting, ringtoneUri != null ? ringtoneUri.toString() : null, context.getUserId());
            if (ringtoneUri != null) {
                String actualUri = MediaStore.getPath(context, ringtoneUri);
                if (actualUri == null || !actualUri.endsWith(".isma")) {
                    Uri cacheUri = getCacheForType(type, context.getUserId());
                    try {
                        in = openRingtone(context, ringtoneUri);
                        out = resolver.openOutputStream(cacheUri);
                        FileUtils.copy(in, out);
                        if (out != null) {
                            $closeResource(null, out);
                        }
                        if (in != null) {
                            $closeResource(null, in);
                        }
                    } catch (IOException e) {
                        Log.w(TAG, "Failed to cache ringtone: " + e);
                    } catch (Throwable th3) {
                        if (in != null) {
                            $closeResource(r2, in);
                        }
                        throw th3;
                    }
                } else {
                    Log.d(TAG, "setActualDefaultRingtoneUri actualUri = " + actualUri);
                    Toast.makeText(context, R.string.ringtone_unknown, 1).show();
                    return;
                }
            }
            return;
        }
        return;
        if (out != null) {
            $closeResource(th, out);
        }
        throw th2;
    }

    private static boolean isInternalRingtoneUri(Uri uri) {
        return isRingtoneUriInStorage(uri, MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
    }

    private static boolean isExternalRingtoneUri(Uri uri) {
        return isRingtoneUriInStorage(uri, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
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
        File parent = null;
        File ringtoneFile = uri == null ? null : getRingtonePathFromUri(uri);
        if (ringtoneFile != null) {
            parent = ringtoneFile.getParentFile();
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0073, code lost:
        r6 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0074, code lost:
        r7 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0078, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0079, code lost:
        r8 = r7;
        r7 = r6;
        r6 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0080, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0084, code lost:
        if (r3 != null) goto L_0x0086;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0086, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0089, code lost:
        throw r5;
     */
    public Uri addCustomExternalRingtone(Uri fileUri, int type) throws FileNotFoundException, IllegalArgumentException, IOException {
        OutputStream output;
        Throwable th;
        Throwable th2;
        NewRingtoneScanner scanner;
        if (Environment.getExternalStorageState().equals("mounted")) {
            String mimeType = this.mContext.getContentResolver().getType(fileUri);
            if (mimeType == null || (!mimeType.startsWith("audio/") && !mimeType.equals("application/ogg"))) {
                throw new IllegalArgumentException("Ringtone file must have MIME type \"audio/*\". Given file has MIME type \"" + mimeType + "\"");
            }
            File outFile = Utils.getUniqueExternalFile(this.mContext, getExternalDirectoryForType(type), Utils.getFileDisplayNameFromUri(this.mContext, fileUri), mimeType);
            InputStream input = this.mContext.getContentResolver().openInputStream(fileUri);
            output = new FileOutputStream(outFile);
            FileUtils.copy(input, output);
            $closeResource(null, output);
            if (input != null) {
                $closeResource(null, input);
            }
            try {
                scanner = new NewRingtoneScanner(outFile);
                Uri take = scanner.take();
                $closeResource(null, scanner);
                return take;
            } catch (InterruptedException e) {
                throw new IOException("Audio file failed to scan as a ringtone", e);
            } catch (Throwable th3) {
                $closeResource(r4, scanner);
                throw th3;
            }
        } else {
            throw new IOException("External storage is not mounted. Unable to install ringtones.");
        }
        $closeResource(th, output);
        throw th2;
    }

    private static final String getExternalDirectoryForType(int type) {
        if (type == 4) {
            return Environment.DIRECTORY_ALARMS;
        }
        switch (type) {
            case 1:
                return Environment.DIRECTORY_RINGTONES;
            case 2:
                return Environment.DIRECTORY_NOTIFICATIONS;
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

    private static InputStream openRingtone(Context context, Uri uri) throws IOException {
        try {
            return context.getContentResolver().openInputStream(uri);
        } catch (IOException | SecurityException e) {
            Log.w(TAG, "Failed to open directly; attempting failover: " + e);
            try {
                return new ParcelFileDescriptor.AutoCloseInputStream(((AudioManager) context.getSystemService(AudioManager.class)).getRingtonePlayer().openRingtone(uri));
            } catch (Exception e2) {
                throw new IOException(e2);
            }
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
            return ContentProvider.maybeAddUserId(Settings.System.RINGTONE_CACHE_URI, userId);
        }
        if ((type & 8) != 0) {
            return ContentProvider.maybeAddUserId(Settings.System.RINGTONE2_CACHE_URI, userId);
        }
        if ((type & 2) != 0) {
            return ContentProvider.maybeAddUserId(Settings.System.NOTIFICATION_SOUND_CACHE_URI, userId);
        }
        if ((type & 4) != 0) {
            return ContentProvider.maybeAddUserId(Settings.System.ALARM_ALERT_CACHE_URI, userId);
        }
        return null;
    }

    public static boolean isDefault(Uri ringtoneUri) {
        return getDefaultType(ringtoneUri) != -1;
    }

    public static int getDefaultType(Uri defaultRingtoneUri) {
        Uri defaultRingtoneUri2 = ContentProvider.getUriWithoutUserId(defaultRingtoneUri);
        if (defaultRingtoneUri2 == null) {
            return -1;
        }
        if (defaultRingtoneUri2.equals(Settings.System.HUAWEI_RINGTONE2_URI)) {
            return 8;
        }
        if (defaultRingtoneUri2.equals(Settings.System.DEFAULT_RINGTONE_URI)) {
            return 1;
        }
        if (defaultRingtoneUri2.equals(Settings.System.DEFAULT_NOTIFICATION_URI)) {
            return 2;
        }
        if (defaultRingtoneUri2.equals(Settings.System.DEFAULT_ALARM_ALERT_URI)) {
            return 4;
        }
        return -1;
    }

    public static Uri getDefaultUri(int type) {
        if ((type & 1) != 0) {
            return Settings.System.DEFAULT_RINGTONE_URI;
        }
        if ((type & 8) != 0) {
            return Settings.System.HUAWEI_RINGTONE2_URI;
        }
        if ((type & 2) != 0) {
            return Settings.System.DEFAULT_NOTIFICATION_URI;
        }
        if ((type & 4) != 0) {
            return Settings.System.DEFAULT_ALARM_ALERT_URI;
        }
        return null;
    }

    private static Context createPackageContextAsUser(Context context, int userId) {
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, UserHandle.of(userId));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to create package context", e);
            return null;
        }
    }
}
