package com.android.internal.telephony.gsm;

import android.os.Handler;
import android.os.Message;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.telephony.RlogEx;

public class HwUsimPhoneBookManager extends Handler implements IHwUsimPhoneBookManagerEx {
    private static final boolean DBG = true;
    protected static final int EVENT_GET_SIZE_DONE = 101;
    private static final String LOG_TAG = "HwUsimPhoneBookManager";
    private static final int USIM_EFADN_TAG = 192;
    private final Object mLock;
    private IUsimPhoneBookManagerInner mUsimPhoneBookManagerInner;
    private int[] recordSize = new int[3];
    private int[] temRecordSize = new int[3];

    public HwUsimPhoneBookManager(IUsimPhoneBookManagerInner iUsimPhoneBookManagerInner, Object lock) {
        this.mUsimPhoneBookManagerInner = iUsimPhoneBookManagerInner;
        this.mLock = lock;
    }

    public int[] getAdnRecordsSizeFromEFHw() {
        synchronized (this.mLock) {
            if (!this.mUsimPhoneBookManagerInner.getIsPbrPresent()) {
                return null;
            }
            if (this.mUsimPhoneBookManagerInner.getPbrRecordsSize() == -1) {
                this.mUsimPhoneBookManagerInner.readPbrFileAndWaitHw();
            }
            int numRecs = this.mUsimPhoneBookManagerInner.getPbrRecordsSize();
            if (numRecs == -1) {
                return null;
            }
            this.temRecordSize[0] = 0;
            this.temRecordSize[1] = 0;
            this.temRecordSize[2] = 0;
            for (int i = 0; i < numRecs; i++) {
                this.recordSize[0] = 0;
                this.recordSize[1] = 0;
                this.recordSize[2] = 0;
                getAdnRecordsSizeAndWait(i);
                log("getAdnRecordsSizeFromEFHw: recordSize[2]=" + this.recordSize[2]);
                if (this.recordSize[0] != 0) {
                    this.temRecordSize[0] = this.recordSize[0];
                }
                if (this.recordSize[1] != 0) {
                    this.temRecordSize[1] = this.recordSize[1];
                }
                this.temRecordSize[2] = this.recordSize[2] + this.temRecordSize[2];
            }
            log("getAdnRecordsSizeFromEFHw: temRecordSize[2]=" + this.temRecordSize[2]);
            return this.temRecordSize;
        }
    }

    private void getAdnRecordsSizeAndWait(int recNum) {
        int efid = this.mUsimPhoneBookManagerInner.getEFidInPBRForEx(recNum, (int) USIM_EFADN_TAG);
        RlogEx.d(LOG_TAG, "getAdnRecordsSize: efid=" + efid);
        if (efid != 0) {
            this.mUsimPhoneBookManagerInner.getIccFileHandler().getEFLinearRecordSize(efid, obtainMessage(EVENT_GET_SIZE_DONE));
            for (boolean isWait = true; isWait; isWait = false) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    RlogEx.e(LOG_TAG, "Interrupted Exception in getAdnRecordsSizeAndWait");
                    return;
                }
            }
        }
    }

    public int getPbrFileSizeHw() {
        int size = 0;
        if (this.mUsimPhoneBookManagerInner.getPbrRecordsSize() != -1) {
            size = this.mUsimPhoneBookManagerInner.getPbrRecordsSize();
        }
        log("getPbrFileSize:" + size);
        return size;
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        if (msg.what != EVENT_GET_SIZE_DONE) {
            super.handleMessage(msg);
            return;
        }
        AsyncResultEx asyncResultEx = AsyncResultEx.from(msg.obj);
        synchronized (this.mLock) {
            if (asyncResultEx != null) {
                if (asyncResultEx.getException() == null) {
                    this.recordSize = (int[]) asyncResultEx.getResult();
                    log("GET_RECORD_SIZE Size " + this.recordSize[0] + " total " + this.recordSize[1] + " #record " + this.recordSize[2]);
                }
            }
            this.mLock.notify();
        }
    }

    public int getEFidInPBRHw(int recNum, int tag) {
        return this.mUsimPhoneBookManagerInner.getEFidInPBRForEx(recNum, tag);
    }

    private void log(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }
}
