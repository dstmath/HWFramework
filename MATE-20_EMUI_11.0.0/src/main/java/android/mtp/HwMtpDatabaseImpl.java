package android.mtp;

import android.database.Cursor;
import android.media.BuildConfig;
import android.util.Log;
import com.huawei.android.os.storage.ExternalStorageFileImplEx;
import java.io.File;

public class HwMtpDatabaseImpl extends DefaultHwMtpDatabaseManager {
    private static final int COLUMN_NUM_ALBUM = 17;
    private static final int COLUMN_NUM_ALBUM_ARTIST = 18;
    private static final int COLUMN_NUM_ARTIST = 16;
    private static final int COLUMN_NUM_COMPOSER = 22;
    private static final int COLUMN_NUM_DATA = 4;
    private static final int COLUMN_NUM_DATE_ADDED = 6;
    private static final int COLUMN_NUM_DESCRIPTION = 15;
    private static final int COLUMN_NUM_DISPLAYNAME = 10;
    private static final int COLUMN_NUM_DURATION = 19;
    private static final int COLUMN_NUM_FORMAT = 2;
    private static final int COLUMN_NUM_HEIGHT = 14;
    private static final int COLUMN_NUM_ISDRM = 12;
    private static final int COLUMN_NUM_MEDIA_TYPE = 11;
    private static final int COLUMN_NUM_MIME_TYPE = 8;
    private static final int COLUMN_NUM_NAME = 23;
    private static final int COLUMN_NUM_TITLE = 9;
    private static final int COLUMN_NUM_TRACK = 20;
    private static final int COLUMN_NUM_WIDTH = 13;
    private static final int COLUMN_NUM_YEAR = 21;
    private static final int COLUMN_TOTLE_NUM = 24;
    private static final int CONST_KILO = 1000;
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int EIGHT_BITS_MAX = 255;
    private static final int GROUP_4_PROP = 4;
    private static final int GROUP_8_PROP = 8;
    private static final Object LOCK = new Object();
    private static final String[] OBJECT_ALL_COLUMNS_PROJECTION = {"_id", "storage_id", "format", "parent", "_data", "_size", "date_added", "date_modified", "mime_type", "title", "_display_name", "media_type", "is_drm", "width", "height", "description", "artist", "album", "album_artist", "duration", "track", "composer", "year", "name"};
    private static final int PREFIX_LENGTH = 32;
    private static final int ROOT_DIR_PARENT_ID = 0;
    private static final int STORAGE_INDEX_FORMAT = 1;
    private static final int STORAGE_INDEX_ID = 0;
    private static final int STORAGE_INDEX_PARENT = 2;
    private static final String TAG = "HwMtpDatabaseImpl";
    private static HwMtpDatabaseImpl sHwMtpDatabaseManager = new HwMtpDatabaseImpl();
    private int[] mGroup4Props = {HwMtpConstants.PROPERTY_OBJECT_FORMAT, HwMtpConstants.PROPERTY_OBJECT_SIZE};
    private int[] mGroup8Props = {HwMtpConstants.PROPERTY_PROTECTION_STATUS, HwMtpConstants.PROPERTY_OBJECT_FILE_NAME, HwMtpConstants.PROPERTY_NAME, HwMtpConstants.PROPERTY_DATE_MODIFIED, HwMtpConstants.PROPERTY_DATE_ADDED, HwMtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE, HwMtpConstants.PROPERTY_PERSISTENT_UID, HwMtpConstants.PROPERTY_TRACK, HwMtpConstants.PROPERTY_ARTIST, HwMtpConstants.PROPERTY_ALBUM_NAME, HwMtpConstants.PROPERTY_GENRE, HwMtpConstants.PROPERTY_AUDIO_WAVE_CODEC, HwMtpConstants.PROPERTY_AUDIO_BITRATE, HwMtpConstants.PROPERTY_SAMPLE_RATE, HwMtpConstants.PROPERTY_BITRATE_TYPE, HwMtpConstants.PROPERTY_NUMBER_OF_CHANNELS, HwMtpConstants.PROPERTY_STORAGE_ID, HwMtpConstants.PROPERTY_PARENT_OBJECT, HwMtpConstants.PROPERTY_DURATION, HwMtpConstants.PROPERTY_DISPLAY_NAME, HwMtpConstants.PROPERTY_ALBUM_ARTIST, HwMtpConstants.PROPERTY_COMPOSER, HwMtpConstants.PROPERTY_DESCRIPTION};
    private MtpObjectColumnInfo mObjectColumnInfo = new MtpObjectColumnInfo();

