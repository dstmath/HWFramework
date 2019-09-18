package android.mtp;

import android.database.Cursor;
import android.mtp.MtpStorageManager;
import android.os.storage.ExternalStorageFileImpl;
import android.rms.iaware.DataContract;
import android.util.Log;
import java.io.File;

public class HwMtpDatabaseImpl implements HwMtpDatabaseManager {
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
    private static final int FILE_SIZE_MAGNIFICATION = 3;
    private static final int GROUP_4_PROP = 4;
    private static final int GROUP_8_PROP = 8;
    private static final String[] OBJECT_ALL_COLUMNS_PROJECTION = {"_id", "storage_id", "format", "parent", "_data", "_size", "date_added", "date_modified", "mime_type", "title", "_display_name", "media_type", "is_drm", "width", "height", "description", "artist", "album", "album_artist", DataContract.DevStatusProperty.VIBRATOR_DURATION, "track", "composer", "year", "name"};
    private static final int PREFIX_LENGTH = 32;
    private static final int ROOT_DIR_PARENT_ID = 0;
    private static final int STORAGE_INDEX_FORMAT = 1;
    private static final int STORAGE_INDEX_ID = 0;
    private static final int STORAGE_INDEX_PARENT = 2;
    private static final String SUFFIX_JPG_FILE = ".jpg";
    private static final String TAG = "HwMtpDatabaseImpl";
    private static HwMtpDatabaseManager mHwMtpDatabaseManager = new HwMtpDatabaseImpl();
    private int[] mGroup4Props = {56322, 56324};
    private int[] mGroup8Props = {56323, 56327, 56388, 56329, 56398, 56473, 56385, 56459, 56390, 56474, 56460, 56985, 56986, 56979, 56978, 56980, 56321, 56331, 56457, 56544, 56475, 56470, 56392};
    private Object mLockObject = new Object();
    private MtpObjectColumnInfo mObjectColumnInfo = new MtpObjectColumnInfo();

    private static class MtpObjectColumnInfo {
        /* access modifiers changed from: private */
        public String album;
        /* access modifiers changed from: private */
        public String albumArtist;
        /* access modifiers changed from: private */
        public String artist;
        /* access modifiers changed from: private */
        public String composer;
        /* access modifiers changed from: private */
        public String data;
        /* access modifiers changed from: private */
        public long dateAdded;
        /* access modifiers changed from: private */
        public long dateModified;
        /* access modifiers changed from: private */
        public String description;
        /* access modifiers changed from: private */
        public String displayName;
        /* access modifiers changed from: private */
        public int duration;
        /* access modifiers changed from: private */
        public int format;
        /* access modifiers changed from: private */
        public int handle = -1;
        /* access modifiers changed from: private */
        public int height;
        /* access modifiers changed from: private */
        public int isDrm;
        private Object lockObject = new Object();
        /* access modifiers changed from: private */
        public int mediaType;
        /* access modifiers changed from: private */
        public int mimeType;
        /* access modifiers changed from: private */
        public String name;
        /* access modifiers changed from: private */
        public int parent;
        /* access modifiers changed from: private */
        public long size;
        /* access modifiers changed from: private */
        public int storageId;
        /* access modifiers changed from: private */
        public String title;
        /* access modifiers changed from: private */
        public int track;
        /* access modifiers changed from: private */
        public int[] types = new int[24];
        /* access modifiers changed from: private */
        public int width;
        /* access modifiers changed from: private */
        public int year;

        public int getHandle() {
            int i;
            synchronized (this.lockObject) {
                i = this.handle;
            }
            return i;
        }

