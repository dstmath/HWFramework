package android.mtp;

import android.app.DownloadManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaFormat;
import android.media.MediaScanner;
import android.media.midi.MidiDeviceInfo;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Files;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class MtpDatabase implements AutoCloseable {
    static final int[] AUDIO_PROPERTIES = new int[]{MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DISPLAY_NAME, MtpConstants.PROPERTY_DATE_ADDED, MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_ALBUM_ARTIST, MtpConstants.PROPERTY_TRACK, MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_GENRE, MtpConstants.PROPERTY_COMPOSER, MtpConstants.PROPERTY_AUDIO_WAVE_CODEC, MtpConstants.PROPERTY_BITRATE_TYPE, MtpConstants.PROPERTY_AUDIO_BITRATE, MtpConstants.PROPERTY_NUMBER_OF_CHANNELS, MtpConstants.PROPERTY_SAMPLE_RATE};
    private static final int DEVICE_PROPERTIES_DATABASE_VERSION = 1;
    static final int[] FILE_PROPERTIES = new int[]{MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DISPLAY_NAME, MtpConstants.PROPERTY_DATE_ADDED};
    private static final String FORMAT_PARENT_WHERE = "format=? AND parent=?";
    private static final String[] FORMAT_PROJECTION = new String[]{DownloadManager.COLUMN_ID, "format"};
    private static final String FORMAT_WHERE = "format=?";
    private static final String[] ID_PROJECTION = new String[]{DownloadManager.COLUMN_ID};
    private static final String ID_WHERE = "_id=?";
    static final int[] IMAGE_PROPERTIES = new int[]{MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DISPLAY_NAME, MtpConstants.PROPERTY_DATE_ADDED, MtpConstants.PROPERTY_DESCRIPTION};
    private static final String[] OBJECT_ALL_COLUMNS_PROJECTION = new String[]{DownloadManager.COLUMN_ID, "storage_id", "format", "parent", "_data", "_size", "date_added", "date_modified", "mime_type", "title", "_display_name", DownloadManager.COLUMN_MEDIA_TYPE, "is_drm", MediaFormat.KEY_WIDTH, MediaFormat.KEY_HEIGHT, "description", "artist", "album", "album_artist", "duration", "track", "composer", "year", MidiDeviceInfo.PROPERTY_NAME};
    private static final String[] OBJECT_INFO_PROJECTION = new String[]{DownloadManager.COLUMN_ID, "storage_id", "format", "parent", "_data", "_size", "date_added", "date_modified"};
    private static final String PARENT_WHERE = "parent=?";
    private static final String[] PATH_FORMAT_PROJECTION = new String[]{DownloadManager.COLUMN_ID, "_data", "_size", "format"};
    private static final String[] PATH_PROJECTION = new String[]{DownloadManager.COLUMN_ID, "_data"};
    private static final String PATH_WHERE = "_data=?";
    private static final String STORAGE_FORMAT_PARENT_WHERE = "storage_id=? AND format=? AND parent=?";
    private static final String STORAGE_FORMAT_WHERE = "storage_id=? AND format=?";
    private static final String STORAGE_PARENT_WHERE = "storage_id=? AND parent=?";
    private static final String STORAGE_WHERE = "storage_id=?";
    private static final String TAG = "MtpDatabase";
    static final int[] VIDEO_PROPERTIES = new int[]{MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DISPLAY_NAME, MtpConstants.PROPERTY_DATE_ADDED, MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_DESCRIPTION};
    private int mBatteryLevel;
    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                MtpDatabase.this.mBatteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int newLevel = intent.getIntExtra("level", 0);
                if (newLevel != MtpDatabase.this.mBatteryLevel) {
                    MtpDatabase.this.mBatteryLevel = newLevel;
                    try {
                        MtpDatabase.this.mServer.sendDevicePropertyChanged(MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL);
                    } catch (NullPointerException e) {
                        Log.e(MtpDatabase.TAG, "mServer already set to null");
                    }
                }
            }
        }
    };
    private int mBatteryScale;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    private final Context mContext;
    private boolean mDatabaseModified;
    private SharedPreferences mDeviceProperties;
    private int mDeviceType;
    private final ContentProviderClient mMediaProvider;
    private final MediaScanner mMediaScanner;
    private final String mMediaStoragePath;
    private long mNativeContext;
    private final Uri mObjectsUri;
    private final String mPackageName;
    private final HashMap<Integer, MtpPropertyGroup> mPropertyGroupsByFormat = new HashMap();
    private final HashMap<Integer, MtpPropertyGroup> mPropertyGroupsByProperty = new HashMap();
    private MtpServer mServer;
    private final HashMap<String, MtpStorage> mStorageMap = new HashMap();
    private final String[] mSubDirectories;
    private String mSubDirectoriesWhere;
    private String[] mSubDirectoriesWhereArgs;
    private final String mVolumeName;

    private final native void native_finalize();

    private final native void native_setup();

    static {
        System.loadLibrary("media_jni");
    }

    public MtpDatabase(Context context, String volumeName, String storagePath, String[] subDirectories) {
        native_setup();
        this.mContext = context;
        this.mPackageName = context.getPackageName();
        this.mMediaProvider = context.getContentResolver().acquireContentProviderClient("media");
        this.mVolumeName = volumeName;
        this.mMediaStoragePath = storagePath;
        this.mObjectsUri = Files.getMtpObjectsUri(volumeName).buildUpon().appendQueryParameter("nonotify", "1").build();
        this.mMediaScanner = new MediaScanner(context, this.mVolumeName);
        this.mSubDirectories = subDirectories;
        if (subDirectories != null) {
            int i;
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            for (i = 0; i < count; i++) {
                builder.append("_data=? OR _data LIKE ?");
                if (i != count - 1) {
                    builder.append(" OR ");
                }
            }
            builder.append(")");
            this.mSubDirectoriesWhere = builder.toString();
            this.mSubDirectoriesWhereArgs = new String[(count * 2)];
            int j = 0;
            for (String path : subDirectories) {
                int i2 = j + 1;
                this.mSubDirectoriesWhereArgs[j] = path;
                j = i2 + 1;
                this.mSubDirectoriesWhereArgs[i2] = path + "/%";
            }
        }
        initDeviceProperties(context);
        this.mDeviceType = SystemProperties.getInt("sys.usb.mtp.device_type", 0);
        this.mCloseGuard.open("close");
    }

    public void setServer(MtpServer server) {
        this.mServer = server;
        try {
            this.mContext.unregisterReceiver(this.mBatteryReceiver);
        } catch (IllegalArgumentException e) {
        }
        if (server != null) {
            this.mContext.registerReceiver(this.mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
    }

    public void close() {
        HwFrameworkFactory.getHwMtpDatabaseManager().hwClearSavedObject();
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            this.mMediaScanner.close();
            this.mMediaProvider.close();
            native_finalize();
        }
    }

    protected void finalize() throws Throwable {
        try {
            this.mCloseGuard.warnIfOpen();
            close();
        } finally {
            super.finalize();
        }
    }

    public void addStorage(MtpStorage storage) {
        this.mStorageMap.put(storage.getPath(), storage);
    }

    public void removeStorage(MtpStorage storage) {
        this.mStorageMap.remove(storage.getPath());
    }

    private void initDeviceProperties(Context context) {
        String devicePropertiesName = "device-properties";
        this.mDeviceProperties = context.getSharedPreferences("device-properties", 0);
        if (context.getDatabasePath("device-properties").exists()) {
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = context.openOrCreateDatabase("device-properties", 0, null);
                if (db != null) {
                    cursor = db.query("properties", new String[]{DownloadManager.COLUMN_ID, "code", "value"}, null, null, null, null, null);
                    if (cursor != null) {
                        Editor e = this.mDeviceProperties.edit();
                        while (cursor.moveToNext()) {
                            e.putString(cursor.getString(1), cursor.getString(2));
                        }
                        e.commit();
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception e2) {
                Log.e(TAG, "failed to migrate device properties", e2);
                context.deleteDatabase("device-properties");
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    db.close();
                }
            }
            context.deleteDatabase("device-properties");
        }
    }

    private boolean inStorageSubDirectory(String path) {
        if (this.mSubDirectories == null) {
            return true;
        }
        if (path == null) {
            return false;
        }
        boolean allowed = false;
        int pathLength = path.length();
        for (int i = 0; i < this.mSubDirectories.length && (allowed ^ 1) != 0; i++) {
            String subdir = this.mSubDirectories[i];
            int subdirLength = subdir.length();
            if (subdirLength < pathLength && path.charAt(subdirLength) == '/' && path.startsWith(subdir)) {
                allowed = true;
            }
        }
        return allowed;
    }

    private boolean isStorageSubDirectory(String path) {
        if (this.mSubDirectories == null) {
            return false;
        }
        for (Object equals : this.mSubDirectories) {
            if (path.equals(equals)) {
                return true;
            }
        }
        return false;
    }

    private boolean inStorageRoot(String path) {
        try {
            String canonical = new File(path).getCanonicalPath();
            for (String root : this.mStorageMap.keySet()) {
                if (canonical.startsWith(root)) {
                    return true;
                }
            }
        } catch (IOException e) {
        }
        return false;
    }

    private int beginSendObject(String path, int format, int parent, int storageId, long size, long modified) {
        if (!inStorageRoot(path)) {
            Log.e(TAG, "attempt to put file outside of storage area: " + path);
            return -1;
        } else if (!inStorageSubDirectory(path)) {
            return -1;
        } else {
            if (path != null) {
                Cursor cursor = null;
                try {
                    cursor = this.mMediaProvider.query(this.mObjectsUri, ID_PROJECTION, PATH_WHERE, new String[]{path}, null, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        Log.w(TAG, "file already exists in beginSendObject: " + path);
                        int hwBeginSendObject = HwFrameworkFactory.getHwMtpDatabaseManager().hwBeginSendObject(path, cursor);
                        if (cursor != null) {
                            cursor.close();
                        }
                        return hwBeginSendObject;
                    } else if (cursor != null) {
                        cursor.close();
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in beginSendObject", e);
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            this.mDatabaseModified = true;
            ContentValues values = new ContentValues();
            values.put("_data", path);
            values.put("format", Integer.valueOf(format));
            values.put("parent", Integer.valueOf(parent));
            values.put("storage_id", Integer.valueOf(storageId));
            values.put("_size", Long.valueOf(size));
            values.put("date_modified", Long.valueOf(modified));
            try {
                Uri uri = this.mMediaProvider.insert(this.mObjectsUri, values);
                if (uri != null) {
                    return Integer.parseInt((String) uri.getPathSegments().get(2));
                }
                return -1;
            } catch (RemoteException e2) {
                Log.e(TAG, "RemoteException in beginSendObject", e2);
                return -1;
            }
        }
    }

    private void endSendObject(String path, int handle, int format, boolean succeeded) {
        if (!succeeded) {
            deleteFile(handle);
        } else if (format == MtpConstants.FORMAT_ABSTRACT_AV_PLAYLIST) {
            String name = path;
            int lastSlash = path.lastIndexOf(47);
            if (lastSlash >= 0) {
                name = path.substring(lastSlash + 1);
            }
            if (name.endsWith(".pla")) {
                name = name.substring(0, name.length() - 4);
            }
            ContentValues values = new ContentValues(1);
            values.put("_data", path);
            values.put(MidiDeviceInfo.PROPERTY_NAME, name);
            values.put("format", Integer.valueOf(format));
            values.put("date_modified", Long.valueOf(System.currentTimeMillis() / 1000));
            values.put("media_scanner_new_object_id", Integer.valueOf(handle));
            try {
                Uri insert = this.mMediaProvider.insert(Playlists.EXTERNAL_CONTENT_URI, values);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in endSendObject", e);
            }
        } else {
            this.mMediaScanner.scanMtpFile(path, handle, format);
        }
    }

    private Cursor createObjectQuery(int storageID, int format, int parent) throws RemoteException {
        String where;
        String[] whereArgs;
        if (storageID == -1) {
            if (format == 0) {
                if (parent == 0) {
                    where = null;
                    whereArgs = null;
                } else {
                    if (parent == -1) {
                        parent = 0;
                    }
                    where = PARENT_WHERE;
                    whereArgs = new String[]{Integer.toString(parent)};
                }
            } else if (parent == 0) {
                where = FORMAT_WHERE;
                whereArgs = new String[]{Integer.toString(format)};
            } else {
                if (parent == -1) {
                    parent = 0;
                }
                where = FORMAT_PARENT_WHERE;
                whereArgs = new String[]{Integer.toString(format), Integer.toString(parent)};
            }
        } else if (format == 0) {
            if (parent == 0) {
                where = STORAGE_WHERE;
                whereArgs = new String[]{Integer.toString(storageID)};
            } else {
                if (parent == -1) {
                    parent = 0;
                }
                where = STORAGE_PARENT_WHERE;
                whereArgs = new String[]{Integer.toString(storageID), Integer.toString(parent)};
            }
        } else if (parent == 0) {
            where = STORAGE_FORMAT_WHERE;
            whereArgs = new String[]{Integer.toString(storageID), Integer.toString(format)};
        } else {
            if (parent == -1) {
                parent = 0;
            }
            where = STORAGE_FORMAT_PARENT_WHERE;
            whereArgs = new String[]{Integer.toString(storageID), Integer.toString(format), Integer.toString(parent)};
        }
        if (this.mSubDirectoriesWhere != null) {
            if (where == null) {
                where = this.mSubDirectoriesWhere;
                whereArgs = this.mSubDirectoriesWhereArgs;
            } else {
                where = where + " AND " + this.mSubDirectoriesWhere;
                String[] newWhereArgs = new String[(whereArgs.length + this.mSubDirectoriesWhereArgs.length)];
                int i = 0;
                while (i < whereArgs.length) {
                    newWhereArgs[i] = whereArgs[i];
                    i++;
                }
                for (String str : this.mSubDirectoriesWhereArgs) {
                    newWhereArgs[i] = str;
                    i++;
                }
                whereArgs = newWhereArgs;
            }
        }
        return this.mMediaProvider.query(this.mObjectsUri, ID_PROJECTION, where, whereArgs, null, null);
    }

    private int[] getObjectList(int storageID, int format, int parent) {
        Cursor cursor = null;
        try {
            cursor = createObjectQuery(storageID, format, parent);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            int count = cursor.getCount();
            if (count > 0) {
                int[] result = new int[count];
                for (int i = 0; i < count; i++) {
                    cursor.moveToNext();
                    result[i] = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
                return result;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectList", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int getNumObjects(int storageID, int format, int parent) {
        Cursor cursor = null;
        try {
            cursor = createObjectQuery(storageID, format, parent);
            if (cursor != null) {
                int count = cursor.getCount();
                if (cursor != null) {
                    cursor.close();
                }
                return count;
            }
            if (cursor != null) {
                cursor.close();
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getNumObjects", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int[] getSupportedPlaybackFormats() {
        return new int[]{12288, 12289, 12292, 12293, 12296, 12297, 12299, MtpConstants.FORMAT_EXIF_JPEG, MtpConstants.FORMAT_TIFF_EP, MtpConstants.FORMAT_BMP, MtpConstants.FORMAT_GIF, MtpConstants.FORMAT_JFIF, MtpConstants.FORMAT_PNG, MtpConstants.FORMAT_TIFF, MtpConstants.FORMAT_WMA, MtpConstants.FORMAT_OGG, MtpConstants.FORMAT_AAC, MtpConstants.FORMAT_MP4_CONTAINER, MtpConstants.FORMAT_MP2, MtpConstants.FORMAT_3GP_CONTAINER, MtpConstants.FORMAT_ABSTRACT_AV_PLAYLIST, MtpConstants.FORMAT_WPL_PLAYLIST, MtpConstants.FORMAT_M3U_PLAYLIST, MtpConstants.FORMAT_PLS_PLAYLIST, MtpConstants.FORMAT_XML_DOCUMENT, MtpConstants.FORMAT_FLAC, MtpConstants.FORMAT_DNG, MtpConstants.FORMAT_BMP};
    }

    private int[] getSupportedCaptureFormats() {
        return null;
    }

    public int[] getSupportedObjectProperties(int format) {
        switch (format) {
            case 12296:
            case 12297:
            case MtpConstants.FORMAT_WMA /*47361*/:
            case MtpConstants.FORMAT_OGG /*47362*/:
            case MtpConstants.FORMAT_AAC /*47363*/:
                return (int[]) AUDIO_PROPERTIES.clone();
            case 12299:
            case MtpConstants.FORMAT_WMV /*47489*/:
            case MtpConstants.FORMAT_3GP_CONTAINER /*47492*/:
                return (int[]) VIDEO_PROPERTIES.clone();
            case MtpConstants.FORMAT_EXIF_JPEG /*14337*/:
            case MtpConstants.FORMAT_BMP /*14340*/:
            case MtpConstants.FORMAT_GIF /*14343*/:
            case MtpConstants.FORMAT_PNG /*14347*/:
            case MtpConstants.FORMAT_DNG /*14353*/:
                return (int[]) IMAGE_PROPERTIES.clone();
            default:
                return (int[]) FILE_PROPERTIES.clone();
        }
    }

    private int[] getSupportedDeviceProperties() {
        return new int[]{MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER, MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME, MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE, MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL, MtpConstants.DEVICE_PROPERTY_PERCEIVED_DEVICE_TYPE};
    }

    private void cacheObjectAllInfos(int handle) {
        if (handle > 0 && HwFrameworkFactory.getHwMtpDatabaseManager().hwGetSavedObjectHandle() != handle) {
            Cursor cursor = null;
            try {
                cursor = this.mMediaProvider.query(this.mObjectsUri, OBJECT_ALL_COLUMNS_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle)}, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    HwFrameworkFactory.getHwMtpDatabaseManager().hwSaveCurrentObject(cursor);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in cacheObjectAllInfos", e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private MtpPropertyList getObjectPropertyList(int handle, int format, int property, int groupCode, int depth) {
        MtpPropertyList result;
        MtpPropertyGroup propertyGroup;
        if (depth == 0) {
            cacheObjectAllInfos(handle);
            result = HwFrameworkFactory.getHwMtpDatabaseManager().getObjectPropertyList(this, handle, format, property, groupCode);
            if (result != null) {
                return result;
            }
        }
        if (property == -1) {
            if (!(format != 0 || handle == 0 || handle == -1)) {
                format = getObjectFormat(handle);
            }
            if (depth == 0 && handle != -1) {
                result = HwFrameworkFactory.getHwMtpDatabaseManager().getObjectPropertyList(handle, format, getSupportedObjectProperties(format));
                if (result != null) {
                    return result;
                }
            }
            propertyGroup = (MtpPropertyGroup) this.mPropertyGroupsByFormat.get(Integer.valueOf(format));
            if (propertyGroup == null) {
                propertyGroup = new MtpPropertyGroup(this, this.mMediaProvider, this.mVolumeName, getSupportedObjectProperties(format));
                this.mPropertyGroupsByFormat.put(Integer.valueOf(format), propertyGroup);
            }
        } else {
            if (depth == 0 && handle != -1) {
                result = HwFrameworkFactory.getHwMtpDatabaseManager().getObjectPropertyList(handle, format, new int[]{property});
                if (result != null) {
                    return result;
                }
            }
            propertyGroup = (MtpPropertyGroup) this.mPropertyGroupsByProperty.get(Integer.valueOf(property));
            if (propertyGroup == null) {
                propertyGroup = new MtpPropertyGroup(this, this.mMediaProvider, this.mVolumeName, new int[]{property});
                this.mPropertyGroupsByProperty.put(Integer.valueOf(property), propertyGroup);
            }
        }
        return propertyGroup.getPropertyList(handle, format, depth);
    }

    private int renameFile(int handle, String newName) {
        Cursor cursor = null;
        String path = null;
        String[] whereArgs = new String[]{Integer.toString(handle)};
        try {
            cursor = this.mMediaProvider.query(this.mObjectsUri, PATH_PROJECTION, ID_WHERE, whereArgs, null, null);
            if (cursor != null && cursor.moveToNext()) {
                path = cursor.getString(1);
            }
            if (cursor != null) {
                cursor.close();
            }
            if (path == null) {
                return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
            }
            if (isStorageSubDirectory(path)) {
                return MtpConstants.RESPONSE_OBJECT_WRITE_PROTECTED;
            }
            File oldFile = new File(path);
            int lastSlash = path.lastIndexOf(47);
            if (lastSlash <= 1) {
                return 8194;
            }
            String newPath = path.substring(0, lastSlash + 1) + newName;
            File newFile = new File(newPath);
            if (oldFile.renameTo(newFile)) {
                ContentValues values = new ContentValues();
                values.put("_data", newPath);
                int updated = 0;
                try {
                    updated = this.mMediaProvider.update(this.mObjectsUri, values, ID_WHERE, whereArgs);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in mMediaProvider.update", e);
                }
                if (updated == 0) {
                    Log.e(TAG, "Unable to update path for " + path + " to " + newPath);
                    newFile.renameTo(oldFile);
                    return 8194;
                }
                if (newFile.isDirectory()) {
                    if (oldFile.getName().startsWith(".") && (newPath.startsWith(".") ^ 1) != 0) {
                        try {
                            this.mMediaProvider.call("unhide", newPath, null);
                        } catch (RemoteException e2) {
                            Log.e(TAG, "failed to unhide/rescan for " + newPath);
                        }
                    }
                } else if (!oldFile.getName().toLowerCase(Locale.US).equals(".nomedia") || (newPath.toLowerCase(Locale.US).equals(".nomedia") ^ 1) == 0) {
                    this.mMediaScanner.scanMtpFile(newPath, handle, 12288);
                } else {
                    try {
                        this.mMediaProvider.call("unhide", oldFile.getParent(), null);
                    } catch (RemoteException e3) {
                        Log.e(TAG, "failed to unhide/rescan for " + newPath);
                    }
                }
                return MtpConstants.RESPONSE_OK;
            }
            Log.w(TAG, "renaming " + path + " to " + newPath + " failed");
            return 8194;
        } catch (RemoteException e4) {
            Log.e(TAG, "RemoteException in getObjectFilePath", e4);
            if (cursor != null) {
                cursor.close();
            }
            return 8194;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private int setObjectProperty(int handle, int property, long intValue, String stringValue) {
        if (HwFrameworkFactory.getHwMtpDatabaseManager().hwGetSavedObjectHandle() == handle) {
            HwFrameworkFactory.getHwMtpDatabaseManager().hwClearSavedObject();
        }
        switch (property) {
            case MtpConstants.PROPERTY_OBJECT_FILE_NAME /*56327*/:
                return renameFile(handle, stringValue);
            default:
                return MtpConstants.RESPONSE_OBJECT_PROP_NOT_SUPPORTED;
        }
    }

    private int getDeviceProperty(int property, long[] outIntValue, char[] outStringValue) {
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE /*20483*/:
                Display display = ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                String imageSize = Integer.toString(display.getMaximumSizeDimension()) + "x" + Integer.toString(display.getMaximumSizeDimension());
                imageSize.getChars(0, imageSize.length(), outStringValue, 0);
                outStringValue[imageSize.length()] = 0;
                return MtpConstants.RESPONSE_OK;
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER /*54273*/:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME /*54274*/:
                String value = this.mDeviceProperties.getString(Integer.toString(property), ProxyInfo.LOCAL_EXCL_LIST);
                if (property == MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME) {
                    if (Global.getInt(this.mContext.getContentResolver(), "unified_device_name_updated", 0) == 1) {
                        value = Global.getString(this.mContext.getContentResolver(), "unified_device_name");
                        Global.putInt(this.mContext.getContentResolver(), "unified_device_name_updated", 0);
                        Editor e = this.mDeviceProperties.edit();
                        e.putString(Integer.toString(property), value);
                        e.commit();
                    }
                    if (value == null || value.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
                        value = SystemProperties.get("ro.config.marketing_name");
                    }
                    if (value == null || value.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
                        value = SystemProperties.get("ro.product.model");
                    }
                }
                int length = value.length();
                if (length > 255) {
                    length = 255;
                }
                value.getChars(0, length, outStringValue, 0);
                outStringValue[length] = 0;
                return MtpConstants.RESPONSE_OK;
            case MtpConstants.DEVICE_PROPERTY_PERCEIVED_DEVICE_TYPE /*54279*/:
                outIntValue[0] = (long) this.mDeviceType;
                return MtpConstants.RESPONSE_OK;
            default:
                return MtpConstants.RESPONSE_DEVICE_PROP_NOT_SUPPORTED;
        }
    }

    private int setDeviceProperty(int property, long intValue, String stringValue) {
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER /*54273*/:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME /*54274*/:
                int i;
                Editor e = this.mDeviceProperties.edit();
                e.putString(Integer.toString(property), stringValue);
                if (e.commit()) {
                    i = MtpConstants.RESPONSE_OK;
                } else {
                    i = 8194;
                }
                return i;
            default:
                return MtpConstants.RESPONSE_DEVICE_PROP_NOT_SUPPORTED;
        }
    }

    private boolean getObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outCreatedModified) {
        cacheObjectAllInfos(handle);
        if (HwFrameworkFactory.getHwMtpDatabaseManager().hwGetObjectInfo(handle, outStorageFormatParent, outName, outCreatedModified)) {
            return true;
        }
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(this.mObjectsUri, OBJECT_INFO_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle)}, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            outStorageFormatParent[0] = cursor.getInt(1);
            outStorageFormatParent[1] = cursor.getInt(2);
            outStorageFormatParent[2] = cursor.getInt(3);
            String path = cursor.getString(4);
            int lastSlash = path.lastIndexOf(47);
            int start = lastSlash >= 0 ? lastSlash + 1 : 0;
            int end = path.length();
            if (end - start > 255) {
                end = start + 255;
            }
            path.getChars(start, end, outName, 0);
            outName[end - start] = 0;
            outCreatedModified[0] = cursor.getLong(5);
            outCreatedModified[1] = cursor.getLong(6);
            if (outCreatedModified[0] == 0) {
                outCreatedModified[0] = outCreatedModified[1];
            }
            if (cursor != null) {
                cursor.close();
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectInfo", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int getObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        if (handle == 0) {
            this.mMediaStoragePath.getChars(0, this.mMediaStoragePath.length(), outFilePath, 0);
            outFilePath[this.mMediaStoragePath.length()] = 0;
            outFileLengthFormat[0] = 0;
            outFileLengthFormat[1] = 12289;
            return MtpConstants.RESPONSE_OK;
        } else if (HwFrameworkFactory.getHwMtpDatabaseManager().hwGetObjectFilePath(handle, outFilePath, outFileLengthFormat) != -1) {
            return MtpConstants.RESPONSE_OK;
        } else {
            Cursor cursor = null;
            try {
                cursor = this.mMediaProvider.query(this.mObjectsUri, PATH_FORMAT_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle)}, null, null);
                if (cursor == null || !cursor.moveToNext()) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
                }
                String path = cursor.getString(1);
                path.getChars(0, path.length(), outFilePath, 0);
                outFilePath[path.length()] = 0;
                outFileLengthFormat[0] = new File(path).length();
                outFileLengthFormat[1] = cursor.getLong(3);
                if (cursor != null) {
                    cursor.close();
                }
                return MtpConstants.RESPONSE_OK;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in getObjectFilePath", e);
                if (cursor != null) {
                    cursor.close();
                }
                return 8194;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    private int getObjectFormat(int handle) {
        int format = HwFrameworkFactory.getHwMtpDatabaseManager().hwGetObjectFormat(handle);
        if (format != -1) {
            return format;
        }
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(this.mObjectsUri, FORMAT_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle)}, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return -1;
            }
            int i = cursor.getInt(1);
            if (cursor != null) {
                cursor.close();
            }
            return i;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectFilePath", e);
            if (cursor != null) {
                cursor.close();
            }
            return -1;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private int deleteFile(int handle) {
        this.mDatabaseModified = true;
        Cursor c = null;
        try {
            c = this.mMediaProvider.query(this.mObjectsUri, PATH_FORMAT_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle)}, null, null);
            if (c == null || !c.moveToNext()) {
                if (c != null) {
                    c.close();
                }
                return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
            }
            String path = c.getString(1);
            int format = c.getInt(3);
            if (path == null || format == 0) {
                if (c != null) {
                    c.close();
                }
                return 8194;
            } else if (isStorageSubDirectory(path)) {
                if (c != null) {
                    c.close();
                }
                return MtpConstants.RESPONSE_OBJECT_WRITE_PROTECTED;
            } else {
                if (format == 12289) {
                    int delete = this.mMediaProvider.delete(Files.getMtpObjectsUri(this.mVolumeName), "_data LIKE ?1 AND lower(substr(_data,1,?2))=lower(?3)", new String[]{path + "/%", Integer.toString(path.length() + 1), path + "/"});
                }
                if (this.mMediaProvider.delete(Files.getMtpObjectsUri(this.mVolumeName, (long) handle), null, null) > 0) {
                    if (format != 12289 && path.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                        try {
                            this.mMediaProvider.call("unhide", path.substring(0, path.lastIndexOf("/")), null);
                        } catch (RemoteException e) {
                            Log.e(TAG, "failed to unhide/rescan for " + path);
                        }
                    }
                    if (c != null) {
                        c.close();
                    }
                    return MtpConstants.RESPONSE_OK;
                }
                if (c != null) {
                    c.close();
                }
                return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in deleteFile", e2);
            if (c != null) {
                c.close();
            }
            return 8194;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private int[] getObjectReferences(int handle) {
        if (HwFrameworkFactory.getHwMtpDatabaseManager().hwGetObjectReferences(handle)) {
            return null;
        }
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(Files.getMtpReferencesUri(this.mVolumeName, (long) handle).buildUpon().appendQueryParameter("nonotify", "1").build(), ID_PROJECTION, null, null, null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            int count = cursor.getCount();
            if (count > 0) {
                int[] result = new int[count];
                for (int i = 0; i < count; i++) {
                    cursor.moveToNext();
                    result[i] = cursor.getInt(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
                return result;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectList", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int setObjectReferences(int handle, int[] references) {
        this.mDatabaseModified = true;
        Uri uri = Files.getMtpReferencesUri(this.mVolumeName, (long) handle);
        int count = references.length;
        ContentValues[] valuesList = new ContentValues[count];
        for (int i = 0; i < count; i++) {
            ContentValues values = new ContentValues();
            values.put(DownloadManager.COLUMN_ID, Integer.valueOf(references[i]));
            valuesList[i] = values;
        }
        try {
            if (this.mMediaProvider.bulkInsert(uri, valuesList) > 0) {
                return MtpConstants.RESPONSE_OK;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setObjectReferences", e);
        }
        return 8194;
    }

    private void sessionStarted() {
        this.mDatabaseModified = false;
    }

    private void sessionEnded() {
        if (this.mDatabaseModified) {
            this.mContext.sendBroadcast(new Intent("android.provider.action.MTP_SESSION_END"));
            this.mDatabaseModified = false;
        }
        HwFrameworkFactory.getHwMtpDatabaseManager().hwClearSavedObject();
    }
}
