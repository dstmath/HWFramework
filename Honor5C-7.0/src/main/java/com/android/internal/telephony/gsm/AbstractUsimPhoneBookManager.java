package com.android.internal.telephony.gsm;

import android.os.Handler;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.IccFileHandler;
import java.util.ArrayList;

public class AbstractUsimPhoneBookManager extends Handler {
    public ArrayList<AdnRecord> getPhonebookRecords() {
        return null;
    }

    public void setIccFileHandler(IccFileHandler fh) {
    }

    public int[] getAdnRecordsSizeFromEF() {
        return new int[]{0};
    }

    public void getAdnRecordsSizeAndWait(int recNum) {
    }

    public int getPbrFileSize() {
        return 0;
    }

    public int getEFidInPBR(int recNum, int tag) {
        return 0;
    }

    public boolean updateEmailFile(int adnRecNum, String oldEmail, String newEmail, int efidIndex) {
        return false;
    }

    public boolean updateAnrFile(int adnRecNum, String oldAnr, String newAnr, int efidIndex) {
        return false;
    }

    public int getAnrCount() {
        return 0;
    }

    public int getEmailCount() {
        return 0;
    }

    public int getSpareAnrCount() {
        return 0;
    }

    public int getSpareEmailCount() {
        return 0;
    }

    public int getUsimAdnCount() {
        return 0;
    }

    public int getEmptyEmailNum_Pbrindex(int pbrindex) {
        return 0;
    }

    public int getEmptyAnrNum_Pbrindex(int pbrindex) {
        return 0;
    }

    public int getEmailFilesCountEachAdn() {
        return 0;
    }

    public int getAnrFilesCountEachAdn() {
        return 0;
    }

    public int getAdnRecordsFreeSize() {
        return 0;
    }

    public int getPbrIndexBy(int adnIndex) {
        return 0;
    }

    public int getPbrIndexByEfid(int efid) {
        return 0;
    }

    public int getInitIndexByPbr(int pbrIndex) {
        return 0;
    }

    public int getExt1Count() {
        return 0;
    }

    public int getSpareExt1Count() {
        return 0;
    }

    public boolean updateExt1File(int adnRecNum, AdnRecord oldAdnRecord, AdnRecord newAdnRecord, int tagOrEfid) {
        return false;
    }

    public void readExt1FileForSim(int efid) {
    }
}
