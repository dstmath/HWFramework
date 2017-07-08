package android.mtp;

import android.database.Cursor;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HwMtpDatabaseImpl implements HwMtpDatabaseManager {
    private static final String[] OBJECT_ALL_COLUMNS_PROJECTION = null;
    private static final String TAG = "HwMtpDatabaseImpl";
    private static HwMtpDatabaseManager mHwMtpDatabaseManager;
    Integer[] mGroup4Props;
    Integer[] mGroup8Props;
    private Object mLockObject;
    private MtpObjectColumnInfo mObjectColumnInfo;
    private HashMap<String, int[]> mPLCache;

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
        public int handle;
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
        public int[] types;
        public int width;
        public int year;

        public MtpObjectColumnInfo() {
            this.types = new int[24];
            this.handle = -1;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.mtp.HwMtpDatabaseImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.mtp.HwMtpDatabaseImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.mtp.HwMtpDatabaseImpl.<clinit>():void");
    }

    private HwMtpDatabaseImpl() {
        this.mPLCache = new HashMap();
        this.mLockObject = new Object();
        this.mObjectColumnInfo = new MtpObjectColumnInfo();
        this.mGroup4Props = new Integer[]{Integer.valueOf(56322), Integer.valueOf(56324)};
        this.mGroup8Props = new Integer[]{Integer.valueOf(56323), Integer.valueOf(56327), Integer.valueOf(56388), Integer.valueOf(56329), Integer.valueOf(56398), Integer.valueOf(56473), Integer.valueOf(56385), Integer.valueOf(56459), Integer.valueOf(56390), Integer.valueOf(56474), Integer.valueOf(56460), Integer.valueOf(56985), Integer.valueOf(56986), Integer.valueOf(56979), Integer.valueOf(56978), Integer.valueOf(56980), Integer.valueOf(56321), Integer.valueOf(56331), Integer.valueOf(56457), Integer.valueOf(56544), Integer.valueOf(56475), Integer.valueOf(56470), Integer.valueOf(56392)};
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
        if (groupCode != 4 && groupCode != 8) {
            return new MtpPropertyList(0, 43013);
        }
        String key = Integer.toString(format) + "/" + Integer.toString(groupCode);
        int[] proplist = (int[]) this.mPLCache.get(key);
        if (proplist == null) {
            int i;
            int[] fpl = database.getSupportedObjectProperties(format);
            List<Integer> s1 = new ArrayList(fpl.length);
            List<Integer> s2 = Arrays.asList(getGroupObjectProperties(groupCode));
            for (int valueOf : fpl) {
                s1.add(Integer.valueOf(valueOf));
            }
            s1.retainAll(s2);
            proplist = new int[s1.size()];
            for (i = 0; i < s1.size(); i++) {
                proplist[i] = ((Integer) s1.get(i)).intValue();
            }
            this.mPLCache.put(key, proplist);
        }
        return getCurrentPropertyList(handle, proplist);
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
            outFilePath[path.length()] = '\u0000';
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

    public boolean hwGetObjectReferences(int handle) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle <= 0 || handle != this.mObjectColumnInfo.handle) {
                return false;
            } else if (this.mObjectColumnInfo.types[11] == 0 || 4 == this.mObjectColumnInfo.mediaType) {
                return false;
            } else {
                return true;
            }
        }
    }

    public boolean hwGetObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outModified) {
        synchronized (this.mLockObject) {
            if (this.mObjectColumnInfo.handle <= 0 || this.mObjectColumnInfo.handle != handle) {
                return false;
            }
            composeObjectInfoParemeters(outStorageFormatParent, outName, outModified, this.mObjectColumnInfo.storageId, this.mObjectColumnInfo.format, this.mObjectColumnInfo.parent, this.mObjectColumnInfo.data, this.mObjectColumnInfo.size, this.mObjectColumnInfo.dateModified);
            return true;
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
        if (end - start > PduHeaders.STORE_STATUS_ERROR_END) {
            end = start + PduHeaders.STORE_STATUS_ERROR_END;
        }
        data.getChars(start, end, outName, 0);
        outName[end - start] = '\u0000';
        outSizeModified[0] = size;
        outSizeModified[1] = dateModified;
    }

    private Integer[] getGroupObjectProperties(int groupCode) {
        if (groupCode == 4) {
            return this.mGroup4Props;
        }
        if (groupCode == 8) {
            return this.mGroup8Props;
        }
        return new Integer[0];
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
