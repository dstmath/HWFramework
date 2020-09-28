package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import android.util.SparseArray;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;
import com.android.internal.telephony.uicc.AdnRecord;
import com.android.internal.telephony.uicc.AdnRecordCache;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.ArrayList;

public class HwUsimPhoneBookManager extends UsimPhoneBookManager {
    private static final boolean DBG = true;
    protected static final int EVENT_GET_SIZE_DONE = 101;
    private static final String LOG_TAG = "HwUsimPhoneBookManager";
    private static final int USIM_EFADN_TAG = 192;
    private static UsimPhoneBookManagerUtils usimPhoneBookManagerUtils = EasyInvokeFactory.getInvokeUtils(UsimPhoneBookManagerUtils.class);
    private int[] recordSize;
    private int[] temRecordSize;

    public HwUsimPhoneBookManager(IccFileHandler fh) {
        super(fh, (AdnRecordCache) null);
        this.recordSize = new int[3];
        this.temRecordSize = new int[3];
        usimPhoneBookManagerUtils.setFh(this, fh);
        usimPhoneBookManagerUtils.setPhoneBookRecords(this, new ArrayList<>());
        usimPhoneBookManagerUtils.setPbrRecords(this, null);
        usimPhoneBookManagerUtils.setIsPbrPresent(this, true);
    }

    public HwUsimPhoneBookManager(IccFileHandler fh, AdnRecordCache cache) {
        super(fh, cache);
        this.recordSize = new int[3];
        this.temRecordSize = new int[3];
    }

    public ArrayList<AdnRecord> getPhonebookRecords() {
        if (!usimPhoneBookManagerUtils.getPhoneBookRecords(this).isEmpty()) {
            return usimPhoneBookManagerUtils.getPhoneBookRecords(this);
        }
        return null;
    }

    public void setIccFileHandler(IccFileHandler fh) {
        usimPhoneBookManagerUtils.setFh(this, fh);
    }

    public int[] getAdnRecordsSizeFromEF() {
        synchronized (usimPhoneBookManagerUtils.getLockObject(this)) {
            if (!usimPhoneBookManagerUtils.getIsPbrPresent(this)) {
                return null;
            }
            if (usimPhoneBookManagerUtils.getPbrRecords(this) == null) {
                usimPhoneBookManagerUtils.readPbrFileAndWait(this);
            }
            if (usimPhoneBookManagerUtils.getPbrRecords(this) == null) {
                return null;
            }
            int numRecs = usimPhoneBookManagerUtils.getPbrRecords(this).size();
            this.temRecordSize[0] = 0;
            this.temRecordSize[1] = 0;
            this.temRecordSize[2] = 0;
            for (int i = 0; i < numRecs; i++) {
                this.recordSize[0] = 0;
                this.recordSize[1] = 0;
                this.recordSize[2] = 0;
                getAdnRecordsSizeAndWait(i);
                Rlog.d(LOG_TAG, "getAdnRecordsSizeFromEF: recordSize[2]=" + this.recordSize[2]);
                if (this.recordSize[0] != 0) {
                    this.temRecordSize[0] = this.recordSize[0];
                }
                if (this.recordSize[1] != 0) {
                    this.temRecordSize[1] = this.recordSize[1];
                }
                this.temRecordSize[2] = this.recordSize[2] + this.temRecordSize[2];
            }
            Rlog.d(LOG_TAG, "getAdnRecordsSizeFromEF: temRecordSize[2]=" + this.temRecordSize[2]);
            return this.temRecordSize;
        }
    }

    public void getAdnRecordsSizeAndWait(int recNum) {
        SparseArray<UsimPhoneBookManager.File> files;
        if (!(usimPhoneBookManagerUtils.getPbrRecords(this) == null || (files = usimPhoneBookManagerUtils.getPbrRecords(this).get(recNum).getFileIdHw()) == null || files.size() == 0 || files.get(USIM_EFADN_TAG) == null)) {
            int efid = files.get(USIM_EFADN_TAG).getEfid();
            Rlog.d(LOG_TAG, "getAdnRecordsSize: efid=" + efid);
            usimPhoneBookManagerUtils.getFh(this).getEFLinearRecordSize(efid, obtainMessage(EVENT_GET_SIZE_DONE));
            for (boolean isWait = true; isWait; isWait = false) {
                try {
                    usimPhoneBookManagerUtils.getLockObject(this).wait();
                } catch (InterruptedException e) {
                    Rlog.e(LOG_TAG, "Interrupted Exception in getAdnRecordsSizeAndWait");
                    return;
                }
            }
        }
    }

    public int getPbrFileSize() {
        int size = 0;
        if (usimPhoneBookManagerUtils.getPbrRecords(this) != null) {
            size = usimPhoneBookManagerUtils.getPbrRecords(this).size();
        }
        log("getPbrFileSize:" + size);
        return size;
    }

    public int getEFidInPBR(int recNum, int tag) {
        SparseArray<UsimPhoneBookManager.File> files;
        int efid = 0;
        if (usimPhoneBookManagerUtils.getPbrRecords(this) == null || (files = usimPhoneBookManagerUtils.getPbrRecords(this).get(recNum).getFileIdHw()) == null || files.size() == 0) {
            return 0;
        }
        if (files.get(tag) != null) {
            efid = files.get(tag).getEfid();
        }
        log("getEFidInPBR, efid = " + efid + ", recNum = " + recNum + ", tag = " + tag);
        return efid;
    }

    public void handleMessage(Message msg) {
        if (msg.what != EVENT_GET_SIZE_DONE) {
            HwUsimPhoneBookManager.super.handleMessage(msg);
            return;
        }
        AsyncResult ar = (AsyncResult) msg.obj;
        synchronized (usimPhoneBookManagerUtils.getLockObject(this)) {
            if (ar.exception == null) {
                this.recordSize = (int[]) ar.result;
                log("GET_RECORD_SIZE Size " + this.recordSize[0] + " total " + this.recordSize[1] + " #record " + this.recordSize[2]);
            }
            usimPhoneBookManagerUtils.getLockObject(this).notify();
        }
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }
}
