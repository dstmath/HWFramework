package com.android.internal.telephony;

import android.content.ContentValues;

public abstract class AbstractIccPhoneBookInterfaceManager {
    public boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int efid, ContentValues values, String pin2) {
        return false;
    }

    public boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) {
        return false;
    }

    public int getAdnCountHW() {
        return 0;
    }

    public int getAnrCountHW() {
        return 0;
    }

    public int getEmailCountHW() {
        return 0;
    }

    public int getSpareAnrCountHW() {
        return 0;
    }

    public int getSpareEmailCountHW() {
        return 0;
    }

    public int[] getRecordsSizeHW() {
        return new int[]{0};
    }

    public int updateEfFor3gCardType(int efid) {
        return 0;
    }

    public int getSpareExt1CountHW() {
        return 0;
    }
}
