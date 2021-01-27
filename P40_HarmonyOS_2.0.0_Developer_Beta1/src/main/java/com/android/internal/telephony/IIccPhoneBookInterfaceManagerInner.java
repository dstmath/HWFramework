package com.android.internal.telephony;

import android.content.ContentValues;
import android.os.Message;

public interface IIccPhoneBookInterfaceManagerInner {
    void checkThreadHw();

    int getAdnCountHw();

    void getAdnRecordsInEfForEx(int i);

    int getAnrCountHw();

    int getEmailCountHw();

    int[] getRecordsSizeHw();

    Object getRequest();

    Object getRequestResult(Object obj);

    int getSpareAnrCountHw();

    int getSpareEmailCountHw();

    int getSpareExt1CountHw();

    void handleMessageForEx(Message message);

    boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int i, ContentValues contentValues, String str);

    int updateEfForIccTypeHw(int i);

    boolean updateUsimAdnRecordsInEfByIndexHW(int i, String str, String str2, String[] strArr, String[] strArr2, int i2, int i3, String str3);

    void waitForResultHw(Object obj);

    static int getEventIdUpdateDoneHw() {
        return 3;
    }
}
