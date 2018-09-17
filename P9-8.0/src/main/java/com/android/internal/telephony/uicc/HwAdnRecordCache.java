package com.android.internal.telephony.uicc;

import android.os.Message;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
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
    private static AdnRecordCacheUtils adnRecordCacheUtils = ((AdnRecordCacheUtils) EasyInvokeFactory.getInvokeUtils(AdnRecordCacheUtils.class));
    public static final AtomicReference<Integer> s_efid = new AtomicReference();
    public static final AtomicReference<Integer> s_index = new AtomicReference();
    private static UiccCardApplicationUtils uiccCardApplicationUtils = new UiccCardApplicationUtils();
    private int mAdncountofIcc = 0;

    public HwAdnRecordCache(IccFileHandler fh) {
        super(fh);
    }

    protected void updateAdnRecordId(AdnRecord adn, int efid, int index) {
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
        if (uiccCardApplicationUtils.getUiccCard(adnRecordCacheUtils.getFh(this).mParentApp).isApplicationOnIcc(AppType.APPTYPE_USIM)) {
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
        switch (efid) {
            case 20272:
                return 0;
            case 28474:
                return 28490;
            case 28475:
                return 28491;
            case 28480:
                return 28490;
            case 28489:
                return 28492;
            case 28615:
                return 28616;
            default:
                return getUsimExtensionEfForAdnEf(efid);
        }
    }

    public void updateAdnBySearch(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        if (IccRecords.getEmailAnrSupport()) {
            int extensionEF = extensionEfForEf(efid);
            if (extensionEF < 0) {
                adnRecordCacheUtils.sendErrorResponse(this, response, "EF is not known ADN-like EF:" + efid);
                return;
            }
            ArrayList oldAdnList;
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
            int index = -1;
            int count = 1;
            int prePbrIndex = -2;
            int anrNum = 0;
            int emailNum = 0;
            Iterator<AdnRecord> it = oldAdnList.iterator();
            while (it.hasNext()) {
                AdnRecord nextAdnRecord = (AdnRecord) it.next();
                boolean isEmailOrAnrIsFull = false;
                if (efid == 20272) {
                    int pbrIndex = getUsimPhoneBookManager().getPbrIndexBy(count - 1);
                    if (pbrIndex != prePbrIndex) {
                        anrNum = getUsimPhoneBookManager().getEmptyAnrNum_Pbrindex(pbrIndex);
                        emailNum = getUsimPhoneBookManager().getEmptyEmailNum_Pbrindex(pbrIndex);
                        prePbrIndex = pbrIndex;
                        Rlog.d(TAG, "updateAdnBySearch, pbrIndex: " + pbrIndex + " anrNum:" + anrNum + " emailNum:" + emailNum);
                    }
                    if ((anrNum == 0 && oldAdn.getAdditionalNumbers() == null && newAdn.getAdditionalNumbers() != null) || (emailNum == 0 && oldAdn.getEmails() == null && newAdn.getEmails() != null)) {
                        isEmailOrAnrIsFull = true;
                    }
                }
                if (!isEmailOrAnrIsFull && oldAdn.isEqual(nextAdnRecord)) {
                    index = count;
                    break;
                }
                count++;
            }
            Rlog.d(TAG, "updateAdnBySearch  index :" + index);
            if (index == -1) {
                adnRecordCacheUtils.sendErrorResponse(this, response, "Adn record don't exist for " + oldAdn);
                return;
            }
            AdnRecord foundAdn;
            if (efid == 20272) {
                foundAdn = (AdnRecord) oldAdnList.get(index - 1);
                newAdn.mEfid = foundAdn.mEfid;
                newAdn.mExtRecord = foundAdn.mExtRecord;
                newAdn.mRecordNumber = foundAdn.mRecordNumber;
                oldAdn.setAdditionalNumbers(foundAdn.getAdditionalNumbers());
                oldAdn.setEmails(foundAdn.getEmails());
                newAdn.updateAnrEmailArray(oldAdn, getUsimPhoneBookManager().getEmailFilesCountEachAdn(), getUsimPhoneBookManager().getAnrFilesCountEachAdn());
            } else if (efid == 28474) {
                foundAdn = (AdnRecord) oldAdnList.get(index - 1);
                newAdn.mEfid = foundAdn.mEfid;
                newAdn.mExtRecord = foundAdn.mExtRecord;
                newAdn.mRecordNumber = foundAdn.mRecordNumber;
            }
            if (((Message) this.mUserWriteResponse.get(efid)) != null) {
                adnRecordCacheUtils.sendErrorResponse(this, response, "Have pending update for EF:" + efid);
                return;
            } else if (efid == 20272) {
                updateEmailAndAnr(efid, oldAdn, newAdn, index, pin2, response);
            } else if (getUsimPhoneBookManager().updateExt1File(index, oldAdn, newAdn, extensionEF)) {
                this.mUserWriteResponse.put(efid, response);
                new AdnRecordLoader(adnRecordCacheUtils.getFh(this)).updateEF(newAdn, efid, extensionEF, index, pin2, obtainMessage(2, efid, index, newAdn));
            } else {
                adnRecordCacheUtils.sendErrorResponse(this, response, "update ext1 failed");
                return;
            }
        }
        super.updateAdnBySearch(efid, oldAdn, newAdn, pin2, response);
    }

    private void updateEmailAndAnr(int efid, AdnRecord oldAdn, AdnRecord newAdn, int index, String pin2, Message response) {
        int extensionEF = extensionEfForEf(newAdn.mEfid);
        if (!updateUsimRecord(oldAdn, newAdn, index, USIM_EFEMAIL_TAG)) {
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
        switch (tag) {
            case USIM_EFANR_TAG /*196*/:
                return getUsimPhoneBookManager().updateAnrFile(index, oldRecord, newRecord, efidIndex);
            case USIM_EFEMAIL_TAG /*202*/:
                try {
                    return getUsimPhoneBookManager().updateEmailFile(index, oldRecord, newRecord, efidIndex);
                } catch (RuntimeException e) {
                    Rlog.e(TAG, "update usim record failed", e);
                    return false;
                }
            default:
                return false;
        }
    }

    private boolean updateUsimRecord(AdnRecord oldAdn, AdnRecord newAdn, int index, int tag) {
        String[] oldRecords;
        String[] newRecords;
        boolean success = true;
        switch (tag) {
            case USIM_EFANR_TAG /*196*/:
                oldRecords = oldAdn.getAdditionalNumbers();
                newRecords = newAdn.getAdditionalNumbers();
                break;
            case USIM_EFEMAIL_TAG /*202*/:
                oldRecords = oldAdn.getEmails();
                newRecords = newAdn.getEmails();
                break;
            default:
                return false;
        }
        boolean isAllEmpty = oldRecords == null && newRecords == null;
        boolean isOldEmpty = oldRecords == null && newRecords != null;
        boolean isNewEmpty = oldRecords != null && newRecords == null;
        if (isAllEmpty) {
            Rlog.e(TAG, "Both old and new EMAIL/ANR are null");
            return true;
        }
        String newRecord;
        String str;
        int i;
        if (isOldEmpty) {
            for (i = 0; i < newRecords.length; i++) {
                if (!TextUtils.isEmpty(newRecords[i])) {
                    success &= updateAnrEmailFile(null, newRecords[i], index, tag, i);
                }
            }
        } else if (isNewEmpty) {
            for (i = 0; i < oldRecords.length; i++) {
                if (!TextUtils.isEmpty(oldRecords[i])) {
                    success &= updateAnrEmailFile(oldRecords[i], null, index, tag, i);
                }
            }
        } else {
            int maxLen = oldRecords.length > newRecords.length ? oldRecords.length : newRecords.length;
            i = 0;
            newRecord = null;
            str = null;
            while (i < maxLen) {
                str = i >= oldRecords.length ? null : oldRecords[i];
                Object obj = i >= newRecords.length ? null : newRecords[i];
                boolean noEmpty = (TextUtils.isEmpty(str) && TextUtils.isEmpty(obj)) ? true : (str == null || obj == null) ? false : str.equals(obj);
                if (!noEmpty) {
                    success &= updateAnrEmailFile(str, obj, index, tag, i);
                }
                i++;
            }
            return success;
        }
        newRecord = null;
        str = null;
        return success;
    }

    public void updateUsimAdnByIndexHW(int efid, AdnRecord newAdn, int sEf_id, int recordIndex, String pin2, Message response) {
        int extensionEF = extensionEfForEf(efid);
        if (extensionEF < 0) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "EF is not known ADN-like EF:" + efid);
            return;
        }
        ArrayList oldAdnList;
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
        int index = recordIndex;
        AdnRecord foundAdn;
        if (efid == 20272) {
            int pbrIndex = getUsimPhoneBookManager().getPbrIndexByEfid(sEf_id);
            index = recordIndex + getUsimPhoneBookManager().getInitIndexByPbr(pbrIndex);
            Rlog.d(TAG, "sEf_id " + sEf_id + " index " + index + " pbrIndex " + pbrIndex);
            foundAdn = (AdnRecord) oldAdnList.get(index - 1);
            newAdn.mEfid = foundAdn.mEfid;
            newAdn.mExtRecord = foundAdn.mExtRecord;
            newAdn.mRecordNumber = foundAdn.mRecordNumber;
        } else if (efid == 28474) {
            foundAdn = (AdnRecord) oldAdnList.get(recordIndex - 1);
            newAdn.mEfid = foundAdn.mEfid;
            newAdn.mExtRecord = foundAdn.mExtRecord;
            newAdn.mRecordNumber = foundAdn.mRecordNumber;
        }
        if (((Message) this.mUserWriteResponse.get(efid)) != null) {
            adnRecordCacheUtils.sendErrorResponse(this, response, "Have pending update for EF:" + efid);
            return;
        }
        if (efid == 20272) {
            updateEmailAndAnr(efid, (AdnRecord) oldAdnList.get(index - 1), newAdn, index, pin2, response);
        } else if (getUsimPhoneBookManager().updateExt1File(index, (AdnRecord) oldAdnList.get(index - 1), newAdn, extensionEF)) {
            this.mUserWriteResponse.put(efid, response);
            new AdnRecordLoader(adnRecordCacheUtils.getFh(this)).updateEF(newAdn, efid, extensionEF, index, pin2, obtainMessage(2, efid, index, newAdn));
        } else {
            adnRecordCacheUtils.sendErrorResponse(this, response, "update ext1 failed");
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
        Rlog.i(TAG, "getRecordsSizeByIdInAdnlist efid " + efid);
        if (adnList == null) {
            return 0;
        }
        return adnList.size();
    }

    public int getRecordsFreeSizeByIdInAdnlist(int efid) {
        int RecordsFreeSize = 0;
        ArrayList<AdnRecord> adnList = getRecordsIfLoaded(efid);
        Rlog.i(TAG, "getRecordsFreeSizeByIdInAdnlist efid " + efid);
        if (adnList == null) {
            return 0;
        }
        int adnListSize = adnList.size();
        for (int i = 0; i < adnListSize; i++) {
            if (((AdnRecord) adnList.get(i)).isEmpty()) {
                RecordsFreeSize++;
            }
        }
        return RecordsFreeSize;
    }

    public int[] getRecordsSizeHW() {
        int i;
        int[] recordSize = new int[9];
        Rlog.i(TAG, "getRecordsSize(): enter.");
        for (i = 0; i < recordSize.length; i++) {
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
        for (i = 0; i < recordSize.length; i++) {
            Rlog.i(TAG, "getRecordsSize(): recordSize[" + i + "] = " + recordSize[i]);
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
        super.reset();
        this.mAdncountofIcc = 0;
    }

    private void logd(String msg) {
        Rlog.d(TAG, msg);
    }
}
