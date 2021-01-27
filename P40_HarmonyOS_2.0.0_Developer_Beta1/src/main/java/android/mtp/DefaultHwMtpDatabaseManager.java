package android.mtp;

import android.database.Cursor;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultHwMtpDatabaseManager implements HwMtpDatabaseManager {
    private static DefaultHwMtpDatabaseManager sHwMtpDatabaseManager = new DefaultHwMtpDatabaseManager();

    public static DefaultHwMtpDatabaseManager getDefault() {
        return sHwMtpDatabaseManager;
    }

    @Override // android.mtp.HwMtpDatabaseManager
    public int hwBeginSendObject(String path, Cursor c) {
        return -1;
    }

    @Override // android.mtp.HwMtpDatabaseManager
    public MtpPropertyListEx getObjectPropertyList(int property, int handle) {
        return null;
    }

    @Override // android.mtp.HwMtpDatabaseManager
    public MtpPropertyListEx getObjectPropertyList(int handle, int format, int[] proplist) {
        return null;
    }

    @Override // android.mtp.HwMtpDatabaseManager
    public MtpPropertyListEx getObjectPropertyList(int handle, int format, int property, int groupCode) {
        return null;
    }

    @Override // android.mtp.HwMtpDatabaseManager
    public int hwGetObjectFilePath(int handle, char[] outFilePath, long[] outFileLengthFormat) {
        return -1;
    }

    @Override // android.mtp.HwMtpDatabaseManager
    public int hwGetObjectFormat(int handle) {
        return -1;
    }

    @Override // android.mtp.HwMtpDatabaseManager
    public boolean hwGetObjectReferences(int handle) {
        return false;
    }

    @Override // android.mtp.HwMtpDatabaseManager
    public boolean hwGetObjectInfo(int handle, int[] outStorageFormatParent, char[] outName, long[] outModified) {
        return false;
    }

    @Override // android.mtp.HwMtpDatabaseManager
    public void hwSaveCurrentObject(MtpObjectEx mtpObject, Cursor c) {
    }

    @Override // android.mtp.HwMtpDatabaseManager
    public void hwClearSavedObject() {
    }

    @Override // android.mtp.HwMtpDatabaseManager
    public int hwGetSavedObjectHandle() {
        return -1;
    }
}
