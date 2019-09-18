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
            switch (msg.what) {
                case 1:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    byte[] data = (byte[]) ar.result;
                    if (ar.exception == null) {
                        AdnRecord adn = new AdnRecord(this.mEf, this.mRecordNumber, data);
                        this.mResult = adn;
                        if (adn.hasExtendedRecord()) {
                            this.mPendingExtLoads = 1;
                            this.mFh.loadEFLinearFixed(this.mExtensionEF, adn.mExtRecord, obtainMessage(2, adn));
                            break;
                        }
                    } else {
                        throw new RuntimeException("load failed", ar.exception);
                    }
                    break;
                case 2:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    byte[] data2 = (byte[]) ar2.result;
                    AdnRecord adn2 = (AdnRecord) ar2.userObj;
                    if (ar2.exception == null) {
                        Rlog.d(LOG_TAG, "ADN extension EF: 0x" + Integer.toHexString(this.mExtensionEF) + ":" + adn2.mExtRecord + "\n" + IccUtils.bytesToHexString(data2));
                        adn2.appendExtRecord(data2);
                    } else {
                        Rlog.e(LOG_TAG, "Failed to read ext record. Do Not clear the whole number.");
                    }
                    this.mPendingExtLoads--;
                    break;
                case 3:
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    ArrayList<byte[]> datas = (ArrayList) ar3.result;
                    if (ar3.exception == null) {
                        this.mAdns = new ArrayList<>(datas.size());
                        this.mResult = this.mAdns;
                        this.mPendingExtLoads = 0;
                        int s = datas.size();
                        for (int i = 0; i < s; i++) {
                            AdnRecord adn3 = new AdnRecord(this.mEf, 1 + i, datas.get(i));
                            this.mAdns.add(adn3);
                            if (adn3.hasExtendedRecord()) {
                                this.mPendingExtLoads++;
                                this.mFh.loadEFLinearFixed(this.mExtensionEF, adn3.mExtRecord, obtainMessage(2, adn3));
                            }
                        }
                        break;
                    } else {
                        throw new RuntimeException("load failed", ar3.exception);
                    }
                case 4:
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    AdnRecord adn4 = (AdnRecord) ar4.userObj;
                    if (ar4.exception == null) {
                        int[] recordSize = (int[]) ar4.result;
                        if (recordSize.length != 3 || this.mRecordNumber > recordSize[2]) {
                            throw new RuntimeException("get wrong EF record size format", ar4.exception);
                        }
                        byte[] data3 = adn4.buildAdnString(recordSize[0]);
                        if (!(adn4.mExtRecord == 255 || data3 == null || data3.length <= 0)) {
                            data3[data3.length - 1] = (byte) adn4.mExtRecord;
                        }
                        if (data3 != null) {
                            this.mFh.updateEFLinearFixed(this.mEf, getEFPath(this.mEf), this.mRecordNumber, data3, this.mPin2, obtainMessage(5));
                            this.mPendingExtLoads = 1;
                            break;
                        } else {
                            throw new RuntimeException("wrong ADN format", ar4.exception);
                        }
                    } else {
                        throw new RuntimeException("get EF record size failed", ar4.exception);
                    }
                    break;
                case 5:
                    AsyncResult ar5 = (AsyncResult) msg.obj;
                    if (ar5.exception == null) {
                        this.mPendingExtLoads = 0;
                        this.mResult = null;
                        break;
                    } else {
                        throw new RuntimeException("update EF adn record failed", ar5.exception);
                    }
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
