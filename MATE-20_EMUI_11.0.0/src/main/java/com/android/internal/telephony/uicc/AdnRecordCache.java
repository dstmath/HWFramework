package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.Rlog;
import android.util.SparseArray;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.gsm.IUsimPhoneBookManagerInner;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.huawei.internal.telephony.uicc.AdnRecordExt;
import java.util.ArrayList;
import java.util.Iterator;

public class AdnRecordCache extends Handler implements IccConstants, IAdnRecordCacheInner {
    static final int EVENT_LOAD_ALL_ADN_LIKE_DONE = 1;
    static final int EVENT_UPDATE_ADN_DONE = 2;
    SparseArray<ArrayList<AdnRecord>> mAdnLikeFiles = new SparseArray<>();
    @UnsupportedAppUsage
    SparseArray<ArrayList<Message>> mAdnLikeWaiters = new SparseArray<>();
    @UnsupportedAppUsage
    private IccFileHandler mFh;
    IHwAdnRecordCacheEx mIHwAdnRecordCacheEx = null;
    @UnsupportedAppUsage
    SparseArray<Message> mUserWriteResponse = new SparseArray<>();
    @UnsupportedAppUsage
    private UsimPhoneBookManager mUsimPhoneBookManager;

    AdnRecordCache(IccFileHandler fh) {
        this.mFh = fh;
        this.mUsimPhoneBookManager = new UsimPhoneBookManager(this.mFh, this);
        this.mIHwAdnRecordCacheEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwAdnRecordCacheEx(this, fh);
    }

    @UnsupportedAppUsage
    public void reset() {
        this.mAdnLikeFiles.clear();
        this.mUsimPhoneBookManager.reset();
        clearWaiters();
        clearUserWriters();
        this.mIHwAdnRecordCacheEx.resetHw();
    }

    private void clearWaiters() {
        int size = this.mAdnLikeWaiters.size();
        for (int i = 0; i < size; i++) {
            notifyWaiters(this.mAdnLikeWaiters.valueAt(i), new AsyncResult((Object) null, (Object) null, new RuntimeException("AdnCache reset")));
        }
        this.mAdnLikeWaiters.clear();
    }

    private void clearUserWriters() {
        int size = this.mUserWriteResponse.size();
        for (int i = 0; i < size; i++) {
            sendErrorResponse(this.mUserWriteResponse.valueAt(i), "AdnCace reset");
        }
        this.mUserWriteResponse.clear();
    }

