package com.android.internal.telephony.uicc;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.google.android.mms.pdu.PduHeaders;
import java.util.ArrayList;

public abstract class IccFileHandler extends AbstractIccFileHandler implements IccConstants {
    protected static final int COMMAND_GET_RESPONSE = 192;
    protected static final int COMMAND_READ_BINARY = 176;
    protected static final int COMMAND_READ_RECORD = 178;
    protected static final int COMMAND_SEEK = 162;
    protected static final int COMMAND_UPDATE_BINARY = 214;
    protected static final int COMMAND_UPDATE_RECORD = 220;
    protected static final int COMMAND_WRITE_MEID_OR_PESN = 222;
    protected static final int EF_ESN_ME = 28472;
    protected static final int EF_TYPE_CYCLIC = 3;
    protected static final int EF_TYPE_LINEAR_FIXED = 1;
    protected static final int EF_TYPE_TRANSPARENT = 0;
    protected static final int EVENT_GET_BINARY_SIZE_DONE = 4;
    protected static final int EVENT_GET_EF_LINEAR_RECORD_SIZE_DONE = 8;
    protected static final int EVENT_GET_RECORD_SIZE_DONE = 6;
    protected static final int EVENT_GET_RECORD_SIZE_IMG_DONE = 11;
    protected static final int EVENT_READ_BINARY_DONE = 5;
    protected static final int EVENT_READ_ICON_DONE = 10;
    protected static final int EVENT_READ_IMG_DONE = 9;
    protected static final int EVENT_READ_RECORD_DONE = 7;
    protected static final int GET_RESPONSE_EF_IMG_SIZE_BYTES = 10;
    protected static final int GET_RESPONSE_EF_SIZE_BYTES = 15;
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
    protected static final int TYPE_DF = 2;
    protected static final int TYPE_EF = 4;
    protected static final int TYPE_MF = 1;
    protected static final int TYPE_RFU = 0;
    protected final String mAid;
    protected final CommandsInterface mCi;
    protected final UiccCardApplication mParentApp;

    static class LoadLinearFixedContext {
        int mCountRecords;
        int mEfid;
        boolean mLoadAll;
        Message mOnLoaded;
        String mPath;
        int mRecordNum;
        int mRecordSize;
        ArrayList<byte[]> results;

        LoadLinearFixedContext(int efid, int recordNum, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = recordNum;
            this.mOnLoaded = onLoaded;
            this.mLoadAll = false;
            this.mPath = null;
        }

        LoadLinearFixedContext(int efid, int recordNum, String path, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = recordNum;
            this.mOnLoaded = onLoaded;
            this.mLoadAll = false;
            this.mPath = path;
        }

        LoadLinearFixedContext(int efid, String path, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = IccFileHandler.TYPE_MF;
            this.mLoadAll = true;
            this.mOnLoaded = onLoaded;
            this.mPath = path;
        }

        LoadLinearFixedContext(int efid, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = IccFileHandler.TYPE_MF;
            this.mLoadAll = true;
            this.mOnLoaded = onLoaded;
            this.mPath = null;
        }
    }

    protected abstract String getEFPath(int i);

    protected abstract void logd(String str);

    protected abstract void loge(String str);

