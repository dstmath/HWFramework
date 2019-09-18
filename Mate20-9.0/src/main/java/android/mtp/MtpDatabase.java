package android.mtp;

import android.app.DownloadManager;
import android.app.slice.Slice;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.HeifUtils;
import android.media.MediaFormat;
import android.media.MediaScanner;
import android.media.midi.MidiDeviceInfo;
import android.mtp.MtpStorageManager;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.storage.ExternalStorageFileImpl;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.google.android.collect.Sets;
import dalvik.system.CloseGuard;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MtpDatabase implements AutoCloseable {
    private static final int[] AUDIO_PROPERTIES = {MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_ALBUM_ARTIST, MtpConstants.PROPERTY_TRACK, MtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_GENRE, MtpConstants.PROPERTY_COMPOSER, MtpConstants.PROPERTY_AUDIO_WAVE_CODEC, MtpConstants.PROPERTY_BITRATE_TYPE, MtpConstants.PROPERTY_AUDIO_BITRATE, MtpConstants.PROPERTY_NUMBER_OF_CHANNELS, MtpConstants.PROPERTY_SAMPLE_RATE};
    private static int DATABASE_COLUMN_INDEX_FIR = 1;
    private static int DATABASE_COLUMN_INDEX_FOR = 4;
    private static int DATABASE_COLUMN_INDEX_SEC = 2;
    private static int DATABASE_COLUMN_INDEX_THI = 3;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int[] DEVICE_PROPERTIES = {MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER, MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME, MtpConstants.DEVICE_PROPERTY_IMAGE_SIZE, MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL, MtpConstants.DEVICE_PROPERTY_PERCEIVED_DEVICE_TYPE};
    private static final int[] FILE_PROPERTIES = {MtpConstants.PROPERTY_STORAGE_ID, MtpConstants.PROPERTY_OBJECT_FORMAT, MtpConstants.PROPERTY_PROTECTION_STATUS, MtpConstants.PROPERTY_OBJECT_SIZE, MtpConstants.PROPERTY_OBJECT_FILE_NAME, MtpConstants.PROPERTY_DATE_MODIFIED, MtpConstants.PROPERTY_PERSISTENT_UID, MtpConstants.PROPERTY_PARENT_OBJECT, MtpConstants.PROPERTY_NAME, MtpConstants.PROPERTY_DISPLAY_NAME, MtpConstants.PROPERTY_DATE_ADDED};
    private static final int GALLERY_HEIF_SETTING_AUTOMATIC = 0;
    private static int HANDLE_OFFSET_FOR_HISUITE = 10000000;
    private static final String[] ID_PROJECTION = {DownloadManager.COLUMN_ID};
    private static final String ID_WHERE = "_id=?";
    private static final int[] IMAGE_PROPERTIES = {56392};
    private static final String MTP_TRANSFER_TEMP_FOLDER = "/data/data/com.android.providers.media/mtp_trans_cache";
    private static final String NO_MEDIA = ".nomedia";
    private static final String[] OBJECT_ALL_COLUMNS_PROJECTION = {DownloadManager.COLUMN_ID, "storage_id", "format", "parent", "_data", "_size", "date_added", "date_modified", "mime_type", "title", "_display_name", DownloadManager.COLUMN_MEDIA_TYPE, "is_drm", MediaFormat.KEY_WIDTH, MediaFormat.KEY_HEIGHT, "description", "artist", "album", "album_artist", "duration", "track", "composer", "year", MidiDeviceInfo.PROPERTY_NAME};
    private static final String[] OBJECT_INFO_PROJECTION = {DownloadManager.COLUMN_ID, "storage_id", "format", "parent", "_data", "_size", "date_added", "date_modified"};
    private static final String[] PATH_FORMAT_PROJECTION = {DownloadManager.COLUMN_ID, "_data", "_size", "format"};
    private static final String[] PATH_PROJECTION = {"_data"};
    private static final String[] PATH_PROJECTION_FOR_HISUITE = {DownloadManager.COLUMN_ID, "_data", "storage_id"};
    private static final String PATH_WHERE = "_data=?";
    private static final int[] PLAYBACK_FORMATS = {MtpConstants.FORMAT_UNDEFINED, MtpConstants.FORMAT_ASSOCIATION, MtpConstants.FORMAT_TEXT, MtpConstants.FORMAT_HTML, MtpConstants.FORMAT_WAV, MtpConstants.FORMAT_MP3, MtpConstants.FORMAT_MPEG, MtpConstants.FORMAT_EXIF_JPEG, MtpConstants.FORMAT_TIFF_EP, MtpConstants.FORMAT_BMP, MtpConstants.FORMAT_GIF, MtpConstants.FORMAT_JFIF, MtpConstants.FORMAT_PNG, MtpConstants.FORMAT_TIFF, MtpConstants.FORMAT_WMA, MtpConstants.FORMAT_OGG, MtpConstants.FORMAT_AAC, MtpConstants.FORMAT_MP4_CONTAINER, MtpConstants.FORMAT_MP2, MtpConstants.FORMAT_3GP_CONTAINER, MtpConstants.FORMAT_ABSTRACT_AV_PLAYLIST, MtpConstants.FORMAT_WPL_PLAYLIST, MtpConstants.FORMAT_M3U_PLAYLIST, MtpConstants.FORMAT_PLS_PLAYLIST, MtpConstants.FORMAT_XML_DOCUMENT, MtpConstants.FORMAT_FLAC, MtpConstants.FORMAT_DNG, MtpConstants.FORMAT_HEIF};
    private static final Uri QUERY_GALLERY_HEIF_SETTING_URI = Uri.parse("content://com.android.gallery3d.provider.GallerySettingsProvider.provider");
    private static final String SUFFIX_JPG_FILE = ".jpg";
    /* access modifiers changed from: private */
    public static final String TAG = MtpDatabase.class.getSimpleName();
    private static final int[] VIDEO_PROPERTIES = {MtpConstants.PROPERTY_ARTIST, MtpConstants.PROPERTY_ALBUM_NAME, MtpConstants.PROPERTY_DURATION, MtpConstants.PROPERTY_DESCRIPTION};
    private static int sHandleOffsetForHisuite = HANDLE_OFFSET_FOR_HISUITE;
    private static boolean sIsHeifSettingAutomaticMode;
    /* access modifiers changed from: private */
    public int mBatteryLevel;
    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int unused = MtpDatabase.this.mBatteryScale = intent.getIntExtra("scale", 0);
                int newLevel = intent.getIntExtra(MediaFormat.KEY_LEVEL, 0);
                if (newLevel != MtpDatabase.this.mBatteryLevel) {
                    int unused2 = MtpDatabase.this.mBatteryLevel = newLevel;
                    try {
                        MtpDatabase.this.mServer.sendDevicePropertyChanged(MtpConstants.DEVICE_PROPERTY_BATTERY_LEVEL);
                    } catch (NullPointerException e) {
                        Log.e(MtpDatabase.TAG, "mServer already set to null");
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mBatteryScale;
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    private final Context mContext;
    private SharedPreferences mDeviceProperties;
    private int mDeviceType;
    private MtpStorageManager mManager;
    private final ContentProviderClient mMediaProvider;
    private final MediaScanner mMediaScanner;
    private long mNativeContext;
    private final Uri mObjectsUri;
    private final HashMap<Integer, MtpPropertyGroup> mPropertyGroupsByFormat = new HashMap<>();
    private final HashMap<Integer, MtpPropertyGroup> mPropertyGroupsByProperty = new HashMap<>();
    private int mSendObjectFormat;
    private String mSendobjectPath;
    /* access modifiers changed from: private */
    public MtpServer mServer;
    private final HashMap<String, MtpStorage> mStorageMap = new HashMap<>();
    private final String mVolumeName;

    private final native void native_finalize();

    private final native void native_setup();

    static {
        System.loadLibrary("media_jni");
    }

    private int[] getSupportedObjectProperties(int format) {
        switch (format) {
            case MtpConstants.FORMAT_WAV:
            case MtpConstants.FORMAT_MP3:
            case MtpConstants.FORMAT_WMA:
            case MtpConstants.FORMAT_OGG:
            case MtpConstants.FORMAT_AAC:
                return IntStream.concat(Arrays.stream(FILE_PROPERTIES), Arrays.stream(AUDIO_PROPERTIES)).toArray();
            case MtpConstants.FORMAT_MPEG:
            case MtpConstants.FORMAT_WMV:
            case MtpConstants.FORMAT_3GP_CONTAINER:
                return IntStream.concat(Arrays.stream(FILE_PROPERTIES), Arrays.stream(VIDEO_PROPERTIES)).toArray();
            case MtpConstants.FORMAT_EXIF_JPEG:
            case MtpConstants.FORMAT_BMP:
            case MtpConstants.FORMAT_GIF:
            case MtpConstants.FORMAT_PNG:
            case MtpConstants.FORMAT_DNG:
            case MtpConstants.FORMAT_HEIF:
                return IntStream.concat(Arrays.stream(FILE_PROPERTIES), Arrays.stream(IMAGE_PROPERTIES)).toArray();
            default:
                return FILE_PROPERTIES;
        }
    }

    private int[] getSupportedDeviceProperties() {
        return DEVICE_PROPERTIES;
    }

    private int[] getSupportedPlaybackFormats() {
        return PLAYBACK_FORMATS;
    }

    private int[] getSupportedCaptureFormats() {
        return null;
    }

    public MtpDatabase(Context context, String volumeName, String[] subDirectories) {
        native_setup();
        this.mContext = context;
        this.mMediaProvider = context.getContentResolver().acquireContentProviderClient("media");
        this.mVolumeName = volumeName;
        this.mObjectsUri = MediaStore.Files.getMtpObjectsUri(volumeName).buildUpon().appendQueryParameter("nonotify", "1").build();
        this.mMediaScanner = new MediaScanner(context, this.mVolumeName);
        this.mManager = new MtpStorageManager(new MtpStorageManager.MtpNotifier() {
            public void sendObjectAdded(int id) {
                if (MtpDatabase.this.mServer != null) {
                    MtpDatabase.this.mServer.sendObjectAdded(id);
                }
            }

            public void sendObjectRemoved(int id) {
                if (MtpDatabase.this.mServer != null) {
                    MtpDatabase.this.mServer.sendObjectRemoved(id);
                }
            }
        }, subDirectories == null ? null : Sets.newHashSet(subDirectories));
        initDeviceProperties(context);
        this.mDeviceType = SystemProperties.getInt("sys.usb.mtp.device_type", 0);
        fetchGalleryHeifSetting();
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
        this.mManager.close();
        HwFrameworkFactory.getHwMtpDatabaseManager().hwClearSavedObject();
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            this.mMediaScanner.close();
            if (this.mMediaProvider != null) {
                this.mMediaProvider.close();
            }
            native_finalize();
        }
    }

    /* access modifiers changed from: protected */
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

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00ba, code lost:
        if (r0 != null) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00bc, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00d0, code lost:
        if (r0 == null) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00d3, code lost:
        return;
     */
    public void addStorage(StorageVolume storage) {
        Cursor c = null;
        try {
            c = this.mMediaProvider.query(this.mObjectsUri, PATH_PROJECTION_FOR_HISUITE, PATH_WHERE, new String[]{storage.getPath()}, null, null);
            if (c == null || !c.moveToNext()) {
                ContentValues values = new ContentValues();
                values.put("_data", storage.getPath());
                values.put("format", Integer.valueOf(MtpConstants.FORMAT_ASSOCIATION));
                values.put("date_modified", Long.valueOf(new File(storage.getPath()).lastModified()));
                try {
                    this.mMediaProvider.insert(this.mObjectsUri, values);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in addStorage", e);
                }
                if (c != null) {
                    c.close();
                }
                c = this.mMediaProvider.query(this.mObjectsUri, PATH_PROJECTION_FOR_HISUITE, PATH_WHERE, new String[]{storage.getPath()}, null, null);
                if (c != null) {
                    if (!c.moveToNext()) {
                    }
                }
                if (c != null) {
                    c.close();
                }
                return;
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
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    public void removeStorage(StorageVolume storage) {
        MtpStorage mtpStorage = this.mStorageMap.get(storage.getPath());
        if (mtpStorage != null) {
            if (this.mServer != null) {
                this.mServer.removeStorage(mtpStorage);
            }
            this.mManager.removeMtpStorage(mtpStorage);
            this.mStorageMap.remove(storage.getPath());
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0082  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x008e  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0093  */
    private void initDeviceProperties(Context context) {
        Context context2 = context;
        this.mDeviceProperties = context2.getSharedPreferences("device-properties", 0);
        if (context2.getDatabasePath("device-properties").exists()) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                SQLiteDatabase db2 = context2.openOrCreateDatabase("device-properties", 0, null);
                if (db2 != null) {
                    try {
                        c = db2.query("properties", new String[]{DownloadManager.COLUMN_ID, "code", Slice.SUBTYPE_VALUE}, null, null, null, null, null);
                        if (c != null) {
                            SharedPreferences.Editor e = this.mDeviceProperties.edit();
                            while (c.moveToNext()) {
                                e.putString(c.getString(1), c.getString(2));
                            }
                            e.commit();
                        }
                    } catch (Exception e2) {
                        e = e2;
                        db = db2;
                        try {
                            Log.e(TAG, "failed to migrate device properties", e);
                            if (c != null) {
                                c.close();
                            }
                            if (db != null) {
                                db.close();
                            }
                            SQLiteDatabase sQLiteDatabase = db;
                            context2.deleteDatabase("device-properties");
                        } catch (Throwable th) {
                            th = th;
                            db2 = db;
                            if (c != null) {
                                c.close();
                            }
                            if (db2 != null) {
                                db2.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (c != null) {
                        }
                        if (db2 != null) {
                        }
                        throw th;
                    }
                }
                if (c != null) {
                    c.close();
                }
                if (db2 != null) {
                    db2.close();
                }
            } catch (Exception e3) {
                e = e3;
                Log.e(TAG, "failed to migrate device properties", e);
                if (c != null) {
                }
                if (db != null) {
                }
                SQLiteDatabase sQLiteDatabase2 = db;
                context2.deleteDatabase("device-properties");
            }
            context2.deleteDatabase("device-properties");
        }
    }

    private int beginSendObject(String path, int format, int parent, int storageId) {
        if (parent > sHandleOffsetForHisuite) {
            return beginSendObjectHisuite(path, format, parent, storageId) + sHandleOffsetForHisuite;
        }
        MtpStorageManager.MtpObject parentObj = parent == 0 ? this.mManager.getStorageRoot(storageId) : this.mManager.getObject(parent);
        if (parentObj == null) {
            return -1;
        }
        return this.mManager.beginSendObject(parentObj, Paths.get(path, new String[0]).getFileName().toString(), format);
    }

    private void endSendObject(int handle, boolean succeeded) {
        if (!endSendObjectHisuite(handle, succeeded)) {
            MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
            if (obj == null || !this.mManager.endSendObject(obj, succeeded)) {
                Log.e(TAG, "Failed to successfully end send object");
                return;
            }
            if (succeeded) {
                String path = obj.getPath().toString();
                int format = obj.getFormat();
                ContentValues values = new ContentValues();
                values.put("_data", path);
                values.put("format", Integer.valueOf(format));
                values.put("_size", Long.valueOf(obj.getSize()));
                values.put("date_modified", Long.valueOf(obj.getModifiedTime()));
                try {
                    if (obj.getParent().isRoot()) {
                        values.put("parent", (Integer) 0);
                    } else {
                        int parentId = findInMedia(obj.getParent().getPath());
                        if (parentId != -1) {
                            values.put("parent", Integer.valueOf(parentId));
                        } else {
                            return;
                        }
                    }
                    Uri uri = this.mMediaProvider.insert(this.mObjectsUri, values);
                    if (uri != null) {
                        rescanFile(path, Integer.parseInt(uri.getPathSegments().get(2)), format);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in beginSendObject", e);
                }
            }
        }
    }

    private void rescanFile(String path, int handle, int format) {
        if (format == 47621) {
            String name = path;
            int lastSlash = name.lastIndexOf(47);
            if (lastSlash >= 0) {
                name = name.substring(lastSlash + 1);
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
                this.mMediaProvider.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in endSendObject", e);
            }
        } else {
            this.mMediaScanner.scanMtpFile(path, handle, format);
        }
    }

    private int[] getObjectList(int storageID, int format, int parent) {
        Stream<MtpStorageManager.MtpObject> objectStream = this.mManager.getObjects(parent, format, storageID);
        if (objectStream == null) {
            return null;
        }
        return objectStream.mapToInt($$Lambda$iwOv5HKUnGm7PVU3weoI9JmsXc.INSTANCE).toArray();
    }

    private int getNumObjects(int storageID, int format, int parent) {
        Stream<MtpStorageManager.MtpObject> objectStream = this.mManager.getObjects(parent, format, storageID);
        if (objectStream == null) {
            return -1;
        }
        return (int) objectStream.count();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0060, code lost:
        if (r0 != null) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0062, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0070, code lost:
        if (r0 == null) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0073, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ad, code lost:
        if (r0 != null) goto L_0x00af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00af, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00bd, code lost:
        if (r0 == null) goto L_0x00c0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00c0, code lost:
        return;
     */
    private void cacheObjectAllInfos(int handle) {
        if (handle > 0) {
            Cursor c = null;
            if (HwFrameworkFactory.getHwMtpDatabaseManager().hwGetSavedObjectHandle() != handle) {
                boolean z = false;
                if (handle > sHandleOffsetForHisuite) {
                    try {
                        c = this.mMediaProvider.query(this.mObjectsUri, OBJECT_ALL_COLUMNS_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle - sHandleOffsetForHisuite)}, null, null);
                        if (c != null && c.moveToNext()) {
                            String string = c.getString(DATABASE_COLUMN_INDEX_FOR);
                            if (c.getInt(DATABASE_COLUMN_INDEX_SEC) == 12289) {
                                z = true;
                            }
                            MtpStorageManager.MtpObject newObject = new MtpStorageManager.MtpObject(string, handle, null, z);
                            newObject.setStorageId(c.getInt(DATABASE_COLUMN_INDEX_FIR));
                            HwFrameworkFactory.getHwMtpDatabaseManager().hwSaveCurrentObject(newObject, c);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoteException in cacheObjectAllInfos", e);
                    } catch (Throwable th) {
                        if (c != null) {
                            c.close();
                        }
                        throw th;
                    }
                } else {
                    MtpStorageManager.MtpObject newObject2 = this.mManager.getObject(handle);
                    if (newObject2 != null) {
                        try {
                            c = this.mMediaProvider.query(this.mObjectsUri, OBJECT_ALL_COLUMNS_PROJECTION, PATH_WHERE, new String[]{newObject2.getPath().toString()}, null, null);
                            if (c != null && c.moveToNext()) {
                                HwFrameworkFactory.getHwMtpDatabaseManager().hwSaveCurrentObject(newObject2, c);
                            }
                        } catch (RemoteException e2) {
                            Log.e(TAG, "RemoteException in cacheObjectAllInfos", e2);
                        } catch (Throwable th2) {
                            if (c != null) {
                                c.close();
                            }
                            throw th2;
                        }
                    }
                }
            }
        }
    }

    private MtpPropertyList getObjectPropertyList(int handle, int format, int property, int groupCode, int depth) {
        int handle2;
        MtpPropertyGroup propertyGroup;
        int i = handle;
        int i2 = format;
        int i3 = property;
        int depth2 = depth;
        if (depth2 == 0) {
            cacheObjectAllInfos(handle);
            MtpPropertyList result = HwFrameworkFactory.getHwMtpDatabaseManager().getObjectPropertyList(this, i, i2, i3, groupCode);
            if (result != null) {
                return result;
            }
        }
        if (i3 != 0) {
            int i4 = -1;
            if (depth2 == -1 && (i == 0 || i == -1)) {
                handle2 = -1;
                depth2 = 0;
            } else {
                handle2 = i;
            }
            int i5 = 1;
            if (depth2 != 0 && depth2 != 1) {
                return new MtpPropertyList(MtpConstants.RESPONSE_SPECIFICATION_BY_DEPTH_UNSUPPORTED);
            }
            Stream<MtpStorageManager.MtpObject> objectStream = Stream.of(new MtpStorageManager.MtpObject[0]);
            if (handle2 == -1) {
                objectStream = this.mManager.getObjects(0, i2, -1);
                if (objectStream == null) {
                    return new MtpPropertyList(MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
                }
            } else if (handle2 != 0) {
                MtpStorageManager.MtpObject obj = this.mManager.getObject(handle2);
                if (obj == null) {
                    return new MtpPropertyList(MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
                }
                if (obj.getFormat() == i2 || i2 == 0) {
                    objectStream = Stream.of(obj);
                }
            }
            if (handle2 == 0 || depth2 == 1) {
                if (handle2 == 0) {
                    handle2 = -1;
                }
                Stream<MtpStorageManager.MtpObject> childStream = this.mManager.getObjects(handle2, i2, -1);
                if (childStream == null) {
                    return new MtpPropertyList(MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
                }
                objectStream = Stream.concat(objectStream, childStream);
            }
            MtpPropertyList ret = new MtpPropertyList(MtpConstants.RESPONSE_OK);
            for (MtpStorageManager.MtpObject obj2 : objectStream) {
                if (i3 == i4) {
                    propertyGroup = this.mPropertyGroupsByFormat.get(Integer.valueOf(obj2.getFormat()));
                    if (propertyGroup == null) {
                        propertyGroup = new MtpPropertyGroup(this.mMediaProvider, this.mVolumeName, getSupportedObjectProperties(i2));
                        this.mPropertyGroupsByFormat.put(Integer.valueOf(format), propertyGroup);
                    }
                } else {
                    int[] propertyList = new int[i5];
                    propertyList[0] = i3;
                    propertyGroup = this.mPropertyGroupsByProperty.get(Integer.valueOf(property));
                    if (propertyGroup == null) {
                        propertyGroup = new MtpPropertyGroup(this.mMediaProvider, this.mVolumeName, propertyList);
                        this.mPropertyGroupsByProperty.put(Integer.valueOf(property), propertyGroup);
                    }
                }
                int err = propertyGroup.getPropertyList(obj2, ret);
                if (err != 8193) {
                    return new MtpPropertyList(err);
                }
                i4 = -1;
                i5 = 1;
            }
            return ret;
        } else if (groupCode == 0) {
            return new MtpPropertyList(MtpConstants.RESPONSE_PARAMETER_NOT_SUPPORTED);
        } else {
            return new MtpPropertyList(MtpConstants.RESPONSE_SPECIFICATION_BY_GROUP_UNSUPPORTED);
        }
    }

    private int renameFile(int handle, String newName) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
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
        int oldHandle = -1;
        try {
            oldHandle = this.mMediaProvider.update(this.mObjectsUri, values, PATH_WHERE, new String[]{oldPath.toString()});
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in mMediaProvider.update", e2);
        }
        if (oldHandle <= 0) {
            if (obj.isDir()) {
                ContentValues values2 = new ContentValues();
                values2.put("_size", Long.valueOf(obj.getSize()));
                values2.put("date_modified", Long.valueOf(obj.getModifiedTime()));
                values2.put("format", Integer.valueOf(MtpConstants.FORMAT_ASSOCIATION));
                values2.put("_data", newPath.toString());
                try {
                    this.mMediaProvider.insert(this.mObjectsUri, values2);
                } catch (RemoteException e3) {
                    Log.e(TAG, "RemoteException in scanMtpFile", e3);
                }
            } else {
                this.mMediaScanner.scanSingleFile(newPath.toString(), null);
            }
            return MtpConstants.RESPONSE_OK;
        }
        if (obj.isDir()) {
            if (oldPath.getFileName().startsWith(".") && !newPath.startsWith(".")) {
                try {
                    this.mMediaProvider.call("unhide", newPath.toString(), null);
                } catch (RemoteException e4) {
                    if (DEBUG) {
                        Log.e(TAG, "failed to unhide/rescan for " + newPath);
                    }
                }
            }
        } else if (oldPath.getFileName().toString().toLowerCase(Locale.US).equals(NO_MEDIA) && !newPath.getFileName().toString().toLowerCase(Locale.US).equals(NO_MEDIA)) {
            try {
                this.mMediaProvider.call("unhide", oldPath.getParent().toString(), null);
            } catch (RemoteException e5) {
                if (DEBUG) {
                    Log.e(TAG, "failed to unhide/rescan for " + newPath);
                }
            }
        }
        return MtpConstants.RESPONSE_OK;
    }

    private int beginMoveObject(int handle, int newParent, int newStorage) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        MtpStorageManager.MtpObject parent = newParent == 0 ? this.mManager.getStorageRoot(newStorage) : this.mManager.getObject(newParent);
        if (obj == null || parent == null) {
            return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
        }
        return this.mManager.beginMoveObject(obj, parent) ? MtpConstants.RESPONSE_OK : 8194;
    }

    private void endMoveObject(int oldParent, int newParent, int oldStorage, int newStorage, int objId, boolean success) {
        MtpStorageManager.MtpObject mtpObject;
        MtpStorageManager.MtpObject mtpObject2;
        int i = oldParent;
        int i2 = newParent;
        int i3 = objId;
        boolean z = success;
        if (i == 0) {
            mtpObject = this.mManager.getStorageRoot(oldStorage);
        } else {
            int i4 = oldStorage;
            mtpObject = this.mManager.getObject(i);
        }
        MtpStorageManager.MtpObject oldParentObj = mtpObject;
        if (i2 == 0) {
            mtpObject2 = this.mManager.getStorageRoot(newStorage);
        } else {
            int i5 = newStorage;
            mtpObject2 = this.mManager.getObject(i2);
        }
        MtpStorageManager.MtpObject newParentObj = mtpObject2;
        String name = this.mManager.getObject(i3).getName();
        if (newParentObj == null || oldParentObj == null || !this.mManager.endMoveObject(oldParentObj, newParentObj, name, z)) {
            Log.e(TAG, "Failed to end move object");
            return;
        }
        MtpStorageManager.MtpObject obj = this.mManager.getObject(i3);
        if (z && obj != null) {
            ContentValues values = new ContentValues();
            Path path = newParentObj.getPath().resolve(name);
            Path oldPath = oldParentObj.getPath().resolve(name);
            values.put("_data", path.toString());
            if (obj.getParent().isRoot()) {
                values.put("parent", (Integer) 0);
            } else {
                int parentId = findInMedia(path.getParent());
                if (parentId != -1) {
                    values.put("parent", Integer.valueOf(parentId));
                } else {
                    deleteFromMedia(oldPath, obj.isDir());
                    return;
                }
            }
            String[] whereArgs = {oldPath.toString()};
            int parentId2 = -1;
            try {
                if (!oldParentObj.isRoot()) {
                    try {
                        parentId2 = findInMedia(oldPath.getParent());
                    } catch (RemoteException e) {
                        e = e;
                        Log.e(TAG, "RemoteException in mMediaProvider.update", e);
                    }
                }
                if (oldParentObj.isRoot()) {
                    int i6 = parentId2;
                } else if (parentId2 != -1) {
                    int i7 = parentId2;
                } else {
                    int i8 = parentId2;
                    try {
                        values.put("format", Integer.valueOf(obj.getFormat()));
                        values.put("_size", Long.valueOf(obj.getSize()));
                        values.put("date_modified", Long.valueOf(obj.getModifiedTime()));
                        Uri uri = this.mMediaProvider.insert(this.mObjectsUri, values);
                        if (uri != null) {
                            Uri uri2 = uri;
                            rescanFile(path.toString(), Integer.parseInt(uri.getPathSegments().get(2)), obj.getFormat());
                        }
                    } catch (RemoteException e2) {
                        e = e2;
                        Log.e(TAG, "RemoteException in mMediaProvider.update", e);
                    }
                }
                this.mMediaProvider.update(this.mObjectsUri, values, PATH_WHERE, whereArgs);
            } catch (RemoteException e3) {
                e = e3;
                Log.e(TAG, "RemoteException in mMediaProvider.update", e);
            }
        }
    }

    private int beginCopyObject(int handle, int newParent, int newStorage) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        MtpStorageManager.MtpObject parent = newParent == 0 ? this.mManager.getStorageRoot(newStorage) : this.mManager.getObject(newParent);
        if (obj == null || parent == null) {
            return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
        }
        return this.mManager.beginCopyObject(obj, parent);
    }

    private void endCopyObject(int handle, boolean success) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null || !this.mManager.endCopyObject(obj, success)) {
            Log.e(TAG, "Failed to end copy object");
        } else if (success) {
            String path = obj.getPath().toString();
            int format = obj.getFormat();
            ContentValues values = new ContentValues();
            values.put("_data", path);
            values.put("format", Integer.valueOf(format));
            values.put("_size", Long.valueOf(obj.getSize()));
            values.put("date_modified", Long.valueOf(obj.getModifiedTime()));
            try {
                if (obj.getParent().isRoot()) {
                    values.put("parent", (Integer) 0);
                } else {
                    int parentId = findInMedia(obj.getParent().getPath());
                    if (parentId != -1) {
                        values.put("parent", Integer.valueOf(parentId));
                    } else {
                        return;
                    }
                }
                if (obj.isDir() != 0) {
                    this.mMediaScanner.scanDirectories(new String[]{path});
                } else {
                    Uri uri = this.mMediaProvider.insert(this.mObjectsUri, values);
                    if (uri != null) {
                        rescanFile(path, Integer.parseInt(uri.getPathSegments().get(2)), format);
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in beginSendObject", e);
            }
        }
    }

    private int setObjectProperty(int handle, int property, long intValue, String stringValue) {
        if (HwFrameworkFactory.getHwMtpDatabaseManager().hwGetSavedObjectHandle() == handle) {
            HwFrameworkFactory.getHwMtpDatabaseManager().hwClearSavedObject();
        }
        if (property != 56327) {
            return MtpConstants.RESPONSE_OBJECT_PROP_NOT_SUPPORTED;
        }
        return renameFile(handle, stringValue);
    }

    private int getDeviceProperty(int property, long[] outIntValue, char[] outStringValue) {
        boolean isDeviceNameUpdate = true;
        if (property == 20481) {
            outIntValue[0] = (long) this.mBatteryLevel;
            outIntValue[1] = (long) this.mBatteryScale;
            return MtpConstants.RESPONSE_OK;
        } else if (property == 20483) {
            Display display = ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int width = display.getMaximumSizeDimension();
            int height = display.getMaximumSizeDimension();
            String imageSize = Integer.toString(width) + "x" + Integer.toString(height);
            imageSize.getChars(0, imageSize.length(), outStringValue, 0);
            outStringValue[imageSize.length()] = 0;
            return MtpConstants.RESPONSE_OK;
        } else if (property != 54279) {
            switch (property) {
                case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER:
                case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME:
                    String value = this.mDeviceProperties.getString(Integer.toString(property), "");
                    if (property == 54274) {
                        if (Settings.Global.getInt(this.mContext.getContentResolver(), "unified_device_name_updated", 0) != 1) {
                            isDeviceNameUpdate = false;
                        }
                        if (isDeviceNameUpdate) {
                            value = Settings.Global.getString(this.mContext.getContentResolver(), "unified_device_name");
                            Settings.Global.putInt(this.mContext.getContentResolver(), "unified_device_name_updated", 0);
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
                    return MtpConstants.RESPONSE_OK;
                default:
                    return MtpConstants.RESPONSE_DEVICE_PROP_NOT_SUPPORTED;
            }
        } else {
            outIntValue[0] = (long) this.mDeviceType;
            return MtpConstants.RESPONSE_OK;
        }
    }

    private int setDeviceProperty(int property, long intValue, String stringValue) {
        int i;
        switch (property) {
            case MtpConstants.DEVICE_PROPERTY_SYNCHRONIZATION_PARTNER:
            case MtpConstants.DEVICE_PROPERTY_DEVICE_FRIENDLY_NAME:
                SharedPreferences.Editor e = this.mDeviceProperties.edit();
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

    private int getObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        String path;
        String pathEx;
        if (handle > sHandleOffsetForHisuite) {
            Cursor c = null;
            try {
                Cursor c2 = this.mMediaProvider.query(this.mObjectsUri, PATH_FORMAT_PROJECTION, ID_WHERE, new String[]{Integer.toString(handle - sHandleOffsetForHisuite)}, null, null);
                if (c2 == null || !c2.moveToNext()) {
                    if (c2 != null) {
                        c2.close();
                    }
                    return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
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
                if (c2 != null) {
                    c2.close();
                }
                return MtpConstants.RESPONSE_OK;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in getObjectFilePath", e);
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
        } else {
            MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
            if (obj == null) {
                return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
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
            return MtpConstants.RESPONSE_OK;
        }
    }

    private int getObjectFormat(int handle) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return -1;
        }
        return obj.getFormat();
    }

    private int beginDeleteObject(int handle) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
        }
        if (!this.mManager.beginRemoveObject(obj)) {
            return 8194;
        }
        return MtpConstants.RESPONSE_OK;
    }

    private void endDeleteObject(int handle, boolean success) {
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj != null) {
            if (!this.mManager.endRemoveObject(obj, success)) {
                Log.e(TAG, "Failed to end remove object");
            }
            if (success) {
                deleteFromMedia(obj.getPath(), obj.isDir());
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0050, code lost:
        if (r1 != null) goto L_0x002a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0053, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0028, code lost:
        if (r1 != null) goto L_0x002a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002a, code lost:
        r1.close();
     */
    private int findInMedia(Path path) {
        int ret = -1;
        Cursor c = null;
        try {
            c = this.mMediaProvider.query(this.mObjectsUri, ID_PROJECTION, PATH_WHERE, new String[]{path.toString()}, null, null);
            if (c != null && c.moveToNext()) {
                ret = c.getInt(0);
            }
        } catch (RemoteException e) {
            if (DEBUG) {
                String str = TAG;
                Log.e(str, "Error finding " + path + " in MediaProvider");
            }
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private void deleteFromMedia(Path path, boolean isDir) {
        if (isDir) {
            try {
                ContentProviderClient contentProviderClient = this.mMediaProvider;
                Uri uri = this.mObjectsUri;
                contentProviderClient.delete(uri, "_data LIKE ?1 AND lower(substr(_data,1,?2))=lower(?3)", new String[]{path + "/%", Integer.toString(path.toString().length() + 1), path.toString() + "/"});
            } catch (Exception e) {
                if (DEBUG) {
                    String str = TAG;
                    Log.d(str, "Failed to delete " + path + " from MediaProvider");
                    return;
                }
                return;
            }
        }
        if (this.mMediaProvider.delete(this.mObjectsUri, PATH_WHERE, new String[]{path.toString()}) > 0) {
            if (!isDir && path.toString().toLowerCase(Locale.US).endsWith(NO_MEDIA)) {
                try {
                    this.mMediaProvider.call("unhide", path.getParent().toString(), null);
                } catch (RemoteException e2) {
                    if (DEBUG) {
                        String str2 = TAG;
                        Log.e(str2, "failed to unhide/rescan for " + path);
                    }
                }
            }
        } else if (DEBUG) {
            String str3 = TAG;
            Log.i(str3, "Mediaprovider didn't delete " + path);
        }
    }

    private int[] getObjectReferences(int handle) {
        if (HwFrameworkFactory.getHwMtpDatabaseManager().hwGetObjectReferences(handle)) {
            return null;
        }
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return null;
        }
        int handle2 = findInMedia(obj.getPath());
        if (handle2 == -1) {
            return null;
        }
        Cursor c = null;
        try {
            c = this.mMediaProvider.query(MediaStore.Files.getMtpReferencesUri(this.mVolumeName, (long) handle2), PATH_PROJECTION, null, null, null, null);
            if (c == null) {
                if (c != null) {
                    c.close();
                }
                return null;
            }
            ArrayList<Integer> result = new ArrayList<>();
            while (c.moveToNext()) {
                MtpStorageManager.MtpObject refObj = this.mManager.getByPath(c.getString(0));
                if (refObj != null) {
                    result.add(Integer.valueOf(refObj.getId()));
                }
            }
            int[] array = result.stream().mapToInt($$Lambda$MtpDatabase$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
            if (c != null) {
                c.close();
            }
            return array;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getObjectList", e);
            if (c != null) {
                c.close();
            }
            return null;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private int setObjectReferences(int handle, int[] references) {
        int[] iArr = references;
        MtpStorageManager.MtpObject obj = this.mManager.getObject(handle);
        if (obj == null) {
            return MtpConstants.RESPONSE_INVALID_OBJECT_HANDLE;
        }
        int handle2 = findInMedia(obj.getPath());
        int i = -1;
        if (handle2 == -1) {
            return 8194;
        }
        Uri uri = MediaStore.Files.getMtpReferencesUri(this.mVolumeName, (long) handle2);
        ArrayList<ContentValues> valuesList = new ArrayList<>();
        int length = iArr.length;
        int i2 = 0;
        while (i2 < length) {
            MtpStorageManager.MtpObject refObj = this.mManager.getObject(iArr[i2]);
            if (refObj != null) {
                int refHandle = findInMedia(refObj.getPath());
                if (refHandle != i) {
                    ContentValues values = new ContentValues();
                    values.put(DownloadManager.COLUMN_ID, Integer.valueOf(refHandle));
                    valuesList.add(values);
                }
            }
            i2++;
            i = -1;
        }
        try {
            if (this.mMediaProvider.bulkInsert(uri, (ContentValues[]) valuesList.toArray(new ContentValues[0])) > 0) {
                return MtpConstants.RESPONSE_OK;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in setObjectReferences", e);
        }
        return 8194;
    }

    private void convertHeifToJgp(String heifPath) {
        try {
            HeifUtils.convertHeifToJpg(heifPath, MTP_TRANSFER_TEMP_FOLDER + heifPath.substring(heifPath.lastIndexOf(47), heifPath.lastIndexOf(46)) + SUFFIX_JPG_FILE);
        } catch (IOException e) {
            Log.e(TAG, "failed to convert heif to jpg");
        }
    }

    private boolean fetchGalleryHeifSetting() {
        sIsHeifSettingAutomaticMode = false;
        return sIsHeifSettingAutomaticMode;
    }

    public static boolean issIsHeifSettingAutomaticMode() {
        return sIsHeifSettingAutomaticMode;
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
        } catch (IOException e) {
            Log.e(TAG, "inStorageRoot ioException!");
        }
        return false;
    }

    private boolean endSendObjectHisuite(int handle, boolean succeeded) {
        if (handle < sHandleOffsetForHisuite) {
            return false;
        }
        this.mMediaScanner.scanMtpFile(this.mSendobjectPath, handle - sHandleOffsetForHisuite, this.mSendObjectFormat);
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006c, code lost:
        if (r1 != null) goto L_0x007b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0079, code lost:
        if (r1 == null) goto L_0x0085;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007b, code lost:
        r1.close();
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
                        int hwBeginSendObject = HwFrameworkFactory.getHwMtpDatabaseManager().hwBeginSendObject(path, c);
                        if (c != null) {
                            c.close();
                        }
                        return hwBeginSendObject;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in beginSendObject", e);
                } catch (Throwable th) {
                    if (c != null) {
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
            values.put("storage_id", Integer.valueOf(storageId));
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
}