    private HwMtpDatabaseImpl() {
    }

    public static HwMtpDatabaseImpl getDefault() {
        return sHwMtpDatabaseManager;
    }

    public int hwBeginSendObject(String path, Cursor cursor) {
        ExternalStorageFileImplEx tempFile = new ExternalStorageFileImplEx(path);
        if (tempFile.exists()) {
            if (DEBUG) {
                Log.w(TAG, " exist, delete it");
            }
            if (!tempFile.delete()) {
                Log.i(TAG, "delete fail.");
            }
        }
        if (cursor == null) {
            return 0;
        }
        cursor.moveToNext();
        int handleExist = cursor.getInt(cursor.getColumnIndex("_id"));
        if (DEBUG) {
            Log.w(TAG, "file already exists in beginSendObject: ID = " + handleExist);
        }
        return handleExist;
    }

    public void hwSaveCurrentObject(MtpObjectEx mtpObject, Cursor cursor) {
        if (mtpObject == null) {
            Log.w(TAG, "hwSaveCurrentObject(): mtpObject is null!");
            return;
        }
        if (cursor != null) {
            cursor.moveToFirst();
        }
        synchronized (LOCK) {
            if (cursor != null) {
                for (int i = 0; i < COLUMN_TOTLE_NUM; i++) {
                    this.mObjectColumnInfo.types[i] = cursor.getType(i);
                }
            }
            this.mObjectColumnInfo.handle = mtpObject.getId();
            this.mObjectColumnInfo.storageId = mtpObject.getStorageId();
            this.mObjectColumnInfo.format = cursor != null ? cursor.getInt(2) : mtpObject.getFormat();
            this.mObjectColumnInfo.parent = mtpObject.getParentId();
            if (cursor != null) {
                this.mObjectColumnInfo.data = cursor.getString(4);
            } else {
                this.mObjectColumnInfo.data = mtpObject.getPath();
            }
            this.mObjectColumnInfo.size = mtpObject.getSize();
            this.mObjectColumnInfo.dateAdded = cursor != null ? cursor.getLong(6) : 0;
            this.mObjectColumnInfo.dateModified = mtpObject.getModifiedTime();
            int i2 = 0;
            this.mObjectColumnInfo.mimeType = cursor != null ? cursor.getInt(8) : 0;
            this.mObjectColumnInfo.title = cursor != null ? cursor.getString(9) : BuildConfig.FLAVOR;
            this.mObjectColumnInfo.displayName = cursor != null ? cursor.getString(10) : BuildConfig.FLAVOR;
            this.mObjectColumnInfo.mediaType = cursor != null ? cursor.getInt(COLUMN_NUM_MEDIA_TYPE) : 0;
            this.mObjectColumnInfo.isDrm = cursor != null ? cursor.getInt(12) : 0;
            this.mObjectColumnInfo.width = cursor != null ? cursor.getInt(COLUMN_NUM_WIDTH) : 0;
            this.mObjectColumnInfo.height = cursor != null ? cursor.getInt(COLUMN_NUM_HEIGHT) : 0;
            this.mObjectColumnInfo.description = cursor != null ? cursor.getString(COLUMN_NUM_DESCRIPTION) : BuildConfig.FLAVOR;
            this.mObjectColumnInfo.artist = cursor != null ? cursor.getString(16) : BuildConfig.FLAVOR;
            this.mObjectColumnInfo.album = cursor != null ? cursor.getString(COLUMN_NUM_ALBUM) : BuildConfig.FLAVOR;
            this.mObjectColumnInfo.albumArtist = cursor != null ? cursor.getString(COLUMN_NUM_ALBUM_ARTIST) : BuildConfig.FLAVOR;
            this.mObjectColumnInfo.duration = cursor != null ? cursor.getInt(COLUMN_NUM_DURATION) : 0;
            this.mObjectColumnInfo.track = cursor != null ? cursor.getInt(COLUMN_NUM_TRACK) : 0;
            MtpObjectColumnInfo mtpObjectColumnInfo = this.mObjectColumnInfo;
            if (cursor != null) {
                i2 = cursor.getInt(COLUMN_NUM_YEAR);
            }
            mtpObjectColumnInfo.year = i2;
            this.mObjectColumnInfo.composer = cursor != null ? cursor.getString(COLUMN_NUM_COMPOSER) : BuildConfig.FLAVOR;
            this.mObjectColumnInfo.name = mtpObject.getName();
        }
    }

