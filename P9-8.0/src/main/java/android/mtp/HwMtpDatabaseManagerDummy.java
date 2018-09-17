package android.mtp;

import android.database.Cursor;

public class HwMtpDatabaseManagerDummy implements HwMtpDatabaseManager {
    private static HwMtpDatabaseManager mHwMtpDatabaseManager = new HwMtpDatabaseManagerDummy();

    private HwMtpDatabaseManagerDummy() {
    }

    public static HwMtpDatabaseManager getDefault() {
        return mHwMtpDatabaseManager;
    }

    public int hwBeginSendObject(String path, Cursor c) {
        return -1;
    }

    public MtpPropertyList getObjectPropertyList(int property, int handle) {
        return null;
    }

    public MtpPropertyList getObjectPropertyList(int handle, int format, int[] proplist) {
        return null;
    }

    public MtpPropertyList getObjectPropertyList(MtpDatabase database, int handle, int format, int property, int groupCode) {
        return null;
    }

    public int hwGetObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        return -1;
    }

    public int hwGetObjectFormat(int handle) {
        return -1;
    }

    public boolean hwGetObjectReferences(int handle) {
        return false;
    }

    public boolean hwGetObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outModified) {
        return false;
    }

    public void hwSaveCurrentObject(Cursor c) {
    }

    public void hwClearSavedObject() {
    }

    public int hwGetSavedObjectHandle() {
        return -1;
    }
}