        public void setHandle(int handle2) {
            synchronized (this.lockObject) {
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

    private HwMtpDatabaseImpl() {
    }

    public static HwMtpDatabaseManager getDefault() {
        return mHwMtpDatabaseManager;
    }

    public int hwBeginSendObject(String path, Cursor c) {
        ExternalStorageFileImpl tempFile = new ExternalStorageFileImpl(path);
        if (tempFile.exists()) {
            if (DEBUG) {
                Log.w(TAG, path + " exist, delete it");
            }
            if (!tempFile.delete()) {
                Log.i(TAG, "delete fail.");
            }
        }
        c.moveToNext();
        int handleExist = c.getInt(c.getColumnIndex("_id"));
        if (DEBUG) {
            Log.w(TAG, "file already exists in beginSendObject: " + path + ", ID = " + handleExist);
        }
        return handleExist;
    }

    public void hwSaveCurrentObject(MtpStorageManager.MtpObject mtpObject, Cursor c) {
        c.moveToFirst();
        synchronized (this.mLockObject) {
            int i = 0;
            int i2 = 0;
            while (i2 < 24) {
                try {
                    this.mObjectColumnInfo.types[i2] = c.getType(i2);
                    i2++;
                } catch (Throwable th) {
                    throw th;
                }
            }
            int unused = this.mObjectColumnInfo.handle = mtpObject.getId();
            int unused2 = this.mObjectColumnInfo.storageId = mtpObject.getStorageId();
            int unused3 = this.mObjectColumnInfo.format = c.getInt(2);
            if (mtpObject.getParent() != null) {
                MtpObjectColumnInfo mtpObjectColumnInfo = this.mObjectColumnInfo;
                if (!mtpObject.getParent().isRoot()) {
                    i = mtpObject.getParent().getId();
                }
                int unused4 = mtpObjectColumnInfo.parent = i;
            }
            String unused5 = this.mObjectColumnInfo.data = c.getString(4);
            long unused6 = this.mObjectColumnInfo.size = mtpObject.getSize();
            long unused7 = this.mObjectColumnInfo.dateAdded = c.getLong(6);
            long unused8 = this.mObjectColumnInfo.dateModified = mtpObject.getModifiedTime();
            int unused9 = this.mObjectColumnInfo.mimeType = c.getInt(8);
            String unused10 = this.mObjectColumnInfo.title = c.getString(9);
            String unused11 = this.mObjectColumnInfo.displayName = c.getString(10);
            int unused12 = this.mObjectColumnInfo.mediaType = c.getInt(11);
            int unused13 = this.mObjectColumnInfo.isDrm = c.getInt(12);
            int unused14 = this.mObjectColumnInfo.width = c.getInt(13);
            int unused15 = this.mObjectColumnInfo.height = c.getInt(14);
            String unused16 = this.mObjectColumnInfo.description = c.getString(15);
            String unused17 = this.mObjectColumnInfo.artist = c.getString(16);
            String unused18 = this.mObjectColumnInfo.album = c.getString(17);
            String unused19 = this.mObjectColumnInfo.albumArtist = c.getString(18);
            int unused20 = this.mObjectColumnInfo.duration = c.getInt(19);
            int unused21 = this.mObjectColumnInfo.track = c.getInt(20);
            int unused22 = this.mObjectColumnInfo.year = c.getInt(21);
            String unused23 = this.mObjectColumnInfo.composer = c.getString(22);
            String unused24 = this.mObjectColumnInfo.name = mtpObject.getName();
        }
    }

    public void hwClearSavedObject() {
        synchronized (this.mLockObject) {
            int unused = this.mObjectColumnInfo.handle = -1;
        }
    }

    public int hwGetSavedObjectHandle() {
        int access$100;
        synchronized (this.mLockObject) {
            access$100 = this.mObjectColumnInfo.handle;
        }
        return access$100;
    }

    public MtpPropertyListEx getObjectPropertyList(MtpDatabase database, int handle, int format, int property, int groupCode) {
        synchronized (this.mLockObject) {
            if (groupCode == 0) {
                return null;
            }
            if (groupCode == 4 || groupCode == 8) {
                MtpPropertyListEx currentPropertyList = getCurrentPropertyList(handle, getGroupObjectProperties(groupCode));
                return currentPropertyList;
            }
            MtpPropertyListEx mtpPropertyListEx = new MtpPropertyListEx(43013);
            return mtpPropertyListEx;
        }
    }

    public MtpPropertyListEx getObjectPropertyList(int property, int handle) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle == this.mObjectColumnInfo.handle) {
                    MtpPropertyListEx currentPropertyList = getCurrentPropertyList(handle, new int[]{property});
                    return currentPropertyList;
                }
            }
            return null;
        }
    }

    public MtpPropertyListEx getObjectPropertyList(int handle, int format, int[] proplist) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle == this.mObjectColumnInfo.handle) {
                    if (format == 0 || this.mObjectColumnInfo.format == format) {
                        MtpPropertyListEx currentPropertyList = getCurrentPropertyList(handle, proplist);
                        return currentPropertyList;
                    }
                    MtpPropertyListEx mtpPropertyListEx = new MtpPropertyListEx(8201);
                    return mtpPropertyListEx;
                }
            }
            return null;
        }
    }

    public int hwGetObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        synchronized (this.mLockObject) {
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
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle == this.mObjectColumnInfo.handle) {
                    int access$300 = this.mObjectColumnInfo.format;
                    return access$300;
                }
            }
            return -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0030, code lost:
        return false;
     */
    public boolean hwGetObjectReferences(int handle) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle > 0) {
                if (handle == this.mObjectColumnInfo.handle) {
                    if (this.mObjectColumnInfo.types[11] != 0 && 4 != this.mObjectColumnInfo.mediaType) {
                        return true;
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0049, code lost:
        return false;
     */
    public boolean hwGetObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outModified) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle > 0 && this.mObjectColumnInfo.handle == handle) {
                outStorageFormatParent[0] = this.mObjectColumnInfo.storageId;
                outStorageFormatParent[1] = this.mObjectColumnInfo.format;
                outStorageFormatParent[2] = this.mObjectColumnInfo.parent;
                composeObjectInfoParemeters(outName, outModified, this.mObjectColumnInfo.data, this.mObjectColumnInfo.size, this.mObjectColumnInfo.dateModified);
                return true;
            }
        }
    }

    private void composeObjectInfoParemeters(char[] outName, long[] outSizeModified, String data, long size, long dateModified) {
        String path = data;
        if (path != null) {
            int lastSlash = path.lastIndexOf(47);
            int start = lastSlash >= 0 ? lastSlash + 1 : 0;
            int end = path.length();
            if (end - start > 255) {
                end = start + 255;
            }
            path.getChars(start, end, outName, 0);
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
        int i = handle;
        int[] iArr = proplist;
        if (this.mObjectColumnInfo.handle <= 0 || i != this.mObjectColumnInfo.handle) {
            return null;
        }
        MtpPropertyListEx result = new MtpPropertyListEx(8193);
        for (int propertyCode : iArr) {
            switch (propertyCode) {
                case 56321:
                    result.append(i, propertyCode, 6, (long) this.mObjectColumnInfo.storageId);
                    break;
                case 56322:
                    result.append(i, propertyCode, 4, (long) this.mObjectColumnInfo.format);
                    break;
                case 56323:
                case 56978:
                case 56980:
                    result.append(i, propertyCode, 4, 0);
                    break;
                case 56324:
                    if (needConvertHeifToJPG()) {
                        long unused = this.mObjectColumnInfo.size = this.mObjectColumnInfo.size * 3;
                    }
                    result.append(i, propertyCode, 8, this.mObjectColumnInfo.size);
                    break;
                case 56327:
                    if (this.mObjectColumnInfo.types[4] != 0) {
                        if (this.mObjectColumnInfo.data == null) {
                            break;
                        } else {
                            String filePath = this.mObjectColumnInfo.data;
                            if (!needConvertHeifToJPG()) {
                                result.append(i, propertyCode, nameFromPath(this.mObjectColumnInfo.data));
                                break;
                            } else {
                                int suffixIndex = filePath.lastIndexOf(46);
                                result.append(i, propertyCode, nameFromPath(filePath.substring(0, suffixIndex) + SUFFIX_JPG_FILE));
                                break;
                            }
                        }
                    } else {
                        result.setResult(8201);
                        break;
                    }
                case 56329:
                    result.append(i, propertyCode, formatDateTime(this.mObjectColumnInfo.dateModified));
                    break;
                case 56331:
                    result.append(i, propertyCode, 6, (long) this.mObjectColumnInfo.parent);
                    break;
                case 56385:
                    result.append(i, propertyCode, 10, (((long) this.mObjectColumnInfo.storageId) << 32) + ((long) i));
                    break;
                case 56388:
                    if (this.mObjectColumnInfo.types[9] == 0) {
                        if (this.mObjectColumnInfo.types[23] == 0) {
                            if (this.mObjectColumnInfo.types[4] == 0) {
                                result.setResult(8201);
                                break;
                            } else {
                                result.append(i, propertyCode, nameFromPath(this.mObjectColumnInfo.data));
                                break;
                            }
                        } else {
                            result.append(i, propertyCode, this.mObjectColumnInfo.name);
                            break;
                        }
                    } else {
                        result.append(i, propertyCode, this.mObjectColumnInfo.title);
                        break;
                    }
                case 56390:
                    String artist = "";
                    if (this.mObjectColumnInfo.mediaType == 2) {
                        artist = this.mObjectColumnInfo.artist;
                    }
                    result.append(i, propertyCode, artist);
                    break;
                case 56392:
                    result.append(i, propertyCode, this.mObjectColumnInfo.description);
                    break;
                case 56398:
                    result.append(i, propertyCode, formatDateTime(this.mObjectColumnInfo.dateAdded));
                    break;
                case 56457:
                    result.append(i, propertyCode, 6, (long) this.mObjectColumnInfo.duration);
                    break;
                case 56459:
                    result.append(i, propertyCode, 4, (long) (this.mObjectColumnInfo.track % 1000));
                    break;
                case 56460:
                    if (this.mObjectColumnInfo.mediaType != 2) {
                        result.append(i, propertyCode, "");
                        break;
                    } else {
                        result.append(i, propertyCode, this.mObjectColumnInfo.name);
                        break;
                    }
                case 56470:
                    result.append(i, propertyCode, this.mObjectColumnInfo.composer);
                    break;
                case 56473:
                    result.append(i, propertyCode, Integer.toString(this.mObjectColumnInfo.year) + "0101T000000");
                    break;
                case 56474:
                    String album = "";
                    if (this.mObjectColumnInfo.mediaType == 2) {
                        album = this.mObjectColumnInfo.album;
                    }
                    result.append(i, propertyCode, album);
                    break;
                case 56475:
                    result.append(i, propertyCode, this.mObjectColumnInfo.albumArtist);
                    break;
                case 56544:
                    if (this.mObjectColumnInfo.displayName != null) {
                        String displayName = this.mObjectColumnInfo.displayName;
                        if (needConvertHeifToJPG()) {
                            int suffixIndex2 = displayName.lastIndexOf(46);
                            result.append(i, propertyCode, displayName.substring(0, suffixIndex2) + SUFFIX_JPG_FILE);
                            break;
                        }
                    }
                    result.append(i, propertyCode, this.mObjectColumnInfo.displayName);
                    break;
                case 56979:
                case 56985:
                case 56986:
                    result.append(i, propertyCode, 6, 0);
                    break;
                default:
                    result.append(i, propertyCode, 0, 0);
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
        if (end - start > 255) {
            end = start + 255;
        }
        return path.substring(start, end);
    }

    private static String formatDateTime(long seconds) {
        return MtpPropertyGroup.formatDateTime(seconds);
    }

    private boolean needConvertHeifToJPG() {
        boolean isAutomaticMode = MtpDatabase.issIsHeifSettingAutomaticMode();
        boolean isHeifFile = this.mObjectColumnInfo.format == 14354;
        if (!isAutomaticMode || !isHeifFile) {
            return false;
        }
        return true;
    }
}
