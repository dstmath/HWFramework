package com.android.internal.telephony.uicc;

import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class HwAdnRecordCache extends AdnRecordCache {
    private static String TAG = "HwAdnRecordCache";
    private static final int USIM_ADN_MAX_LENGTH_WITHOUT_EXT = 20;
    private static final int USIM_EFANR_TAG = 196;
    private static final int USIM_EFEMAIL_TAG = 202;
    private static final int USIM_EFEXT1_TAG = 194;
    private static AdnRecordCacheUtils adnRecordCacheUtils = EasyInvokeFactory.getInvokeUtils(AdnRecordCacheUtils.class);
    public static final AtomicReference<Integer> s_efid = new AtomicReference<>();
    public static final AtomicReference<Integer> s_index = new AtomicReference<>();
    private int mAdncountofIcc = 0;

    public HwAdnRecordCache(IccFileHandler fh) {
        super(fh);
    }

    /* access modifiers changed from: protected */
    public void updateAdnRecordId(AdnRecord adn, int efid, int index) {
        if (!(adn == null || efid == 20272)) {
            adn.mEfid = efid;
            adn.mRecordNumber = index;
        }
        if (efid != 20272) {
            s_efid.set(Integer.valueOf(efid));
            s_index.set(Integer.valueOf(index));
        } else if (adn != null) {
            s_efid.set(Integer.valueOf(adn.mEfid));
            s_index.set(Integer.valueOf(adn.mRecordNumber));
        }
    }

    public int getUsimExtensionEfForAdnEf(int AdnEfid) {
        logd("getUsimExtensionEfForAdnEf AdnEfid = " + AdnEfid);
        IccFileHandler fh = adnRecordCacheUtils.getFh(this);
        UiccProfile profile = null;
        UiccCardApplication app = fh == null ? null : fh.mParentApp;
        if (app != null) {
            profile = app.getUiccProfile();
        }
        if (profile != null && profile.isApplicationOnIcc(IccCardApplicationStatus.AppType.APPTYPE_USIM)) {
            logd("getUsimExtensionEfForAdnEf sim application is on APPTYPE_USIM");
            int pbrSize = adnRecordCacheUtils.getUsimPhoneBookManager(this).getPbrFileSize();
            logd("getUsimExtensionEfForAdnEf pbrSize = " + pbrSize);
            if (pbrSize <= 0) {
                return -1;
            }
            for (int loop = 0; loop < pbrSize; loop++) {
                int efid = adnRecordCacheUtils.getUsimPhoneBookManager(this).getEFidInPBR(loop, 192);
                logd("getUsimExtensionEfForAdnEf loop = " + loop + " ; efid = " + efid);
                if (AdnEfid == efid) {
                    int extensionEF = adnRecordCacheUtils.getUsimPhoneBookManager(this).getEFidInPBR(loop, USIM_EFEXT1_TAG);
                    logd("getUsimExtensionEfForAdnEf extensionEF = " + extensionEF);
                    if (extensionEF < 0) {
                        return -1;
                    }
                    return extensionEF;
                }
            }
        }
        logd("getUsimExtensionEfForAdnEf no match pbr return -1");
        return -1;
    }

    public int extensionEfForEf(int efid) {
        logd("extensionEfForEf efid = " + efid);
        if (efid == 20272) {
            return 0;
        }
        if (efid == 28480) {
            return 28490;
        }
        if (efid == 28489) {
            return 28492;
        }
        if (efid == 28615) {
            return 28616;
        }
        switch (efid) {
            case 28474:
                return 28490;
            case 28475:
                return 28491;
            default:
                return getUsimExtensionEfForAdnEf(efid);
        }
    }

    public void updateAdnBySearch(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        ArrayList<AdnRecord> oldAdnList;
        int index;
        Iterator<AdnRecord> it;
        int index2;
        int prePbrIndex;
        int i = efid;
        AdnRecord adnRecord = oldAdn;
        AdnRecord adnRecord2 = newAdn;
        Message message = response;
        if (!IccRecords.getEmailAnrSupport()) {
            HwAdnRecordCache.super.updateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
        } else {
            int extensionEF = extensionEfForEf(efid);
            if (extensionEF < 0) {
                adnRecordCacheUtils.sendErrorResponse(this, message, "EF is not known ADN-like EF:" + i);
                return;
            }
            int i2 = 20272;
            if (i == 20272) {
                try {
                    oldAdnList = getUsimPhoneBookManager().loadEfFilesFromUsim();
                } catch (NullPointerException e) {
                    oldAdnList = null;
                }
            } else {
                oldAdnList = getRecordsIfLoaded(efid);
            }
            if (oldAdnList == null) {
                adnRecordCacheUtils.sendErrorResponse(this, message, "Adn list not exist for EF:" + i);
                return;
            }
            int index3 = -1;
            int prePbrIndex2 = -2;
            int anrNum = 0;
            int emailNum = 0;
            Iterator<AdnRecord> it2 = oldAdnList.iterator();
            int count = 1;
            while (true) {
                if (it2.hasNext() == 0) {
                    int i3 = anrNum;
                    int i4 = emailNum;
                    index = index3;
                    int i5 = prePbrIndex2;
                    break;
                }
                AdnRecord nextAdnRecord = it2.next();
                boolean isEmailOrAnrIsFull = false;
                if (i == i2) {
                    index2 = index3;
                    int index4 = getUsimPhoneBookManager().getPbrIndexBy(count - 1);
                    if (index4 != prePbrIndex2) {
                        anrNum = getUsimPhoneBookManager().getEmptyAnrNum_Pbrindex(index4);
                        emailNum = getUsimPhoneBookManager().getEmptyEmailNum_Pbrindex(index4);
                        String str = TAG;
                        prePbrIndex = index4;
                        StringBuilder sb = new StringBuilder();
                        it = it2;
                        sb.append("updateAdnBySearch, pbrIndex: ");
                        sb.append(index4);
                        sb.append(" anrNum:");
                        sb.append(anrNum);
                        sb.append(" emailNum:");
                        sb.append(emailNum);
                        Rlog.d(str, sb.toString());
                    } else {
                        it = it2;
                        prePbrIndex = prePbrIndex2;
                    }
                    if ((anrNum == 0 && oldAdn.getAdditionalNumbers() == null && newAdn.getAdditionalNumbers() != null) || (emailNum == 0 && oldAdn.getEmails() == null && newAdn.getEmails() != null)) {
                        isEmailOrAnrIsFull = true;
                    }
                    prePbrIndex2 = prePbrIndex;
                } else {
                    index2 = index3;
                    it = it2;
                }
                if (!isEmailOrAnrIsFull && adnRecord.isEqual(nextAdnRecord)) {
                    index = count;
                    int i6 = prePbrIndex2;
                    int i7 = anrNum;
                    int i8 = emailNum;
                    break;
                }
                count++;
                index3 = index2;
                it2 = it;
                i2 = 20272;
            }
            Rlog.d(TAG, "updateAdnBySearch  index :" + index);
            if (index == -1) {
                adnRecordCacheUtils.sendErrorResponse(this, message, "Adn record don't exist for " + adnRecord);
                return;
            }
            if (i == 20272) {
                AdnRecord foundAdn = oldAdnList.get(index - 1);
                adnRecord2.mEfid = foundAdn.mEfid;
                adnRecord2.mExtRecord = foundAdn.mExtRecord;
                adnRecord2.mRecordNumber = foundAdn.mRecordNumber;
                adnRecord.setAdditionalNumbers(foundAdn.getAdditionalNumbers());
                adnRecord.setEmails(foundAdn.getEmails());
                adnRecord2.updateAnrEmailArray(adnRecord, getUsimPhoneBookManager().getEmailFilesCountEachAdn(), getUsimPhoneBookManager().getAnrFilesCountEachAdn());
            } else if (i == 28474) {
                AdnRecord foundAdn2 = oldAdnList.get(index - 1);
                adnRecord2.mEfid = foundAdn2.mEfid;
                adnRecord2.mExtRecord = foundAdn2.mExtRecord;
                adnRecord2.mRecordNumber = foundAdn2.mRecordNumber;
            }
            if (((Message) this.mUserWriteResponse.get(i)) != null) {
                adnRecordCacheUtils.sendErrorResponse(this, message, "Have pending update for EF:" + i);
            } else if (i == 20272) {
                updateEmailAndAnr(i, adnRecord, adnRecord2, index, pin2, message);
            } else if (!getUsimPhoneBookManager().updateExt1File(index, adnRecord, adnRecord2, extensionEF)) {
                adnRecordCacheUtils.sendErrorResponse(this, message, "update ext1 failed");
            } else {
                this.mUserWriteResponse.put(i, message);
                new AdnRecordLoader(adnRecordCacheUtils.getFh(this)).updateEF(adnRecord2, i, extensionEF, index, pin2, obtainMessage(2, i, index, adnRecord2));
            }
        }
    }

    private void updateEmailAndAnr(int efid, AdnRecord oldAdn, AdnRecord newAdn, int index, String pin2, Message response) {
        int extensionEF = extensionEfForEf(newAdn.mEfid);
        if (!updateUsimRecord(oldAdn, newAdn, index, 202)) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "update email failed");
        } else if (!updateUsimRecord(oldAdn, newAdn, index, USIM_EFANR_TAG)) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "update anr failed");
        } else if (getUsimPhoneBookManager().updateExt1File(index, oldAdn, newAdn, USIM_EFEXT1_TAG)) {
            this.mUserWriteResponse.put(efid, response);
            new AdnRecordLoader(adnRecordCacheUtils.getFh(this)).updateEF(newAdn, newAdn.mEfid, extensionEF, newAdn.mRecordNumber, pin2, obtainMessage(2, efid, index, newAdn));
        } else {
            adnRecordCacheUtils.sendErrorResponse(this, response, "update ext1 failed");
        }
    }

    private boolean updateAnrEmailFile(String oldRecord, String newRecord, int index, int tag, int efidIndex) {
        if (tag == USIM_EFANR_TAG) {
            return getUsimPhoneBookManager().updateAnrFile(index, oldRecord, newRecord, efidIndex);
        }
        if (tag != 202) {
            return false;
        }
        try {
            return getUsimPhoneBookManager().updateEmailFile(index, oldRecord, newRecord, efidIndex);
        } catch (RuntimeException e) {
            Rlog.e(TAG, "update usim record failed", e);
            return false;
        }
    }

    private boolean updateUsimRecord(AdnRecord oldAdn, AdnRecord newAdn, int index, int tag) {
        String[] newRecords;
        String[] oldRecords;
        boolean success;
        int maxLen;
        int i;
        int i2 = tag;
        int i3 = 0;
        if (i2 == USIM_EFANR_TAG) {
            oldRecords = oldAdn.getAdditionalNumbers();
            newRecords = newAdn.getAdditionalNumbers();
        } else if (i2 != 202) {
            return false;
        } else {
            oldRecords = oldAdn.getEmails();
            newRecords = newAdn.getEmails();
        }
        String[] oldRecords2 = oldRecords;
        String[] newRecords2 = newRecords;
        boolean isAllEmpty = oldRecords2 == null && newRecords2 == null;
        boolean isOldEmpty = oldRecords2 == null && newRecords2 != null;
        boolean isNewEmpty = oldRecords2 != null && newRecords2 == null;
        if (isAllEmpty) {
            Rlog.e(TAG, "Both old and new EMAIL/ANR are null");
            return true;
        }
        if (isOldEmpty) {
            success = true;
            while (i3 < newRecords2.length) {
                if (!TextUtils.isEmpty(newRecords2[i3])) {
                    success = updateAnrEmailFile(null, newRecords2[i3], index, i2, i3) & success;
                }
                i3++;
            }
        } else if (isNewEmpty) {
            success = true;
            while (i3 < oldRecords2.length) {
                if (!TextUtils.isEmpty(oldRecords2[i3])) {
                    success = updateAnrEmailFile(oldRecords2[i3], null, index, i2, i3) & success;
                }
                i3++;
            }
        } else {
            int maxLen2 = oldRecords2.length > newRecords2.length ? oldRecords2.length : newRecords2.length;
            boolean success2 = true;
            int i4 = 0;
            while (true) {
                int i5 = i4;
                if (i5 >= maxLen2) {
                    break;
                }
                String str = null;
                String oldRecord = i5 >= oldRecords2.length ? null : oldRecords2[i5];
                if (i5 < newRecords2.length) {
                    str = newRecords2[i5];
                }
                String newRecord = str;
                if ((TextUtils.isEmpty(oldRecord) && TextUtils.isEmpty(newRecord)) || !(oldRecord == null || newRecord == null || !oldRecord.equals(newRecord))) {
                    i = i5;
                    maxLen = maxLen2;
                } else {
                    i = i5;
                    maxLen = maxLen2;
                    success2 &= updateAnrEmailFile(oldRecord, newRecord, index, i2, i);
                }
                i4 = i + 1;
                maxLen2 = maxLen;
            }
            success = success2;
        }
        return success;
    }

    public void updateUsimAdnByIndexHW(int efid, AdnRecord newAdn, int sEf_id, int recordIndex, String pin2, Message response) {
        ArrayList<AdnRecord> oldAdnList;
        int i = efid;
        AdnRecord adnRecord = newAdn;
        int i2 = sEf_id;
        Message message = response;
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            AdnRecordCacheUtils adnRecordCacheUtils2 = adnRecordCacheUtils;
            adnRecordCacheUtils2.sendErrorResponse(this, message, "EF is not known ADN-like EF:" + i);
            return;
        }
        if (i == 20272) {
            try {
                oldAdnList = getUsimPhoneBookManager().loadEfFilesFromUsim();
            } catch (NullPointerException e) {
                oldAdnList = null;
            }
        } else {
            oldAdnList = getRecordsIfLoaded(efid);
        }
        if (oldAdnList == null) {
            AdnRecordCacheUtils adnRecordCacheUtils3 = adnRecordCacheUtils;
            adnRecordCacheUtils3.sendErrorResponse(this, message, "Adn list not exist for EF:" + i);
            return;
        }
        int index = recordIndex;
        if (i == 20272) {
            int pbrIndex = getUsimPhoneBookManager().getPbrIndexByEfid(i2);
            index += getUsimPhoneBookManager().getInitIndexByPbr(pbrIndex);
            String str = TAG;
            Rlog.d(str, "sEf_id " + i2 + " index " + index + " pbrIndex " + pbrIndex);
            AdnRecord foundAdn = oldAdnList.get(index + -1);
            adnRecord.mEfid = foundAdn.mEfid;
            adnRecord.mExtRecord = foundAdn.mExtRecord;
            adnRecord.mRecordNumber = foundAdn.mRecordNumber;
        } else if (i == 28474) {
            AdnRecord foundAdn2 = oldAdnList.get(index - 1);
            adnRecord.mEfid = foundAdn2.mEfid;
            adnRecord.mExtRecord = foundAdn2.mExtRecord;
            adnRecord.mRecordNumber = foundAdn2.mRecordNumber;
        }
        int index2 = index;
        if (((Message) this.mUserWriteResponse.get(i)) != null) {
            AdnRecordCacheUtils adnRecordCacheUtils4 = adnRecordCacheUtils;
            adnRecordCacheUtils4.sendErrorResponse(this, message, "Have pending update for EF:" + i);
            return;
        }
        if (i == 20272) {
            updateEmailAndAnr(i, oldAdnList.get(index2 - 1), adnRecord, index2, pin2, message);
        } else if (!getUsimPhoneBookManager().updateExt1File(index2, oldAdnList.get(index2 - 1), adnRecord, extensionEF)) {
            adnRecordCacheUtils.sendErrorResponse(this, message, "update ext1 failed");
        } else {
            this.mUserWriteResponse.put(i, message);
            new AdnRecordLoader(adnRecordCacheUtils.getFh(this)).updateEF(adnRecord, i, extensionEF, index2, pin2, obtainMessage(2, i, index2, adnRecord));
        }
    }

    public int getAnrCountHW() {
        return getUsimPhoneBookManager().getAnrCount();
    }

    public int getEmailCountHW() {
        return getUsimPhoneBookManager().getEmailCount();
    }

    public int getSpareAnrCountHW() {
        return getUsimPhoneBookManager().getSpareAnrCount();
    }

    public int getSpareEmailCountHW() {
        return getUsimPhoneBookManager().getSpareEmailCount();
    }

    public int getAdnCountHW() {
        return this.mAdncountofIcc;
    }

    public void setAdnCountHW(int count) {
        this.mAdncountofIcc = count;
    }

    public int getUsimAdnCountHW() {
        return getUsimPhoneBookManager().getUsimAdnCount();
    }

    public UsimPhoneBookManager getUsimPhoneBookManager() {
        return adnRecordCacheUtils.getUsimPhoneBookManager(this);
    }

    public int getRecordsSizeByIdInAdnlist(int efid) {
        ArrayList<AdnRecord> adnList = getRecordsIfLoaded(efid);
        String str = TAG;
        Rlog.i(str, "getRecordsSizeByIdInAdnlist efid " + efid);
        if (adnList == null) {
            return 0;
        }
        return adnList.size();
    }

    public int getRecordsFreeSizeByIdInAdnlist(int efid) {
        int RecordsFreeSize = 0;
        ArrayList<AdnRecord> adnList = getRecordsIfLoaded(efid);
        String str = TAG;
        Rlog.i(str, "getRecordsFreeSizeByIdInAdnlist efid " + efid);
        if (adnList == null) {
            return 0;
        }
        int adnListSize = adnList.size();
        for (int i = 0; i < adnListSize; i++) {
            if (adnList.get(i).isEmpty()) {
                RecordsFreeSize++;
            }
        }
        return RecordsFreeSize;
    }

    public int[] getRecordsSizeHW() {
        int[] recordSize = new int[9];
        Rlog.i(TAG, "getRecordsSize(): enter.");
        for (int i = 0; i < recordSize.length; i++) {
            recordSize[i] = -1;
        }
        recordSize[0] = 0;
        boolean isCsim3Gphonebook = false;
        if ((adnRecordCacheUtils.getFh(this) instanceof CsimFileHandler) && adnRecordCacheUtils.getFh(this).getIccRecords() != null) {
            isCsim3Gphonebook = adnRecordCacheUtils.getFh(this).getIccRecords().has3Gphonebook();
        }
        boolean isUsim3Gphonebook = false;
        if ((adnRecordCacheUtils.getFh(this) instanceof UsimFileHandler) && adnRecordCacheUtils.getFh(this).getIccRecords() != null) {
            isUsim3Gphonebook = adnRecordCacheUtils.getFh(this).getIccRecords().has3Gphonebook();
        }
        if (isUsim3Gphonebook || isCsim3Gphonebook || (adnRecordCacheUtils.getFh(this) instanceof IsimFileHandler)) {
            Rlog.i(TAG, "getRecordsSize(): usim card branch.");
            if (getUsimPhoneBookManager() == null) {
                return recordSize;
            }
            if (getUsimAdnCountHW() > 0) {
                recordSize[2] = getUsimAdnCountHW();
                recordSize[1] = getUsimPhoneBookManager().getAdnRecordsFreeSize();
            }
            if (getEmailCountHW() > 0) {
                recordSize[5] = getEmailCountHW();
                if (!(recordSize[5] == -1 || recordSize[5] == 0)) {
                    recordSize[4] = getUsimPhoneBookManager().getSpareEmailCount();
                    recordSize[3] = 1;
                }
            }
            if (getAnrCountHW() > 0) {
                recordSize[8] = getAnrCountHW();
                if (!(recordSize[8] == -1 || recordSize[8] == 0)) {
                    recordSize[7] = getUsimPhoneBookManager().getSpareAnrCount();
                    recordSize[6] = 2;
                }
            }
        } else {
            Rlog.i(TAG, "getRecordsSize(): sim card branch.");
            recordSize[1] = getRecordsFreeSizeByIdInAdnlist(28474);
            recordSize[2] = getRecordsSizeByIdInAdnlist(28474);
        }
        for (int i2 = 0; i2 < recordSize.length; i2++) {
            Rlog.i(TAG, "getRecordsSize(): recordSize[" + i2 + "] = " + recordSize[i2]);
        }
        return recordSize;
    }

    public void updateUsimPhoneBookRecord(AdnRecord adn, int efid, int index) {
        if (20272 == efid) {
            ArrayList<AdnRecord> tempAdnList = getUsimPhoneBookManager().loadEfFilesFromUsim();
            if (tempAdnList != null) {
                tempAdnList.set(index - 1, adn);
            } else {
                Rlog.e(TAG, "loadEfFilesFromUsim result null.");
            }
        }
    }

    public int getExt1CountHW() {
        return getUsimPhoneBookManager().getExt1Count();
    }

    public ArrayList<AdnRecord> getAdnFilesForSim() {
        return (ArrayList) this.mAdnLikeFiles.get(28474);
    }

    public int getSpareExt1CountHW() {
        if (getExt1CountHW() > 0) {
            return getUsimPhoneBookManager().getSpareExt1Count();
        }
        return -1;
    }

    public void reset() {
        HwAdnRecordCache.super.reset();
        this.mAdncountofIcc = 0;
    }

    private void logd(String msg) {
        Rlog.d(TAG, msg);
    }
}
