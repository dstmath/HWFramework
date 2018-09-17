package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import android.util.SparseArray;
import com.android.internal.telephony.gsm.UsimPhoneBookManager.File;
import com.android.internal.telephony.gsm.UsimPhoneBookManager.PbrRecord;
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
    private static UsimPhoneBookManagerUtils usimPhoneBookManagerUtils = ((UsimPhoneBookManagerUtils) EasyInvokeFactory.getInvokeUtils(UsimPhoneBookManagerUtils.class));
    private int[] recordSize;
    private int[] temRecordSize;

    public HwUsimPhoneBookManager(IccFileHandler fh) {
        super(fh, null);
        this.recordSize = new int[3];
        this.temRecordSize = new int[3];
        usimPhoneBookManagerUtils.setFh(this, fh);
        usimPhoneBookManagerUtils.setPhoneBookRecords(this, new ArrayList());
        usimPhoneBookManagerUtils.setPbrRecords(this, null);
        usimPhoneBookManagerUtils.setIsPbrPresent(this, true);
    }

    public HwUsimPhoneBookManager(IccFileHandler fh, AdnRecordCache cache) {
        super(fh, cache);
        this.recordSize = new int[3];
        this.temRecordSize = new int[3];
    }

    public ArrayList<AdnRecord> getPhonebookRecords() {
        if (usimPhoneBookManagerUtils.getPhoneBookRecords(this).isEmpty()) {
            return null;
        }
        return usimPhoneBookManagerUtils.getPhoneBookRecords(this);
    }

    public void setIccFileHandler(IccFileHandler fh) {
        usimPhoneBookManagerUtils.setFh(this, fh);
    }

    public int[] getAdnRecordsSizeFromEF() {
        synchronized (usimPhoneBookManagerUtils.getLockObject(this)) {
            if (usimPhoneBookManagerUtils.getIsPbrPresent(this)) {
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
                int[] iArr = this.temRecordSize;
                return iArr;
            }
            return null;
        }
    }

    /* JADX WARNING: Missing block: B:7:0x0022, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getAdnRecordsSizeAndWait(int recNum) {
        if (usimPhoneBookManagerUtils.getPbrRecords(this) != null) {
            SparseArray<File> files = ((PbrRecord) usimPhoneBookManagerUtils.getPbrRecords(this).get(recNum)).mFileIds;
            if (files != null && files.size() != 0 && files.get(USIM_EFADN_TAG) != null) {
                int efid = ((File) files.get(USIM_EFADN_TAG)).getEfid();
                Rlog.d(LOG_TAG, "getAdnRecordsSize: efid=" + efid);
                usimPhoneBookManagerUtils.getFh(this).getEFLinearRecordSize(efid, obtainMessage(EVENT_GET_SIZE_DONE));
                boolean isWait = true;
                while (isWait) {
                    try {
                        usimPhoneBookManagerUtils.getLockObject(this).wait();
                        isWait = false;
                    } catch (InterruptedException e) {
                        Rlog.e(LOG_TAG, "Interrupted Exception in getAdnRecordsSizeAndWait");
                    }
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
        int efid = 0;
        if (usimPhoneBookManagerUtils.getPbrRecords(this) == null) {
            return 0;
        }
        SparseArray<File> files = ((PbrRecord) usimPhoneBookManagerUtils.getPbrRecords(this).get(recNum)).mFileIds;
        if (files == null || files.size() == 0) {
            return 0;
        }
        if (files.get(tag) != null) {
            efid = ((File) files.get(tag)).getEfid();
        }
        log("getEFidInPBR, efid = " + efid + ", recNum = " + recNum + ", tag = " + tag);
        return efid;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_GET_SIZE_DONE /*101*/:
                AsyncResult ar = msg.obj;
                synchronized (usimPhoneBookManagerUtils.getLockObject(this)) {
                    if (ar.exception == null) {
                        this.recordSize = (int[]) ar.result;
                        log("GET_RECORD_SIZE Size " + this.recordSize[0] + " total " + this.recordSize[1] + " #record " + this.recordSize[2]);
                    }
                    usimPhoneBookManagerUtils.getLockObject(this).notify();
                }
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }
}
