package android.media;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.common.HwFrameworkFactory;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.drm.DrmManagerClient;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.SensorAdditionalInfo;
import android.hwtheme.HwThemeManager;
import android.media.MediaCodec;
import android.media.MediaFile;
import android.media.midi.MidiDeviceInfo;
import android.mtp.MtpConstants;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.ExternalStorageFileImpl;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.Settings;
import android.sax.ElementListener;
import android.sax.RootElement;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
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
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class MediaScanner implements AutoCloseable {
    private static final String ALARMS_DIR = "/alarms/";
    private static final int DATE_MODIFIED_PLAYLISTS_COLUMN_INDEX = 2;
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final boolean ENABLE_BULK_INSERTS = true;
    private static final int FILES_BLACKLIST_ID_COLUMN_INDEX = 0;
    private static final int FILES_BLACKLIST_MEDIA_TYPE_COLUMN_INDEX = 2;
    private static final int FILES_BLACKLIST_PATH_COLUMN_INDEX = 1;
    private static final String[] FILES_BLACKLIST_PROJECTION_MEDIA = {DownloadManager.COLUMN_ID, "_data", DownloadManager.COLUMN_MEDIA_TYPE};
    private static final int FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX = 3;
    private static final int FILES_PRESCAN_FORMAT_COLUMN_INDEX = 2;
    private static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
    private static final int FILES_PRESCAN_MEDIA_TYPE_COLUMN_INDEX = 4;
    private static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
    private static final String[] FILES_PRESCAN_PROJECTION = {DownloadManager.COLUMN_ID, "_data", "format", "date_modified", DownloadManager.COLUMN_MEDIA_TYPE};
    private static final String[] FILES_PRESCAN_PROJECTION_MEDIA = {DownloadManager.COLUMN_ID, "_data"};
    private static final String HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN = "special_file_type";
    /* access modifiers changed from: private */
    public static final String[] ID3_GENRES = {"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie", "Britpop", null, "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian", "Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "Synthpop"};
    private static final int ID_PLAYLISTS_COLUMN_INDEX = 0;
    private static final String[] ID_PROJECTION = {DownloadManager.COLUMN_ID};
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
    private static final String INTERNAL_VOLUME = "internal";
    public static final String LAST_INTERNAL_SCAN_FINGERPRINT = "lastScanFingerprint";
    private static long MAX_NOMEDIA_SIZE = 1024;
    private static final String MUSIC_DIR = "/music/";
    private static final String NOTIFICATIONS_DIR = "/notifications/";
    private static final int PATH_PLAYLISTS_COLUMN_INDEX = 1;
    private static final String[] PLAYLIST_MEMBERS_PROJECTION = {"playlist_id"};
    private static final String PODCAST_DIR = "/podcasts/";
    private static final String PRODUCT_SOUNDS_DIR = "/product/media/audio";
    private static final String RINGTONES_DIR = "/ringtones/";
    public static final String SCANNED_BUILD_PREFS_NAME = "MediaScanBuild";
    private static final int SQL_MEDIA_TYPE_BLACKLIST = 10;
    private static final int SQL_MEDIA_TYPE_IMGAGE = 1;
    private static final int SQL_QUERY_COUNT = 100;
    private static final String SQL_QUERY_LIMIT = "1000";
    private static final String SQL_VALUE_EXIF_FLAG = "1";
    private static final String SYSTEM_SOUNDS_DIR = "/system/media/audio";
    private static final String TAG = "MediaScanner";
    private static HashMap<String, String> mMediaPaths = new HashMap<>();
    private static HashMap<String, String> mNoMediaPaths = new HashMap<>();
    public static final Set sBlackList = new HashSet();
    public static String sCurScanDIR = "";
    /* access modifiers changed from: private */
    public static String sLastInternalScanFingerprint;
    private static final String[] sNomediaFilepath = {"/.nomedia", "/DCIM/.nomedia", "/DCIM/Camera/.nomedia", "/Pictures/.nomedia", "/Pictures/Screenshots/.nomedia", "/tencent/.nomedia", "/tencent/MicroMsg/.nomedia", "/tencent/MicroMsg/Weixin/.nomedia", "/tencent/QQ_Images/.nomedia"};
    public static boolean sPowerConnect = true;
    private final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    /* access modifiers changed from: private */
    public final Uri mAudioUri;
    /* access modifiers changed from: private */
    public final BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();
    /* access modifiers changed from: private */
    public boolean mBlackListFlag = false;
    private final MyMediaScannerClient mClient = new MyMediaScannerClient();
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public String mDefaultAlarmAlertFilename;
    /* access modifiers changed from: private */
    public boolean mDefaultAlarmSet;
    /* access modifiers changed from: private */
    public String mDefaultNotificationFilename;
    /* access modifiers changed from: private */
    public boolean mDefaultNotificationSet;
    /* access modifiers changed from: private */
    public String mDefaultRingtoneFilename;
    /* access modifiers changed from: private */
    public boolean mDefaultRingtoneSet;
    /* access modifiers changed from: private */
    public DrmManagerClient mDrmManagerClient = null;
    /* access modifiers changed from: private */
    public String mExtStroagePath;
    /* access modifiers changed from: private */
    public HashMap<String, FileEntry> mFileCache;
    /* access modifiers changed from: private */
    public final Uri mFilesUri;
    private final Uri mFilesUriNoNotify;
    /* access modifiers changed from: private */
    public final Uri mImagesUri;
    /* access modifiers changed from: private */
    public boolean mIsImageType = false;
    private boolean mIsPrescanFiles = true;
    private int mMaxFileCacheEntrySize = 40000;
    /* access modifiers changed from: private */
    public MediaInserter mMediaInserter;
    /* access modifiers changed from: private */
    public ContentProviderClient mMediaProvider;
    /* access modifiers changed from: private */
    public boolean mMediaTypeConflict;
    /* access modifiers changed from: private */
    public int mMtpObjectHandle;
    private long mNativeContext;
    /* access modifiers changed from: private */
    public boolean mNeedFilter;
    private int mOriginalCount;
    private final String mPackageName;
    /* access modifiers changed from: private */
    public final ArrayList<FileEntry> mPlayLists = new ArrayList<>();
    private final ArrayList<PlaylistEntry> mPlaylistEntries = new ArrayList<>();
    private final Uri mPlaylistsUri;
    /* access modifiers changed from: private */
    public final boolean mProcessGenres;
    /* access modifiers changed from: private */
    public final boolean mProcessPlaylists;
    private long mScanDirectoryFilesNum = 0;
    /* access modifiers changed from: private */
    public boolean mSkipExternelQuery = false;
    /* access modifiers changed from: private */
    public int mStorageIdForCurScanDIR;
    /* access modifiers changed from: private */
    public final Uri mVideoUri;
    private final String mVolumeName;

    private static class FileEntry {
        int mFormat;
        long mLastModified;
        boolean mLastModifiedChanged = false;
        int mMediaType;
        String mPath;
        long mRowId;

        FileEntry(long rowId, String path, long lastModified, int format, int mediaType) {
            this.mRowId = rowId;
            this.mPath = path;
            this.mLastModified = lastModified;
            this.mFormat = format;
            this.mMediaType = mediaType;
        }

        public String toString() {
            return this.mPath + " mRowId: " + this.mRowId;
        }
    }

    static class MediaBulkDeleter {
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
                this.whereClause.append(",");
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
                ArrayList<String> arrayList = this.whereArgs;
                ContentProviderClient contentProviderClient = this.mProvider;
                Uri uri = this.mBaseUri;
                int delete = contentProviderClient.delete(uri, "_id IN (" + this.whereClause.toString() + ")", (String[]) arrayList.toArray(new String[size]));
                this.whereClause.setLength(0);
                this.whereArgs.clear();
            }
        }
    }

    public class MyMediaScannerClient implements MediaScannerClient {
        private static final int ALBUM = 1;
        private static final int ARTIST = 2;
        private static final int TITLE = 3;
        private static final long Time_1970 = 2082844800;
        private String mAlbum;
        private String mAlbumArtist;
        private String mArtist;
        private int mCompilation;
        private String mComposer;
        private long mDate;
        private final SimpleDateFormat mDateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        private int mDuration;
        private long mFileSize;
        private int mFileType;
        private String mGenre;
        private int mHeight;
        private boolean mIsAlbumMessy;
        private boolean mIsArtistMessy;
        private boolean mIsDrm;
        private boolean mIsTitleMessy;
        private long mLastModified;
        private String mMimeType;
        private boolean mNoMedia;
        private String mPath;
        private boolean mScanSuccess;
        private String mTitle;
        private int mTrack;
        private int mWidth;
        private String mWriter;
        private int mYear;

        public MyMediaScannerClient() {
            this.mDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public FileEntry beginFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            boolean z;
            boolean noMedia2;
            String str = path;
            String str2 = mimeType;
            long j = lastModified;
            this.mMimeType = str2;
            this.mFileType = 0;
            this.mFileSize = fileSize;
            this.mIsDrm = false;
            if (MediaScanner.this.mNeedFilter && HwFrameworkFactory.getHwMediaScannerManager().isAudioFilterFile(str)) {
                return null;
            }
            if (str != null && (str.endsWith(".isma") || str.endsWith(".ismv"))) {
                this.mIsDrm = true;
            }
            this.mScanSuccess = true;
            if (!isDirectory) {
                if (noMedia || !MediaScanner.isNoMediaFile(path)) {
                    noMedia2 = noMedia;
                } else {
                    noMedia2 = true;
                }
                this.mNoMedia = noMedia2;
                if (str2 != null) {
                    this.mFileType = MediaFile.getFileTypeForMimeType(mimeType);
                }
                if (this.mFileType == 0) {
                    MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
                    if (mediaFileType != null) {
                        this.mFileType = mediaFileType.fileType;
                        if (this.mMimeType == null) {
                            this.mMimeType = mediaFileType.mimeType;
                        }
                    }
                }
                if (MediaScanner.this.isDrmEnabled() && MediaFile.isDrmFileType(this.mFileType)) {
                    this.mFileType = getFileTypeFromDrm(path);
                }
                boolean z2 = noMedia2;
            } else {
                boolean z3 = noMedia;
            }
            String key = str;
            FileEntry entry = (FileEntry) MediaScanner.this.mFileCache.remove(key);
            if (entry == null && (!MediaScanner.this.mSkipExternelQuery || !str.startsWith(MediaScanner.this.mExtStroagePath))) {
                entry = MediaScanner.this.makeEntryFor(str);
            }
            FileEntry entry2 = entry;
            long delta = entry2 != null ? j - entry2.mLastModified : 0;
            boolean wasModified = delta > 1 || delta < -1;
            if (entry2 == null || wasModified) {
                if (wasModified) {
                    entry2.mLastModified = j;
                    z = true;
                    String str3 = key;
                } else {
                    FileEntry fileEntry = entry2;
                    z = true;
                    String str4 = key;
                    FileEntry entry3 = new FileEntry(0, str, j, isDirectory ? 12289 : 0, this.mFileType);
                    entry2 = entry3;
                }
                entry2.mLastModifiedChanged = z;
            } else {
                String str5 = key;
            }
            if (!MediaScanner.this.mProcessPlaylists || !MediaFile.isPlayListFileType(this.mFileType)) {
                this.mArtist = null;
                this.mAlbumArtist = null;
                this.mAlbum = null;
                this.mTitle = null;
                this.mComposer = null;
                this.mGenre = null;
                this.mTrack = 0;
                this.mYear = 0;
                this.mDuration = 0;
                this.mPath = str;
                this.mDate = 0;
                this.mLastModified = j;
                this.mWriter = null;
                this.mCompilation = 0;
                this.mWidth = 0;
                this.mHeight = 0;
                this.mIsAlbumMessy = false;
                this.mIsArtistMessy = false;
                this.mIsTitleMessy = false;
                return entry2;
            }
            MediaScanner.this.mPlayLists.add(entry2);
            return null;
        }

        public void setBlackListFlag(boolean flag) {
            boolean unused = MediaScanner.this.mBlackListFlag = true;
        }

        public void scanFile(String path, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            MediaScanner.access$904(MediaScanner.this);
            doScanFile(path, null, lastModified, fileSize, isDirectory, false, noMedia);
        }

        /* JADX WARNING: Removed duplicated region for block: B:102:0x01cb A[Catch:{ RemoteException -> 0x021c, NullPointerException -> 0x021a }] */
        /* JADX WARNING: Removed duplicated region for block: B:105:0x01de A[Catch:{ RemoteException -> 0x021c, NullPointerException -> 0x021a }] */
        /* JADX WARNING: Removed duplicated region for block: B:108:0x01f1 A[Catch:{ RemoteException -> 0x021c, NullPointerException -> 0x021a }] */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x00e7 A[SYNTHETIC, Splitter:B:47:0x00e7] */
        /* JADX WARNING: Removed duplicated region for block: B:86:0x0185 A[Catch:{ RemoteException -> 0x021c, NullPointerException -> 0x021a }] */
        /* JADX WARNING: Removed duplicated region for block: B:91:0x0197 A[Catch:{ RemoteException -> 0x021c, NullPointerException -> 0x021a }] */
        public Uri doScanFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean scanAlways, boolean noMedia) {
            boolean scanAlways2;
            boolean music;
            boolean isaudio;
            boolean isimage;
            boolean scanAlways3;
            String path2 = path;
            if (path2 != null && (path.toUpperCase().endsWith(".HEIC") || path.toUpperCase().endsWith(".HEIF"))) {
                HwMediaMonitorManager.writeBigData(HwMediaMonitorUtils.BD_MEDIA_HEIF, HwMediaMonitorUtils.M_HEIF_SCAN);
            }
            Uri result = null;
            try {
                FileEntry entry = beginFile(path2, mimeType, lastModified, fileSize, isDirectory, noMedia);
                if (entry == null) {
                    return null;
                }
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    entry.mRowId = 0;
                }
                if (entry.mPath != null) {
                    if ((!MediaScanner.this.mDefaultNotificationSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename)) || ((!MediaScanner.this.mDefaultRingtoneSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultRingtoneFilename)) || (!MediaScanner.this.mDefaultAlarmSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultAlarmAlertFilename)))) {
                        Log.w(MediaScanner.TAG, "forcing rescan , since ringtone setting didn't finish");
                        scanAlways3 = true;
                    } else if (MediaScanner.isSystemSoundWithMetadata(entry.mPath) && !Build.FINGERPRINT.equals(MediaScanner.sLastInternalScanFingerprint)) {
                        if (MediaScanner.DEBUG) {
                            Log.i(MediaScanner.TAG, "forcing rescan of " + entry.mPath + " since build fingerprint changed");
                        }
                        scanAlways3 = true;
                    } else if (this.mFileType == 40 && entry.mMediaType != 1) {
                        boolean unused = MediaScanner.this.mMediaTypeConflict = true;
                        scanAlways3 = true;
                    }
                    scanAlways2 = scanAlways3;
                    if (entry != null) {
                        try {
                            if (entry.mLastModifiedChanged || scanAlways2 || this.mIsDrm) {
                                if (noMedia) {
                                    result = endFile(entry, false, false, false, false, false);
                                } else {
                                    String lowpath = path2.toLowerCase(Locale.ROOT);
                                    boolean ringtones = lowpath.indexOf(MediaScanner.RINGTONES_DIR) > 0;
                                    boolean notifications = lowpath.indexOf(MediaScanner.NOTIFICATIONS_DIR) > 0;
                                    boolean alarms = lowpath.indexOf(MediaScanner.ALARMS_DIR) > 0;
                                    boolean podcasts = lowpath.indexOf(MediaScanner.PODCAST_DIR) > 0;
                                    if (lowpath.indexOf(MediaScanner.MUSIC_DIR) <= 0) {
                                        if (ringtones || notifications || alarms || podcasts) {
                                            music = false;
                                            boolean ringtones2 = ringtones | HwThemeManager.isTRingtones(lowpath);
                                            boolean notifications2 = notifications | HwThemeManager.isTNotifications(lowpath);
                                            boolean alarms2 = alarms | HwThemeManager.isTAlarms(lowpath);
                                            isaudio = MediaFile.isAudioFileType(this.mFileType);
                                            boolean isvideo = MediaFile.isVideoFileType(this.mFileType);
                                            isimage = MediaFile.isImageFileType(this.mFileType);
                                            boolean unused2 = MediaScanner.this.mIsImageType = isimage;
                                            if (isaudio || isvideo || isimage) {
                                                path2 = Environment.maybeTranslateEmulatedPathToInternal(new File(path2)).getAbsolutePath();
                                            }
                                            if (!isaudio) {
                                                if (!isvideo) {
                                                    String str = mimeType;
                                                    if (isimage) {
                                                        this.mScanSuccess = processImageFile(path2);
                                                    }
                                                    boolean ringtones3 = this.mScanSuccess & ringtones2;
                                                    boolean notifications3 = notifications2 & this.mScanSuccess;
                                                    boolean alarms3 = alarms2 & this.mScanSuccess;
                                                    boolean podcasts2 = podcasts & this.mScanSuccess;
                                                    boolean music2 = music & this.mScanSuccess;
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
                                                    boolean z = isimage;
                                                    result = endFile(entry, ringtones3, notifications3, alarms3, music2, podcasts2);
                                                }
                                            }
                                            this.mScanSuccess = MediaScanner.this.processFile(path2, mimeType, this);
                                            if (isimage) {
                                            }
                                            boolean ringtones32 = this.mScanSuccess & ringtones2;
                                            boolean notifications32 = notifications2 & this.mScanSuccess;
                                            boolean alarms32 = alarms2 & this.mScanSuccess;
                                            boolean podcasts22 = podcasts & this.mScanSuccess;
                                            boolean music22 = music & this.mScanSuccess;
                                            HwFrameworkFactory.getHwMediaScannerManager().initializeSniffer(this.mPath);
                                            if (this.mIsAlbumMessy) {
                                            }
                                            if (this.mIsArtistMessy) {
                                            }
                                            if (this.mIsTitleMessy) {
                                            }
                                            HwFrameworkFactory.getHwMediaScannerManager().resetSniffer();
                                            boolean z2 = isimage;
                                            result = endFile(entry, ringtones32, notifications32, alarms32, music22, podcasts22);
                                        }
                                    }
                                    music = true;
                                    boolean ringtones22 = ringtones | HwThemeManager.isTRingtones(lowpath);
                                    boolean notifications22 = notifications | HwThemeManager.isTNotifications(lowpath);
                                    boolean alarms22 = alarms | HwThemeManager.isTAlarms(lowpath);
                                    isaudio = MediaFile.isAudioFileType(this.mFileType);
                                    boolean isvideo2 = MediaFile.isVideoFileType(this.mFileType);
                                    isimage = MediaFile.isImageFileType(this.mFileType);
                                    boolean unused3 = MediaScanner.this.mIsImageType = isimage;
                                    path2 = Environment.maybeTranslateEmulatedPathToInternal(new File(path2)).getAbsolutePath();
                                    if (!isaudio) {
                                    }
                                    this.mScanSuccess = MediaScanner.this.processFile(path2, mimeType, this);
                                    if (isimage) {
                                    }
                                    boolean ringtones322 = this.mScanSuccess & ringtones22;
                                    boolean notifications322 = notifications22 & this.mScanSuccess;
                                    boolean alarms322 = alarms22 & this.mScanSuccess;
                                    boolean podcasts222 = podcasts & this.mScanSuccess;
                                    boolean music222 = music & this.mScanSuccess;
                                    HwFrameworkFactory.getHwMediaScannerManager().initializeSniffer(this.mPath);
                                    if (this.mIsAlbumMessy) {
                                    }
                                    if (this.mIsArtistMessy) {
                                    }
                                    if (this.mIsTitleMessy) {
                                    }
                                    HwFrameworkFactory.getHwMediaScannerManager().resetSniffer();
                                    boolean z22 = isimage;
                                    result = endFile(entry, ringtones322, notifications322, alarms322, music222, podcasts222);
                                }
                            }
                        } catch (RemoteException e) {
                            e = e;
                            Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                            boolean unused4 = MediaScanner.this.mBlackListFlag = false;
                            boolean unused5 = MediaScanner.this.mIsImageType = false;
                            return result;
                        } catch (NullPointerException e2) {
                            e = e2;
                            Log.e(MediaScanner.TAG, "NullPointerException in MediaScanner", e);
                            boolean unused6 = MediaScanner.this.mBlackListFlag = false;
                            boolean unused7 = MediaScanner.this.mIsImageType = false;
                            return result;
                        }
                    }
                    boolean unused8 = MediaScanner.this.mBlackListFlag = false;
                    boolean unused9 = MediaScanner.this.mIsImageType = false;
                    return result;
                }
                scanAlways2 = scanAlways;
                if (entry != null) {
                }
                boolean unused10 = MediaScanner.this.mBlackListFlag = false;
                boolean unused11 = MediaScanner.this.mIsImageType = false;
                return result;
            } catch (RemoteException e3) {
                e = e3;
                boolean z3 = scanAlways;
                Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
                boolean unused12 = MediaScanner.this.mBlackListFlag = false;
                boolean unused13 = MediaScanner.this.mIsImageType = false;
                return result;
            } catch (NullPointerException e4) {
                e = e4;
                boolean z4 = scanAlways;
                Log.e(MediaScanner.TAG, "NullPointerException in MediaScanner", e);
                boolean unused14 = MediaScanner.this.mBlackListFlag = false;
                boolean unused15 = MediaScanner.this.mIsImageType = false;
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
            int start3 = s.charAt(start);
            if (start3 < 48 || start3 > 57) {
                return defaultValue;
            }
            int result = start3 - 48;
            while (start2 < length) {
                int start4 = start2 + 1;
                char ch = s.charAt(start2);
                if (ch < '0' || ch > '9') {
                    return result;
                }
                result = (result * 10) + (ch - '0');
                start2 = start4;
            }
            return result;
        }

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
            } else if (name.equalsIgnoreCase("composer") || name.startsWith("composer;")) {
                this.mComposer = value.trim();
            } else if (MediaScanner.this.mProcessGenres && (name.equalsIgnoreCase("genre") || name.startsWith("genre;"))) {
                this.mGenre = getGenreName(value);
            } else if (name.equalsIgnoreCase("year") || name.startsWith("year;")) {
                this.mYear = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("tracknumber") || name.startsWith("tracknumber;")) {
                this.mTrack = ((this.mTrack / 1000) * 1000) + parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("discnumber") || name.equals("set") || name.startsWith("set;")) {
                this.mTrack = (parseSubstring(value, 0, 0) * 1000) + (this.mTrack % 1000);
            } else if (name.equalsIgnoreCase("duration")) {
                this.mDuration = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("writer") || name.startsWith("writer;")) {
                this.mWriter = value.trim();
            } else if (name.equalsIgnoreCase("compilation")) {
                this.mCompilation = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase("isdrm")) {
                if (parseSubstring(value, 0, 0) != 1) {
                    z = false;
                }
                this.mIsDrm = z;
            } else if (name.equalsIgnoreCase("date")) {
                this.mDate = parseDate(value);
            } else if (name.equalsIgnoreCase(MediaFormat.KEY_WIDTH)) {
                this.mWidth = parseSubstring(value, 0, 0);
            } else if (name.equalsIgnoreCase(MediaFormat.KEY_HEIGHT)) {
                this.mHeight = parseSubstring(value, 0, 0);
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
            convertGenreCode("2", "Country");
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
            boolean z = false;
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
                if (this.mWidth > 0 && this.mHeight > 0) {
                    z = true;
                }
                return z;
            } catch (Throwable th) {
                return false;
            }
        }

        public void setMimeType(String mimeType) {
            if (!"audio/mp4".equals(this.mMimeType) || !mimeType.startsWith(MediaCodec.MetricsConstants.MODE_VIDEO)) {
                this.mMimeType = mimeType;
                this.mFileType = MediaFile.getFileTypeForMimeType(mimeType);
            }
        }

        private ContentValues toValues() {
            ContentValues map = new ContentValues();
            map.put("_data", this.mPath);
            map.put("title", this.mTitle);
            map.put("date_modified", Long.valueOf(this.mLastModified));
            map.put("_size", Long.valueOf(this.mFileSize));
            map.put("mime_type", this.mMimeType);
            map.put("is_drm", Boolean.valueOf(this.mIsDrm));
            String resolution = null;
            if (this.mWidth > 0 && this.mHeight > 0) {
                map.put(MediaFormat.KEY_WIDTH, Integer.valueOf(this.mWidth));
                map.put(MediaFormat.KEY_HEIGHT, Integer.valueOf(this.mHeight));
                resolution = this.mWidth + "x" + this.mHeight;
            }
            if (!this.mNoMedia) {
                if (MediaFile.isVideoFileType(this.mFileType)) {
                    map.put("artist", (this.mArtist == null || this.mArtist.length() <= 0) ? "<unknown>" : this.mArtist);
                    map.put("album", (this.mAlbum == null || this.mAlbum.length() <= 0) ? "<unknown>" : this.mAlbum);
                    map.put("duration", Integer.valueOf(this.mDuration));
                    if (resolution != null) {
                        map.put("resolution", resolution);
                    }
                    if (this.mDate > Time_1970) {
                        map.put("datetaken", Long.valueOf(this.mDate));
                    }
                    if (this.mDuration == 0) {
                        Log.e(MediaScanner.TAG, "video file duration = 0 and file size:" + this.mFileSize);
                    }
                } else if (!MediaFile.isImageFileType(this.mFileType) && this.mScanSuccess && MediaFile.isAudioFileType(this.mFileType)) {
                    map.put("artist", (this.mArtist == null || this.mArtist.length() <= 0) ? "<unknown>" : this.mArtist);
                    map.put("album_artist", (this.mAlbumArtist == null || this.mAlbumArtist.length() <= 0) ? null : this.mAlbumArtist);
                    map.put("album", (this.mAlbum == null || this.mAlbum.length() <= 0) ? "<unknown>" : this.mAlbum);
                    map.put("composer", this.mComposer);
                    map.put("genre", this.mGenre);
                    if (this.mYear != 0) {
                        map.put("year", Integer.valueOf(this.mYear));
                    }
                    map.put("track", Integer.valueOf(this.mTrack));
                    map.put("duration", Integer.valueOf(this.mDuration));
                    map.put("compilation", Integer.valueOf(this.mCompilation));
                }
                if (!this.mScanSuccess) {
                    map.put(DownloadManager.COLUMN_MEDIA_TYPE, (Integer) 0);
                }
            }
            return map;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:96:0x0201, code lost:
            if (doesPathHaveFilename(r2.mPath, android.media.MediaScanner.access$1200(r1.this$0)) != false) goto L_0x0206;
         */
        /* JADX WARNING: Removed duplicated region for block: B:120:0x026f  */
        /* JADX WARNING: Removed duplicated region for block: B:148:0x0300  */
        /* JADX WARNING: Removed duplicated region for block: B:173:0x036e  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x006c  */
        private Uri endFile(FileEntry entry, boolean ringtones, boolean notifications, boolean alarms, boolean music, boolean podcasts) throws RemoteException {
            Uri result;
            boolean needToSetSettings2;
            boolean needToSetSettings;
            long rowId;
            Uri result2;
            Uri result3;
            int degree;
            FileEntry fileEntry = entry;
            if (this.mArtist == null || this.mArtist.length() == 0) {
                this.mArtist = this.mAlbumArtist;
            }
            ContentValues values = toValues();
            String title = values.getAsString("title");
            if (title == null || TextUtils.isEmpty(title.trim())) {
                title = MediaFile.getFileTitle(values.getAsString("_data"));
                values.put("title", title);
            }
            String album = values.getAsString("album");
            if ("<unknown>".equals(album)) {
                album = values.getAsString("_data");
                int lastSlash = album.lastIndexOf(47);
                if (lastSlash >= 0) {
                    int previousSlash = 0;
                    while (true) {
                        int idx = album.indexOf(47, previousSlash + 1);
                        if (idx >= 0 && idx < lastSlash) {
                            previousSlash = idx;
                        } else if (previousSlash != 0) {
                            album = album.substring(previousSlash + 1, lastSlash);
                            values.put("album", album);
                        }
                    }
                    if (previousSlash != 0) {
                    }
                }
            }
            long rowId2 = fileEntry.mRowId;
            if (MediaFile.isAudioFileType(this.mFileType) && (rowId2 == 0 || MediaScanner.this.mMtpObjectHandle != 0)) {
                values.put("is_ringtone", Boolean.valueOf(ringtones));
                values.put("is_notification", Boolean.valueOf(notifications));
                values.put("is_alarm", Boolean.valueOf(alarms));
                values.put("is_music", Boolean.valueOf(music));
                values.put("is_podcast", Boolean.valueOf(podcasts));
            } else if ((this.mFileType == 34 || this.mFileType == 40 || MediaFile.isRawImageFileType(this.mFileType)) && !this.mNoMedia) {
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(fileEntry.mPath);
                } catch (IOException e) {
                }
                if (exif != null) {
                    float[] latlng = new float[2];
                    boolean mHasLatLong = exif.getLatLong(latlng);
                    if (mHasLatLong) {
                        values.put("latitude", Float.valueOf(latlng[0]));
                        values.put("longitude", Float.valueOf(latlng[1]));
                    }
                    long time = exif.getGpsDateTime();
                    if (time == -1 || !mHasLatLong) {
                        time = exif.getDateTimeOriginal();
                        if (time == -1) {
                            time = exif.getDateTime();
                        }
                        if (time != -1) {
                            values.put("datetaken", Long.valueOf(time));
                        }
                    } else {
                        float[] fArr = latlng;
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
                        int i = orientation;
                        long j = time;
                        values.put("orientation", Integer.valueOf(degree));
                    } else {
                        long j2 = time;
                    }
                    scannerSpecialImageType(values, exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
                }
                HwFrameworkFactory.getHwMediaScannerManager().initializeHwVoiceAndFocus(fileEntry.mPath, values);
            }
            MediaScanner.this.updateValues(fileEntry.mPath, values);
            Uri tableUri = MediaScanner.this.mFilesUri;
            MediaInserter inserter = MediaScanner.this.mMediaInserter;
            if (this.mScanSuccess && !this.mNoMedia && (!MediaScanner.this.mBlackListFlag || !MediaScanner.this.mIsImageType)) {
                if (MediaFile.isVideoFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mVideoUri;
                } else if (MediaFile.isImageFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mImagesUri;
                } else if (MediaFile.isAudioFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mAudioUri;
                }
            }
            boolean needToSetSettings3 = false;
            if (!notifications || MediaScanner.this.mDefaultNotificationSet) {
                result = null;
                if (ringtones) {
                    if ((!MediaScanner.this.mDefaultRingtoneSet && TextUtils.isEmpty(MediaScanner.this.mDefaultRingtoneFilename)) || doesPathHaveFilename(fileEntry.mPath, MediaScanner.this.mDefaultRingtoneFilename)) {
                        needToSetSettings3 = true;
                    }
                    needToSetSettings2 = HwFrameworkFactory.getHwMediaScannerManager().hwNeedSetSettings(fileEntry.mPath);
                    needToSetSettings = needToSetSettings3;
                    if (rowId2 != 0) {
                        if (MediaScanner.this.mMtpObjectHandle != 0) {
                            values.put("media_scanner_new_object_id", Integer.valueOf(MediaScanner.this.mMtpObjectHandle));
                        }
                        if (tableUri == MediaScanner.this.mFilesUri) {
                            int format = fileEntry.mFormat;
                            if (format == 0) {
                                format = MediaFile.getFormatCode(fileEntry.mPath, this.mMimeType);
                            }
                            values.put("format", Integer.valueOf(format));
                        }
                        if (MediaScanner.this.mBlackListFlag && MediaScanner.this.mIsImageType) {
                            values.put(DownloadManager.COLUMN_MEDIA_TYPE, (Integer) 10);
                        }
                        values.put("storage_id", Integer.valueOf(MediaScanner.this.mStorageIdForCurScanDIR));
                        if (inserter == null || needToSetSettings || needToSetSettings2) {
                            if (inserter != null) {
                                inserter.flushAll();
                            }
                            result3 = MediaScanner.this.mMediaProvider.insert(tableUri, values);
                        } else {
                            if (fileEntry.mFormat == 12289) {
                                inserter.insertwithPriority(tableUri, values);
                            } else {
                                inserter.insert(tableUri, values);
                            }
                            result3 = result;
                        }
                        if (result3 != null) {
                            long rowId3 = ContentUris.parseId(result3);
                            fileEntry.mRowId = rowId3;
                            result2 = result3;
                            rowId = rowId3;
                            if (needToSetSettings) {
                                if (notifications) {
                                    setRingtoneIfNotSet("notification_sound", tableUri, rowId);
                                    boolean unused = MediaScanner.this.mDefaultNotificationSet = true;
                                } else if (ringtones) {
                                    setRingtoneIfNotSet("ringtone", tableUri, rowId);
                                    boolean unused2 = MediaScanner.this.mDefaultRingtoneSet = true;
                                } else if (alarms) {
                                    setRingtoneIfNotSet("alarm_alert", tableUri, rowId);
                                    boolean unused3 = MediaScanner.this.mDefaultAlarmSet = true;
                                }
                            }
                            long j3 = rowId;
                            MediaInserter mediaInserter = inserter;
                            HwFrameworkFactory.getHwMediaScannerManager().hwSetRingtone2Settings(needToSetSettings2, ringtones, tableUri, rowId, MediaScanner.this.mContext);
                            return result2;
                        }
                    } else {
                        result3 = ContentUris.withAppendedId(tableUri, rowId2);
                        values.remove("_data");
                        int mediaType = 0;
                        if (MediaScanner.this.mBlackListFlag && MediaScanner.this.mIsImageType) {
                            values.put(DownloadManager.COLUMN_MEDIA_TYPE, (Integer) 10);
                        } else if (this.mScanSuccess && !MediaScanner.isNoMediaPath(fileEntry.mPath)) {
                            int fileType = MediaFile.getFileTypeForMimeType(this.mMimeType);
                            if (MediaFile.isAudioFileType(fileType)) {
                                mediaType = 2;
                            } else if (MediaFile.isVideoFileType(fileType)) {
                                mediaType = 3;
                            } else if (MediaFile.isImageFileType(fileType)) {
                                mediaType = 1;
                            } else if (MediaFile.isPlayListFileType(fileType)) {
                                mediaType = 4;
                            }
                            values.put(DownloadManager.COLUMN_MEDIA_TYPE, Integer.valueOf(mediaType));
                        }
                        MediaScanner.this.mMediaProvider.update(result3, values, null, null);
                    }
                    result2 = result3;
                    rowId = rowId2;
                    if (needToSetSettings) {
                    }
                    long j32 = rowId;
                    MediaInserter mediaInserter2 = inserter;
                    HwFrameworkFactory.getHwMediaScannerManager().hwSetRingtone2Settings(needToSetSettings2, ringtones, tableUri, rowId, MediaScanner.this.mContext);
                    return result2;
                } else if (alarms && !MediaScanner.this.mDefaultAlarmSet && (TextUtils.isEmpty(MediaScanner.this.mDefaultAlarmAlertFilename) || doesPathHaveFilename(fileEntry.mPath, MediaScanner.this.mDefaultAlarmAlertFilename))) {
                    needToSetSettings3 = true;
                }
            } else {
                if (!TextUtils.isEmpty(MediaScanner.this.mDefaultNotificationFilename)) {
                    result = null;
                } else {
                    result = null;
                }
                needToSetSettings3 = true;
            }
            needToSetSettings = needToSetSettings3;
            needToSetSettings2 = false;
            if (rowId2 != 0) {
            }
            result2 = result3;
            rowId = rowId2;
            if (needToSetSettings) {
            }
            long j322 = rowId;
            MediaInserter mediaInserter22 = inserter;
            HwFrameworkFactory.getHwMediaScannerManager().hwSetRingtone2Settings(needToSetSettings2, ringtones, tableUri, rowId, MediaScanner.this.mContext);
            return result2;
        }

        private void scannerSpecialImageType(ContentValues values, String exifDescription) {
            int hdrType = 0;
            if ("hdr".equals(exifDescription)) {
                hdrType = 1;
            } else if (MediaScanner.IMAGE_TYPE_JHDR.equals(exifDescription)) {
                hdrType = 2;
            }
            values.put("is_hdr", Integer.valueOf(hdrType));
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

        private int getFileTypeFromDrm(String path) {
            if (!MediaScanner.this.isDrmEnabled()) {
                return 0;
            }
            int resultFileType = 0;
            if (MediaScanner.this.mDrmManagerClient == null) {
                DrmManagerClient unused = MediaScanner.this.mDrmManagerClient = new DrmManagerClient(MediaScanner.this.mContext);
            }
            if (MediaScanner.this.mDrmManagerClient.canHandle(path, (String) null)) {
                this.mIsDrm = true;
                String drmMimetype = MediaScanner.this.mDrmManagerClient.getOriginalMimeType(path);
                if (drmMimetype != null) {
                    this.mMimeType = drmMimetype;
                    resultFileType = MediaFile.getFileTypeForMimeType(drmMimetype);
                }
            }
            return resultFileType;
        }
    }

    private static class PlaylistEntry {
        long bestmatchid;
        int bestmatchlevel;
        String path;

        private PlaylistEntry() {
        }
    }

    class WplHandler implements ElementListener {
        final ContentHandler handler;
        String playListDirectory;

        public WplHandler(String playListDirectory2, Uri uri, Cursor fileList) {
            this.playListDirectory = playListDirectory2;
            RootElement root = new RootElement("smil");
            root.getChild(TtmlUtils.TAG_BODY).getChild("seq").getChild("media").setElementListener(this);
            this.handler = root.getContentHandler();
        }

        public void start(Attributes attributes) {
            String path = attributes.getValue("", "src");
            if (path != null) {
                MediaScanner.this.cachePlaylistEntry(path, this.playListDirectory);
            }
        }

        public void end() {
        }

        /* access modifiers changed from: package-private */
        public ContentHandler getContentHandler() {
            return this.handler;
        }
    }

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup();

    private native void processDirectory(String str, MediaScannerClient mediaScannerClient);

    /* access modifiers changed from: private */
    public native boolean processFile(String str, String str2, MediaScannerClient mediaScannerClient);

    private static native void releaseBlackList();

    private static native void setBlackList(String str);

    private native void setLocale(String str);

    public static native void setStorageEjectFlag(boolean z);

    public native void addSkipCustomDirectory(String str, int i);

    public native void clearSkipCustomDirectory();

    public native byte[] extractAlbumArt(FileDescriptor fileDescriptor);

    public native void setExteLen(int i);

    static /* synthetic */ long access$904(MediaScanner x0) {
        long j = x0.mScanDirectoryFilesNum + 1;
        x0.mScanDirectoryFilesNum = j;
        return j;
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    public MediaScanner(Context c, String volumeName) {
        native_setup();
        this.mContext = c;
        this.mPackageName = c.getPackageName();
        this.mVolumeName = volumeName;
        this.mBitmapOptions.inSampleSize = 1;
        this.mBitmapOptions.inJustDecodeBounds = true;
        setDefaultRingtoneFileNames();
        this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient("media");
        if (sLastInternalScanFingerprint == null) {
            sLastInternalScanFingerprint = this.mContext.getSharedPreferences(SCANNED_BUILD_PREFS_NAME, 0).getString(LAST_INTERNAL_SCAN_FINGERPRINT, new String());
        }
        this.mAudioUri = MediaStore.Audio.Media.getContentUri(volumeName);
        this.mVideoUri = MediaStore.Video.Media.getContentUri(volumeName);
        this.mImagesUri = MediaStore.Images.Media.getContentUri(volumeName);
        this.mFilesUri = MediaStore.Files.getContentUri(volumeName);
        this.mFilesUriNoNotify = this.mFilesUri.buildUpon().appendQueryParameter("nonotify", SQL_VALUE_EXIF_FLAG).build();
        if (!volumeName.equals(INTERNAL_VOLUME)) {
            this.mProcessPlaylists = true;
            this.mProcessGenres = true;
            this.mPlaylistsUri = MediaStore.Audio.Playlists.getContentUri(volumeName);
            this.mExtStroagePath = HwFrameworkFactory.getHwMediaScannerManager().getExtSdcardVolumePath(this.mContext);
            this.mSkipExternelQuery = HwFrameworkFactory.getHwMediaScannerManager().isSkipExtSdcard(this.mMediaProvider, this.mExtStroagePath, this.mPackageName, this.mFilesUriNoNotify);
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
                    setLocale(language + "_" + country);
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
            this.mMaxFileCacheEntrySize = ((totalMemoryGb - 2) * SQLiteDatabase.SQLITE_MAX_LIKE_PATTERN_LENGTH) + 40000;
        }
        if (this.mMaxFileCacheEntrySize > 350000) {
            this.mMaxFileCacheEntrySize = 350000;
        }
        Log.d(TAG, "totalMemoryGb " + totalMemoryGb + ", lowRamDevice " + lowRamDevice + "mMaxFileCacheEntrySize = " + this.mMaxFileCacheEntrySize);
    }

    private void setDefaultRingtoneFileNames() {
        this.mDefaultRingtoneFilename = SystemProperties.get("ro.config.ringtone");
        HwFrameworkFactory.getHwMediaScannerManager().setHwDefaultRingtoneFileNames();
        this.mDefaultNotificationFilename = SystemProperties.get("ro.config.notification_sound");
        this.mDefaultAlarmAlertFilename = SystemProperties.get("ro.config.alarm_alert");
    }

    /* access modifiers changed from: private */
    public boolean isDrmEnabled() {
        String prop = SystemProperties.get("drm.service.enabled");
        return prop != null && prop.equals("true");
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

    /* JADX WARNING: Code restructure failed: missing block: B:117:0x0213, code lost:
        if (r4 != null) goto L_0x0215;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x022f, code lost:
        if (r4 == null) goto L_0x0232;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x009f, code lost:
        r5 = r17;
     */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x0224 A[Catch:{ SQLException -> 0x0225, OperationApplicationException -> 0x0219, RemoteException -> 0x0209, all -> 0x0204, all -> 0x0254 }] */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x0259  */
    public void updateBlackListFile() {
        ArrayList<ContentProviderOperation> ops;
        int fileCount;
        int count;
        Cursor cursor;
        String str;
        int i;
        String[] strArr;
        String where;
        String[] selectionArgs;
        Uri updateRowId;
        String where2 = "_id>? and (media_type=10 or media_type=1)";
        Cursor cursor2 = null;
        String[] selectionArgs2 = {"0"};
        int count2 = 0;
        Uri limitUri = this.mFilesUri.buildUpon().appendQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT, SQL_QUERY_LIMIT).build();
        long start = System.currentTimeMillis();
        ArrayList<ContentProviderOperation> ops2 = new ArrayList<>();
        long lastId = Long.MIN_VALUE;
        int fileCount2 = 0;
        while (true) {
            if (count2 >= 100) {
                String[] strArr2 = selectionArgs2;
                ops = ops2;
                fileCount = fileCount2;
                break;
            }
            try {
                selectionArgs2[0] = "" + lastId;
                count = count2 + 1;
                if (cursor2 != null) {
                    try {
                        cursor2.close();
                        cursor2 = null;
                    } catch (SQLException e) {
                        String str2 = where2;
                        String[] strArr3 = selectionArgs2;
                        count2 = count;
                        ArrayList<ContentProviderOperation> arrayList = ops2;
                        Log.e(TAG, "updateBlackListFile SQLException ! ");
                    } catch (OperationApplicationException e2) {
                        String str3 = where2;
                        String[] strArr4 = selectionArgs2;
                        count2 = count;
                        ArrayList<ContentProviderOperation> arrayList2 = ops2;
                        Log.e(TAG, "MediaProvider upate all file Exception when use the applyBatch ! ");
                        if (cursor2 != null) {
                        }
                        long end = System.currentTimeMillis();
                        StringBuilder sb = new StringBuilder();
                        sb.append("updateBlackListFile total time = ");
                        Cursor cursor3 = cursor2;
                        int i2 = count2;
                        sb.append(end - start);
                        Log.d(TAG, sb.toString());
                    } catch (RemoteException e3) {
                        String str4 = where2;
                        String[] strArr5 = selectionArgs2;
                        count2 = count;
                        ArrayList<ContentProviderOperation> arrayList3 = ops2;
                        Log.e(TAG, "updateBlackListFile RemoteException ! ");
                    } catch (Throwable th) {
                        th = th;
                        String str5 = where2;
                        String[] strArr6 = selectionArgs2;
                        ArrayList<ContentProviderOperation> arrayList4 = ops2;
                        if (cursor2 != null) {
                        }
                        throw th;
                    }
                }
                cursor = cursor2;
                try {
                    str = where2;
                    i = 0;
                    strArr = selectionArgs2;
                    where = where2;
                    ops = ops2;
                    selectionArgs = selectionArgs2;
                    fileCount = fileCount2;
                } catch (SQLException e4) {
                    String str6 = where2;
                    String[] strArr7 = selectionArgs2;
                    ArrayList<ContentProviderOperation> arrayList5 = ops2;
                    int i3 = fileCount2;
                    count2 = count;
                    cursor2 = cursor;
                    Log.e(TAG, "updateBlackListFile SQLException ! ");
                } catch (OperationApplicationException e5) {
                    String str7 = where2;
                    String[] strArr8 = selectionArgs2;
                    ArrayList<ContentProviderOperation> arrayList6 = ops2;
                    int i4 = fileCount2;
                    count2 = count;
                    cursor2 = cursor;
                    Log.e(TAG, "MediaProvider upate all file Exception when use the applyBatch ! ");
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    long end2 = System.currentTimeMillis();
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("updateBlackListFile total time = ");
                    Cursor cursor32 = cursor2;
                    int i22 = count2;
                    sb2.append(end2 - start);
                    Log.d(TAG, sb2.toString());
                } catch (RemoteException e6) {
                    String str8 = where2;
                    String[] strArr9 = selectionArgs2;
                    ArrayList<ContentProviderOperation> arrayList7 = ops2;
                    int i5 = fileCount2;
                    count2 = count;
                    cursor2 = cursor;
                    Log.e(TAG, "updateBlackListFile RemoteException ! ");
                } catch (Throwable th2) {
                    th = th2;
                    String str9 = where2;
                    String[] strArr10 = selectionArgs2;
                    ArrayList<ContentProviderOperation> arrayList8 = ops2;
                    int i6 = fileCount2;
                    cursor2 = cursor;
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    throw th;
                }
            } catch (SQLException e7) {
                String str10 = where2;
                String[] strArr11 = selectionArgs2;
                ArrayList<ContentProviderOperation> arrayList9 = ops2;
                int i7 = fileCount2;
                Log.e(TAG, "updateBlackListFile SQLException ! ");
            } catch (OperationApplicationException e8) {
                String str11 = where2;
                String[] strArr12 = selectionArgs2;
                ArrayList<ContentProviderOperation> arrayList10 = ops2;
                int i8 = fileCount2;
                Log.e(TAG, "MediaProvider upate all file Exception when use the applyBatch ! ");
                if (cursor2 != null) {
                }
                long end22 = System.currentTimeMillis();
                StringBuilder sb22 = new StringBuilder();
                sb22.append("updateBlackListFile total time = ");
                Cursor cursor322 = cursor2;
                int i222 = count2;
                sb22.append(end22 - start);
                Log.d(TAG, sb22.toString());
            } catch (RemoteException e9) {
                String str12 = where2;
                String[] strArr13 = selectionArgs2;
                ArrayList<ContentProviderOperation> arrayList11 = ops2;
                int i9 = fileCount2;
                Log.e(TAG, "updateBlackListFile RemoteException ! ");
            } catch (Throwable th3) {
                th = th3;
                String str13 = where2;
                String[] strArr14 = selectionArgs2;
                ArrayList<ContentProviderOperation> arrayList12 = ops2;
                int i10 = fileCount2;
                int i11 = count2;
                if (cursor2 != null) {
                }
                throw th;
            }
            try {
                cursor2 = this.mMediaProvider.query(limitUri, FILES_BLACKLIST_PROJECTION_MEDIA, str, strArr, DownloadManager.COLUMN_ID, null);
                if (cursor2 == null) {
                    break;
                }
                try {
                    int num = cursor2.getCount();
                    if (num == 0) {
                        break;
                    }
                    fileCount2 = fileCount;
                    while (cursor2.moveToNext()) {
                        try {
                            long rowId = cursor2.getLong(i);
                            String path = cursor2.getString(1);
                            int mediatype = cursor2.getInt(2);
                            lastId = rowId;
                            fileCount2++;
                            ContentValues values = new ContentValues();
                            int len = getRootDirLength(path);
                            boolean isBlackListFlag = isBlackListPath(path, len);
                            int num2 = num;
                            if (!isBlackListFlag || mediatype != 1) {
                                int i12 = len;
                                if (!isBlackListFlag && mediatype == 10) {
                                    values.put(DownloadManager.COLUMN_MEDIA_TYPE, (Integer) 1);
                                    updateRowId = ContentUris.withAppendedId(this.mImagesUri, rowId);
                                    ops.add(ContentProviderOperation.newUpdate(updateRowId).withValues(values).build());
                                }
                                num = num2;
                                i = 0;
                            } else {
                                int i13 = len;
                                values.put(DownloadManager.COLUMN_MEDIA_TYPE, (Integer) 10);
                                updateRowId = ContentUris.withAppendedId(this.mImagesUri, rowId);
                                try {
                                    ops.add(ContentProviderOperation.newUpdate(updateRowId).withValues(values).build());
                                } catch (SQLException e10) {
                                    Uri uri = updateRowId;
                                } catch (OperationApplicationException e11) {
                                    Uri uri2 = updateRowId;
                                    count2 = count;
                                    Log.e(TAG, "MediaProvider upate all file Exception when use the applyBatch ! ");
                                    if (cursor2 != null) {
                                    }
                                    long end222 = System.currentTimeMillis();
                                    StringBuilder sb222 = new StringBuilder();
                                    sb222.append("updateBlackListFile total time = ");
                                    Cursor cursor3222 = cursor2;
                                    int i2222 = count2;
                                    sb222.append(end222 - start);
                                    Log.d(TAG, sb222.toString());
                                } catch (RemoteException e12) {
                                    Uri uri3 = updateRowId;
                                    count2 = count;
                                    Log.e(TAG, "updateBlackListFile RemoteException ! ");
                                } catch (Throwable th4) {
                                    th = th4;
                                    Uri uri4 = updateRowId;
                                    if (cursor2 != null) {
                                    }
                                    throw th;
                                }
                            }
                            num = num2;
                            i = 0;
                        } catch (SQLException e13) {
                            count2 = count;
                            Log.e(TAG, "updateBlackListFile SQLException ! ");
                        } catch (OperationApplicationException e14) {
                            count2 = count;
                            Log.e(TAG, "MediaProvider upate all file Exception when use the applyBatch ! ");
                            if (cursor2 != null) {
                            }
                            long end2222 = System.currentTimeMillis();
                            StringBuilder sb2222 = new StringBuilder();
                            sb2222.append("updateBlackListFile total time = ");
                            Cursor cursor32222 = cursor2;
                            int i22222 = count2;
                            sb2222.append(end2222 - start);
                            Log.d(TAG, sb2222.toString());
                        } catch (RemoteException e15) {
                            count2 = count;
                            Log.e(TAG, "updateBlackListFile RemoteException ! ");
                        } catch (Throwable th5) {
                            th = th5;
                            if (cursor2 != null) {
                            }
                            throw th;
                        }
                    }
                    ops2 = ops;
                    count2 = count;
                    where2 = where;
                    selectionArgs2 = selectionArgs;
                } catch (SQLException e16) {
                    int i14 = fileCount;
                    count2 = count;
                    Log.e(TAG, "updateBlackListFile SQLException ! ");
                } catch (OperationApplicationException e17) {
                    int i15 = fileCount;
                    count2 = count;
                    Log.e(TAG, "MediaProvider upate all file Exception when use the applyBatch ! ");
                    if (cursor2 != null) {
                    }
                    long end22222 = System.currentTimeMillis();
                    StringBuilder sb22222 = new StringBuilder();
                    sb22222.append("updateBlackListFile total time = ");
                    Cursor cursor322222 = cursor2;
                    int i222222 = count2;
                    sb22222.append(end22222 - start);
                    Log.d(TAG, sb22222.toString());
                } catch (RemoteException e18) {
                    int i16 = fileCount;
                    count2 = count;
                    Log.e(TAG, "updateBlackListFile RemoteException ! ");
                } catch (Throwable th6) {
                    th = th6;
                    if (cursor2 != null) {
                    }
                    throw th;
                }
            } catch (SQLException e19) {
                int i17 = fileCount;
                count2 = count;
                cursor2 = cursor;
                Log.e(TAG, "updateBlackListFile SQLException ! ");
            } catch (OperationApplicationException e20) {
                int i18 = fileCount;
                count2 = count;
                cursor2 = cursor;
                Log.e(TAG, "MediaProvider upate all file Exception when use the applyBatch ! ");
                if (cursor2 != null) {
                }
                long end222222 = System.currentTimeMillis();
                StringBuilder sb222222 = new StringBuilder();
                sb222222.append("updateBlackListFile total time = ");
                Cursor cursor3222222 = cursor2;
                int i2222222 = count2;
                sb222222.append(end222222 - start);
                Log.d(TAG, sb222222.toString());
            } catch (RemoteException e21) {
                int i19 = fileCount;
                count2 = count;
                cursor2 = cursor;
                Log.e(TAG, "updateBlackListFile RemoteException ! ");
            } catch (Throwable th7) {
                th = th7;
                int i20 = fileCount;
                cursor2 = cursor;
                if (cursor2 != null) {
                }
                throw th;
            }
        }
        try {
            Log.d(TAG, "updateBlackListFile filecount = " + fileCount);
            if (count2 == 100) {
                Log.d(TAG, "SQL query exceed the limit 100 !");
            }
            this.mMediaProvider.applyBatch(ops);
            if (cursor2 != null) {
                cursor2.close();
            }
            int i21 = fileCount;
        } catch (SQLException e22) {
            int i23 = fileCount;
            Log.e(TAG, "updateBlackListFile SQLException ! ");
        } catch (OperationApplicationException e23) {
            int i24 = fileCount;
            Log.e(TAG, "MediaProvider upate all file Exception when use the applyBatch ! ");
            if (cursor2 != null) {
            }
            long end2222222 = System.currentTimeMillis();
            StringBuilder sb2222222 = new StringBuilder();
            sb2222222.append("updateBlackListFile total time = ");
            Cursor cursor32222222 = cursor2;
            int i22222222 = count2;
            sb2222222.append(end2222222 - start);
            Log.d(TAG, sb2222222.toString());
        } catch (RemoteException e24) {
            int i25 = fileCount;
            Log.e(TAG, "updateBlackListFile RemoteException ! ");
        } catch (Throwable th8) {
            th = th8;
            int i26 = count2;
            if (cursor2 != null) {
            }
            throw th;
        }
        long end22222222 = System.currentTimeMillis();
        StringBuilder sb22222222 = new StringBuilder();
        sb22222222.append("updateBlackListFile total time = ");
        Cursor cursor322222222 = cursor2;
        int i222222222 = count2;
        sb22222222.append(end22222222 - start);
        Log.d(TAG, sb22222222.toString());
    }

    /* access modifiers changed from: private */
    public static boolean isSystemSoundWithMetadata(String path) {
        if (path.startsWith("/system/media/audio/alarms/") || path.startsWith("/system/media/audio/ringtones/") || path.startsWith("/system/media/audio/notifications/") || path.startsWith("/product/media/audio/alarms/") || path.startsWith("/product/media/audio/ringtones/") || path.startsWith("/product/media/audio/notifications/")) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public String settingSetIndicatorName(String base) {
        return base + "_set";
    }

    /* access modifiers changed from: private */
    public boolean wasRingtoneAlreadySet(String name) {
        boolean z = false;
        try {
            if (Settings.System.getInt(this.mContext.getContentResolver(), settingSetIndicatorName(name)) != 0) {
                z = true;
            }
            return z;
        } catch (Settings.SettingNotFoundException e) {
            return false;
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
            if (c == null) {
                return;
            }
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
        c.close();
    }

    private void prescanOnlyMedia(Uri uriTable, boolean isOnlyMedia) throws RemoteException {
        hwPrescan(null, true, uriTable, isOnlyMedia);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0068, code lost:
        r3 = r16;
     */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00f0  */
    /* JADX WARNING: Removed duplicated region for block: B:70:? A[RETURN, SYNTHETIC] */
    public void updateExifFile() throws RemoteException {
        int num;
        String[] selectionArgs = {"0"};
        Uri limitUri = this.mImagesUri.buildUpon().appendQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT, SQL_QUERY_LIMIT).build();
        int i = 0;
        long lastId = Long.MIN_VALUE;
        Cursor c = null;
        int count = 0;
        while (true) {
            num = count;
            try {
                if (sPowerConnect == 0 || num >= 100) {
                    break;
                }
                int count2 = num + 1;
                try {
                    selectionArgs[i] = "" + lastId;
                    if (c != null) {
                        c.close();
                        c = null;
                    }
                    Cursor c2 = c;
                    try {
                        c = this.mMediaProvider.query(limitUri, FILES_PRESCAN_PROJECTION_MEDIA, "_id>? and cam_exif_flag is null", selectionArgs, DownloadManager.COLUMN_ID, null);
                        if (c == null) {
                            break;
                        } else if (c.getCount() == 0) {
                            break;
                        } else {
                            while (sPowerConnect && c.moveToNext()) {
                                long rowId = c.getLong(i);
                                lastId = rowId;
                                ContentValues values = new ContentValues();
                                values.put("cam_exif_flag", SQL_VALUE_EXIF_FLAG);
                                Uri updateRowId = ContentUris.withAppendedId(this.mImagesUri, rowId);
                                ExifInterface exif = null;
                                try {
                                    exif = new ExifInterface(c.getString(1));
                                } catch (IOException e) {
                                    Log.e(TAG, "new ExifInterface Exception !");
                                }
                                HwFrameworkFactory.getHwMediaScannerManager().scanHwMakerNote(values, exif);
                                this.mMediaProvider.update(updateRowId, values, null, null);
                                i = 0;
                            }
                            count = count2;
                            i = 0;
                        }
                    } catch (SQLException e2) {
                        num = count2;
                        c = c2;
                        try {
                            Log.e(TAG, "updateExifFile SQLException ! ");
                            if (c == null) {
                            }
                            c.close();
                        } catch (Throwable th) {
                            th = th;
                            int i2 = num;
                            if (c != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        c = c2;
                        if (c != null) {
                        }
                        throw th;
                    }
                } catch (SQLException e3) {
                    num = count2;
                    Log.e(TAG, "updateExifFile SQLException ! ");
                    if (c == null) {
                        return;
                    }
                    c.close();
                } catch (Throwable th3) {
                    th = th3;
                    if (c != null) {
                        c.close();
                    }
                    throw th;
                }
            } catch (SQLException e4) {
                Log.e(TAG, "updateExifFile SQLException ! ");
                if (c == null) {
                }
                c.close();
            }
        }
        if (num == 100) {
            Log.d(TAG, "SQL query exceed the limit 10 !");
        }
        if (c == null) {
            return;
        }
        c.close();
    }

    /* JADX WARNING: Removed duplicated region for block: B:98:0x01d2  */
    private void hwPrescan(String filePath, boolean prescanFiles, Uri uriTable, boolean isOnlyMedia) throws RemoteException {
        String where;
        String[] selectionArgs;
        MediaBulkDeleter deleter;
        MediaBulkDeleter deleter2;
        MediaBulkDeleter deleter3;
        int mediaType;
        int num;
        Cursor c;
        String str;
        Cursor c2 = null;
        this.mPlayLists.clear();
        if (this.mFileCache == null) {
            this.mFileCache = new HashMap<>();
        } else {
            this.mFileCache.clear();
        }
        int i = 2;
        int i2 = 1;
        int fileType = 0;
        if (filePath != null) {
            where = "_id>? AND _data=?";
            selectionArgs = new String[]{"", filePath};
        } else {
            where = "_id>?";
            selectionArgs = new String[]{""};
        }
        String[] selectionArgs2 = selectionArgs;
        String where2 = where;
        Uri.Builder builder = uriTable.buildUpon();
        builder.appendQueryParameter("deletedata", "false");
        MediaBulkDeleter deleter4 = new MediaBulkDeleter(this.mMediaProvider, builder.build());
        if (prescanFiles) {
            try {
                Uri limitUri = uriTable.buildUpon().appendQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT, SQL_QUERY_LIMIT).build();
                deleteFilesIfPossible();
                int count = 0;
                long lastId = Long.MIN_VALUE;
                while (true) {
                    selectionArgs2[fileType] = "" + lastId;
                    if (c2 != null) {
                        try {
                            c2.close();
                            c2 = null;
                        } catch (Throwable th) {
                            th = th;
                            String str2 = where2;
                            deleter2 = deleter4;
                            Uri.Builder builder2 = builder;
                        }
                    }
                    long lastId2 = lastId;
                    deleter3 = deleter4;
                    Uri.Builder builder3 = builder;
                    try {
                        c2 = this.mMediaProvider.query(limitUri, isOnlyMedia ? FILES_PRESCAN_PROJECTION_MEDIA : FILES_PRESCAN_PROJECTION, where2, selectionArgs2, DownloadManager.COLUMN_ID, null);
                        if (c2 == null) {
                            break;
                        }
                        try {
                            int num2 = c2.getCount();
                            if (num2 == 0) {
                                break;
                            }
                            long lastModified = 0;
                            int format = 0;
                            int count2 = count;
                            int mediaType2 = fileType;
                            while (c2.moveToNext()) {
                                long rowId = c2.getLong(fileType);
                                String path = c2.getString(i2);
                                if (!isOnlyMedia) {
                                    try {
                                        format = c2.getInt(i);
                                        lastModified = c2.getLong(3);
                                        mediaType = c2.getInt(4);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        String str3 = where2;
                                        deleter2 = deleter3;
                                        if (c2 != null) {
                                        }
                                        deleter2.flush();
                                        throw th;
                                    }
                                } else {
                                    mediaType = mediaType2;
                                }
                                lastId2 = rowId;
                                if (path != null) {
                                    if (path.startsWith("/")) {
                                        boolean exists = fileType;
                                        try {
                                            exists = Os.access(path, OsConstants.F_OK);
                                        } catch (ErrnoException e) {
                                        }
                                        if (exists == 0) {
                                            if (!MtpConstants.isAbstractObject(format)) {
                                                MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
                                                if (mediaFileType != null) {
                                                    fileType = mediaFileType.fileType;
                                                }
                                                if (!MediaFile.isPlayListFileType(fileType)) {
                                                    c = c2;
                                                    str = where2;
                                                    deleter2 = deleter3;
                                                    try {
                                                        deleter2.delete(rowId);
                                                        MediaFile.MediaFileType mediaFileType2 = mediaFileType;
                                                        if (path.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                                                            deleter2.flush();
                                                            int i3 = fileType;
                                                            num = num2;
                                                            this.mMediaProvider.call("unhide", new File(path).getParent(), null);
                                                        } else {
                                                            num = num2;
                                                        }
                                                        mediaType2 = mediaType;
                                                        deleter3 = deleter2;
                                                        where2 = str;
                                                        c2 = c;
                                                        num2 = num;
                                                        i = 2;
                                                        i2 = 1;
                                                        fileType = 0;
                                                    } catch (Throwable th3) {
                                                        th = th3;
                                                        c2 = c;
                                                        if (c2 != null) {
                                                            c2.close();
                                                        }
                                                        deleter2.flush();
                                                        throw th;
                                                    }
                                                }
                                            }
                                        }
                                        c = c2;
                                        str = where2;
                                        num = num2;
                                        deleter2 = deleter3;
                                        long rowId2 = rowId;
                                        if (!this.mNeedFilter || !HwFrameworkFactory.getHwMediaScannerManager().isAudioFilterFile(path)) {
                                            if (!isOnlyMedia && count2 < this.mMaxFileCacheEntrySize) {
                                                FileEntry fileEntry = new FileEntry(rowId2, path, lastModified, format, mediaType);
                                                this.mFileCache.put(path, fileEntry);
                                            }
                                            count2++;
                                            mediaType2 = mediaType;
                                            deleter3 = deleter2;
                                            where2 = str;
                                            c2 = c;
                                            num2 = num;
                                            i = 2;
                                            i2 = 1;
                                            fileType = 0;
                                        } else {
                                            deleter2.delete(rowId2);
                                            mediaType2 = mediaType;
                                            deleter3 = deleter2;
                                            where2 = str;
                                            c2 = c;
                                            num2 = num;
                                            i = 2;
                                            i2 = 1;
                                            fileType = 0;
                                        }
                                    }
                                }
                                c = c2;
                                str = where2;
                                num = num2;
                                deleter2 = deleter3;
                                mediaType2 = mediaType;
                                deleter3 = deleter2;
                                where2 = str;
                                c2 = c;
                                num2 = num;
                                i = 2;
                                i2 = 1;
                                fileType = 0;
                            }
                            Cursor cursor = c2;
                            String str4 = where2;
                            deleter4 = deleter3;
                            count = count2;
                            builder = builder3;
                            lastId = lastId2;
                            i = 2;
                            i2 = 1;
                            fileType = 0;
                        } catch (Throwable th4) {
                            th = th4;
                            Cursor cursor2 = c2;
                            String str5 = where2;
                            deleter2 = deleter3;
                            if (c2 != null) {
                            }
                            deleter2.flush();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        String str6 = where2;
                        deleter2 = deleter3;
                        if (c2 != null) {
                        }
                        deleter2.flush();
                        throw th;
                    }
                }
                String str7 = where2;
                deleter = deleter3;
            } catch (Throwable th6) {
                th = th6;
                String str8 = where2;
                deleter2 = deleter4;
                Uri.Builder builder4 = builder;
                if (c2 != null) {
                }
                deleter2.flush();
                throw th;
            }
        } else {
            deleter = deleter4;
            Uri.Builder builder5 = builder;
        }
        if (c2 != null) {
            c2.close();
        }
        deleter.flush();
        if (isOnlyMedia) {
            this.mOriginalCount = -1;
            this.mDefaultRingtoneSet = true;
            this.mDefaultNotificationSet = true;
            this.mDefaultAlarmSet = true;
            return;
        }
        this.mDefaultRingtoneSet = wasRingtoneAlreadySet("ringtone");
        this.mDefaultNotificationSet = wasRingtoneAlreadySet("notification_sound");
        this.mDefaultAlarmSet = wasRingtoneAlreadySet("alarm_alert");
        this.mOriginalCount = 0;
        Cursor c3 = this.mMediaProvider.query(this.mImagesUri, new String[]{"COUNT(*)"}, null, null, null, null);
        if (c3 != null) {
            if (c3.moveToFirst()) {
                this.mOriginalCount = c3.getInt(0);
            }
            c3.close();
        }
    }

    private void prescan(String filePath, boolean prescanFiles) throws RemoteException {
        Log.d(TAG, "prescan begin prescanFiles: " + prescanFiles);
        hwPrescan(filePath, prescanFiles, this.mFilesUri, false);
        Log.d(TAG, "prescan end mFileCache size " + this.mFileCache.size());
    }

    public void postscan(String[] directories) throws RemoteException {
        if (this.mProcessPlaylists) {
            processPlayLists();
        }
        HwFrameworkFactory.getHwMediaScannerManager().pruneDeadThumbnailsFolder();
        this.mPlayLists.clear();
    }

    private void releaseResources() {
        if (this.mDrmManagerClient != null) {
            this.mDrmManagerClient.close();
            this.mDrmManagerClient = null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00f5, code lost:
        if (r1.mFileCache != null) goto L_0x0139;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0117, code lost:
        if (r1.mFileCache != null) goto L_0x0139;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0127, code lost:
        if (r1.mFileCache != null) goto L_0x0139;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0137, code lost:
        if (r1.mFileCache != null) goto L_0x0139;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0139, code lost:
        r1.mFileCache.clear();
        r1.mFileCache = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0140, code lost:
        r1.mSkipExternelQuery = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0143, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x014b  */
    public void scanDirectories(String[] directories) {
        MediaScanner mediaScanner = this;
        String[] strArr = directories;
        try {
            StorageVolume[] volumes = StorageManager.getVolumeList(UserHandle.myUserId(), 256);
            int length = volumes.length;
            boolean flag = false;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String rootPath = volumes[i].getPath();
                int i2 = 0;
                while (true) {
                    if (i2 >= strArr.length) {
                        break;
                    } else if (rootPath.equalsIgnoreCase(strArr[i2])) {
                        flag = true;
                        Log.i(TAG, "MediaScanner scanDirectories flag = true means root dirs!");
                        break;
                    } else {
                        i2++;
                    }
                }
                if (flag) {
                    break;
                }
                i++;
            }
            if (INTERNAL_VOLUME.equalsIgnoreCase(mediaScanner.mVolumeName)) {
                flag = true;
                Log.i(TAG, "MediaScanner scanDirectories flag = true means internal!");
            }
            long start = System.currentTimeMillis();
            if (flag) {
                mediaScanner.prescan(null, mediaScanner.mIsPrescanFiles);
            } else {
                mediaScanner.prescanOnlyMedia(mediaScanner.mAudioUri, true);
                mediaScanner.prescanOnlyMedia(mediaScanner.mVideoUri, true);
                mediaScanner.prescanOnlyMedia(mediaScanner.mImagesUri, true);
            }
            long currentTimeMillis = System.currentTimeMillis();
            mediaScanner.mMediaInserter = new MediaInserter(mediaScanner.mMediaProvider, ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY);
            Log.d(TAG, "delete nomedia File when scanDirectories");
            mediaScanner.deleteNomediaFile(StorageManager.getVolumeList(UserHandle.myUserId(), 256));
            for (int i3 = 0; i3 < strArr.length; i3++) {
                mediaScanner.setExteLen(mediaScanner.getRootDirLength(strArr[i3]));
                sCurScanDIR = strArr[i3];
                mediaScanner.setStorageIdForCurScanDIR(strArr[i3]);
                mediaScanner.processDirectory(strArr[i3], mediaScanner.mClient);
            }
            if (mediaScanner.mMediaTypeConflict != 0) {
                Log.i(TAG, "find some files's media type did not match with database");
            }
            Log.d(TAG, "scanDirectories total files number is " + mediaScanner.mScanDirectoryFilesNum);
            mediaScanner.mMediaInserter.flushAll();
            mediaScanner.mMediaInserter = null;
            long currentTimeMillis2 = System.currentTimeMillis();
            postscan(directories);
            try {
                HwMediaMonitorManager.writeBigData(HwMediaMonitorUtils.BD_MEDIA_SCANNING, HwMediaMonitorUtils.M_SCANNING_COMPELETED_PERIOD, (int) ((System.currentTimeMillis() - start) / 1000), 0);
                mediaScanner = this;
                releaseResources();
            } catch (SQLException e) {
                e = e;
                mediaScanner = this;
                Log.e(TAG, "SQLException in MediaScanner.scan()", e);
                releaseResources();
            } catch (UnsupportedOperationException e2) {
                e = e2;
                mediaScanner = this;
                Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e);
                releaseResources();
            } catch (RemoteException e3) {
                e = e3;
                mediaScanner = this;
                try {
                    Log.e(TAG, "RemoteException in MediaScanner.scan()", e);
                    releaseResources();
                } catch (Throwable th) {
                    th = th;
                    releaseResources();
                    if (mediaScanner.mFileCache != null) {
                        mediaScanner.mFileCache.clear();
                        mediaScanner.mFileCache = null;
                    }
                    mediaScanner.mSkipExternelQuery = false;
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                mediaScanner = this;
                releaseResources();
                if (mediaScanner.mFileCache != null) {
                }
                mediaScanner.mSkipExternelQuery = false;
                throw th;
            }
        } catch (SQLException e4) {
            e = e4;
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
            releaseResources();
        } catch (UnsupportedOperationException e5) {
            e = e5;
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e);
            releaseResources();
        } catch (RemoteException e6) {
            e = e6;
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e);
            releaseResources();
        }
    }

    public void scanCustomDirectories(String[] directories, String volumeName, String[] whiteList, String[] blackList) {
        long start = System.currentTimeMillis();
        this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient("media");
        this.mMediaInserter = new MediaInserter(this.mMediaProvider, ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY);
        HwFrameworkFactory.getHwMediaScannerManager().setMediaInserter(this.mMediaInserter);
        Log.d(TAG, "delete nomedia File when scanCustomDirectories");
        deleteNomediaFile(StorageManager.getVolumeList(UserHandle.myUserId(), 256));
        HwFrameworkFactory.getHwMediaScannerManager().scanCustomDirectories(this, this.mClient, directories, whiteList, blackList);
        clearSkipCustomDirectory();
        if (this.mMediaTypeConflict) {
            Log.i(TAG, "find some files's media type did not match with database");
        }
        Log.d(TAG, "scanCustomDirectories total files number is " + this.mScanDirectoryFilesNum);
        if (this.mFileCache != null) {
            this.mFileCache.clear();
            this.mFileCache = null;
        }
        HwFrameworkFactory.getHwMediaScannerManager().setMediaInserter(null);
        this.mMediaInserter = null;
        this.mSkipExternelQuery = false;
        HwMediaMonitorManager.writeBigData(HwMediaMonitorUtils.BD_MEDIA_SCANNING, HwMediaMonitorUtils.M_SCANNING_COMPELETED_PERIOD, (int) ((System.currentTimeMillis() - start) / 1000), 0);
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

    public Uri scanSingleFile(String path, String mimeType) {
        String str = path;
        try {
            prescan(str, true);
            this.mBlackListFlag = false;
            if (isBlackListPath(str, getRootDirLength(path))) {
                this.mBlackListFlag = true;
            }
            File file = new File(str);
            if (file.exists()) {
                if (file.canRead()) {
                    Log.d(TAG, "delete nomedia File when scanSingleFile");
                    deleteNomediaFile(StorageManager.getVolumeList(UserHandle.myUserId(), 256));
                    long lastModifiedSeconds = file.lastModified() / 1000;
                    setStorageIdForCurScanDIR(path);
                    Uri doScanFile = this.mClient.doScanFile(str, mimeType, lastModifiedSeconds, file.length(), false, true, isNoMediaPath(path));
                    releaseResources();
                    return doScanFile;
                }
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
            return null;
        } finally {
            releaseResources();
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0072, code lost:
        if (r12.regionMatches(true, r1 + 1, "AlbumArtSmall", 0, 13) == false) goto L_0x0074;
     */
    public static boolean isNoMediaFile(String path) {
        if (new File(path).isDirectory()) {
            return false;
        }
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0 && lastSlash + 2 < path.length()) {
            if (path.regionMatches(lastSlash + 1, "._", 0, 2)) {
                return true;
            }
            if (path.regionMatches(true, path.length() - 4, ".jpg", 0, 4)) {
                if (!path.regionMatches(true, lastSlash + 1, "AlbumArt_{", 0, 10)) {
                    if (!path.regionMatches(true, lastSlash + 1, "AlbumArt.", 0, 9)) {
                        int length = (path.length() - lastSlash) - 1;
                        if (length == 17) {
                        }
                        if (length == 10) {
                            if (path.regionMatches(true, lastSlash + 1, "Folder", 0, 6)) {
                                return true;
                            }
                        }
                    }
                }
                return true;
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

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0072, code lost:
        return isNoMediaFile(r11);
     */
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
                        if (new File(path.substring(0, slashIndex) + ".nomedia").exists()) {
                            mNoMediaPaths.put(parent, "");
                            return true;
                        }
                    }
                    offset = slashIndex;
                }
                mMediaPaths.put(parent, "");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:69:0x0135  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0143  */
    public void scanMtpFile(String path, int objectHandle, int format) {
        Cursor fileList;
        String key = path;
        MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = mediaFileType == null ? 0 : mediaFileType.fileType;
        File file = new File(key);
        long lastModifiedSeconds = file.lastModified() / 1000;
        if (MediaFile.isAudioFileType(fileType) || MediaFile.isVideoFileType(fileType) || MediaFile.isImageFileType(fileType) || MediaFile.isPlayListFileType(fileType) || MediaFile.isDrmFileType(fileType)) {
            this.mMtpObjectHandle = objectHandle;
            Cursor fileList2 = null;
            try {
                if (MediaFile.isPlayListFileType(fileType)) {
                    try {
                        prescan(null, true);
                        FileEntry entry = this.mFileCache.remove(key);
                        if (entry == null) {
                            entry = makeEntryFor(path);
                        }
                        if (entry != null) {
                            fileList = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, null, null, null, null);
                            try {
                                processPlayList(entry, fileList);
                                fileList2 = fileList;
                            } catch (RemoteException e) {
                                e = e;
                                long j = lastModifiedSeconds;
                                File file2 = file;
                                try {
                                    Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
                                    this.mMtpObjectHandle = 0;
                                    if (fileList != null) {
                                    }
                                    releaseResources();
                                    Cursor cursor = fileList;
                                    return;
                                } catch (Throwable th) {
                                    th = th;
                                    this.mMtpObjectHandle = 0;
                                    if (fileList != null) {
                                    }
                                    releaseResources();
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                long j2 = lastModifiedSeconds;
                                File file3 = file;
                                this.mMtpObjectHandle = 0;
                                if (fileList != null) {
                                }
                                releaseResources();
                                throw th;
                            }
                        }
                        long j3 = lastModifiedSeconds;
                        File file4 = file;
                    } catch (RemoteException e2) {
                        e = e2;
                        long j4 = lastModifiedSeconds;
                        File file5 = file;
                        fileList = null;
                        Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
                        this.mMtpObjectHandle = 0;
                        if (fileList != null) {
                            fileList.close();
                        }
                        releaseResources();
                        Cursor cursor2 = fileList;
                        return;
                    } catch (Throwable th3) {
                        th = th3;
                        long j5 = lastModifiedSeconds;
                        File file6 = file;
                        fileList = null;
                        this.mMtpObjectHandle = 0;
                        if (fileList != null) {
                            fileList.close();
                        }
                        releaseResources();
                        throw th;
                    }
                } else {
                    prescan(key, false);
                    long j6 = lastModifiedSeconds;
                    File file7 = file;
                    try {
                        this.mClient.doScanFile(key, mediaFileType.mimeType, lastModifiedSeconds, file.length(), format == 12289, true, isNoMediaPath(path));
                    } catch (RemoteException e3) {
                        e = e3;
                    } catch (Throwable th4) {
                        th = th4;
                        fileList = null;
                        this.mMtpObjectHandle = 0;
                        if (fileList != null) {
                        }
                        releaseResources();
                        throw th;
                    }
                }
                Cursor fileList3 = fileList2;
                this.mMtpObjectHandle = 0;
                if (fileList3 != null) {
                    fileList3.close();
                }
                releaseResources();
            } catch (RemoteException e4) {
                e = e4;
                long j7 = lastModifiedSeconds;
                File file8 = file;
                fileList = null;
                Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                }
                releaseResources();
                Cursor cursor22 = fileList;
                return;
            } catch (Throwable th5) {
                th = th5;
                long j8 = lastModifiedSeconds;
                File file9 = file;
                fileList = null;
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                }
                releaseResources();
                throw th;
            }
            return;
        }
        ContentValues values = new ContentValues();
        values.put("_size", Long.valueOf(file.length()));
        values.put("date_modified", Long.valueOf(lastModifiedSeconds));
        try {
            this.mMediaProvider.update(MediaStore.Files.getMtpObjectsUri(this.mVolumeName), values, "_id=?", new String[]{Integer.toString(objectHandle)});
        } catch (RemoteException e5) {
            Log.e(TAG, "RemoteException in scanMtpFile", e5);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004e, code lost:
        if (r3 == null) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0051, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0040, code lost:
        if (r3 != null) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0042, code lost:
        r3.close();
     */
    public FileEntry makeEntryFor(String path) {
        Cursor c = null;
        try {
            c = this.mMediaProvider.query(this.mFilesUriNoNotify, FILES_PRESCAN_PROJECTION, "_data=?", new String[]{path}, null, null);
            if (c.moveToFirst()) {
                FileEntry fileEntry = new FileEntry(c.getLong(0), path, c.getLong(3), c.getInt(2), c.getInt(4));
                if (c != null) {
                    c.close();
                }
                return fileEntry;
            }
        } catch (RemoteException e) {
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    private int matchPaths(String path1, String path2) {
        int start2;
        String str = path1;
        String str2 = path2;
        int end1 = path1.length();
        int end2 = path2.length();
        int result = 0;
        int end12 = end1;
        while (true) {
            int end22 = end2;
            if (end12 <= 0 || end22 <= 0) {
                break;
            }
            int slash1 = str.lastIndexOf(47, end12 - 1);
            int slash2 = str2.lastIndexOf(47, end22 - 1);
            int backSlash1 = str.lastIndexOf(92, end12 - 1);
            int backSlash2 = str2.lastIndexOf(92, end22 - 1);
            int start1 = slash1 > backSlash1 ? slash1 : backSlash1;
            int start22 = slash2 > backSlash2 ? slash2 : backSlash2;
            int start12 = start1 < 0 ? 0 : start1 + 1;
            if (start22 < 0) {
                start2 = 0;
            } else {
                start2 = start22 + 1;
            }
            int length = end12 - start12;
            if (end22 - start2 == length) {
                int i = length;
                if (!str.regionMatches(true, start12, str2, start2, length)) {
                    break;
                }
                result++;
                end12 = start12 - 1;
                end2 = start2 - 1;
            } else {
                break;
            }
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
    public void cachePlaylistEntry(String line, String playListDirectory) {
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
        int i;
        fileList.moveToPosition(-1);
        do {
            if (!fileList.moveToNext()) {
                break;
            }
        } while (!matchEntries(fileList.getLong(0), fileList.getString(1)));
        int len = this.mPlaylistEntries.size();
        int index = 0;
        for (i = 0; i < len; i++) {
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
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
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
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                this.mPlaylistEntries.clear();
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (line.startsWith("File")) {
                        int equals = line.indexOf(61);
                        if (equals > 0) {
                            cachePlaylistEntry(line.substring(equals + 1), playListDirectory);
                        }
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
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e3);
                }
            }
            throw th;
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
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (fis != null) {
                fis.close();
            }
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e4) {
                    Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e4);
                }
            }
            throw th;
        }
    }

    private void processPlayList(FileEntry entry, Cursor fileList) throws RemoteException {
        Uri uri;
        Uri membersUri;
        String str;
        FileEntry fileEntry = entry;
        String path = fileEntry.mPath;
        ContentValues values = new ContentValues();
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0) {
            long rowId = fileEntry.mRowId;
            String name = values.getAsString(MidiDeviceInfo.PROPERTY_NAME);
            if (name == null) {
                name = values.getAsString("title");
                if (name == null) {
                    int lastDot = path.lastIndexOf(46);
                    if (lastDot < 0) {
                        str = path.substring(lastSlash + 1);
                    } else {
                        str = path.substring(lastSlash + 1, lastDot);
                    }
                    name = str;
                }
            }
            values.put(MidiDeviceInfo.PROPERTY_NAME, name);
            values.put("date_modified", Long.valueOf(fileEntry.mLastModified));
            if (rowId == 0) {
                values.put("_data", path);
                uri = this.mMediaProvider.insert(this.mPlaylistsUri, values);
                rowId = ContentUris.parseId(uri);
                membersUri = Uri.withAppendedPath(uri, "members");
            } else {
                uri = ContentUris.withAppendedId(this.mPlaylistsUri, rowId);
                this.mMediaProvider.update(uri, values, null, null);
                membersUri = Uri.withAppendedPath(uri, "members");
                this.mMediaProvider.delete(membersUri, null, null);
            }
            Uri membersUri2 = membersUri;
            int i = 0;
            String playListDirectory = path.substring(0, lastSlash + 1);
            MediaFile.MediaFileType mediaFileType = MediaFile.getFileType(path);
            if (mediaFileType != null) {
                i = mediaFileType.fileType;
            }
            int fileType = i;
            if (fileType == 44) {
                int i2 = fileType;
                MediaFile.MediaFileType mediaFileType2 = mediaFileType;
                processM3uPlayList(path, playListDirectory, membersUri2, values, fileList);
                return;
            }
            int fileType2 = fileType;
            MediaFile.MediaFileType mediaFileType3 = mediaFileType;
            if (fileType2 == 45) {
                processPlsPlayList(path, playListDirectory, membersUri2, values, fileList);
            } else if (fileType2 == 46) {
                processWplPlayList(path, playListDirectory, membersUri2, values, fileList);
            }
        } else {
            throw new IllegalArgumentException("bad path ");
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
            if (fileList == null) {
                return;
            }
        } catch (Throwable th) {
            if (fileList != null) {
                fileList.close();
            }
            throw th;
        }
        fileList.close();
    }

    public static void setStorageEject(String path) {
        if (path == null || path.equals("")) {
            Log.e(TAG, "setStorageEject path = null!");
            return;
        }
        if (sCurScanDIR.startsWith(path)) {
            Log.d(TAG, "setStorageEject curscanDir is ejected storage ! ");
            setStorageEjectFlag(true);
        }
    }

    public void close() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
            this.mMediaProvider.close();
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

    /* access modifiers changed from: protected */
    public void updateValues(String path, ContentValues contentValues) {
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00c2  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00dd A[SYNTHETIC] */
    public void deleteNomediaFile(StorageVolume[] volumes) {
        StorageVolume[] storageVolumeArr = volumes;
        if (storageVolumeArr != null) {
            for (StorageVolume storageVolume : storageVolumeArr) {
                String rootPath = storageVolume.getPath();
                for (String nomedia : sNomediaFilepath) {
                    ExternalStorageFileImpl nomediaFile = new ExternalStorageFileImpl(rootPath + nomedia);
                    try {
                        if (nomediaFile.exists()) {
                            if (!nomediaFile.isFile() || nomediaFile.length() <= MAX_NOMEDIA_SIZE) {
                                try {
                                    if (deleteFile(nomediaFile)) {
                                        if (DEBUG) {
                                            Log.w(TAG, "delete nomedia file success [" + nomediaPath + "]");
                                        }
                                    } else if (DEBUG) {
                                        Log.w(TAG, "delete nomedia file fail [" + nomediaPath + "]");
                                    }
                                } catch (IOException e) {
                                    if (!DEBUG) {
                                    }
                                }
                            } else {
                                if (DEBUG) {
                                    Log.w(TAG, "skip nomedia file [" + nomediaPath + "]  size:" + nomediaFile.length());
                                }
                            }
                        }
                    } catch (IOException e2) {
                        if (!DEBUG) {
                            Log.w(TAG, "delete nomedia file exception [" + nomediaPath + "]");
                        }
                    }
                }
            }
        }
    }

    private boolean deleteFile(File file) throws IOException {
        boolean result = true;
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            if (!file.delete()) {
                result = false;
            }
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File deleteFile : files) {
                    if (!deleteFile(deleteFile)) {
                        result = false;
                    }
                }
            }
            if (file.delete() == 0) {
                result = false;
            }
        }
        return result;
    }

    public void setStorageIdForCurScanDIR(String directory) {
        this.mStorageIdForCurScanDIR = getStorageId(directory);
    }

    private boolean isClonedProfile(UserInfo ui) {
        boolean z = false;
        if (ui == null) {
            return false;
        }
        if (ui.id != ActivityManager.getCurrentUser() && !ui.isManagedProfile()) {
            z = true;
        }
        return z;
    }

    private UserInfo getAppClonedUserInfo(Context context) {
        if (this.IS_SUPPORT_CLONE_APP) {
            List<UserInfo> profiles = ((UserManager) context.getSystemService(Context.USER_SERVICE)).getProfiles(context.getUserId());
            int uiCount = profiles == null ? 0 : profiles.size();
            for (int i = 0; i < uiCount; i++) {
                UserInfo ui = profiles.get(i);
                if (isClonedProfile(ui)) {
                    return ui;
                }
            }
        }
        return null;
    }

    private int getStorageId(String path) {
        File file = new File(path);
        StorageVolume vol = ((StorageManager) this.mContext.getSystemService(StorageManager.class)).getStorageVolume(file);
        if (vol == null) {
            UserInfo ui = getAppClonedUserInfo(this.mContext);
            vol = ui == null ? null : StorageManager.getStorageVolume(file, ui.id);
        }
        if (vol != null) {
            return getStorageId(vol);
        }
        Log.w(TAG, "Missing volume for " + path + "; assuming invalid");
        return 0;
    }

    private int getStorageId(StorageVolume vol) {
        if (vol.isPrimary()) {
            return SensorAdditionalInfo.TYPE_INTERNAL_TEMPERATURE;
        }
        String fsUuid = vol.getUuid();
        if (TextUtils.isEmpty(fsUuid)) {
            return 0;
        }
        int hash = 0;
        int fsUuidLength = fsUuid.length();
        for (int i = 0; i < fsUuidLength; i++) {
            hash = (31 * hash) + fsUuid.charAt(i);
        }
        int hash2 = ((hash << 16) ^ hash) & Color.RED;
        if (hash2 == 0) {
            hash2 = 131072;
        }
        if (hash2 == 65536) {
            hash2 = 131072;
        }
        if (hash2 == -65536) {
            hash2 = -131072;
        }
        return hash2 | 1;
    }
}
