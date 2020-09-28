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

    public int hwBeginSendObject(String path, Cursor c) {
        ExternalStorageFileImplEx tempFile = new ExternalStorageFileImplEx(path);
        if (tempFile.exists()) {
            if (DEBUG) {
                Log.w(TAG, path + " exist, delete it");
            }
            if (!tempFile.delete()) {
                Log.i(TAG, "delete fail.");
            }
        }
        if (c == null) {
            return 0;
        }
        c.moveToNext();
        int handleExist = c.getInt(c.getColumnIndex("_id"));
        if (DEBUG) {
            Log.w(TAG, "file already exists in beginSendObject: " + path + ", ID = " + handleExist);
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

    public MtpPropertyListEx getObjectPropertyList(int handle, int format, int[] proplist) {
        synchronized (LOCK) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle == this.mObjectColumnInfo.handle) {
                    if (format == 0 || this.mObjectColumnInfo.format == format) {
                        return getCurrentPropertyList(handle, proplist);
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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0020, code lost:
        if (r8 >= r1) goto L_0x0201;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0022, code lost:
        r9 = r15[r8];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0029, code lost:
        switch(r9) {
            case 56321: goto L_0x01ee;
            case 56322: goto L_0x01df;
            case 56323: goto L_0x01d5;
            case 56324: goto L_0x01c6;
            case 56327: goto L_0x01a1;
            case 56329: goto L_0x0192;
            case 56331: goto L_0x0182;
            case 56385: goto L_0x016a;
            case 56388: goto L_0x0103;
            case 56390: goto L_0x00ee;
            case 56392: goto L_0x00e3;
            case 56398: goto L_0x00d4;
            case 56457: goto L_0x00c4;
            case 56459: goto L_0x00b2;
            case 56460: goto L_0x0098;
            case 56470: goto L_0x008d;
            case 56473: goto L_0x006d;
            case 56474: goto L_0x0058;
            case 56475: goto L_0x004d;
            case 56544: goto L_0x0042;
            case 56978: goto L_0x01d5;
            case 56979: goto L_0x0037;
            case 56980: goto L_0x01d5;
            case 56985: goto L_0x0037;
            case 56986: goto L_0x0037;
            default: goto L_0x002c;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002c, code lost:
        r0.append(r14, r9, 0, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0037, code lost:
        r0.append(r14, r9, 6, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0042, code lost:
        r0.append(r14, r9, r13.mObjectColumnInfo.displayName);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004d, code lost:
        r0.append(r14, r9, r13.mObjectColumnInfo.albumArtist);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0058, code lost:
        r2 = android.media.BuildConfig.FLAVOR;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0060, code lost:
        if (r13.mObjectColumnInfo.mediaType != 2) goto L_0x0068;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0062, code lost:
        r2 = r13.mObjectColumnInfo.album;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0068, code lost:
        r0.append(r14, r9, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006d, code lost:
        r0.append(r14, r9, java.lang.Integer.toString(r13.mObjectColumnInfo.year) + "0101T000000");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x008d, code lost:
        r0.append(r14, r9, r13.mObjectColumnInfo.composer);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x009e, code lost:
        if (r13.mObjectColumnInfo.mediaType != 2) goto L_0x00ab;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00a0, code lost:
        r0.append(r14, r9, r13.mObjectColumnInfo.name);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00ab, code lost:
        r0.append(r14, r9, android.media.BuildConfig.FLAVOR);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00b2, code lost:
        r0.append(r14, r9, 4, (long) (r13.mObjectColumnInfo.track % android.mtp.HwMtpDatabaseImpl.CONST_KILO));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00c4, code lost:
        r0.append(r14, r9, 6, (long) r13.mObjectColumnInfo.duration);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00d4, code lost:
        r0.append(r14, r9, formatDateTime(r13.mObjectColumnInfo.dateAdded));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00e3, code lost:
        r0.append(r14, r9, r13.mObjectColumnInfo.description);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00ee, code lost:
        r2 = android.media.BuildConfig.FLAVOR;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00f6, code lost:
        if (r13.mObjectColumnInfo.mediaType != 2) goto L_0x00fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00f8, code lost:
        r2 = r13.mObjectColumnInfo.artist;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00fe, code lost:
        r0.append(r14, r9, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x010c, code lost:
        if (r13.mObjectColumnInfo.types.length <= 9) goto L_0x0123;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0116, code lost:
        if (r13.mObjectColumnInfo.types[9] == 0) goto L_0x0123;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0118, code lost:
        r0.append(r14, r9, r13.mObjectColumnInfo.title);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x012c, code lost:
        if (r13.mObjectColumnInfo.types.length <= android.mtp.HwMtpDatabaseImpl.COLUMN_NUM_NAME) goto L_0x0143;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0136, code lost:
        if (r13.mObjectColumnInfo.types[android.mtp.HwMtpDatabaseImpl.COLUMN_NUM_NAME] == 0) goto L_0x0143;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0138, code lost:
        r0.append(r14, r9, r13.mObjectColumnInfo.name);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x014a, code lost:
        if (r13.mObjectColumnInfo.types.length <= 4) goto L_0x0165;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0154, code lost:
        if (r13.mObjectColumnInfo.types[4] == 0) goto L_0x0165;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0156, code lost:
        r0.append(r14, r9, nameFromPath(r13.mObjectColumnInfo.data));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0165, code lost:
        r0.setResult((int) android.mtp.HwMtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x016a, code lost:
        r0.append(r14, r9, 10, (((long) r13.mObjectColumnInfo.storageId) << 32) + ((long) r14));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0182, code lost:
        r0.append(r14, r9, 6, (long) r13.mObjectColumnInfo.parent);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0192, code lost:
        r0.append(r14, r9, formatDateTime(r13.mObjectColumnInfo.dateModified));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x01a8, code lost:
        if (r13.mObjectColumnInfo.types.length <= 4) goto L_0x01c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x01b2, code lost:
        if (r13.mObjectColumnInfo.types[4] == 0) goto L_0x01c2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01b4, code lost:
        r0.append(r14, r9, nameFromPath(r13.mObjectColumnInfo.data));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x01c2, code lost:
        r0.setResult((int) android.mtp.HwMtpConstants.RESPONSE_INVALID_OBJECT_HANDLE);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x01c6, code lost:
        r0.append(r14, r9, 8, r13.mObjectColumnInfo.size);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x01d5, code lost:
        r0.append(r14, r9, 4, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x01df, code lost:
        r0.append(r14, r9, 4, (long) r13.mObjectColumnInfo.format);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x01ee, code lost:
        r0.append(r14, r9, 6, (long) r13.mObjectColumnInfo.storageId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x01fd, code lost:
        r8 = r8 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0201, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0016, code lost:
        r0 = new android.mtp.MtpPropertyListEx((int) android.mtp.HwMtpConstants.RESPONSE_OK);
        r1 = r15.length;
        r8 = 0;
     */
    private MtpPropertyListEx getCurrentPropertyList(int handle, int[] proplist) {
        synchronized (LOCK) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle != this.mObjectColumnInfo.handle) {
                }
            }
            return null;
        }
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
