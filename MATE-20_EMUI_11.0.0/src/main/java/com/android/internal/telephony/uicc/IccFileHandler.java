package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.HwPartTelephonyFactory;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.huawei.internal.telephony.CommandsInterfaceEx;
import com.huawei.internal.telephony.uicc.UiccProfileEx;
import java.util.ArrayList;

public abstract class IccFileHandler extends Handler implements IIccFileHandlerInner, IccConstants {
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
    private static final boolean VDBG = false;
    @UnsupportedAppUsage
    protected final String mAid;
    @UnsupportedAppUsage
    protected final CommandsInterface mCi;
    IHwIccFileHandlerEx mHwIccFileHandlerEx;
    @UnsupportedAppUsage
    protected final UiccCardApplication mParentApp;

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public abstract String getEFPath(int i);

    /* access modifiers changed from: protected */
    public abstract void logd(String str);

    /* access modifiers changed from: protected */
    public abstract void loge(String str);

    /* access modifiers changed from: package-private */
    public static class LoadLinearFixedContext {
        @UnsupportedAppUsage
        int mCountRecords;
        int mEfid;
        boolean mLoadAll;
        Message mOnLoaded;
        String mPath;
        @UnsupportedAppUsage
        int mRecordNum;
        @UnsupportedAppUsage
        int mRecordSize;
        @UnsupportedAppUsage
        ArrayList<byte[]> results;

        @UnsupportedAppUsage
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
            this.mRecordNum = 1;
            this.mLoadAll = true;
            this.mOnLoaded = onLoaded;
            this.mPath = path;
        }

