package com.android.internal.telephony.gsm;

import com.android.internal.telephony.uicc.IIccFileHandlerInner;
import com.huawei.internal.telephony.uicc.AdnRecordExt;
import java.util.ArrayList;

public interface IHwUsimPhoneBookManagerEx {
    default void setIccFileHandlerHw(IIccFileHandlerInner fh) {
    }

    default int[] getAdnRecordsSizeFromEFHw() {
        return new int[]{0};
    }

    default int getPbrFileSizeHw() {
        return 0;
    }

    default int getEFidInPBRHw(int recNum, int tag) {
        return 0;
    }

    default boolean updateEmailFile(int adnRecNum, String oldEmail, String newEmail, int efidIndex) {
        return false;
    }

    default boolean updateAnrFile(int adnRecNum, String oldAnr, String newAnr, int efidIndex) {
        return false;
    }

    default int getAnrCount() {
        return 0;
    }

    default int getEmailCount() {
        return 0;
    }

    default int getSpareAnrCount() {
        return 0;
    }

    default int getSpareEmailCount() {
        return 0;
    }

    default int getUsimAdnCount() {
        return 0;
    }

    default int getEmptyEmailNumByPbrindex(int pbrindex) {
        return 0;
    }

    default int getEmptyAnrNumByPbrindex(int pbrindex) {
        return 0;
    }

    default int getEmailFilesCountEachAdn() {
        return 0;
    }

    default int getAnrFilesCountEachAdn() {
        return 0;
    }

    default int getAdnRecordsFreeSize() {
        return 0;
    }

    default int getPbrIndexBy(int adnIndex) {
        return 0;
    }

    default int getPbrIndexByEfid(int efid) {
        return 0;
    }

    default int getInitIndexByPbr(int pbrIndex) {
        return 0;
    }

    default int getExt1Count() {
        return 0;
    }

    default int getSpareExt1Count() {
        return 0;
    }

    default boolean updateExt1File(int adnRecNum, AdnRecordExt oldAdnRecord, AdnRecordExt newAdnRecord, int tagOrEfid) {
        return false;
    }

    default void readExt1FileForSim(int efid) {
    }

    default ArrayList<AdnRecordExt> loadEfFilesFromUsimHw() {
        return new ArrayList<>();
    }

    default void reset() {
    }

    default void invalidateCache() {
    }
}
