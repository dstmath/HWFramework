package android.media;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.common.HwFrameworkFactory;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.SQLException;
import android.drm.DrmManagerClient;
import android.graphics.BitmapFactory;
import android.hwtheme.HwThemeManager;
import android.media.scan.IsoInterface;
import android.media.scan.XmpInterface;
import android.mtp.MtpConstants;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiScanLog;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.Settings;
import android.sax.ElementListener;
import android.sax.RootElement;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.telecom.Logging.Session;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.util.Xml;
import dalvik.system.CloseGuard;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

@Deprecated
public class MediaScanner implements AutoCloseable {
    private static final String ALARMS_DIR = "/alarms/";
    private static final String AUDIOBOOKS_DIR = "/audiobooks/";
    private static final int DATE_MODIFIED_PLAYLISTS_COLUMN_INDEX = 2;
    private static final long DAY_IN_HOURS = 24;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final boolean ENABLE_BULK_INSERTS = true;
    private static final boolean ENABLE_PROFILE_SCANNER = SystemProperties.getBoolean("persist.sys.profile_scanner", false);
    private static final int FILES_BLACKLIST_ID_COLUMN_INDEX = 0;
    private static final int FILES_BLACKLIST_MEDIA_TYPE_COLUMN_INDEX = 2;
    private static final int FILES_BLACKLIST_PATH_COLUMN_INDEX = 1;
    private static final String[] FILES_BLACKLIST_PROJECTION_MEDIA = {"_id", "_data", "media_type"};
    private static final int FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX = 3;
    private static final int FILES_PRESCAN_FORMAT_COLUMN_INDEX = 2;
    private static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
    private static final int FILES_PRESCAN_MEDIA_TYPE_COLUMN_INDEX = 4;
    private static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
    @UnsupportedAppUsage
    private static final String[] FILES_PRESCAN_PROJECTION = {"_id", "_data", "format", "date_modified", "media_type"};
    private static final String[] FILES_PRESCAN_PROJECTION_MEDIA = {"_id", "_data"};
    private static final String HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN = "special_file_type";
    private static final String[] ID3_GENRES = {"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie", "Britpop", null, "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian", "Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "Synthpop"};
    private static final int ID_PLAYLISTS_COLUMN_INDEX = 0;
    private static final String[] ID_PROJECTION = {"_id"};
    private static final String IMAGE_TYPE_BEAUTY_FRONT = "fbt";
    private static final String IMAGE_TYPE_BEAUTY_REAR = "rbt";
    private static final String IMAGE_TYPE_HDR = "hdr";
    private static final String IMAGE_TYPE_JHDR = "jhdr";
    private static final String IMAGE_TYPE_PORTRAIT_FRONT = "fpt";
    private static final String IMAGE_TYPE_PORTRAIT_REAR = "rpt";
    private static final int IMAGE_TYPE_VALUE_BEAUTY_FRONT = 40;
    private static final int IMAGE_TYPE_VALUE_BEAUTY_REAR = 41;
    private static final int IMAGE_TYPE_VALUE_DEFAULT = 0;
    private static final int IMAGE_TYPE_VALUE_HDR = 1;
    private static final int IMAGE_TYPE_VALUE_JHDR = 2;
    private static final int IMAGE_TYPE_VALUE_PORTRAIT_FRONT = 30;
    private static final int IMAGE_TYPE_VALUE_PORTRAIT_REAR = 31;
    public static final String LAST_INTERNAL_SCAN_FINGERPRINT = "lastScanFingerprint";
    private static final long MINIMUM_GRANULARITY_IN_MILLIS = 15;
    private static final String MUSIC_DIR = "/music/";
    private static final String NOTIFICATIONS_DIR = "/notifications/";
    private static final String OEM_SOUNDS_DIR = (Environment.getOemDirectory() + "/media/audio");
    private static final int PATH_PLAYLISTS_COLUMN_INDEX = 1;
    private static final String[] PLAYLIST_MEMBERS_PROJECTION = {MediaStore.Audio.Playlists.Members.PLAYLIST_ID};
    private static final String PODCASTS_DIR = "/podcasts/";
    private static final int PRINT_ONCE_PER_FILES_NUMBER = 1000;
    private static final String PRODUCT_SOUNDS_DIR = (Environment.getProductDirectory() + "/media/audio");
    private static final String RINGTONES_DIR = "/ringtones/";
    public static final String SCANNED_BUILD_PREFS_NAME = "MediaScanBuild";
    private static final int SQL_MEDIA_TYPE_BLACKLIST = 10;
    private static final int SQL_MEDIA_TYPE_IMGAGE = 1;
    private static final int SQL_QUERY_COUNT = 100;
    private static final String SQL_QUERY_LIMIT = "1000";
    private static final String SQL_VALUE_EXIF_FLAG = "1";
    private static final String SYSTEM_SOUNDS_DIR = (Environment.getRootDirectory() + "/media/audio");
    private static final String TAG = "MediaScanner";
    private static final int TIME_LIMIT_MAY_ERROR = 15000;
    private static HashMap<String, String> mMediaPaths = new HashMap<>();
    private static HashMap<String, String> mNoMediaPaths = new HashMap<>();
    public static final Set sBlackList = new HashSet();
    public static String sCurScanDIR = "";
    private static String sLastInternalScanFingerprint;
    public static boolean sPowerConnect = true;
    private long mAudioFileNumber;
    @UnsupportedAppUsage
    private final Uri mAudioUri;
    private final BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();
    private boolean mBlackListFlag = false;
    @UnsupportedAppUsage
    private final MyMediaScannerClient mClient = new MyMediaScannerClient();
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    @UnsupportedAppUsage
    private final Context mContext;
    private long mCurrentTimeMillis;
    @UnsupportedAppUsage
    private String mDefaultAlarmAlertFilename;
    private boolean mDefaultAlarmSet;
    @UnsupportedAppUsage
    private String mDefaultNotificationFilename;
    private boolean mDefaultNotificationSet;
    @UnsupportedAppUsage
    private String mDefaultRingtoneFilename;
    private boolean mDefaultRingtoneSet;
    private DrmManagerClient mDrmManagerClient = null;
    private String mExtStroagePath;
    private HashMap<String, FileEntry> mFileCache;
    private final Uri mFilesFullUri;
    @UnsupportedAppUsage
    private final Uri mFilesUri;
    private long mImageFileNumber;
    private final Uri mImagesUri;
    private boolean mIsImageType = false;
    private boolean mIsPrescanFiles = true;
    private long mMakeEntryFor;
    private int mMaxFileCacheEntrySize = 40000;
    @UnsupportedAppUsage
    private MediaInserter mMediaInserter;
    private final ContentProviderClient mMediaProvider;
    private boolean mMediaTypeConflict;
    private int mMtpObjectHandle;
    private long mNativeContext;
    private boolean mNeedFilter;
    private int mOriginalCount;
    @UnsupportedAppUsage
    private final String mPackageName;
    private final ArrayList<FileEntry> mPlayLists = new ArrayList<>();
    private final ArrayList<PlaylistEntry> mPlaylistEntries = new ArrayList<>();
    private final Uri mPlaylistsUri;
    private long mProcessAVFileTime;
    private final boolean mProcessGenres;
    private long mProcessImageFileTime;
    private final boolean mProcessPlaylists;
    private long mScanDirectoryFilesNum = 0;
    private long mScanFileTime;
    private boolean mSkipExternelQuery;
    private int mStorageIdForCurScanDIR = 0;
    private long mVideoFileNumber;
    private final Uri mVideoUri;
    private final String mVolumeName;

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup();

    private native void processDirectory(String str, MediaScannerClient mediaScannerClient);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native boolean processFile(String str, String str2, MediaScannerClient mediaScannerClient);

    private static native void releaseBlackList();

    private static native void setBlackList(String str);

    @UnsupportedAppUsage
    private native void setLocale(String str);

    public static native void setStorageEjectFlag(boolean z);

    public native void addSkipCustomDirectory(String str, int i);

    public native void clearSkipCustomDirectory();

    public native byte[] extractAlbumArt(FileDescriptor fileDescriptor);

    public native void setExteLen(int i);

    static /* synthetic */ long access$1014(MediaScanner x0, long x1) {
        long j = x0.mScanFileTime + x1;
        x0.mScanFileTime = j;
        return j;
    }

    static /* synthetic */ long access$1104(MediaScanner x0) {
        long j = x0.mScanDirectoryFilesNum + 1;
        x0.mScanDirectoryFilesNum = j;
        return j;
    }

    static /* synthetic */ long access$2614(MediaScanner x0, long x1) {
        long j = x0.mProcessAVFileTime + x1;
        x0.mProcessAVFileTime = j;
        return j;
    }

    static /* synthetic */ long access$2708(MediaScanner x0) {
        long j = x0.mAudioFileNumber;
        x0.mAudioFileNumber = 1 + j;
        return j;
    }

    static /* synthetic */ long access$2808(MediaScanner x0) {
        long j = x0.mVideoFileNumber;
        x0.mVideoFileNumber = 1 + j;
        return j;
    }

    static /* synthetic */ long access$2914(MediaScanner x0, long x1) {
        long j = x0.mProcessImageFileTime + x1;
        x0.mProcessImageFileTime = j;
        return j;
    }

    static /* synthetic */ long access$3008(MediaScanner x0) {
        long j = x0.mImageFileNumber;
        x0.mImageFileNumber = 1 + j;
        return j;
    }

    static /* synthetic */ long access$614(MediaScanner x0, long x1) {
        long j = x0.mMakeEntryFor + x1;
        x0.mMakeEntryFor = j;
        return j;
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    /* access modifiers changed from: private */
    public static class FileEntry {
        int mFormat;
        long mLastModified;
        @UnsupportedAppUsage
        boolean mLastModifiedChanged;
        int mMediaType;
        String mPath;
        @UnsupportedAppUsage
        long mRowId;

        @UnsupportedAppUsage
        @Deprecated
        FileEntry(long rowId, String path, long lastModified, int format) {
            this(rowId, path, lastModified, format, 0);
        }

        FileEntry(long rowId, String path, long lastModified, int format, int mediaType) {
            this.mRowId = rowId;
            this.mPath = path;
            this.mLastModified = lastModified;
            this.mFormat = format;
            this.mMediaType = mediaType;
            this.mLastModifiedChanged = false;
        }

        public String toString() {
            return this.mPath + " mRowId: " + this.mRowId;
        }
    }

    /* access modifiers changed from: private */
    public static class PlaylistEntry {
        long bestmatchid;
        int bestmatchlevel;
        String path;

        private PlaylistEntry() {
        }
    }

    @UnsupportedAppUsage
    public MediaScanner(Context c, String volumeName) {
        native_setup();
        this.mContext = c;
        this.mPackageName = c.getPackageName();
        this.mVolumeName = volumeName;
        BitmapFactory.Options options = this.mBitmapOptions;
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        setDefaultRingtoneFileNames();
        this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        if (sLastInternalScanFingerprint == null) {
            sLastInternalScanFingerprint = this.mContext.getSharedPreferences(SCANNED_BUILD_PREFS_NAME, 0).getString(LAST_INTERNAL_SCAN_FINGERPRINT, new String());
        }
        this.mAudioUri = MediaStore.Audio.Media.getContentUri(volumeName);
        this.mVideoUri = MediaStore.Video.Media.getContentUri(volumeName);
        this.mImagesUri = MediaStore.Images.Media.getContentUri(volumeName);
        this.mFilesUri = MediaStore.Files.getContentUri(volumeName);
        this.mFilesFullUri = MediaStore.setIncludeTrashed(MediaStore.setIncludePending(this.mFilesUri.buildUpon().appendQueryParameter("nonotify", "1").build()));
        if (!volumeName.equals(MediaStore.VOLUME_INTERNAL)) {
            this.mProcessPlaylists = true;
            this.mProcessGenres = true;
            this.mPlaylistsUri = MediaStore.Audio.Playlists.getContentUri(volumeName);
            this.mExtStroagePath = HwFrameworkFactory.getHwMediaScannerManager().getExtSdcardVolumePath(this.mContext);
            this.mSkipExternelQuery = HwFrameworkFactory.getHwMediaScannerManager().isSkipExtSdcard(this.mMediaProvider, this.mExtStroagePath, this.mPackageName, this.mFilesFullUri);
            this.mNeedFilter = false;
        } else {
            this.mProcessPlaylists = false;
            this.mProcessGenres = false;
            this.mPlaylistsUri = null;
            this.mNeedFilter = HwFrameworkFactory.getHwMediaScannerManager().loadAudioFilterConfig(this.mContext);
        }
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (locale != null) {
            String language = locale.getLanguage();
            String country = locale.getCountry();
            if (language != null) {
                if (country != null) {
                    setLocale(language + Session.SESSION_SEPARATION_CHAR_CHILD + country);
                } else {
                    setLocale(language);
                }
            }
        }
        setStorageEjectFlag(false);
        configMaxFileCacheEntrySize();
        this.mCloseGuard.open("close");
    }

    private void configMaxFileCacheEntrySize() {
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        int totalMemoryGb = (int) (memoryInfo.totalMem / TrafficStats.GB_IN_BYTES);
        boolean lowRamDevice = activityManager.isLowRamDevice();
        if (totalMemoryGb <= 2 || lowRamDevice) {
            this.mMaxFileCacheEntrySize = 40000;
        } else {
            this.mMaxFileCacheEntrySize = ((totalMemoryGb - 2) * 50000) + 40000;
        }
        if (this.mMaxFileCacheEntrySize > 350000) {
            this.mMaxFileCacheEntrySize = 350000;
        }
        Log.d(TAG, "totalMemoryGb " + totalMemoryGb + ", lowRamDevice " + lowRamDevice + ", mMaxFileCacheEntrySize = " + this.mMaxFileCacheEntrySize);
    }

    public void setIsPrescanFiles(boolean prescanFiles) {
        this.mIsPrescanFiles = prescanFiles;
    }

    public boolean getIsPrescanFiles() {
        return this.mIsPrescanFiles;
    }

    public static void updateBlackList(Set blackLists) {
        Iterator it = blackLists.iterator();
        sBlackList.clear();
        releaseBlackList();
        while (it.hasNext()) {
            String black = (String) it.next();
            if (!(black == null || black.length() == 0)) {
                setBlackList(black);
                sBlackList.add(black);
            }
        }
    }

    public boolean startWithIgnoreCase(String src, String sub) {
        if (sub.length() > src.length()) {
            return false;
        }
        return src.substring(0, sub.length()).equalsIgnoreCase(sub);
    }

    private boolean isBlackListPath(String path, int exterlen) {
        if (path == null || sBlackList.size() == 0 || exterlen == 0 || path.length() <= exterlen) {
            return false;
        }
        String subPath = path.substring(exterlen);
        for (String itp : sBlackList) {
            if (startWithIgnoreCase(subPath, itp)) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:114:0x023d, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x0244, code lost:
        r2 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x024a, code lost:
        r2 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:121:0x0250, code lost:
        r2 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x025c, code lost:
        if (r4 == null) goto L_0x0275;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x025e, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x0268, code lost:
        if (r4 == null) goto L_0x0275;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:130:0x0272, code lost:
        if (r4 == null) goto L_0x0275;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0092, code lost:
        r5 = r17;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x023d A[ExcHandler: all (th java.lang.Throwable), Splitter:B:97:0x0204] */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x0297  */
    public void updateBlackListFile() {
        String str;
        long lastId;
        Throwable th;
        String str2;
        int count;
        Cursor cursor;
        ContentProviderClient contentProviderClient;
        String[] strArr;
        int i;
        Cursor cursor2;
        Cursor cursor3 = null;
        String[] selectionArgs = {WifiEnterpriseConfig.ENGINE_DISABLE};
        int count2 = 0;
        Uri limitUri = this.mFilesUri.buildUpon().appendQueryParameter("limit", SQL_QUERY_LIMIT).build();
        long start = System.currentTimeMillis();
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        long lastId2 = Long.MIN_VALUE;
        int fileCount = 0;
        while (true) {
            if (count2 >= 100) {
                str = TAG;
                lastId = lastId2;
                break;
            }
            try {
                selectionArgs[0] = "" + lastId2;
                count = count2 + 1;
                if (cursor3 != null) {
                    try {
                        cursor3.close();
                        cursor = null;
                    } catch (SQLException e) {
                        str2 = TAG;
                        Log.e(str2, "updateBlackListFile SQLException ! ");
                    } catch (OperationApplicationException e2) {
                        str2 = TAG;
                        Log.e(str2, "MediaProvider upate all file Exception when use the applyBatch ! ");
                    } catch (RemoteException e3) {
                        str2 = TAG;
                        count2 = count;
                        try {
                            Log.e(str2, "updateBlackListFile RemoteException ! ");
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (cursor3 != null) {
                        }
                        throw th;
                    }
                } else {
                    cursor = cursor3;
                }
                try {
                    contentProviderClient = this.mMediaProvider;
                    strArr = FILES_BLACKLIST_PROJECTION_MEDIA;
                    i = 0;
                    str = TAG;
                    lastId = lastId2;
                } catch (SQLException e4) {
                    str2 = TAG;
                    cursor3 = cursor;
                    Log.e(str2, "updateBlackListFile SQLException ! ");
                } catch (OperationApplicationException e5) {
                    str2 = TAG;
                    cursor3 = cursor;
                    Log.e(str2, "MediaProvider upate all file Exception when use the applyBatch ! ");
                } catch (RemoteException e6) {
                    str2 = TAG;
                    count2 = count;
                    cursor3 = cursor;
                    Log.e(str2, "updateBlackListFile RemoteException ! ");
                } catch (Throwable th4) {
                    th = th4;
                    cursor3 = cursor;
                    if (cursor3 != null) {
                    }
                    throw th;
                }
            } catch (SQLException e7) {
                str2 = TAG;
                Log.e(str2, "updateBlackListFile SQLException ! ");
            } catch (OperationApplicationException e8) {
                str2 = TAG;
                Log.e(str2, "MediaProvider upate all file Exception when use the applyBatch ! ");
            } catch (RemoteException e9) {
                str2 = TAG;
                Log.e(str2, "updateBlackListFile RemoteException ! ");
            } catch (Throwable th5) {
                th = th5;
                if (cursor3 != null) {
                }
                throw th;
            }
            try {
                cursor3 = contentProviderClient.query(limitUri, strArr, "_id>? and (media_type=10 or media_type=1)", selectionArgs, "_id", null);
                if (cursor3 == null) {
                    break;
                }
                try {
                    int num = cursor3.getCount();
                    if (num == 0) {
                        break;
                    }
                    lastId2 = lastId;
                    while (cursor3.moveToNext()) {
                        try {
                            long rowId = cursor3.getLong(i);
                            String path = cursor3.getString(1);
                            int mediatype = cursor3.getInt(2);
                            lastId2 = rowId;
                            fileCount++;
                            ContentValues values = new ContentValues();
                            boolean isBlackListFlag = isBlackListPath(path, getRootDirLength(path));
                            if (!isBlackListFlag || mediatype != 1) {
                                cursor2 = cursor3;
                                if (!isBlackListFlag && mediatype == 10) {
                                    values.put("media_type", (Integer) 1);
                                    ops.add(ContentProviderOperation.newUpdate(ContentUris.withAppendedId(this.mImagesUri, rowId)).withValues(values).build());
                                }
                            } else {
                                cursor2 = cursor3;
                                try {
                                    values.put("media_type", (Integer) 10);
                                    ops.add(ContentProviderOperation.newUpdate(ContentUris.withAppendedId(this.mImagesUri, rowId)).withValues(values).build());
                                } catch (SQLException e10) {
                                    str2 = str;
                                    cursor3 = cursor2;
                                    Log.e(str2, "updateBlackListFile SQLException ! ");
                                } catch (OperationApplicationException e11) {
                                    str2 = str;
                                    cursor3 = cursor2;
                                    Log.e(str2, "MediaProvider upate all file Exception when use the applyBatch ! ");
                                } catch (RemoteException e12) {
                                    count2 = count;
                                    str2 = str;
                                    cursor3 = cursor2;
                                    Log.e(str2, "updateBlackListFile RemoteException ! ");
                                } catch (Throwable th6) {
                                    th = th6;
                                    cursor3 = cursor2;
                                    if (cursor3 != null) {
                                    }
                                    throw th;
                                }
                            }
                            num = num;
                            cursor3 = cursor2;
                            i = 0;
                        } catch (SQLException e13) {
                            str2 = str;
                            Log.e(str2, "updateBlackListFile SQLException ! ");
                        } catch (OperationApplicationException e14) {
                            str2 = str;
                            Log.e(str2, "MediaProvider upate all file Exception when use the applyBatch ! ");
                        } catch (RemoteException e15) {
                            count2 = count;
                            str2 = str;
                            Log.e(str2, "updateBlackListFile RemoteException ! ");
                        } catch (Throwable th7) {
                            th = th7;
                            if (cursor3 != null) {
                                cursor3.close();
                            }
                            throw th;
                        }
                    }
                    count2 = count;
                } catch (SQLException e16) {
                    str2 = str;
                    Log.e(str2, "updateBlackListFile SQLException ! ");
                } catch (OperationApplicationException e17) {
                    str2 = str;
                    Log.e(str2, "MediaProvider upate all file Exception when use the applyBatch ! ");
                } catch (RemoteException e18) {
                    count2 = count;
                    str2 = str;
                    Log.e(str2, "updateBlackListFile RemoteException ! ");
                } catch (Throwable th8) {
                    th = th8;
                    if (cursor3 != null) {
                    }
                    throw th;
                }
            } catch (SQLException e19) {
                cursor3 = cursor;
                str2 = str;
                Log.e(str2, "updateBlackListFile SQLException ! ");
            } catch (OperationApplicationException e20) {
                cursor3 = cursor;
                str2 = str;
                Log.e(str2, "MediaProvider upate all file Exception when use the applyBatch ! ");
            } catch (RemoteException e21) {
                count2 = count;
                cursor3 = cursor;
                str2 = str;
                Log.e(str2, "updateBlackListFile RemoteException ! ");
            } catch (Throwable th9) {
                th = th9;
                cursor3 = cursor;
                if (cursor3 != null) {
                }
                throw th;
            }
        }
        try {
            str2 = str;
            Log.d(str2, "updateBlackListFile filecount = " + fileCount);
            if (count2 == 100) {
                Log.d(str2, "SQL query exceed the limit 100 !");
            }
            this.mMediaProvider.applyBatch(ops);
            if (cursor3 != null) {
                cursor3.close();
            }
        } catch (SQLException e22) {
            Log.e(str2, "updateBlackListFile SQLException ! ");
        } catch (OperationApplicationException e23) {
            Log.e(str2, "MediaProvider upate all file Exception when use the applyBatch ! ");
        } catch (RemoteException e24) {
            Log.e(str2, "updateBlackListFile RemoteException ! ");
        } catch (Throwable th10) {
        }
        long end = System.currentTimeMillis();
        Log.d(str2, "updateBlackListFile total time = " + (end - start));
    }

    private void setDefaultRingtoneFileNames() {
        this.mDefaultRingtoneFilename = SystemProperties.get("ro.config.ringtone");
        HwFrameworkFactory.getHwMediaScannerManager().setHwDefaultRingtoneFileNames();
        this.mDefaultNotificationFilename = SystemProperties.get("ro.config.notification_sound");
        this.mDefaultAlarmAlertFilename = SystemProperties.get("ro.config.alarm_alert");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @UnsupportedAppUsage
    private boolean isDrmEnabled() {
        String prop = SystemProperties.get("drm.service.enabled");
        return prop != null && prop.equals("true");
    }

    public class MyMediaScannerClient implements MediaScannerClient {
        private static final int ALBUM = 1;
        private static final int ARTIST = 2;
        private static final int TITLE = 3;
        private static final long Time_1970 = 2082844800;
        private String mAlbum;
        private String mAlbumArtist;
        private String mArtist;
        private int mColorRange;
        private int mColorStandard;
        private int mColorTransfer;
        private int mCompilation;
        private String mComposer;
        private long mDate;
        private final SimpleDateFormat mDateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        private int mDuration;
        private long mFileSize;
        @UnsupportedAppUsage
        @Deprecated
        private int mFileType;
        private String mGenre;
        private int mHeight;
        private boolean mIsAlbumMessy;
        private boolean mIsArtistMessy;
        @UnsupportedAppUsage
        private boolean mIsDrm;
        private boolean mIsTitleMessy;
        private long mLastModified;
        @UnsupportedAppUsage
        private String mMimeType;
        @UnsupportedAppUsage
        private boolean mNoMedia;
        @UnsupportedAppUsage
        private String mPath;
        private boolean mScanSuccess;
        private String mTitle;
        private int mTrack;
        private int mWidth;
        private String mWriter;
        private int mYear;

        public MyMediaScannerClient() {
            this.mDateFormatter.setTimeZone(TimeZone.getTimeZone(Time.TIMEZONE_UTC));
        }

        @UnsupportedAppUsage
        public FileEntry beginFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            FileEntry entry;
            boolean z;
            boolean noMedia2;
            this.mMimeType = mimeType;
            this.mFileSize = fileSize;
            this.mIsDrm = false;
            this.mScanSuccess = true;
            if (MediaScanner.this.mNeedFilter && HwFrameworkFactory.getHwMediaScannerManager().isAudioFilterFile(path)) {
                return null;
            }
            if (path != null && (path.endsWith(".isma") || path.endsWith(".ismv"))) {
                this.mIsDrm = true;
            }
            if (!isDirectory) {
                if (noMedia || !MediaScanner.isNoMediaFile(path)) {
                    noMedia2 = noMedia;
                } else {
                    noMedia2 = true;
                }
                this.mNoMedia = noMedia2;
                if (this.mMimeType == null) {
                    this.mMimeType = MediaFile.getMimeTypeForFile(path);
                }
                if (MediaScanner.this.isDrmEnabled() && MediaFile.isDrmMimeType(this.mMimeType)) {
                    getMimeTypeFromDrm(path);
                }
            }
            FileEntry entry2 = (FileEntry) MediaScanner.this.mFileCache.remove(path);
            if (entry2 != null || (MediaScanner.this.mSkipExternelQuery && path.startsWith(MediaScanner.this.mExtStroagePath))) {
                entry = entry2;
            } else {
                long start = System.currentTimeMillis();
                FileEntry entry3 = MediaScanner.this.makeEntryFor(path);
                MediaScanner.access$614(MediaScanner.this, System.currentTimeMillis() - start);
                entry = entry3;
            }
            long delta = entry != null ? lastModified - entry.mLastModified : 0;
            boolean wasModified = delta > 1 || delta < -1;
            if (entry == null || wasModified) {
                if (wasModified) {
                    entry.mLastModified = lastModified;
                    z = true;
                } else {
                    z = true;
                    entry = new FileEntry(0, path, lastModified, isDirectory ? 12289 : 0, 0);
                }
                entry.mLastModifiedChanged = z;
            }
            if (!MediaScanner.this.mProcessPlaylists || !MediaFile.isPlayListMimeType(this.mMimeType)) {
                this.mArtist = null;
                this.mAlbumArtist = null;
                this.mAlbum = null;
                this.mTitle = null;
                this.mComposer = null;
                this.mGenre = null;
                this.mTrack = 0;
                this.mYear = 0;
                this.mDuration = 0;
                this.mPath = path;
                this.mDate = 0;
                this.mLastModified = lastModified;
                this.mWriter = null;
                this.mCompilation = 0;
                this.mWidth = 0;
                this.mHeight = 0;
                this.mColorStandard = -1;
                this.mColorTransfer = -1;
                this.mColorRange = -1;
                this.mIsAlbumMessy = false;
                this.mIsArtistMessy = false;
                this.mIsTitleMessy = false;
                return entry;
            }
            MediaScanner.this.mPlayLists.add(entry);
            return null;
        }

        @Override // android.media.MediaScannerClient
        public void setBlackListFlag(boolean flag) {
            MediaScanner.this.mBlackListFlag = true;
        }

        @Override // android.media.MediaScannerClient
        @UnsupportedAppUsage
        public void scanFile(String path, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            long start = System.currentTimeMillis();
            doScanFile(path, null, lastModified, fileSize, isDirectory, false, noMedia);
            long end = System.currentTimeMillis();
            MediaScanner.access$1014(MediaScanner.this, end - start);
            if (MediaScanner.this.mScanDirectoryFilesNum % 1000 == 0) {
                if (MediaScanner.this.mScanDirectoryFilesNum == 0) {
                    MediaScanner.this.mCurrentTimeMillis = start;
                } else {
                    Log.d(MediaScanner.TAG, "scan file number: " + MediaScanner.this.mScanDirectoryFilesNum + ",time: " + (end - MediaScanner.this.mCurrentTimeMillis));
                    MediaScanner.this.mCurrentTimeMillis = end;
                }
            }
            MediaScanner.access$1104(MediaScanner.this);
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x0068 A[Catch:{ RemoteException -> 0x02a0 }] */
        /* JADX WARNING: Removed duplicated region for block: B:52:0x0107 A[Catch:{ RemoteException -> 0x029e }] */
        /* JADX WARNING: Removed duplicated region for block: B:53:0x0117 A[Catch:{ RemoteException -> 0x029e }] */
        @UnsupportedAppUsage
        public Uri doScanFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean scanAlways, boolean noMedia) {
            RemoteException e;
            String fingerprint;
            boolean scanAlways2;
            String path2 = path;
            if (path2 != null && (path.toUpperCase().endsWith(".HEIC") || path.toUpperCase().endsWith(".HEIF"))) {
                HwMediaMonitorManager.writeBigData(HwMediaMonitorUtils.BD_MEDIA_HEIF, HwMediaMonitorUtils.M_HEIF_SCAN);
            }
            Uri result = null;
            try {
                FileEntry entry = beginFile(path, mimeType, lastModified, fileSize, isDirectory, noMedia);
                if (entry == null) {
                    return null;
                }
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    entry.mRowId = 0;
                }
                String fingerprint2 = Build.FINGERPRINTEX;
                if (!"unknown".equals(fingerprint2)) {
                    if (!fingerprint2.isEmpty()) {
                        fingerprint = fingerprint2;
                        if (entry.mPath != null) {
                            if ((!MediaScanner.this.mDefaultNotificationSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename)) || ((!MediaScanner.this.mDefaultRingtoneSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultRingtoneFilename)) || (!MediaScanner.this.mDefaultAlarmSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultAlarmAlertFilename)))) {
                                Log.w(MediaScanner.TAG, "forcing rescan of " + entry.mPath + "since ringtone setting didn't finish");
                                scanAlways2 = true;
                                if (!noMedia) {
                                }
                                MediaScanner.this.mBlackListFlag = false;
                                MediaScanner.this.mIsImageType = false;
                                return result;
                            } else if (MediaScanner.isSystemSoundWithMetadata(entry.mPath) && !fingerprint.equals(MediaScanner.sLastInternalScanFingerprint)) {
                                if (MediaScanner.DEBUG) {
                                    Log.i(MediaScanner.TAG, "forcing rescan of " + entry.mPath + " since build fingerprint changed");
                                }
                                scanAlways2 = true;
                                if (entry.mLastModifiedChanged || scanAlways2) {
                                    if (!noMedia) {
                                        result = endFile(entry, false, false, false, false, false, false);
                                    } else {
                                        boolean isaudio = MediaFile.isAudioMimeType(this.mMimeType);
                                        boolean isvideo = MediaFile.isVideoMimeType(this.mMimeType);
                                        boolean isimage = MediaFile.isImageMimeType(this.mMimeType);
                                        MediaScanner.this.mIsImageType = isimage;
                                        if (isaudio || isvideo || isimage) {
                                            path2 = Environment.maybeTranslateEmulatedPathToInternal(new File(path2)).getAbsolutePath();
                                        }
                                        if (isaudio || isvideo) {
                                            long startAV = -1;
                                            try {
                                                if (MediaScanner.ENABLE_PROFILE_SCANNER) {
                                                    startAV = System.currentTimeMillis();
                                                }
                                                this.mScanSuccess = MediaScanner.this.processFile(path2, mimeType, this);
                                                if (MediaScanner.ENABLE_PROFILE_SCANNER) {
                                                    MediaScanner.access$2614(MediaScanner.this, System.currentTimeMillis() - startAV);
                                                }
                                                if (isaudio) {
                                                    MediaScanner.access$2708(MediaScanner.this);
                                                } else {
                                                    MediaScanner.access$2808(MediaScanner.this);
                                                }
                                            } catch (RemoteException e2) {
                                                e = e2;
                                                Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                                                MediaScanner.this.mBlackListFlag = false;
                                                MediaScanner.this.mIsImageType = false;
                                                return result;
                                            }
                                        }
                                        if (isimage) {
                                            long startImg = -1;
                                            if (MediaScanner.ENABLE_PROFILE_SCANNER) {
                                                startImg = System.currentTimeMillis();
                                            }
                                            this.mScanSuccess = processImageFile(path2);
                                            if (MediaScanner.ENABLE_PROFILE_SCANNER) {
                                                MediaScanner.access$2914(MediaScanner.this, System.currentTimeMillis() - startImg);
                                            }
                                            MediaScanner.access$3008(MediaScanner.this);
                                        }
                                        String lowpath = path2.toLowerCase(Locale.ROOT);
                                        boolean ringtones = this.mScanSuccess && lowpath.indexOf(MediaScanner.RINGTONES_DIR) > 0;
                                        boolean notifications = this.mScanSuccess && lowpath.indexOf(MediaScanner.NOTIFICATIONS_DIR) > 0;
                                        boolean alarms = this.mScanSuccess && lowpath.indexOf(MediaScanner.ALARMS_DIR) > 0;
                                        boolean podcasts = this.mScanSuccess && lowpath.indexOf(MediaScanner.PODCASTS_DIR) > 0;
                                        boolean audiobooks = this.mScanSuccess && lowpath.indexOf(MediaScanner.AUDIOBOOKS_DIR) > 0;
                                        boolean music = this.mScanSuccess && (lowpath.indexOf(MediaScanner.MUSIC_DIR) > 0 || (!ringtones && !notifications && !alarms && !podcasts && !audiobooks));
                                        boolean ringtones2 = ringtones | HwThemeManager.isTRingtones(lowpath);
                                        boolean notifications2 = notifications | HwThemeManager.isTNotifications(lowpath);
                                        boolean alarms2 = alarms | HwThemeManager.isTAlarms(lowpath);
                                        if (isaudio && (this.mIsAlbumMessy || this.mIsArtistMessy || this.mIsTitleMessy)) {
                                            HwFrameworkFactory.getHwMediaScannerManager().initializeSniffer(this.mPath);
                                            if (this.mIsAlbumMessy) {
                                                this.mAlbum = HwFrameworkFactory.getHwMediaScannerManager().postHandleStringTag(this.mAlbum, this.mPath, 1);
                                            }
                                            if (this.mIsArtistMessy) {
                                                this.mArtist = HwFrameworkFactory.getHwMediaScannerManager().postHandleStringTag(this.mArtist, this.mPath, 2);
                                            }
                                            if (this.mIsTitleMessy) {
                                                this.mTitle = HwFrameworkFactory.getHwMediaScannerManager().postHandleStringTag(this.mTitle, this.mPath, 3);
                                            }
                                            HwFrameworkFactory.getHwMediaScannerManager().resetSniffer();
                                        }
                                        result = endFile(entry, ringtones2, notifications2, alarms2, podcasts, audiobooks, music);
                                    }
                                }
                                MediaScanner.this.mBlackListFlag = false;
                                MediaScanner.this.mIsImageType = false;
                                return result;
                            }
                        }
                        scanAlways2 = scanAlways;
                        if (!noMedia) {
                        }
                        MediaScanner.this.mBlackListFlag = false;
                        MediaScanner.this.mIsImageType = false;
                        return result;
                    }
                }
                fingerprint = Build.FINGERPRINT;
                if (entry.mPath != null) {
                }
                scanAlways2 = scanAlways;
                try {
                    if (!noMedia) {
                    }
                } catch (RemoteException e3) {
                    e = e3;
                    Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                    MediaScanner.this.mBlackListFlag = false;
                    MediaScanner.this.mIsImageType = false;
                    return result;
                }
                MediaScanner.this.mBlackListFlag = false;
                MediaScanner.this.mIsImageType = false;
                return result;
            } catch (RemoteException e4) {
                e = e4;
                Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                MediaScanner.this.mBlackListFlag = false;
                MediaScanner.this.mIsImageType = false;
                return result;
            }
        }

        private long parseDate(String date) {
            try {
                return this.mDateFormatter.parse(date).getTime();
            } catch (ParseException e) {
                return 0;
            }
        }

        private int parseSubstring(String s, int start, int defaultValue) {
            int length = s.length();
            if (start == length) {
                return defaultValue;
            }
            int start2 = start + 1;
            char ch = s.charAt(start);
            if (ch < '0' || ch > '9') {
                return defaultValue;
            }
            int result = ch - '0';
            while (start2 < length) {
                int start3 = start2 + 1;
                char ch2 = s.charAt(start2);
                if (ch2 < '0' || ch2 > '9') {
                    return result;
                }
                result = (result * 10) + (ch2 - '0');
                start2 = start3;
            }
            return result;
        }

        @Override // android.media.MediaScannerClient
        @UnsupportedAppUsage
        public void handleStringTag(String name, String value) {
            boolean z = true;
            boolean isAlbum = name.equalsIgnoreCase("album") || name.startsWith("album;");
            boolean isArtist = name.equalsIgnoreCase("artist") || name.startsWith("artist;");
            boolean isTitle = name.equalsIgnoreCase("title") || name.startsWith("title;");
            if (isAlbum) {
                this.mIsAlbumMessy = HwFrameworkFactory.getHwMediaScannerManager().preHandleStringTag(value, this.mMimeType);
            }
            if (isArtist) {
                this.mIsArtistMessy = HwFrameworkFactory.getHwMediaScannerManager().preHandleStringTag(value, this.mMimeType);
            }
            if (isTitle) {
                this.mIsTitleMessy = HwFrameworkFactory.getHwMediaScannerManager().preHandleStringTag(value, this.mMimeType);
            }
            if (name.equalsIgnoreCase("title") || name.startsWith("title;")) {
                this.mTitle = value;
            } else if (name.equalsIgnoreCase("artist") || name.startsWith("artist;")) {
                this.mArtist = value.trim();
            } else if (name.equalsIgnoreCase("albumartist") || name.startsWith("albumartist;") || name.equalsIgnoreCase("band") || name.startsWith("band;")) {
                this.mAlbumArtist = value.trim();
            } else if (name.equalsIgnoreCase("album") || name.startsWith("album;")) {
                this.mAlbum = value.trim();
            } else if (name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.COMPOSER) || name.startsWith("composer;")) {
                this.mComposer = value.trim();
            } else if (MediaScanner.this.mProcessGenres && (name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.GENRE) || name.startsWith("genre;"))) {
                this.mGenre = getGenreName(value);
            } else if (name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.YEAR) || name.startsWith("year;")) {
                this.mYear = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("tracknumber") || name.startsWith("tracknumber;")) {
                this.mTrack = ((this.mTrack / 1000) * 1000) + parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("discnumber") || name.equals("set") || name.startsWith("set;")) {
                this.mTrack = (parseSubstring(value, 0, 0) * 1000) + (this.mTrack % 1000);
            } else if (name.equalsIgnoreCase("duration")) {
                this.mDuration = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("writer") || name.startsWith("writer;")) {
                this.mWriter = value.trim();
            } else if (name.equalsIgnoreCase(MediaStore.Audio.AudioColumns.COMPILATION)) {
                this.mCompilation = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("isdrm")) {
                if (parseSubstring(value, 0, 0) != 1) {
                    z = false;
                }
                this.mIsDrm = z;
            } else if (name.equalsIgnoreCase("date")) {
                this.mDate = parseDate(value);
            } else if (name.equalsIgnoreCase("width")) {
                this.mWidth = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("height")) {
                this.mHeight = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("colorstandard")) {
                this.mColorStandard = parseSubstring(value, 0, -1);
            } else if (name.equalsIgnoreCase("colortransfer")) {
                this.mColorTransfer = parseSubstring(value, 0, -1);
            } else if (name.equalsIgnoreCase("colorrange")) {
                this.mColorRange = parseSubstring(value, 0, -1);
            }
        }

        private boolean convertGenreCode(String input, String expected) {
            String output = getGenreName(input);
            if (output.equals(expected)) {
                return true;
            }
            Log.d(MediaScanner.TAG, "'" + input + "' -> '" + output + "', expected '" + expected + "'");
            return false;
        }

        private void testGenreNameConverter() {
            convertGenreCode(WifiScanLog.EVENT_KEY2, "Country");
            convertGenreCode("(2)", "Country");
            convertGenreCode("(2", "(2");
            convertGenreCode("2 Foo", "Country");
            convertGenreCode("(2) Foo", "Country");
            convertGenreCode("(2 Foo", "(2 Foo");
            convertGenreCode("2Foo", "2Foo");
            convertGenreCode("(2)Foo", "Country");
            convertGenreCode("200 Foo", "Foo");
            convertGenreCode("(200) Foo", "Foo");
            convertGenreCode("200Foo", "200Foo");
            convertGenreCode("(200)Foo", "Foo");
            convertGenreCode("200)Foo", "200)Foo");
            convertGenreCode("200) Foo", "200) Foo");
        }

        public String getGenreName(String genreTagValue) {
            if (genreTagValue == null) {
                return null;
            }
            int length = genreTagValue.length();
            if (length > 0) {
                boolean parenthesized = false;
                StringBuffer number = new StringBuffer();
                int i = 0;
                while (i < length) {
                    char c = genreTagValue.charAt(i);
                    if (i != 0 || c != '(') {
                        if (!Character.isDigit(c)) {
                            break;
                        }
                        number.append(c);
                    } else {
                        parenthesized = true;
                    }
                    i++;
                }
                char charAfterNumber = i < length ? genreTagValue.charAt(i) : ' ';
                if ((parenthesized && charAfterNumber == ')') || (!parenthesized && Character.isWhitespace(charAfterNumber))) {
                    try {
                        short genreIndex = Short.parseShort(number.toString());
                        if (genreIndex >= 0) {
                            if (genreIndex < MediaScanner.ID3_GENRES.length && MediaScanner.ID3_GENRES[genreIndex] != null) {
                                return MediaScanner.ID3_GENRES[genreIndex];
                            }
                            if (genreIndex == 255) {
                                return null;
                            }
                            if (genreIndex >= 255 || i + 1 >= length) {
                                return number.toString();
                            }
                            if (parenthesized && charAfterNumber == ')') {
                                i++;
                            }
                            String ret = genreTagValue.substring(i).trim();
                            if (ret.length() != 0) {
                                return ret;
                            }
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            return genreTagValue;
        }

        private boolean processImageFile(String path) {
            try {
                MediaScanner.this.mBitmapOptions.outWidth = 0;
                MediaScanner.this.mBitmapOptions.outHeight = 0;
                if (HwFrameworkFactory.getHwMediaScannerManager().isBitmapSizeTooLarge(path)) {
                    this.mWidth = -1;
                    this.mHeight = -1;
                } else {
                    BitmapFactory.decodeFile(path, MediaScanner.this.mBitmapOptions);
                    this.mWidth = MediaScanner.this.mBitmapOptions.outWidth;
                    this.mHeight = MediaScanner.this.mBitmapOptions.outHeight;
                }
                if (this.mWidth <= 0 || this.mHeight <= 0) {
                    return false;
                }
                return true;
            } catch (Throwable th) {
                return false;
            }
        }

        @Override // android.media.MediaScannerClient
        @UnsupportedAppUsage
        public void setMimeType(String mimeType) {
            if ("audio/mp4".equals(this.mMimeType) && mimeType.startsWith("video")) {
                return;
            }
            if ("m4a".equals(MediaFile.getFileExtension(this.mPath)) && MediaFormat.MIMETYPE_AUDIO_MPEG.equals(this.mMimeType)) {
                return;
            }
            if (!this.mMimeType.startsWith("video") || !mimeType.startsWith("audio")) {
                this.mMimeType = mimeType;
            }
        }

        @UnsupportedAppUsage
        private ContentValues toValues() {
            ContentValues map = new ContentValues();
            map.put("_data", this.mPath);
            map.put("title", this.mTitle);
            map.put("date_modified", Long.valueOf(this.mLastModified));
            map.put("_size", Long.valueOf(this.mFileSize));
            map.put("mime_type", this.mMimeType);
            map.put(MediaStore.MediaColumns.IS_DRM, Boolean.valueOf(this.mIsDrm));
            map.putNull(MediaStore.MediaColumns.HASH);
            String resolution = null;
            int i = this.mWidth;
            if (i > 0 && this.mHeight > 0) {
                map.put("width", Integer.valueOf(i));
                map.put("height", Integer.valueOf(this.mHeight));
                resolution = this.mWidth + "x" + this.mHeight;
            }
            if (!this.mNoMedia) {
                boolean isVideoMimeType = MediaFile.isVideoMimeType(this.mMimeType);
                String str = MediaStore.UNKNOWN_STRING;
                if (isVideoMimeType) {
                    String str2 = this.mArtist;
                    map.put("artist", (str2 == null || str2.length() <= 0) ? str : this.mArtist);
                    String str3 = this.mAlbum;
                    if (str3 != null && str3.length() > 0) {
                        str = this.mAlbum;
                    }
                    map.put("album", str);
                    map.put("duration", Integer.valueOf(this.mDuration));
                    if (resolution != null) {
                        map.put(MediaStore.Video.VideoColumns.RESOLUTION, resolution);
                    }
                    int i2 = this.mColorStandard;
                    if (i2 >= 0) {
                        map.put(MediaStore.Video.VideoColumns.COLOR_STANDARD, Integer.valueOf(i2));
                    }
                    int i3 = this.mColorTransfer;
                    if (i3 >= 0) {
                        map.put(MediaStore.Video.VideoColumns.COLOR_TRANSFER, Integer.valueOf(i3));
                    }
                    int i4 = this.mColorRange;
                    if (i4 >= 0) {
                        map.put(MediaStore.Video.VideoColumns.COLOR_RANGE, Integer.valueOf(i4));
                    }
                    long j = this.mDate;
                    if (j > Time_1970) {
                        map.put("datetaken", Long.valueOf(j));
                    }
                    if (this.mDuration == 0) {
                        Log.e(MediaScanner.TAG, "video file duration = 0 and file size:" + this.mFileSize);
                    }
                } else if (!MediaFile.isImageMimeType(this.mMimeType) && MediaFile.isAudioMimeType(this.mMimeType)) {
                    String str4 = this.mArtist;
                    map.put("artist", (str4 == null || str4.length() <= 0) ? str : this.mArtist);
                    String str5 = this.mAlbumArtist;
                    map.put(MediaStore.Audio.AudioColumns.ALBUM_ARTIST, (str5 == null || str5.length() <= 0) ? null : this.mAlbumArtist);
                    String str6 = this.mAlbum;
                    if (str6 != null && str6.length() > 0) {
                        str = this.mAlbum;
                    }
                    map.put("album", str);
                    map.put(MediaStore.Audio.AudioColumns.COMPOSER, this.mComposer);
                    map.put(MediaStore.Audio.AudioColumns.GENRE, this.mGenre);
                    int i5 = this.mYear;
                    if (i5 != 0) {
                        map.put(MediaStore.Audio.AudioColumns.YEAR, Integer.valueOf(i5));
                    }
                    map.put(MediaStore.Audio.AudioColumns.TRACK, Integer.valueOf(this.mTrack));
                    map.put("duration", Integer.valueOf(this.mDuration));
                    map.put(MediaStore.Audio.AudioColumns.COMPILATION, Integer.valueOf(this.mCompilation));
                }
            }
            return map;
        }

        private String maybeOverrideMimeType(String extMimeType, String xmpMimeType) {
            int xmpSplit;
            if (!TextUtils.isEmpty(xmpMimeType) && (xmpSplit = xmpMimeType.indexOf(47)) != -1 && extMimeType.regionMatches(0, xmpMimeType, 0, xmpSplit + 1)) {
                return xmpMimeType;
            }
            return extMimeType;
        }

        private void withXmpValues(ContentValues values, XmpInterface xmp, String mimeType) {
            values.put("mime_type", maybeOverrideMimeType(mimeType, xmp.getFormat()));
            values.put("document_id", xmp.getDocumentId());
            values.put(MediaStore.MediaColumns.INSTANCE_ID, xmp.getInstanceId());
            values.put(MediaStore.MediaColumns.ORIGINAL_DOCUMENT_ID, xmp.getOriginalDocumentId());
        }

        private long parseDateTaken(ExifInterface exif, long lastModifiedTime) {
            long originalTime = exif.getDateTimeOriginal();
            if (exif.hasAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL)) {
                return originalTime;
            }
            long gpsTime = exif.getGpsDateTime();
            boolean hasLatLong = exif.getLatLong(new float[2]);
            if (gpsTime > 0 && hasLatLong) {
                long offset = gpsTime - originalTime;
                if (Math.abs(offset) < 86400000) {
                    return originalTime + (((long) Math.round(((float) offset) / 900000.0f)) * AlarmManager.INTERVAL_FIFTEEN_MINUTES);
                }
            }
            if (lastModifiedTime > 0) {
                long offset2 = lastModifiedTime - originalTime;
                if (Math.abs(offset2) < 86400000) {
                    return originalTime + (((long) Math.round(((float) offset2) / 900000.0f)) * AlarmManager.INTERVAL_FIFTEEN_MINUTES);
                }
            }
            if (originalTime != -1) {
                return originalTime;
            }
            return exif.getDateTime();
        }

        /* JADX WARNING: Removed duplicated region for block: B:125:0x029e  */
        /* JADX WARNING: Removed duplicated region for block: B:153:0x0330  */
        /* JADX WARNING: Removed duplicated region for block: B:165:0x0387  */
        @UnsupportedAppUsage
        private Uri endFile(FileEntry entry, boolean ringtones, boolean notifications, boolean alarms, boolean podcasts, boolean audiobooks, boolean music) throws RemoteException {
            Uri tableUri;
            int mediaType;
            boolean needToSetSettings2;
            boolean needToSetSettings;
            Uri result;
            long rowId;
            Uri tableUri2;
            int degree;
            String str = this.mArtist;
            if (str == null || str.length() == 0) {
                this.mArtist = this.mAlbumArtist;
            }
            ContentValues values = toValues();
            String title = values.getAsString("title");
            if (title == null || TextUtils.isEmpty(title.trim())) {
                values.put("title", MediaFile.getFileTitle(values.getAsString("_data")));
            }
            if (MediaStore.UNKNOWN_STRING.equals(values.getAsString("album"))) {
                String album = values.getAsString("_data");
                int lastSlash = album.lastIndexOf(47);
                if (lastSlash >= 0) {
                    int previousSlash = 0;
                    while (true) {
                        int idx = album.indexOf(47, previousSlash + 1);
                        if (idx < 0 || idx >= lastSlash) {
                            break;
                        }
                        previousSlash = idx;
                    }
                    if (previousSlash != 0) {
                        values.put("album", album.substring(previousSlash + 1, lastSlash));
                    }
                }
            }
            long rowId2 = entry.mRowId;
            if (MediaFile.isAudioMimeType(this.mMimeType) && (rowId2 == 0 || MediaScanner.this.mMtpObjectHandle != 0)) {
                values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, Boolean.valueOf(ringtones));
                values.put(MediaStore.Audio.AudioColumns.IS_NOTIFICATION, Boolean.valueOf(notifications));
                values.put(MediaStore.Audio.AudioColumns.IS_ALARM, Boolean.valueOf(alarms));
                values.put(MediaStore.Audio.AudioColumns.IS_MUSIC, Boolean.valueOf(music));
                values.put(MediaStore.Audio.AudioColumns.IS_PODCAST, Boolean.valueOf(podcasts));
                values.put(MediaStore.Audio.AudioColumns.IS_AUDIOBOOK, Boolean.valueOf(audiobooks));
            } else if (MediaFile.isExifMimeType(this.mMimeType) && !this.mNoMedia) {
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(entry.mPath);
                } catch (Exception e) {
                }
                if (exif != null) {
                    float[] latlng = new float[2];
                    if (exif.getLatLong(latlng)) {
                        values.put("latitude", Float.valueOf(latlng[0]));
                        values.put("longitude", Float.valueOf(latlng[1]));
                    }
                    long time = parseDateTaken(exif, this.mLastModified * 1000);
                    if (time != -1) {
                        values.put("datetaken", Long.valueOf(time));
                    }
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                    if (orientation != -1) {
                        if (orientation == 3) {
                            degree = 180;
                        } else if (orientation == 6) {
                            degree = 90;
                        } else if (orientation != 8) {
                            degree = 0;
                        } else {
                            degree = 270;
                        }
                        values.put("orientation", Integer.valueOf(degree));
                    }
                    scannerSpecialImageType(values, exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
                    try {
                        withXmpValues(values, XmpInterface.fromContainer(exif), this.mMimeType);
                    } catch (IOException e2) {
                        Log.v(MediaScanner.TAG, "Image xmp values get exception");
                    }
                }
                HwFrameworkFactory.getHwMediaScannerManager().initializeHwVoiceAndFocus(entry.mPath, values);
            }
            MediaScanner.this.updateValues(entry.mPath, values);
            Uri tableUri3 = MediaScanner.this.mFilesUri;
            int mediaType2 = 0;
            MediaInserter inserter = MediaScanner.this.mMediaInserter;
            if (this.mNoMedia || (MediaScanner.this.mBlackListFlag && MediaScanner.this.mIsImageType)) {
                tableUri = tableUri3;
                mediaType = 0;
            } else {
                if (MediaFile.isVideoMimeType(this.mMimeType)) {
                    mediaType2 = 3;
                    tableUri2 = MediaScanner.this.mVideoUri;
                } else if (MediaFile.isImageMimeType(this.mMimeType)) {
                    mediaType2 = 1;
                    tableUri2 = MediaScanner.this.mImagesUri;
                } else if (MediaFile.isAudioMimeType(this.mMimeType)) {
                    mediaType2 = 2;
                    tableUri2 = MediaScanner.this.mAudioUri;
                } else if (MediaFile.isPlayListMimeType(this.mMimeType)) {
                    mediaType2 = 4;
                    tableUri2 = MediaScanner.this.mPlaylistsUri;
                } else {
                    tableUri2 = tableUri3;
                }
                if (mediaType2 == 2 || mediaType2 == 3) {
                    try {
                        withXmpValues(values, XmpInterface.fromContainer(IsoInterface.fromFileDescriptor(new FileInputStream(new File(entry.mPath)).getFD())), this.mMimeType);
                    } catch (IOException e3) {
                        Log.v(MediaScanner.TAG, "Media xmp values get exception");
                    }
                }
                mediaType = mediaType2;
                tableUri = tableUri2;
            }
            Uri result2 = null;
            boolean needToSetSettings3 = false;
            if (!notifications || MediaScanner.this.mDefaultNotificationSet) {
                if (ringtones) {
                    if ((!MediaScanner.this.mDefaultRingtoneSet && TextUtils.isEmpty(MediaScanner.this.mDefaultRingtoneFilename)) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultRingtoneFilename)) {
                        needToSetSettings3 = true;
                    }
                    needToSetSettings = needToSetSettings3;
                    needToSetSettings2 = HwFrameworkFactory.getHwMediaScannerManager().hwNeedSetSettings(entry.mPath);
                } else if (alarms && !MediaScanner.this.mDefaultAlarmSet && (TextUtils.isEmpty(MediaScanner.this.mDefaultAlarmAlertFilename) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultAlarmAlertFilename))) {
                    needToSetSettings = true;
                    needToSetSettings2 = false;
                }
                if (rowId2 == 0) {
                    if (MediaScanner.this.mMtpObjectHandle != 0) {
                        values.put(MediaStore.MediaColumns.MEDIA_SCANNER_NEW_OBJECT_ID, Integer.valueOf(MediaScanner.this.mMtpObjectHandle));
                    }
                    if (tableUri == MediaScanner.this.mFilesUri) {
                        int format = entry.mFormat;
                        if (format == 0) {
                            format = MediaFile.getFormatCode(entry.mPath, this.mMimeType);
                        }
                        values.put("format", Integer.valueOf(format));
                    }
                    if (MediaScanner.this.mBlackListFlag && MediaScanner.this.mIsImageType) {
                        values.put("media_type", (Integer) 10);
                    }
                    values.put(MediaStore.Files.FileColumns.STORAGE_ID, Integer.valueOf(MediaScanner.this.mStorageIdForCurScanDIR));
                    if (inserter == null || needToSetSettings || needToSetSettings2) {
                        if (inserter != null) {
                            inserter.flushAll();
                        }
                        result2 = MediaScanner.this.mMediaProvider.insert(tableUri, values);
                    } else if (entry.mFormat == 12289) {
                        inserter.insertwithPriority(tableUri, values);
                    } else {
                        inserter.insert(tableUri, values);
                    }
                    if (result2 != null) {
                        long rowId3 = ContentUris.parseId(result2);
                        entry.mRowId = rowId3;
                        result = result2;
                        rowId = rowId3;
                    } else {
                        result = result2;
                        rowId = rowId2;
                    }
                } else {
                    Uri result3 = ContentUris.withAppendedId(tableUri, rowId2);
                    values.remove("_data");
                    if (MediaScanner.this.mBlackListFlag && MediaScanner.this.mIsImageType) {
                        values.put("media_type", (Integer) 10);
                    } else if (!this.mNoMedia && mediaType != entry.mMediaType) {
                        ContentValues mediaTypeValues = new ContentValues();
                        mediaTypeValues.put("media_type", Integer.valueOf(mediaType));
                        MediaScanner.this.mMediaProvider.update(ContentUris.withAppendedId(MediaScanner.this.mFilesUri, rowId2), mediaTypeValues, null, null);
                    }
                    MediaScanner.this.mMediaProvider.update(result3, values, null, null);
                    result = result3;
                    rowId = rowId2;
                }
                if (needToSetSettings) {
                    if (notifications) {
                        setRingtoneIfNotSet(Settings.System.NOTIFICATION_SOUND, tableUri, rowId);
                        MediaScanner.this.mDefaultNotificationSet = true;
                    } else if (ringtones) {
                        setRingtoneIfNotSet(Settings.System.RINGTONE, tableUri, rowId);
                        MediaScanner.this.mDefaultRingtoneSet = true;
                    } else if (alarms) {
                        setRingtoneIfNotSet(Settings.System.ALARM_ALERT, tableUri, rowId);
                        MediaScanner.this.mDefaultAlarmSet = true;
                    }
                }
                HwFrameworkFactory.getHwMediaScannerManager().hwSetRingtone2Settings(needToSetSettings2, ringtones, tableUri, rowId, MediaScanner.this.mContext);
                return result;
            } else if (TextUtils.isEmpty(MediaScanner.this.mDefaultNotificationFilename) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename)) {
                needToSetSettings = true;
                needToSetSettings2 = false;
                if (rowId2 == 0) {
                }
                if (needToSetSettings) {
                }
                HwFrameworkFactory.getHwMediaScannerManager().hwSetRingtone2Settings(needToSetSettings2, ringtones, tableUri, rowId, MediaScanner.this.mContext);
                return result;
            }
            needToSetSettings = false;
            needToSetSettings2 = false;
            if (rowId2 == 0) {
            }
            if (needToSetSettings) {
            }
            HwFrameworkFactory.getHwMediaScannerManager().hwSetRingtone2Settings(needToSetSettings2, ringtones, tableUri, rowId, MediaScanner.this.mContext);
            return result;
        }

        private void scannerSpecialImageType(ContentValues values, String exifDescription) {
            int hdrType = 0;
            if ("hdr".equals(exifDescription)) {
                hdrType = 1;
            } else if (MediaScanner.IMAGE_TYPE_JHDR.equals(exifDescription)) {
                hdrType = 2;
            }
            values.put(MediaStore.Images.ImageColumns.IS_HDR, Integer.valueOf(hdrType));
            if (exifDescription != null && exifDescription.length() >= 3) {
                String subString = exifDescription.substring(0, 3);
                if (MediaScanner.IMAGE_TYPE_PORTRAIT_FRONT.equals(subString)) {
                    values.put(MediaScanner.HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, (Integer) 30);
                } else if (MediaScanner.IMAGE_TYPE_PORTRAIT_REAR.equals(subString)) {
                    values.put(MediaScanner.HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, (Integer) 31);
                } else if (MediaScanner.IMAGE_TYPE_BEAUTY_FRONT.equals(subString)) {
                    values.put(MediaScanner.HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, (Integer) 40);
                } else if (MediaScanner.IMAGE_TYPE_BEAUTY_REAR.equals(subString)) {
                    values.put(MediaScanner.HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, (Integer) 41);
                }
            }
        }

        private boolean doesPathHaveFilename(String path, String filename) {
            int pathFilenameStart = path.lastIndexOf(File.separatorChar) + 1;
            int filenameLength = filename.length();
            if (!path.regionMatches(pathFilenameStart, filename, 0, filenameLength) || pathFilenameStart + filenameLength != path.length()) {
                return false;
            }
            return true;
        }

        private void setRingtoneIfNotSet(String settingName, Uri uri, long rowId) {
            Log.v(MediaScanner.TAG, "setRingtoneIfNotSet.name:" + settingName + " value:" + uri + rowId);
            if (!MediaScanner.this.wasRingtoneAlreadySet(settingName)) {
                ContentResolver cr = MediaScanner.this.mContext.getContentResolver();
                if (TextUtils.isEmpty(Settings.System.getString(cr, settingName))) {
                    Log.v(MediaScanner.TAG, "setSetting when NotSet");
                    Uri settingUri = Settings.System.getUriFor(settingName);
                    RingtoneManager.setActualDefaultRingtoneUri(MediaScanner.this.mContext, RingtoneManager.getDefaultType(settingUri), ContentUris.withAppendedId(uri, rowId));
                }
                Settings.System.putInt(cr, MediaScanner.this.settingSetIndicatorName(settingName), 1);
            }
        }

        @UnsupportedAppUsage
        @Deprecated
        private int getFileTypeFromDrm(String path) {
            return 0;
        }

        private void getMimeTypeFromDrm(String path) {
            this.mMimeType = null;
            if (MediaScanner.this.mDrmManagerClient == null) {
                MediaScanner mediaScanner = MediaScanner.this;
                mediaScanner.mDrmManagerClient = new DrmManagerClient(mediaScanner.mContext);
            }
            if (MediaScanner.this.mDrmManagerClient.canHandle(path, (String) null)) {
                this.mIsDrm = true;
                this.mMimeType = MediaScanner.this.mDrmManagerClient.getOriginalMimeType(path);
            }
            if (this.mMimeType == null) {
                this.mMimeType = "application/octet-stream";
            }
        }
    }

    /* access modifiers changed from: private */
    public static boolean isSystemSoundWithMetadata(String path) {
        if (path.startsWith(SYSTEM_SOUNDS_DIR + ALARMS_DIR)) {
            return true;
        }
        if (path.startsWith(SYSTEM_SOUNDS_DIR + RINGTONES_DIR)) {
            return true;
        }
        if (path.startsWith(SYSTEM_SOUNDS_DIR + NOTIFICATIONS_DIR)) {
            return true;
        }
        if (path.startsWith(OEM_SOUNDS_DIR + ALARMS_DIR)) {
            return true;
        }
        if (path.startsWith(OEM_SOUNDS_DIR + RINGTONES_DIR)) {
            return true;
        }
        if (path.startsWith(OEM_SOUNDS_DIR + NOTIFICATIONS_DIR)) {
            return true;
        }
        if (path.startsWith(PRODUCT_SOUNDS_DIR + ALARMS_DIR)) {
            return true;
        }
        if (path.startsWith(PRODUCT_SOUNDS_DIR + RINGTONES_DIR)) {
            return true;
        }
        if (path.startsWith(PRODUCT_SOUNDS_DIR + NOTIFICATIONS_DIR)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String settingSetIndicatorName(String base) {
        return base + "_set";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean wasRingtoneAlreadySet(String name) {
        try {
            return Settings.System.getInt(this.mContext.getContentResolver(), settingSetIndicatorName(name)) != 0;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    /* JADX INFO: Multiple debug info for r14v13 'rowId'  long: [D('builder' android.net.Uri$Builder), D('rowId' long)] */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x02d9  */
    @UnsupportedAppUsage
    private void prescan(String filePath, boolean prescanFiles) throws RemoteException {
        String[] selectionArgs;
        String where;
        Cursor c;
        Throwable th;
        long start;
        Uri.Builder builder;
        Cursor c2;
        long queryTime;
        int count;
        String where2;
        Uri.Builder builder2;
        long rowId;
        long rowId2;
        MediaScanner mediaScanner = this;
        Cursor c3 = null;
        long queryTime2 = 0;
        long totalAccessTime = 0;
        int fileCount = 0;
        Log.d(TAG, "prescan begin prescanFiles: " + prescanFiles);
        mediaScanner.mPlayLists.clear();
        HashMap<String, FileEntry> hashMap = mediaScanner.mFileCache;
        if (hashMap == null) {
            mediaScanner.mFileCache = new HashMap<>();
        } else {
            hashMap.clear();
        }
        String path = "";
        if (filePath != null) {
            selectionArgs = new String[]{path, filePath};
            where = "_id>? AND _data=?";
        } else {
            selectionArgs = new String[]{path};
            where = "_id>?";
        }
        mediaScanner.mDefaultRingtoneSet = mediaScanner.wasRingtoneAlreadySet(Settings.System.RINGTONE);
        mediaScanner.mDefaultNotificationSet = mediaScanner.wasRingtoneAlreadySet(Settings.System.NOTIFICATION_SOUND);
        mediaScanner.mDefaultAlarmSet = mediaScanner.wasRingtoneAlreadySet(Settings.System.ALARM_ALERT);
        Uri.Builder builder3 = mediaScanner.mFilesUri.buildUpon();
        builder3.appendQueryParameter(MediaStore.PARAM_DELETE_DATA, "false");
        MediaBulkDeleter deleter = new MediaBulkDeleter(mediaScanner.mMediaProvider, builder3.build());
        if (prescanFiles) {
            try {
                Uri limitUri = mediaScanner.mFilesUri.buildUpon().appendQueryParameter("limit", SQL_QUERY_LIMIT).build();
                deleteFilesIfPossible();
                int count2 = 0;
                long totalAccessTime2 = 0;
                long queryTime3 = 0;
                Cursor c4 = null;
                long lastId = Long.MIN_VALUE;
                while (true) {
                    try {
                        selectionArgs[0] = path + lastId;
                        if (c4 != null) {
                            try {
                                c4.close();
                                c4 = null;
                            } catch (Throwable th2) {
                                th = th2;
                                c = c4;
                                if (c != null) {
                                }
                                deleter.flush();
                                throw th;
                            }
                        }
                        start = System.currentTimeMillis();
                        builder = builder3;
                    } catch (Throwable th3) {
                        th = th3;
                        c = c4;
                        if (c != null) {
                        }
                        deleter.flush();
                        throw th;
                    }
                    try {
                        c2 = mediaScanner.mMediaProvider.query(limitUri, FILES_PRESCAN_PROJECTION, where, selectionArgs, "_id", null);
                        try {
                            long query = System.currentTimeMillis();
                            queryTime = queryTime3 + (query - start);
                            if (c2 == null) {
                                break;
                            }
                            try {
                                if (c2.getCount() == 0) {
                                    break;
                                }
                                String where3 = where;
                                int count3 = count2;
                                long accessTime = 0;
                                long filesNum = 0;
                                long existFileNum = 0;
                                long lastId2 = lastId;
                                while (c2.moveToNext()) {
                                    try {
                                        long rowId3 = c2.getLong(0);
                                        String path2 = c2.getString(1);
                                        int format = c2.getInt(2);
                                        long lastModified = c2.getLong(3);
                                        int mediaType = c2.getInt(4);
                                        lastId2 = rowId3;
                                        if (path2 == null || !path2.startsWith("/")) {
                                            c = c2;
                                            builder2 = builder;
                                            where2 = where3;
                                        } else {
                                            boolean exists = false;
                                            try {
                                                long accessStart = System.currentTimeMillis();
                                                exists = Os.access(path2, OsConstants.F_OK);
                                                accessTime += System.currentTimeMillis() - accessStart;
                                                filesNum++;
                                                if (exists) {
                                                    existFileNum++;
                                                }
                                            } catch (ErrnoException e) {
                                            } catch (Throwable th4) {
                                                th = th4;
                                                c = c2;
                                                if (c != null) {
                                                }
                                                deleter.flush();
                                                throw th;
                                            }
                                            if (exists || MtpConstants.isAbstractObject(format)) {
                                                c = c2;
                                                builder2 = builder;
                                                where2 = where3;
                                                rowId2 = rowId3;
                                                rowId = accessTime;
                                            } else if (!MediaFile.isPlayListMimeType(MediaFile.getMimeTypeForFile(path2))) {
                                                builder2 = builder;
                                                where2 = where3;
                                                rowId2 = rowId3;
                                                try {
                                                    deleter.delete(rowId2);
                                                    rowId = accessTime;
                                                    if (path2.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                                                        deleter.flush();
                                                        c = c2;
                                                        mediaScanner.mMediaProvider.call(MediaStore.UNHIDE_CALL, new File(path2).getParent(), null);
                                                    } else {
                                                        c = c2;
                                                    }
                                                } catch (Throwable th5) {
                                                    th = th5;
                                                    if (c != null) {
                                                    }
                                                    deleter.flush();
                                                    throw th;
                                                }
                                            } else {
                                                c = c2;
                                                builder2 = builder;
                                                where2 = where3;
                                                rowId2 = rowId3;
                                                rowId = accessTime;
                                            }
                                            if (!mediaScanner.mNeedFilter || !HwFrameworkFactory.getHwMediaScannerManager().isAudioFilterFile(path2)) {
                                                if (count3 < mediaScanner.mMaxFileCacheEntrySize) {
                                                    mediaScanner.mFileCache.put(path2, new FileEntry(rowId2, path2, lastModified, format, mediaType));
                                                }
                                                count3++;
                                                accessTime = rowId;
                                            } else {
                                                deleter.delete(rowId2);
                                                path = path;
                                                builder = builder2;
                                                accessTime = rowId;
                                                where3 = where2;
                                                c2 = c;
                                            }
                                        }
                                        path = path;
                                        builder = builder2;
                                        where3 = where2;
                                        c2 = c;
                                    } catch (Throwable th6) {
                                        th = th6;
                                        c = c2;
                                        if (c != null) {
                                        }
                                        deleter.flush();
                                        throw th;
                                    }
                                }
                                c = c2;
                                long end = System.currentTimeMillis();
                                totalAccessTime2 += accessTime;
                                if (end - start > 15000) {
                                    try {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("prescan time: ");
                                        count = count3;
                                        sb.append(end - start);
                                        sb.append(",query: ");
                                        sb.append(query - start);
                                        sb.append(",access:");
                                        sb.append(accessTime);
                                        sb.append(",filesNum: ");
                                        sb.append(filesNum);
                                        sb.append(",existFileNum: ");
                                        sb.append(existFileNum);
                                        Log.d(TAG, sb.toString());
                                    } catch (Throwable th7) {
                                        th = th7;
                                    }
                                } else {
                                    count = count3;
                                }
                                mediaScanner = this;
                                fileCount = count3;
                                count2 = count;
                                queryTime3 = queryTime;
                                lastId = lastId2;
                                path = path;
                                builder3 = builder;
                                where = where3;
                                c4 = c;
                            } catch (Throwable th8) {
                                th = th8;
                                c = c2;
                                if (c != null) {
                                }
                                deleter.flush();
                                throw th;
                            }
                        } catch (Throwable th9) {
                            th = th9;
                            c = c2;
                            if (c != null) {
                            }
                            deleter.flush();
                            throw th;
                        }
                    } catch (Throwable th10) {
                        th = th10;
                        c = c4;
                        if (c != null) {
                            c.close();
                        }
                        deleter.flush();
                        throw th;
                    }
                }
                c3 = c2;
                queryTime2 = queryTime;
                totalAccessTime = totalAccessTime2;
            } catch (Throwable th11) {
                th = th11;
                c = null;
                if (c != null) {
                }
                deleter.flush();
                throw th;
            }
        }
        if (c3 != null) {
            c3.close();
        }
        deleter.flush();
        this.mOriginalCount = 0;
        Cursor c5 = this.mMediaProvider.query(this.mImagesUri, ID_PROJECTION, null, null, null, null);
        if (c5 != null) {
            this.mOriginalCount = c5.getCount();
            c5.close();
        }
        Log.d(TAG, "prescan end cacheSize: " + this.mFileCache.size() + ",query: " + queryTime2 + ",totalAccessTime: " + totalAccessTime + ",count: " + fileCount);
    }

    /* access modifiers changed from: package-private */
    public static class MediaBulkDeleter {
        final Uri mBaseUri;
        final ContentProviderClient mProvider;
        ArrayList<String> whereArgs = new ArrayList<>(100);
        StringBuilder whereClause = new StringBuilder();

        public MediaBulkDeleter(ContentProviderClient provider, Uri baseUri) {
            this.mProvider = provider;
            this.mBaseUri = baseUri;
        }

        public void delete(long id) throws RemoteException {
            if (this.whereClause.length() != 0) {
                this.whereClause.append(SmsManager.REGEX_PREFIX_DELIMITER);
            }
            this.whereClause.append("?");
            ArrayList<String> arrayList = this.whereArgs;
            arrayList.add("" + id);
            if (this.whereArgs.size() > 100) {
                flush();
            }
        }

        public void flush() throws RemoteException {
            int size = this.whereArgs.size();
            if (size > 0) {
                String[] foo = (String[]) this.whereArgs.toArray(new String[size]);
                ContentProviderClient contentProviderClient = this.mProvider;
                Uri uri = this.mBaseUri;
                contentProviderClient.delete(uri, "_id IN (" + this.whereClause.toString() + ")", foo);
                this.whereClause.setLength(0);
                this.whereArgs.clear();
            }
        }
    }

    @UnsupportedAppUsage
    public void postscan(String[] directories) throws RemoteException {
        if (this.mProcessPlaylists) {
            processPlayLists();
        }
        HwFrameworkFactory.getHwMediaScannerManager().pruneDeadThumbnailsFolder();
        this.mPlayLists.clear();
    }

    private void releaseResources() {
        DrmManagerClient drmManagerClient = this.mDrmManagerClient;
        if (drmManagerClient != null) {
            drmManagerClient.close();
            this.mDrmManagerClient = null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x00ff, code lost:
        if (r0 != null) goto L_0x0130;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0110, code lost:
        if (r0 == null) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x011f, code lost:
        if (r0 == null) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x012e, code lost:
        if (r0 == null) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0130, code lost:
        r0.clear();
        r21.mFileCache = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0136, code lost:
        r21.mSkipExternelQuery = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0139, code lost:
        return;
     */
    public void scanDirectories(String[] directories) {
        HashMap<String, FileEntry> hashMap;
        try {
            long start = System.currentTimeMillis();
            prescan(null, true);
            System.currentTimeMillis();
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, 500);
            Log.d(TAG, "delete nomedia File when scanDirectories");
            deleteNomediaFile(StorageManager.getVolumeList(UserHandle.myUserId(), 256));
            for (int i = 0; i < directories.length; i++) {
                setExteLen(getRootDirLength(directories[i]));
                sCurScanDIR = directories[i];
                setStorageIdForCurScanDIR(directories[i]);
                processDirectory(directories[i], this.mClient);
            }
            if (this.mMediaTypeConflict) {
                Log.i(TAG, "find some files's media type did not match with database");
            }
            Log.d(TAG, "scanDirectories total files number is " + this.mScanDirectoryFilesNum + ",audio: " + this.mAudioFileNumber + ",video: " + this.mVideoFileNumber + ",image: " + this.mImageFileNumber + ",mScanFileTime: " + this.mScanFileTime + ",inserterTime : " + this.mMediaInserter.getInsertTime() + ",make entry: " + this.mMakeEntryFor);
            if (ENABLE_PROFILE_SCANNER) {
                Log.d(TAG, "process av: " + this.mProcessAVFileTime + ",process image: " + this.mProcessImageFileTime);
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
            System.currentTimeMillis();
            postscan(directories);
            HwMediaMonitorManager.writeBigData((int) HwMediaMonitorUtils.BD_MEDIA_SCANNING, HwMediaMonitorUtils.M_SCANNING_COMPELETED_PERIOD, (int) ((System.currentTimeMillis() - start) / 1000), 0);
            releaseResources();
            hashMap = this.mFileCache;
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
            releaseResources();
            hashMap = this.mFileCache;
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
            releaseResources();
            hashMap = this.mFileCache;
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
            releaseResources();
            hashMap = this.mFileCache;
        } catch (Throwable th) {
            releaseResources();
            HashMap<String, FileEntry> hashMap2 = this.mFileCache;
            if (hashMap2 != null) {
                hashMap2.clear();
                this.mFileCache = null;
            }
            this.mSkipExternelQuery = false;
            throw th;
        }
    }

    public void scanCustomDirectories(String[] directories, String volumeName, String[] whiteList, String[] blackList) {
        long start = System.currentTimeMillis();
        this.mMediaInserter = new MediaInserter(this.mMediaProvider, 500);
        HwFrameworkFactory.getHwMediaScannerManager().setMediaInserter(this.mMediaInserter);
        Log.d(TAG, "delete nomedia File when scanCustomDirectories");
        deleteNomediaFile(StorageManager.getVolumeList(UserHandle.myUserId(), 256));
        HwFrameworkFactory.getHwMediaScannerManager().scanCustomDirectories(this, this.mClient, directories, whiteList, blackList);
        clearSkipCustomDirectory();
        if (this.mMediaTypeConflict) {
            Log.i(TAG, "find some files's media type did not match with database");
        }
        Log.d(TAG, "scanDirectories total files number is " + this.mScanDirectoryFilesNum + ",audio: " + this.mAudioFileNumber + ",video: " + this.mVideoFileNumber + ",image: " + this.mImageFileNumber + ",mScanFileTime: " + this.mScanFileTime + ",inserterTime : " + this.mMediaInserter.getInsertTime() + ",make entry: " + this.mMakeEntryFor);
        if (ENABLE_PROFILE_SCANNER) {
            Log.d(TAG, "process av: " + this.mProcessAVFileTime + ",process image: " + this.mProcessImageFileTime);
        }
        HashMap<String, FileEntry> hashMap = this.mFileCache;
        if (hashMap != null) {
            hashMap.clear();
            this.mFileCache = null;
        }
        HwFrameworkFactory.getHwMediaScannerManager().setMediaInserter(null);
        this.mMediaInserter = null;
        this.mSkipExternelQuery = false;
        HwMediaMonitorManager.writeBigData((int) HwMediaMonitorUtils.BD_MEDIA_SCANNING, HwMediaMonitorUtils.M_SCANNING_COMPELETED_PERIOD, (int) ((System.currentTimeMillis() - start) / 1000), 0);
    }

    public int getRootDirLength(String path) {
        if (path == null) {
            return 0;
        }
        for (StorageVolume storageVolume : StorageManager.getVolumeList(UserHandle.myUserId(), 256)) {
            String rootPath = storageVolume.getPath();
            if (path.startsWith(rootPath)) {
                return rootPath.length();
            }
        }
        return 0;
    }

    @UnsupportedAppUsage
    public Uri scanSingleFile(String path, String mimeType) {
        try {
            prescan(path, true);
            this.mBlackListFlag = false;
            if (isBlackListPath(path, getRootDirLength(path))) {
                this.mBlackListFlag = true;
            }
            File file = new File(path);
            if (file.exists()) {
                if (file.canRead()) {
                    Log.d(TAG, "delete nomedia File when scanSingleFile");
                    deleteNomediaFile(StorageManager.getVolumeList(UserHandle.myUserId(), 256));
                    long lastModifiedSeconds = file.lastModified() / 1000;
                    setStorageIdForCurScanDIR(path);
                    Uri doScanFile = this.mClient.doScanFile(path, mimeType, lastModifiedSeconds, file.length(), false, true, isNoMediaPath(path));
                    releaseResources();
                    return doScanFile;
                }
            }
            Log.d(TAG, "scanSingleFile doesn't exists or can't read");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
            return null;
        } finally {
            releaseResources();
        }
    }

    private void deleteFilesIfPossible() {
        Cursor c = null;
        try {
            c = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, "_data is null and media_type != 4", null, null, null);
            if (c != null && c.getCount() > 0) {
                this.mMediaProvider.delete(this.mFilesUri, "_data is null and media_type != 4", null);
            }
            if (c == null) {
                return;
            }
        } catch (RemoteException e) {
            Log.d(TAG, "deleteFilesIfPossible catch RemoteException ");
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

    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00f6, code lost:
        r0 = 100;
     */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00fb  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0122  */
    /* JADX WARNING: Removed duplicated region for block: B:85:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:87:? A[RETURN, SYNTHETIC] */
    public void updateExifFile() throws RemoteException {
        Throwable th;
        int i;
        int count;
        Cursor c;
        String where = "_id>? and cam_exif_flag is null";
        String[] selectionArgs = {WifiEnterpriseConfig.ENGINE_DISABLE};
        Uri limitUri = this.mImagesUri.buildUpon().appendQueryParameter("limit", SQL_QUERY_LIMIT).build();
        int count2 = 0;
        long lastId = Long.MIN_VALUE;
        Cursor c2 = null;
        while (true) {
            try {
                if (count2 == i) {
                    try {
                        Log.d(TAG, "SQL query exceed the limit 10 !");
                    } catch (SQLException e) {
                    }
                }
                if (c2 == null) {
                    return;
                }
            } catch (SQLException e2) {
                try {
                    Log.e(TAG, "updateExifFile SQLException ! ");
                    if (c2 == null) {
                    }
                    c2.close();
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Throwable th3) {
                th = th3;
                if (c2 != null) {
                }
                throw th;
            }
            if (!sPowerConnect || count2 >= 100) {
                break;
            }
            count = count2 + 1;
            try {
                selectionArgs[0] = "" + lastId;
                if (c2 != null) {
                    try {
                        c2.close();
                        c = null;
                    } catch (SQLException e3) {
                        count2 = count;
                        Log.e(TAG, "updateExifFile SQLException ! ");
                        if (c2 == null) {
                        }
                        c2.close();
                    } catch (Throwable th4) {
                        th = th4;
                        if (c2 != null) {
                        }
                        throw th;
                    }
                } else {
                    c = c2;
                }
                try {
                    i = 100;
                    try {
                        c2 = this.mMediaProvider.query(limitUri, FILES_PRESCAN_PROJECTION_MEDIA, where, selectionArgs, "_id", null);
                        if (c2 == null) {
                            break;
                        }
                        try {
                            if (c2.getCount() == 0) {
                                break;
                            }
                            while (sPowerConnect && c2.moveToNext()) {
                                long rowId = c2.getLong(0);
                                lastId = rowId;
                                ContentValues values = new ContentValues();
                                values.put("cam_exif_flag", "1");
                                Uri updateRowId = ContentUris.withAppendedId(this.mImagesUri, rowId);
                                ExifInterface exif = null;
                                try {
                                    exif = new ExifInterface(c2.getString(1));
                                } catch (IOException e4) {
                                    Log.e(TAG, "new ExifInterface Exception !");
                                }
                                HwFrameworkFactory.getHwMediaScannerManager().scanHwMakerNote(values, exif);
                                this.mMediaProvider.update(updateRowId, values, null, null);
                            }
                            count2 = count;
                            where = where;
                        } catch (SQLException e5) {
                            count2 = count;
                            Log.e(TAG, "updateExifFile SQLException ! ");
                            if (c2 == null) {
                            }
                            c2.close();
                        } catch (Throwable th5) {
                            th = th5;
                            if (c2 != null) {
                            }
                            throw th;
                        }
                    } catch (SQLException e6) {
                        count2 = count;
                        c2 = c;
                        Log.e(TAG, "updateExifFile SQLException ! ");
                        if (c2 == null) {
                        }
                        c2.close();
                    } catch (Throwable th6) {
                        th = th6;
                        c2 = c;
                        if (c2 != null) {
                        }
                        throw th;
                    }
                } catch (SQLException e7) {
                    count2 = count;
                    c2 = c;
                    Log.e(TAG, "updateExifFile SQLException ! ");
                    if (c2 == null) {
                    }
                    c2.close();
                } catch (Throwable th7) {
                    th = th7;
                    c2 = c;
                    if (c2 != null) {
                    }
                    throw th;
                }
            } catch (SQLException e8) {
                count2 = count;
                Log.e(TAG, "updateExifFile SQLException ! ");
                if (c2 == null) {
                    return;
                }
                c2.close();
            } catch (Throwable th8) {
                th = th8;
                if (c2 != null) {
                    c2.close();
                }
                throw th;
            }
            c2.close();
        }
        count2 = count;
        if (count2 == i) {
        }
        if (c2 == null) {
        }
        c2.close();
    }

    /* access modifiers changed from: private */
    public static boolean isNoMediaFile(String path) {
        int lastSlash;
        if (!new File(path).isDirectory() && (lastSlash = path.lastIndexOf(47)) >= 0 && lastSlash + 2 < path.length()) {
            if (path.regionMatches(lastSlash + 1, "._", 0, 2)) {
                return true;
            }
            if (path.regionMatches(true, path.length() - 4, ".jpg", 0, 4)) {
                if (path.regionMatches(true, lastSlash + 1, "AlbumArt_{", 0, 10) || path.regionMatches(true, lastSlash + 1, "AlbumArt.", 0, 9)) {
                    return true;
                }
                int length = (path.length() - lastSlash) - 1;
                if ((length == 17 && path.regionMatches(true, lastSlash + 1, "AlbumArtSmall", 0, 13)) || (length == 10 && path.regionMatches(true, lastSlash + 1, "Folder", 0, 6))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void clearMediaPathCache(boolean clearMediaPaths, boolean clearNoMediaPaths) {
        synchronized (MediaScanner.class) {
            if (clearMediaPaths) {
                try {
                    mMediaPaths.clear();
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (clearNoMediaPaths) {
                mNoMediaPaths.clear();
            }
        }
    }

    @UnsupportedAppUsage
    public static boolean isNoMediaPath(String path) {
        if (path == null) {
            return false;
        }
        if (path.indexOf("/.") >= 0) {
            return true;
        }
        int firstSlash = path.lastIndexOf(47);
        if (firstSlash <= 0) {
            return false;
        }
        String parent = path.substring(0, firstSlash);
        synchronized (MediaScanner.class) {
            if (mNoMediaPaths.containsKey(parent)) {
                return true;
            }
            if (!mMediaPaths.containsKey(parent)) {
                int offset = 1;
                while (offset >= 0) {
                    int slashIndex = path.indexOf(47, offset);
                    if (slashIndex > offset) {
                        slashIndex++;
                        if (new File(path.substring(0, slashIndex) + MediaStore.MEDIA_IGNORE_FILENAME).exists()) {
                            mNoMediaPaths.put(parent, "");
                            return true;
                        }
                    }
                    offset = slashIndex;
                }
                mMediaPaths.put(parent, "");
            }
            return isNoMediaFile(path);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ee, code lost:
        if (r18 != null) goto L_0x0106;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0104, code lost:
        if (r18 == null) goto L_0x0109;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0106, code lost:
        r18.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0109, code lost:
        releaseResources();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x010d, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0113  */
    public void scanMtpFile(String path, int objectHandle, int format) {
        int i;
        Throwable th;
        String str;
        RemoteException e;
        String mimeType = MediaFile.getMimeType(path, format);
        File file = new File(path);
        long lastModifiedSeconds = file.lastModified() / 1000;
        if (MediaFile.isAudioMimeType(mimeType) || MediaFile.isVideoMimeType(mimeType) || MediaFile.isImageMimeType(mimeType) || MediaFile.isPlayListMimeType(mimeType) || MediaFile.isDrmMimeType(mimeType)) {
            this.mMtpObjectHandle = objectHandle;
            Cursor fileList = null;
            try {
                if (MediaFile.isPlayListMimeType(mimeType)) {
                    prescan(null, true);
                    FileEntry entry = this.mFileCache.remove(path);
                    if (entry == null) {
                        entry = makeEntryFor(path);
                    }
                    if (entry != null) {
                        Cursor fileList2 = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, null, null, null, null);
                        try {
                            processPlayList(entry, fileList2);
                            fileList = fileList2;
                        } catch (RemoteException e2) {
                            e = e2;
                            fileList = fileList2;
                            i = 0;
                            str = TAG;
                            try {
                                Log.e(str, "RemoteException in MediaScanner.scanFile()", e);
                                this.mMtpObjectHandle = i;
                            } catch (Throwable th2) {
                                th = th2;
                                this.mMtpObjectHandle = i;
                                if (fileList != null) {
                                }
                                releaseResources();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            fileList = fileList2;
                            i = 0;
                            this.mMtpObjectHandle = i;
                            if (fileList != null) {
                                fileList.close();
                            }
                            releaseResources();
                            throw th;
                        }
                    }
                    i = 0;
                } else {
                    prescan(path, false);
                    MyMediaScannerClient myMediaScannerClient = this.mClient;
                    long length = file.length();
                    boolean z = format == 12289;
                    boolean isNoMediaPath = isNoMediaPath(path);
                    i = 0;
                    str = TAG;
                    try {
                        myMediaScannerClient.doScanFile(path, mimeType, lastModifiedSeconds, length, z, true, isNoMediaPath);
                    } catch (RemoteException e3) {
                        e = e3;
                        Log.e(str, "RemoteException in MediaScanner.scanFile()", e);
                        this.mMtpObjectHandle = i;
                    }
                }
                this.mMtpObjectHandle = i;
            } catch (RemoteException e4) {
                e = e4;
                i = 0;
                str = TAG;
                Log.e(str, "RemoteException in MediaScanner.scanFile()", e);
                this.mMtpObjectHandle = i;
            } catch (Throwable th4) {
                th = th4;
                i = 0;
                this.mMtpObjectHandle = i;
                if (fileList != null) {
                }
                releaseResources();
                throw th;
            }
        } else {
            ContentValues values = new ContentValues();
            values.put("_size", Long.valueOf(file.length()));
            values.put("date_modified", Long.valueOf(lastModifiedSeconds));
            try {
                this.mMediaProvider.update(MediaStore.Files.getMtpObjectsUri(this.mVolumeName), values, "_id=?", new String[]{Integer.toString(objectHandle)});
            } catch (RemoteException e5) {
                Log.e(TAG, "RemoteException in scanMtpFile", e5);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public FileEntry makeEntryFor(String path) {
        Cursor c = null;
        try {
            Cursor c2 = this.mMediaProvider.query(this.mFilesFullUri, FILES_PRESCAN_PROJECTION, "_data=?", new String[]{path}, null, null);
            if (c2 != null && c2.moveToFirst()) {
                FileEntry fileEntry = new FileEntry(c2.getLong(0), path, c2.getLong(3), c2.getInt(2), c2.getInt(4));
                c2.close();
                return fileEntry;
            } else if (c2 == null) {
                return null;
            } else {
                c2.close();
                return null;
            }
        } catch (RemoteException e) {
            if (0 == 0) {
                return null;
            }
            c.close();
            return null;
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    private int matchPaths(String path1, String path2) {
        int result = 0;
        int end1 = path1.length();
        int end2 = path2.length();
        while (end1 > 0 && end2 > 0) {
            int slash1 = path1.lastIndexOf(47, end1 - 1);
            int slash2 = path2.lastIndexOf(47, end2 - 1);
            int backSlash1 = path1.lastIndexOf(92, end1 - 1);
            int backSlash2 = path2.lastIndexOf(92, end2 - 1);
            int start1 = slash1 > backSlash1 ? slash1 : backSlash1;
            int start2 = slash2 > backSlash2 ? slash2 : backSlash2;
            int start12 = start1 < 0 ? 0 : start1 + 1;
            int start22 = start2 < 0 ? 0 : start2 + 1;
            int length = end1 - start12;
            if (end2 - start22 != length || !path1.regionMatches(true, start12, path2, start22, length)) {
                break;
            }
            result++;
            end1 = start12 - 1;
            end2 = start22 - 1;
        }
        return result;
    }

    private boolean matchEntries(long rowId, String data) {
        int len = this.mPlaylistEntries.size();
        boolean done = true;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = this.mPlaylistEntries.get(i);
            if (entry.bestmatchlevel != Integer.MAX_VALUE) {
                done = false;
                if (data.equalsIgnoreCase(entry.path)) {
                    entry.bestmatchid = rowId;
                    entry.bestmatchlevel = Integer.MAX_VALUE;
                } else {
                    int matchLength = matchPaths(data, entry.path);
                    if (matchLength > entry.bestmatchlevel) {
                        entry.bestmatchid = rowId;
                        entry.bestmatchlevel = matchLength;
                    }
                }
            }
        }
        return done;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cachePlaylistEntry(String line, String playListDirectory) {
        PlaylistEntry entry = new PlaylistEntry();
        int entryLength = line.length();
        while (entryLength > 0 && Character.isWhitespace(line.charAt(entryLength - 1))) {
            entryLength--;
        }
        if (entryLength >= 3) {
            boolean fullPath = false;
            if (entryLength < line.length()) {
                line = line.substring(0, entryLength);
            }
            char ch1 = line.charAt(0);
            if (ch1 == '/' || (Character.isLetter(ch1) && line.charAt(1) == ':' && line.charAt(2) == '\\')) {
                fullPath = true;
            }
            if (!fullPath) {
                line = playListDirectory + line;
            }
            entry.path = line;
            this.mPlaylistEntries.add(entry);
        }
    }

    private void processCachedPlaylist(Cursor fileList, ContentValues values, Uri playlistUri) {
        fileList.moveToPosition(-1);
        while (fileList.moveToNext() && !matchEntries(fileList.getLong(0), fileList.getString(1))) {
        }
        int len = this.mPlaylistEntries.size();
        int index = 0;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = this.mPlaylistEntries.get(i);
            if (entry.bestmatchlevel > 0) {
                try {
                    values.clear();
                    values.put("play_order", Integer.valueOf(index));
                    values.put("audio_id", Long.valueOf(entry.bestmatchid));
                    this.mMediaProvider.insert(playlistUri, values);
                    index++;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in MediaScanner.processCachedPlaylist()", e);
                    return;
                }
            }
        }
        this.mPlaylistEntries.clear();
    }

    private void processM3uPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                this.mPlaylistEntries.clear();
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (line.length() > 0 && line.charAt(0) != '#') {
                        cachePlaylistEntry(line, playListDirectory);
                    }
                }
                processCachedPlaylist(fileList, values, uri);
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e);
                }
            }
        } catch (IOException e2) {
            Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e2);
            if (0 != 0) {
                reader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e3);
                }
            }
            throw th;
        }
    }

    private void processPlsPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        int equals;
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                this.mPlaylistEntries.clear();
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (line.startsWith("File") && (equals = line.indexOf(61)) > 0) {
                        cachePlaylistEntry(line.substring(equals + 1), playListDirectory);
                    }
                }
                processCachedPlaylist(fileList, values, uri);
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e);
                }
            }
        } catch (IOException e2) {
            Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e2);
            if (0 != 0) {
                reader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e3);
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public class WplHandler implements ElementListener {
        final ContentHandler handler;
        String playListDirectory;

        public WplHandler(String playListDirectory2, Uri uri, Cursor fileList) {
            this.playListDirectory = playListDirectory2;
            RootElement root = new RootElement("smil");
            root.getChild("body").getChild("seq").getChild(MediaStore.AUTHORITY).setElementListener(this);
            this.handler = root.getContentHandler();
        }

        @Override // android.sax.StartElementListener
        public void start(Attributes attributes) {
            String path = attributes.getValue("", "src");
            if (path != null) {
                MediaScanner.this.cachePlaylistEntry(path, this.playListDirectory);
            }
        }

        @Override // android.sax.EndElementListener
        public void end() {
        }

        /* access modifiers changed from: package-private */
        public ContentHandler getContentHandler() {
            return this.handler;
        }
    }

    private void processWplPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        FileInputStream fis = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                fis = new FileInputStream(f);
                this.mPlaylistEntries.clear();
                Xml.parse(fis, Xml.findEncodingByName("UTF-8"), new WplHandler(playListDirectory, uri, fileList).getContentHandler());
                processCachedPlaylist(fileList, values, uri);
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e);
                }
            }
        } catch (SAXException e2) {
            e2.printStackTrace();
            if (0 != 0) {
                fis.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (0 != 0) {
                fis.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e4) {
                    Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e4);
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00bd, code lost:
        if (r5.equals("application/vnd.ms-wpl") == false) goto L_0x00d4;
     */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d7  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00fc  */
    private void processPlayList(FileEntry entry, Cursor fileList) throws RemoteException {
        String name;
        Uri membersUri;
        String name2;
        String path = entry.mPath;
        ContentValues values = new ContentValues();
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0) {
            long rowId = entry.mRowId;
            String name3 = values.getAsString("name");
            if (name3 == null) {
                String name4 = values.getAsString("title");
                if (name4 == null) {
                    int lastDot = path.lastIndexOf(46);
                    if (lastDot < 0) {
                        name2 = path.substring(lastSlash + 1);
                    } else {
                        name2 = path.substring(lastSlash + 1, lastDot);
                    }
                    name = name2;
                } else {
                    name = name4;
                }
            } else {
                name = name3;
            }
            values.put("name", name);
            values.put("date_modified", Long.valueOf(entry.mLastModified));
            if (rowId == 0) {
                values.put("_data", path);
                Uri uri = this.mMediaProvider.insert(this.mPlaylistsUri, values);
                if (uri == null) {
                    Log.w(TAG, "insert error, uri is null.");
                    return;
                } else {
                    ContentUris.parseId(uri);
                    membersUri = Uri.withAppendedPath(uri, "members");
                }
            } else {
                Uri uri2 = ContentUris.withAppendedId(this.mPlaylistsUri, rowId);
                this.mMediaProvider.update(uri2, values, null, null);
                Uri membersUri2 = Uri.withAppendedPath(uri2, "members");
                this.mMediaProvider.delete(membersUri2, null, null);
                membersUri = membersUri2;
            }
            char c = 0;
            String playListDirectory = path.substring(0, lastSlash + 1);
            String mimeType = MediaFile.getMimeTypeForFile(path);
            int hashCode = mimeType.hashCode();
            if (hashCode != -1165508903) {
                if (hashCode != 264230524) {
                    if (hashCode == 1872259501) {
                    }
                } else if (mimeType.equals("audio/x-mpegurl")) {
                    c = 1;
                    if (c == 0) {
                        processWplPlayList(path, playListDirectory, membersUri, values, fileList);
                        return;
                    } else if (c == 1) {
                        processM3uPlayList(path, playListDirectory, membersUri, values, fileList);
                        return;
                    } else if (c == 2) {
                        processPlsPlayList(path, playListDirectory, membersUri, values, fileList);
                        return;
                    } else {
                        return;
                    }
                }
            } else if (mimeType.equals("audio/x-scpls")) {
                c = 2;
                if (c == 0) {
                }
            }
            c = 65535;
            if (c == 0) {
            }
        } else {
            throw new IllegalArgumentException("bad path " + path);
        }
    }

    private void processPlayLists() throws RemoteException {
        Iterator<FileEntry> iterator = this.mPlayLists.iterator();
        Cursor fileList = null;
        try {
            fileList = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, "media_type=2", null, null, null);
            while (iterator.hasNext()) {
                FileEntry entry = iterator.next();
                if (entry.mLastModifiedChanged) {
                    processPlayList(entry, fileList);
                }
            }
            if (fileList == null) {
                return;
            }
        } catch (RemoteException e) {
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                fileList.close();
            }
            throw th;
        }
        fileList.close();
    }

    public static void setStorageEject(String path) {
        if (path == null || path.equals("")) {
            Log.e(TAG, "setStorageEject path = null!");
        } else if (sCurScanDIR.startsWith(path)) {
            Log.d(TAG, "setStorageEject curscanDir is ejected storage ! ");
            setStorageEjectFlag(true);
        }
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            this.mMediaProvider.close();
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

    public void setStorageIdForCurScanDIR(String directory) {
        try {
            Bundle result = this.mMediaProvider.call(MediaStore.GET_STORAGE_ID_CALL, directory, null);
            if (result != null) {
                this.mStorageIdForCurScanDIR = result.getInt(MediaStore.Files.FileColumns.STORAGE_ID, 0);
            }
        } catch (RemoteException | UnsupportedOperationException e) {
            Log.w(TAG, "setStorageIdForCurScanDIR exception.");
        }
    }

    /* access modifiers changed from: protected */
    public void updateValues(String path, ContentValues contentValues) {
    }

    public void deleteNomediaFile(StorageVolume[] volumes) {
        if (volumes != null) {
            HwFrameworkFactory.getHwMediaScannerManager().deleteNomediaFile(volumes);
        }
    }
}