    public void hwClearSavedObject() {
        synchronized (LOCK) {
            this.mObjectColumnInfo.handle = -1;
        }
    }

    public int hwGetSavedObjectHandle() {
        int i;
        synchronized (LOCK) {
            i = this.mObjectColumnInfo.handle;
        }
        return i;
    }

    public MtpPropertyListEx getObjectPropertyList(int handle, int format, int property, int groupCode) {
        if (groupCode == 0) {
            return null;
        }
        if (groupCode == 4 || groupCode == 8) {
            return getCurrentPropertyList(handle, getGroupObjectProperties(groupCode));
        }
        return new MtpPropertyListEx((int) HwMtpConstants.RESPONSE_GROUP_NOT_SUPPORTED);
    }

    public MtpPropertyListEx getObjectPropertyList(int property, int handle) {
        synchronized (LOCK) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle == this.mObjectColumnInfo.handle) {
                    return getCurrentPropertyList(handle, new int[]{property});
                }
            }
            return null;
        }
    }

    public MtpPropertyListEx getObjectPropertyList(int handle, int format, int[] propList) {
        synchronized (LOCK) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle == this.mObjectColumnInfo.handle) {
                    if (format == 0 || this.mObjectColumnInfo.format == format) {
                        return getCurrentPropertyList(handle, propList);
                    }
                    return new MtpPropertyListEx((int) HwMtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
                }
            }
            return null;
        }
    }

    public int hwGetObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        synchronized (LOCK) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle == this.mObjectColumnInfo.handle) {
                    String path = this.mObjectColumnInfo.data;
                    path.getChars(0, path.length(), outFilePath, 0);
                    outFilePath[path.length()] = 0;
                    outFileLengthFormat[0] = new File(path).length();
                    outFileLengthFormat[1] = (long) this.mObjectColumnInfo.format;
                    return 0;
                }
            }
            return -1;
        }
    }

    public int hwGetObjectFormat(int handle) {
        synchronized (LOCK) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle == this.mObjectColumnInfo.handle) {
                    return this.mObjectColumnInfo.format;
                }
            }
            return -1;
        }
    }

    public boolean hwGetObjectReferences(int handle) {
        synchronized (LOCK) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle == this.mObjectColumnInfo.handle) {
                    if (this.mObjectColumnInfo.types.length <= COLUMN_NUM_MEDIA_TYPE || this.mObjectColumnInfo.types[COLUMN_NUM_MEDIA_TYPE] == 0 || 4 == this.mObjectColumnInfo.mediaType) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class MtpObjectColumnInfo {
        private String album;
        private String albumArtist;
        private String artist;
        private String composer;
        private String data;
        private long dateAdded;
        private long dateModified;
        private String description;
        private String displayName;
        private int duration;
        private int format;
        private int handle = -1;
        private int height;
        private int isDrm;
        private int mediaType;
        private int mimeType;
        private String name;
        private int parent;
        private long size;
        private int storageId;
        private String title;
        private int track;
        private int[] types = new int[HwMtpDatabaseImpl.COLUMN_TOTLE_NUM];
        private int width;
        private int year;

        public int getHandle() {
            int i;
            synchronized (HwMtpDatabaseImpl.LOCK) {
                i = this.handle;
            }
            return i;
        }

        public void setHandle(int handle2) {
            synchronized (HwMtpDatabaseImpl.LOCK) {
                this.handle = handle2;
            }
        }

        public int getStorageId() {
            return this.storageId;
        }

        public void setStorageId(int storageId2) {
            this.storageId = storageId2;
        }

        public int getFormat() {
            return this.format;
        }

        public void setFormat(int format2) {
            this.format = format2;
        }

        public int getParent() {
            return this.parent;
        }

        public void setParent(int parent2) {
            this.parent = parent2;
        }

        public String getData() {
            return this.data;
        }

        public void setData(String data2) {
            this.data = data2;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name2) {
            this.name = name2;
        }

        public long getSize() {
            return this.size;
        }

        public void setSize(long size2) {
            this.size = size2;
        }

        public long getDateAdded() {
            return this.dateAdded;
        }

        public void setDateAdded(long dateAdded2) {
            this.dateAdded = dateAdded2;
        }

        public long getDateModified() {
            return this.dateModified;
        }

        public void setDateModified(long dateModified2) {
            this.dateModified = dateModified2;
        }

        public int getMimeType() {
            return this.mimeType;
        }

        public void setMimeType(int mimeType2) {
            this.mimeType = mimeType2;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title2) {
            this.title = title2;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public void setDisplayName(String displayName2) {
            this.displayName = displayName2;
        }

        public int getMediaType() {
            return this.mediaType;
        }

        public void setMediaType(int mediaType2) {
            this.mediaType = mediaType2;
        }

        public int getIsDrm() {
            return this.isDrm;
        }

        public void setIsDrm(int isDrm2) {
            this.isDrm = isDrm2;
        }

        public int getWidth() {
            return this.width;
        }

        public void setWidth(int width2) {
            this.width = width2;
        }

        public int getHeight() {
            return this.height;
        }

        public void setHeight(int height2) {
            this.height = height2;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description2) {
            this.description = description2;
        }

        public String getArtist() {
            return this.artist;
        }

        public void setArtist(String artist2) {
            this.artist = artist2;
        }

        public String getAlbum() {
            return this.album;
        }

        public void setAlbum(String album2) {
            this.album = album2;
        }

        public String getAlbumArtist() {
            return this.albumArtist;
        }

        public void setAlbumArtist(String albumArtist2) {
            this.albumArtist = albumArtist2;
        }

        public int getDuration() {
            return this.duration;
        }

        public void setDuration(int duration2) {
            this.duration = duration2;
        }

        public int getTrack() {
            return this.track;
        }

        public void setTrack(int track2) {
            this.track = track2;
        }

        public int getYear() {
            return this.year;
        }

        public void setYear(int year2) {
            this.year = year2;
        }

        public String getComposer() {
            return this.composer;
        }

        public void setComposer(String composer2) {
            this.composer = composer2;
        }

        public int[] getTypes() {
            return this.types;
        }

        public void setTypes(int[] types2) {
            this.types = types2;
        }

        public String toString() {
            return "MtpObjectColumnInfo[mimeType : " + this.mimeType + ", isDrm : " + this.isDrm + ", width : " + this.width + ", height : " + this.height + "]";
        }
    }

    public boolean hwGetObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outModified) {
        synchronized (LOCK) {
            try {
                if (outStorageFormatParent.length <= 2) {
                    return false;
                }
                if (this.mObjectColumnInfo.handle > 0) {
                    if (this.mObjectColumnInfo.handle == handle) {
                        outStorageFormatParent[0] = this.mObjectColumnInfo.storageId;
                        outStorageFormatParent[1] = this.mObjectColumnInfo.format;
                        outStorageFormatParent[2] = this.mObjectColumnInfo.parent;
                        composeObjectInfoParemeters(outName, outModified, this.mObjectColumnInfo.data, this.mObjectColumnInfo.size, this.mObjectColumnInfo.dateModified);
                        return true;
                    }
                }
                return false;
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    private void composeObjectInfoParemeters(char[] outName, long[] outSizeModified, String data, long size, long dateModified) {
        if (data != null) {
            int lastSlash = data.lastIndexOf(47);
            int start = lastSlash >= 0 ? lastSlash + 1 : 0;
            int end = data.length();
            if (end - start > EIGHT_BITS_MAX) {
                end = start + EIGHT_BITS_MAX;
            }
            data.getChars(start, end, outName, 0);
            outName[end - start] = 0;
            outSizeModified[0] = size;
            outSizeModified[1] = dateModified;
        }
    }

    private int[] getGroupObjectProperties(int groupCode) {
        if (groupCode == 4) {
            return this.mGroup4Props;
        }
        if (groupCode == 8) {
            return this.mGroup8Props;
        }
        return new int[0];
    }

    private MtpPropertyListEx getCurrentPropertyList(int handle, int[] proplist) {
        synchronized (LOCK) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle != this.mObjectColumnInfo.handle) {
                }
            }
            return null;
        }
        MtpPropertyListEx result = new MtpPropertyListEx((int) HwMtpConstants.RESPONSE_OK);
        for (int item : proplist) {
            switch (item) {
                case HwMtpConstants.PROPERTY_STORAGE_ID /* 56321 */:
                    result.append(handle, item, 6, (long) this.mObjectColumnInfo.storageId);
                    break;
                case HwMtpConstants.PROPERTY_OBJECT_FORMAT /* 56322 */:
                    result.append(handle, item, 4, (long) this.mObjectColumnInfo.format);
                    break;
                case HwMtpConstants.PROPERTY_PROTECTION_STATUS /* 56323 */:
                case HwMtpConstants.PROPERTY_BITRATE_TYPE /* 56978 */:
                case HwMtpConstants.PROPERTY_NUMBER_OF_CHANNELS /* 56980 */:
                    result.append(handle, item, 4, 0);
                    break;
                case HwMtpConstants.PROPERTY_OBJECT_SIZE /* 56324 */:
                    result.append(handle, item, 8, this.mObjectColumnInfo.size);
                    break;
                case HwMtpConstants.PROPERTY_OBJECT_FILE_NAME /* 56327 */:
                    if (this.mObjectColumnInfo.types.length <= 4 || this.mObjectColumnInfo.types[4] == 0) {
                        result.setResult((int) HwMtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
                        break;
                    } else {
                        result.append(handle, item, nameFromPath(this.mObjectColumnInfo.data));
                        break;
                    }
                    break;
                case HwMtpConstants.PROPERTY_DATE_MODIFIED /* 56329 */:
                    result.append(handle, item, formatDateTime(this.mObjectColumnInfo.dateModified));
                    break;
                case HwMtpConstants.PROPERTY_PARENT_OBJECT /* 56331 */:
                    result.append(handle, item, 6, (long) this.mObjectColumnInfo.parent);
                    break;
                case HwMtpConstants.PROPERTY_PERSISTENT_UID /* 56385 */:
                    result.append(handle, item, 10, (((long) this.mObjectColumnInfo.storageId) << 32) + ((long) handle));
                    break;
                case HwMtpConstants.PROPERTY_NAME /* 56388 */:
                    if (this.mObjectColumnInfo.types.length <= 9 || this.mObjectColumnInfo.types[9] == 0) {
                        if (this.mObjectColumnInfo.types.length <= COLUMN_NUM_NAME || this.mObjectColumnInfo.types[COLUMN_NUM_NAME] == 0) {
                            if (this.mObjectColumnInfo.types.length <= 4 || this.mObjectColumnInfo.types[4] == 0) {
                                result.setResult((int) HwMtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
                                break;
                            } else {
                                result.append(handle, item, nameFromPath(this.mObjectColumnInfo.data));
                                break;
                            }
                        } else {
                            result.append(handle, item, this.mObjectColumnInfo.name);
                            break;
                        }
                    } else {
                        result.append(handle, item, this.mObjectColumnInfo.title);
                        break;
                    }
                    break;
                case HwMtpConstants.PROPERTY_ARTIST /* 56390 */:
                    String artist = BuildConfig.FLAVOR;
                    if (this.mObjectColumnInfo.mediaType == 2) {
                        artist = this.mObjectColumnInfo.artist;
                    }
                    result.append(handle, item, artist);
                    break;
                case HwMtpConstants.PROPERTY_DESCRIPTION /* 56392 */:
                    result.append(handle, item, this.mObjectColumnInfo.description);
                    break;
                case HwMtpConstants.PROPERTY_DATE_ADDED /* 56398 */:
                    result.append(handle, item, formatDateTime(this.mObjectColumnInfo.dateAdded));
                    break;
                case HwMtpConstants.PROPERTY_DURATION /* 56457 */:
                    result.append(handle, item, 6, (long) this.mObjectColumnInfo.duration);
                    break;
                case HwMtpConstants.PROPERTY_TRACK /* 56459 */:
                    result.append(handle, item, 4, (long) (this.mObjectColumnInfo.track % CONST_KILO));
                    break;
                case HwMtpConstants.PROPERTY_GENRE /* 56460 */:
                    if (this.mObjectColumnInfo.mediaType == 2) {
                        result.append(handle, item, this.mObjectColumnInfo.name);
                        break;
                    } else {
                        result.append(handle, item, BuildConfig.FLAVOR);
                        break;
                    }
                case HwMtpConstants.PROPERTY_COMPOSER /* 56470 */:
                    result.append(handle, item, this.mObjectColumnInfo.composer);
                    break;
                case HwMtpConstants.PROPERTY_ORIGINAL_RELEASE_DATE /* 56473 */:
                    result.append(handle, item, Integer.toString(this.mObjectColumnInfo.year) + "0101T000000");
                    break;
                case HwMtpConstants.PROPERTY_ALBUM_NAME /* 56474 */:
                    String album = BuildConfig.FLAVOR;
                    if (this.mObjectColumnInfo.mediaType == 2) {
                        album = this.mObjectColumnInfo.album;
                    }
                    result.append(handle, item, album);
                    break;
                case HwMtpConstants.PROPERTY_ALBUM_ARTIST /* 56475 */:
                    result.append(handle, item, this.mObjectColumnInfo.albumArtist);
                    break;
                case HwMtpConstants.PROPERTY_DISPLAY_NAME /* 56544 */:
                    result.append(handle, item, this.mObjectColumnInfo.displayName);
                    break;
                case HwMtpConstants.PROPERTY_SAMPLE_RATE /* 56979 */:
                case HwMtpConstants.PROPERTY_AUDIO_WAVE_CODEC /* 56985 */:
                case HwMtpConstants.PROPERTY_AUDIO_BITRATE /* 56986 */:
                    result.append(handle, item, 6, 0);
                    break;
                default:
                    result.append(handle, item, 0, 0);
                    break;
            }
        }
        return result;
    }

    private static String nameFromPath(String path) {
        int start = 0;
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0) {
            start = lastSlash + 1;
        }
        int end = path.length();
        if (end - start > EIGHT_BITS_MAX) {
            end = start + EIGHT_BITS_MAX;
        }
        return path.substring(start, end);
    }

    private static String formatDateTime(long seconds) {
        return MtpPropertyGroupEx.formatDateTime(seconds);
    }
}