    protected IccFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        this.mParentApp = app;
        this.mAid = aid;
        this.mCi = ci;
    }

    public void dispose() {
    }

    public void loadEFLinearFixed(int fileid, String path, int recordNum, Message onLoaded) {
        String efPath = path == null ? getEFPath(fileid) : path;
        this.mCi.iccIOForApp(COMMAND_GET_RESPONSE, fileid, efPath, RESPONSE_DATA_RFU_1, RESPONSE_DATA_RFU_1, GET_RESPONSE_EF_SIZE_BYTES, null, null, this.mAid, obtainMessage(RESPONSE_DATA_FILE_TYPE, new LoadLinearFixedContext(fileid, recordNum, efPath, onLoaded)));
    }

    public void loadEFLinearFixed(int fileid, int recordNum, Message onLoaded) {
        loadEFLinearFixed(fileid, getEFPath(fileid), recordNum, onLoaded);
    }

    public void loadEFImgLinearFixed(int recordNum, Message onLoaded) {
        int i = recordNum;
        String str = null;
        this.mCi.iccIOForApp(COMMAND_GET_RESPONSE, IccConstants.EF_IMG, getEFPath(IccConstants.EF_IMG), i, TYPE_EF, RESPONSE_DATA_ACCESS_CONDITION_3, null, str, this.mAid, obtainMessage(RESPONSE_DATA_FILE_STATUS, new LoadLinearFixedContext((int) IccConstants.EF_IMG, recordNum, onLoaded)));
    }

    public void getEFLinearRecordSize(int fileid, String path, Message onLoaded) {
        String efPath = path == null ? getEFPath(fileid) : path;
        this.mCi.iccIOForApp(COMMAND_GET_RESPONSE, fileid, efPath, RESPONSE_DATA_RFU_1, RESPONSE_DATA_RFU_1, GET_RESPONSE_EF_SIZE_BYTES, null, null, this.mAid, obtainMessage(RESPONSE_DATA_ACCESS_CONDITION_1, new LoadLinearFixedContext(fileid, efPath, onLoaded)));
    }

    public void getEFLinearRecordSize(int fileid, Message onLoaded) {
        getEFLinearRecordSize(fileid, getEFPath(fileid), onLoaded);
    }

    public void loadEFLinearFixedAll(int fileid, String path, Message onLoaded) {
        String efPath = path == null ? getEFPath(fileid) : path;
        this.mCi.iccIOForApp(COMMAND_GET_RESPONSE, fileid, efPath, RESPONSE_DATA_RFU_1, RESPONSE_DATA_RFU_1, GET_RESPONSE_EF_SIZE_BYTES, null, null, this.mAid, obtainMessage(RESPONSE_DATA_FILE_TYPE, new LoadLinearFixedContext(fileid, efPath, onLoaded)));
    }

    public void loadEFLinearFixedAll(int fileid, Message onLoaded) {
        loadEFLinearFixedAll(fileid, getEFPath(fileid), onLoaded);
    }

    public void loadEFTransparent(int fileid, Message onLoaded) {
        int i = fileid;
        int i2 = RESPONSE_DATA_RFU_1;
        String str = null;
        this.mCi.iccIOForApp(COMMAND_GET_RESPONSE, i, getEFPath(fileid), RESPONSE_DATA_RFU_1, i2, GET_RESPONSE_EF_SIZE_BYTES, null, str, this.mAid, obtainMessage(TYPE_EF, fileid, RESPONSE_DATA_RFU_1, onLoaded));
    }

    public void loadEFTransparent(int fileid, int size, Message onLoaded) {
        int i = fileid;
        int i2 = RESPONSE_DATA_RFU_1;
        int i3 = size;
        String str = null;
        this.mCi.iccIOForApp(COMMAND_READ_BINARY, i, getEFPath(fileid), RESPONSE_DATA_RFU_1, i2, i3, null, str, this.mAid, obtainMessage(RESPONSE_DATA_FILE_ID_2, fileid, RESPONSE_DATA_RFU_1, onLoaded));
    }

    public void loadEFImgTransparent(int fileid, int highOffset, int lowOffset, int length, Message onLoaded) {
        Message response = obtainMessage(RESPONSE_DATA_ACCESS_CONDITION_3, fileid, RESPONSE_DATA_RFU_1, onLoaded);
        logd("IccFileHandler: loadEFImgTransparent fileid = " + fileid + " filePath = " + getEFPath(IccConstants.EF_IMG) + " highOffset = " + highOffset + " lowOffset = " + lowOffset + " length = " + length);
        this.mCi.iccIOForApp(COMMAND_READ_BINARY, fileid, getEFPath(IccConstants.EF_IMG), highOffset, lowOffset, length, null, null, this.mAid, response);
    }

    public void updateEFLinearFixed(int fileid, String path, int recordNum, byte[] data, String pin2, Message onComplete) {
        this.mCi.iccIOForApp(COMMAND_UPDATE_RECORD, fileid, path == null ? getEFPath(fileid) : path, recordNum, TYPE_EF, data.length, IccUtils.bytesToHexString(data), pin2, this.mAid, onComplete);
    }

    public void updateEFLinearFixed(int fileid, int recordNum, byte[] data, String pin2, Message onComplete) {
        this.mCi.iccIOForApp(COMMAND_UPDATE_RECORD, fileid, getEFPath(fileid), recordNum, TYPE_EF, data.length, IccUtils.bytesToHexString(data), pin2, this.mAid, onComplete);
    }

    public void updateEFTransparent(int fileid, byte[] data, Message onComplete) {
        this.mCi.iccIOForApp(COMMAND_UPDATE_BINARY, fileid, getEFPath(fileid), RESPONSE_DATA_RFU_1, RESPONSE_DATA_RFU_1, data.length, IccUtils.bytesToHexString(data), null, this.mAid, onComplete);
    }

    private void sendResult(Message response, Object result, Throwable ex) {
        if (response != null) {
            AsyncResult.forMessage(response, result, ex);
            response.sendToTarget();
        }
    }

    private boolean processException(Message response, AsyncResult ar) {
        IccIoResult result = ar.result;
        if (ar.exception != null) {
            sendResult(response, null, ar.exception);
            return true;
        }
        IccException iccException = result.getException();
        if (iccException == null) {
            return false;
        }
        sendResult(response, null, iccException);
        return true;
    }

    public void handleMessage(Message msg) {
        Message response = null;
        try {
            AsyncResult ar;
            IccIoResult result;
            byte[] data;
            LoadLinearFixedContext lc;
            String path;
            switch (msg.what) {
                case TYPE_EF /*4*/:
                    ar = (AsyncResult) msg.obj;
                    response = (Message) ar.userObj;
                    result = (IccIoResult) ar.result;
                    if (!processException(response, (AsyncResult) msg.obj)) {
                        data = result.payload;
                        int fileid = msg.arg1;
                        if (TYPE_EF != data[RESPONSE_DATA_FILE_TYPE]) {
                            throw new IccFileTypeMismatch();
                        } else if (data[RESPONSE_DATA_STRUCTURE] != null) {
                            throw new IccFileTypeMismatch();
                        } else {
                            this.mCi.iccIOForApp(COMMAND_READ_BINARY, fileid, getEFPath(fileid), RESPONSE_DATA_RFU_1, RESPONSE_DATA_RFU_1, ((data[TYPE_DF] & PduHeaders.STORE_STATUS_ERROR_END) << RESPONSE_DATA_ACCESS_CONDITION_1) + (data[RESPONSE_DATA_FILE_SIZE_2] & PduHeaders.STORE_STATUS_ERROR_END), null, null, this.mAid, obtainMessage(RESPONSE_DATA_FILE_ID_2, fileid, RESPONSE_DATA_RFU_1, response));
                            return;
                        }
                    }
                    return;
                case RESPONSE_DATA_FILE_ID_2 /*5*/:
                case RESPONSE_DATA_ACCESS_CONDITION_3 /*10*/:
                    ar = (AsyncResult) msg.obj;
                    response = (Message) ar.userObj;
                    result = (IccIoResult) ar.result;
                    if (!processException(response, (AsyncResult) msg.obj)) {
                        sendResult(response, result.payload, null);
                        return;
                    }
                    return;
                case RESPONSE_DATA_FILE_TYPE /*6*/:
                case RESPONSE_DATA_FILE_STATUS /*11*/:
                    ar = (AsyncResult) msg.obj;
                    lc = (LoadLinearFixedContext) ar.userObj;
                    result = (IccIoResult) ar.result;
                    if (!processException(lc.mOnLoaded, (AsyncResult) msg.obj)) {
                        data = result.payload;
                        path = lc.mPath;
                        if (TYPE_EF != data[RESPONSE_DATA_FILE_TYPE]) {
                            throw new IccFileTypeMismatch();
                        } else if (TYPE_MF != data[RESPONSE_DATA_STRUCTURE]) {
                            throw new IccFileTypeMismatch();
                        } else {
                            lc.mRecordSize = data[RESPONSE_DATA_RECORD_LENGTH] & PduHeaders.STORE_STATUS_ERROR_END;
                            lc.mCountRecords = (((data[TYPE_DF] & PduHeaders.STORE_STATUS_ERROR_END) << RESPONSE_DATA_ACCESS_CONDITION_1) + (data[RESPONSE_DATA_FILE_SIZE_2] & PduHeaders.STORE_STATUS_ERROR_END)) / lc.mRecordSize;
                            if (lc.mLoadAll) {
                                lc.results = new ArrayList(lc.mCountRecords);
                            }
                            if (path == null) {
                                path = getEFPath(lc.mEfid);
                            }
                            this.mCi.iccIOForApp(COMMAND_READ_RECORD, lc.mEfid, path, lc.mRecordNum, TYPE_EF, lc.mRecordSize, null, null, this.mAid, obtainMessage(RESPONSE_DATA_RFU_3, lc));
                            return;
                        }
                    }
                    return;
                case RESPONSE_DATA_RFU_3 /*7*/:
                case RESPONSE_DATA_ACCESS_CONDITION_2 /*9*/:
                    ar = (AsyncResult) msg.obj;
                    lc = (LoadLinearFixedContext) ar.userObj;
                    result = (IccIoResult) ar.result;
                    response = lc.mOnLoaded;
                    path = lc.mPath;
                    if (!processException(response, (AsyncResult) msg.obj)) {
                        if (lc.mLoadAll) {
                            lc.results.add(result.payload);
                            lc.mRecordNum += TYPE_MF;
                            if (lc.mRecordNum > lc.mCountRecords) {
                                sendResult(response, lc.results, null);
                                return;
                            }
                            if (path == null) {
                                path = getEFPath(lc.mEfid);
                            }
                            String str = path;
                            this.mCi.iccIOForApp(COMMAND_READ_RECORD, lc.mEfid, str, lc.mRecordNum, TYPE_EF, lc.mRecordSize, null, null, this.mAid, obtainMessage(RESPONSE_DATA_RFU_3, lc));
                            return;
                        }
                        sendResult(response, result.payload, null);
                        return;
                    }
                    return;
                case RESPONSE_DATA_ACCESS_CONDITION_1 /*8*/:
                    ar = msg.obj;
                    lc = ar.userObj;
                    result = ar.result;
                    response = lc.mOnLoaded;
                    if (!processException(response, (AsyncResult) msg.obj)) {
                        data = result.payload;
                        if (TYPE_EF == data[RESPONSE_DATA_FILE_TYPE] && TYPE_MF == data[RESPONSE_DATA_STRUCTURE]) {
                            Object recordSize = new int[RESPONSE_DATA_FILE_SIZE_2];
                            recordSize[RESPONSE_DATA_RFU_1] = data[RESPONSE_DATA_RECORD_LENGTH] & PduHeaders.STORE_STATUS_ERROR_END;
                            recordSize[TYPE_MF] = ((data[TYPE_DF] & PduHeaders.STORE_STATUS_ERROR_END) << RESPONSE_DATA_ACCESS_CONDITION_1) + (data[RESPONSE_DATA_FILE_SIZE_2] & PduHeaders.STORE_STATUS_ERROR_END);
                            recordSize[TYPE_DF] = recordSize[TYPE_MF] / recordSize[RESPONSE_DATA_RFU_1];
                            sendResult(response, recordSize, null);
                            return;
                        }
                        throw new IccFileTypeMismatch();
                    }
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        } catch (Throwable exc) {
            if (response == null) {
                loge("uncaught exception" + exc);
            } else {
                sendResult(response, null, exc);
            }
        }
        if (response == null) {
            sendResult(response, null, exc);
        } else {
            loge("uncaught exception" + exc);
        }
    }

    protected String getCommonIccEFPath(int efid) {
        switch (efid) {
            case IccConstants.EF_PL /*12037*/:
            case IccConstants.EF_ICCID /*12258*/:
                return IccConstants.MF_SIM;
            case IccConstants.EF_IMG /*20256*/:
                return "3F007F105F50";
            case IccConstants.EF_PBR /*20272*/:
                return "3F007F105F3A";
            case IccConstants.EF_CSIM_LI /*28474*/:
            case IccConstants.EF_FDN /*28475*/:
            case IccConstants.EF_MSISDN /*28480*/:
            case IccConstants.EF_SDN /*28489*/:
            case IccConstants.EF_EXT1 /*28490*/:
            case IccConstants.EF_EXT2 /*28491*/:
            case IccConstants.EF_EXT3 /*28492*/:
            case IccConstants.EF_PSI /*28645*/:
                return "3F007F10";
            default:
                return null;
        }
    }

    private String getCdmaPath() {
        String filePath = "3F007F25";
        UiccCard card = this.mParentApp.getUiccCard();
        if (card == null || !card.isApplicationOnIcc(AppType.APPTYPE_CSIM)) {
            return filePath;
        }
        return "3F007FFF";
    }

    public void isUimSupportMeidValue(Message result) {
        this.mCi.iccIOForApp(COMMAND_READ_BINARY, IccConstants.EF_CST, "3F007F25", RESPONSE_DATA_RFU_1, RESPONSE_DATA_RFU_1, RESPONSE_DATA_FILE_SIZE_2, null, null, this.mAid, result);
    }

    public void getMeidOrPesnValue(Message result) {
        this.mCi.iccIOForApp(COMMAND_READ_BINARY, EF_ESN_ME, getCdmaPath(), RESPONSE_DATA_RFU_1, RESPONSE_DATA_RFU_1, RESPONSE_DATA_ACCESS_CONDITION_1, null, null, this.mAid, result);
    }

    public void setMeidOrPesnValue(String meid, String pesn, Message result) {
        String writedValue = "";
        if (meid != null) {
            writedValue = ProxyController.MODEM_0 + (meid.length() / TYPE_DF) + meid;
        } else {
            writedValue = ProxyController.MODEM_0 + (pesn.length() / TYPE_DF) + pesn + ServiceStateTracker.UNACTIVATED_MIN2_VALUE;
        }
        this.mCi.iccIOForApp(COMMAND_WRITE_MEID_OR_PESN, EF_ESN_ME, getCdmaPath(), RESPONSE_DATA_RFU_1, RESPONSE_DATA_RFU_1, RESPONSE_DATA_ACCESS_CONDITION_1, writedValue, null, this.mAid, result);
    }
}