    @UnsupportedAppUsage
    public ArrayList<AdnRecord> getRecordsIfLoaded(int efid) {
        return this.mAdnLikeFiles.get(efid);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    @UnsupportedAppUsage
    public int extensionEfForEf(int efid) {
        if (efid == 20272) {
            return 0;
        }
        if (efid == 28480) {
            return IccConstants.EF_EXT1;
        }
        if (efid == 28489) {
            return IccConstants.EF_EXT3;
        }
        if (efid == 28615) {
            return IccConstants.EF_EXT6;
        }
        if (efid == 28474) {
            return IccConstants.EF_EXT1;
        }
        if (efid != 28475) {
            return -1;
        }
        return IccConstants.EF_EXT2;
    }

    @UnsupportedAppUsage
    private void sendErrorResponse(Message response, String errString) {
        if (response != null) {
            AsyncResult.forMessage(response).exception = new RuntimeException(errString);
            response.sendToTarget();
        }
    }

    @UnsupportedAppUsage
    public void updateAdnByIndex(int efid, AdnRecord adn, int recordIndex, String pin2, Message response) {
        int extensionEF = extensionEfForEfHw(efid);
        if (extensionEF < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
        } else if (this.mUserWriteResponse.get(efid) != null) {
            sendErrorResponse(response, "Have pending update for EF:0x" + Integer.toHexString(efid).toUpperCase());
        } else {
            this.mUserWriteResponse.put(efid, response);
            new AdnRecordLoader(this.mFh).updateEF(adn, efid, extensionEF, recordIndex, pin2, obtainMessage(2, efid, recordIndex, adn));
        }
    }

    public void updateAdnBySearch(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        ArrayList<AdnRecord> oldAdnList;
        int index;
        int extensionEF;
        int efid2;
        if (IccRecords.getEmailAnrSupport()) {
            this.mIHwAdnRecordCacheEx.updateAdnBySearchHw(efid, AdnRecordExt.from(oldAdn), AdnRecordExt.from(newAdn), pin2, response);
            return;
        }
        int extensionEF2 = extensionEfForEfHw(efid);
        if (extensionEF2 < 0) {
            sendErrorResponse(response, "EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
            return;
        }
        if (efid == 20272) {
            oldAdnList = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
        } else {
            oldAdnList = getRecordsIfLoaded(efid);
        }
        if (oldAdnList == null) {
            sendErrorResponse(response, "Adn list not exist for EF:0x" + Integer.toHexString(efid).toUpperCase());
            return;
        }
        int index2 = -1;
        Iterator<AdnRecord> it = oldAdnList.iterator();
        int count = 1;
        while (true) {
            if (!it.hasNext()) {
                break;
            } else if (oldAdn.isEqual(it.next())) {
                index2 = count;
                break;
            } else {
                count++;
            }
        }
        if (index2 == -1) {
            sendErrorResponse(response, "Adn record don't exist for " + oldAdn);
            return;
        }
        if (efid == 20272) {
            AdnRecord foundAdn = oldAdnList.get(index2 - 1);
            int efid3 = foundAdn.mEfid;
            int extensionEF3 = foundAdn.mExtRecord;
            int index3 = foundAdn.mRecordNumber;
            newAdn.mEfid = efid3;
            newAdn.mExtRecord = extensionEF3;
            newAdn.mRecordNumber = index3;
            extensionEF = extensionEF3;
            index = index3;
            efid2 = efid3;
        } else {
            extensionEF = extensionEF2;
            index = index2;
            efid2 = efid;
        }
        if (this.mUserWriteResponse.get(efid2) != null) {
            sendErrorResponse(response, "Have pending update for EF:0x" + Integer.toHexString(efid2).toUpperCase());
            return;
        }
        this.mUserWriteResponse.put(efid2, response);
        new AdnRecordLoader(this.mFh).updateEF(newAdn, efid2, extensionEF, index, pin2, obtainMessage(2, efid2, index, newAdn));
    }

    public void requestLoadAllAdnLike(int efid, int extensionEf, Message response) {
        ArrayList<AdnRecord> result;
        if (efid == 20272) {
            result = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
        } else {
            result = getRecordsIfLoaded(efid);
        }
        if (result == null) {
            ArrayList<Message> waiters = this.mAdnLikeWaiters.get(efid);
            if (waiters != null) {
                waiters.add(response);
            } else if (extensionEf >= 0) {
                ArrayList<Message> waiters2 = new ArrayList<>();
                waiters2.add(response);
                this.mAdnLikeWaiters.put(efid, waiters2);
                new AdnRecordLoader(this.mFh).loadAllFromEF(efid, extensionEf, obtainMessage(1, efid, 0));
            } else if (response != null) {
                AsyncResult forMessage = AsyncResult.forMessage(response);
                forMessage.exception = new RuntimeException("EF is not known ADN-like EF:0x" + Integer.toHexString(efid).toUpperCase());
                response.sendToTarget();
            }
        } else if (response != null) {
            AsyncResult.forMessage(response).result = result;
            response.sendToTarget();
        }
    }

    private void notifyWaiters(ArrayList<Message> waiters, AsyncResult ar) {
        if (waiters != null) {
            int s = waiters.size();
            for (int i = 0; i < s; i++) {
                Message waiter = waiters.get(i);
                AsyncResult.forMessage(waiter, ar.result, ar.exception);
                waiter.sendToTarget();
            }
        }
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        Rlog.e("ADN RECORD", "handle message = " + msg.what);
        int i = msg.what;
        if (i == 1) {
            AsyncResult ar = (AsyncResult) msg.obj;
            int efid = msg.arg1;
            ArrayList<Message> waiters = this.mAdnLikeWaiters.get(efid);
            this.mAdnLikeWaiters.delete(efid);
            if (ar.exception == null) {
                this.mAdnLikeFiles.put(efid, (ArrayList) ar.result);
            }
            notifyWaiters(waiters, ar);
            if (IccRecords.getEmailAnrSupport() && this.mAdnLikeFiles.get(28474) != null) {
                this.mIHwAdnRecordCacheEx.setAdnCountHw(this.mAdnLikeFiles.get(28474).size());
            }
            if (IccRecords.getAdnLongNumberSupport() && this.mAdnLikeFiles.get(28474) != null) {
                this.mUsimPhoneBookManager.readExt1FileForSim(efid);
            }
        } else if (i == 2) {
            AsyncResult ar2 = (AsyncResult) msg.obj;
            int efid2 = msg.arg1;
            int index = msg.arg2;
            AdnRecord adn = (AdnRecord) ar2.userObj;
            AdnRecordExt adnRecordExt = AdnRecordExt.from(adn);
            this.mIHwAdnRecordCacheEx.updateAdnRecordId(adnRecordExt, efid2, index);
            if (ar2.exception == null) {
                if (this.mAdnLikeFiles.get(efid2) != null) {
                    if (index < 1 || index > this.mAdnLikeFiles.get(efid2).size()) {
                        Rlog.i("ADN RECORD", "IndexOutOfBounds index = " + index + ",size = " + this.mAdnLikeFiles.get(efid2).size());
                    } else {
                        this.mAdnLikeFiles.get(efid2).set(index - 1, adn);
                    }
                    Rlog.i("ADN RECORD", "index is " + index);
                } else {
                    Rlog.e("ADN RECORD", "mAdnLikeFiles.get is null, efid = " + efid2);
                }
                if (!IccRecords.getEmailAnrSupport()) {
                    this.mUsimPhoneBookManager.invalidateCache();
                } else {
                    this.mIHwAdnRecordCacheEx.updateUsimPhoneBookRecordHw(adnRecordExt, efid2, index);
                }
            }
            Message response = this.mUserWriteResponse.get(efid2);
            this.mUserWriteResponse.delete(efid2);
            if (response != null) {
                AsyncResult.forMessage(response, (Object) null, ar2.exception);
                response.sendToTarget();
            }
        }
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public void requestLoadAllAdnHw(int fileId, int extensionEf, Message response) {
        this.mIHwAdnRecordCacheEx.requestLoadAllAdnHw(fileId, extensionEf, response);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public ArrayList<AdnRecordExt> getRecordsIfLoadedForEx(int fileId) {
        return AdnRecordExt.convertAdnRecordToExt(getRecordsIfLoaded(fileId));
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public SparseArray<ArrayList<Message>> getAdnLikeWaiters() {
        return this.mAdnLikeWaiters;
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public void handleMessageForEx(Message msg) {
        handleMessage(msg);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int extensionEfForEfHw(int efid) {
        return this.mIHwAdnRecordCacheEx.extensionEfForEfHw(this, efid);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getEFidInPBR(int recNum, int tag) {
        return this.mUsimPhoneBookManager.getEFidInPBR(recNum, tag);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getPbrFileSize() {
        return this.mUsimPhoneBookManager.getPbrFileSize();
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public void sendErrorResponseHw(Message response, String errString) {
        sendErrorResponse(response, errString);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public ArrayList<AdnRecordExt> getAdnRecordsLoaded(int efid) {
        ArrayList<AdnRecord> adnRecords;
        if (efid == 20272) {
            try {
                adnRecords = this.mUsimPhoneBookManager.loadEfFilesFromUsim();
            } catch (NullPointerException e) {
                return null;
            }
        } else {
            adnRecords = getRecordsIfLoaded(efid);
        }
        return AdnRecordExt.convertAdnRecordToExt(adnRecords);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getPbrIndexByAdnRecIndex(int adnIndex) {
        return this.mUsimPhoneBookManager.getPbrIndexBy(adnIndex);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getEmptyAnrNumByPbrindex(int pbrIndex) {
        return this.mUsimPhoneBookManager.getEmptyAnrNumByPbrindex(pbrIndex);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getEmptyEmailNumByPbrindex(int pbrIndex) {
        return this.mUsimPhoneBookManager.getEmptyEmailNumByPbrindex(pbrIndex);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public boolean updateExt1File(int adnRecNum, AdnRecordExt oldAdnRecord, AdnRecordExt newAdnRecord, int tagOrEfid) {
        return this.mUsimPhoneBookManager.updateExt1File(adnRecNum, oldAdnRecord, newAdnRecord, tagOrEfid);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getEmailFilesCountEachAdn() {
        return this.mUsimPhoneBookManager.getEmailFilesCountEachAdn();
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getAnrFilesCountEachAdn() {
        return this.mUsimPhoneBookManager.getAnrFilesCountEachAdn();
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public boolean updateEmailFile(int adnRecNum, String oldEmail, String newEmail, int efidIndex) {
        return this.mUsimPhoneBookManager.updateEmailFile(adnRecNum, oldEmail, newEmail, efidIndex);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public boolean updateAnrFile(int adnRecNum, String oldAnr, String newAnr, int efidIndex) {
        return this.mUsimPhoneBookManager.updateAnrFile(adnRecNum, oldAnr, newAnr, efidIndex);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public Message getUserWriteResponseHw(int efid) {
        return this.mUserWriteResponse.get(efid);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public void putUserWriteResponseHw(int efid, Message response) {
        this.mUserWriteResponse.put(efid, response);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public void updateUsimAdnByIndexHw(int efid, AdnRecordExt newAdn, int originEfid, int recordIndex, String pin2, Message response) {
        this.mIHwAdnRecordCacheEx.updateUsimAdnByIndexHw(efid, newAdn, originEfid, recordIndex, pin2, response);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getPbrIndexByEfid(int efid) {
        return this.mUsimPhoneBookManager.getPbrIndexByEfid(efid);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getInitIndexByPbr(int pbrIndex) {
        return this.mUsimPhoneBookManager.getInitIndexByPbr(pbrIndex);
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getAnrCountHw() {
        return this.mUsimPhoneBookManager.getAnrCount();
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getEmailCountHw() {
        return this.mUsimPhoneBookManager.getEmailCount();
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getSpareAnrCountHw() {
        return this.mUsimPhoneBookManager.getSpareAnrCount();
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getSpareEmailCountHw() {
        return this.mUsimPhoneBookManager.getSpareEmailCount();
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public ArrayList<AdnRecordExt> getAdnFilesForSim() {
        return AdnRecordExt.convertAdnRecordToExt(this.mAdnLikeFiles.get(28474));
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getSpareExt1CountHw() {
        if (this.mUsimPhoneBookManager.getExt1Count() > 0) {
            return this.mUsimPhoneBookManager.getSpareExt1Count();
        }
        return -1;
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getUsimAdnCountHw() {
        return this.mUsimPhoneBookManager.getUsimAdnCount();
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int[] getRecordsSizeHw() {
        return this.mIHwAdnRecordCacheEx.getRecordsSizeHw();
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public IUsimPhoneBookManagerInner getUsimPhoneBookManager() {
        return this.mUsimPhoneBookManager;
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getAdnRecordsFreeSize() {
        return this.mUsimPhoneBookManager.getAdnRecordsFreeSize();
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public int getAdnCountHw() {
        return this.mIHwAdnRecordCacheEx.getAdnCountHw();
    }

    @Override // com.android.internal.telephony.uicc.IAdnRecordCacheInner
    public void updateAdnBySearch(int efid, AdnRecordExt oldAdnRecordExt, AdnRecordExt newAdnRecordExt, String pin2, Message response) {
        if (oldAdnRecordExt != null && newAdnRecordExt != null) {
            updateAdnBySearch(efid, (AdnRecord) oldAdnRecordExt.getAdnRecord(), (AdnRecord) newAdnRecordExt.getAdnRecord(), pin2, response);
        }
    }
}
