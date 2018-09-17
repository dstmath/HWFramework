package com.android.internal.telephony;

import android.hardware.radio.V1_0.CellInfo;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.telephony.Rlog;
import android.telephony.UiccAuthResponse;
import android.telephony.UiccAuthResponse.UiccAuthResponseData;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.ArrayList;
import vendor.huawei.hardware.radio.V1_0.CsgNetworkInfo;
import vendor.huawei.hardware.radio.V1_0.RILDeviceVersionResponse;
import vendor.huawei.hardware.radio.V1_0.RILDsFlowInfoResponse;
import vendor.huawei.hardware.radio.V1_0.RILImsCall;
import vendor.huawei.hardware.radio.V1_0.RILPreferredPLMNSelector;
import vendor.huawei.hardware.radio.V1_0.RILRADIOSYSINFO;
import vendor.huawei.hardware.radio.V1_0.RILUICCAUTHAUTSTYPE;
import vendor.huawei.hardware.radio.V1_0.RILUICCAUTHCKTYPE;
import vendor.huawei.hardware.radio.V1_0.RILUICCAUTHIKTYPE;
import vendor.huawei.hardware.radio.V1_0.RILUICCAUTHRESPCHALLENGETYPE;
import vendor.huawei.hardware.radio.V1_0.RILUICCAUTHRESPONSE;
import vendor.huawei.hardware.radio.V1_0.RILUICCAUTHRESTYPE;
import vendor.huawei.hardware.radio.V1_0.RspMsgPayload;

public class HwRadioResponse extends RadioResponse {
    private static final int DEVICE_VERSION_LENGTH = 11;
    private static final int SYSTEM_INFO_EX_LENGTH = 7;
    private static final int TRAFFIC_DATA_LENGTH = 6;
    private int mPhoneId;
    RIL mRil;
    private String mTag = null;

    public HwRadioResponse(RIL ril) {
        super(ril);
        this.mRil = ril;
        this.mPhoneId = 0;
        this.mTag = "HwRadioResponse" + this.mPhoneId;
    }

    public void acknowledgeRequest(int serial) {
        this.mRil.processRequestAck(serial);
    }

