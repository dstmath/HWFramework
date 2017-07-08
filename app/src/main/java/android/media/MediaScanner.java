package android.media;

import android.app.AlarmManager;
import android.bluetooth.BluetoothAssignedNumbers;
import android.common.HwFrameworkFactory;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.drm.DrmManagerClient;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.hardware.Camera.Parameters;
import android.hwtheme.HwThemeManager;
import android.media.MediaFile.MediaFileType;
import android.mtp.MtpConstants;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.Uri.Builder;
import android.net.wifi.AnqpInformationElement;
import android.net.wifi.WifiEnterpriseConfig;
import android.opengl.GLES11;
import android.os.Environment;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.Preference;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Audio.Playlists.Members;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.Thumbnails;
import android.provider.MediaStore.Video.VideoColumns;
import android.provider.OpenableColumns;
import android.provider.Settings.Bookmarks;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.VoicemailContract.Voicemails;
import android.rms.iaware.AwareConstant.Database.HwUserData;
import android.sax.ElementListener;
import android.sax.RootElement;
import android.security.KeyChain;
import android.speech.SpeechRecognizer;
import android.speech.tts.Voice;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.telecom.AudioState;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class MediaScanner implements AutoCloseable {
    private static final String ALARMS_DIR = "/alarms/";
    private static final int DATE_MODIFIED_PLAYLISTS_COLUMN_INDEX = 2;
    private static final String DEFAULT_RINGTONE_PROPERTY_PREFIX = "ro.config.";
    private static final boolean ENABLE_BULK_INSERTS = true;
    private static final int FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX = 3;
    private static final int FILES_PRESCAN_FORMAT_COLUMN_INDEX = 2;
    private static final int FILES_PRESCAN_ID_COLUMN_INDEX = 0;
    private static final int FILES_PRESCAN_PATH_COLUMN_INDEX = 1;
    private static final String[] FILES_PRESCAN_PROJECTION = null;
    private static final String[] ID3_GENRES = null;
    private static final int ID_PLAYLISTS_COLUMN_INDEX = 0;
    private static final String[] ID_PROJECTION = null;
    private static final int MAX_ENTRY_SIZE = 40000;
    private static final String MUSIC_DIR = "/music/";
    private static final String NOTIFICATIONS_DIR = "/notifications/";
    private static final int PATH_PLAYLISTS_COLUMN_INDEX = 1;
    private static final String[] PLAYLIST_MEMBERS_PROJECTION = null;
    private static final String PODCAST_DIR = "/podcasts/";
    private static final String RINGTONES_DIR = "/ringtones/";
    private static final String TAG = "MediaScanner";
    private static HashMap<String, String> mMediaPaths;
    private static HashMap<String, String> mNoMediaPaths;
    private final Uri mAudioUri;
    private final Options mBitmapOptions;
    private final MyMediaScannerClient mClient;
    private final CloseGuard mCloseGuard;
    private final AtomicBoolean mClosed;
    private final Context mContext;
    private String mDefaultAlarmAlertFilename;
    private boolean mDefaultAlarmSet;
    private String mDefaultNotificationFilename;
    private boolean mDefaultNotificationSet;
    private String mDefaultRingtoneFilename;
    private boolean mDefaultRingtoneSet;
    private DrmManagerClient mDrmManagerClient;
    private String mExtStroagePath;
    private HashMap<String, FileEntry> mFileCache;
    private final Uri mFilesUri;
    private final Uri mFilesUriNoNotify;
    private final Uri mImagesUri;
    private MediaInserter mMediaInserter;
    private ContentProviderClient mMediaProvider;
    private int mMtpObjectHandle;
    private long mNativeContext;
    private int mOriginalCount;
    private final String mPackageName;
    private final ArrayList<FileEntry> mPlayLists;
    private final ArrayList<PlaylistEntry> mPlaylistEntries;
    private final Uri mPlaylistsUri;
    private final boolean mProcessGenres;
    private final boolean mProcessPlaylists;
    private boolean mSkipExternelQuery;
    private final Uri mThumbsUri;
    private final Uri mVideoUri;
    private final String mVolumeName;

    private static class FileEntry {
        int mFormat;
        long mLastModified;
        boolean mLastModifiedChanged;
        String mPath;
        long mRowId;

        FileEntry(long rowId, String path, long lastModified, int format) {
            this.mRowId = rowId;
            this.mPath = path;
            this.mLastModified = lastModified;
            this.mFormat = format;
            this.mLastModifiedChanged = false;
        }

        public String toString() {
            return this.mPath + " mRowId: " + this.mRowId;
        }
    }

    static class MediaBulkDeleter {
        final Uri mBaseUri;
        final ContentProviderClient mProvider;
        ArrayList<String> whereArgs;
        StringBuilder whereClause;

        public MediaBulkDeleter(ContentProviderClient provider, Uri baseUri) {
            this.whereClause = new StringBuilder();
            this.whereArgs = new ArrayList(100);
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
                this.whereClause.setLength(MediaScanner.ID_PLAYLISTS_COLUMN_INDEX);
                this.whereArgs.clear();
            }
        }
    }

    public class MyMediaScannerClient implements MediaScannerClient {
        private static final int ALBUM = 1;
        private static final int ARTIST = 2;
        private static final int TITLE = 3;
        private String mAlbum;
        private String mAlbumArtist;
        private String mArtist;
        private int mCompilation;
        private String mComposer;
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public FileEntry beginFile(String path, String mimeType, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            this.mMimeType = mimeType;
            this.mFileType = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
            this.mFileSize = fileSize;
            this.mIsDrm = false;
            if (!isDirectory) {
                if (!noMedia && MediaScanner.isNoMediaFile(path)) {
                    noMedia = MediaScanner.ENABLE_BULK_INSERTS;
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
            boolean wasModified = (delta > 1 || delta < -1) ? MediaScanner.ENABLE_BULK_INSERTS : false;
            if (entry == null || wasModified) {
                if (wasModified) {
                    entry.mLastModified = lastModified;
                } else {
                    entry = new FileEntry(0, path, lastModified, isDirectory ? GLES11.GL_CLIP_PLANE1 : MediaScanner.ID_PLAYLISTS_COLUMN_INDEX);
                }
                entry.mLastModifiedChanged = MediaScanner.ENABLE_BULK_INSERTS;
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
            this.mTrack = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
            this.mYear = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
            this.mDuration = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
            this.mPath = path;
            this.mLastModified = lastModified;
            this.mWriter = null;
            this.mCompilation = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
            this.mWidth = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
            this.mHeight = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
            this.mIsAlbumMessy = false;
            this.mIsArtistMessy = false;
            this.mIsTitleMessy = false;
            return entry;
        }

        public void scanFile(String path, long lastModified, long fileSize, boolean isDirectory, boolean noMedia) {
            doScanFile(path, null, lastModified, fileSize, isDirectory, false, noMedia);
        }

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
                if (entry.mPath != null && ((!MediaScanner.this.mDefaultNotificationSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename)) || ((!MediaScanner.this.mDefaultRingtoneSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultRingtoneFilename)) || (!MediaScanner.this.mDefaultAlarmSet && doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultAlarmAlertFilename))))) {
                    Log.w(MediaScanner.TAG, "forcing rescan , since ringtone setting didn't finish");
                    scanAlways = MediaScanner.ENABLE_BULK_INSERTS;
                }
                if (entry != null && (entry.mLastModifiedChanged || r29)) {
                    if (noMedia) {
                        result = endFile(entry, false, false, false, false, false);
                    } else {
                        String lowpath = path.toLowerCase(Locale.ROOT);
                        boolean ringtones = lowpath.indexOf(MediaScanner.RINGTONES_DIR) > 0 ? MediaScanner.ENABLE_BULK_INSERTS : false;
                        boolean notifications = lowpath.indexOf(MediaScanner.NOTIFICATIONS_DIR) > 0 ? MediaScanner.ENABLE_BULK_INSERTS : false;
                        boolean alarms = lowpath.indexOf(MediaScanner.ALARMS_DIR) > 0 ? MediaScanner.ENABLE_BULK_INSERTS : false;
                        boolean podcasts = lowpath.indexOf(MediaScanner.PODCAST_DIR) > 0 ? MediaScanner.ENABLE_BULK_INSERTS : false;
                        boolean music = lowpath.indexOf(MediaScanner.MUSIC_DIR) <= 0 ? (ringtones || notifications || alarms || podcasts) ? false : MediaScanner.ENABLE_BULK_INSERTS : MediaScanner.ENABLE_BULK_INSERTS;
                        ringtones |= HwThemeManager.isTRingtones(lowpath);
                        notifications |= HwThemeManager.isTNotifications(lowpath);
                        alarms |= HwThemeManager.isTAlarms(lowpath);
                        boolean isaudio = MediaFile.isAudioFileType(this.mFileType);
                        boolean isvideo = MediaFile.isVideoFileType(this.mFileType);
                        boolean isimage = MediaFile.isImageFileType(this.mFileType);
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
                                this.mAlbum = HwFrameworkFactory.getHwMediaScannerManager().postHandleStringTag(this.mAlbum, this.mPath, ALBUM);
                            }
                            if (this.mIsArtistMessy) {
                                this.mArtist = HwFrameworkFactory.getHwMediaScannerManager().postHandleStringTag(this.mArtist, this.mPath, ARTIST);
                            }
                            if (this.mIsTitleMessy) {
                                this.mTitle = HwFrameworkFactory.getHwMediaScannerManager().postHandleStringTag(this.mTitle, this.mPath, TITLE);
                            }
                            HwFrameworkFactory.getHwMediaScannerManager().resetSniffer();
                        }
                        result = endFile(entry, ringtones, notifications, alarms, music, podcasts);
                    }
                }
                return result;
            } catch (RemoteException e) {
                Log.e(MediaScanner.TAG, "RemoteException in MediaScanner.scanFile()", e);
            }
        }

        private int parseSubstring(String s, int start, int defaultValue) {
            int length = s.length();
            if (start == length) {
                return defaultValue;
            }
            int start2 = start + ALBUM;
            char ch = s.charAt(start);
            if (ch < '0' || ch > '9') {
                return defaultValue;
            }
            int result = ch - 48;
            while (start2 < length) {
                start = start2 + ALBUM;
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
            boolean z = MediaScanner.ENABLE_BULK_INSERTS;
            boolean startsWith = !name.equalsIgnoreCase(VideoColumns.ALBUM) ? name.startsWith("album;") : MediaScanner.ENABLE_BULK_INSERTS;
            boolean startsWith2 = !name.equalsIgnoreCase(VideoColumns.ARTIST) ? name.startsWith("artist;") : MediaScanner.ENABLE_BULK_INSERTS;
            boolean startsWith3 = !name.equalsIgnoreCase(Bookmarks.TITLE) ? name.startsWith("title;") : MediaScanner.ENABLE_BULK_INSERTS;
            if (startsWith) {
                this.mIsAlbumMessy = HwFrameworkFactory.getHwMediaScannerManager().preHandleStringTag(value, this.mMimeType);
            }
            if (startsWith2) {
                this.mIsArtistMessy = HwFrameworkFactory.getHwMediaScannerManager().preHandleStringTag(value, this.mMimeType);
            }
            if (startsWith3) {
                this.mIsTitleMessy = HwFrameworkFactory.getHwMediaScannerManager().preHandleStringTag(value, this.mMimeType);
            }
            if (name.equalsIgnoreCase(Bookmarks.TITLE) || name.startsWith("title;")) {
                this.mTitle = value;
            } else if (name.equalsIgnoreCase(VideoColumns.ARTIST) || name.startsWith("artist;")) {
                this.mArtist = value.trim();
            } else if (name.equalsIgnoreCase("albumartist") || name.startsWith("albumartist;") || name.equalsIgnoreCase("band") || name.startsWith("band;")) {
                this.mAlbumArtist = value.trim();
            } else if (name.equalsIgnoreCase(VideoColumns.ALBUM) || name.startsWith("album;")) {
                this.mAlbum = value.trim();
            } else if (name.equalsIgnoreCase(AudioColumns.COMPOSER) || name.startsWith("composer;")) {
                this.mComposer = value.trim();
            } else if (MediaScanner.this.mProcessGenres && (name.equalsIgnoreCase(AudioColumns.GENRE) || name.startsWith("genre;"))) {
                this.mGenre = getGenreName(value);
            } else if (name.equalsIgnoreCase(AudioColumns.YEAR) || name.startsWith("year;")) {
                this.mYear = parseSubstring(value, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX);
            } else if (name.equalsIgnoreCase("tracknumber") || name.startsWith("tracknumber;")) {
                this.mTrack = ((this.mTrack / Process.SYSTEM_UID) * Process.SYSTEM_UID) + parseSubstring(value, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX);
            } else if (name.equalsIgnoreCase("discnumber") || name.equals("set") || name.startsWith("set;")) {
                this.mTrack = (parseSubstring(value, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX) * Process.SYSTEM_UID) + (this.mTrack % Process.SYSTEM_UID);
            } else if (name.equalsIgnoreCase(DevStatusProperty.VIBRATOR_DURATION)) {
                this.mDuration = parseSubstring(value, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX);
            } else if (name.equalsIgnoreCase("writer") || name.startsWith("writer;")) {
                this.mWriter = value.trim();
            } else if (name.equalsIgnoreCase(AudioColumns.COMPILATION)) {
                this.mCompilation = parseSubstring(value, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX);
            } else if (name.equalsIgnoreCase("isdrm")) {
                if (parseSubstring(value, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX) != ALBUM) {
                    z = false;
                }
                this.mIsDrm = z;
            } else if (name.equalsIgnoreCase(Thumbnails.WIDTH)) {
                this.mWidth = parseSubstring(value, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX);
            } else if (name.equalsIgnoreCase(Thumbnails.HEIGHT)) {
                this.mHeight = parseSubstring(value, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX);
            }
        }

        private boolean convertGenreCode(String input, String expected) {
            String output = getGenreName(input);
            if (output.equals(expected)) {
                return MediaScanner.ENABLE_BULK_INSERTS;
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
                int i = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
                while (i < length) {
                    char c = genreTagValue.charAt(i);
                    if (i != 0 || c != '(') {
                        if (!Character.isDigit(c)) {
                            break;
                        }
                        number.append(c);
                    } else {
                        parenthesized = MediaScanner.ENABLE_BULK_INSERTS;
                    }
                    i += ALBUM;
                }
                char charAt = i < length ? genreTagValue.charAt(i) : ' ';
                if ((parenthesized && charAt == ')') || (!parenthesized && Character.isWhitespace(charAt))) {
                    try {
                        short genreIndex = Short.parseShort(number.toString());
                        if (genreIndex >= (short) 0) {
                            if (genreIndex < MediaScanner.ID3_GENRES.length && MediaScanner.ID3_GENRES[genreIndex] != null) {
                                return MediaScanner.ID3_GENRES[genreIndex];
                            }
                            if (genreIndex == (short) 255) {
                                return null;
                            }
                            if (genreIndex >= (short) 255 || i + ALBUM >= length) {
                                return number.toString();
                            }
                            if (parenthesized && charAt == ')') {
                                i += ALBUM;
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
                MediaScanner.this.mBitmapOptions.outWidth = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
                MediaScanner.this.mBitmapOptions.outHeight = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
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
            if (!"audio/mp4".equals(this.mMimeType) || !mimeType.startsWith("video")) {
                this.mMimeType = mimeType;
                this.mFileType = MediaFile.getFileTypeForMimeType(mimeType);
            }
        }

        private ContentValues toValues() {
            ContentValues map = new ContentValues();
            map.put(Voicemails._DATA, this.mPath);
            map.put(Bookmarks.TITLE, this.mTitle);
            map.put(PlaylistsColumns.DATE_MODIFIED, Long.valueOf(this.mLastModified));
            map.put(OpenableColumns.SIZE, Long.valueOf(this.mFileSize));
            map.put(Voicemails.MIME_TYPE, this.mMimeType);
            map.put(MediaColumns.IS_DRM, Boolean.valueOf(this.mIsDrm));
            String resolution = null;
            if (this.mWidth > 0 && this.mHeight > 0) {
                map.put(Thumbnails.WIDTH, Integer.valueOf(this.mWidth));
                map.put(Thumbnails.HEIGHT, Integer.valueOf(this.mHeight));
                resolution = this.mWidth + "x" + this.mHeight;
            }
            if (!this.mNoMedia) {
                String str;
                String str2;
                if (MediaFile.isVideoFileType(this.mFileType)) {
                    str = VideoColumns.ARTIST;
                    str2 = (this.mArtist == null || this.mArtist.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mArtist;
                    map.put(str, str2);
                    str = VideoColumns.ALBUM;
                    str2 = (this.mAlbum == null || this.mAlbum.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mAlbum;
                    map.put(str, str2);
                    map.put(DevStatusProperty.VIBRATOR_DURATION, Integer.valueOf(this.mDuration));
                    if (resolution != null) {
                        map.put(VideoColumns.RESOLUTION, resolution);
                    }
                } else if (!MediaFile.isImageFileType(this.mFileType) && MediaFile.isAudioFileType(this.mFileType)) {
                    String str3 = VideoColumns.ARTIST;
                    str2 = (this.mArtist == null || this.mArtist.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mArtist;
                    map.put(str3, str2);
                    str3 = AudioColumns.ALBUM_ARTIST;
                    if (this.mAlbumArtist == null || this.mAlbumArtist.length() <= 0) {
                        str2 = null;
                    } else {
                        str2 = this.mAlbumArtist;
                    }
                    map.put(str3, str2);
                    str = VideoColumns.ALBUM;
                    str2 = (this.mAlbum == null || this.mAlbum.length() <= 0) ? MediaStore.UNKNOWN_STRING : this.mAlbum;
                    map.put(str, str2);
                    map.put(AudioColumns.COMPOSER, this.mComposer);
                    map.put(AudioColumns.GENRE, this.mGenre);
                    if (this.mYear != 0) {
                        map.put(AudioColumns.YEAR, Integer.valueOf(this.mYear));
                    }
                    map.put(AudioColumns.TRACK, Integer.valueOf(this.mTrack));
                    map.put(DevStatusProperty.VIBRATOR_DURATION, Integer.valueOf(this.mDuration));
                    map.put(AudioColumns.COMPILATION, Integer.valueOf(this.mCompilation));
                }
            }
            return map;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private Uri endFile(FileEntry entry, boolean ringtones, boolean notifications, boolean alarms, boolean music, boolean podcasts) throws RemoteException {
            if (this.mArtist == null || this.mArtist.length() == 0) {
                this.mArtist = this.mAlbumArtist;
            }
            ContentValues values = toValues();
            String title = values.getAsString(Bookmarks.TITLE);
            if (title == null || TextUtils.isEmpty(title.trim())) {
                title = MediaFile.getFileTitle(values.getAsString(Voicemails._DATA));
                values.put(Bookmarks.TITLE, title);
            }
            if (MediaStore.UNKNOWN_STRING.equals(values.getAsString(VideoColumns.ALBUM))) {
                String album = values.getAsString(Voicemails._DATA);
                int lastSlash = album.lastIndexOf(47);
                if (lastSlash >= 0) {
                    ContentValues contentValues;
                    int previousSlash = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
                    while (true) {
                        int idx = album.indexOf(47, previousSlash + ALBUM);
                        if (idx >= 0 && idx < lastSlash) {
                            previousSlash = idx;
                        } else if (previousSlash != 0) {
                            contentValues = values;
                            contentValues.put(VideoColumns.ALBUM, album.substring(previousSlash + ALBUM, lastSlash));
                        }
                    }
                    if (previousSlash != 0) {
                        contentValues = values;
                        contentValues.put(VideoColumns.ALBUM, album.substring(previousSlash + ALBUM, lastSlash));
                    }
                }
            }
            long rowId = entry.mRowId;
            if (MediaFile.isAudioFileType(this.mFileType) && (rowId == 0 || MediaScanner.this.mMtpObjectHandle != 0)) {
                values.put(AudioColumns.IS_RINGTONE, Boolean.valueOf(ringtones));
                values.put(AudioColumns.IS_NOTIFICATION, Boolean.valueOf(notifications));
                values.put(AudioColumns.IS_ALARM, Boolean.valueOf(alarms));
                values.put(AudioColumns.IS_MUSIC, Boolean.valueOf(music));
                values.put(AudioColumns.IS_PODCAST, Boolean.valueOf(podcasts));
            } else if ((this.mFileType == 34 || MediaFile.isRawImageFileType(this.mFileType)) && !this.mNoMedia) {
                ExifInterface exifInterface = null;
                try {
                    exifInterface = new ExifInterface(entry.mPath);
                } catch (IOException e) {
                }
                if (exifInterface != null) {
                    float[] latlng = new float[ARTIST];
                    boolean mHasLatLong = exifInterface.getLatLong(latlng);
                    if (mHasLatLong) {
                        values.put(VideoColumns.LATITUDE, Float.valueOf(latlng[MediaScanner.ID_PLAYLISTS_COLUMN_INDEX]));
                        values.put(VideoColumns.LONGITUDE, Float.valueOf(latlng[ALBUM]));
                    }
                    long time = exifInterface.getGpsDateTime();
                    if (time == -1 || !mHasLatLong) {
                        time = exifInterface.getDateTime();
                        if (time != -1) {
                            if (Math.abs((this.mLastModified * 1000) - time) >= AlarmManager.INTERVAL_DAY) {
                                values.put(VideoColumns.DATE_TAKEN, Long.valueOf(time));
                            }
                        }
                    } else {
                        values.put(VideoColumns.DATE_TAKEN, Long.valueOf(time));
                    }
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
                    if (orientation != -1) {
                        int degree;
                        switch (orientation) {
                            case TITLE /*3*/:
                                degree = BluetoothAssignedNumbers.BDE_TECHNOLOGY;
                                break;
                            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                                degree = 90;
                                break;
                            case AudioState.ROUTE_SPEAKER /*8*/:
                                degree = AnqpInformationElement.ANQP_TDLS_CAP;
                                break;
                            default:
                                degree = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
                                break;
                        }
                        values.put(ImageColumns.ORIENTATION, Integer.valueOf(degree));
                    }
                    values.put(ImageColumns.IS_HDR, Boolean.valueOf(Parameters.SCENE_MODE_HDR.equals(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION))));
                }
                HwFrameworkFactory.getHwMediaScannerManager().initializeHwVoiceAndFocus(entry.mPath, values);
            }
            MediaScanner.this.updateValues(entry.mPath, values);
            Uri tableUri = MediaScanner.this.mFilesUri;
            MediaInserter inserter = MediaScanner.this.mMediaInserter;
            if (!this.mNoMedia) {
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
            if (!notifications || MediaScanner.this.mDefaultNotificationSet) {
                if (ringtones) {
                    if ((!MediaScanner.this.mDefaultRingtoneSet && TextUtils.isEmpty(MediaScanner.this.mDefaultRingtoneFilename)) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultRingtoneFilename)) {
                        needToSetSettings = MediaScanner.ENABLE_BULK_INSERTS;
                    }
                    needToSetSettings2 = HwFrameworkFactory.getHwMediaScannerManager().hwNeedSetSettings(entry.mPath);
                } else if (alarms && !MediaScanner.this.mDefaultAlarmSet && (TextUtils.isEmpty(MediaScanner.this.mDefaultAlarmAlertFilename) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultAlarmAlertFilename))) {
                    needToSetSettings = MediaScanner.ENABLE_BULK_INSERTS;
                }
            } else if (TextUtils.isEmpty(MediaScanner.this.mDefaultNotificationFilename) || doesPathHaveFilename(entry.mPath, MediaScanner.this.mDefaultNotificationFilename)) {
                needToSetSettings = MediaScanner.ENABLE_BULK_INSERTS;
            }
            if (rowId == 0) {
                if (MediaScanner.this.mMtpObjectHandle != 0) {
                    values.put(MediaColumns.MEDIA_SCANNER_NEW_OBJECT_ID, Integer.valueOf(MediaScanner.this.mMtpObjectHandle));
                }
                if (tableUri == MediaScanner.this.mFilesUri) {
                    int format = entry.mFormat;
                    if (format == 0) {
                        format = MediaFile.getFormatCode(entry.mPath, this.mMimeType);
                    }
                    values.put(FileColumns.FORMAT, Integer.valueOf(format));
                }
                if (inserter == null || needToSetSettings || needToSetSettings2) {
                    if (inserter != null) {
                        inserter.flushAll();
                    }
                    result = MediaScanner.this.mMediaProvider.insert(tableUri, values);
                } else if (entry.mFormat == GLES11.GL_CLIP_PLANE1) {
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
                values.remove(Voicemails._DATA);
                int mediaType = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
                if (!MediaScanner.isNoMediaPath(entry.mPath)) {
                    int fileType = MediaFile.getFileTypeForMimeType(this.mMimeType);
                    if (MediaFile.isAudioFileType(fileType)) {
                        mediaType = ARTIST;
                    } else if (MediaFile.isVideoFileType(fileType)) {
                        mediaType = TITLE;
                    } else if (MediaFile.isImageFileType(fileType)) {
                        mediaType = ALBUM;
                    } else if (MediaFile.isPlayListFileType(fileType)) {
                        mediaType = 4;
                    }
                    values.put(FileColumns.MEDIA_TYPE, Integer.valueOf(mediaType));
                }
                MediaScanner.this.mMediaProvider.update(result, values, null, null);
            }
            if (needToSetSettings) {
                if (notifications) {
                    setRingtoneIfNotSet(System.NOTIFICATION_SOUND, tableUri, rowId);
                    MediaScanner.this.mDefaultNotificationSet = MediaScanner.ENABLE_BULK_INSERTS;
                } else if (ringtones) {
                    setRingtoneIfNotSet(System.RINGTONE, tableUri, rowId);
                    MediaScanner.this.mDefaultRingtoneSet = MediaScanner.ENABLE_BULK_INSERTS;
                } else if (alarms) {
                    setRingtoneIfNotSet(System.ALARM_ALERT, tableUri, rowId);
                    MediaScanner.this.mDefaultAlarmSet = MediaScanner.ENABLE_BULK_INSERTS;
                }
            }
            HwFrameworkFactory.getHwMediaScannerManager().hwSetRingtone2Settings(needToSetSettings2, ringtones, tableUri, rowId, MediaScanner.this.mContext);
            return result;
        }

        private boolean doesPathHaveFilename(String path, String filename) {
            int pathFilenameStart = path.lastIndexOf(File.separatorChar) + ALBUM;
            int filenameLength = filename.length();
            if (path.regionMatches(pathFilenameStart, filename, MediaScanner.ID_PLAYLISTS_COLUMN_INDEX, filenameLength) && pathFilenameStart + filenameLength == path.length()) {
                return MediaScanner.ENABLE_BULK_INSERTS;
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
                System.putInt(cr, MediaScanner.this.settingSetIndicatorName(settingName), ALBUM);
            }
        }

        private int getFileTypeFromDrm(String path) {
            if (!MediaScanner.this.isDrmEnabled()) {
                return MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
            }
            int resultFileType = MediaScanner.ID_PLAYLISTS_COLUMN_INDEX;
            if (MediaScanner.this.mDrmManagerClient == null) {
                MediaScanner.this.mDrmManagerClient = new DrmManagerClient(MediaScanner.this.mContext);
            }
            if (MediaScanner.this.mDrmManagerClient.canHandle(path, null)) {
                this.mIsDrm = MediaScanner.ENABLE_BULK_INSERTS;
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

        public WplHandler(String playListDirectory, Uri uri, Cursor fileList) {
            this.playListDirectory = playListDirectory;
            RootElement root = new RootElement("smil");
            root.getChild(TtmlUtils.TAG_BODY).getChild("seq").getChild(MediaStore.AUTHORITY).setElementListener(this);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.MediaScanner.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.MediaScanner.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.<clinit>():void");
    }

    private final native void native_finalize();

    private static final native void native_init();

    private final native void native_setup();

    private native void processDirectory(String str, MediaScannerClient mediaScannerClient);

    private native void processFile(String str, String str2, MediaScannerClient mediaScannerClient);

    private native void setLocale(String str);

    public native void addSkipCustomDirectory(String str, int i);

    public native void clearSkipCustomDirectory();

    public native byte[] extractAlbumArt(FileDescriptor fileDescriptor);

    public void scanMtpFile(java.lang.String r23, int r24, int r25) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0118 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r22 = this;
        r19 = android.media.MediaFile.getFileType(r23);
        if (r19 != 0) goto L_0x0075;
    L_0x0006:
        r17 = 0;
    L_0x0008:
        r15 = new java.io.File;
        r0 = r23;
        r15.<init>(r0);
        r2 = r15.lastModified();
        r4 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r6 = r2 / r4;
        r2 = android.media.MediaFile.isAudioFileType(r17);
        if (r2 != 0) goto L_0x0023;
    L_0x001d:
        r2 = android.media.MediaFile.isVideoFileType(r17);
        if (r2 == 0) goto L_0x007c;
    L_0x0023:
        r0 = r24;
        r1 = r22;
        r1.mMtpObjectHandle = r0;
        r16 = 0;
        r2 = android.media.MediaFile.isPlayListFileType(r17);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        if (r2 == 0) goto L_0x00dd;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x0031:
        r2 = 0;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r3 = 1;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0.prescan(r2, r3);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r18 = r23;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r2 = r0.mFileCache;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r23;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r14 = r2.remove(r0);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r14 = (android.media.MediaScanner.FileEntry) r14;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        if (r14 != 0) goto L_0x004c;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x0048:
        r14 = r22.makeEntryFor(r23);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x004c:
        if (r14 == 0) goto L_0x0067;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x004e:
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r2 = r0.mMediaProvider;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r3 = r0.mFilesUri;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r4 = FILES_PRESCAN_PROJECTION;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r5 = 0;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r6 = 0;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r7 = 0;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r8 = 0;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r16 = r2.query(r3, r4, r5, r6, r7, r8);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r1 = r16;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0.processPlayList(r14, r1);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x0067:
        r2 = 0;
        r0 = r22;
        r0.mMtpObjectHandle = r2;
        if (r16 == 0) goto L_0x0071;
    L_0x006e:
        r16.close();
    L_0x0071:
        r22.releaseResources();
    L_0x0074:
        return;
    L_0x0075:
        r0 = r19;
        r0 = r0.fileType;
        r17 = r0;
        goto L_0x0008;
    L_0x007c:
        r2 = android.media.MediaFile.isImageFileType(r17);
        if (r2 != 0) goto L_0x0023;
    L_0x0082:
        r2 = android.media.MediaFile.isPlayListFileType(r17);
        if (r2 != 0) goto L_0x0023;
    L_0x0088:
        r2 = android.media.MediaFile.isDrmFileType(r17);
        if (r2 != 0) goto L_0x0023;
    L_0x008e:
        r20 = new android.content.ContentValues;
        r20.<init>();
        r2 = "_size";
        r4 = r15.length();
        r3 = java.lang.Long.valueOf(r4);
        r0 = r20;
        r0.put(r2, r3);
        r2 = "date_modified";
        r3 = java.lang.Long.valueOf(r6);
        r0 = r20;
        r0.put(r2, r3);
        r2 = 1;
        r0 = new java.lang.String[r2];	 Catch:{ RemoteException -> 0x00d2 }
        r21 = r0;	 Catch:{ RemoteException -> 0x00d2 }
        r2 = java.lang.Integer.toString(r24);	 Catch:{ RemoteException -> 0x00d2 }
        r3 = 0;	 Catch:{ RemoteException -> 0x00d2 }
        r21[r3] = r2;	 Catch:{ RemoteException -> 0x00d2 }
        r0 = r22;	 Catch:{ RemoteException -> 0x00d2 }
        r2 = r0.mMediaProvider;	 Catch:{ RemoteException -> 0x00d2 }
        r0 = r22;	 Catch:{ RemoteException -> 0x00d2 }
        r3 = r0.mVolumeName;	 Catch:{ RemoteException -> 0x00d2 }
        r3 = android.provider.MediaStore.Files.getMtpObjectsUri(r3);	 Catch:{ RemoteException -> 0x00d2 }
        r4 = "_id=?";	 Catch:{ RemoteException -> 0x00d2 }
        r0 = r20;	 Catch:{ RemoteException -> 0x00d2 }
        r1 = r21;	 Catch:{ RemoteException -> 0x00d2 }
        r2.update(r3, r0, r4, r1);	 Catch:{ RemoteException -> 0x00d2 }
    L_0x00d1:
        return;
    L_0x00d2:
        r13 = move-exception;
        r2 = "MediaScanner";
        r3 = "RemoteException in scanMtpFile";
        android.util.Log.e(r2, r3, r13);
        goto L_0x00d1;
    L_0x00dd:
        r2 = 0;
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r1 = r23;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0.prescan(r1, r2);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r22;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r3 = r0.mClient;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r19;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r5 = r0.mimeType;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r8 = r15.length();	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r2 = 12289; // 0x3001 float:1.722E-41 double:6.0716E-320;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r0 = r25;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        if (r0 != r2) goto L_0x011d;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x00f7:
        r10 = 1;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
    L_0x00f8:
        r12 = isNoMediaPath(r23);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r11 = 1;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r4 = r23;	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r3.doScanFile(r4, r5, r6, r8, r10, r11, r12);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        goto L_0x0067;
    L_0x0104:
        r13 = move-exception;
        r2 = "MediaScanner";	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r3 = "RemoteException in MediaScanner.scanFile()";	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        android.util.Log.e(r2, r3, r13);	 Catch:{ RemoteException -> 0x0104, all -> 0x011f }
        r2 = 0;
        r0 = r22;
        r0.mMtpObjectHandle = r2;
        if (r16 == 0) goto L_0x0118;
    L_0x0115:
        r16.close();
    L_0x0118:
        r22.releaseResources();
        goto L_0x0074;
    L_0x011d:
        r10 = 0;
        goto L_0x00f8;
    L_0x011f:
        r2 = move-exception;
        r3 = 0;
        r0 = r22;
        r0.mMtpObjectHandle = r3;
        if (r16 == 0) goto L_0x012a;
    L_0x0127:
        r16.close();
    L_0x012a:
        r22.releaseResources();
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.scanMtpFile(java.lang.String, int, int):void");
    }

    public android.net.Uri scanSingleFile(java.lang.String r15, java.lang.String r16) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:19:? in {6, 10, 15, 16, 18, 20, 21} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r14 = this;
        r1 = 1;
        r14.prescan(r15, r1);	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r11 = new java.io.File;	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r11.<init>(r15);	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r1 = r11.exists();	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        if (r1 != 0) goto L_0x0014;
    L_0x000f:
        r1 = 0;
        r14.releaseResources();
        return r1;
    L_0x0014:
        r1 = "MediaScanner";	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r2 = "delete nomedia File when scanSingleFile";	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        android.util.Log.d(r1, r2);	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r1 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r1.<init>();	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r2 = android.os.Environment.getExternalStorageDirectory();	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r1 = r1.append(r2);	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r2 = "/.nomedia";	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r1 = r1.append(r2);	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r13 = r1.toString();	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r12 = new java.io.File;	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r12.<init>(r13);	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r14.deleteFile(r12);	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r2 = r11.lastModified();	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r6 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r4 = r2 / r6;	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r1 = r14.mClient;	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r6 = r11.length();	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r10 = isNoMediaPath(r15);	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r8 = 0;	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r9 = 1;	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r2 = r15;	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r3 = r16;	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r1 = r1.doScanFile(r2, r3, r4, r6, r8, r9, r10);	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r14.releaseResources();
        return r1;
    L_0x005c:
        r0 = move-exception;
        r1 = "MediaScanner";	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r2 = "RemoteException in MediaScanner.scanFile()";	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        android.util.Log.e(r1, r2, r0);	 Catch:{ RemoteException -> 0x005c, all -> 0x006b }
        r1 = 0;
        r14.releaseResources();
        return r1;
    L_0x006b:
        r1 = move-exception;
        r14.releaseResources();
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaScanner.scanSingleFile(java.lang.String, java.lang.String):android.net.Uri");
    }

    public MediaScanner(Context c, String volumeName) {
        this.mClosed = new AtomicBoolean();
        this.mCloseGuard = CloseGuard.get();
        this.mSkipExternelQuery = false;
        this.mBitmapOptions = new Options();
        this.mPlaylistEntries = new ArrayList();
        this.mPlayLists = new ArrayList();
        this.mDrmManagerClient = null;
        this.mClient = new MyMediaScannerClient();
        native_setup();
        this.mContext = c;
        this.mPackageName = c.getPackageName();
        this.mVolumeName = volumeName;
        this.mBitmapOptions.inSampleSize = PATH_PLAYLISTS_COLUMN_INDEX;
        this.mBitmapOptions.inJustDecodeBounds = ENABLE_BULK_INSERTS;
        setDefaultRingtoneFileNames();
        this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        this.mAudioUri = Media.getContentUri(volumeName);
        this.mVideoUri = Video.Media.getContentUri(volumeName);
        this.mImagesUri = Images.Media.getContentUri(volumeName);
        this.mThumbsUri = Images.Thumbnails.getContentUri(volumeName);
        this.mFilesUri = Files.getContentUri(volumeName);
        this.mFilesUriNoNotify = this.mFilesUri.buildUpon().appendQueryParameter("nonotify", WifiEnterpriseConfig.ENGINE_ENABLE).build();
        if (volumeName.equals("internal")) {
            this.mProcessPlaylists = false;
            this.mProcessGenres = false;
            this.mPlaylistsUri = null;
        } else {
            this.mProcessPlaylists = ENABLE_BULK_INSERTS;
            this.mProcessGenres = ENABLE_BULK_INSERTS;
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

    private String settingSetIndicatorName(String base) {
        return base + "_set";
    }

    private boolean wasRingtoneAlreadySet(String name) {
        boolean z = false;
        try {
            if (System.getInt(this.mContext.getContentResolver(), settingSetIndicatorName(name)) != 0) {
                z = ENABLE_BULK_INSERTS;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

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
        }
    }

    private void prescan(String filePath, boolean prescanFiles) throws RemoteException {
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
            selectionArgs = new String[FILES_PRESCAN_FORMAT_COLUMN_INDEX];
            selectionArgs[ID_PLAYLISTS_COLUMN_INDEX] = ProxyInfo.LOCAL_EXCL_LIST;
            selectionArgs[PATH_PLAYLISTS_COLUMN_INDEX] = filePath;
        } else {
            where = "_id>?";
            selectionArgs = new String[PATH_PLAYLISTS_COLUMN_INDEX];
            selectionArgs[ID_PLAYLISTS_COLUMN_INDEX] = ProxyInfo.LOCAL_EXCL_LIST;
        }
        this.mDefaultRingtoneSet = wasRingtoneAlreadySet(System.RINGTONE);
        this.mDefaultNotificationSet = wasRingtoneAlreadySet(System.NOTIFICATION_SOUND);
        this.mDefaultAlarmSet = wasRingtoneAlreadySet(System.ALARM_ALERT);
        Builder builder = this.mFilesUri.buildUpon();
        builder.appendQueryParameter(MediaStore.PARAM_DELETE_DATA, "false");
        MediaBulkDeleter mediaBulkDeleter = new MediaBulkDeleter(this.mMediaProvider, builder.build());
        if (prescanFiles) {
            long lastId = Long.MIN_VALUE;
            Uri limitUri = this.mFilesUri.buildUpon().appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY, "1000").build();
            deleteFilesIfPossible();
            int count = ID_PLAYLISTS_COLUMN_INDEX;
            while (true) {
                selectionArgs[ID_PLAYLISTS_COLUMN_INDEX] = ProxyInfo.LOCAL_EXCL_LIST + lastId;
                if (c != null) {
                    c.close();
                }
                c = this.mMediaProvider.query(limitUri, FILES_PRESCAN_PROJECTION, where, selectionArgs, HwUserData._ID, null);
                if (c != null) {
                    if (c.getCount() == 0) {
                        break;
                    }
                    while (c.moveToNext()) {
                        long rowId = c.getLong(ID_PLAYLISTS_COLUMN_INDEX);
                        String path = c.getString(PATH_PLAYLISTS_COLUMN_INDEX);
                        int format = c.getInt(FILES_PRESCAN_FORMAT_COLUMN_INDEX);
                        long lastModified = c.getLong(FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX);
                        lastId = rowId;
                        if (path != null && path.startsWith("/")) {
                            boolean exists = false;
                            try {
                                exists = Os.access(path, OsConstants.F_OK);
                            } catch (ErrnoException e) {
                            }
                            if (!exists) {
                                if (!MtpConstants.isAbstractObject(format)) {
                                    int fileType;
                                    MediaFileType mediaFileType = MediaFile.getFileType(path);
                                    if (mediaFileType == null) {
                                        fileType = ID_PLAYLISTS_COLUMN_INDEX;
                                    } else {
                                        try {
                                            fileType = mediaFileType.fileType;
                                        } catch (Throwable th) {
                                            if (c != null) {
                                                c.close();
                                            }
                                            mediaBulkDeleter.flush();
                                        }
                                    }
                                    if (!MediaFile.isPlayListFileType(fileType)) {
                                        mediaBulkDeleter.delete(rowId);
                                        if (path.toLowerCase(Locale.US).endsWith("/.nomedia")) {
                                            mediaBulkDeleter.flush();
                                            this.mMediaProvider.call(MediaStore.UNHIDE_CALL, new File(path).getParent(), null);
                                        }
                                    }
                                }
                            }
                            if (count < MAX_ENTRY_SIZE) {
                                String key = path;
                                this.mFileCache.put(path, new FileEntry(rowId, path, lastModified, format));
                            }
                            count += PATH_PLAYLISTS_COLUMN_INDEX;
                        }
                    }
                } else {
                    break;
                }
            }
        }
        if (c != null) {
            c.close();
        }
        mediaBulkDeleter.flush();
        this.mOriginalCount = ID_PLAYLISTS_COLUMN_INDEX;
        ContentProviderClient contentProviderClient = this.mMediaProvider;
        Uri uri = this.mImagesUri;
        String[] strArr = new String[PATH_PLAYLISTS_COLUMN_INDEX];
        strArr[ID_PLAYLISTS_COLUMN_INDEX] = "COUNT(*)";
        c = contentProviderClient.query(uri, strArr, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                this.mOriginalCount = c.getInt(ID_PLAYLISTS_COLUMN_INDEX);
            }
            c.close();
        }
    }

    private boolean inScanDirectory(String path, String[] directories) {
        for (int i = ID_PLAYLISTS_COLUMN_INDEX; i < directories.length; i += PATH_PLAYLISTS_COLUMN_INDEX) {
            if (path.startsWith(directories[i])) {
                return ENABLE_BULK_INSERTS;
            }
        }
        return false;
    }

    private void pruneDeadThumbnailFiles() {
        HashSet<String> existingFiles = new HashSet();
        String directory = "/sdcard/DCIM/.thumbnails";
        String[] files = new File(directory).list();
        Cursor cursor = null;
        if (files == null) {
            files = new String[ID_PLAYLISTS_COLUMN_INDEX];
        }
        for (int i = ID_PLAYLISTS_COLUMN_INDEX; i < files.length; i += PATH_PLAYLISTS_COLUMN_INDEX) {
            existingFiles.add(directory + "/" + files[i]);
        }
        try {
            ContentProviderClient contentProviderClient = this.mMediaProvider;
            Uri uri = this.mThumbsUri;
            String[] strArr = new String[PATH_PLAYLISTS_COLUMN_INDEX];
            strArr[ID_PLAYLISTS_COLUMN_INDEX] = Voicemails._DATA;
            cursor = contentProviderClient.query(uri, strArr, null, null, null, null);
            Log.v(TAG, "pruneDeadThumbnailFiles... " + cursor);
            if (cursor == null || !cursor.moveToFirst()) {
                for (String fileToDelete : existingFiles) {
                    try {
                        new File(fileToDelete).delete();
                    } catch (SecurityException e) {
                    }
                }
                Log.v(TAG, "/pruneDeadThumbnailFiles... " + cursor);
                if (cursor != null) {
                    cursor.close();
                }
            }
            do {
                existingFiles.remove(cursor.getString(ID_PLAYLISTS_COLUMN_INDEX));
            } while (cursor.moveToNext());
            while (fileToDelete$iterator.hasNext()) {
                new File(fileToDelete).delete();
            }
            Log.v(TAG, "/pruneDeadThumbnailFiles... " + cursor);
            if (cursor != null) {
                cursor.close();
            }
        } catch (RemoteException e2) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void postscan(String[] directories) throws RemoteException {
        if (this.mProcessPlaylists) {
            processPlayLists();
        }
        if (this.mOriginalCount == 0 && this.mImagesUri.equals(Images.Media.getContentUri("external"))) {
            pruneDeadThumbnailFiles();
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

    private void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                if (!file.delete()) {
                    Log.w(TAG, "delete file failed.");
                }
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = ID_PLAYLISTS_COLUMN_INDEX; i < files.length; i += PATH_PLAYLISTS_COLUMN_INDEX) {
                    deleteFile(files[i]);
                }
                if (!file.delete()) {
                    Log.w(TAG, "delete file failed.");
                }
            }
            Log.i(TAG, "Delete the .nomedia file in the root directory.");
        }
    }

    public void scanDirectories(String[] directories) {
        try {
            long start = System.currentTimeMillis();
            prescan(null, ENABLE_BULK_INSERTS);
            long prescan = System.currentTimeMillis();
            this.mMediaInserter = new MediaInserter(this.mMediaProvider, Voice.QUALITY_VERY_HIGH);
            Log.d(TAG, "delete nomedia File when scanDirectories");
            deleteFile(new File(Environment.getExternalStorageDirectory() + "/.nomedia"));
            int i = ID_PLAYLISTS_COLUMN_INDEX;
            while (true) {
                int length = directories.length;
                if (i >= r0) {
                    break;
                }
                processDirectory(directories[i], this.mClient);
                i += PATH_PLAYLISTS_COLUMN_INDEX;
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
        this.mMediaProvider = this.mContext.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
        this.mMediaInserter = new MediaInserter(this.mMediaProvider, Voice.QUALITY_VERY_HIGH);
        HwFrameworkFactory.getHwMediaScannerManager().setMediaInserter(this.mMediaInserter);
        Log.d(TAG, "delete nomedia File when scanCustomDirectories");
        deleteFile(new File(Environment.getExternalStorageDirectory() + "/.nomedia"));
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isNoMediaFile(String path) {
        if (new File(path).isDirectory()) {
            return false;
        }
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0 && lastSlash + FILES_PRESCAN_FORMAT_COLUMN_INDEX < path.length()) {
            if (path.regionMatches(lastSlash + PATH_PLAYLISTS_COLUMN_INDEX, "._", ID_PLAYLISTS_COLUMN_INDEX, FILES_PRESCAN_FORMAT_COLUMN_INDEX)) {
                return ENABLE_BULK_INSERTS;
            }
            if (path.regionMatches(ENABLE_BULK_INSERTS, path.length() - 4, ".jpg", ID_PLAYLISTS_COLUMN_INDEX, 4)) {
                if (!path.regionMatches(ENABLE_BULK_INSERTS, lastSlash + PATH_PLAYLISTS_COLUMN_INDEX, "AlbumArt_{", ID_PLAYLISTS_COLUMN_INDEX, 10)) {
                    if (!path.regionMatches(ENABLE_BULK_INSERTS, lastSlash + PATH_PLAYLISTS_COLUMN_INDEX, "AlbumArt.", ID_PLAYLISTS_COLUMN_INDEX, 9)) {
                        int length = (path.length() - lastSlash) - 1;
                        if (length == 17) {
                        }
                        if (length == 10) {
                            if (path.regionMatches(ENABLE_BULK_INSERTS, lastSlash + PATH_PLAYLISTS_COLUMN_INDEX, "Folder", ID_PLAYLISTS_COLUMN_INDEX, 6)) {
                                return ENABLE_BULK_INSERTS;
                            }
                        }
                    }
                }
                return ENABLE_BULK_INSERTS;
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

    public static boolean isNoMediaPath(String path) {
        if (path == null) {
            return false;
        }
        if (path.indexOf("/.") >= 0) {
            return ENABLE_BULK_INSERTS;
        }
        int firstSlash = path.lastIndexOf(47);
        if (firstSlash <= 0) {
            return false;
        }
        String parent = path.substring(ID_PLAYLISTS_COLUMN_INDEX, firstSlash);
        synchronized (MediaScanner.class) {
            if (mNoMediaPaths.containsKey(parent)) {
                return ENABLE_BULK_INSERTS;
            }
            if (!mMediaPaths.containsKey(parent)) {
                int offset = PATH_PLAYLISTS_COLUMN_INDEX;
                while (offset >= 0) {
                    int slashIndex = path.indexOf(47, offset);
                    if (slashIndex > offset) {
                        slashIndex += PATH_PLAYLISTS_COLUMN_INDEX;
                        if (new File(path.substring(ID_PLAYLISTS_COLUMN_INDEX, slashIndex) + MediaStore.MEDIA_IGNORE_FILENAME).exists()) {
                            mNoMediaPaths.put(parent, ProxyInfo.LOCAL_EXCL_LIST);
                            return ENABLE_BULK_INSERTS;
                        }
                    }
                    offset = slashIndex;
                }
                mMediaPaths.put(parent, ProxyInfo.LOCAL_EXCL_LIST);
            }
            return isNoMediaFile(path);
        }
    }

    FileEntry makeEntryFor(String path) {
        Cursor cursor = null;
        try {
            String[] selectionArgs = new String[PATH_PLAYLISTS_COLUMN_INDEX];
            selectionArgs[ID_PLAYLISTS_COLUMN_INDEX] = path;
            cursor = this.mMediaProvider.query(this.mFilesUriNoNotify, FILES_PRESCAN_PROJECTION, "_data=?", selectionArgs, null, null);
            if (cursor.moveToFirst()) {
                String str = path;
                FileEntry fileEntry = new FileEntry(cursor.getLong(ID_PLAYLISTS_COLUMN_INDEX), str, cursor.getLong(FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX), cursor.getInt(FILES_PRESCAN_FORMAT_COLUMN_INDEX));
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int matchPaths(String path1, String path2) {
        int result = ID_PLAYLISTS_COLUMN_INDEX;
        int end1 = path1.length();
        int end2 = path2.length();
        while (end1 > 0 && end2 > 0) {
            int slash1 = path1.lastIndexOf(47, end1 - 1);
            int slash2 = path2.lastIndexOf(47, end2 - 1);
            int backSlash1 = path1.lastIndexOf(92, end1 - 1);
            int backSlash2 = path2.lastIndexOf(92, end2 - 1);
            int start1 = slash1 > backSlash1 ? slash1 : backSlash1;
            int start2 = slash2 > backSlash2 ? slash2 : backSlash2;
            start1 = start1 < 0 ? ID_PLAYLISTS_COLUMN_INDEX : start1 + PATH_PLAYLISTS_COLUMN_INDEX;
            start2 = start2 < 0 ? ID_PLAYLISTS_COLUMN_INDEX : start2 + PATH_PLAYLISTS_COLUMN_INDEX;
            int length = end1 - start1;
            if (end2 - start2 == length && path1.regionMatches(ENABLE_BULK_INSERTS, start1, path2, start2, length)) {
                result += PATH_PLAYLISTS_COLUMN_INDEX;
                end1 = start1 - 1;
                end2 = start2 - 1;
            }
        }
        return result;
    }

    private boolean matchEntries(long rowId, String data) {
        int len = this.mPlaylistEntries.size();
        boolean done = ENABLE_BULK_INSERTS;
        for (int i = ID_PLAYLISTS_COLUMN_INDEX; i < len; i += PATH_PLAYLISTS_COLUMN_INDEX) {
            PlaylistEntry entry = (PlaylistEntry) this.mPlaylistEntries.get(i);
            if (entry.bestmatchlevel != Preference.DEFAULT_ORDER) {
                done = false;
                if (data.equalsIgnoreCase(entry.path)) {
                    entry.bestmatchid = rowId;
                    entry.bestmatchlevel = Preference.DEFAULT_ORDER;
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
        boolean z = ENABLE_BULK_INSERTS;
        PlaylistEntry entry = new PlaylistEntry();
        int entryLength = line.length();
        while (entryLength > 0 && Character.isWhitespace(line.charAt(entryLength - 1))) {
            entryLength--;
        }
        if (entryLength >= FILES_PRESCAN_DATE_MODIFIED_COLUMN_INDEX) {
            boolean fullPath;
            if (entryLength < line.length()) {
                line = line.substring(ID_PLAYLISTS_COLUMN_INDEX, entryLength);
            }
            char ch1 = line.charAt(ID_PLAYLISTS_COLUMN_INDEX);
            if (ch1 == '/') {
                fullPath = ENABLE_BULK_INSERTS;
            } else if (Character.isLetter(ch1) && line.charAt(PATH_PLAYLISTS_COLUMN_INDEX) == ':') {
                if (line.charAt(FILES_PRESCAN_FORMAT_COLUMN_INDEX) != '\\') {
                    z = ID_PLAYLISTS_COLUMN_INDEX;
                }
                fullPath = z;
            } else {
                fullPath = false;
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
        while (fileList.moveToNext()) {
            if (matchEntries(fileList.getLong(ID_PLAYLISTS_COLUMN_INDEX), fileList.getString(PATH_PLAYLISTS_COLUMN_INDEX))) {
                break;
            }
        }
        int len = this.mPlaylistEntries.size();
        int index = ID_PLAYLISTS_COLUMN_INDEX;
        for (int i = ID_PLAYLISTS_COLUMN_INDEX; i < len; i += PATH_PLAYLISTS_COLUMN_INDEX) {
            PlaylistEntry entry = (PlaylistEntry) this.mPlaylistEntries.get(i);
            if (entry.bestmatchlevel > 0) {
                try {
                    values.clear();
                    values.put(Members.PLAY_ORDER, Integer.valueOf(index));
                    values.put(Members.AUDIO_ID, Long.valueOf(entry.bestmatchid));
                    this.mMediaProvider.insert(playlistUri, values);
                    index += PATH_PLAYLISTS_COLUMN_INDEX;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException in MediaScanner.processCachedPlaylist()", e);
                    return;
                }
            }
        }
        this.mPlaylistEntries.clear();
    }

    private void processM3uPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        IOException e;
        Throwable th;
        BufferedReader bufferedReader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), Process.PROC_OUT_LONG);
                try {
                    String line = reader.readLine();
                    this.mPlaylistEntries.clear();
                    while (line != null) {
                        if (line.length() > 0 && line.charAt(ID_PLAYLISTS_COLUMN_INDEX) != '#') {
                            cachePlaylistEntry(line, playListDirectory);
                        }
                        line = reader.readLine();
                    }
                    processCachedPlaylist(fileList, values, uri);
                    bufferedReader = reader;
                } catch (IOException e2) {
                    e = e2;
                    bufferedReader = reader;
                    try {
                        Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e3);
                                return;
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e32) {
                                Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e32);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = reader;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    throw th;
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e322) {
                    Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e322);
                }
            }
        } catch (IOException e4) {
            e322 = e4;
            Log.e(TAG, "IOException in MediaScanner.processM3uPlayList()", e322);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

    private void processPlsPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        IOException e;
        Throwable th;
        BufferedReader bufferedReader = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), Process.PROC_OUT_LONG);
                try {
                    this.mPlaylistEntries.clear();
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        if (line.startsWith("File")) {
                            int equals = line.indexOf(61);
                            if (equals > 0) {
                                cachePlaylistEntry(line.substring(equals + PATH_PLAYLISTS_COLUMN_INDEX), playListDirectory);
                            }
                        }
                    }
                    processCachedPlaylist(fileList, values, uri);
                    bufferedReader = reader;
                } catch (IOException e2) {
                    e = e2;
                    bufferedReader = reader;
                    try {
                        Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e3);
                                return;
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e32) {
                                Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e32);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bufferedReader = reader;
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    throw th;
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e322) {
                    Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e322);
                }
            }
        } catch (IOException e4) {
            e322 = e4;
            Log.e(TAG, "IOException in MediaScanner.processPlsPlayList()", e322);
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
    }

    private void processWplPlayList(String path, String playListDirectory, Uri uri, ContentValues values, Cursor fileList) {
        SAXException e;
        IOException e2;
        Throwable th;
        FileInputStream fileInputStream = null;
        try {
            File f = new File(path);
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                try {
                    this.mPlaylistEntries.clear();
                    Xml.parse(fis, Xml.findEncodingByName("UTF-8"), new WplHandler(playListDirectory, uri, fileList).getContentHandler());
                    processCachedPlaylist(fileList, values, uri);
                    fileInputStream = fis;
                } catch (SAXException e3) {
                    e = e3;
                    fileInputStream = fis;
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22) {
                            Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e22);
                            return;
                        }
                    }
                } catch (IOException e4) {
                    e22 = e4;
                    fileInputStream = fis;
                    try {
                        e22.printStackTrace();
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e222) {
                                Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e222);
                                return;
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e2222) {
                                Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e2222);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = fis;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e22222) {
                    Log.e(TAG, "IOException in MediaScanner.processWplPlayList()", e22222);
                }
            }
        } catch (SAXException e5) {
            e = e5;
            e.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException e6) {
            e22222 = e6;
            e22222.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
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
        String name = values.getAsString(KeyChain.EXTRA_NAME);
        if (name == null) {
            name = values.getAsString(Bookmarks.TITLE);
            if (name == null) {
                int lastDot = path.lastIndexOf(46);
                if (lastDot < 0) {
                    name = path.substring(lastSlash + PATH_PLAYLISTS_COLUMN_INDEX);
                } else {
                    name = path.substring(lastSlash + PATH_PLAYLISTS_COLUMN_INDEX, lastDot);
                }
            }
        }
        values.put(KeyChain.EXTRA_NAME, name);
        values.put(PlaylistsColumns.DATE_MODIFIED, Long.valueOf(entry.mLastModified));
        Uri uri;
        if (rowId == 0) {
            values.put(Voicemails._DATA, path);
            uri = this.mMediaProvider.insert(this.mPlaylistsUri, values);
            rowId = ContentUris.parseId(uri);
            membersUri = Uri.withAppendedPath(uri, Members.CONTENT_DIRECTORY);
        } else {
            uri = ContentUris.withAppendedId(this.mPlaylistsUri, rowId);
            this.mMediaProvider.update(uri, values, null, null);
            membersUri = Uri.withAppendedPath(uri, Members.CONTENT_DIRECTORY);
            this.mMediaProvider.delete(membersUri, null, null);
        }
        String playListDirectory = path.substring(ID_PLAYLISTS_COLUMN_INDEX, lastSlash + PATH_PLAYLISTS_COLUMN_INDEX);
        MediaFileType mediaFileType = MediaFile.getFileType(path);
        int fileType = mediaFileType == null ? ID_PLAYLISTS_COLUMN_INDEX : mediaFileType.fileType;
        if (fileType == 44) {
            processM3uPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 45) {
            processPlsPlayList(path, playListDirectory, membersUri, values, fileList);
        } else if (fileType == 46) {
            processWplPlayList(path, playListDirectory, membersUri, values, fileList);
        }
    }

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
        }
    }

    public void close() {
        this.mCloseGuard.close();
        if (this.mClosed.compareAndSet(false, ENABLE_BULK_INSERTS)) {
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
}
