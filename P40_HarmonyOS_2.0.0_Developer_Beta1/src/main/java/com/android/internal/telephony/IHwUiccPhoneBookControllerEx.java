package com.android.internal.telephony;

import android.content.ContentValues;

public interface IHwUiccPhoneBookControllerEx {
    default boolean updateAdnRecordsWithContentValuesInEfBySearchUsingSubIdHW(int subId, int efid, ContentValues values, String pin2) {
        return false;
    }

    default boolean updateUsimAdnRecordsInEfByIndexUsingSubIdHW(int subId, int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) {
        return false;
    }

    default int getAdnCountUsingSubIdHW(int subId) {
        return 0;
    }

    default int getAnrCountUsingSubIdHW(int subId) {
        return 0;
    }

    default int getEmailCountUsingSubIdHW(int subId) {
        return 0;
    }

    default int getSpareAnrCountUsingSubIdHW(int subId) {
        return 0;
    }

    default int getSpareEmailCountUsingSubIdHW(int subId) {
        return 0;
    }

    default int[] getRecordsSizeUsingSubIdHW(int subId) {
        return new int[0];
    }

    default int getSpareExt1CountUsingSubIdHW(int subId) {
        return 0;
    }
}