    public void processRadioResponse(RadioResponseInfo responseInfo, Object ret) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }

    public void RspMsg(RadioResponseInfo responseInfo, int msgId, RspMsgPayload payload) {
        Object ret = null;
        if (payload == null) {
            Rlog.d(this.mTag, "got null payload msgId = " + msgId);
            return;
        }
        switch (msgId) {
            case 158:
            case 162:
            case 164:
            case 171:
            case 172:
            case 175:
            case 177:
            case 178:
            case 182:
            case 188:
            case 189:
            case 194:
            case 196:
            case 199:
            case 205:
            case 243:
            case 245:
            case 249:
            case 268:
            case 271:
            case 272:
            case 277:
            case 285:
            case 286:
            case 288:
            case 290:
            case 292:
            case 296:
            case 297:
            case 298:
            case 302:
            case 303:
            case 308:
            case 338:
                break;
            case 168:
                ret = processInts(payload);
                break;
            case 169:
                ret = processString(payload);
                break;
            case 173:
                ret = processInts(payload);
                break;
            case 174:
                ret = processStrings(payload);
                break;
            case 176:
                ret = processInts(payload);
                break;
            case 192:
                ret = processInts(payload);
                break;
            case 206:
                ret = processInts(payload);
                break;
            case 209:
                ret = processString(payload);
                break;
            case 214:
                ret = processInts(payload);
                break;
            case 215:
                ret = processInts(payload);
                break;
            case 219:
                ret = processString(payload);
                break;
            case 241:
                ret = processInts(payload);
                break;
            case 242:
                ret = responseNetworkInfoWithActs(payload);
                break;
            case 250:
                ret = processInts(payload);
                break;
            case 252:
                ret = responseICCID(payload.strData);
                break;
            case 265:
                ret = processInts(payload);
                break;
            case 266:
                ret = processInts(payload);
                break;
            case 287:
                ret = processInts(payload);
                break;
            case 289:
                ret = processInts(payload);
                break;
            case 293:
                ret = processInts(payload);
                break;
            case 304:
                ret = processInts(payload);
                break;
            case 305:
                ret = processInts(payload);
                break;
            case 307:
                ret = processStrings(payload);
                break;
            case 309:
                ret = processInts(payload);
                break;
            case 329:
                ret = processInts(payload);
                break;
            case 331:
                ret = processInts(payload);
                break;
            case 344:
                ret = processInts(payload);
                break;
            default:
                extendRspMsg(responseInfo, msgId, payload, true);
                break;
        }
        if (1 != null) {
            if (responseInfo.error != 0) {
                ret = null;
            }
            processRadioResponse(responseInfo, ret);
        }
    }

    public void extendRspMsg(RadioResponseInfo responseInfo, int msgId, RspMsgPayload payload, boolean validMsgId) {
        Object ret = null;
        switch (msgId) {
            case 263:
                ret = processInts(payload);
                break;
            case 299:
            case 312:
            case 316:
            case 345:
            case 347:
                break;
            case 332:
                ret = HwRadioIndication.convertHwHalSignalStrength((int[]) processInts(payload));
                break;
            case 346:
                ret = processInts(payload);
                break;
            default:
                validMsgId = false;
                this.mRil.processResponseDone(this.mRil.processResponse(responseInfo), responseInfo, null);
                Rlog.d(this.mTag, "got invalid msgId = " + msgId);
                break;
        }
        if (validMsgId) {
            if (responseInfo.error != 0) {
                ret = null;
            }
            processRadioResponse(responseInfo, ret);
        }
    }

    public void uiccAuthResponse(RadioResponseInfo responseInfo, RILUICCAUTHRESPONSE uiccAuthRst) {
        responseUiccAuth(responseInfo, uiccAuthRst);
    }

    private void responseUiccAuth(RadioResponseInfo responseInfo, RILUICCAUTHRESPONSE uiccAuthRst) {
        Rlog.d(this.mTag, "Response of RIL_REQUEST_HW_VOWIFI_UICC_AUTH");
        RILRequest rr = this.mRil.processResponse(responseInfo);
        if (rr != null) {
            Object ret = null;
            if (responseInfo.error == 0) {
                Rlog.d(this.mTag, "NO ERROR,start to process GbaAuth");
                UiccAuthResponse uiccResponse = new UiccAuthResponse();
                uiccResponse.mResult = uiccAuthRst.authStatus;
                Rlog.d(this.mTag, "responseUiccAuth, mStatus=" + uiccResponse.mResult);
                if (uiccResponse.mResult == 0) {
                    RILUICCAUTHRESPCHALLENGETYPE authChang = uiccAuthRst.authChallenge;
                    RILUICCAUTHRESTYPE resDatas = authChang.resData;
                    uiccResponse.mUiccAuthChallenge.mResData = new UiccAuthResponseData();
                    uiccResponse.mUiccAuthChallenge.mResData.present = resDatas.resPresent;
                    uiccResponse.mUiccAuthChallenge.mResData.data = IccUtils.hexStringToBytes(resDatas.res);
                    uiccResponse.mUiccAuthChallenge.mResData.len = uiccResponse.mUiccAuthChallenge.mResData.data.length;
                    RILUICCAUTHIKTYPE ikType = authChang.ikData;
                    uiccResponse.mUiccAuthChallenge.mIkData = new UiccAuthResponseData();
                    uiccResponse.mUiccAuthChallenge.mIkData.present = ikType.ikPresent;
                    uiccResponse.mUiccAuthChallenge.mIkData.data = IccUtils.hexStringToBytes(ikType.ik);
                    uiccResponse.mUiccAuthChallenge.mIkData.len = uiccResponse.mUiccAuthChallenge.mIkData.data.length;
                    RILUICCAUTHCKTYPE ckDatas = authChang.ckData;
                    uiccResponse.mUiccAuthChallenge.mCkData = new UiccAuthResponseData();
                    uiccResponse.mUiccAuthChallenge.mCkData.present = ckDatas.ckPresent;
                    uiccResponse.mUiccAuthChallenge.mCkData.data = IccUtils.hexStringToBytes(ckDatas.ck);
                    uiccResponse.mUiccAuthChallenge.mCkData.len = uiccResponse.mUiccAuthChallenge.mCkData.data.length;
                } else {
                    RILUICCAUTHAUTSTYPE autsDatas = uiccAuthRst.authSyncfail.autsData;
                    uiccResponse.mUiccAuthSyncFail.present = autsDatas.autsPresent;
                    uiccResponse.mUiccAuthSyncFail.data = IccUtils.hexStringToBytes(autsDatas.auts);
                    uiccResponse.mUiccAuthSyncFail.len = uiccResponse.mUiccAuthSyncFail.data.length;
                }
                sendMessageResponse(rr.mResult, uiccResponse);
                ret = uiccResponse;
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }

    private Object responseICCID(String response) {
        return HwIccUtils.hexStringToBcd(response);
    }

    private Object processInts(RspMsgPayload payload) {
        int numInts = payload.nDatas.size();
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = ((Integer) payload.nDatas.get(i)).intValue();
        }
        return response;
    }

    private Object processStrings(RspMsgPayload payload) {
        int numStrings = payload.strDatas.size();
        String[] response = new String[numStrings];
        for (int i = 0; i < numStrings; i++) {
            response[i] = (String) payload.strDatas.get(i);
        }
        return response;
    }

    private Object processString(RspMsgPayload payload) {
        return payload.strData;
    }

    public void getDeviceVersionResponse(RadioResponseInfo responseInfo, RILDeviceVersionResponse deviceVersion) {
        if (deviceVersion == null) {
            Rlog.d(this.mTag, "getDeviceVersionResponse: deviceVersion is null.");
            return;
        }
        processRadioResponse(responseInfo, new String[]{deviceVersion.buildTime, deviceVersion.externalSwVersion, deviceVersion.internalSwVersion, deviceVersion.externalDbVersion, deviceVersion.internalDbVersion, deviceVersion.externalHwVersion, deviceVersion.internalHwVersion, deviceVersion.externalDutName, deviceVersion.internalDutName, deviceVersion.configurateVersion, deviceVersion.prlVersion});
    }

    public void getDsFlowInfoResponse(RadioResponseInfo responseInfo, RILDsFlowInfoResponse dsFlowInfo) {
        if (dsFlowInfo == null) {
            Rlog.d(this.mTag, "getDsFlowInfoResponse: dsFlowInfo is null.");
            return;
        }
        processRadioResponse(responseInfo, new String[]{dsFlowInfo.lastDsTime, dsFlowInfo.lastTxFlow, dsFlowInfo.lastRxFlow, dsFlowInfo.totalDsTime, dsFlowInfo.totalTxFlow, dsFlowInfo.totalRxFlow});
    }

    public void getPolListResponse(RadioResponseInfo responseInfo, RILPreferredPLMNSelector preferredplmnselector) {
    }

    public void getSystemInfoExResponse(RadioResponseInfo responseInfo, RILRADIOSYSINFO sysInfo) {
        if (sysInfo == null) {
            Rlog.d(this.mTag, "getSystemInfoExResponse: sysInfo is null.");
            return;
        }
        processRadioResponse(responseInfo, new int[]{sysInfo.sysSubmode, sysInfo.srvStatus, sysInfo.srvDomain, sysInfo.roamStatus, sysInfo.sysMode, sysInfo.simState, sysInfo.lockState});
    }

    private Object responseNetworkInfoWithActs(RspMsgPayload payload) {
        int numInts = payload.nDatas.size();
        int[] response = new int[(numInts * 6)];
        for (int i = 0; i < numInts * 6; i++) {
            response[i] = ((Integer) payload.nDatas.get(i)).intValue();
        }
        return response;
    }

    public void getCurrentImsCallsResponse(RadioResponseInfo responseInfo, ArrayList<RILImsCall> arrayList) {
    }

    public void getAvailableCsgIdsResponse(RadioResponseInfo responseInfo, ArrayList<CsgNetworkInfo> csgNetworkInfos) {
        responseCsgNetworkInfos(responseInfo, csgNetworkInfos);
    }

    public void manualSelectionCsgIdResponse(RadioResponseInfo responseInfo) {
        processRadioResponse(responseInfo, null);
    }

    private void responseCsgNetworkInfos(RadioResponseInfo responseInfo, ArrayList<CsgNetworkInfo> csgNetworkInfos) {
        RILRequest rr = this.mRil.processResponse(responseInfo);
        Rlog.d(this.mTag, "Response of AvailableCsgIds rr: " + rr + "error: " + responseInfo.error + " csgNetworkInfos: " + csgNetworkInfos.size());
        if (rr != null) {
            ArrayList<HwHisiCsgNetworkInfo> ret = new ArrayList();
            int csgNetworkInfoSize = csgNetworkInfos.size();
            for (int i = 0; i < csgNetworkInfoSize; i++) {
                ret.add(new HwHisiCsgNetworkInfo(((CsgNetworkInfo) csgNetworkInfos.get(i)).plmn, ((CsgNetworkInfo) csgNetworkInfos.get(i)).csgId, ((CsgNetworkInfo) csgNetworkInfos.get(i)).networkRat));
            }
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDone(rr, responseInfo, ret);
        }
    }

    public void deactivateDataCallEmergencyResponse(RadioResponseInfo responseInfo) {
        Rlog.d(this.mTag, "deactivateDataCallEmergencyResponse");
        deactivateDataCallResponse(responseInfo);
    }

    public void setupDataCallEmergencyResponse(RadioResponseInfo responseInfo, SetupDataCallResult setupDataCallResult) {
        Rlog.d(this.mTag, "setupDataCallEmergencyResponse");
        setupDataCallResponse(responseInfo, setupDataCallResult);
    }

    public void getCellInfoListOtdoaResponse(RadioResponseInfo responseInfo, ArrayList<CellInfo> cellInfo) {
        getCellInfoListResponse(responseInfo, cellInfo);
    }
}
