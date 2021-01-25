package com.android.internal.telephony.uicc;

import android.os.Message;
import android.util.SparseArray;
import com.android.internal.telephony.gsm.IUsimPhoneBookManagerInner;
import com.huawei.internal.telephony.uicc.AdnRecordExt;
import java.util.ArrayList;

public interface IAdnRecordCacheInner {
    int extensionEfForEf(int i);

    int extensionEfForEfHw(int i);

    int getAdnCountHw();

    ArrayList<AdnRecordExt> getAdnFilesForSim();

    SparseArray<ArrayList<Message>> getAdnLikeWaiters();

    int getAdnRecordsFreeSize();

    ArrayList<AdnRecordExt> getAdnRecordsLoaded(int i);

    int getAnrCountHw();

    int getAnrFilesCountEachAdn();

    int getEFidInPBR(int i, int i2);

    int getEmailCountHw();

    int getEmailFilesCountEachAdn();

    int getEmptyAnrNumByPbrindex(int i);

    int getEmptyEmailNumByPbrindex(int i);

    int getInitIndexByPbr(int i);

    int getPbrFileSize();

    int getPbrIndexByAdnRecIndex(int i);

    int getPbrIndexByEfid(int i);

    ArrayList<AdnRecordExt> getRecordsIfLoadedForEx(int i);

    int[] getRecordsSizeHw();

    int getSpareAnrCountHw();

    int getSpareEmailCountHw();

    int getSpareExt1CountHw();

    Message getUserWriteResponseHw(int i);

    int getUsimAdnCountHw();

    IUsimPhoneBookManagerInner getUsimPhoneBookManager();

    void handleMessageForEx(Message message);

    void putUserWriteResponseHw(int i, Message message);

    void requestLoadAllAdnHw(int i, int i2, Message message);

    void sendErrorResponseHw(Message message, String str);

    void updateAdnBySearch(int i, AdnRecordExt adnRecordExt, AdnRecordExt adnRecordExt2, String str, Message message);

    boolean updateAnrFile(int i, String str, String str2, int i2);

    boolean updateEmailFile(int i, String str, String str2, int i2);

    boolean updateExt1File(int i, AdnRecordExt adnRecordExt, AdnRecordExt adnRecordExt2, int i2);

    void updateUsimAdnByIndexHw(int i, AdnRecordExt adnRecordExt, int i2, int i3, String str, Message message);

    static int getEventIdLoadAllAdnLikeDoneHw() {
        return 1;
    }

    static int getEventIdUpdateAdnDoneHw() {
        return 2;
    }
}
