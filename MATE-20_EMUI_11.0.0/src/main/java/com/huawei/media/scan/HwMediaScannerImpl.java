package com.huawei.media.scan;

import android.app.ActivityManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.BuildConfig;
import android.media.HwMediaMonitorImpl;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.storage.ExternalStorageFileImplEx;
import com.huawei.android.os.storage.StorageVolumeEx;
import com.huawei.android.provider.MediaStoreEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.media.scan.HwModernScannerEx;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class HwMediaScannerImpl extends DefaultHwMediaScanner {
    private static final String ACTION_COTA_PARA_LOADED = "com.huawei.settingsprovider.cota_para_loaded";
    private static final String ACTION_MEDIA_SCANNER_SCAN_FOLDER = "huawei.intent.action.MEDIA_SCANNER_SCAN_FOLDER";
    private static final String ACTION_POWER_CONNECTED = "android.intent.action.ACTION_POWER_CONNECTED";
    private static final String ACTION_SYSTEM_MANAGER_UPDATE_FILES = "com.huawei.systemmanager.action.UPDATE_FILES";
    private static final long CONST_TEN = 10;
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final int DEFAULT_THRESHOLD_MAX_BYTES = 524288000;
    private static final int FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX = 2;
    private static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
    private static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
    private static final String[] FILES_PRESCAN_PROJECTION = {"_id", "_data", "date_modified", "_size"};
    private static final int FILES_PRESCAN_SIZE_COLUMN_INDEX = 3;
    private static final int FILE_CACHE_ENTRY_SIZE_PER_ONE_GB = 50000;
    private static final int FLAG_FOR_WRITE = 256;
    private static final Object LOCK_MEDIA_SCAN = new Object();
    private static final boolean LOGD = Log.isLoggable(TAG, 3);
    private static final boolean LOGV = Log.isLoggable(TAG, 2);
    private static final int LOW_MEMORY_DEVICE_GB = 2;
    private static final int MAX_FILE_CACHE_ENTRY_SIZE = 350000;
    private static final long MAX_NOMEDIA_SIZE = 1024;
    private static final int MIN_FILE_CACHE_ENTRY_SIZE = 40000;
    private static final int MSG_INIT_UPDATE_BLACK_LIST = 1;
    private static final int MSG_UPDATE_BLACK_LIST = 3;
    private static final int MSG_UPDATE_EXIF = 2;
    private static final String[] NO_MEDIA_FILE_PATH = {"/.nomedia", "/DCIM/.nomedia", "/DCIM/Camera/.nomedia", "/Pictures/.nomedia", "/Pictures/Screenshots/.nomedia", "/tencent/.nomedia", "/tencent/MicroMsg/.nomedia", "/tencent/MicroMsg/Weixin/.nomedia", "/tencent/QQ_Images/.nomedia"};
    private static final long ONE_GIGABYTE = 1073741824;
    private static final String TAG = "HwMediaScannerImpl";
    private static final long UPDATE_EXIF_INFO_PERIOD = 600000;
    private static Context sContext = null;
    private static volatile HashMap<String, HwModernScannerEx.FileEntry> sFileCache;
    private static HwMediaScannerImpl sInstance = null;
    private static int sMaxFileCacheEntrySize = MIN_FILE_CACHE_ENTRY_SIZE;
    private static ArrayList<Long> sWhiteListIds = new ArrayList<>();
    private AudioFilterUtils mAudioFilterUtils;
    private BlackListUtils mBlackListUtils;
    private Context mContext;
    private long mEndTime;
    private String mExtStoragePath;
    private String mExternalStoragePath;
    private Uri mFilesUri;
    private boolean mIsInit;
    private boolean mIsScanExternalVolumes;
    private long mLastUpdateTime;
    private ContentProviderClient mMediaProvider;
    private MediaServiceProxy mMediaService;
    private volatile ServiceHandler mServiceHandler;
    private volatile Looper mServiceLooper;
    private boolean mSkipExternalQuery;
    private SpecialImageUtils mSpecialImageUtils;
    private long mStartTime;
    private WhiteListUtils mWhiteListUtils;

    private HwMediaScannerImpl() {
    }

    public static HwMediaScannerImpl getDefault() {
        HwMediaScannerImpl hwMediaScannerImpl;
        synchronized (LOCK_MEDIA_SCAN) {
            if (sInstance == null) {
                sInstance = new HwMediaScannerImpl();
            }
            hwMediaScannerImpl = sInstance;
        }
        return hwMediaScannerImpl;
    }

    private void init(Context context) {
        boolean z = false;
        if (this.mContext == null) {
            if (context != null) {
                this.mContext = context.getApplicationContext();
            }
            if (this.mContext == null) {
                this.mContext = ActivityThreadEx.currentApplication().getApplicationContext();
            }
            StringBuilder sb = new StringBuilder();
            sb.append("getApplicationContext ");
            sb.append(this.mContext != null);
            Log.i(TAG, sb.toString());
        }
        Context context2 = this.mContext;
        if (context2 != null && !this.mIsInit) {
            sContext = context2;
            HandlerThread thread = new HandlerThread(TAG);
            thread.start();
            this.mServiceLooper = thread.getLooper();
            if (this.mServiceLooper != null) {
                this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
            } else {
                Log.e(TAG, "mServiceLooper is null!");
            }
            Log.i(TAG, "before acquireContentProviderClient");
            this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient("media");
            this.mBlackListUtils = new BlackListUtils(this.mContext, this);
            BlackListUtils blackListUtils = this.mBlackListUtils;
            if (blackListUtils != null) {
                blackListUtils.init();
            }
            Log.i(TAG, "BlackListUtils init done");
            this.mWhiteListUtils = new WhiteListUtils(this.mContext, this.mMediaService);
            this.mSpecialImageUtils = new SpecialImageUtils(this.mContext);
            this.mAudioFilterUtils = new AudioFilterUtils(this.mContext);
            try {
                this.mExternalStoragePath = Environment.getExternalStorageDirectory().getCanonicalPath();
            } catch (IOException e) {
                Log.e(TAG, "init getCanonicalPath exception");
            }
            this.mFilesUri = MediaStore.Files.getContentUri("external");
            if (HwModernScannerEx.ENABLE_OPTIMIZE_FILE_CACHE) {
                configMaxFileCacheEntrySize();
            }
            this.mIsInit = true;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("init mContext:");
            sb2.append(this.mContext != null);
            sb2.append(", mMediaService:");
            if (this.mMediaService != null) {
                z = true;
            }
            sb2.append(z);
            Log.i(TAG, sb2.toString());
        }
    }

    public void initHwMediaScanner(Context context, MediaServiceProxy proxy) {
        StringBuilder sb = new StringBuilder();
        sb.append("initHwMediaScanner context:");
        boolean z = true;
        sb.append(context != null);
        sb.append(", proxy:");
        if (proxy == null) {
            z = false;
        }
        sb.append(z);
        Log.i(TAG, sb.toString());
        synchronized (LOCK_MEDIA_SCAN) {
            if (context != null) {
                try {
                    if (this.mContext == null) {
                        this.mContext = context;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (proxy == null) {
                Log.i(TAG, "proxy is null, initHwMediaScanner is call in MediaProvider");
                return;
            }
            this.mMediaService = proxy;
            init(context);
        }
    }

    public ModernScannerEx createModernScannerEx(Context context, FileVisitor<Path> visitor, File root, ContentValues contentValues) {
        synchronized (LOCK_MEDIA_SCAN) {
            init(context);
        }
        return new HwModernScannerEx(context, visitor, this, root, contentValues);
    }

    public boolean isEnableMultiThread() {
        return true;
    }

    public boolean onHandleIntent(Context context, Intent intent) {
        if (intent == null) {
            Log.e(TAG, "parameter is invalid");
            return true;
        }
        boolean handled = handledAction(intent);
        if (LOGD) {
            Log.d(TAG, "End " + intent + ", handled: " + handled);
        }
        return handled;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private boolean handledAction(Intent intent) {
        String action = intent.getAction();
        char c = 1;
        if (action == null) {
            Log.e(TAG, "parameter is invalid");
            return true;
        }
        try {
            switch (action.hashCode()) {
                case -1886648615:
                    if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1514214344:
                    if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -378472217:
                    if (action.equals(ACTION_MEDIA_SCANNER_SCAN_FOLDER)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -93375409:
                    if (action.equals(ACTION_SYSTEM_MANAGER_UPDATE_FILES)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 580190462:
                    if (action.equals(ACTION_COTA_PARA_LOADED)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 852070077:
                    if (action.equals("android.intent.action.MEDIA_SCANNER_SCAN_FILE")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1019184907:
                    if (action.equals(ACTION_POWER_CONNECTED)) {
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    if (this.mServiceHandler != null) {
                        this.mServiceHandler.sendEmptyMessage(3);
                    }
                    return true;
                case 1:
                    handlePowerConnected();
                    return true;
                case 2:
                    if (this.mSpecialImageUtils != null) {
                        SpecialImageUtils specialImageUtils = this.mSpecialImageUtils;
                        SpecialImageUtils.setPowerConnectState(false);
                    }
                    return true;
                case 3:
                    onScanForCOTA();
                    return true;
                case 4:
                    handleMediaMounted(intent);
                    return false;
                case 5:
                    handleScanFolder(intent);
                    return true;
                case 6:
                    handleScanSingleFile(intent);
                    return true;
                default:
                    return false;
            }
        } catch (IllegalStateException | SecurityException e) {
            Log.w(TAG, "Failed operation " + intent + ", Exception " + e.getMessage());
            return false;
        }
    }

    private void handleMediaMounted(Intent intent) {
        String volumeName;
        this.mIsScanExternalVolumes = false;
        Uri uri = intent.getData();
        if (uri != null) {
            try {
                String volumeName2 = MediaStoreEx.getVolumeName(new File(uri.getPath()).getCanonicalFile());
                Log.i(TAG, "handleMediaMounted " + volumeName2);
            } catch (IOException e) {
                Log.e(TAG, "handleMediaMounted IOException");
            }
        }
        Bundle arguments = intent.getExtras();
        if (!(arguments == null || (volumeName = arguments.getString("volume")) == null || !volumeName.equals("external"))) {
            this.mIsScanExternalVolumes = true;
        }
        if (this.mIsScanExternalVolumes) {
            Log.i(TAG, "is the clear database case: " + this.mIsScanExternalVolumes);
        }
        Uri uri2 = this.mFilesUri;
        if (uri2 != null) {
            this.mSkipExternalQuery = isSkipExtSdcard(this.mMediaProvider, this.mExtStoragePath, MediaStoreEx.setIncludeTrashed(MediaStoreEx.setIncludePending(uri2.buildUpon().appendQueryParameter("nonotify", "1").build())));
        }
        this.mExtStoragePath = Utils.getExtSdcardVolumePath(this.mContext);
        Log.i(TAG, "extStoragePath: " + this.mExtStoragePath + ", skipExternalQuery: " + this.mSkipExternalQuery);
    }

    public boolean isSkipExtSdcardQuery(Path file) {
        if (this.mExtStoragePath != null && this.mSkipExternalQuery && file.toString().startsWith(this.mExtStoragePath)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004a, code lost:
        if (0 == 0) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004c, code lost:
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0056, code lost:
        if (r10 != null) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0059, code lost:
        if (r1 != 0) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005b, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        return false;
     */
    private boolean isSkipExtSdcard(ContentProviderClient mediaProvider, String extStoragePath, Uri filesUriNoNotify) {
        int externalNum = -1;
        String[] projectionIn = {"COUNT(*)"};
        String selection = "_data LIKE '" + extStoragePath + "%'";
        Cursor cursor = null;
        if (!(extStoragePath == null || mediaProvider == null || filesUriNoNotify == null)) {
            try {
                cursor = mediaProvider.query(filesUriNoNotify, projectionIn, selection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    externalNum = cursor.getInt(0);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "isSkipExtSdcard query error");
            } catch (Throwable th) {
                if (0 != 0) {
                    cursor.close();
                }
                throw th;
            }
        }
    }

    private void handleScanFolder(Intent intent) {
        String path;
        Uri uri = intent.getData();
        Bundle folderBundle = intent.getExtras();
        String folderPath = null;
        if (folderBundle != null) {
            try {
                folderPath = folderBundle.getString("folderpath");
            } catch (BadParcelableException e) {
                Log.e(TAG, "folderPath in BadParcelableException");
            }
        }
        if ((uri != null && ("file".equals(uri.getScheme()) || "content".equals(uri.getScheme()))) || folderPath != null) {
            if (uri != null) {
                path = uri.getPath();
            } else {
                path = folderPath;
            }
            String path2 = Utils.processLegacyPath(this.mContext, path);
            if (Utils.isValidPath(this.mContext, path2)) {
                onScanFolder(path2);
            } else {
                Log.e(TAG, "handleScanFolder path is invalid");
            }
        }
        sWhiteListIds.clear();
    }

    private void handlePowerConnected() {
        Log.i(TAG, "handlePowerConnected");
        long curTime = System.currentTimeMillis();
        SpecialImageUtils.setPowerConnectState(true);
        if (curTime - this.mLastUpdateTime > UPDATE_EXIF_INFO_PERIOD) {
            setLastUpdateTime(curTime);
            if (this.mServiceHandler != null) {
                this.mServiceHandler.sendEmptyMessage(2);
            }
        }
    }

    private void handleScanSingleFile(Intent intent) {
        if (intent != null) {
            ContentValues contentValues = new ContentValues();
            if (intent.hasExtra("datetaken")) {
                contentValues.put("datetaken", Long.valueOf(intent.getLongExtra("datetaken", -1)));
            }
            deleteNomediaFile();
            Uri uri = intent.getData();
            if (uri != null && scanFile(uri.getPath(), contentValues) == null) {
                Log.i(TAG, "try scan file with encoded path");
                scanFile(uri.getEncodedPath(), contentValues);
            }
        }
    }

    private Uri scanFile(String path, ContentValues contentValues) {
        try {
            File file = new File(path).getCanonicalFile();
            if (!file.isFile()) {
                Log.i(TAG, "handleScanSingleFile but scan path is not single file!");
                contentValues.put("skipWhiteList", (Boolean) false);
            }
            if (this.mMediaService != null) {
                Uri res = this.mMediaService.scanFileEx(this.mContext, file, contentValues);
                Log.i(TAG, "handleScanSingleFile res " + res);
                return res;
            }
            Log.e(TAG, "MediaService proxy is null");
            return null;
        } catch (IOException e) {
            Log.e(TAG, "IOException");
            return null;
        }
    }

    public void preScan(String volumeName) {
        Log.i(TAG, "preScan" + volumeName);
        if (this.mContext == null || volumeName == null || this.mWhiteListUtils == null) {
            Log.e(TAG, "context or volumeName is null");
            return;
        }
        Log.i(TAG, "start scanning volume " + volumeName);
        this.mStartTime = System.currentTimeMillis();
        if (!volumeName.equals("internal")) {
            deleteNomediaFile();
            deleteInvalidRecords();
            HwMediaScannerFileVisitor.startScanDirectory();
            if (HwModernScannerEx.ENABLE_OPTIMIZE_FILE_CACHE) {
                try {
                    cacheFileEntry();
                } catch (RemoteException e) {
                    Log.e(TAG, "cacheFileEntry RemoteException");
                }
            }
            if (this.mIsScanExternalVolumes) {
                this.mWhiteListUtils.scanWhiteListDirectories("external", null);
            } else {
                this.mWhiteListUtils.scanWhiteListDirectories(volumeName, null);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0070, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0075, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0076, code lost:
        r0.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0079, code lost:
        throw r1;
     */
    private void deleteInvalidRecords() {
        Log.i(TAG, "deleteInvalidRecords");
        try {
            Cursor cursor = this.mMediaProvider.query(MediaStore.Files.getContentUri("external"), new String[]{"_data"}, "_data like ?", new String[]{"%Pre-loaded%"}, "_id", null);
            while (cursor != null) {
                if (!cursor.moveToNext()) {
                    break;
                }
                String path = cursor.getString(0);
                boolean exists = false;
                try {
                    exists = Os.access(path, OsConstants.F_OK);
                } catch (ErrnoException e) {
                    Log.v(TAG, "access exception");
                }
                if (!exists) {
                    this.mMediaProvider.delete(MediaStore.Files.getContentUri("external"), "_data=?", new String[]{path});
                    Log.i(TAG, "deleteInvalidRecords");
                    if (LOGD) {
                        Log.d(TAG, "deleteAudioFilterFile" + path);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in deleteInvalidRecords");
        }
    }

    private static void resetFileCache() {
        if (sFileCache == null) {
            sFileCache = new HashMap<>();
        } else {
            sFileCache.clear();
        }
    }

    public static HashMap<String, HwModernScannerEx.FileEntry> getFileCache() {
        return sFileCache;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0039 A[Catch:{ all -> 0x00c6 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00cc  */
    private void cacheFileEntry() throws RemoteException {
        Cursor cursor;
        Throwable th;
        Cursor cursor2;
        Log.i(TAG, "cacheFileEntry begin");
        resetFileCache();
        Uri uri = this.mFilesUri;
        if (uri != null) {
            String[] selectionArgs = {BuildConfig.FLAVOR};
            try {
                Uri limitUri = uri.buildUpon().appendQueryParameter("limit", "1000").build();
                long lastId = Long.MIN_VALUE;
                cursor = null;
                int count = 0;
                while (count >= sMaxFileCacheEntrySize) {
                    try {
                        selectionArgs[0] = BuildConfig.FLAVOR + lastId;
                        if (cursor != null) {
                            cursor.close();
                            cursor2 = null;
                        } else {
                            cursor2 = cursor;
                        }
                        if (cursor == null || cursor.getCount() == 0) {
                            break;
                        }
                        while (true) {
                            if (cursor.moveToNext()) {
                                long rowId = cursor.getLong(0);
                                String path = cursor.getString(1);
                                long lastModified = cursor.getLong(2);
                                int size = cursor.getInt(3);
                                lastId = rowId;
                                if (path != null && path.startsWith("/")) {
                                    sFileCache.put(path, new HwModernScannerEx.FileEntry(rowId, lastModified, (long) size));
                                    count++;
                                }
                            }
                        }
                        if (count >= sMaxFileCacheEntrySize) {
                        }
                        break;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                        }
                        throw th;
                    }
                    try {
                        cursor = this.mMediaProvider.query(limitUri, FILES_PRESCAN_PROJECTION, "_id>?", selectionArgs, "_id", null);
                    } catch (Throwable th3) {
                        th = th3;
                        cursor = cursor2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                Log.i(TAG, "cacheFileEntry size " + sFileCache.size());
            } catch (Throwable th4) {
                th = th4;
                cursor = null;
                if (cursor != null) {
                }
                throw th;
            }
        }
    }

    public void postScan(String volumeName) {
        Log.i(TAG, "postScan " + volumeName);
        if (this.mContext == null || volumeName == null) {
            Log.e(TAG, "context or volumeName is null");
            return;
        }
        if (this.mIsScanExternalVolumes && !volumeName.equals("internal")) {
            Log.i(TAG, "postScan mIsScanExternalVolumes is true");
            try {
                if (volumeName.equals("external_primary")) {
                    for (String exactVolume : MediaStoreEx.getExternalVolumeNames(this.mContext)) {
                        Log.i(TAG, "postScan exactVolume " + exactVolume);
                        if (!exactVolume.equals("external_primary")) {
                            if (this.mMediaService != null) {
                                this.mMediaService.scanDirectoryEx(this.mContext, MediaStoreEx.getVolumePath(exactVolume));
                            } else {
                                Log.e(TAG, "MediaService proxy is null");
                            }
                        }
                    }
                } else {
                    try {
                        String volumeExternalPrimary = MediaStoreEx.getVolumePath("external_primary").getCanonicalPath();
                        Log.i(TAG, "volumeExternalPrimary " + volumeExternalPrimary);
                    } catch (IOException e) {
                        Log.e(TAG, "postScan getCanonicalPath exception");
                    }
                    if (this.mMediaService != null) {
                        this.mMediaService.scanDirectoryEx(this.mContext, MediaStoreEx.getVolumePath("external_primary"));
                    } else {
                        Log.e(TAG, "MediaService proxy is null");
                    }
                }
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "FileNotFoundException in postScan");
            }
        }
        updateScanTag(volumeName);
        sWhiteListIds.clear();
        this.mSkipExternalQuery = false;
        pruneDeadThumbnailsFolder();
        this.mEndTime = System.currentTimeMillis();
        HwMediaMonitorImpl.getDefault().writeBigData(Utils.BD_MEDIA_SCANNING, Utils.M_SCANNING_COMPELETED_PERIOD, (int) ((this.mEndTime - this.mStartTime) / 1000), 0);
        Log.i(TAG, "done scanning volume " + volumeName + " in " + (this.mEndTime - this.mStartTime) + " ms, file number: " + HwMediaScannerFileVisitor.getScanDirectoryFilesNum());
    }

    private void updateScanTag(String volumeName) {
        if (volumeName.equals("external_primary")) {
            try {
                Collection<File> dirs = MediaStoreEx.getVolumeScanPaths(volumeName);
                updateScanPreloadTag(dirs);
                updateScanCustomTag(dirs);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "FileNotFoundException in updateScanTag");
            }
        }
    }

    public static boolean isFirstTimeScan() {
        Context context = sContext;
        if (context == null) {
            Log.i(TAG, "init is not called now");
            return true;
        }
        String buildVersion = context.getSharedPreferences("galleryUnit", 0).getString("build_version", BuildConfig.FLAVOR);
        String systemBuildVersion = Utils.getDisplayId();
        Log.i(TAG, "buildVersion:" + buildVersion + ",systemBuildVersion:" + systemBuildVersion);
        return true ^ buildVersion.equalsIgnoreCase(systemBuildVersion);
    }

    public static boolean isFirstTimeScanForEcota() {
        Context context = sContext;
        if (context == null) {
            Log.i(TAG, "init is not called now");
            return true;
        }
        String ecotaVersion = context.getApplicationContext().getSharedPreferences("ecota", 0).getString("ecota_version", BuildConfig.FLAVOR);
        String systemEcotaVersion = SystemPropertiesEx.get("ro.product.EcotaVersion", BuildConfig.FLAVOR);
        Log.i(TAG, "ecotaVersion:" + ecotaVersion + ",systemEcotaVersion:" + systemEcotaVersion);
        return true ^ ecotaVersion.equalsIgnoreCase(systemEcotaVersion);
    }

    private void updateScanPreloadTag(Collection<File> dirs) {
        String preloadMediaDirectory;
        Log.i(TAG, "updateScanPreloadTag");
        if (!(dirs == null || (preloadMediaDirectory = HwMediaStoreImpl.getPreloadMediaDirectory()) == null)) {
            for (File dir : dirs) {
                try {
                    if (preloadMediaDirectory.equalsIgnoreCase(dir.getCanonicalPath())) {
                        String systemBuildVersion = Utils.getDisplayId();
                        SharedPreferences.Editor editor = this.mContext.getApplicationContext().getSharedPreferences("galleryUnit", 0).edit();
                        editor.putString("build_version", systemBuildVersion);
                        editor.commit();
                        Log.d(TAG, "scan Pre-loaded has done, systemBuildVersion:" + systemBuildVersion);
                        return;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IOException is updateScanPreloadTag");
                }
            }
        }
    }

    private void updateScanCustomTag(Collection<File> dirs) {
        String customMediaDirectory;
        Log.i(TAG, "updateScanCustomTag");
        if (HwMediaStoreImpl.isEcotaVersion() && (customMediaDirectory = HwMediaStoreImpl.getCustomMediaDirectory()) != null) {
            for (File dir : dirs) {
                try {
                    if (customMediaDirectory.equalsIgnoreCase(dir.getCanonicalPath())) {
                        String systemEcotaVersion = SystemPropertiesEx.get("ro.product.EcotaVersion", BuildConfig.FLAVOR);
                        SharedPreferences.Editor editor = this.mContext.getApplicationContext().getSharedPreferences("ecota", 0).edit();
                        editor.putString("ecota_version", systemEcotaVersion);
                        editor.commit();
                        Log.i(TAG, "scan custom has done, systemEcotaVersion:" + systemEcotaVersion);
                        return;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IOException is updateScanCustomTag");
                }
            }
        }
    }

    private void onScanFolder(String path) {
        Throwable th;
        Log.i(TAG, "start scanning folder");
        if (path == null) {
            Log.e(TAG, "onScanFolder path is null");
            return;
        }
        long startTime = System.currentTimeMillis();
        deleteNomediaFile();
        HwMediaScannerFileVisitor.startScanDirectory();
        if (HwModernScannerEx.ENABLE_OPTIMIZE_FILE_CACHE) {
            try {
                cacheFileEntry();
            } catch (RemoteException e) {
                Log.e(TAG, "cacheFileEntry RemoteException");
            }
        }
        ContentResolver resolver = this.mContext.getContentResolver();
        try {
            File file = new File(path).getCanonicalFile();
            String volumeName = MediaStoreEx.getVolumeName(file);
            Uri uri = Uri.parse("file://" + path);
            ContentValues values = new ContentValues();
            values.put("volume", volumeName);
            Uri scanUri = resolver.insert(MediaStore.getMediaScannerUri(), values);
            try {
                if (!"internal".equals(volumeName)) {
                    try {
                        this.mContext.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_STARTED", uri));
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
                this.mWhiteListUtils.scanWhiteListDirectories(volumeName, file);
                if (this.mMediaService != null) {
                    this.mMediaService.scanDirectoryEx(this.mContext, file);
                } else {
                    Log.e(TAG, "MediaService proxy is null");
                }
                resolver.delete(scanUri, null, null);
                if (!"internal".equals(volumeName)) {
                    this.mContext.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_FINISHED", uri));
                }
                long end = System.currentTimeMillis();
                HwMediaMonitorImpl.getDefault().writeBigData(Utils.BD_MEDIA_SCANNING, Utils.M_SCANNING_COMPELETED_PERIOD, (int) ((end - startTime) / 1000), 0);
                Log.i(TAG, "done scanning folder  in " + (end - startTime) + " ms, file number: " + HwMediaScannerFileVisitor.getScanDirectoryFilesNum());
            } catch (Throwable th3) {
                th = th3;
                if (!"internal".equals(volumeName)) {
                    this.mContext.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_FINISHED", uri));
                }
                throw th;
            }
        } catch (IOException e2) {
            Log.e(TAG, "onScanFolder IOException");
        }
    }

    private void onScanForCOTA() {
        MediaServiceProxy mediaServiceProxy;
        Log.i(TAG, "onScanForCOTA");
        ContentResolver resolver = this.mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("volume", "internal");
        Uri scanUri = resolver.insert(MediaStore.getMediaScannerUri(), values);
        Iterator<String> it = HwMediaStoreImpl.getCfgPolicyMediaDirs().iterator();
        while (it.hasNext()) {
            File file = new File(it.next());
            if (file.exists() && (mediaServiceProxy = this.mMediaService) != null) {
                mediaServiceProxy.scanDirectoryEx(this.mContext, file);
            }
        }
        resolver.delete(scanUri, null, null);
    }

    private void setLastUpdateTime(long time) {
        this.mLastUpdateTime = time;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00cb, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00d1, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00d2, code lost:
        r0.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00d6, code lost:
        throw r0;
     */
    public void ensureDefaultRingtonesEx(Context context) {
        Log.i(TAG, "ensureDefaultRingtonesEx");
        if (!isMultiSimEnabled()) {
            return;
        }
        if (context != null) {
            String setting = settingSetIndicatorName(SettingsEx.System.RINGTONE2);
            if (Settings.System.getInt(context.getContentResolver(), setting, 0) == 0) {
                String fileName = SystemPropertiesEx.get(DEFAULT_RINGTONE_PROPERTY_PREFIX + SettingsEx.System.RINGTONE2);
                int i = 1;
                Uri[] baseUris = {MediaStore.Audio.Media.INTERNAL_CONTENT_URI, MediaStore.Video.Media.INTERNAL_CONTENT_URI};
                int length = baseUris.length;
                int i2 = 0;
                while (i2 < length) {
                    Uri baseUri = baseUris[i2];
                    Log.i(TAG, "baseUri " + baseUri);
                    String[] strArr = new String[i];
                    strArr[0] = fileName;
                    Cursor cursor = context.getContentResolver().query(baseUri, new String[]{"_id"}, "_display_name=?", strArr, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            Uri uri = ContentUris.withAppendedId(baseUri, cursor.getLong(0));
                            Uri ringtoneUri = context.getContentResolver().canonicalize(uri);
                            if (ringtoneUri == null) {
                                ringtoneUri = uri;
                            }
                            int type = RingtoneManager.getDefaultType(Settings.System.getUriFor(SettingsEx.System.RINGTONE2));
                            Log.i(TAG, "ensureDefaultRingtones2 type " + type);
                            RingtoneManager.setActualDefaultRingtoneUri(context, type, ringtoneUri);
                            Settings.System.putInt(context.getContentResolver(), setting, 1);
                            cursor.close();
                            return;
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    i2++;
                    i = 1;
                }
            }
        }
    }

    private static boolean isMultiSimEnabled() {
        return TelephonyManagerEx.isMultiSimEnabled();
    }

    private String settingSetIndicatorName(String base) {
        return base + "_set";
    }

    private void pruneDeadThumbnailsFolder() {
        boolean isDelete = false;
        if (this.mExternalStoragePath == null) {
            try {
                this.mExternalStoragePath = Environment.getExternalStorageDirectory().getCanonicalPath();
            } catch (IOException e) {
                Log.e(TAG, "pruneDeadThumbnailsFolder getCanonicalPath exception");
            }
        }
        try {
            Log.i(TAG, "pruneDeadThumbnailsFolder mExternalStoragePath " + this.mExternalStoragePath);
            StatFs sdcardFileStats = new StatFs(this.mExternalStoragePath);
            long freeMem = ((long) sdcardFileStats.getAvailableBlocks()) * ((long) sdcardFileStats.getBlockSize());
            long totalMem = (((long) sdcardFileStats.getBlockCount()) * ((long) sdcardFileStats.getBlockSize())) / CONST_TEN;
            long thresholdMem = 524288000;
            if (totalMem <= 524288000) {
                thresholdMem = totalMem;
            }
            isDelete = freeMem <= thresholdMem;
            Log.i(TAG, "freeMem[" + freeMem + "] 10%totalMem[" + totalMem + "] under " + this.mExternalStoragePath);
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "IllegalArgumentException in pruneDeadThumbnailsFolder error");
        }
        if (isDelete) {
            File thumbFolder = new File(this.mExternalStoragePath + "/DCIM/.thumbnails");
            if (!thumbFolder.exists()) {
                Log.e(TAG, ".thumbnails folder not exists. ");
                return;
            }
            File[] files = thumbFolder.listFiles();
            if (files != null) {
                Log.v(TAG, "delete .thumbnails");
                for (File file : files) {
                    if (!file.delete()) {
                        Log.e(TAG, "Failed to delete file!");
                    }
                }
            }
        }
    }

    private void deleteNomediaFile() {
        deleteNomediaFile(MediaStoreEx.getVolumeList(UserHandleEx.myUserId(), 256));
        delNomediaForAppClonedUser();
    }

    private void delNomediaForAppClonedUser() {
        StorageVolume[] storageVolumes;
        if (MediaStoreEx.IS_SUPPORT_CLONE_APP && (storageVolumes = MediaStoreEx.getAppClonedUserVolumes(this.mContext)) != null && storageVolumes.length > 0) {
            Log.i(TAG, "delete nomedia file for cloned user");
            deleteNomediaFile(storageVolumes);
        }
    }

    private void deleteNomediaFile(StorageVolume[] volumes) {
        for (StorageVolume storageVolume : volumes) {
            String rootPath = StorageVolumeEx.getPath(storageVolume);
            for (String nomedia : NO_MEDIA_FILE_PATH) {
                ExternalStorageFileImplEx nomediaFile = new ExternalStorageFileImplEx(rootPath + nomedia);
                if (nomediaFile.exists()) {
                    if (nomediaFile.isFile() && nomediaFile.length() > MAX_NOMEDIA_SIZE) {
                        Log.i(TAG, "skip to nomedia file");
                    } else if (nomediaFile.delete()) {
                        Log.i(TAG, "Success delete the .nomediaPath");
                    } else {
                        Log.i(TAG, "fail delete the .nomediaPath");
                    }
                }
            }
        }
    }

    public static void setScannedId(long scannedId) {
        ArrayList<Long> arrayList = sWhiteListIds;
        if (arrayList != null && !arrayList.contains(Long.valueOf(scannedId))) {
            sWhiteListIds.add(Long.valueOf(scannedId));
        }
    }

    public static ArrayList<Long> getWhiteListIds() {
        return sWhiteListIds;
    }

    public WhiteListUtils getWhiteListUtils() {
        return this.mWhiteListUtils;
    }

    public BlackListUtils getBlackListUtils() {
        return this.mBlackListUtils;
    }

    public SpecialImageUtils getSpecialImageUtils() {
        return this.mSpecialImageUtils;
    }

    public AudioFilterUtils getAudioFilterUtils() {
        return this.mAudioFilterUtils;
    }

    public void initAndUpdateBlackList() {
        Log.i(TAG, "init and update the blackList");
        if (this.mServiceHandler != null) {
            this.mServiceHandler.sendEmptyMessage(1);
        }
    }

    private void configMaxFileCacheEntrySize() {
        ActivityManager activityManager = null;
        Context context = this.mContext;
        if (context != null && (context.getSystemService("activity") instanceof ActivityManager)) {
            activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        }
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        boolean lowRamDevice = false;
        if (activityManager != null) {
            activityManager.getMemoryInfo(memoryInfo);
            lowRamDevice = activityManager.isLowRamDevice();
        }
        int totalMemoryGb = (int) (memoryInfo.totalMem / ONE_GIGABYTE);
        if (totalMemoryGb <= 2 || lowRamDevice) {
            sMaxFileCacheEntrySize = MIN_FILE_CACHE_ENTRY_SIZE;
        } else {
            sMaxFileCacheEntrySize = ((totalMemoryGb - 2) * FILE_CACHE_ENTRY_SIZE_PER_ONE_GB) + MIN_FILE_CACHE_ENTRY_SIZE;
        }
        if (sMaxFileCacheEntrySize > MAX_FILE_CACHE_ENTRY_SIZE) {
            sMaxFileCacheEntrySize = MAX_FILE_CACHE_ENTRY_SIZE;
        }
        if (LOGV) {
            Log.v(TAG, "totalMemoryGb " + totalMemoryGb + ",lowRamDevice " + lowRamDevice + ",sMaxFileCacheEntrySize = " + sMaxFileCacheEntrySize);
        }
    }

    /* access modifiers changed from: private */
    public final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg != null) {
                Log.i(HwMediaScannerImpl.TAG, "begin handleMessage " + msg.what);
                int i = msg.what;
                if (i != 1) {
                    if (i != 2) {
                        if (i == 3 && HwMediaScannerImpl.this.mBlackListUtils != null) {
                            Log.i(HwMediaScannerImpl.TAG, "mBlackListUtils");
                            HwMediaScannerImpl.this.mBlackListUtils.updateBlackList();
                        }
                    } else if (HwMediaScannerImpl.this.mSpecialImageUtils != null) {
                        Log.i(HwMediaScannerImpl.TAG, "updateExifFile");
                        HwMediaScannerImpl.this.mSpecialImageUtils.updateExifFile();
                    }
                } else if (HwMediaScannerImpl.this.mBlackListUtils != null) {
                    Log.i(HwMediaScannerImpl.TAG, "updateBlackListInMediaProvider");
                    HwMediaScannerImpl.this.mBlackListUtils.updateBlackListInMediaProvider();
                }
                Log.i(HwMediaScannerImpl.TAG, "finish handleMessage " + msg.what);
            }
        }
    }
}
