package com.android.internal.telephony.gsm;

import com.android.internal.telephony.uicc.IIccFileHandlerInner;
import com.huawei.internal.telephony.uicc.AdnRecordExt;
import java.util.ArrayList;

public interface IUsimPhoneBookManagerInner {
    public static final int PBR_RECORD_SIZE_NOT_INIT = -1;

    int getAdnRecordsFreeSize();

    int[] getAdnRecordsSizeFromEF();

    int getAnrCount();

    int getAnrFilesCountEachAdn();

    int getEFidInPBR(int i, int i2);

    int getEFidInPBRForEx(int i, int i2);

    int getEmailCount();

    int getEmailFilesCountEachAdn();

    int getEmptyAnrNumByPbrindex(int i);

    int getEmptyEmailNumByPbrindex(int i);

    int getExt1Count();

    IIccFileHandlerInner getIccFileHandler();

    int getInitIndexByPbr(int i);

    boolean getIsPbrPresent();

    int getPbrFileSize();

    int getPbrIndexBy(int i);

    int getPbrIndexByEfid(int i);

    int getPbrRecordsSize();

    int getSpareAnrCount();

    int getSpareEmailCount();

    int getSpareExt1Count();

    int getUsimAdnCount();

    ArrayList<AdnRecordExt> loadEfFilesFromUsimHw();

    void readExt1FileForSim(int i);

    void readPbrFileAndWaitHw();

    void setIccFileHandler(IIccFileHandlerInner iIccFileHandlerInner);

    boolean updateAnrFile(int i, String str, String str2, int i2);

    boolean updateEmailFile(int i, String str, String str2, int i2);

    boolean updateExt1File(int i, AdnRecordExt adnRecordExt, AdnRecordExt adnRecordExt2, int i2);
}
