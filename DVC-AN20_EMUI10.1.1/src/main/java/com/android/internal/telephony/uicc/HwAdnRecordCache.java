package com.android.internal.telephony.uicc;

import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.huawei.internal.telephony.uicc.IccRecordsEx;
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
                    int extensionEF = adnRecordCacheUtils.getUsimPhoneBookManager(this).getEFidInPBR(loop, (int) USIM_EFEXT1_TAG);
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
        if (efid == 28474) {
            return 28490;
        }
        if (efid != 28475) {
            return getUsimExtensionEfForAdnEf(efid);
        }
        return 28491;
    }

    public void updateAdnBySearch(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        ArrayList<AdnRecord> oldAdnList;
        int index;
        int index2;
        int prePbrIndex;
        if (!IccRecordsEx.getEmailAnrSupport()) {
            HwAdnRecordCache.super.updateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
            return;
        }
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "EF is not known ADN-like EF:" + efid);
            return;
        }
        int i = 20272;
        if (efid == 20272) {
            try {
                oldAdnList = getUsimPhoneBookManager().loadEfFilesFromUsim();
            } catch (NullPointerException e) {
                oldAdnList = null;
            }
        } else {
            oldAdnList = getRecordsIfLoaded(efid);
        }
        if (oldAdnList == null) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "Adn list not exist for EF:" + efid);
            return;
        }
        int index3 = -1;
        int prePbrIndex2 = -2;
        int anrNum = 0;
        int emailNum = 0;
        Iterator<AdnRecord> it = oldAdnList.iterator();
        int count = 1;
        while (true) {
            if (!it.hasNext()) {
                index = index3;
                break;
            }
            AdnRecord nextAdnRecord = it.next();
            boolean isEmailOrAnrIsFull = false;
            if (efid == i) {
                index2 = index3;
                int pbrIndex = getUsimPhoneBookManager().getPbrIndexBy(count - 1);
                if (pbrIndex != prePbrIndex2) {
                    anrNum = getUsimPhoneBookManager().getEmptyAnrNum_Pbrindex(pbrIndex);
                    emailNum = getUsimPhoneBookManager().getEmptyEmailNum_Pbrindex(pbrIndex);
                    StringBuilder sb = new StringBuilder();
                    prePbrIndex = pbrIndex;
                    sb.append("updateAdnBySearch, pbrIndex: ");
                    sb.append(pbrIndex);
                    sb.append(" anrNum:");
                    sb.append(anrNum);
                    sb.append(" emailNum:");
                    sb.append(emailNum);
                    logd(sb.toString());
                } else {
                    prePbrIndex = prePbrIndex2;
                }
                if ((anrNum == 0 && oldAdn.getAdditionalNumbers() == null && newAdn.getAdditionalNumbers() != null) || (emailNum == 0 && oldAdn.getEmails() == null && newAdn.getEmails() != null)) {
                    isEmailOrAnrIsFull = true;
                    prePbrIndex2 = prePbrIndex;
                } else {
                    prePbrIndex2 = prePbrIndex;
                }
            } else {
                index2 = index3;
            }
            if (!isEmailOrAnrIsFull && oldAdn.isEqual(nextAdnRecord)) {
                index = count;
                break;
            }
            count++;
            index3 = index2;
            i = 20272;
        }
        logd("updateAdnBySearch  index :" + index);
        if (index == -1) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "Adn record don't exist for " + oldAdn);
            return;
        }
        if (efid == 20272) {
            AdnRecord foundAdn = oldAdnList.get(index - 1);
            newAdn.mEfid = foundAdn.mEfid;
            newAdn.mExtRecord = foundAdn.mExtRecord;
            newAdn.mRecordNumber = foundAdn.mRecordNumber;
            oldAdn.setAdditionalNumbers(foundAdn.getAdditionalNumbers());
            oldAdn.setEmails(foundAdn.getEmails());
            newAdn.updateAnrEmailArray(oldAdn, getUsimPhoneBookManager().getEmailFilesCountEachAdn(), getUsimPhoneBookManager().getAnrFilesCountEachAdn());
        } else if (efid == 28474) {
            AdnRecord foundAdn2 = oldAdnList.get(index - 1);
            newAdn.mEfid = foundAdn2.mEfid;
            newAdn.mExtRecord = foundAdn2.mExtRecord;
            newAdn.mRecordNumber = foundAdn2.mRecordNumber;
        }
        if (((Message) this.mUserWriteResponse.get(efid)) != null) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "Have pending update for EF:" + efid);
        } else if (efid == 20272) {
            updateEmailAndAnr(efid, oldAdn, newAdn, index, pin2, response);
        } else if (!getUsimPhoneBookManager().updateExt1File(index, oldAdn, newAdn, extensionEF)) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "update ext1 failed");
        } else {
            this.mUserWriteResponse.put(efid, response);
            new AdnRecordLoader(adnRecordCacheUtils.getFh(this)).updateEF(newAdn, efid, extensionEF, index, pin2, obtainMessage(2, efid, index, newAdn));
        }
    }

    private void updateEmailAndAnr(int efid, AdnRecord oldAdn, AdnRecord newAdn, int index, String pin2, Message response) {
        int extensionEF = extensionEfForEf(newAdn.mEfid);
        if (!updateUsimRecord(oldAdn, newAdn, index, USIM_EFEMAIL_TAG)) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "update email failed");
        } else if (!updateUsimRecord(oldAdn, newAdn, index, USIM_EFANR_TAG)) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "update anr failed");
        } else if (getUsimPhoneBookManager().updateExt1File(index, oldAdn, newAdn, (int) USIM_EFEXT1_TAG)) {
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
        if (tag != USIM_EFEMAIL_TAG) {
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
        int maxLen;
        String oldRecord;
        if (tag == USIM_EFANR_TAG) {
            oldRecords = oldAdn.getAdditionalNumbers();
            newRecords = newAdn.getAdditionalNumbers();
        } else if (tag != USIM_EFEMAIL_TAG) {
            return false;
        } else {
            oldRecords = oldAdn.getEmails();
            newRecords = newAdn.getEmails();
        }
        boolean isAllEmpty = oldRecords == null && newRecords == null;
        boolean isOldEmpty = oldRecords == null && newRecords != null;
        boolean isNewEmpty = oldRecords != null && newRecords == null;
        if (isAllEmpty) {
            Rlog.e(TAG, "Both old and new EMAIL/ANR are null");
            return true;
        } else if (isOldEmpty) {
            boolean success = true;
            for (int i = 0; i < newRecords.length; i++) {
                if (!TextUtils.isEmpty(newRecords[i])) {
                    success = updateAnrEmailFile(null, newRecords[i], index, tag, i) & success;
                }
            }
            return success;
        } else if (isNewEmpty) {
            boolean success2 = true;
            for (int i2 = 0; i2 < oldRecords.length; i2++) {
                if (!TextUtils.isEmpty(oldRecords[i2])) {
                    success2 = updateAnrEmailFile(oldRecords[i2], null, index, tag, i2) & success2;
                }
            }
            return success2;
        } else {
            int maxLen2 = oldRecords.length > newRecords.length ? oldRecords.length : newRecords.length;
            boolean success3 = true;
            int i3 = 0;
            String oldRecord2 = null;
            while (i3 < maxLen2) {
                String newRecord = null;
                String oldRecord3 = i3 >= oldRecords.length ? null : oldRecords[i3];
                if (i3 < newRecords.length) {
                    newRecord = newRecords[i3];
                }
                if ((TextUtils.isEmpty(oldRecord3) && TextUtils.isEmpty(newRecord)) || !(oldRecord3 == null || newRecord == null || !oldRecord3.equals(newRecord))) {
                    oldRecord = oldRecord3;
                    maxLen = maxLen2;
                } else {
                    oldRecord = oldRecord3;
                    maxLen = maxLen2;
                    success3 &= updateAnrEmailFile(oldRecord3, newRecord, index, tag, i3);
                }
                i3++;
                oldRecord2 = oldRecord;
                maxLen2 = maxLen;
            }
            return success3;
        }
    }

    public void updateUsimAdnByIndexHW(int efid, AdnRecord newAdn, int sEf_id, int recordIndex, String pin2, Message response) {
        ArrayList<AdnRecord> oldAdnList;
        int index;
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "EF is not known ADN-like EF:" + efid);
            return;
        }
        if (efid == 20272) {
            try {
                oldAdnList = getUsimPhoneBookManager().loadEfFilesFromUsim();
            } catch (NullPointerException e) {
                oldAdnList = null;
            }
        } else {
            oldAdnList = getRecordsIfLoaded(efid);
        }
        if (oldAdnList == null) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "Adn list not exist for EF:" + efid);
            return;
        }
        if (efid == 20272) {
            int pbrIndex = getUsimPhoneBookManager().getPbrIndexByEfid(sEf_id);
            int index2 = recordIndex + getUsimPhoneBookManager().getInitIndexByPbr(pbrIndex);
            logd("sEf_id " + sEf_id + " index " + index2 + " pbrIndex " + pbrIndex);
            AdnRecord foundAdn = oldAdnList.get(index2 + -1);
            newAdn.mEfid = foundAdn.mEfid;
            newAdn.mExtRecord = foundAdn.mExtRecord;
            newAdn.mRecordNumber = foundAdn.mRecordNumber;
            index = index2;
        } else {
            if (efid == 28474) {
                AdnRecord foundAdn2 = oldAdnList.get(recordIndex - 1);
                newAdn.mEfid = foundAdn2.mEfid;
                newAdn.mExtRecord = foundAdn2.mExtRecord;
                newAdn.mRecordNumber = foundAdn2.mRecordNumber;
            }
            index = recordIndex;
        }
        if (((Message) this.mUserWriteResponse.get(efid)) != null) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "Have pending update for EF:" + efid);
        } else if (efid == 20272) {
            updateEmailAndAnr(efid, oldAdnList.get(index - 1), newAdn, index, pin2, response);
        } else if (!getUsimPhoneBookManager().updateExt1File(index, oldAdnList.get(index - 1), newAdn, extensionEF)) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "update ext1 failed");
        } else {
            this.mUserWriteResponse.put(efid, response);
            new AdnRecordLoader(adnRecordCacheUtils.getFh(this)).updateEF(newAdn, efid, extensionEF, index, pin2, obtainMessage(2, efid, index, newAdn));
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
            String str = TAG;
            Rlog.i(str, "getRecordsSize(): recordSize[" + i2 + "] = " + recordSize[i2]);
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
        Rlog.i(TAG, msg);
    }
}
