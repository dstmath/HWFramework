package android.mtp;

import android.database.Cursor;
import android.util.Log;
import java.io.File;

public class HwMtpDatabaseImpl implements HwMtpDatabaseManager {
    private static final String[] OBJECT_ALL_COLUMNS_PROJECTION = new String[]{"_id", "storage_id", "format", "parent", "_data", "_size", "date_added", "date_modified", "mime_type", "title", "_display_name", "media_type", "is_drm", "width", "height", "description", "artist", "album", "album_artist", "duration", "track", "composer", "year", "name"};
    private static final String TAG = "HwMtpDatabaseImpl";
    private static HwMtpDatabaseManager mHwMtpDatabaseManager = new HwMtpDatabaseImpl();
    int[] mGroup4Props = new int[]{56322, 56324};
    int[] mGroup8Props = new int[]{56323, 56327, 56388, 56329, 56398, 56473, 56385, 56459, 56390, 56474, 56460, 56985, 56986, 56979, 56978, 56980, 56321, 56331, 56457, 56544, 56475, 56470, 56392};
    private Object mLockObject = new Object();
    private MtpObjectColumnInfo mObjectColumnInfo = new MtpObjectColumnInfo();

    private static class MtpObjectColumnInfo {
        public String album;
        public String albumArtist;
        public String artist;
        public String composer;
        public String data;
        public long dateAdded;
        public long dateModified;
        public String description;
        public String displayName;
        public int duration;
        public int format;
        public int handle = -1;
        public int height;
        public int isDrm;
        public int mediaType;
        public int mimeType;
        public String name;
        public int parent;
        public long size;
        public int storageId;
        public String title;
        public int track;
        public int[] types = new int[24];
        public int width;
        public int year;

        public String toString() {
            return "MtpObjectColumnInfo[mimeType : " + this.mimeType + ", " + "isDrm : " + this.isDrm + ", " + "width : " + this.width + ", " + "height : " + this.height + "]";
        }
    }

    private HwMtpDatabaseImpl() {
    }

    public static HwMtpDatabaseManager getDefault() {
        return mHwMtpDatabaseManager;
    }

    public int hwBeginSendObject(String path, Cursor c) {
        File tempFile = new File(path);
        if (tempFile.exists()) {
            Log.w(TAG, path + " exist, delete it");
            if (!tempFile.delete()) {
                Log.w(TAG, "delete fail.");
            }
        }
        c.moveToNext();
        int handleExist = c.getInt(c.getColumnIndex("_id"));
        Log.w(TAG, "file already exists in beginSendObject: " + path + ", ID = " + handleExist);
        return handleExist;
    }

    public void hwSaveCurrentObject(Cursor c) {
        c.moveToFirst();
        synchronized (this.mLockObject) {
            for (int i = 0; i < 24; i++) {
                this.mObjectColumnInfo.types[i] = c.getType(i);
            }
            this.mObjectColumnInfo.handle = c.getInt(0);
            this.mObjectColumnInfo.storageId = c.getInt(1);
            this.mObjectColumnInfo.format = c.getInt(2);
            this.mObjectColumnInfo.parent = c.getInt(3);
            this.mObjectColumnInfo.data = c.getString(4);
            this.mObjectColumnInfo.size = c.getLong(5);
            this.mObjectColumnInfo.dateAdded = c.getLong(6);
            this.mObjectColumnInfo.dateModified = c.getLong(7);
            this.mObjectColumnInfo.mimeType = c.getInt(8);
            this.mObjectColumnInfo.title = c.getString(9);
            this.mObjectColumnInfo.displayName = c.getString(10);
            this.mObjectColumnInfo.mediaType = c.getInt(11);
            this.mObjectColumnInfo.isDrm = c.getInt(12);
            this.mObjectColumnInfo.width = c.getInt(13);
            this.mObjectColumnInfo.height = c.getInt(14);
            this.mObjectColumnInfo.description = c.getString(15);
            this.mObjectColumnInfo.artist = c.getString(16);
            this.mObjectColumnInfo.album = c.getString(17);
            this.mObjectColumnInfo.albumArtist = c.getString(18);
            this.mObjectColumnInfo.duration = c.getInt(19);
            this.mObjectColumnInfo.track = c.getInt(20);
            this.mObjectColumnInfo.year = c.getInt(21);
            this.mObjectColumnInfo.composer = c.getString(22);
            this.mObjectColumnInfo.name = c.getString(23);
        }
    }

