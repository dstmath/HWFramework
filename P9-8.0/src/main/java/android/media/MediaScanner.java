package android.media;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
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
import android.database.Cursor;
import android.database.SQLException;
import android.drm.DrmManagerClient;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.hwtheme.HwThemeManager;
import android.media.MediaCodec.MetricsConstants;
import android.media.MediaFile.MediaFileType;
import android.media.midi.MidiDeviceInfo;
import android.mtp.MtpConstants;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiScanLog;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
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
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final boolean ENABLE_BULK_INSERTS = true;
    private static final int FILES_BLACKLIST_ID_COLUMN_INDEX = 0;
    private static final int FILES_BLACKLIST_MEDIA_TYPE_COLUMN_INDEX = 2;
    private static final int FILES_BLACKLIST_PATH_COLUMN_INDEX = 1;
    private static final String[] FILES_BLACKLIST_PROJECTION_MEDIA = new String[]{DownloadManager.COLUMN_ID, "_data", DownloadManager.COLUMN_MEDIA_TYPE};
    private static final int FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX = 3;
    private static final int FILES_PRESCAN_FORMAT_COLUMN_INDEX = 2;
    private static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
    private static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
    private static final String[] FILES_PRESCAN_PROJECTION = new String[]{DownloadManager.COLUMN_ID, "_data", "format", "date_modified"};
    private static final String[] FILES_PRESCAN_PROJECTION_MEDIA = new String[]{DownloadManager.COLUMN_ID, "_data"};
    private static final String HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN = "special_file_type";
    private static final String[] ID3_GENRES = new String[]{"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie", "Britpop", null, "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian", "Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "Synthpop"};
    private static final int ID_PLAYLISTS_COLUMN_INDEX = 0;
    private static final String[] ID_PROJECTION = new String[]{DownloadManager.COLUMN_ID};
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
    private static final int MAX_ENTRY_SIZE = 40000;
    private static long MAX_NOMEDIA_SIZE = 1024;
    private static final String MUSIC_DIR = "/music/";
    private static final String NOTIFICATIONS_DIR = "/notifications/";
    private static final int PATH_PLAYLISTS_COLUMN_INDEX = 1;
    private static final String[] PLAYLIST_MEMBERS_PROJECTION = new String[]{"playlist_id"};
    private static final String PODCAST_DIR = "/podcasts/";
    private static final String RINGTONES_DIR = "/ringtones/";
    public static final String SCANNED_BUILD_PREFS_NAME = "MediaScanBuild";
    private static final int SQL_MEDIA_TYPE_BLACKLIST = 10;
    private static final int SQL_MEDIA_TYPE_IMGAGE = 1;
    private static final int SQL_QUERY_COUNT = 100;
    private static final String SQL_QUERY_LIMIT = "1000";
    private static final String SYSTEM_SOUNDS_DIR = "/system/media/audio";
    private static final String TAG = "MediaScanner";
    private static HashMap<String, String> mMediaPaths = new HashMap();
    private static HashMap<String, String> mNoMediaPaths = new HashMap();
    public static final Set sBlackList = new HashSet();
    private static String sLastInternalScanFingerprint;
    private static final String[] sNomediaFilepath = new String[]{"/.nomedia", "/DCIM/.nomedia", "/DCIM/Camera/.nomedia", "/Pictures/.nomedia", "/Pictures/Screenshots/.nomedia", "/tencent/.nomedia", "/tencent/MicroMsg/.nomedia", "/tencent/MicroMsg/Weixin/.nomedia", "/tencent/QQ_Images/.nomedia"};
    private final Uri mAudioUri;
    private final Options mBitmapOptions = new Options();
    private boolean mBlackListFlag = false;
    private final MyMediaScannerClient mClient = new MyMediaScannerClient();
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private final AtomicBoolean mClosed = new AtomicBoolean();
    private final Context mContext;
    private String mDefaultAlarmAlertFilename;
    private boolean mDefaultAlarmSet;
    private String mDefaultNotificationFilename;
    private boolean mDefaultNotificationSet;
    private String mDefaultRingtoneFilename;
    private boolean mDefaultRingtoneSet;
    private DrmManagerClient mDrmManagerClient = null;
    private String mExtStroagePath;
    private HashMap<String, FileEntry> mFileCache;
    private final Uri mFilesUri;
    private final Uri mFilesUriNoNotify;
    private final Uri mImagesUri;
    private boolean mIsImageType = false;
    private MediaInserter mMediaInserter;
    private ContentProviderClient mMediaProvider;
    private int mMtpObjectHandle;
    private long mNativeContext;
    private int mOriginalCount;
    private final String mPackageName;
    private final ArrayList<FileEntry> mPlayLists = new ArrayList();
    private final ArrayList<PlaylistEntry> mPlaylistEntries = new ArrayList();
    private final Uri mPlaylistsUri;
    private final boolean mProcessGenres;
    private final boolean mProcessPlaylists;
    private boolean mSkipExternelQuery = false;
    private final Uri mVideoUri;
    private final String mVolumeName;

    private static class FileEntry {
        int mFormat;
        long mLastModified;
        boolean mLastModifiedChanged = false;
        String mPath;
        long mRowId;

        FileEntry(long rowId, String path, long lastModified, int format) {
            this.mRowId = rowId;
            this.mPath = path;
            this.mLastModified = lastModified;
            this.mFormat = format;
        }

        public String toString() {
            return this.mPath + " mRowId: " + this.mRowId;
        }
    }

    static class MediaBulkDeleter {
        final Uri mBaseUri;
        final ContentProviderClient mProvider;
        ArrayList<String> whereArgs = new ArrayList(100);
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
            this.whereArgs.add(ProxyInfo.LOCAL_EXCL_LIST + id);
            if (this.whereArgs.size() > 100) {
                flush();
            }
        }

        public void flush() throws RemoteException {
            int size = this.whereArgs.size();
            if (size > 0) {
                int numrows = this.mProvider.delete(this.mBaseUri, "_id IN (" + this.whereClause.toString() + ")", (String[]) this.whereArgs.toArray(new String[size]));
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
        private String mTitle;
        private int mTrack;
        private int mWidth;
        private String mWriter;
        private int mYear;

        public MyMediaScannerClient() {
            this.mDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        /* JADX WARNING: Missing block: B:5:0x0024, code:
            if (r16.endsWith(".ismv") != false) goto L_0x0026;
     */
        /* JADX WARNING: Missing block: B:32:0x0095, code:
            if ((r16.startsWith(android.media.MediaScanner.-get12(r15.this$0)) ^ 1) != 0) goto L_0x0097;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public FileEntry beginFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            this.mMimeType = mimeType;
            this.mFileType = 0;
            this.mFileSize = fileSize;
            this.mIsDrm = false;
            if (path != null) {
                if (!path.endsWith(".isma")) {
                }
                this.mIsDrm = true;
            }
            if (!isDirectory) {
                if (!noMedia && MediaScanner.isNoMediaFile(path)) {
                    noMedia = true;
                }
                this.mNoMedia = noMedia;
                if (mimeType != null) {
                    this.mFileType = MediaFile.getFileTypeForMimeType(mimeType);
                }
                if (this.mFileType == 0) {
                    MediaFileType mediaFileType = MediaFile.getFileType(path);
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
            }
            String key = path;
            FileEntry entry = (FileEntry) MediaScanner.this.mFileCache.remove(path);
            if (entry == null) {
                if (MediaScanner.this.mSkipExternelQuery) {
                }
                entry = MediaScanner.this.makeEntryFor(path);
            }
            long delta = entry != null ? lastModified - entry.mLastModified : 0;
            boolean wasModified = delta > 1 || delta < -1;
            if (entry == null || wasModified) {
                if (wasModified) {
                    entry.mLastModified = lastModified;
                } else {
                    entry = new FileEntry(0, path, lastModified, isDirectory ? 12289 : 0);
                }
                entry.mLastModifiedChanged = true;
            }
            if (MediaScanner.this.mProcessPlaylists && MediaFile.isPlayListFileType(this.mFileType)) {
                MediaScanner.this.mPlayLists.add(entry);
                return null;
            }
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
            this.mIsAlbumMessy = false;
            this.mIsArtistMessy = false;
            this.mIsTitleMessy = false;
            return entry;
        }

        public void setBlackListFlag(boolean flag) {
            MediaScanner.this.mBlackListFlag = true;
        }

        public void scanFile(String path, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            doScanFile(path, null, lastModified, fileSize, isDirectory, false, noMedia);
        }

        /* JADX WARNING: Missing block: B:14:0x0044, code:
            if (doesPathHaveFilename(r5.mPath, android.media.MediaScanner.-get7(r21.this$0)) == false) goto L_0x0046;
     */
        /* JADX WARNING: Missing block: B:18:0x0060, code:
            if (doesPathHaveFilename(r5.mPath, android.media.MediaScanner.-get9(r21.this$0)) == false) goto L_0x0062;
     */
        /* JADX WARNING: Missing block: B:22:0x007c, code:
            if (doesPathHaveFilename(r5.mPath, android.media.MediaScanner.-get5(r21.this$0)) != false) goto L_0x007e;
     */
        /* JADX WARNING: Missing block: B:23:0x007e, code:
            android.util.Log.w(android.media.MediaScanner.TAG, "forcing rescan , since ringtone setting didn't finish");
            r29 = true;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Uri doScanFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean scanAlways, boolean noMedia) {
            Uri result = null;
            try {
                FileEntry entry = beginFile(path, mimeType, lastModified, fileSize, isDirectory, noMedia);
                if (entry == null) {
                    return null;
                }
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    entry.mRowId = 0;
                }
                if (entry.mPath != null) {
                    if (!MediaScanner.this.mDefaultNotificationSet) {
                    }
                    if (!MediaScanner.this.mDefaultRingtoneSet) {
                    }
                    if (!MediaScanner.this.mDefaultAlarmSet) {
                    }
                    if (MediaScanner.isSystemSoundWithMetadata(entry.mPath) && (Build.FINGERPRINT.equals(MediaScanner.sLastInternalScanFingerprint) ^ 1) != 0) {
                        Log.i(MediaScanner.TAG, "forcing rescan of " + entry.mPath + " since build fingerprint changed");
                        scanAlways = true;
                    }
                }
                if (entry != null && (entry.mLastModifiedChanged || scanAlways || this.mIsDrm)) {
                    if (noMedia) {
                        result = endFile(entry, false, false, false, false, false);
                    } else {
                        String lowpath = path.toLowerCase(Locale.ROOT);
                        boolean ringtones = lowpath.indexOf(MediaScanner.RINGTONES_DIR) > 0;
                        boolean notifications = lowpath.indexOf(MediaScanner.NOTIFICATIONS_DIR) > 0;
                        boolean alarms = lowpath.indexOf(MediaScanner.ALARMS_DIR) > 0;
                        boolean podcasts = lowpath.indexOf(MediaScanner.PODCAST_DIR) > 0;
                        boolean music = lowpath.indexOf(MediaScanner.MUSIC_DIR) <= 0 ? (ringtones || (notifications ^ 1) == 0 || (alarms ^ 1) == 0) ? false : podcasts ^ 1 : true;
                        ringtones |= HwThemeManager.isTRingtones(lowpath);
                        notifications |= HwThemeManager.isTNotifications(lowpath);
                        alarms |= HwThemeManager.isTAlarms(lowpath);
                        boolean isaudio = MediaFile.isAudioFileType(this.mFileType);
                        boolean isvideo = MediaFile.isVideoFileType(this.mFileType);
                        boolean isimage = MediaFile.isImageFileType(this.mFileType);
                        MediaScanner.this.mIsImageType = isimage;
                        if (isaudio || isvideo || isimage) {
                            path = Environment.maybeTranslateEmulatedPathToInternal(new File(path)).getAbsolutePath();
                        }
                        if (isaudio || isvideo) {
                            MediaScanner.this.processFile(path, mimeType, this);
                        }
                        if (isimage) {
                            processImageFile(path);
                        }
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
                        result = endFile(entry, ringtones, notifications, alarms, music, podcasts);
                    }
                }
                MediaScanner.this.mBlackListFlag = false;
                MediaScanner.this.mIsImageType = false;
                return result;
            } catch (RemoteException e) {
                Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
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
            int result = ch - 48;
            while (start2 < length) {
                start = start2 + 1;
                ch = s.charAt(start2);
                if (ch < '0' || ch > '9') {
                    return result;
                }
                result = (result * 10) + (ch - 48);
                start2 = start;
            }
            return result;
        }

        public void handleStringTag(String name, String value) {
            boolean z = true;
            boolean isAlbum = !name.equalsIgnoreCase("album") ? name.startsWith("album;") : true;
            boolean isArtist = !name.equalsIgnoreCase("artist") ? name.startsWith("artist;") : true;
            boolean isTitle = !name.equalsIgnoreCase("title") ? name.startsWith("title;") : true;
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
                        if (genreIndex >= (short) 0) {
                            if (genreIndex < MediaScanner.ID3_GENRES.length && MediaScanner.ID3_GENRES[genreIndex] != null) {
                                return MediaScanner.ID3_GENRES[genreIndex];
                            }
                            if (genreIndex == (short) 255) {
                                return null;
                            }
                            if (genreIndex >= (short) 255 || i + 1 >= length) {
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

        private void processImageFile(String path) {
            try {
                MediaScanner.this.mBitmapOptions.outWidth = 0;
                MediaScanner.this.mBitmapOptions.outHeight = 0;
                if (HwFrameworkFactory.getHwMediaScannerManager().isBitmapSizeTooLarge(path)) {
                    this.mWidth = -1;
                    this.mHeight = -1;
                    return;
                }
                BitmapFactory.decodeFile(path, MediaScanner.this.mBitmapOptions);
                this.mWidth = MediaScanner.this.mBitmapOptions.outWidth;
                this.mHeight = MediaScanner.this.mBitmapOptions.outHeight;
            } catch (Throwable th) {
            }
        }

        public void setMimeType(String mimeType) {
            if (!"audio/mp4".equals(this.mMimeType) || !mimeType.startsWith(MetricsConstants.MODE_VIDEO)) {
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
                String str;
                String str2;
                if (MediaFile.isVideoFileType(this.mFileType)) {
                    str = "artist";
                    str2 = (this.mArtist == null || this.mArtist.length() <= 0) ? "<unknown>" : this.mArtist;
                    map.put(str, str2);
                    str = "album";
                    str2 = (this.mAlbum == null || this.mAlbum.length() <= 0) ? "<unknown>" : this.mAlbum;
                    map.put(str, str2);
                    map.put("duration", Integer.valueOf(this.mDuration));
                    if (resolution != null) {
                        map.put("resolution", resolution);
                    }
                    if (this.mDate > Time_1970) {
                        map.put("datetaken", Long.valueOf(this.mDate));
                    }
                } else if (!MediaFile.isImageFileType(this.mFileType) && MediaFile.isAudioFileType(this.mFileType)) {
                    String str3 = "artist";
                    str2 = (this.mArtist == null || this.mArtist.length() <= 0) ? "<unknown>" : this.mArtist;
                    map.put(str3, str2);
                    str3 = "album_artist";
                    if (this.mAlbumArtist == null || this.mAlbumArtist.length() <= 0) {
                        str2 = null;
                    } else {
                        str2 = this.mAlbumArtist;
                    }
                    map.put(str3, str2);
                    str = "album";
                    str2 = (this.mAlbum == null || this.mAlbum.length() <= 0) ? "<unknown>" : this.mAlbum;
                    map.put(str, str2);
                    map.put("composer", this.mComposer);
                    map.put("genre", this.mGenre);
                    if (this.mYear != 0) {
                        map.put("year", Integer.valueOf(this.mYear));
                    }
                    map.put("track", Integer.valueOf(this.mTrack));
                    map.put("duration", Integer.valueOf(this.mDuration));
                    map.put("compilation", Integer.valueOf(this.mCompilation));
                }
            }
            return map;
        }

        /* JADX WARNING: Removed duplicated region for block: B:20:0x007f  */
        /* JADX WARNING: Missing block: B:44:0x0167, code:
            if (doesPathHaveFilename(r35.mPath, android.media.MediaScanner.-get7(r34.this$0)) != false) goto L_0x0169;
     */
        /* JADX WARNING: Missing block: B:121:0x0355, code:
            if (doesPathHaveFilename(r35.mPath, android.media.MediaScanner.-get9(r34.this$0)) != false) goto L_0x0357;
     */
        /* JADX WARNING: Missing block: B:130:0x0395, code:
            if (doesPathHaveFilename(r35.mPath, android.media.MediaScanner.-get5(r34.this$0)) != false) goto L_0x0397;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private Uri endFile(FileEntry entry, boolean ringtones, boolean notifications, boolean alarms, boolean music, boolean podcasts) throws RemoteException {
            if (this.mArtist == null || this.mArtist.length() == 0) {
                this.mArtist = this.mAlbumArtist;
            }
            ContentValues values = toValues();
            String title = values.getAsString("title");
            if (title == null || TextUtils.isEmpty(title.trim())) {
                title = MediaFile.getFileTitle(values.getAsString("_data"));
                values.put("title", title);
            }
            if ("<unknown>".equals(values.getAsString("album"))) {
                String album = values.getAsString("_data");
                int lastSlash = album.lastIndexOf(47);
                if (lastSlash >= 0) {
                    int previousSlash = 0;
                    while (true) {
                        int idx = album.indexOf(47, previousSlash + 1);
                        if (idx >= 0 && idx < lastSlash) {
                            previousSlash = idx;
                        } else if (previousSlash != 0) {
                            ContentValues contentValues = values;
                            contentValues.put("album", album.substring(previousSlash + 1, lastSlash));
                        }
                    }
                    if (previousSlash != 0) {
                    }
                }
            }
            long rowId = entry.mRowId;
            if (MediaFile.isAudioFileType(this.mFileType) && (rowId == 0 || MediaScanner.this.mMtpObjectHandle != 0)) {
                values.put("is_ringtone", Boolean.valueOf(ringtones));
                values.put("is_notification", Boolean.valueOf(notifications));
                values.put("is_alarm", Boolean.valueOf(alarms));
                values.put("is_music", Boolean.valueOf(music));
                values.put("is_podcast", Boolean.valueOf(podcasts));
            } else if ((this.mFileType == 34 || MediaFile.isRawImageFileType(this.mFileType)) && (this.mNoMedia ^ 1) != 0) {
                ExifInterface exifInterface = null;
                try {
                    exifInterface = new ExifInterface(entry.mPath);
                } catch (IOException e) {
                }
                if (exifInterface != null) {
                    float[] latlng = new float[2];
                    boolean mHasLatLong = exifInterface.getLatLong(latlng);
                    if (mHasLatLong) {
                        values.put("latitude", Float.valueOf(latlng[0]));
                        values.put("longitude", Float.valueOf(latlng[1]));
                    }
                    long time = exifInterface.getGpsDateTime();
                    if (time == -1 || !mHasLatLong) {
                        time = exifInterface.getDateTime();
                        if (time != -1 && Math.abs((this.mLastModified * 1000) - time) >= AlarmManager.INTERVAL_DAY) {
                            values.put("datetaken", Long.valueOf(time));
                        }
                    } else {
                        values.put("datetaken", Long.valueOf(time));
                    }
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                    if (orientation != -1) {
                        int degree;
                        switch (orientation) {
                            case 3:
                                degree = 180;
                                break;
                            case 6:
                                degree = 90;
                                break;
                            case 8:
                                degree = 270;
                                break;
                            default:
                                degree = 0;
                                break;
                        }
                        values.put("orientation", Integer.valueOf(degree));
                    }
                    scannerSpecialImageType(values, exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
                }
                HwFrameworkFactory.getHwMediaScannerManager().initializeHwVoiceAndFocus(entry.mPath, values);
            }
            MediaScanner.this.updateValues(entry.mPath, values);
            Uri tableUri = MediaScanner.this.mFilesUri;
            MediaInserter inserter = MediaScanner.this.mMediaInserter;
            if (!(this.mNoMedia || (MediaScanner.this.mBlackListFlag && (MediaScanner.this.mIsImageType ^ 1) == 0))) {
                if (MediaFile.isVideoFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mVideoUri;
                } else if (MediaFile.isImageFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mImagesUri;
                } else if (MediaFile.isAudioFileType(this.mFileType)) {
                    tableUri = MediaScanner.this.mAudioUri;
                }
            }
            Uri result = null;
            boolean needToSetSettings = false;
            boolean needToSetSettings2 = false;
            if (notifications && (MediaScanner.this.mDefaultNotificationSet ^ 1) != 0) {
                if (!TextUtils.isEmpty(MediaScanner.this.mDefaultNotificationFilename)) {
                }
                needToSetSettings = true;
            } else if (ringtones) {
                if (MediaScanner.this.mDefaultRingtoneSet || !TextUtils.isEmpty(MediaScanner.this.mDefaultRingtoneFilename)) {
                }
                needToSetSettings = true;
                needToSetSettings2 = HwFrameworkFactory.getHwMediaScannerManager().hwNeedSetSettings(entry.mPath);
            } else if (alarms && (MediaScanner.this.mDefaultAlarmSet ^ 1) != 0) {
                if (!TextUtils.isEmpty(MediaScanner.this.mDefaultAlarmAlertFilename)) {
                }
                needToSetSettings = true;
            }
            if (rowId == 0) {
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    values.put("media_scanner_new_object_id", Integer.valueOf(MediaScanner.this.mMtpObjectHandle));
                }
                if (tableUri == MediaScanner.this.mFilesUri) {
                    int format = entry.mFormat;
                    if (format == 0) {
                        format = MediaFile.getFormatCode(entry.mPath, this.mMimeType);
                    }
                    values.put("format", Integer.valueOf(format));
                }
                if (MediaScanner.this.mBlackListFlag && MediaScanner.this.mIsImageType) {
                    values.put(DownloadManager.COLUMN_MEDIA_TYPE, Integer.valueOf(10));
                }
                if (inserter == null || needToSetSettings || needToSetSettings2) {
                    if (inserter != null) {
                        inserter.flushAll();
                    }
                    result = MediaScanner.this.mMediaProvider.insert(tableUri, values);
                } else if (entry.mFormat == 12289) {
                    inserter.insertwithPriority(tableUri, values);
                } else {
                    inserter.insert(tableUri, values);
                }
                if (result != null) {
                    rowId = ContentUris.parseId(result);
                    entry.mRowId = rowId;
                }
            } else {
                result = ContentUris.withAppendedId(tableUri, rowId);
                values.remove("_data");
                int mediaType = 0;
                if (MediaScanner.this.mBlackListFlag && MediaScanner.this.mIsImageType) {
                    values.put(DownloadManager.COLUMN_MEDIA_TYPE, Integer.valueOf(10));
                } else if (!MediaScanner.isNoMediaPath(entry.mPath)) {
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
                MediaScanner.this.mMediaProvider.update(result, values, null, null);
            }
            if (needToSetSettings) {
                if (notifications) {
                    setRingtoneIfNotSet("notification_sound", tableUri, rowId);
                    MediaScanner.this.mDefaultNotificationSet = true;
                } else if (ringtones) {
                    setRingtoneIfNotSet("ringtone", tableUri, rowId);
                    MediaScanner.this.mDefaultRingtoneSet = true;
                } else if (alarms) {
                    setRingtoneIfNotSet("alarm_alert", tableUri, rowId);
                    MediaScanner.this.mDefaultAlarmSet = true;
                }
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
            values.put("is_hdr", Integer.valueOf(hdrType));
            if (exifDescription != null && exifDescription.length() >= 3) {
                String subString = exifDescription.substring(0, 3);
                if (MediaScanner.IMAGE_TYPE_PORTRAIT_FRONT.equals(subString)) {
                    values.put(MediaScanner.HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, Integer.valueOf(30));
                } else if (MediaScanner.IMAGE_TYPE_PORTRAIT_REAR.equals(subString)) {
                    values.put(MediaScanner.HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, Integer.valueOf(31));
                } else if (MediaScanner.IMAGE_TYPE_BEAUTY_FRONT.equals(subString)) {
                    values.put(MediaScanner.HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, Integer.valueOf(40));
                } else if (MediaScanner.IMAGE_TYPE_BEAUTY_REAR.equals(subString)) {
                    values.put(MediaScanner.HW_SPECIAL_FILE_TYPE_IMAGE_COLUMN, Integer.valueOf(41));
                }
            }
        }

        private boolean doesPathHaveFilename(String path, String filename) {
            int pathFilenameStart = path.lastIndexOf(File.separatorChar) + 1;
            int filenameLength = filename.length();
            if (path.regionMatches(pathFilenameStart, filename, 0, filenameLength) && pathFilenameStart + filenameLength == path.length()) {
                return true;
            }
            return false;
        }

        private void setRingtoneIfNotSet(String settingName, Uri uri, long rowId) {
            Log.v(MediaScanner.TAG, "setRingtoneIfNotSet.name:" + settingName + " value:" + uri + rowId);
            if (!MediaScanner.this.wasRingtoneAlreadySet(settingName)) {
                ContentResolver cr = MediaScanner.this.mContext.getContentResolver();
                if (TextUtils.isEmpty(System.getString(cr, settingName))) {
                    Log.v(MediaScanner.TAG, "setSetting when NotSet");
                    Uri settingUri = System.getUriFor(settingName);
                    RingtoneManager.setActualDefaultRingtoneUri(MediaScanner.this.mContext, RingtoneManager.getDefaultType(settingUri), ContentUris.withAppendedId(uri, rowId));
                }
                System.putInt(cr, MediaScanner.this.settingSetIndicatorName(settingName), 1);
            }
        }

        private int getFileTypeFromDrm(String path) {
            if (!MediaScanner.this.isDrmEnabled()) {
                return 0;
            }
            int resultFileType = 0;
            if (MediaScanner.this.mDrmManagerClient == null) {
                MediaScanner.this.mDrmManagerClient = new DrmManagerClient(MediaScanner.this.mContext);
            }
            if (MediaScanner.this.mDrmManagerClient.canHandle(path, null)) {
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

        /* synthetic */ PlaylistEntry(PlaylistEntry -this0) {
            this();
        }

        private PlaylistEntry() {
        }
    }

    class WplHandler implements ElementListener {
        final ContentHandler handler;
        String playListDirectory;

        public WplHandler(String playListDirectory, Uri uri, Cursor fileList) {
            this.playListDirectory = playListDirectory;
            RootElement root = new RootElement("smil");
            root.getChild(TtmlUtils.TAG_BODY).getChild(BatteryManager.EXTRA_SEQUENCE).getChild("media").setElementListener(this);
            this.handler = root.getContentHandler();
        }

        public void start(Attributes attributes) {
            String path = attributes.getValue(ProxyInfo.LOCAL_EXCL_LIST, "src");
            if (path != null) {
                MediaScanner.this.cachePlaylistEntry(path, this.playListDirectory);
            }
        }

        public void end() {
        }

        ContentHandler getContentHandler() {
            return this.handler;
        }
    }

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup();

    private native void processDirectory(String str, MediaScannerClient mediaScannerClient);

    private native void processFile(String str, String str2, MediaScannerClient mediaScannerClient);

    private static native void releaseBlackList();

    private static native void setBlackList(String str);

    private native void setLocale(String str);

    public native void addSkipCustomDirectory(String str, int i);

    public native void clearSkipCustomDirectory();

    public native byte[] extractAlbumArt(FileDescriptor fileDescriptor);

    public native void setExteLen(int i);

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
        this.mAudioUri = Media.getContentUri(volumeName);
        this.mVideoUri = Video.Media.getContentUri(volumeName);
        this.mImagesUri = Images.Media.getContentUri(volumeName);
        this.mFilesUri = Files.getContentUri(volumeName);
        this.mFilesUriNoNotify = this.mFilesUri.buildUpon().appendQueryParameter("nonotify", "1").build();
        if (volumeName.equals(INTERNAL_VOLUME)) {
            this.mProcessPlaylists = false;
            this.mProcessGenres = false;
            this.mPlaylistsUri = null;
        } else {
            this.mProcessPlaylists = true;
            this.mProcessGenres = true;
            this.mPlaylistsUri = Playlists.getContentUri(volumeName);
            this.mExtStroagePath = HwFrameworkFactory.getHwMediaScannerManager().getExtSdcardVolumePath(this.mContext);
            this.mSkipExternelQuery = HwFrameworkFactory.getHwMediaScannerManager().isSkipExtSdcard(this.mMediaProvider, this.mExtStroagePath, this.mPackageName, this.mFilesUriNoNotify);
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
        this.mCloseGuard.open("close");
    }

    private void setDefaultRingtoneFileNames() {
        this.mDefaultRingtoneFilename = SystemProperties.get("ro.config.ringtone");
        HwFrameworkFactory.getHwMediaScannerManager().setHwDefaultRingtoneFileNames();
        this.mDefaultNotificationFilename = SystemProperties.get("ro.config.notification_sound");
        this.mDefaultAlarmAlertFilename = SystemProperties.get("ro.config.alarm_alert");
    }

    private boolean isDrmEnabled() {
        String prop = SystemProperties.get("drm.service.enabled");
        return prop != null ? prop.equals("true") : false;
    }

    public static void updateBlackList(Set blackLists) {
        sBlackList.clear();
        releaseBlackList();
        for (String black : blackLists) {
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

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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

    public void updateBlackListFile() {
        String where = "_id>? and (media_type=10 or media_type=1)";
        long lastId = Long.MIN_VALUE;
        Cursor cursor = null;
        String[] selectionArgs = new String[]{WifiEnterpriseConfig.ENGINE_DISABLE};
        int count = 0;
        Uri limitUri = this.mFilesUri.buildUpon().appendQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT, SQL_QUERY_LIMIT).build();
        int fileCount = 0;
        long start = System.currentTimeMillis();
        ArrayList<ContentProviderOperation> ops = new ArrayList();
        while (count < 100) {
            try {
                selectionArgs[0] = ProxyInfo.LOCAL_EXCL_LIST + lastId;
                count++;
                if (cursor != null) {
                    cursor.close();
                }
                cursor = this.mMediaProvider.query(limitUri, FILES_BLACKLIST_PROJECTION_MEDIA, where, selectionArgs, DownloadManager.COLUMN_ID, null);
                if (cursor != null) {
                    if (cursor.getCount() == 0) {
                        break;
                    }
                    while (cursor.moveToNext()) {
                        long rowId = cursor.getLong(0);
                        String path = cursor.getString(1);
                        int mediatype = cursor.getInt(2);
                        lastId = rowId;
                        fileCount++;
                        ContentValues values = new ContentValues();
                        boolean isBlackListFlag = isBlackListPath(path, getRootDirLength(path));
                        if (isBlackListFlag && mediatype == 1) {
                            values.put(DownloadManager.COLUMN_MEDIA_TYPE, Integer.valueOf(10));
                            ops.add(ContentProviderOperation.newUpdate(ContentUris.withAppendedId(this.mImagesUri, rowId)).withValues(values).build());
                        } else if (!isBlackListFlag && mediatype == 10) {
                            values.put(DownloadManager.COLUMN_MEDIA_TYPE, Integer.valueOf(1));
                            ops.add(ContentProviderOperation.newUpdate(ContentUris.withAppendedId(this.mImagesUri, rowId)).withValues(values).build());
                        }
                    }
                } else {
                    break;
                }
            } catch (SQLException e) {
                Log.e(TAG, "updateBlackListFile SQLException ! ");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (OperationApplicationException e2) {
                Log.e(TAG, "MediaProvider upate all file Exception when use the applyBatch ! ");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (RemoteException e3) {
                Log.e(TAG, "updateBlackListFile RemoteException ! ");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        Log.d(TAG, "updateBlackListFile filecount = " + fileCount);
        if (count == 100) {
            Log.d(TAG, "SQL query exceed the limit 100 !");
        }
        this.mMediaProvider.applyBatch(ops);
        if (cursor != null) {
            cursor.close();
        }
        Log.d(TAG, "updateBlackListFile total time = " + (System.currentTimeMillis() - start));
    }

    private static boolean isSystemSoundWithMetadata(String path) {
        if (path.startsWith("/system/media/audio/alarms/") || path.startsWith("/system/media/audio/ringtones/") || path.startsWith("/system/media/audio/notifications/")) {
            return true;
        }
        return false;
    }

    private String settingSetIndicatorName(String base) {
        return base + "_set";
    }

    private boolean wasRingtoneAlreadySet(String name) {
        boolean z = false;
        try {
            if (System.getInt(this.mContext.getContentResolver(), settingSetIndicatorName(name)) != 0) {
                z = true;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void deleteFilesIfPossible() {
        String where = "_data is null and media_type != 4";
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, where, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                this.mMediaProvider.delete(this.mFilesUri, where, null);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RemoteException e) {
            Log.d(TAG, "deleteFilesIfPossible catch RemoteException ");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private void prescanOnlyMedia(Uri uriTable, boolean isOnlyMedia) throws RemoteException {
        hwPrescan(null, true, uriTable, isOnlyMedia);
    }

    private void hwPrescan(String filePath, boolean prescanFiles, Uri uriTable, boolean isOnlyMedia) throws RemoteException {
        String where;
        String[] selectionArgs;
        Cursor c = null;
        this.mPlayLists.clear();
        if (this.mFileCache == null) {
            this.mFileCache = new HashMap();
        } else {
            this.mFileCache.clear();
        }
        if (filePath != null) {
            where = "_id>? AND _data=?";
            selectionArgs = new String[]{ProxyInfo.LOCAL_EXCL_LIST, filePath};
        } else {
            where = "_id>?";
            selectionArgs = new String[]{ProxyInfo.LOCAL_EXCL_LIST};
        }
        Builder builder = uriTable.buildUpon();
        builder.appendQueryParameter("deletedata", "false");
        MediaBulkDeleter mediaBulkDeleter = new MediaBulkDeleter(this.mMediaProvider, builder.build());
        if (prescanFiles) {
            long lastId = Long.MIN_VALUE;
            try {
                Uri limitUri = uriTable.buildUpon().appendQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT, SQL_QUERY_LIMIT).build();
                deleteFilesIfPossible();
                int count = 0;
                while (true) {
                    selectionArgs[0] = ProxyInfo.LOCAL_EXCL_LIST + lastId;
                    if (c != null) {
                        c.close();
                        c = null;
                    }
                    c = this.mMediaProvider.query(limitUri, isOnlyMedia ? FILES_PRESCAN_PROJECTION_MEDIA : FILES_PRESCAN_PROJECTION, where, selectionArgs, DownloadManager.COLUMN_ID, null);
                    if (c == null || c.getCount() == 0) {
                        break;
                    }
                    int format = 0;
                    long lastModified = 0;
                    while (c.moveToNext()) {
                        long rowId = c.getLong(0);
                        String path = c.getString(1);
                        if (!isOnlyMedia) {
                            format = c.getInt(2);
                            lastModified = c.getLong(3);
                        }
                        lastId = rowId;
                        if (path != null && path.startsWith("/")) {
                            boolean exists = false;
                            try {
                                exists = Os.access(path, OsConstants.F_OK);
                            } catch (ErrnoException e) {
                            }
                            if (!exists) {
                                if ((MtpConstants.isAbstractObject(format) ^ 1) != 0) {
                                    MediaFileType mediaFileType = MediaFile.getFileType(path);
                                    if (!MediaFile.isPlayListFileType(mediaFileType == null ? 0 : mediaFileType.fileType)) {
                                        mediaBulkDeleter.delete(rowId);
                                        if (path.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                                            mediaBulkDeleter.flush();
                                            this.mMediaProvider.call("unhide", new File(path).getParent(), null);
                                        }
                                    }
                                }
                            }
                            if (!isOnlyMedia && count < 40000) {
                                String key = path;
                                this.mFileCache.put(path, new FileEntry(rowId, path, lastModified, format));
                            }
                            count++;
                        }
                    }
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
                mediaBulkDeleter.flush();
            }
        }
        if (c != null) {
            c.close();
        }
        mediaBulkDeleter.flush();
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
        c = this.mMediaProvider.query(this.mImagesUri, new String[]{"COUNT(*)"}, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                this.mOriginalCount = c.getInt(0);
            }
            c.close();
        }
    }

    private void prescan(String filePath, boolean prescanFiles) throws RemoteException {
        hwPrescan(filePath, prescanFiles, this.mFilesUri, false);
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

    public void scanDirectories(String[] directories) {
        boolean flag = false;
        try {
            int i;
            for (StorageVolume storageVolume : StorageManager.getVolumeList(UserHandle.myUserId(), 256)) {
                String rootPath = storageVolume.getPath();
                for (String equalsIgnoreCase : directories) {
                    if (rootPath.equalsIgnoreCase(equalsIgnoreCase)) {
                        flag = true;
                        Log.i(TAG, "MediaScanner scanDirectories flag = true means root dirs!");
                        break;
                    }
                }
                if (flag) {
                    break;
                }
            }
            if (INTERNAL_VOLUME.equalsIgnoreCase(this.mVolumeName)) {
                flag = true;
                Log.i(TAG, "MediaScanner scanDirectories flag = true means internal!");
            }
            long start = System.currentTimeMillis();
            if (flag) {
                prescan(null, true);
            } else {
                prescanOnlyMedia(this.mAudioUri, true);
                prescanOnlyMedia(this.mVideoUri, true);
                prescanOnlyMedia(this.mImagesUri, true);
            }
            long prescan = System.currentTimeMillis();
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, RunningAppProcessInfo.IMPORTANCE_EMPTY);
            Log.d(TAG, "delete nomedia File when scanDirectories");
            deleteNomediaFile(StorageManager.getVolumeList(UserHandle.myUserId(), 256));
            for (i = 0; i < directories.length; i++) {
                setExteLen(getRootDirLength(directories[i]));
                processDirectory(directories[i], this.mClient);
            }
            this.mMediaInserter.flushAll();
            this.mMediaInserter = null;
            long scan = System.currentTimeMillis();
            postscan(directories);
            long end = System.currentTimeMillis();
            releaseResources();
            if (this.mFileCache != null) {
                this.mFileCache.clear();
                this.mFileCache = null;
            }
        } catch (SQLException e) {
            Log.e(TAG, "SQLException in MediaScanner.scan()", e);
            releaseResources();
            if (this.mFileCache != null) {
                this.mFileCache.clear();
                this.mFileCache = null;
            }
        } catch (UnsupportedOperationException e2) {
            Log.e(TAG, "UnsupportedOperationException in MediaScanner.scan()", e2);
            releaseResources();
            if (this.mFileCache != null) {
                this.mFileCache.clear();
                this.mFileCache = null;
            }
        } catch (RemoteException e3) {
            Log.e(TAG, "RemoteException in MediaScanner.scan()", e3);
            releaseResources();
            if (this.mFileCache != null) {
                this.mFileCache.clear();
                this.mFileCache = null;
            }
        } catch (Throwable th) {
            releaseResources();
            if (this.mFileCache != null) {
                this.mFileCache.clear();
                this.mFileCache = null;
            }
            this.mSkipExternelQuery = false;
        }
        this.mSkipExternelQuery = false;
    }

    public void scanCustomDirectories(String[] directories, String volumeName, String[] whiteList, String[] blackList) {
        this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient("media");
        this.mMediaInserter = new MediaInserter(this.mMediaProvider, RunningAppProcessInfo.IMPORTANCE_EMPTY);
        HwFrameworkFactory.getHwMediaScannerManager().setMediaInserter(this.mMediaInserter);
        Log.d(TAG, "delete nomedia File when scanCustomDirectories");
        deleteNomediaFile(StorageManager.getVolumeList(UserHandle.myUserId(), 256));
        HwFrameworkFactory.getHwMediaScannerManager().scanCustomDirectories(this, this.mClient, directories, volumeName, whiteList, blackList);
        clearSkipCustomDirectory();
        if (this.mFileCache != null) {
            this.mFileCache.clear();
            this.mFileCache = null;
        }
        HwFrameworkFactory.getHwMediaScannerManager().setMediaInserter(null);
        this.mMediaInserter = null;
        this.mSkipExternelQuery = false;
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
        try {
            prescan(path, true);
            this.mBlackListFlag = false;
            if (isBlackListPath(path, getRootDirLength(path))) {
                this.mBlackListFlag = true;
            }
            File file = new File(path);
            if (file.exists() && (file.canRead() ^ 1) == 0) {
                Log.d(TAG, "delete nomedia File when scanSingleFile");
                deleteNomediaFile(StorageManager.getVolumeList(UserHandle.myUserId(), 256));
                String str = path;
                String str2 = mimeType;
                Uri doScanFile = this.mClient.doScanFile(str, str2, file.lastModified() / 1000, file.length(), false, true, isNoMediaPath(path));
                releaseResources();
                return doScanFile;
            }
            releaseResources();
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
            releaseResources();
            return null;
        } catch (Throwable th) {
            releaseResources();
            throw th;
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0071, code:
            if (r10.regionMatches(true, r7 + 1, "AlbumArtSmall", 0, 13) == false) goto L_0x0073;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isNoMediaFile(String path) {
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
                mMediaPaths.clear();
            }
            if (clearNoMediaPaths) {
                mNoMediaPaths.clear();
            }
        }
    }

    /* JADX WARNING: Missing block: B:33:0x007b, code:
            return isNoMediaFile(r9);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            } else if (!mMediaPaths.containsKey(parent)) {
                int offset = 1;
                while (offset >= 0) {
                    int slashIndex = path.indexOf(47, offset);
                    if (slashIndex > offset) {
                        slashIndex++;
                        if (new File(path.substring(0, slashIndex) + ".nomedia").exists()) {
                            mNoMediaPaths.put(parent, ProxyInfo.LOCAL_EXCL_LIST);
                            return true;
                        }
                    }
                    offset = slashIndex;
                }
                mMediaPaths.put(parent, ProxyInfo.LOCAL_EXCL_LIST);
            }
        }
    }

    public void scanMtpFile(String path, int objectHandle, int format) {
        MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = mediaFileType == null ? 0 : mediaFileType.fileType;
        File file = new File(path);
        long lastModifiedSeconds = file.lastModified() / 1000;
        if (MediaFile.isAudioFileType(fileType) || (MediaFile.isVideoFileType(fileType) ^ 1) == 0 || (MediaFile.isImageFileType(fileType) ^ 1) == 0 || (MediaFile.isPlayListFileType(fileType) ^ 1) == 0 || (MediaFile.isDrmFileType(fileType) ^ 1) == 0) {
            this.mMtpObjectHandle = objectHandle;
            Cursor fileList = null;
            try {
                if (MediaFile.isPlayListFileType(fileType)) {
                    prescan(null, true);
                    String key = path;
                    FileEntry entry = (FileEntry) this.mFileCache.remove(path);
                    if (entry == null) {
                        entry = makeEntryFor(path);
                    }
                    if (entry != null) {
                        fileList = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, null, 0, null, null);
                        processPlayList(entry, fileList);
                    }
                } else {
                    prescan(path, false);
                    this.mClient.doScanFile(path, mediaFileType.mimeType, lastModifiedSeconds, file.length(), format == 12289, true, isNoMediaPath(path));
                }
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                    fileList.close();
                }
                releaseResources();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in MediaScanner.scanFile()", e);
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                    fileList.close();
                }
                releaseResources();
            } catch (Throwable th) {
                this.mMtpObjectHandle = 0;
                if (fileList != null) {
                    fileList.close();
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
            this.mMediaProvider.update(Files.getMtpObjectsUri(this.mVolumeName), values, "_id=?", new String[]{Integer.toString(objectHandle)});
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException in scanMtpFile", e2);
        }
    }

    FileEntry makeEntryFor(String path) {
        Cursor cursor = null;
        try {
            String[] selectionArgs = new String[]{path};
            cursor = this.mMediaProvider.query(this.mFilesUriNoNotify, FILES_PRESCAN_PROJECTION, "_data=?", selectionArgs, null, null);
            if (cursor.moveToFirst()) {
                String str = path;
                FileEntry fileEntry = new FileEntry(cursor.getLong(0), str, cursor.getLong(3), cursor.getInt(2));
                if (cursor != null) {
                    cursor.close();
                }
                return fileEntry;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (RemoteException e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
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
            start1 = start1 < 0 ? 0 : start1 + 1;
            start2 = start2 < 0 ? 0 : start2 + 1;
            int length = end1 - start1;
            if (end2 - start2 != length || !path1.regionMatches(true, start1, path2, start2, length)) {
                break;
            }
            result++;
            end1 = start1 - 1;
            end2 = start2 - 1;
        }
        return result;
    }

    private boolean matchEntries(long rowId, String data) {
        int len = this.mPlaylistEntries.size();
        boolean done = true;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = (PlaylistEntry) this.mPlaylistEntries.get(i);
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

    private void cachePlaylistEntry(String line, String playListDirectory) {
        PlaylistEntry entry = new PlaylistEntry();
        int entryLength = line.length();
        while (entryLength > 0 && Character.isWhitespace(line.charAt(entryLength - 1))) {
            entryLength--;
        }
        if (entryLength >= 3) {
            if (entryLength < line.length()) {
                line = line.substring(0, entryLength);
            }
            char ch1 = line.charAt(0);
            boolean fullPath = ch1 != '/' ? Character.isLetter(ch1) && line.charAt(1) == ':' && line.charAt(2) == '\\' : true;
            if (!fullPath) {
                line = playListDirectory + line;
            }
            entry.path = line;
            this.mPlaylistEntries.add(entry);
        }
    }

    private void processCachedPlaylist(Cursor fileList, ContentValues values, Uri playlistUri) {
        fileList.moveToPosition(-1);
        while (fileList.moveToNext()) {
            if (matchEntries(fileList.getLong(0), fileList.getString(1))) {
                break;
            }
        }
        int len = this.mPlaylistEntries.size();
        int index = 0;
        for (int i = 0; i < len; i++) {
            PlaylistEntry entry = (PlaylistEntry) this.mPlaylistEntries.get(i);
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

    /* JADX WARNING: Removed duplicated region for block: B:47:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0060 A:{SYNTHETIC, Splitter: B:25:0x0060} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0072 A:{SYNTHETIC, Splitter: B:31:0x0072} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processM3uPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                try {
                    String line = reader2.readLine();
                    this.mPlaylistEntries.clear();
                    while (line != null) {
                        if (line.length() > 0 && line.charAt(0) != '#') {
                            cachePlaylistEntry(line, playListDirectory);
                        }
                        line = reader2.readLine();
                    }
                    processCachedPlaylist(fileList, values, uri);
                    reader = reader2;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                    try {
                        Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e);
                        if (reader == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e3);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e32) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e32);
                }
            }
        } catch (IOException e4) {
            e32 = e4;
            Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e32);
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e322) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e322);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0068 A:{SYNTHETIC, Splitter: B:25:0x0068} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x007a A:{SYNTHETIC, Splitter: B:31:0x007a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processPlsPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 8192);
                try {
                    this.mPlaylistEntries.clear();
                    for (String line = reader2.readLine(); line != null; line = reader2.readLine()) {
                        if (line.startsWith("File")) {
                            int equals = line.indexOf(61);
                            if (equals > 0) {
                                cachePlaylistEntry(line.substring(equals + 1), playListDirectory);
                            }
                        }
                    }
                    processCachedPlaylist(fileList, values, uri);
                    reader = reader2;
                } catch (IOException e2) {
                    e = e2;
                    reader = reader2;
                    try {
                        Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e);
                        if (reader == null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e3);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    reader = reader2;
                    if (reader != null) {
                    }
                    throw th;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e32) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e32);
                }
            }
        } catch (IOException e4) {
            e32 = e4;
            Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e32);
            if (reader == null) {
                try {
                    reader.close();
                } catch (IOException e322) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e322);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x006b A:{SYNTHETIC, Splitter: B:31:0x006b} */
    /* JADX WARNING: Removed duplicated region for block: B:47:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0059 A:{SYNTHETIC, Splitter: B:25:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0044 A:{SYNTHETIC, Splitter: B:17:0x0044} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processWplPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        SAXException e;
        IOException e2;
        Throwable th;
        FileInputStream fis = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                FileInputStream fis2 = new FileInputStream(f);
                try {
                    this.mPlaylistEntries.clear();
                    Xml.parse(fis2, Xml.findEncodingByName("UTF-8"), new WplHandler(playListDirectory, uri, fileList).getContentHandler());
                    processCachedPlaylist(fileList, values, uri);
                    fis = fis2;
                } catch (SAXException e3) {
                    e = e3;
                    fis = fis2;
                    e.printStackTrace();
                    if (fis == null) {
                        try {
                            fis.close();
                            return;
                        } catch (IOException e22) {
                            Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e22);
                            return;
                        }
                    }
                    return;
                } catch (IOException e4) {
                    e22 = e4;
                    fis = fis2;
                    try {
                        e22.printStackTrace();
                        if (fis == null) {
                            try {
                                fis.close();
                                return;
                            } catch (IOException e222) {
                                Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e222);
                                return;
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fis != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fis = fis2;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e2222) {
                            Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e2222);
                        }
                    }
                    throw th;
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e22222) {
                    Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e22222);
                }
            }
        } catch (SAXException e5) {
            e = e5;
            e.printStackTrace();
            if (fis == null) {
            }
        } catch (IOException e6) {
            e22222 = e6;
            e22222.printStackTrace();
            if (fis == null) {
            }
        }
    }

    private void processPlayList(FileEntry entry, Cursor fileList) throws RemoteException {
        String path = entry.mPath;
        ContentValues values = new ContentValues();
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash < 0) {
            throw new IllegalArgumentException("bad path ");
        }
        Uri membersUri;
        long rowId = entry.mRowId;
        String name = values.getAsString(MidiDeviceInfo.PROPERTY_NAME);
        if (name == null) {
            name = values.getAsString("title");
            if (name == null) {
                int lastDot = path.lastIndexOf(46);
                if (lastDot < 0) {
                    name = path.substring(lastSlash + 1);
                } else {
                    name = path.substring(lastSlash + 1, lastDot);
                }
            }
        }
        values.put(MidiDeviceInfo.PROPERTY_NAME, name);
        values.put("date_modified", Long.valueOf(entry.mLastModified));
        Uri uri;
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
        String playListDirectory = path.substring(0, lastSlash + 1);
        MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = mediaFileType == null ? 0 : mediaFileType.fileType;
        if (fileType == 44) {
            processM3uPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 45) {
            processPlsPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 46) {
            processWplPlayList(path, playListDirectory, membersUri, values, fileList);
        }
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processPlayLists() throws RemoteException {
        Iterator<FileEntry> iterator = this.mPlayLists.iterator();
        Cursor cursor = null;
        try {
            cursor = this.mMediaProvider.query(this.mFilesUri, FILES_PRESCAN_PROJECTION, "media_type=2", null, null, null);
            while (iterator.hasNext()) {
                FileEntry entry = (FileEntry) iterator.next();
                if (entry.mLastModifiedChanged) {
                    processPlayList(entry, cursor);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RemoteException e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public void close() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, true)) {
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

    protected void updateValues(String path, ContentValues contentValues) {
    }

    public void deleteNomediaFile(StorageVolume[] volumes) {
        if (volumes != null) {
            for (StorageVolume storageVolume : volumes) {
                String rootPath = storageVolume.getPath();
                for (String nomedia : sNomediaFilepath) {
                    String nomediaPath = rootPath + nomedia;
                    File nomediaFile = new File(nomediaPath);
                    try {
                        if (nomediaFile.exists()) {
                            if (nomediaFile.isFile() && nomediaFile.length() > MAX_NOMEDIA_SIZE) {
                                Log.w(TAG, "skip nomedia file [" + nomediaPath + "]  size:" + nomediaFile.length());
                            } else if (deleteFile(nomediaFile)) {
                                Log.w(TAG, "delete nomedia file success [" + nomediaPath + "]");
                            } else {
                                Log.w(TAG, "delete nomedia file fail [" + nomediaPath + "]");
                            }
                        }
                    } catch (IOException e) {
                        Log.w(TAG, "delete nomedia file exception [" + nomediaPath + "]");
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
            if (!file.delete()) {
                result = false;
            }
        }
        return result;
    }
}
