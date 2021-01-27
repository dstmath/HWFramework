package com.android.internal.telephony.uicc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.huawei.android.os.AsyncResultEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.IccIoResultExt;
import com.huawei.internal.telephony.uicc.IccUtilsEx;
import java.util.ArrayList;
import java.util.HashSet;

public class HwIccFileHandlerReference extends Handler implements IHwIccFileHandlerEx {
    protected static final int BYTE_BIT_NUM = 8;
    protected static final int BYTE_MAX_NUM = 255;
    protected static final int COMMAND_GET_RESPONSE = 192;
    protected static final int COMMAND_READ_BINARY = 176;
    protected static final int COMMAND_READ_RECORD = 178;
    protected static final int COMMAND_SEEK = 162;
    protected static final int COMMAND_UPDATE_BINARY = 214;
    protected static final int COMMAND_UPDATE_RECORD = 220;
    private static final String CUSTOM_FILE_PATH = "file_path";
    private static final String CUSTOM_FOR_APP = "efid_app";
    protected static final int EF_TYPE_CYCLIC = 3;
    protected static final int EF_TYPE_LINEAR_FIXED = 1;
    protected static final int EF_TYPE_TRANSPARENT = 0;
    protected static final int EMPTY_RECORD_DATA = 255;
    protected static final int EVENT_GET_CUSTOM_BINARY_SIZE_DONE = 1200;
    private static final int EVENT_GET_RECORD_SIZE_DONE = 1203;
    static final int EVENT_GET_SMSC_ADDR_DONE = 1100;
    protected static final int EVENT_READ_CUSTOM_BINARY_DONE = 1201;
    private static final int EVENT_READ_RECORD_DONE = 1204;
    protected static final int EVENT_SEARCH_MATCHED_RECORD_DONE = 1202;
    static final int EVENT_SET_SMSC_ADDR_DONE = 1101;
    protected static final int GET_RESPONSE_EF_IMG_SIZE_BYTES = 10;
    protected static final int GET_RESPONSE_EF_SIZE_BYTES = 15;
    private static final String LOG_TAG = "HwIccFileHandlerReference";
    protected static final int READ_RECORD_MODE_ABSOLUTE = 4;
    protected static final int RESPONSE_DATA_ACCESS_CONDITION_1 = 8;
    protected static final int RESPONSE_DATA_ACCESS_CONDITION_2 = 9;
    protected static final int RESPONSE_DATA_ACCESS_CONDITION_3 = 10;
    protected static final int RESPONSE_DATA_FILE_ID_1 = 4;
    protected static final int RESPONSE_DATA_FILE_ID_2 = 5;
    protected static final int RESPONSE_DATA_FILE_SIZE_1 = 2;
    protected static final int RESPONSE_DATA_FILE_SIZE_2 = 3;
    protected static final int RESPONSE_DATA_FILE_STATUS = 11;
    protected static final int RESPONSE_DATA_FILE_TYPE = 6;
    protected static final int RESPONSE_DATA_LENGTH = 12;
    protected static final int RESPONSE_DATA_RECORD_LENGTH = 14;
    protected static final int RESPONSE_DATA_RFU_1 = 0;
    protected static final int RESPONSE_DATA_RFU_2 = 1;
    protected static final int RESPONSE_DATA_RFU_3 = 7;
    protected static final int RESPONSE_DATA_STRUCTURE = 13;
    protected static final int SEARCH_RECORD_MODE_P1 = 1;
    protected static final int SEARCH_RECORD_MODE_P2 = 4;
    protected static final int TYPE_DF = 2;
    protected static final int TYPE_EF = 4;
    protected static final int TYPE_MF = 1;
    protected static final int TYPE_RFU = 0;
    private static final boolean VDBG = false;
    private CommandsInterfaceEx commandsInterfaceEx;
    private Handler efPartReaderHandler = new EfPartReaderHandler();
    private Handler loadExcludeEmptyHandler = new LoadLinearExcludeEmptyHandler();
    private String mAid;
    private IIccFileHandlerInner mFh;
    private Handler vnReaderHandler = new VnReaderHandler();

