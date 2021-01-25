package com.android.internal.telephony;

import android.content.ContentValues;
import com.android.internal.telephony.uicc.IAdnRecordCacheInner;
import com.android.internal.telephony.uicc.IccConstants;

public interface IHwPhoneBookInterfaceManagerEx {
    default boolean updateAdnRecordsWithContentValuesInEfBySearchHW(int efid, ContentValues values, String pin2) {
        return false;
    }

    default boolean updateUsimAdnRecordsInEfByIndexHW(int efid, String newTag, String newPhoneNumber, String[] newEmails, String[] newAnrNumbers, int sEf_id, int index, String pin2) {
        return false;
    }

    default int getAdnCountHw() {
        return 0;
    }

    default int getAnrCountHw() {
        return 0;
    }

    default int getEmailCountHw() {
        return 0;
    }

    default int getSpareAnrCountHw() {
        return 0;
    }

    default int getSpareEmailCountHw() {
        return 0;
    }

    default int[] getRecordsSizeHw() {
        return new int[]{0};
    }

    default int updateEfFor3gCardType(int efid) {
        return IccConstants.EF_PBR;
    }

    default int getSpareExt1CountHw() {
        return 0;
    }

    default void updateAdnRecordCache(IAdnRecordCacheInner iAdnRecordCacheInner) {
    }

    default int[] getAdnRecordsSizeHw(int efid) {
        return null;
    }
}
