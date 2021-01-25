package com.android.internal.telephony.uicc;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.uicc.AdnRecordExt;
import com.huawei.internal.telephony.uicc.AdnRecordLoaderEx;
import com.huawei.internal.telephony.uicc.IccCardApplicationStatusEx;
import com.huawei.internal.telephony.uicc.UiccProfileEx;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class HwAdnRecordCacheEx extends Handler implements IHwAdnRecordCacheEx {
    private static final String LOG_TAG = "HwAdnRecordCacheEx";
    public static final AtomicReference<Integer> UPDATE_ADN_RECORD_EFID = new AtomicReference<>();
    public static final AtomicReference<Integer> UPDATE_ADN_RECORD_INDEX = new AtomicReference<>();
    private static final int USIM_ADN_MAX_LENGTH_WITHOUT_EXT = 20;
    private static final int USIM_EFANR_TAG = 196;
    private static final int USIM_EFEMAIL_TAG = 202;
    private static final int USIM_EFEXT1_TAG = 194;
    private SparseArray<ArrayList<Message>> mAdnLikeWaiters = null;
    private int mAdncountofIcc = 0;
    private IAdnRecordCacheInner mHwAdnRecordCacheInner = null;
    private IIccFileHandlerInner mIccFileHandler = null;

    public HwAdnRecordCacheEx(IAdnRecordCacheInner adnRecordCacheInner, IIccFileHandlerInner fileHandlerInner) {
        this.mHwAdnRecordCacheInner = adnRecordCacheInner;
        this.mIccFileHandler = fileHandlerInner;
        this.mAdnLikeWaiters = this.mHwAdnRecordCacheInner.getAdnLikeWaiters();
    }

    public void requestLoadAllAdnHw(int fileId, int extensionEf, Message response) {
        Object object = AdnRecordExt.convertToAdnRecords(this.mHwAdnRecordCacheInner.getRecordsIfLoadedForEx(fileId));
        if (object != null) {
            if (response != null) {
                AsyncResultEx.forMessage(response).setResult(object);
                response.sendToTarget();
            }
            logd("getRecords Loaded.");
            return;
        }
        ArrayList<Message> waiters = this.mAdnLikeWaiters.get(fileId);
        if (waiters != null) {
            waiters.add(response);
        } else if (extensionEf < 0) {
            if (response != null) {
                AsyncResultEx forMessage = AsyncResultEx.forMessage(response);
                forMessage.setException(new RuntimeException("EF is not known ADN-like EF:0x" + Integer.toHexString(fileId).toUpperCase(Locale.ENGLISH)));
                response.sendToTarget();
            }
            RlogEx.e(LOG_TAG, "extensionEf < 0.");
        } else {
            IAdnRecordLoaderInner iAdnRecordLoaderInner = IAdnRecordLoaderInner.makeAdnRecordLoaderInstance(this.mIccFileHandler);
            if (iAdnRecordLoaderInner != null) {
                ArrayList<Message> waiters2 = new ArrayList<>();
                waiters2.add(response);
                this.mAdnLikeWaiters.put(fileId, waiters2);
                iAdnRecordLoaderInner.loadAllAdnFromEFHw(fileId, extensionEf, obtainMessage(IAdnRecordCacheInner.getEventIdLoadAllAdnLikeDoneHw(), fileId, 0));
                return;
            }
            AsyncResultEx forMessage2 = AsyncResultEx.forMessage(response);
            forMessage2.setException(new RuntimeException("Unexpected error occur when load EF:0x" + Integer.toHexString(fileId).toUpperCase(Locale.ENGLISH)));
            response.sendToTarget();
        }
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        this.mHwAdnRecordCacheInner.handleMessageForEx(msg);
    }

    public void updateAdnRecordId(AdnRecordExt adn, int efid, int index) {
        if (!(adn == null || efid == 20272)) {
            adn.setEfid(efid);
            adn.setRecordNumber(index);
        }
        if (efid != 20272) {
            UPDATE_ADN_RECORD_EFID.set(Integer.valueOf(efid));
            UPDATE_ADN_RECORD_INDEX.set(Integer.valueOf(index));
        } else if (adn != null) {
            UPDATE_ADN_RECORD_EFID.set(Integer.valueOf(adn.getEfid()));
            UPDATE_ADN_RECORD_INDEX.set(Integer.valueOf(adn.getRecordNumber()));
        }
    }

    private int getUsimExtensionEfForAdnEf(int adnEfid) {
        logd("getUsimExtensionEfForAdnEf adnEfid = " + adnEfid);
        UiccProfileEx uiccProfileEx = this.mIccFileHandler.getUiccProfileEx();
        if (uiccProfileEx != null && uiccProfileEx.isApplicationOnIcc(IccCardApplicationStatusEx.AppTypeEx.APPTYPE_USIM)) {
            logd("getUsimExtensionEfForAdnEf sim application is on APPTYPE_USIM");
            int pbrSize = this.mHwAdnRecordCacheInner.getPbrFileSize();
            logd("getUsimExtensionEfForAdnEf pbrSize = " + pbrSize);
            if (pbrSize <= 0) {
                return -1;
            }
            for (int loop = 0; loop < pbrSize; loop++) {
                int efid = this.mHwAdnRecordCacheInner.getEFidInPBR(loop, 192);
                logd("getUsimExtensionEfForAdnEf loop = " + loop + " ; efid = " + efid);
                if (adnEfid == efid) {
                    int extensionEF = this.mHwAdnRecordCacheInner.getEFidInPBR(loop, (int) USIM_EFEXT1_TAG);
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

    public int extensionEfForEfHw(IAdnRecordCacheInner adnRecordCacheInner, int efid) {
        logd("extensionEfForEfHw efid = " + efid);
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

    private int getAdnRecordIndexToUpdate(AdnRecordExt oldAdn, AdnRecordExt newAdn, ArrayList<AdnRecordExt> adnRecordExts, int efid) {
        int count = 1;
        int prePbrIndex = -2;
        int anrNum = 0;
        int emailNum = 0;
        Iterator<AdnRecordExt> it = adnRecordExts.iterator();
        while (it.hasNext()) {
            AdnRecordExt nextAdnRecord = it.next();
            boolean isEmailOrAnrIsFull = false;
            if (efid == 20272) {
                int pbrIndex = this.mHwAdnRecordCacheInner.getPbrIndexByAdnRecIndex(count - 1);
                if (pbrIndex != prePbrIndex) {
                    anrNum = this.mHwAdnRecordCacheInner.getEmptyAnrNumByPbrindex(pbrIndex);
                    emailNum = this.mHwAdnRecordCacheInner.getEmptyEmailNumByPbrindex(pbrIndex);
                    prePbrIndex = pbrIndex;
                    logd("updateAdnBySearch, pbrIndex: " + pbrIndex + " anrNum:" + anrNum + " emailNum:" + emailNum);
                }
                if ((anrNum == 0 && oldAdn.getAdditionalNumbers() == null && newAdn.getAdditionalNumbers() != null) || (emailNum == 0 && oldAdn.getEmails() == null && newAdn.getEmails() != null)) {
                    isEmailOrAnrIsFull = true;
                }
            }
            if (!isEmailOrAnrIsFull && oldAdn.isSameAdnRecord(nextAdnRecord)) {
                return count;
            }
            count++;
        }
        return -1;
    }

    private void updateAdnRecord(int efid, AdnRecordExt oldAdn, AdnRecordExt newAdn, AdnRecordExt foundAdn) {
        if (efid == 20272) {
            newAdn.setEfid(foundAdn.getEfid());
            newAdn.setExtRecord(foundAdn.getExtRecord());
            newAdn.setRecordNumber(foundAdn.getRecordNumber());
            oldAdn.setAdditionalNumbers(foundAdn.getAdditionalNumbers());
            oldAdn.setEmails(foundAdn.getEmails());
            newAdn.updateAnrEmailArray(oldAdn, this.mHwAdnRecordCacheInner.getEmailFilesCountEachAdn(), this.mHwAdnRecordCacheInner.getAnrFilesCountEachAdn());
        } else if (efid == 28474) {
            newAdn.setEfid(foundAdn.getEfid());
            newAdn.setExtRecord(foundAdn.getExtRecord());
            newAdn.setRecordNumber(foundAdn.getRecordNumber());
        }
    }

    public void updateAdnBySearchHw(int efid, AdnRecordExt oldAdn, AdnRecordExt newAdn, String pin2, Message response) {
        if (oldAdn == null || newAdn == null) {
            this.mHwAdnRecordCacheInner.sendErrorResponseHw(response, "para is null error");
            return;
        }
        int extensionEF = extensionEfForEfHw(this.mHwAdnRecordCacheInner, efid);
        if (extensionEF < 0) {
            IAdnRecordCacheInner iAdnRecordCacheInner = this.mHwAdnRecordCacheInner;
            iAdnRecordCacheInner.sendErrorResponseHw(response, "EF is not known ADN-like EF:" + efid);
            return;
        }
        ArrayList<AdnRecordExt> oldAdnList = this.mHwAdnRecordCacheInner.getAdnRecordsLoaded(efid);
        if (oldAdnList == null) {
            IAdnRecordCacheInner iAdnRecordCacheInner2 = this.mHwAdnRecordCacheInner;
            iAdnRecordCacheInner2.sendErrorResponseHw(response, "Adn list not exist for EF:" + efid);
            return;
        }
        int index = getAdnRecordIndexToUpdate(oldAdn, newAdn, oldAdnList, efid);
        logd("updateAdnBySearchHw index :" + index);
        if (index == -1) {
            IAdnRecordCacheInner iAdnRecordCacheInner3 = this.mHwAdnRecordCacheInner;
            iAdnRecordCacheInner3.sendErrorResponseHw(response, "Adn record don't exist for " + oldAdn);
            return;
        }
        updateAdnRecord(efid, oldAdn, newAdn, oldAdnList.get(index - 1));
        if (this.mHwAdnRecordCacheInner.getUserWriteResponseHw(efid) != null) {
            IAdnRecordCacheInner iAdnRecordCacheInner4 = this.mHwAdnRecordCacheInner;
            iAdnRecordCacheInner4.sendErrorResponseHw(response, "Have pending update for EF:" + efid);
        } else if (efid == 20272) {
            updateEmailAndAnr(efid, oldAdn, newAdn, index, pin2, response);
        } else if (!this.mHwAdnRecordCacheInner.updateExt1File(index, oldAdn, newAdn, extensionEF)) {
            this.mHwAdnRecordCacheInner.sendErrorResponseHw(response, "update ext1 failed");
        } else {
            this.mHwAdnRecordCacheInner.putUserWriteResponseHw(efid, response);
            AdnRecordLoaderEx.updateEF(this.mIccFileHandler, newAdn, efid, extensionEF, index, pin2, obtainMessage(IAdnRecordCacheInner.getEventIdUpdateAdnDoneHw(), efid, index, newAdn.getAdnRecord()));
        }
    }

    private void updateEmailAndAnr(int efid, AdnRecordExt oldAdn, AdnRecordExt newAdn, int index, String pin2, Message response) {
        if (!updateUsimRecord(oldAdn, newAdn, index, USIM_EFEMAIL_TAG)) {
            this.mHwAdnRecordCacheInner.sendErrorResponseHw(response, "update email failed");
        } else if (!updateUsimRecord(oldAdn, newAdn, index, USIM_EFANR_TAG)) {
            this.mHwAdnRecordCacheInner.sendErrorResponseHw(response, "update anr failed");
        } else if (this.mHwAdnRecordCacheInner.updateExt1File(index, oldAdn, newAdn, (int) USIM_EFEXT1_TAG)) {
            int extensionEF = extensionEfForEfHw(this.mHwAdnRecordCacheInner, newAdn.getEfid());
            this.mHwAdnRecordCacheInner.putUserWriteResponseHw(efid, response);
            AdnRecordLoaderEx.updateEF(this.mIccFileHandler, newAdn, newAdn.getEfid(), extensionEF, newAdn.getRecordNumber(), pin2, obtainMessage(IAdnRecordCacheInner.getEventIdUpdateAdnDoneHw(), efid, index, newAdn.getAdnRecord()));
        } else {
            this.mHwAdnRecordCacheInner.sendErrorResponseHw(response, "update ext1 failed");
        }
    }

    private boolean updateAnrEmailFile(String oldRecord, String newRecord, int index, int tag, int efidIndex) {
        if (tag == USIM_EFANR_TAG) {
            return this.mHwAdnRecordCacheInner.updateAnrFile(index, oldRecord, newRecord, efidIndex);
        }
        if (tag != USIM_EFEMAIL_TAG) {
            return false;
        }
        try {
            return this.mHwAdnRecordCacheInner.updateEmailFile(index, oldRecord, newRecord, efidIndex);
        } catch (RuntimeException e) {
            RlogEx.e(LOG_TAG, "update usim record failed, exception catched.");
            return false;
        }
    }

    private boolean updateUsimRecord(AdnRecordExt oldAdn, AdnRecordExt newAdn, int index, int tag) {
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
            RlogEx.e(LOG_TAG, "Both old and new EMAIL/ANR are null");
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

    public void updateUsimAdnByIndexHw(int efid, AdnRecordExt newAdn, int sEf_id, int recordIndex, String pin2, Message response) {
        int index;
        if (newAdn == null) {
            this.mHwAdnRecordCacheInner.sendErrorResponseHw(response, "update adn is null.");
            return;
        }
        int extensionEF = extensionEfForEfHw(this.mHwAdnRecordCacheInner, efid);
        if (extensionEF < 0) {
            IAdnRecordCacheInner iAdnRecordCacheInner = this.mHwAdnRecordCacheInner;
            iAdnRecordCacheInner.sendErrorResponseHw(response, "EF is not known ADN-like EF:" + efid);
            return;
        }
        ArrayList<AdnRecordExt> oldAdnList = this.mHwAdnRecordCacheInner.getAdnRecordsLoaded(efid);
        if (oldAdnList == null) {
            IAdnRecordCacheInner iAdnRecordCacheInner2 = this.mHwAdnRecordCacheInner;
            iAdnRecordCacheInner2.sendErrorResponseHw(response, "Adn list not exist for EF:" + efid);
            return;
        }
        if (efid == 20272) {
            int pbrIndex = this.mHwAdnRecordCacheInner.getPbrIndexByEfid(sEf_id);
            int index2 = recordIndex + this.mHwAdnRecordCacheInner.getInitIndexByPbr(pbrIndex);
            logd("sEf_id " + sEf_id + " index " + index2 + " pbrIndex " + pbrIndex);
            index = index2;
        } else {
            index = recordIndex;
        }
        if (index < 0 || index > oldAdnList.size()) {
            IAdnRecordCacheInner iAdnRecordCacheInner3 = this.mHwAdnRecordCacheInner;
            iAdnRecordCacheInner3.sendErrorResponseHw(response, "Adn list not exist for index:" + index);
            RlogEx.e(LOG_TAG, "Invalid index : " + index);
            return;
        }
        AdnRecordExt foundAdn = oldAdnList.get(index - 1);
        newAdn.setEfid(foundAdn.getEfid());
        newAdn.setExtRecord(foundAdn.getExtRecord());
        newAdn.setRecordNumber(foundAdn.getRecordNumber());
        if (this.mHwAdnRecordCacheInner.getUserWriteResponseHw(efid) != null) {
            IAdnRecordCacheInner iAdnRecordCacheInner4 = this.mHwAdnRecordCacheInner;
            iAdnRecordCacheInner4.sendErrorResponseHw(response, "Have pending update for EF:" + efid);
        } else if (efid == 20272) {
            updateEmailAndAnr(efid, oldAdnList.get(index - 1), newAdn, index, pin2, response);
        } else if (!this.mHwAdnRecordCacheInner.updateExt1File(index, oldAdnList.get(index - 1), newAdn, extensionEF)) {
            this.mHwAdnRecordCacheInner.sendErrorResponseHw(response, "update ext1 failed");
        } else {
            this.mHwAdnRecordCacheInner.putUserWriteResponseHw(efid, response);
            AdnRecordLoaderEx.updateEF(this.mIccFileHandler, newAdn, efid, extensionEF, index, pin2, obtainMessage(IAdnRecordCacheInner.getEventIdUpdateAdnDoneHw(), efid, index, newAdn.getAdnRecord()));
        }
    }

    public int getAdnCountHw() {
        return this.mAdncountofIcc;
    }

    public void setAdnCountHw(int count) {
        this.mAdncountofIcc = count;
    }

    private int getRecordsSizeByIdInAdnlist(int efid) {
        ArrayList<AdnRecordExt> adnList = this.mHwAdnRecordCacheInner.getRecordsIfLoadedForEx(efid);
        logd("getRecordsSizeByIdInAdnlist efid " + efid);
        if (adnList == null) {
            return 0;
        }
        return adnList.size();
    }

    private int getRecordsFreeSizeByIdInAdnlist(int efid) {
        ArrayList<AdnRecordExt> adnList = this.mHwAdnRecordCacheInner.getRecordsIfLoadedForEx(efid);
        logd("getRecordsFreeSizeByIdInAdnlist efid " + efid);
        int recordsFreeSize = 0;
        if (adnList == null) {
            return 0;
        }
        int adnListSize = adnList.size();
        for (int i = 0; i < adnListSize; i++) {
            if (adnList.get(i).isEmpty()) {
                recordsFreeSize++;
            }
        }
        return recordsFreeSize;
    }

    public int[] getRecordsSizeHw() {
        int[] recordSize = new int[9];
        logd("getRecordsSize(): enter.");
        for (int i = 0; i < recordSize.length; i++) {
            recordSize[i] = -1;
        }
        recordSize[0] = 0;
        boolean isCsim3Gphonebook = false;
        if (this.mIccFileHandler.isInstanceOfCsimFileHandler()) {
            isCsim3Gphonebook = this.mIccFileHandler.has3Gphonebook();
        }
        boolean isUsim3Gphonebook = false;
        if (this.mIccFileHandler.isInstanceOfUsimFileHandler()) {
            isUsim3Gphonebook = this.mIccFileHandler.has3Gphonebook();
        }
        if (isUsim3Gphonebook || isCsim3Gphonebook || this.mIccFileHandler.isInstanceOfIsimFileHandler()) {
            logd("getRecordsSize(): usim card branch.");
            if (this.mHwAdnRecordCacheInner.getUsimPhoneBookManager() == null) {
                return recordSize;
            }
            int adnCount = this.mHwAdnRecordCacheInner.getUsimAdnCountHw();
            if (adnCount > 0) {
                recordSize[2] = adnCount;
                recordSize[1] = this.mHwAdnRecordCacheInner.getAdnRecordsFreeSize();
            }
            int emailCount = this.mHwAdnRecordCacheInner.getEmailCountHw();
            if (emailCount > 0) {
                recordSize[5] = emailCount;
                if (!(recordSize[5] == -1 || recordSize[5] == 0)) {
                    recordSize[4] = this.mHwAdnRecordCacheInner.getSpareEmailCountHw();
                    recordSize[3] = 1;
                }
            }
            int anrCount = this.mHwAdnRecordCacheInner.getAnrCountHw();
            if (anrCount > 0) {
                recordSize[8] = anrCount;
                if (!(recordSize[8] == -1 || recordSize[8] == 0)) {
                    recordSize[7] = this.mHwAdnRecordCacheInner.getSpareAnrCountHw();
                    recordSize[6] = 2;
                }
            }
        } else {
            logd("getRecordsSize(): sim card branch.");
            recordSize[1] = getRecordsFreeSizeByIdInAdnlist(28474);
            recordSize[2] = getRecordsSizeByIdInAdnlist(28474);
        }
        for (int i2 = 0; i2 < recordSize.length; i2++) {
            logd("getRecordsSize(): recordSize[" + i2 + "] = " + recordSize[i2]);
        }
        return recordSize;
    }

    public void resetHw() {
        this.mAdncountofIcc = 0;
    }

    public void updateUsimPhoneBookRecordHw(AdnRecordExt adnRecordExt, int efid, int index) {
        if (efid == 20272) {
            ArrayList<AdnRecordExt> tempAdnList = this.mHwAdnRecordCacheInner.getUsimPhoneBookManager().loadEfFilesFromUsimHw();
            if (tempAdnList == null || index <= 0 || index > tempAdnList.size()) {
                logd("loadEfFilesFromUsim result null.");
            } else {
                tempAdnList.set(index - 1, adnRecordExt);
            }
        }
    }

    private void logd(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }
}
