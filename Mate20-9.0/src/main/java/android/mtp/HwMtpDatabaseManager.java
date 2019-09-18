package android.mtp;

import android.database.Cursor;
import android.mtp.MtpStorageManager;

public interface HwMtpDatabaseManager {
    MtpPropertyListEx getObjectPropertyList(int i, int i2);

    MtpPropertyListEx getObjectPropertyList(int i, int i2, int[] iArr);

    MtpPropertyListEx getObjectPropertyList(MtpDatabase mtpDatabase, int i, int i2, int i3, int i4);

    int hwBeginSendObject(String str, Cursor cursor);

    void hwClearSavedObject();

    int hwGetObjectFilePath(int i, char[] cArr, long[] jArr);

    int hwGetObjectFormat(int i);

    boolean hwGetObjectInfo(int i, int[] iArr, char[] cArr, long[] jArr);

    boolean hwGetObjectReferences(int i);

    int hwGetSavedObjectHandle();

    void hwSaveCurrentObject(MtpStorageManager.MtpObject mtpObject, Cursor cursor);
}
