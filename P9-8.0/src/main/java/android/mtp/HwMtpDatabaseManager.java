package android.mtp;

import android.database.Cursor;

public interface HwMtpDatabaseManager {
    MtpPropertyList getObjectPropertyList(int i, int i2);

    MtpPropertyList getObjectPropertyList(int i, int i2, int[] iArr);

    MtpPropertyList getObjectPropertyList(MtpDatabase mtpDatabase, int i, int i2, int i3, int i4);

    int hwBeginSendObject(String str, Cursor cursor);

    void hwClearSavedObject();

    int hwGetObjectFilePath(int i, char[] cArr, long[] jArr);

    int hwGetObjectFormat(int i);

    boolean hwGetObjectInfo(int i, int[] iArr, char[] cArr, long[] jArr);

    boolean hwGetObjectReferences(int i);

    int hwGetSavedObjectHandle();

    void hwSaveCurrentObject(Cursor cursor);
}
