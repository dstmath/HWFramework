package com.android.internal.telephony.uicc;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import java.util.ArrayList;

public class AdnRecordLoader extends Handler {
    static final int EVENT_ADN_LOAD_ALL_DONE = 3;
    static final int EVENT_ADN_LOAD_DONE = 1;
    static final int EVENT_EF_LINEAR_RECORD_SIZE_DONE = 4;
    static final int EVENT_EXT_RECORD_LOAD_DONE = 2;
    static final int EVENT_UPDATE_RECORD_DONE = 5;
    static final String LOG_TAG = "AdnRecordLoader";
    static final boolean VDBG = false;
    ArrayList<AdnRecord> mAdns;
    int mEf;
    int mExtensionEF;
    private IccFileHandler mFh;
    int mPendingExtLoads;
    String mPin2;
    int mRecordNumber;
    Object mResult;
    Message mUserResponse;

    AdnRecordLoader(IccFileHandler fh) {
        super(Looper.getMainLooper());
        this.mFh = fh;
    }

    private String getEFPath(int efid) {
        if (efid == 28474) {
            return "3F007F10";
        }
        return null;
    }

    public void loadFromEF(int ef, int extensionEF, int recordNumber, Message response) {
        this.mEf = ef;
        this.mExtensionEF = extensionEF;
        this.mRecordNumber = recordNumber;
        this.mUserResponse = response;
        this.mFh.loadEFLinearFixed(ef, getEFPath(ef), recordNumber, obtainMessage(1));
    }

    public void loadAllFromEF(int ef, int extensionEF, Message response) {
        this.mEf = ef;
        this.mExtensionEF = extensionEF;
        this.mUserResponse = response;
        this.mFh.loadEFLinearFixedAll(ef, getEFPath(ef), obtainMessage(3));
    }

    public void updateEF(AdnRecord adn, int ef, int extensionEF, int recordNumber, String pin2, Message response) {
        this.mEf = ef;
        this.mExtensionEF = extensionEF;
        this.mRecordNumber = recordNumber;
        this.mUserResponse = response;
        this.mPin2 = pin2;
        this.mFh.getEFLinearRecordSize(ef, getEFPath(ef), obtainMessage(4, adn));
    }

    public void handleMessage(Message msg) {
        try {
            AsyncResult ar;
            byte[] data;
            AdnRecord adn;
            switch (msg.what) {
                case 1:
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        adn = new AdnRecord(this.mEf, this.mRecordNumber, data);
                        this.mResult = adn;
                        if (adn.hasExtendedRecord()) {
                            this.mPendingExtLoads = 1;
                            this.mFh.loadEFLinearFixed(this.mExtensionEF, adn.mExtRecord, obtainMessage(2, adn));
                            break;
                        }
                    }
                    throw new RuntimeException("load failed", ar.exception);
                    break;
                case 2:
                    ar = (AsyncResult) msg.obj;
                    data = (byte[]) ar.result;
                    adn = (AdnRecord) ar.userObj;
                    if (ar.exception == null) {
                        Rlog.d(LOG_TAG, "ADN extension EF: 0x" + Integer.toHexString(this.mExtensionEF) + ":" + adn.mExtRecord + "\n" + IccUtils.bytesToHexString(data));
                        adn.appendExtRecord(data);
                    } else {
                        Rlog.e(LOG_TAG, "Failed to read ext record. Do Not clear the whole number.");
                    }
                    this.mPendingExtLoads--;
                    break;
                case 3:
                    ar = (AsyncResult) msg.obj;
                    ArrayList<byte[]> datas = ar.result;
                    if (ar.exception == null) {
                        this.mAdns = new ArrayList(datas.size());
                        this.mResult = this.mAdns;
                        this.mPendingExtLoads = 0;
                        int s = datas.size();
                        for (int i = 0; i < s; i++) {
                            adn = new AdnRecord(this.mEf, i + 1, (byte[]) datas.get(i));
                            this.mAdns.add(adn);
                            if (adn.hasExtendedRecord()) {
                                this.mPendingExtLoads++;
                                this.mFh.loadEFLinearFixed(this.mExtensionEF, adn.mExtRecord, obtainMessage(2, adn));
                            }
                        }
                        break;
                    }
                    throw new RuntimeException("load failed", ar.exception);
                case 4:
                    ar = msg.obj;
                    adn = ar.userObj;
                    if (ar.exception == null) {
                        int[] recordSize = ar.result;
                        if (recordSize.length == 3 && this.mRecordNumber <= recordSize[2]) {
                            data = adn.buildAdnString(recordSize[0]);
                            if (!(adn.mExtRecord == 255 || data == null || data.length <= 0)) {
                                data[data.length - 1] = (byte) adn.mExtRecord;
                            }
                            if (data != null) {
                                this.mFh.updateEFLinearFixed(this.mEf, getEFPath(this.mEf), this.mRecordNumber, data, this.mPin2, obtainMessage(5));
                                this.mPendingExtLoads = 1;
                                break;
                            }
                            throw new RuntimeException("wrong ADN format", ar.exception);
                        }
                        throw new RuntimeException("get wrong EF record size format", ar.exception);
                    }
                    throw new RuntimeException("get EF record size failed", ar.exception);
                case 5:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        this.mPendingExtLoads = 0;
                        this.mResult = null;
                        break;
                    }
                    throw new RuntimeException("update EF adn record failed", ar.exception);
            }
            if (this.mUserResponse != null && this.mPendingExtLoads == 0) {
                AsyncResult.forMessage(this.mUserResponse).result = this.mResult;
                this.mUserResponse.sendToTarget();
                this.mUserResponse = null;
            }
        } catch (RuntimeException exc) {
            if (this.mUserResponse != null) {
                AsyncResult.forMessage(this.mUserResponse).exception = exc;
                this.mUserResponse.sendToTarget();
                this.mUserResponse = null;
            }
        }
    }
}
