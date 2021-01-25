package android.media;

import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.StaleDataException;
import android.media.IAudioService;
import android.media.VolumeShaper;
import android.net.Uri;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.database.SortCursor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
    public static final int ID_COLUMN_INDEX = 0;
    private static final String[] INTERNAL_COLUMNS = {"_id", "title", "title", "title_key"};
    private static final String[] MEDIA_COLUMNS = {"_id", "title", "title", "title_key"};
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
    @UnsupportedAppUsage
    private Cursor mCursor;
    private final List<String> mFilterColumns;
    private boolean mIncludeParentRingtones;
    private Ringtone mPreviousRingtone;
    private boolean mStopPreviousRingtone;
    private int mType;

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
        Ringtone ringtone = this.mPreviousRingtone;
        if (ringtone != null) {
            ringtone.stop();
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
        Cursor parentRingtonesCursor;
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
        if (this.mIncludeParentRingtones && (parentRingtonesCursor = getParentProfileRingtones()) != null) {
            ringtoneCursors.add(parentRingtonesCursor);
        }
        SortCursor sortCursor = new SortCursor((Cursor[]) ringtoneCursors.toArray(new Cursor[ringtoneCursors.size()]), "title_key");
        this.mCursor = sortCursor;
        return sortCursor;
    }

    private Cursor getParentProfileRingtones() {
        Context parentContext;
        UserInfo parentInfo = UserManager.get(this.mContext).getProfileParent(this.mContext.getUserId());
        if (parentInfo == null || parentInfo.id == this.mContext.getUserId() || (parentContext = createPackageContextAsUser(this.mContext, parentInfo.id)) == null) {
            return null;
        }
        return new ExternalRingtonesCursorWrapper(getMediaRingtones(parentContext), ContentProvider.maybeAddUserId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, parentInfo.id));
    }

    public Ringtone getRingtone(int position) {
        Ringtone ringtone;
        if (this.mStopPreviousRingtone && (ringtone = this.mPreviousRingtone) != null) {
            ringtone.stop();
        }
        this.mPreviousRingtone = getRingtone(this.mContext, getRingtoneUri(position), inferStreamType());
        return this.mPreviousRingtone;
    }

    public Uri getRingtoneUri(int position) {
        Cursor cursor = this.mCursor;
        if (cursor == null || !cursor.moveToPosition(position)) {
            return null;
        }
        return getUriFromCursor(this.mContext, this.mCursor);
    }

    private static Uri getUriFromCursor(Context context, Cursor cursor) {
        return context.getContentResolver().canonicalizeOrElse(ContentUris.withAppendedId(Uri.parse(cursor.getString(2)), cursor.getLong(0)));
    }

    public int getRingtonePosition(Uri ringtoneUri) {
        if (ringtoneUri == null) {
            return -1;
        }
        long ringtoneId = ContentUris.parseId(ringtoneUri);
        Cursor cursor = getCursor();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            if (ringtoneId == cursor.getLong(0)) {
                return cursor.getPosition();
            }
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
            uri = getUriFromCursor(context, cursor);
        }
        cursor.close();
        return uri;
    }

    @UnsupportedAppUsage
    private Cursor getInternalRingtones() {
        return new ExternalRingtonesCursorWrapper(query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS, constructBooleanTrueWhereClause(this.mFilterColumns), null, "title_key"), MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
    }

    private Cursor getMediaRingtones() {
        return new ExternalRingtonesCursorWrapper(getMediaRingtones(this.mContext), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    @UnsupportedAppUsage
    private Cursor getMediaRingtones(Context context) {
        return query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MEDIA_COLUMNS, constructBooleanTrueWhereClause(this.mFilterColumns), null, "title_key", context);
    }

    private void setFilterColumnsList(int type) {
        List<String> columns = this.mFilterColumns;
        columns.clear();
        if ((type & 1) != 0) {
            columns.add(MediaStore.Audio.AudioColumns.IS_RINGTONE);
        }
        if ((type & 8) != 0) {
            columns.add(MediaStore.Audio.AudioColumns.IS_RINGTONE);
        }
        if ((type & 2) != 0) {
            columns.add(MediaStore.Audio.AudioColumns.IS_NOTIFICATION);
        }
        if ((type & 4) != 0) {
            columns.add(MediaStore.Audio.AudioColumns.IS_ALARM);
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
        sb.append(")");
        return sb.toString();
    }

    private Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return query(uri, projection, selection, selectionArgs, sortOrder, this.mContext);
    }

    private Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, Context context) {
        Activity activity = this.mActivity;
        if (activity != null) {
            return activity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
        }
        return context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public static Ringtone getRingtone(Context context, Uri ringtoneUri) {
        return getRingtone(context, ringtoneUri, -1);
    }

    public static Ringtone getRingtone(Context context, Uri ringtoneUri, VolumeShaper.Configuration volumeShaperConfig) {
        return getRingtone(context, ringtoneUri, -1, volumeShaperConfig);
    }

    @UnsupportedAppUsage
    private static Ringtone getRingtone(Context context, Uri ringtoneUri, int streamType) {
        return getRingtone(context, ringtoneUri, streamType, null);
    }

    @UnsupportedAppUsage
    private static Ringtone getRingtone(Context context, Uri ringtoneUri, int streamType, VolumeShaper.Configuration volumeShaperConfig) {
        try {
            mSetUriStat = false;
            Ringtone r = new Ringtone(context, true);
            if (streamType >= 0) {
                r.setStreamType(streamType);
            }
            r.setUri(ringtoneUri, volumeShaperConfig);
            if (r.getPrepareStat()) {
                Log.w(TAG, "detect ringtone prepare failed");
                mSetUriStat = true;
            }
            return r;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to open ringtone : " + ex);
            return null;
        }
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
        Settings.Secure.putIntForUser(userContext.getContentResolver(), Settings.Secure.SYNC_PARENT_SOUNDS, 1, userContext.getUserId());
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
        if (ringtoneUri == null || ContentProvider.getUserIdFromUri(ringtoneUri) != context.getUserId()) {
            return ringtoneUri;
        }
        return ContentProvider.getUriWithoutUserId(ringtoneUri);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005f, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0060, code lost:
        if (r5 != null) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0062, code lost:
        $closeResource(r2, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0065, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0068, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0069, code lost:
        if (r4 != null) goto L_0x006b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x006b, code lost:
        $closeResource(r2, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006e, code lost:
        throw r5;
     */
    public static void setActualDefaultRingtoneUri(Context context, int type, Uri ringtoneUri) {
        String setting = getSettingForType(type);
        if (setting != null) {
            ContentResolver resolver = context.getContentResolver();
            if (Settings.Secure.getIntForUser(resolver, Settings.Secure.SYNC_PARENT_SOUNDS, 0, context.getUserId()) == 1) {
                disableSyncFromParent(context);
            }
            if (!isInternalRingtoneUri(ringtoneUri)) {
                ringtoneUri = ContentProvider.maybeAddUserId(ringtoneUri, context.getUserId());
            }
            Settings.System.putStringForUser(resolver, setting, ringtoneUri != null ? ringtoneUri.toString() : null, context.getUserId());
            if (ringtoneUri != null) {
                Uri cacheUri = getCacheForType(type, context.getUserId());
                try {
                    InputStream in = openRingtone(context, ringtoneUri);
                    OutputStream out = resolver.openOutputStream(cacheUri);
                    FileUtils.copy(in, out);
                    if (out != null) {
                        $closeResource(null, out);
                    }
                    if (in != null) {
                        $closeResource(null, in);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "Failed to cache ringtone: " + e);
                }
            }
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0060, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0061, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0064, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0067, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0068, code lost:
        if (r3 != null) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x006a, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x006d, code lost:
        throw r5;
     */
    public Uri addCustomExternalRingtone(Uri fileUri, int type) throws FileNotFoundException, IllegalArgumentException, IOException {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String mimeType = this.mContext.getContentResolver().getType(fileUri);
            if (mimeType == null || (!mimeType.startsWith("audio/") && !mimeType.equals("application/ogg"))) {
                throw new IllegalArgumentException("Ringtone file must have MIME type \"audio/*\". Given file has MIME type \"" + mimeType + "\"");
            }
            String subdirectory = getExternalDirectoryForType(type);
            Context context = this.mContext;
            File outFile = Utils.getUniqueExternalFile(context, subdirectory, FileUtils.buildValidFatFilename(Utils.getFileDisplayNameFromUri(context, fileUri)), mimeType);
            InputStream input = this.mContext.getContentResolver().openInputStream(fileUri);
            OutputStream output = new FileOutputStream(outFile);
            FileUtils.copy(input, output);
            $closeResource(null, output);
            if (input != null) {
                $closeResource(null, input);
            }
            return MediaStore.scanFile(this.mContext, outFile);
        }
        throw new IOException("External storage is not mounted. Unable to install ringtones.");
    }

    private static final String getExternalDirectoryForType(int type) {
        if (type == 1) {
            return Environment.DIRECTORY_RINGTONES;
        }
        if (type == 2) {
            return Environment.DIRECTORY_NOTIFICATIONS;
        }
        if (type == 4) {
            return Environment.DIRECTORY_ALARMS;
        }
        throw new IllegalArgumentException("Unsupported ringtone type: " + type);
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
            return Settings.System.RINGTONE;
        }
        if ((type & 8) != 0) {
            return "ringtone2";
        }
        if ((type & 2) != 0) {
            return Settings.System.NOTIFICATION_SOUND;
        }
        if ((type & 4) != 0) {
            return Settings.System.ALARM_ALERT;
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

    public static AssetFileDescriptor openDefaultRingtoneUri(Context context, Uri uri) throws FileNotFoundException {
        int type = getDefaultType(uri);
        Uri cacheUri = getCacheForType(type, context.getUserId());
        Uri actualUri = getActualDefaultRingtoneUri(context, type);
        ContentResolver resolver = context.getContentResolver();
        AssetFileDescriptor afd = null;
        if (cacheUri != null && (afd = resolver.openAssetFileDescriptor(cacheUri, "r")) != null) {
            return afd;
        }
        if (actualUri != null) {
            return resolver.openAssetFileDescriptor(actualUri, "r");
        }
        return afd;
    }

    public boolean hasHapticChannels(int position) {
        return hasHapticChannels(getRingtoneUri(position));
    }

    public static boolean hasHapticChannels(Uri ringtoneUri) {
        return AudioManager.hasHapticChannels(ringtoneUri);
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
