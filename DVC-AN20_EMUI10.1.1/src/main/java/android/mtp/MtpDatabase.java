package android.mtp;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.IHwPluginManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.HwMediaFactory;
import android.mtp.MtpStorageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.storage.ExternalStorageFileImpl;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForNative;
import com.google.android.collect.Sets;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class MtpDatabase implements AutoCloseable {
    private static final int[] AUDIO_PROPERTIES = {MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_ALBUM_ARTIST, MtpConstants.PROPERTY_TRACK, MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_COMPOSER, MtpConstants.PROPERTY_AUDIO_WAVE_CODEC, MtpConstants.PROPERTY_BITRATE_TYPE, MtpConstants.PROPERTY_AUDIO_BITRATE, MtpConstants.PROPERTY_NUMBER_OF_CHANNELS, MtpConstants.PROPERTY_SAMPLE_RATE};
    private static int DATABASE_COLUMN_INDEX_FIR = 1;
    private static int DATABASE_COLUMN_INDEX_FOR = 4;
    private static int DATABASE_COLUMN_INDEX_SEC = 2;
    private static int DATABASE_COLUMN_INDEX_THI = 3;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int[] DEVICE_PROPERTIES = {MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER, MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME, MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE, MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL, MtpConstants.DEVICE_PROPERTY_PERCEIVED_DEVICE_TYPE};
    private static final int[] FILE_PROPERTIES = {MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DISPLAY_NAME, MtpConstants.PROPERTY_DATE_ADDED};
    private static int HANDLE_OFFSET_FOR_HISUITE = IHwPluginManager.VERSION_APIMAJOR_POS;
    private static final String[] ID_PROJECTION = {"_id"};
    private static final String ID_WHERE = "_id=?";
    private static final int[] IMAGE_PROPERTIES = {56392};
    private static final String NO_MEDIA = ".nomedia";
    private static final String[] OBJECT_ALL_COLUMNS_PROJECTION = {"_id", MediaStore.Files.FileColumns.STORAGE_ID, "format", "parent", "_data", "_size", "date_added", "date_modified", "mime_type", "title", "_display_name", "media_type", MediaStore.MediaColumns.IS_DRM, "width", "height", "description", "artist", "album", MediaStore.Audio.AudioColumns.ALBUM_ARTIST, "duration", MediaStore.Audio.AudioColumns.TRACK, MediaStore.Audio.AudioColumns.COMPOSER, MediaStore.Audio.AudioColumns.YEAR, "name"};
    private static final String[] PATH_FORMAT_PROJECTION = {"_id", "_data", "_size", "format"};
    private static final String[] PATH_PROJECTION = {"_data"};
    private static final String[] PATH_PROJECTION_FOR_HISUITE = {"_id", "_data", MediaStore.Files.FileColumns.STORAGE_ID};
    private static final String PATH_WHERE = "_data=?";
    private static final int[] PLAYBACK_FORMATS = {12288, 12289, 12292, 12293, 12296, 12297, 12299, MtpConstants.FORMAT_EXIF_JPEG, MtpConstants.FORMAT_TIFF_EP, MtpConstants.FORMAT_BMP, MtpConstants.FORMAT_GIF, MtpConstants.FORMAT_JFIF, MtpConstants.FORMAT_PNG, MtpConstants.FORMAT_TIFF, MtpConstants.FORMAT_WMA, MtpConstants.FORMAT_OGG, MtpConstants.FORMAT_AAC, MtpConstants.FORMAT_MP4_CONTAINER, MtpConstants.FORMAT_MP2, MtpConstants.FORMAT_3GP_CONTAINER, MtpConstants.FORMAT_ABSTRACT_AV_PLAYLIST, MtpConstants.FORMAT_WPL_PLAYLIST, MtpConstants.FORMAT_M3U_PLAYLIST, MtpConstants.FORMAT_PLS_PLAYLIST, MtpConstants.FORMAT_XML_DOCUMENT, MtpConstants.FORMAT_FLAC, MtpConstants.FORMAT_DNG, MtpConstants.FORMAT_HEIF};
    private static final String TAG = MtpDatabase.class.getSimpleName();
    private static final int[] VIDEO_PROPERTIES = {MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_DESCRIPTION};
    private static int sHandleOffsetForHisuite = HANDLE_OFFSET_FOR_HISUITE;
    private int mBatteryLevel;
    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        /* class android.mtp.MtpDatabase.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
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
    private SharedPreferences mDeviceProperties;
    private int mDeviceType;
    private MtpStorageManager mManager;
    private final ContentProviderClient mMediaProvider;
    @VisibleForNative
    private long mNativeContext;
    private final Uri mObjectsUri;
    private final SparseArray<MtpPropertyGroup> mPropertyGroupsByFormat = new SparseArray<>();
    private final SparseArray<MtpPropertyGroup> mPropertyGroupsByProperty = new SparseArray<>();
    private int mSendObjectFormat;
    private String mSendobjectPath;
    private MtpServer mServer;
    private final HashMap<String, MtpStorage> mStorageMap = new HashMap<>();

    private final native void native_finalize();

    private final native void native_setup();

    static {
        System.loadLibrary("media_jni");
    }

    @VisibleForNative
    private int[] getSupportedObjectProperties(int format) {
        if (!(format == 12296 || format == 12297)) {
            if (format != 12299) {
                if (!(format == 14337 || format == 14340 || format == 14343 || format == 14347)) {
                    if (!(format == 47489 || format == 47492)) {
                        if (!(format == 14353 || format == 14354)) {
                            switch (format) {
                                case MtpConstants.FORMAT_WMA:
                                case MtpConstants.FORMAT_OGG:
                                case MtpConstants.FORMAT_AAC:
                                    break;
                                default:
                                    return FILE_PROPERTIES;
                            }
                        }
                    }
                }
                return IntStream.concat(Arrays.stream(FILE_PROPERTIES), Arrays.stream(IMAGE_PROPERTIES)).toArray();
            }
            return IntStream.concat(Arrays.stream(FILE_PROPERTIES), Arrays.stream(VIDEO_PROPERTIES)).toArray();
        }
        return IntStream.concat(Arrays.stream(FILE_PROPERTIES), Arrays.stream(AUDIO_PROPERTIES)).toArray();
    }

    public static Uri getObjectPropertiesUri(int format, String volumeName) {
        if (!(format == 12296 || format == 12297)) {
            if (format != 12299) {
                if (!(format == 14337 || format == 14340 || format == 14343 || format == 14347)) {
                    if (!(format == 47489 || format == 47492)) {
                        if (!(format == 14353 || format == 14354)) {
                            switch (format) {
                                case MtpConstants.FORMAT_WMA:
                                case MtpConstants.FORMAT_OGG:
                                case MtpConstants.FORMAT_AAC:
                                    break;
                                default:
                                    return MediaStore.Files.getContentUri(volumeName);
                            }
                        }
                    }
                }
                return MediaStore.Images.Media.getContentUri(volumeName);
            }
            return MediaStore.Video.Media.getContentUri(volumeName);
        }
        return MediaStore.Audio.Media.getContentUri(volumeName);
    }

    @VisibleForNative
    private int[] getSupportedDeviceProperties() {
        return DEVICE_PROPERTIES;
    }

    @VisibleForNative
    private int[] getSupportedPlaybackFormats() {
        return PLAYBACK_FORMATS;
    }

    @VisibleForNative
    private int[] getSupportedCaptureFormats() {
        return null;
    }

    public MtpDatabase(Context context, String[] subDirectories) {
        native_setup();
        this.mContext = (Context) Objects.requireNonNull(context);
        this.mMediaProvider = context.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        this.mObjectsUri = MediaStore.Files.getMtpObjectsUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        this.mManager = new MtpStorageManager(new MtpStorageManager.MtpNotifier() {
            /* class android.mtp.MtpDatabase.AnonymousClass2 */

            @Override // android.mtp.MtpStorageManager.MtpNotifier
            public void sendObjectAdded(int id) {
                if (MtpDatabase.this.mServer != null) {
                    MtpDatabase.this.mServer.sendObjectAdded(id);
                }
            }

            @Override // android.mtp.MtpStorageManager.MtpNotifier
            public void sendObjectRemoved(int id) {
                if (MtpDatabase.this.mServer != null) {
                    MtpDatabase.this.mServer.sendObjectRemoved(id);
                }
            }

            @Override // android.mtp.MtpStorageManager.MtpNotifier
            public void sendObjectInfoChanged(int id) {
                if (MtpDatabase.this.mServer != null) {
                    MtpDatabase.this.mServer.sendObjectInfoChanged(id);
                }
            }
        }, subDirectories == null ? null : Sets.newHashSet(subDirectories));
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

    public Context getContext() {
        return this.mContext;
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        this.mManager.close();
        HwMediaFactory.getHwMtpDatabaseManager().hwClearSavedObject();
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            ContentProviderClient contentProviderClient = this.mMediaProvider;
            if (contentProviderClient != null) {
                contentProviderClient.close();
            }
            native_finalize();
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }

    public void addStorage(StorageVolume storage) {
        Cursor c = null;
        try {
            Uri objectsUri = MediaStore.Files.getMtpObjectsUri(getVolumeName(storage));
            c = this.mMediaProvider.query(objectsUri, PATH_PROJECTION_FOR_HISUITE, PATH_WHERE, new String[]{storage.getPath()}, null, null);
            if (c == null || !c.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put("_data", storage.getPath());
                values.put("format", (Integer) 12289);
                values.put("date_modified", Long.valueOf(new File(storage.getPath()).lastModified()));
                try {
                    this.mMediaProvider.insert(objectsUri, values);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in addStorage", e);
                }
                if (c != null) {
                    c.close();
                }
                c = this.mMediaProvider.query(objectsUri, PATH_PROJECTION_FOR_HISUITE, PATH_WHERE, new String[]{storage.getPath()}, null, null);
                if (c == null || !c.moveToNext()) {
                    if (c != null) {
                        c.close();
                        return;
                    }
                    return;
                }
            }
            int storageId = c.getInt(DATABASE_COLUMN_INDEX_SEC);
            String str = TAG;
            Log.e(str, "addStorageId : " + storageId);
            MtpStorage mtpStorage = this.mManager.addMtpStorage(storage, storageId);
            this.mStorageMap.put(storage.getPath(), mtpStorage);
            if (this.mServer != null) {
                this.mServer.addStorage(mtpStorage);
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in addStorage", e2);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
        c.close();
    }

    public void removeStorage(StorageVolume storage) {
        MtpStorage mtpStorage = this.mStorageMap.get(storage.getPath());
        if (mtpStorage != null) {
            MtpServer mtpServer = this.mServer;
            if (mtpServer != null) {
                mtpServer.removeStorage(mtpStorage);
            }
            this.mManager.removeMtpStorage(mtpStorage);
            this.mStorageMap.remove(storage.getPath());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0061, code lost:
        if (r6 != null) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0063, code lost:
        r6.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0077, code lost:
        if (0 != 0) goto L_0x0063;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007a, code lost:
        r17.deleteDatabase("device-properties");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    private void initDeviceProperties(Context context) {
        this.mDeviceProperties = context.getSharedPreferences("device-properties", 0);
        if (context.getDatabasePath("device-properties").exists()) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = context.openOrCreateDatabase("device-properties", 0, null);
                if (!(db == null || (c = db.query("properties", new String[]{"_id", "code", "value"}, null, null, null, null, null)) == null)) {
                    SharedPreferences.Editor e = this.mDeviceProperties.edit();
                    while (c.moveToNext()) {
                        e.putString(c.getString(1), c.getString(2));
                    }
                    e.commit();
                }
                if (c != null) {
                    c.close();
                }
            } catch (Exception e2) {
                Log.e(TAG, "failed to migrate device properties", e2);
                if (0 != 0) {
                    c.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                if (0 != 0) {
                    db.close();
                }
                throw th;
            }
        }
    }

    @VisibleForNative
    private int beginSendObject(String path, int format, int parent, int storageId) {
        if (parent > sHandleOffsetForHisuite) {
            return beginSendObjectHisuite(path, format, parent, storageId) + sHandleOffsetForHisuite;
        }
        MtpStorageManager mtpStorageManager = this.mManager;
        MtpStorageManager.MtpObject parentObj = parent == 0 ? mtpStorageManager.getStorageRoot(storageId) : mtpStorageManager.getObject(parent);
        if (parentObj == null) {
            return -1;
        }
        return this.mManager.beginSendObject(parentObj, Paths.get(path, new String[0]).getFileName().toString(), format);
    }

    @VisibleForNative
    private void endSendObject(int handle, boolean succeeded) {
        if (!endSendObjectHisuite(handle, succeeded)) {
            MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
            if (obj == null || !this.mManager.endSendObject(obj, succeeded)) {
                Log.e(TAG, "Failed to successfully end send object");
            } else if (succeeded) {
                MediaStore.scanFile(this.mContext, obj.getPath().toFile());
            }
        }
    }

    @VisibleForNative
    private void rescanFile(String path, int handle, int format) {
        MediaStore.scanFile(this.mContext, new File(path));
    }

    @VisibleForNative
    private int[] getObjectList(int storageID, int format, int parent) {
        List<MtpStorageManager.MtpObject> objs = this.mManager.getObjects(parent, format, storageID);
        if (objs == null) {
            return null;
        }
        int[] ret = new int[objs.size()];
        for (int i = 0; i < objs.size(); i++) {
            ret[i] = objs.get(i).getId();
        }
        return ret;
    }

    @VisibleForNative
    private int getNumObjects(int storageID, int format, int parent) {
        List<MtpStorageManager.MtpObject> objs = this.mManager.getObjects(parent, format, storageID);
        if (objs == null) {
            return -1;
        }
        return objs.size();
    }

    private void cacheObjectForHisuite(int handle) {
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(this.mObjectsUri, OBJECT_ALL_COLUMNS_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle - sHandleOffsetForHisuite)}, null, null);
            if (cursor != null && cursor.moveToNext()) {
                MtpStorage mStorageValue = null;
                Iterator<MtpStorage> it = this.mStorageMap.values().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    MtpStorage mapValue = it.next();
                    if (mapValue != null) {
                        if (cursor.getInt(DATABASE_COLUMN_INDEX_FIR) == mapValue.getStorageId()) {
                            mStorageValue = mapValue;
                            break;
                        }
                    }
                }
                if (mStorageValue == null) {
                    Log.e(TAG, "Can't find matched MtpStorage in cacheObjectForHisuite");
                    cursor.close();
                    return;
                }
                MtpStorageManager.MtpObject newObject = new MtpStorageManager.MtpObject(cursor.getString(DATABASE_COLUMN_INDEX_FOR), handle, mStorageValue, null, cursor.getInt(DATABASE_COLUMN_INDEX_SEC) == 12289);
                newObject.setStorageId(cursor.getInt(DATABASE_COLUMN_INDEX_FIR));
                HwMediaFactory.getHwMtpDatabaseManager().hwSaveCurrentObject(newObject.getMtpObjectEx(), cursor);
            }
            if (cursor == null) {
                return;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in cacheObjectForHisuite");
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
    }

    private void cacheObjectAllInfos(int handle) {
        if (handle > 0) {
            Cursor c = null;
            if (HwMediaFactory.getHwMtpDatabaseManager().hwGetSavedObjectHandle() != handle) {
                if (handle > sHandleOffsetForHisuite) {
                    cacheObjectForHisuite(handle);
                    return;
                }
                MtpStorageManager.MtpObject newObject = this.mManager.getObject(handle);
                if (newObject != null) {
                    try {
                        c = this.mMediaProvider.query(MediaStore.Files.getMtpObjectsUri(newObject.getVolumeName()), OBJECT_ALL_COLUMNS_PROJECTION, PATH_WHERE, new String[]{newObject.getPath().toString()}, null, null);
                        if (c != null) {
                            if (c.moveToNext()) {
                                HwMediaFactory.getHwMtpDatabaseManager().hwSaveCurrentObject(newObject.getMtpObjectEx(), c);
                            } else {
                                HwMediaFactory.getHwMtpDatabaseManager().hwSaveCurrentObject(newObject.getMtpObjectEx(), null);
                            }
                        }
                        if (c == null) {
                            return;
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoteException in cacheObjectAllInfos", e);
                        if (0 == 0) {
                            return;
                        }
                    } catch (Throwable th) {
                        if (0 != 0) {
                            c.close();
                        }
                        throw th;
                    }
                    c.close();
                }
            }
        }
    }

    @VisibleForNative
    private MtpPropertyList getObjectPropertyList(int handle, int format, int property, int groupCode, int depth) {
        MtpPropertyGroup propertyGroup;
        int handle2 = handle;
        int format2 = format;
        int depth2 = depth;
        if (depth2 == 0) {
            cacheObjectAllInfos(handle);
            MtpPropertyList result = HwMediaFactory.getHwMtpDatabaseManager().getObjectPropertyList(handle2, format2, property, groupCode);
            if (result != null) {
                return result;
            }
        }
        if (property != 0) {
            int err = -1;
            if (depth2 == -1 && (handle2 == 0 || handle2 == -1)) {
                handle2 = -1;
                depth2 = 0;
            }
            if (!(depth2 == 0 || depth2 == 1)) {
                return new MtpPropertyList(MtpConstants.RESPONSE_SPECIFICATION_BY_DEPTH_UNSUPPORTED);
            }
            List<MtpStorageManager.MtpObject> objs = null;
            MtpStorageManager.MtpObject thisObj = null;
            if (handle2 == -1) {
                objs = this.mManager.getObjects(0, format2, -1);
                if (objs == null) {
                    return new MtpPropertyList(8201);
                }
            } else if (handle2 != 0) {
                MtpStorageManager.MtpObject obj = this.mManager.getObject(handle2);
                if (obj == null) {
                    return new MtpPropertyList(8201);
                }
                if (obj.getFormat() == format2 || format2 == 0) {
                    thisObj = obj;
                }
            }
            if (handle2 == 0 || depth2 == 1) {
                if (handle2 == 0) {
                    handle2 = -1;
                }
                objs = this.mManager.getObjects(handle2, format2, -1);
                if (objs == null) {
                    return new MtpPropertyList(8201);
                }
            }
            if (objs == null) {
                objs = new ArrayList<>();
            }
            if (thisObj != null) {
                objs.add(thisObj);
            }
            MtpPropertyList ret = new MtpPropertyList(8193);
            for (MtpStorageManager.MtpObject obj2 : objs) {
                if (property == err) {
                    if (!(format2 != 0 || handle2 == 0 || handle2 == err)) {
                        format2 = obj2.getFormat();
                    }
                    propertyGroup = this.mPropertyGroupsByFormat.get(format2);
                    if (propertyGroup == null) {
                        propertyGroup = new MtpPropertyGroup(getSupportedObjectProperties(format2));
                        this.mPropertyGroupsByFormat.put(format2, propertyGroup);
                    }
                } else {
                    propertyGroup = this.mPropertyGroupsByProperty.get(property);
                    if (propertyGroup == null) {
                        propertyGroup = new MtpPropertyGroup(new int[]{property});
                        this.mPropertyGroupsByProperty.put(property, propertyGroup);
                    }
                }
                int err2 = propertyGroup.getPropertyList(this.mMediaProvider, obj2.getVolumeName(), obj2, ret);
                if (err2 != 8193) {
                    return new MtpPropertyList(err2);
                }
                err = -1;
            }
            return ret;
        } else if (groupCode == 0) {
            return new MtpPropertyList(8198);
        } else {
            return new MtpPropertyList(MtpConstants.RESPONSE_SPECIFICATION_BY_GROUP_UNSUPPORTED);
        }
    }

    private int renameFile(int handle, String newName) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return 8201;
        }
        Path oldPath = obj.getPath();
        if (!this.mManager.beginRenameObject(obj, newName)) {
            return 8194;
        }
        Path newPath = obj.getPath();
        boolean success = new ExternalStorageFileImpl(oldPath.toString()).renameTo(new ExternalStorageFileImpl(newPath.toString()));
        try {
            Os.access(oldPath.toString(), OsConstants.F_OK);
            Os.access(newPath.toString(), OsConstants.F_OK);
        } catch (ErrnoException e) {
        }
        if (!this.mManager.endRenameObject(obj, oldPath.getFileName().toString(), success)) {
            Log.e(TAG, "Failed to end rename object");
        }
        if (!success) {
            return 8194;
        }
        ContentValues values = new ContentValues();
        values.put("_data", newPath.toString());
        String[] whereArgs = {oldPath.toString()};
        try {
            this.mMediaProvider.update(MediaStore.Files.getMtpObjectsUri(obj.getVolumeName()), values, PATH_WHERE, whereArgs);
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in mMediaProvider.update", e2);
        }
        if (obj.isDir()) {
            if (!oldPath.getFileName().startsWith(".") || newPath.startsWith(".")) {
                return 8193;
            }
            MediaStore.scanFile(this.mContext, newPath.toFile());
            return 8193;
        } else if (!oldPath.getFileName().toString().toLowerCase(Locale.US).equals(".nomedia") || newPath.getFileName().toString().toLowerCase(Locale.US).equals(".nomedia")) {
            return 8193;
        } else {
            MediaStore.scanFile(this.mContext, newPath.getParent().toFile());
            return 8193;
        }
    }

    @VisibleForNative
    private int beginMoveObject(int handle, int newParent, int newStorage) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        MtpStorageManager.MtpObject parent = newParent == 0 ? this.mManager.getStorageRoot(newStorage) : this.mManager.getObject(newParent);
        if (obj == null || parent == null) {
            return 8201;
        }
        return this.mManager.beginMoveObject(obj, parent) ? 8193 : 8194;
    }

    @VisibleForNative
    private void endMoveObject(int oldParent, int newParent, int oldStorage, int newStorage, int objId, boolean success) {
        MtpStorageManager.MtpObject oldParentObj = oldParent == 0 ? this.mManager.getStorageRoot(oldStorage) : this.mManager.getObject(oldParent);
        MtpStorageManager.MtpObject newParentObj = newParent == 0 ? this.mManager.getStorageRoot(newStorage) : this.mManager.getObject(newParent);
        String name = this.mManager.getObject(objId).getName();
        if (newParentObj == null || oldParentObj == null || !this.mManager.endMoveObject(oldParentObj, newParentObj, name, success)) {
            Log.e(TAG, "Failed to end move object");
            return;
        }
        MtpStorageManager.MtpObject obj = this.mManager.getObject(objId);
        if (success && obj != null) {
            ContentValues values = new ContentValues();
            Path path = newParentObj.getPath().resolve(name);
            Path oldPath = oldParentObj.getPath().resolve(name);
            values.put("_data", path.toString());
            if (obj.getParent().isRoot()) {
                values.put("parent", (Integer) 0);
            } else {
                int parentId = findInMedia(newParentObj, path.getParent());
                if (parentId != -1) {
                    values.put("parent", Integer.valueOf(parentId));
                } else {
                    deleteFromMedia(obj, oldPath, obj.isDir());
                    return;
                }
            }
            String[] whereArgs = {oldPath.toString()};
            int parentId2 = -1;
            try {
                if (!oldParentObj.isRoot()) {
                    try {
                        parentId2 = findInMedia(oldParentObj, oldPath.getParent());
                    } catch (RemoteException e) {
                        e = e;
                    }
                }
                if (!oldParentObj.isRoot()) {
                    if (parentId2 == -1) {
                        try {
                            MediaStore.scanFile(this.mContext, path.toFile());
                            return;
                        } catch (RemoteException e2) {
                            e = e2;
                            Log.e(TAG, "RemoteException in mMediaProvider.update", e);
                        }
                    }
                }
                this.mMediaProvider.update(MediaStore.Files.getMtpObjectsUri(obj.getVolumeName()), values, PATH_WHERE, whereArgs);
            } catch (RemoteException e3) {
                e = e3;
                Log.e(TAG, "RemoteException in mMediaProvider.update", e);
            }
        }
    }

    @VisibleForNative
    private int beginCopyObject(int handle, int newParent, int newStorage) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        MtpStorageManager.MtpObject parent = newParent == 0 ? this.mManager.getStorageRoot(newStorage) : this.mManager.getObject(newParent);
        if (obj == null || parent == null) {
            return 8201;
        }
        return this.mManager.beginCopyObject(obj, parent);
    }

    @VisibleForNative
    private void endCopyObject(int handle, boolean success) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null || !this.mManager.endCopyObject(obj, success)) {
            Log.e(TAG, "Failed to end copy object");
        } else if (success) {
            MediaStore.scanFile(this.mContext, obj.getPath().toFile());
        }
    }

    @VisibleForNative
    private int setObjectProperty(int handle, int property, long intValue, String stringValue) {
        if (HwMediaFactory.getHwMtpDatabaseManager().hwGetSavedObjectHandle() == handle) {
            HwMediaFactory.getHwMtpDatabaseManager().hwClearSavedObject();
        }
        if (property != 56327) {
            return MtpConstants.RESPONSE_OBJECT_PROP_NOT_SUPPORTED;
        }
        return renameFile(handle, stringValue);
    }

    @VisibleForNative
    private int getDeviceProperty(int property, long[] outIntValue, char[] outStringValue) {
        boolean isDeviceNameUpdate = true;
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL:
                outIntValue[0] = (long) this.mBatteryLevel;
                outIntValue[1] = (long) this.mBatteryScale;
                return 8193;
            case MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE:
                Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
                int width = display.getMaximumSizeDimension();
                int height = display.getMaximumSizeDimension();
                String imageSize = Integer.toString(width) + "x" + Integer.toString(height);
                imageSize.getChars(0, imageSize.length(), outStringValue, 0);
                outStringValue[imageSize.length()] = 0;
                return 8193;
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME:
                String value = this.mDeviceProperties.getString(Integer.toString(property), "");
                if (property == 54274) {
                    if (Settings.Global.getInt(this.mContext.getContentResolver(), SettingsEx.Global.DB_KEY_UNIFIED_DEVICE_NAME_UPDATED, 0) != 1) {
                        isDeviceNameUpdate = false;
                    }
                    if (isDeviceNameUpdate) {
                        value = Settings.Global.getString(this.mContext.getContentResolver(), SettingsEx.Global.DB_KEY_UNIFIED_DEVICE_NAME);
                        Settings.Global.putInt(this.mContext.getContentResolver(), SettingsEx.Global.DB_KEY_UNIFIED_DEVICE_NAME_UPDATED, 0);
                        SharedPreferences.Editor e = this.mDeviceProperties.edit();
                        e.putString(Integer.toString(property), value);
                        e.commit();
                    }
                    if (value == null || value.equals("")) {
                        value = SystemProperties.get("ro.config.marketing_name");
                    }
                    if (value == null || value.equals("")) {
                        value = SystemProperties.get("ro.product.model");
                    }
                }
                int length = value.length();
                if (length > 255) {
                    length = 255;
                }
                value.getChars(0, length, outStringValue, 0);
                outStringValue[length] = 0;
                return 8193;
            case MtpConstants.DEVICE_PROPERTY_PERCEIVED_DEVICE_TYPE:
                outIntValue[0] = (long) this.mDeviceType;
                return 8193;
            default:
                return 8202;
        }
    }

    @VisibleForNative
    private int setDeviceProperty(int property, long intValue, String stringValue) {
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME:
                SharedPreferences.Editor e = this.mDeviceProperties.edit();
                e.putString(Integer.toString(property), stringValue);
                if (e.commit()) {
                    return 8193;
                }
                return 8194;
            default:
                return 8202;
        }
    }

    @VisibleForNative
    private boolean getObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outCreatedModified) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return false;
        }
        outStorageFormatParent[0] = obj.getStorageId();
        outStorageFormatParent[1] = obj.getFormat();
        outStorageFormatParent[2] = obj.getParent().isRoot() ? 0 : obj.getParent().getId();
        int nameLen = Integer.min(obj.getName().length(), 255);
        obj.getName().getChars(0, nameLen, outName, 0);
        outName[nameLen] = 0;
        outCreatedModified[0] = obj.getModifiedTime();
        outCreatedModified[1] = obj.getModifiedTime();
        return true;
    }

    private boolean isExternalStoragePath(String path) {
        return path.startsWith("/storage/") && !path.startsWith("/storage/emulated/");
    }

    @VisibleForNative
    private int getObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        String path;
        String pathEx;
        int i = sHandleOffsetForHisuite;
        if (handle > i) {
            Cursor c = null;
            try {
                Cursor c2 = this.mMediaProvider.query(this.mObjectsUri, PATH_FORMAT_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle - i)}, null, null);
                if (c2 == null || !c2.moveToNext()) {
                    if (c2 != null) {
                        c2.close();
                    }
                    return 8201;
                }
                String path2 = c2.getString(DATABASE_COLUMN_INDEX_FIR);
                if (isExternalStoragePath(path2)) {
                    pathEx = path2.replaceFirst("/storage/", "/mnt/media_rw/");
                } else {
                    pathEx = path2;
                }
                pathEx.getChars(0, pathEx.length(), outFilePath, 0);
                outFilePath[pathEx.length()] = 0;
                outFileLengthFormat[0] = new File(pathEx).length();
                outFileLengthFormat[1] = c2.getLong(DATABASE_COLUMN_INDEX_THI);
                c2.close();
                return 8193;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in getObjectFilePath", e);
                if (0 != 0) {
                    c.close();
                }
                return 8194;
            } catch (Throwable th) {
                if (0 != 0) {
                    c.close();
                }
                throw th;
            }
        } else {
            MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
            if (obj == null) {
                return 8201;
            }
            String oldPath = obj.getPath().toString();
            if (isExternalStoragePath(oldPath)) {
                path = oldPath.replaceFirst("/storage/", "/mnt/media_rw/");
            } else {
                path = oldPath;
            }
            int pathLen = Integer.min(path.length(), 4096);
            path.getChars(0, pathLen, outFilePath, 0);
            outFilePath[pathLen] = 0;
            outFileLengthFormat[0] = obj.getSize();
            outFileLengthFormat[1] = (long) obj.getFormat();
            return 8193;
        }
    }

    private int getObjectFormat(int handle) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return -1;
        }
        return obj.getFormat();
    }

    @VisibleForNative
    private int beginDeleteObject(int handle) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return 8201;
        }
        if (!this.mManager.beginRemoveObject(obj)) {
            return 8194;
        }
        return 8193;
    }

    @VisibleForNative
    private void endDeleteObject(int handle, boolean success) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj != null) {
            if (!this.mManager.endRemoveObject(obj, success)) {
                Log.e(TAG, "Failed to end remove object");
            }
            if (success) {
                deleteFromMedia(obj, obj.getPath(), obj.isDir());
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0057, code lost:
        if (0 != 0) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005a, code lost:
        return r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x002f, code lost:
        if (r9 != null) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0031, code lost:
        r9.close();
     */
    private int findInMedia(MtpStorageManager.MtpObject obj, Path path) {
        Uri objectsUri = MediaStore.Files.getMtpObjectsUri(obj.getVolumeName());
        int ret = -1;
        Cursor c = null;
        try {
            c = this.mMediaProvider.query(objectsUri, ID_PROJECTION, PATH_WHERE, new String[]{path.toString()}, null, null);
            if (c != null && c.moveToNext()) {
                ret = c.getInt(0);
            }
        } catch (RemoteException e) {
            if (DEBUG) {
                String str = TAG;
                Log.e(str, "Error finding " + path + " in MediaProvider");
            }
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private void deleteFromMedia(MtpStorageManager.MtpObject obj, Path path, boolean isDir) {
        Uri objectsUri = MediaStore.Files.getMtpObjectsUri(obj.getVolumeName());
        if (isDir) {
            try {
                ContentProviderClient contentProviderClient = this.mMediaProvider;
                contentProviderClient.delete(objectsUri, "_data LIKE ?1 AND lower(substr(_data,1,?2))=lower(?3)", new String[]{path + "/%", Integer.toString(path.toString().length() + 1), path.toString() + "/"});
            } catch (Exception e) {
                if (DEBUG) {
                    String str = TAG;
                    Log.d(str, "Failed to delete " + path + " from MediaProvider");
                    return;
                }
                return;
            }
        }
        if (this.mMediaProvider.delete(objectsUri, PATH_WHERE, new String[]{path.toString()}) > 0) {
            if (!isDir && path.toString().toLowerCase(Locale.US).endsWith(".nomedia")) {
                MediaStore.scanFile(this.mContext, path.getParent().toFile());
            }
        } else if (DEBUG) {
            String str2 = TAG;
            Log.i(str2, "Mediaprovider didn't delete " + path);
        }
    }

    @VisibleForNative
    private int[] getObjectReferences(int handle) {
        MtpStorageManager.MtpObject obj;
        int handle2;
        if (HwMediaFactory.getHwMtpDatabaseManager().hwGetObjectReferences(handle) || (obj = this.mManager.getObject(handle)) == null || (handle2 = findInMedia(obj, obj.getPath())) == -1) {
            return null;
        }
        Cursor c = null;
        try {
            Cursor c2 = this.mMediaProvider.query(MediaStore.Files.getMtpReferencesUri(obj.getVolumeName(), (long) handle2), PATH_PROJECTION, null, null, null, null);
            if (c2 == null) {
                if (c2 != null) {
                    c2.close();
                }
                return null;
            }
            ArrayList<Integer> result = new ArrayList<>();
            while (c2.moveToNext()) {
                MtpStorageManager.MtpObject refObj = this.mManager.getByPath(c2.getString(0));
                if (refObj != null) {
                    result.add(Integer.valueOf(refObj.getId()));
                }
            }
            int[] array = result.stream().mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
            c2.close();
            return array;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectList", e);
            if (0 != 0) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    @VisibleForNative
    private int setObjectReferences(int handle, int[] references) {
        int refHandle;
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return 8201;
        }
        int handle2 = findInMedia(obj, obj.getPath());
        int i = -1;
        if (handle2 == -1) {
            return 8194;
        }
        Uri uri = MediaStore.Files.getMtpReferencesUri(obj.getVolumeName(), (long) handle2);
        ArrayList<ContentValues> valuesList = new ArrayList<>();
        int length = references.length;
        int i2 = 0;
        while (i2 < length) {
            MtpStorageManager.MtpObject refObj = this.mManager.getObject(references[i2]);
            if (!(refObj == null || (refHandle = findInMedia(refObj, refObj.getPath())) == i)) {
                ContentValues values = new ContentValues();
                values.put("_id", Integer.valueOf(refHandle));
                valuesList.add(values);
            }
            i2++;
            i = -1;
        }
        try {
            if (this.mMediaProvider.bulkInsert(uri, (ContentValues[]) valuesList.toArray(new ContentValues[0])) > 0) {
                return 8193;
            }
            return 8194;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setObjectReferences", e);
        }
    }

    public static void setHandleOffsetForHisuite(int offset) {
        sHandleOffsetForHisuite = offset;
    }

    public static int getHandleOffsetForHisuite() {
        return sHandleOffsetForHisuite;
    }

    private boolean inStorageRoot(String path) {
        try {
            String canonical = new File(path).getCanonicalPath();
            for (String root : this.mStorageMap.keySet()) {
                if (canonical.startsWith(root)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            Log.e(TAG, "inStorageRoot ioException!");
            return false;
        }
    }

    private boolean endSendObjectHisuite(int handle, boolean succeeded) {
        if (handle < sHandleOffsetForHisuite) {
            return false;
        }
        MediaStore.scanFile(this.mContext, new File(this.mSendobjectPath));
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006d, code lost:
        if (r2 != null) goto L_0x007b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0079, code lost:
        if (0 == 0) goto L_0x0085;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007b, code lost:
        r2.close();
     */
    private int beginSendObjectHisuite(String path, int format, int parent, int storageId) {
        if (path == null || inStorageRoot(path)) {
            if (path != null) {
                Cursor c = null;
                try {
                    c = this.mMediaProvider.query(this.mObjectsUri, ID_PROJECTION, PATH_WHERE, new String[]{path}, null, null);
                    if (c != null && c.getCount() > 0) {
                        if (DEBUG) {
                            String str = TAG;
                            Log.w(str, "file already exists in beginSendObject: " + path);
                        }
                        int hwBeginSendObject = HwMediaFactory.getHwMtpDatabaseManager().hwBeginSendObject(path, c);
                        c.close();
                        return hwBeginSendObject;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in beginSendObject", e);
                } catch (Throwable th) {
                    if (0 != 0) {
                        c.close();
                    }
                    throw th;
                }
            }
            this.mSendObjectFormat = format;
            this.mSendobjectPath = path;
            ContentValues values = new ContentValues();
            values.put("_data", path);
            values.put("format", Integer.valueOf(format));
            values.put("parent", Integer.valueOf(parent - sHandleOffsetForHisuite));
            values.put(MediaStore.Files.FileColumns.STORAGE_ID, Integer.valueOf(storageId));
            try {
                Uri uri = this.mMediaProvider.insert(this.mObjectsUri, values);
                if (uri != null) {
                    return Integer.parseInt(uri.getPathSegments().get(DATABASE_COLUMN_INDEX_SEC));
                }
                return -1;
            } catch (RemoteException e2) {
                Log.e(TAG, "RemoteException in beginSendObject", e2);
                return -1;
            }
        } else {
            if (DEBUG) {
                String str2 = TAG;
                Log.e(str2, "attempt to put file outside of storage area: " + path);
            }
            return -1;
        }
    }

    private String getVolumeName(StorageVolume volume) {
        if (volume.isPrimary()) {
            return MediaStore.VOLUME_EXTERNAL_PRIMARY;
        }
        return volume.getNormalizedUuid();
    }
}