        LoadLinearFixedContext(int efid, Message onLoaded) {
            this.mEfid = efid;
            this.mRecordNum = 1;
            this.mLoadAll = true;
            this.mOnLoaded = onLoaded;
            this.mPath = null;
        }
    }

    protected IccFileHandler(UiccCardApplication app, String aid, CommandsInterface ci) {
        this.mParentApp = app;
        this.mAid = aid;
        this.mCi = ci;
        CommandsInterfaceEx commandsInterfaceEx = new CommandsInterfaceEx();
        commandsInterfaceEx.setCommandsInterface(ci);
        this.mHwIccFileHandlerEx = HwPartTelephonyFactory.loadFactory(HwPartTelephonyFactory.TELEPHONY_FACTORY_IMPL_NAME).createHwIccFileHandlerReference(this, aid, commandsInterfaceEx);
    }

    public void dispose() {
    }

    @UnsupportedAppUsage
    public void loadEFLinearFixed(int fileid, String path, int recordNum, Message onLoaded) {
        String efPath = path == null ? getEFPath(fileid) : path;
        this.mCi.iccIOForApp(192, fileid, efPath, 0, 0, 15, null, null, this.mAid, obtainMessage(6, new LoadLinearFixedContext(fileid, recordNum, efPath, onLoaded)));
    }

    @UnsupportedAppUsage
    public void loadEFLinearFixed(int fileid, int recordNum, Message onLoaded) {
        loadEFLinearFixed(fileid, getEFPath(fileid), recordNum, onLoaded);
    }

    public void loadEFImgLinearFixed(int recordNum, Message onLoaded) {
        this.mCi.iccIOForApp(192, IccConstants.EF_IMG, getEFPath(IccConstants.EF_IMG), recordNum, 4, 10, null, null, this.mAid, obtainMessage(11, new LoadLinearFixedContext((int) IccConstants.EF_IMG, recordNum, onLoaded)));
    }

    @UnsupportedAppUsage
    public void getEFLinearRecordSize(int fileid, String path, Message onLoaded) {
        String efPath = path == null ? getEFPath(fileid) : path;
        this.mCi.iccIOForApp(192, fileid, efPath, 0, 0, 15, null, null, this.mAid, obtainMessage(8, new LoadLinearFixedContext(fileid, efPath, onLoaded)));
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    @UnsupportedAppUsage
    public void getEFLinearRecordSize(int fileid, Message onLoaded) {
        getEFLinearRecordSize(fileid, getEFPath(fileid), onLoaded);
    }

    @UnsupportedAppUsage
    public void loadEFLinearFixedAll(int fileid, String path, Message onLoaded) {
        String efPath = path == null ? getEFPath(fileid) : path;
        this.mCi.iccIOForApp(192, fileid, efPath, 0, 0, 15, null, null, this.mAid, obtainMessage(6, new LoadLinearFixedContext(fileid, efPath, onLoaded)));
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    @UnsupportedAppUsage
    public void loadEFLinearFixedAll(int fileid, Message onLoaded) {
        loadEFLinearFixedAll(fileid, getEFPath(fileid), onLoaded);
    }

    @UnsupportedAppUsage
    public void loadEFTransparent(int fileid, Message onLoaded) {
        this.mCi.iccIOForApp(192, fileid, getEFPath(fileid), 0, 0, 15, null, null, this.mAid, obtainMessage(4, fileid, 0, onLoaded));
    }

    public void loadEFTransparent(int fileid, int size, Message onLoaded) {
        this.mCi.iccIOForApp(176, fileid, getEFPath(fileid), 0, 0, size, null, null, this.mAid, obtainMessage(5, fileid, 0, onLoaded));
    }

    public void loadEFImgTransparent(int fileid, int highOffset, int lowOffset, int length, Message onLoaded) {
        Message response = obtainMessage(10, fileid, 0, onLoaded);
        logd("IccFileHandler: loadEFImgTransparent fileid = " + fileid + " filePath = " + getEFPath(IccConstants.EF_IMG) + " highOffset = " + highOffset + " lowOffset = " + lowOffset + " length = " + length);
        this.mCi.iccIOForApp(176, fileid, getEFPath(IccConstants.EF_IMG), highOffset, lowOffset, length, null, null, this.mAid, response);
    }

    @UnsupportedAppUsage
    public void updateEFLinearFixed(int fileid, String path, int recordNum, byte[] data, String pin2, Message onComplete) {
        this.mCi.iccIOForApp(COMMAND_UPDATE_RECORD, fileid, path == null ? getEFPath(fileid) : path, recordNum, 4, data.length, IccUtils.bytesToHexString(data), pin2, this.mAid, onComplete);
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    @UnsupportedAppUsage
    public void updateEFLinearFixed(int fileid, int recordNum, byte[] data, String pin2, Message onComplete) {
        this.mCi.iccIOForApp(COMMAND_UPDATE_RECORD, fileid, getEFPath(fileid), recordNum, 4, data.length, IccUtils.bytesToHexString(data), pin2, this.mAid, onComplete);
    }

    @UnsupportedAppUsage
    public void updateEFTransparent(int fileid, byte[] data, Message onComplete) {
        this.mCi.iccIOForApp(214, fileid, getEFPath(fileid), 0, 0, data.length, IccUtils.bytesToHexString(data), null, this.mAid, onComplete);
    }

    private void sendResult(Message response, Object result, Throwable ex) {
        if (response != null) {
            AsyncResult.forMessage(response, result, ex);
            response.sendToTarget();
        }
    }

    private boolean processException(Message response, AsyncResult ar) {
        IccIoResult result = (IccIoResult) ar.result;
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

    @Override // android.os.Handler
    public void handleMessage(Message msg) {
        try {
            switch (msg.what) {
                case 4:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    Message response = (Message) ar.userObj;
                    IccIoResult result = (IccIoResult) ar.result;
                    if (!processException(response, (AsyncResult) msg.obj)) {
                        byte[] data = result.payload;
                        int fileid = msg.arg1;
                        if (4 != data[6]) {
                            throw new IccFileTypeMismatch();
                        } else if (data[13] == 0) {
                            this.mCi.iccIOForApp(176, fileid, getEFPath(fileid), 0, 0, ((data[2] & 255) << 8) + (data[3] & 255), null, null, this.mAid, obtainMessage(5, fileid, 0, response));
                            return;
                        } else {
                            throw new IccFileTypeMismatch();
                        }
                    } else {
                        return;
                    }
                case 5:
                case 10:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    Message response2 = (Message) ar2.userObj;
                    IccIoResult result2 = (IccIoResult) ar2.result;
                    if (!processException(response2, (AsyncResult) msg.obj)) {
                        sendResult(response2, result2.payload, null);
                        return;
                    }
                    return;
                case 6:
                case 11:
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    LoadLinearFixedContext lc = (LoadLinearFixedContext) ar3.userObj;
                    IccIoResult result3 = (IccIoResult) ar3.result;
                    if (processException(lc.mOnLoaded, (AsyncResult) msg.obj)) {
                        loge("exception caught from EVENT_GET_RECORD_SIZE");
                        return;
                    }
                    byte[] data2 = result3.payload;
                    String path = lc.mPath;
                    if (4 != data2[6]) {
                        throw new IccFileTypeMismatch();
                    } else if (1 == data2[13]) {
                        lc.mRecordSize = data2[14] & 255;
                        lc.mCountRecords = (((data2[2] & 255) << 8) + (data2[3] & 255)) / lc.mRecordSize;
                        if (lc.mLoadAll) {
                            lc.results = new ArrayList<>(lc.mCountRecords);
                        }
                        if (path == null) {
                            path = getEFPath(lc.mEfid);
                        }
                        this.mCi.iccIOForApp(178, lc.mEfid, path, lc.mRecordNum, 4, lc.mRecordSize, null, null, this.mAid, obtainMessage(7, lc));
                        return;
                    } else {
                        throw new IccFileTypeMismatch();
                    }
                case 7:
                case 9:
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    LoadLinearFixedContext lc2 = (LoadLinearFixedContext) ar4.userObj;
                    IccIoResult result4 = (IccIoResult) ar4.result;
                    Message response3 = lc2.mOnLoaded;
                    String path2 = lc2.mPath;
                    if (!processException(response3, (AsyncResult) msg.obj)) {
                        if (!lc2.mLoadAll) {
                            sendResult(response3, result4.payload, null);
                            return;
                        }
                        lc2.results.add(result4.payload);
                        lc2.mRecordNum++;
                        if (lc2.mRecordNum > lc2.mCountRecords) {
                            sendResult(response3, lc2.results, null);
                            return;
                        }
                        if (path2 == null) {
                            path2 = getEFPath(lc2.mEfid);
                        }
                        this.mCi.iccIOForApp(178, lc2.mEfid, path2, lc2.mRecordNum, 4, lc2.mRecordSize, null, null, this.mAid, obtainMessage(7, lc2));
                        return;
                    }
                    return;
                case 8:
                    AsyncResult ar5 = (AsyncResult) msg.obj;
                    IccIoResult result5 = (IccIoResult) ar5.result;
                    Message response4 = ((LoadLinearFixedContext) ar5.userObj).mOnLoaded;
                    if (!processException(response4, (AsyncResult) msg.obj)) {
                        byte[] data3 = result5.payload;
                        if (4 == data3[6] && 1 == data3[13]) {
                            int[] recordSize = new int[3];
                            recordSize[0] = data3[14] & 255;
                            recordSize[1] = ((data3[2] & 255) << 8) + (data3[3] & 255);
                            recordSize[2] = recordSize[1] / recordSize[0];
                            sendResult(response4, recordSize, null);
                            return;
                        }
                        throw new IccFileTypeMismatch();
                    }
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        } catch (Exception exc) {
            if (0 != 0) {
                sendResult(null, null, exc);
            } else {
                loge("uncaught exception");
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getCommonIccEFPath(int efid) {
        if (efid == 12037 || efid == 12258) {
            return IccConstants.MF_SIM;
        }
        if (efid == 20256) {
            return "3F007F105F50";
        }
        if (efid == 20272) {
            return "3F007F105F3A";
        }
        if (efid == 20356) {
            return "3F007FFF5F50";
        }
        if (efid == 28480 || efid == 28645 || efid == 28474 || efid == 28475) {
            return "3F007F10";
        }
        switch (efid) {
            case IccConstants.EF_SDN /* 28489 */:
            case IccConstants.EF_EXT1 /* 28490 */:
            case IccConstants.EF_EXT2 /* 28491 */:
            case IccConstants.EF_EXT3 /* 28492 */:
                return "3F007F10";
            default:
                return null;
        }
    }

    private String getCdmaPath() {
        UiccProfile uiccProfile = this.mParentApp.getUiccProfile();
        if (uiccProfile == null || !uiccProfile.isApplicationOnIcc(IccCardApplicationStatus.AppType.APPTYPE_CSIM)) {
            return "3F007F25";
        }
        return "3F007FFF";
    }

    public void isUimSupportMeidValue(Message result) {
        this.mCi.iccIOForApp(176, IccConstants.EF_CST, "3F007F25", 0, 0, 3, null, null, this.mAid, result);
    }

    public void getMeidOrPesnValue(Message result) {
        this.mCi.iccIOForApp(176, 28472, getCdmaPath(), 0, 0, 8, null, null, this.mAid, result);
    }

    public void setMeidOrPesnValue(String meid, String pesn, Message result) {
        String writedValue = PhoneConfigurationManager.SSSS;
        if (meid != null) {
            writedValue = ProxyController.MODEM_0 + (meid.length() / 2) + meid;
        } else if (pesn != null) {
            writedValue = ProxyController.MODEM_0 + (pesn.length() / 2) + pesn + ServiceStateTracker.UNACTIVATED_MIN2_VALUE;
        }
        this.mCi.iccIOForApp(COMMAND_WRITE_MEID_OR_PESN, 28472, getCdmaPath(), 0, 0, 8, writedValue, null, this.mAid, result);
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    public void getSmscAddress(Message result) {
        this.mHwIccFileHandlerEx.getSmscAddress(result);
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    public void setSmscAddress(String address, Message result) {
        this.mHwIccFileHandlerEx.setSmscAddress(address, result);
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    public void loadEFLinearFixedAllExcludeEmpty(int fileId, Message onLoaded) {
        this.mHwIccFileHandlerEx.loadEFLinearFixedAllExcludeEmpty(fileId, onLoaded);
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    public void loadEFTransparent(String filePath, int fileid, Message onLoaded) {
        this.mHwIccFileHandlerEx.loadEFTransparent(filePath, fileid, onLoaded);
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    public void loadEFTransparent(String filePath, int fileid, Message onLoaded, boolean isForApp) {
        this.mHwIccFileHandlerEx.loadEFTransparent(filePath, fileid, onLoaded, isForApp);
    }

    public IccRecords getIccRecords() {
        return this.mParentApp.getIccRecords();
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    public String getEFPathHw(int efid) {
        return getEFPath(efid);
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    public UiccProfileEx getUiccProfileEx() {
        return this.mParentApp.getUiccProfileHw();
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    public boolean has3Gphonebook() {
        if (this.mParentApp.getIccRecords() != null) {
            return this.mParentApp.getIccRecords().has3Gphonebook();
        }
        return false;
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    public boolean isInstanceOfCsimFileHandler() {
        return this instanceof CsimFileHandler;
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    public boolean isInstanceOfUsimFileHandler() {
        return this instanceof UsimFileHandler;
    }

    @Override // com.android.internal.telephony.uicc.IIccFileHandlerInner
    public boolean isInstanceOfIsimFileHandler() {
        return this instanceof IsimFileHandler;
    }
}