    public void hwClearSavedObject() {
        synchronized (this.mLockObject) {
            this.mObjectColumnInfo.handle = -1;
        }
    }

    public int hwGetSavedObjectHandle() {
        int i;
        synchronized (this.mLockObject) {
            i = this.mObjectColumnInfo.handle;
        }
        return i;
    }

    public MtpPropertyList getObjectPropertyList(MtpDatabase database, int handle, int format, int property, int groupCode) {
        if (groupCode == 0) {
            return null;
        }
        if (groupCode == 4 || groupCode == 8) {
            return getCurrentPropertyList(handle, getGroupObjectProperties(groupCode));
        }
        return new MtpPropertyList(0, 43013);
    }

    public MtpPropertyList getObjectPropertyList(int property, int handle) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle <= 0 || handle != this.mObjectColumnInfo.handle) {
                return null;
            }
            MtpPropertyList currentPropertyList = getCurrentPropertyList(handle, new int[]{property});
            return currentPropertyList;
        }
    }

    public MtpPropertyList getObjectPropertyList(int handle, int format, int[] proplist) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle <= 0 || handle != this.mObjectColumnInfo.handle) {
                return null;
            }
            MtpPropertyList mtpPropertyList;
            if (format != 0) {
                if (this.mObjectColumnInfo.format != format) {
                    mtpPropertyList = new MtpPropertyList(0, 8201);
                    return mtpPropertyList;
                }
            }
            mtpPropertyList = getCurrentPropertyList(handle, proplist);
            return mtpPropertyList;
        }
    }

    public int hwGetObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle <= 0 || handle != this.mObjectColumnInfo.handle) {
                return -1;
            }
            String path = this.mObjectColumnInfo.data;
            path.getChars(0, path.length(), outFilePath, 0);
            outFilePath[path.length()] = 0;
            outFileLengthFormat[0] = new File(path).length();
            outFileLengthFormat[1] = (long) this.mObjectColumnInfo.format;
            return 0;
        }
    }

    public int hwGetObjectFormat(int handle) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle <= 0 || handle != this.mObjectColumnInfo.handle) {
                return -1;
            }
            int i = this.mObjectColumnInfo.format;
            return i;
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0011, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:19:0x0027, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hwGetObjectReferences(int handle) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle <= 0 || handle != this.mObjectColumnInfo.handle) {
            } else if (this.mObjectColumnInfo.types[11] == 0 || 4 == this.mObjectColumnInfo.mediaType) {
            } else {
                return true;
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0035, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hwGetObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outModified) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle <= 0 || this.mObjectColumnInfo.handle != handle) {
            } else {
                composeObjectInfoParemeters(outStorageFormatParent, outName, outModified, this.mObjectColumnInfo.storageId, this.mObjectColumnInfo.format, this.mObjectColumnInfo.parent, this.mObjectColumnInfo.data, this.mObjectColumnInfo.size, this.mObjectColumnInfo.dateModified);
                return true;
            }
        }
    }

    private void composeObjectInfoParemeters(int[] outStorageFormatParent, char[] outName, long[] outSizeModified, int storageId, int format, int parent, String data, long size, long dateModified) {
        outStorageFormatParent[0] = storageId;
        outStorageFormatParent[1] = format;
        outStorageFormatParent[2] = parent;
        String path = data;
        int lastSlash = data.lastIndexOf(47);
        int start = lastSlash >= 0 ? lastSlash + 1 : 0;
        int end = data.length();
        if (end - start > 255) {
            end = start + 255;
        }
        data.getChars(start, end, outName, 0);
        outName[end - start] = 0;
        outSizeModified[0] = size;
        outSizeModified[1] = dateModified;
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

    private MtpPropertyList getCurrentPropertyList(int handle, int[] proplist) {
        if (this.mObjectColumnInfo.handle <= 0 || handle != this.mObjectColumnInfo.handle) {
            return null;
        }
        MtpPropertyList result = new MtpPropertyList(proplist.length, 8193);
        for (int propertyCode : proplist) {
            switch (propertyCode) {
                case 56321:
                    result.append(handle, propertyCode, 6, (long) this.mObjectColumnInfo.storageId);
                    break;
                case 56322:
                    result.append(handle, propertyCode, 4, (long) this.mObjectColumnInfo.format);
                    break;
                case 56323:
                case 56978:
                case 56980:
                    result.append(handle, propertyCode, 4, 0);
                    break;
                case 56324:
                    result.append(handle, propertyCode, 8, this.mObjectColumnInfo.size);
                    break;
                case 56327:
                    if (this.mObjectColumnInfo.types[4] == 0) {
                        result.setResult(8201);
                        break;
                    }
                    result.append(handle, propertyCode, nameFromPath(this.mObjectColumnInfo.data));
                    break;
                case 56329:
                    result.append(handle, propertyCode, format_date_time(this.mObjectColumnInfo.dateModified));
                    break;
                case 56331:
                    result.append(handle, propertyCode, 6, (long) this.mObjectColumnInfo.parent);
                    break;
                case 56385:
                    result.append(handle, propertyCode, 10, (((long) this.mObjectColumnInfo.storageId) << 32) + ((long) handle));
                    break;
                case 56388:
                    if (this.mObjectColumnInfo.types[9] == 0) {
                        if (this.mObjectColumnInfo.types[23] == 0) {
                            if (this.mObjectColumnInfo.types[4] == 0) {
                                result.setResult(8201);
                                break;
                            }
                            result.append(handle, propertyCode, nameFromPath(this.mObjectColumnInfo.data));
                            break;
                        }
                        result.append(handle, propertyCode, this.mObjectColumnInfo.name);
                        break;
                    }
                    result.append(handle, propertyCode, this.mObjectColumnInfo.title);
                    break;
                case 56390:
                    String artist = "";
                    if (this.mObjectColumnInfo.mediaType == 2) {
                        artist = this.mObjectColumnInfo.artist;
                    }
                    result.append(handle, propertyCode, artist);
                    break;
                case 56392:
                    result.append(handle, propertyCode, this.mObjectColumnInfo.description);
                    break;
                case 56398:
                    result.append(handle, propertyCode, format_date_time(this.mObjectColumnInfo.dateAdded));
                    break;
                case 56457:
                    result.append(handle, propertyCode, 6, (long) this.mObjectColumnInfo.duration);
                    break;
                case 56459:
                    result.append(handle, propertyCode, 4, (long) (this.mObjectColumnInfo.track % 1000));
                    break;
                case 56460:
                    if (this.mObjectColumnInfo.mediaType != 2) {
                        result.append(handle, propertyCode, "");
                        break;
                    }
                    result.append(handle, propertyCode, this.mObjectColumnInfo.name);
                    break;
                case 56470:
                    result.append(handle, propertyCode, this.mObjectColumnInfo.composer);
                    break;
                case 56473:
                    result.append(handle, propertyCode, Integer.toString(this.mObjectColumnInfo.year) + "0101T000000");
                    break;
                case 56474:
                    String album = "";
                    if (this.mObjectColumnInfo.mediaType == 2) {
                        album = this.mObjectColumnInfo.album;
                    }
                    result.append(handle, propertyCode, album);
                    break;
                case 56475:
                    result.append(handle, propertyCode, this.mObjectColumnInfo.albumArtist);
                    break;
                case 56544:
                    result.append(handle, propertyCode, this.mObjectColumnInfo.displayName);
                    break;
                case 56979:
                case 56985:
                case 56986:
                    result.append(handle, propertyCode, 6, 0);
                    break;
                default:
                    result.append(handle, propertyCode, 0, 0);
                    break;
            }
        }
        return result;
    }

    private static String nameFromPath(String path) {
        return MtpPropertyGroup.nameFromPath(path);
    }

    private static String format_date_time(long seconds) {
        return MtpPropertyGroup.format_date_time(seconds);
    }
}