    public HwIccFileHandlerReference(IIccFileHandlerInner fileHandler, String aid, CommandsInterfaceEx ci) {
        this.mFh = fileHandler;
        this.mAid = aid;
        this.commandsInterfaceEx = ci;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendResult(Message response, Object result, Throwable ex) {
        if (response != null) {
            AsyncResultEx.forMessage(response, result, ex);
            response.sendToTarget();
        }
    }

    public void loadEFTransparent(String filePath, int fileid, Message onLoaded) {
        Message response = this.vnReaderHandler.obtainMessage(EVENT_GET_CUSTOM_BINARY_SIZE_DONE, fileid, 0, onLoaded);
        Bundle data = new Bundle();
        data.putString(CUSTOM_FILE_PATH, filePath);
        response.setData(data);
        this.commandsInterfaceEx.iccIO((int) COMMAND_GET_RESPONSE, fileid, filePath, 0, 0, 15, (String) null, (String) null, response);
    }

    public void loadEFTransparent(String filePath, int fileid, Message onLoaded, boolean isForApp) {
        Message response = this.vnReaderHandler.obtainMessage(EVENT_GET_CUSTOM_BINARY_SIZE_DONE, fileid, 0, onLoaded);
        Bundle data = new Bundle();
        data.putString(CUSTOM_FILE_PATH, filePath);
        data.putBoolean(CUSTOM_FOR_APP, isForApp);
        response.setData(data);
        log("loadEFTransparent isForApp: " + isForApp);
        this.commandsInterfaceEx.iccIOForApp((int) COMMAND_GET_RESPONSE, fileid, filePath, 0, 0, 15, (String) null, (String) null, this.mAid, response);
    }

    public void getSmscAddress(Message result) {
        this.commandsInterfaceEx.getSmscAddress(obtainMessage(EVENT_GET_SMSC_ADDR_DONE, result));
    }

    public void setSmscAddress(String address, Message result) {
        this.commandsInterfaceEx.setSmscAddress(address, obtainMessage(EVENT_SET_SMSC_ADDR_DONE, result));
    }

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == EVENT_GET_SMSC_ADDR_DONE || i == EVENT_SET_SMSC_ADDR_DONE) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            if (ar != null) {
                Message response = (Message) ar.getUserObj();
                AsyncResultEx.forMessage(response).setException(ar.getException());
                AsyncResultEx asyncResultEx = AsyncResultEx.from(response.obj);
                if (asyncResultEx != null) {
                    asyncResultEx.setResult(ar.getResult());
                }
                response.sendToTarget();
                return;
            }
            return;
        }
        log("handleMessage invalid message " + msg.what);
    }

    public void loadEFLinearFixedPartHW(int fileid, ArrayList<Integer> recordNums, Message onLoaded) {
        this.commandsInterfaceEx.iccIOForApp((int) COMMAND_GET_RESPONSE, fileid, this.mFh.getEFPathHw(fileid), 0, 0, 15, (String) null, (String) null, this.mAid, this.efPartReaderHandler.obtainMessage(EVENT_GET_RECORD_SIZE_DONE, new LoadLinearFixedContext(fileid, recordNums, this.mFh.getEFPathHw(fileid), onLoaded)));
    }

    public void loadEFLinearFixedAllExcludeEmpty(int fileid, Message onLoaded) {
        log("loadEFLinearFixedAllExcludeEmpty.");
        String efPath = this.mFh.getEFPathHw(fileid);
        this.commandsInterfaceEx.iccIOForApp((int) COMMAND_GET_RESPONSE, fileid, efPath, 0, 0, 15, (String) null, (String) null, this.mAid, this.loadExcludeEmptyHandler.obtainMessage(EVENT_GET_RECORD_SIZE_DONE, new LoadLinearFixedContext(fileid, efPath, onLoaded)));
    }

    private byte[] getEmptyPayload(int recordSize) {
        byte[] matchData = new byte[recordSize];
        for (int i = 0; i < recordSize; i++) {
            matchData[i] = -1;
        }
        return matchData;
    }

    private boolean processException(Message response, AsyncResultEx ar) {
        IccIoResultExt result = new IccIoResultExt();
        result.setIccIoResult(ar.getResult());
        if (ar.getException() != null) {
            sendResult(response, null, ar.getException());
            loge("ar.exception");
            return true;
        } else if (result.getException() == null) {
            return false;
        } else {
            sendResult(response, null, result.getException());
            loge("iccException");
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetRecordSizeDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null) {
            LoadLinearFixedContext lc = (LoadLinearFixedContext) ar.getUserObj();
            IccIoResultExt result = new IccIoResultExt();
            result.setIccIoResult(ar.getResult());
            if (!processException(lc.mOnLoaded, ar)) {
                byte[] data = result.getPayload();
                if (data.length > RESPONSE_DATA_RECORD_LENGTH && 4 == data[6] && 1 == data[RESPONSE_DATA_STRUCTURE]) {
                    lc.mRecordSize = data[RESPONSE_DATA_RECORD_LENGTH] & 255;
                    lc.mCountRecords = (((data[2] & 255) << 8) + (data[3] & 255)) / lc.mRecordSize;
                    log("EVENT_GET_RECORD_SIZE_DONE : ." + lc.mCountRecords + "load all " + lc.mLoadAll);
                    String path = lc.mPath;
                    if (path == null) {
                        path = this.mFh.getEFPathHw(lc.mEfid);
                    }
                    if (lc.mLoadAll) {
                        byte[] matchData = getEmptyPayload(lc.mRecordSize);
                        this.commandsInterfaceEx.iccIOForApp((int) COMMAND_SEEK, lc.mEfid, path, 1, 4, matchData.length, IccUtilsEx.bytesToHexString(matchData), (String) null, this.mAid, this.loadExcludeEmptyHandler.obtainMessage(EVENT_SEARCH_MATCHED_RECORD_DONE, lc));
                        return;
                    }
                    this.commandsInterfaceEx.iccIOForApp((int) COMMAND_READ_RECORD, lc.mEfid, path, lc.mRecordNum, 4, lc.mRecordSize, (String) null, (String) null, this.mAid, this.loadExcludeEmptyHandler.obtainMessage(EVENT_READ_RECORD_DONE, lc));
                    return;
                }
                sendResult(lc.mOnLoaded, null, new Exception("IccFileTypeMismatch error"));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSearchedMatchedRecordDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null) {
            LoadLinearFixedContext lc = (LoadLinearFixedContext) ar.getUserObj();
            IccIoResultExt result = new IccIoResultExt();
            result.setIccIoResult(ar.getResult());
            log("handleSearchedMatchedRecordDone :lc.mCountRecords" + lc.mCountRecords);
            byte[] data = null;
            int emptyLen = 0;
            if (ar.getException() == null && result.getException() == null) {
                data = result.getPayload();
                emptyLen = data == null ? 0 : data.length;
            }
            HashSet<Integer> emptyRecord = new HashSet<>(emptyLen);
            for (int i = 0; i < emptyLen; i++) {
                emptyRecord.add(Integer.valueOf(data[i] & 255));
            }
            ArrayList<Integer> recordNums = new ArrayList<>();
            for (int i2 = 1; i2 <= lc.mCountRecords; i2++) {
                if (!emptyRecord.contains(Integer.valueOf(i2))) {
                    recordNums.add(Integer.valueOf(i2));
                }
            }
            if (recordNums.size() > 0) {
                loadEFLinearFixedPartHW(lc.mEfid, recordNums, lc.mOnLoaded);
                return;
            }
            lc.results = new ArrayList<>(lc.mCountRecords);
            byte[] emptyData = getEmptyPayload(lc.mRecordSize);
            for (int i3 = 0; i3 < lc.mCountRecords; i3++) {
                lc.results.add(emptyData);
            }
            sendResult(lc.mOnLoaded, lc.results, null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGetRecordDone(Message msg) {
        AsyncResultEx ar = AsyncResultEx.from(msg.obj);
        if (ar != null) {
            LoadLinearFixedContext lc = (LoadLinearFixedContext) ar.getUserObj();
            IccIoResultExt result = new IccIoResultExt();
            result.setIccIoResult(ar.getResult());
            Message response = lc.mOnLoaded;
            log("EVENT_READ_RECORD_DONE :lc.mRecordNum" + lc.mRecordNum + "load all " + lc.mLoadAll);
            if (!processException(response, ar)) {
                sendResult(response, result.getPayload(), null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        RlogEx.i(LOG_TAG, s);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String s) {
        RlogEx.e(LOG_TAG, s);
    }

    /* access modifiers changed from: package-private */
    public static class LoadLinearFixedContext {
        int mCount;
        int mCountLoadrecords;
        int mCountRecords;
        int mEfid;
        boolean mLoadAll;
        boolean mLoadPart;
        Message mOnLoaded;
        String mPath;
        int mRecordNum;
        ArrayList<Integer> mRecordNums;
        int mRecordSize;
        ArrayList<byte[]> results;

        LoadLinearFixedContext(int efid, int recordNum, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = recordNum;
            this.mOnLoaded = onLoaded;
            this.mLoadAll = false;
            this.mLoadPart = false;
            this.mPath = null;
        }

        LoadLinearFixedContext(int efid, int recordNum, String path, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = recordNum;
            this.mOnLoaded = onLoaded;
            this.mLoadAll = false;
            this.mLoadPart = false;
            this.mPath = path;
        }

        LoadLinearFixedContext(int efid, String path, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = 1;
            this.mLoadAll = true;
            this.mLoadPart = false;
            this.mOnLoaded = onLoaded;
            this.mPath = path;
        }

        LoadLinearFixedContext(int efid, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = 1;
            this.mLoadAll = true;
            this.mLoadPart = false;
            this.mOnLoaded = onLoaded;
            this.mPath = null;
        }

        LoadLinearFixedContext(int efid, ArrayList<Integer> recordNums, String path, Message onLoaded) {
            this.mEfid = efid;
            this.mLoadAll = false;
            this.mLoadPart = true;
            this.mRecordNums = new ArrayList<>();
            if (recordNums != null) {
                this.mRecordNum = recordNums.get(0).intValue();
                this.mRecordNums.addAll(recordNums);
                this.mCountLoadrecords = recordNums.size();
            }
            this.mCount = 0;
            this.mOnLoaded = onLoaded;
            this.mPath = path;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void initLCResults(int size) {
            this.results = new ArrayList<>(size);
            byte[] data = new byte[this.mRecordSize];
            for (int i = 0; i < this.mRecordSize; i++) {
                data[i] = -1;
            }
            for (int i2 = 0; i2 < size; i2++) {
                this.results.add(data);
            }
        }
    }

    private class VnReaderHandler extends Handler {
        AsyncResultEx ar;
        byte[] data;
        int fileid;
        Message response;
        IccIoResultExt result;
        int size;

        private VnReaderHandler() {
            this.result = new IccIoResultExt();
            this.response = null;
        }

        private void processGetCustomBinarySizeDone(Message msg) throws Exception {
            this.ar = AsyncResultEx.from(msg.obj);
            AsyncResultEx asyncResultEx = this.ar;
            if (asyncResultEx != null) {
                this.response = (Message) asyncResultEx.getUserObj();
                this.result.setIccIoResult(this.ar.getResult());
                if (this.ar.getException() != null) {
                    HwIccFileHandlerReference.this.sendResult(this.response, null, this.ar.getException());
                } else if (this.result.getException() != null) {
                    HwIccFileHandlerReference.this.sendResult(this.response, null, this.result.getException());
                } else {
                    this.data = this.result.getPayload();
                    this.fileid = msg.arg1;
                    String filePath = msg.getData().getString(HwIccFileHandlerReference.CUSTOM_FILE_PATH);
                    boolean isForApp = msg.getData().getBoolean(HwIccFileHandlerReference.CUSTOM_FOR_APP);
                    byte[] bArr = this.data;
                    if (bArr.length <= 6 || 4 != bArr[6]) {
                        throw new Exception("IccFileTypeMismatch file type error");
                    } else if (bArr.length <= HwIccFileHandlerReference.RESPONSE_DATA_STRUCTURE || bArr[HwIccFileHandlerReference.RESPONSE_DATA_STRUCTURE] != 0) {
                        throw new Exception("IccFileTypeMismatch data structure error");
                    } else {
                        this.size = ((bArr[2] & 255) << 8) + (bArr[3] & 255);
                        if (isForApp) {
                            HwIccFileHandlerReference.this.log("loadEFTransparent for App");
                            HwIccFileHandlerReference.this.commandsInterfaceEx.iccIOForApp((int) HwIccFileHandlerReference.COMMAND_READ_BINARY, this.fileid, filePath, 0, 0, this.size, (String) null, (String) null, HwIccFileHandlerReference.this.mAid, obtainMessage(HwIccFileHandlerReference.EVENT_READ_CUSTOM_BINARY_DONE, this.fileid, 0, this.response));
                            return;
                        }
                        CommandsInterfaceEx commandsInterfaceEx = HwIccFileHandlerReference.this.commandsInterfaceEx;
                        int i = this.fileid;
                        commandsInterfaceEx.iccIO((int) HwIccFileHandlerReference.COMMAND_READ_BINARY, i, filePath, 0, 0, this.size, (String) null, (String) null, obtainMessage(HwIccFileHandlerReference.EVENT_READ_CUSTOM_BINARY_DONE, i, 0, this.response));
                    }
                }
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                int i = msg.what;
                if (i == HwIccFileHandlerReference.EVENT_GET_CUSTOM_BINARY_SIZE_DONE) {
                    processGetCustomBinarySizeDone(msg);
                } else if (i != HwIccFileHandlerReference.EVENT_READ_CUSTOM_BINARY_DONE) {
                    HwIccFileHandlerReference hwIccFileHandlerReference = HwIccFileHandlerReference.this;
                    hwIccFileHandlerReference.log("VnReaderHandler invalid message " + msg.what);
                } else {
                    this.ar = AsyncResultEx.from(msg.obj);
                    if (this.ar != null) {
                        this.response = (Message) this.ar.getUserObj();
                        this.result.setIccIoResult(this.ar.getResult());
                        if (this.ar.getException() != null) {
                            HwIccFileHandlerReference.this.sendResult(this.response, null, this.ar.getException());
                        } else if (this.result.getException() != null) {
                            HwIccFileHandlerReference.this.sendResult(this.response, null, this.result.getException());
                        } else {
                            HwIccFileHandlerReference.this.sendResult(this.response, this.result.getPayload(), null);
                        }
                    }
                }
            } catch (Exception exc) {
                Message message = this.response;
                if (message != null) {
                    HwIccFileHandlerReference.this.sendResult(message, null, exc);
                } else {
                    HwIccFileHandlerReference.this.loge("uncaught exception");
                }
            }
        }
    }

    private class EfPartReaderHandler extends Handler {
        AsyncResultEx ar;
        byte[] data;
        LoadLinearFixedContext lc;
        String path;
        Message response;
        IccIoResultExt result;
        int size;

        private EfPartReaderHandler() {
            this.result = new IccIoResultExt();
            this.response = null;
            this.path = null;
        }

        private void processGetRecordSizeDone(Message msg) throws Exception {
            this.ar = AsyncResultEx.from(msg.obj);
            AsyncResultEx asyncResultEx = this.ar;
            if (asyncResultEx != null) {
                this.lc = (LoadLinearFixedContext) asyncResultEx.getUserObj();
                this.result.setIccIoResult(this.ar.getResult());
                this.response = this.lc.mOnLoaded;
                if (this.ar.getException() != null) {
                    HwIccFileHandlerReference.this.sendResult(this.response, null, this.ar.getException());
                } else if (this.result.getException() != null) {
                    HwIccFileHandlerReference.this.sendResult(this.response, null, this.result.getException());
                } else {
                    this.data = this.result.getPayload();
                    byte[] bArr = this.data;
                    if (bArr.length <= 6 || 4 != bArr[6]) {
                        throw new Exception("IccFileTypeMismatch file type error");
                    } else if (bArr.length <= HwIccFileHandlerReference.RESPONSE_DATA_RECORD_LENGTH || 1 != bArr[HwIccFileHandlerReference.RESPONSE_DATA_STRUCTURE]) {
                        throw new Exception("IccFileTypeMismatch data structure error");
                    } else {
                        LoadLinearFixedContext loadLinearFixedContext = this.lc;
                        loadLinearFixedContext.mRecordSize = bArr[HwIccFileHandlerReference.RESPONSE_DATA_RECORD_LENGTH] & 255;
                        this.size = ((bArr[2] & 255) << 8) + (bArr[3] & 255);
                        loadLinearFixedContext.mCountRecords = this.size / loadLinearFixedContext.mRecordSize;
                        if (this.lc.mLoadAll) {
                            LoadLinearFixedContext loadLinearFixedContext2 = this.lc;
                            loadLinearFixedContext2.results = new ArrayList<>(loadLinearFixedContext2.mCountRecords);
                        } else if (this.lc.mLoadPart) {
                            LoadLinearFixedContext loadLinearFixedContext3 = this.lc;
                            loadLinearFixedContext3.initLCResults(loadLinearFixedContext3.mCountRecords);
                        }
                        HwIccFileHandlerReference.this.commandsInterfaceEx.iccIOForApp((int) HwIccFileHandlerReference.COMMAND_READ_RECORD, this.lc.mEfid, HwIccFileHandlerReference.this.mFh.getEFPathHw(this.lc.mEfid), this.lc.mRecordNum, 4, this.lc.mRecordSize, (String) null, (String) null, HwIccFileHandlerReference.this.mAid, obtainMessage(HwIccFileHandlerReference.EVENT_READ_RECORD_DONE, this.lc));
                    }
                }
            }
        }

        private void processReadRecordDone(Message msg) {
            this.ar = AsyncResultEx.from(msg.obj);
            AsyncResultEx asyncResultEx = this.ar;
            if (asyncResultEx != null) {
                this.lc = (LoadLinearFixedContext) asyncResultEx.getUserObj();
                this.result.setIccIoResult(this.ar.getResult());
                this.response = this.lc.mOnLoaded;
                if (this.ar.getException() != null) {
                    HwIccFileHandlerReference.this.sendResult(this.response, null, this.ar.getException());
                } else if (this.result.getException() != null) {
                    HwIccFileHandlerReference.this.sendResult(this.response, null, this.result.getException());
                } else if (this.lc.mLoadAll) {
                    this.lc.results.add(this.result.getPayload());
                    this.lc.mRecordNum++;
                    if (this.lc.mRecordNum > this.lc.mCountRecords) {
                        HwIccFileHandlerReference.this.sendResult(this.response, this.lc.results, null);
                    } else {
                        HwIccFileHandlerReference.this.commandsInterfaceEx.iccIOForApp((int) HwIccFileHandlerReference.COMMAND_READ_RECORD, this.lc.mEfid, HwIccFileHandlerReference.this.mFh.getEFPathHw(this.lc.mEfid), this.lc.mRecordNum, 4, this.lc.mRecordSize, (String) null, (String) null, HwIccFileHandlerReference.this.mAid, obtainMessage(HwIccFileHandlerReference.EVENT_READ_RECORD_DONE, this.lc));
                    }
                } else if (this.lc.mLoadPart) {
                    this.lc.results.set(this.lc.mRecordNum - 1, this.result.getPayload());
                    this.lc.mCount++;
                    if (this.lc.mCount < this.lc.mCountLoadrecords) {
                        LoadLinearFixedContext loadLinearFixedContext = this.lc;
                        loadLinearFixedContext.mRecordNum = loadLinearFixedContext.mRecordNums.get(this.lc.mCount).intValue();
                        if (this.lc.mRecordNum <= this.lc.mCountRecords) {
                            if (this.path == null) {
                                this.path = HwIccFileHandlerReference.this.mFh.getEFPathHw(this.lc.mEfid);
                            }
                            HwIccFileHandlerReference.this.commandsInterfaceEx.iccIOForApp((int) HwIccFileHandlerReference.COMMAND_READ_RECORD, this.lc.mEfid, this.path, this.lc.mRecordNum, 4, this.lc.mRecordSize, (String) null, (String) null, HwIccFileHandlerReference.this.mAid, obtainMessage(HwIccFileHandlerReference.EVENT_READ_RECORD_DONE, this.lc));
                            return;
                        }
                        HwIccFileHandlerReference.this.sendResult(this.response, this.lc.results, null);
                        return;
                    }
                    HwIccFileHandlerReference.this.sendResult(this.response, this.lc.results, null);
                } else {
                    HwIccFileHandlerReference.this.sendResult(this.response, this.result.getPayload(), null);
                }
            }
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                int i = msg.what;
                if (i == HwIccFileHandlerReference.EVENT_GET_RECORD_SIZE_DONE) {
                    processGetRecordSizeDone(msg);
                } else if (i != HwIccFileHandlerReference.EVENT_READ_RECORD_DONE) {
                    HwIccFileHandlerReference hwIccFileHandlerReference = HwIccFileHandlerReference.this;
                    hwIccFileHandlerReference.log("EfPartReaderHandler invalid message " + msg.what);
                } else {
                    processReadRecordDone(msg);
                }
            } catch (Exception ex) {
                Message message = this.response;
                if (message != null) {
                    HwIccFileHandlerReference.this.sendResult(message, null, ex);
                } else {
                    HwIccFileHandlerReference.this.loge("uncaught exception");
                }
            }
        }
    }

    private class LoadLinearExcludeEmptyHandler extends Handler {
        private LoadLinearExcludeEmptyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            AsyncResultEx ar = AsyncResultEx.from(msg.obj);
            Message response = null;
            if (ar != null) {
                response = ((LoadLinearFixedContext) ar.getUserObj()).mOnLoaded;
            }
            try {
                HwIccFileHandlerReference hwIccFileHandlerReference = HwIccFileHandlerReference.this;
                hwIccFileHandlerReference.log("handleMessage" + msg.what);
                switch (msg.what) {
                    case HwIccFileHandlerReference.EVENT_SEARCH_MATCHED_RECORD_DONE /* 1202 */:
                        HwIccFileHandlerReference.this.handleSearchedMatchedRecordDone(msg);
                        return;
                    case HwIccFileHandlerReference.EVENT_GET_RECORD_SIZE_DONE /* 1203 */:
                        HwIccFileHandlerReference.this.handleGetRecordSizeDone(msg);
                        return;
                    case HwIccFileHandlerReference.EVENT_READ_RECORD_DONE /* 1204 */:
                        HwIccFileHandlerReference.this.handleGetRecordDone(msg);
                        return;
                    default:
                        HwIccFileHandlerReference hwIccFileHandlerReference2 = HwIccFileHandlerReference.this;
                        hwIccFileHandlerReference2.log("LoadLinearExcludeEmptyHandler invalid message " + msg.what);
                        return;
                }
            } catch (ArithmeticException | ArrayStoreException | IndexOutOfBoundsException ex) {
                if (response != null) {
                    HwIccFileHandlerReference.this.sendResult(response, null, ex);
                } else {
                    HwIccFileHandlerReference.this.loge("caught exception");
                }
            } catch (Exception ex2) {
                if (response != null) {
                    HwIccFileHandlerReference.this.sendResult(response, null, ex2);
                } else {
                    HwIccFileHandlerReference.this.loge("uncaught exception");
                }
            }
        }
    }
}
