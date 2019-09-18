package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.UiccAuthResponse;
import android.text.TextUtils;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.android.internal.telephony.uicc.IccUtils;
import java.util.ArrayList;
import java.util.Collections;
import vendor.huawei.hardware.hisiradio.V1_0.CellInfo;
import vendor.huawei.hardware.hisiradio.V1_0.CsgNetworkInfo;
import vendor.huawei.hardware.hisiradio.V1_0.CsgNetworkInfo_1_1;
import vendor.huawei.hardware.hisiradio.V1_0.HwCall_V1_2;
import vendor.huawei.hardware.hisiradio.V1_0.RILDeviceVersionResponse;
import vendor.huawei.hardware.hisiradio.V1_0.RILDsFlowInfoResponse;
import vendor.huawei.hardware.hisiradio.V1_0.RILRADIOSYSINFO;
import vendor.huawei.hardware.hisiradio.V1_0.RILUICCAUTHAUTSTYPE;
import vendor.huawei.hardware.hisiradio.V1_0.RILUICCAUTHCKTYPE;
import vendor.huawei.hardware.hisiradio.V1_0.RILUICCAUTHIKTYPE;
import vendor.huawei.hardware.hisiradio.V1_0.RILUICCAUTHRESPCHALLENGETYPE;
import vendor.huawei.hardware.hisiradio.V1_0.RILUICCAUTHRESPONSE;
import vendor.huawei.hardware.hisiradio.V1_0.RILUICCAUTHRESTYPE;
import vendor.huawei.hardware.hisiradio.V1_0.RadioResponseInfo;
import vendor.huawei.hardware.hisiradio.V1_0.RspMsgPayload;
import vendor.huawei.hardware.hisiradio.V1_0.SetupDataCallResult;
import vendor.huawei.hardware.hisiradio.V1_0.UusInfo;
import vendor.huawei.hardware.hisiradio.V1_1.HwDataRegStateResult_1_1;
import vendor.huawei.hardware.hisiradio.V1_1.HwSignalStrength_1_1;
import vendor.huawei.hardware.hisiradio.V1_1.HwVoiceRegStateResult_1_1;
import vendor.huawei.hardware.hisiradio.V1_1.IHisiRadioResponse;
import vendor.huawei.hardware.hisiradio.V1_1.LteAttachInfo;

public class HwHisiRadioResponse extends IHisiRadioResponse.Stub {
    private static final int DEVICE_VERSION_LENGTH = 11;
    private static final int SYSTEM_INFO_EX_LENGTH = 7;
    private static final int TRAFFIC_DATA_LENGTH = 6;
    HwHisiRIL mRil;
    private String mTag = null;

    public HwHisiRadioResponse() {
    }

    public HwHisiRadioResponse(HwHisiRIL ril) {
        this.mRil = ril;
        this.mTag = getClass().getSimpleName();
    }

    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, null);
            msg.sendToTarget();
        }
    }

    public void processRadioResponse(RadioResponseInfo responseInfo, Object ret) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, ret);
        }
    }

    public void RspMsg(RadioResponseInfo responseInfo, int msgId, RspMsgPayload payload) {
        Object ret = null;
        if (payload == null) {
            String str = this.mTag;
            Rlog.d(str, "got null payload msgId = " + msgId);
            return;
        }
        switch (msgId) {
            case 527:
                ret = processInts(payload);
                break;
            case 532:
                ret = processInts(payload);
                break;
            case 533:
                ret = processStrings(payload);
                break;
            case 555:
            case 558:
            case 564:
            case 604:
            case 626:
            case 630:
            case 644:
            case 646:
            case 648:
            case 655:
            case 656:
            case 660:
            case 661:
            case 666:
            case 696:
                break;
            case 565:
                ret = processInts(payload);
                break;
            case 573:
                ret = processInts(payload);
                break;
            case 574:
                ret = processInts(payload);
                break;
            case 578:
                ret = processString(payload);
                break;
            case 611:
                ret = responseICCID(payload.strData);
                break;
            case 623:
                ret = processInts(payload);
                break;
            case 624:
                ret = processInts(payload);
                break;
            case 645:
                ret = processInts(payload);
                break;
            case 647:
                ret = processInts(payload);
                break;
            case 662:
                ret = processInts(payload);
                break;
            case 665:
                ret = processStrings(payload);
                break;
            case 667:
                ret = processInts(payload);
                break;
            case 687:
                ret = processInts(payload);
                break;
            case 689:
                ret = processInts(payload);
                break;
            case 701:
                ret = processInts(payload);
                break;
            default:
                extendRspMsg(responseInfo, msgId, payload, true);
                return;
        }
        if (1 != 0) {
            if (responseInfo.error != 0) {
                ret = null;
            }
            processRadioResponse(responseInfo, ret);
        }
    }

    public void extendRspMsg(RadioResponseInfo responseInfo, int msgId, RspMsgPayload payload, boolean validMsgId) {
        Object ret = null;
        if (!(msgId == 670 || msgId == 715 || msgId == 717)) {
            if (msgId != 721) {
                if (msgId != 732) {
                    switch (msgId) {
                        case 711:
                        case 712:
                            break;
                        default:
                            validMsgId = false;
                            this.mRil.processResponseDoneEx(this.mRil.processResponseEx(responseInfo), responseInfo, null);
                            String str = this.mTag;
                            Rlog.d(str, "got invalid msgId = " + msgId);
                            break;
                    }
                }
            } else {
                ret = processInts(payload);
            }
        }
        if (validMsgId) {
            if (responseInfo.error != 0) {
                ret = null;
            }
            processRadioResponse(responseInfo, ret);
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

    private void responseVoid(RadioResponseInfo responseInfo) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, null);
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, null);
        }
    }

    public void deactivateDataCallResponse(RadioResponseInfo responseInfo) {
        responseVoid(responseInfo);
    }

    public void getCellInfoListResponse(RadioResponseInfo responseInfo, ArrayList<CellInfo> cellInfo) {
        responseCellInfoList(responseInfo, cellInfo);
    }

    private void responseCellInfoList(RadioResponseInfo responseInfo, ArrayList<CellInfo> cellInfo) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            ArrayList<android.telephony.CellInfo> ret = HwHisiRIL.convertHalCellInfoListEx(cellInfo);
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, ret);
        }
    }

    private void responseSetupDataCall(RadioResponseInfo responseInfo, SetupDataCallResult setupDataCallResult) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, setupDataCallResult);
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, setupDataCallResult);
        }
    }

    public void setupDataCallResponse(RadioResponseInfo responseInfo, SetupDataCallResult setupDataCallResult) {
        responseSetupDataCall(responseInfo, setupDataCallResult);
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

    public void getSystemInfoExResponse(RadioResponseInfo responseInfo, RILRADIOSYSINFO sysInfo) {
        if (sysInfo == null) {
            Rlog.d(this.mTag, "getSystemInfoExResponse: sysInfo is null.");
            return;
        }
        processRadioResponse(responseInfo, new int[]{sysInfo.sysSubmode, sysInfo.srvStatus, sysInfo.srvDomain, sysInfo.roamStatus, sysInfo.sysMode, sysInfo.simState, sysInfo.lockState});
    }

    public void getAvailableCsgIdsResponse(RadioResponseInfo responseInfo, ArrayList<CsgNetworkInfo> csgNetworkInfos) {
        responseCsgNetworkInfos(responseInfo, csgNetworkInfos);
    }

    public void manualSelectionCsgIdResponse(RadioResponseInfo responseInfo) {
        processRadioResponse(responseInfo, null);
    }

    private void responseCsgNetworkInfos(RadioResponseInfo responseInfo, ArrayList<CsgNetworkInfo> csgNetworkInfos) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        String str = this.mTag;
        Rlog.d(str, "Response of AvailableCsgIds rr: " + rr + "error: " + responseInfo.error + " csgNetworkInfos: " + csgNetworkInfos.size());
        if (rr != null) {
            ArrayList<HwHisiCsgNetworkInfo> ret = new ArrayList<>();
            int csgNetworkInfoSize = csgNetworkInfos.size();
            for (int i = 0; i < csgNetworkInfoSize; i++) {
                ret.add(new HwHisiCsgNetworkInfo(csgNetworkInfos.get(i).plmn, csgNetworkInfos.get(i).csgId, csgNetworkInfos.get(i).networkRat));
            }
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, ret);
        }
    }

    public void deactivateDataCallEmergencyResponse(RadioResponseInfo responseInfo) {
        Rlog.d(this.mTag, "deactivateDataCallEmergencyResponse");
        deactivateDataCallResponse(responseInfo);
    }

    public void setupDataCallEmergencyResponse(RadioResponseInfo responseInfo, SetupDataCallResult setupDataCallResult) {
        Rlog.d(this.mTag, "setupDataCallEmergencyResponse");
        processRadioResponse(responseInfo, convertToRadioSetupDataCallResult(setupDataCallResult));
    }

    private android.hardware.radio.V1_0.SetupDataCallResult convertToRadioSetupDataCallResult(SetupDataCallResult setupDataCallResult) {
        android.hardware.radio.V1_0.SetupDataCallResult ret = new android.hardware.radio.V1_0.SetupDataCallResult();
        ret.status = setupDataCallResult.status;
        ret.suggestedRetryTime = setupDataCallResult.suggestedRetryTime;
        ret.cid = setupDataCallResult.cid;
        ret.active = setupDataCallResult.active;
        ret.type = setupDataCallResult.type;
        ret.ifname = setupDataCallResult.ifname;
        ret.addresses = setupDataCallResult.addresses;
        ret.dnses = setupDataCallResult.dnses;
        ret.gateways = setupDataCallResult.gateways;
        ret.pcscf = setupDataCallResult.pcscf;
        ret.mtu = setupDataCallResult.mtu;
        return ret;
    }

    public void getCellInfoListOtdoaResponse(RadioResponseInfo responseInfo, ArrayList<CellInfo> cellInfo) {
        getCellInfoListResponse(responseInfo, cellInfo);
    }

    public void getAvailableCsgIdsResponse_1_1(RadioResponseInfo responseInfo, ArrayList<CsgNetworkInfo_1_1> csgNetworkInfos) {
        Rlog.d(this.mTag, "csg...getAvailableCsgIdsResponse_1_1");
        responseExtendersCsgNetworkInfos(responseInfo, csgNetworkInfos);
    }

    private void responseExtendersCsgNetworkInfos(RadioResponseInfo responseInfo, ArrayList<CsgNetworkInfo_1_1> csgNetworkInfos) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        String str = this.mTag;
        Rlog.d(str, "Response of AvailableCsgIds rr: " + rr + "error: " + responseInfo.error + " csgNetworkInfos: " + csgNetworkInfos.size());
        if (rr != null) {
            ArrayList<HwHisiCsgNetworkInfo> ret = new ArrayList<>();
            int csgNetworkInfoSize = csgNetworkInfos.size();
            for (int i = 0; i < csgNetworkInfoSize; i++) {
                ret.add(getmHwHisiCsgNetworkInfo(csgNetworkInfos.get(i)));
            }
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, ret);
        }
    }

    private HwHisiCsgNetworkInfo getmHwHisiCsgNetworkInfo(CsgNetworkInfo_1_1 csginfo) {
        HwHisiCsgNetworkInfo mcsgNetInfo = new HwHisiCsgNetworkInfo(csginfo.plmn, csginfo.csgId, csginfo.networkRat, csginfo.csgId_type, csginfo.csgId_name, csginfo.longName, csginfo.shortName, csginfo.rsrp, csginfo.rsrq, csginfo.isConnected, converInfoToString(csginfo.csgType));
        return mcsgNetInfo;
    }

    private static String converInfoToString(int state) {
        if (state == 1) {
            return "allow_list";
        }
        if (state == 2) {
            return "operator_list";
        }
        if (state == 3) {
            return "forbiden_list";
        }
        if (state == 4) {
            return "unallow_list";
        }
        return "";
    }

    public void getCurrentCallsResponseHwV1_2(RadioResponseInfo responseInfo, ArrayList<HwCall_V1_2> calls) {
        responseCurrentCallsEx(responseInfo, calls);
    }

    private void responseCurrentCallsEx(RadioResponseInfo responseInfo, ArrayList<HwCall_V1_2> calls) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            int num = calls.size();
            ArrayList<DriverCall> dcCalls = new ArrayList<>(num);
            for (int i = 0; i < num; i++) {
                DriverCall dc = new DriverCall();
                dc.state = DriverCall.stateFromCLCC(calls.get(i).call.state);
                dc.index = calls.get(i).call.index;
                dc.TOA = calls.get(i).call.toa;
                dc.isMpty = calls.get(i).call.isMpty;
                dc.isMT = calls.get(i).call.isMT;
                dc.als = calls.get(i).call.als;
                dc.isVoice = calls.get(i).call.isVoice;
                dc.isVoicePrivacy = calls.get(i).call.isVoicePrivacy;
                dc.number = calls.get(i).call.number;
                dc.numberPresentation = DriverCall.presentationFromCLIP(calls.get(i).call.numberPresentation);
                dc.name = calls.get(i).call.name;
                dc.namePresentation = DriverCall.presentationFromCLIP(calls.get(i).call.namePresentation);
                if (calls.get(i).call.uusInfo.size() == 1) {
                    dc.uusInfo = new UUSInfo();
                    dc.uusInfo.setType(((UusInfo) calls.get(i).call.uusInfo.get(0)).uusType);
                    dc.uusInfo.setDcs(((UusInfo) calls.get(i).call.uusInfo.get(0)).uusDcs);
                    if (!TextUtils.isEmpty(((UusInfo) calls.get(i).call.uusInfo.get(0)).uusData)) {
                        dc.uusInfo.setUserData(((UusInfo) calls.get(i).call.uusInfo.get(0)).uusData.getBytes());
                    } else {
                        this.mRil.riljLog("responseCurrentCallsEx: uusInfo data is null or empty");
                    }
                    this.mRil.riljLogv(String.format("Incoming UUS : type=%d, dcs=%d, length=%d", new Object[]{Integer.valueOf(dc.uusInfo.getType()), Integer.valueOf(dc.uusInfo.getDcs()), Integer.valueOf(dc.uusInfo.getUserData().length)}));
                    this.mRil.riljLogv("Incoming UUS : data (hex): " + IccUtils.bytesToHexString(dc.uusInfo.getUserData()));
                } else {
                    this.mRil.riljLogv("Incoming UUS : NOT present!");
                }
                dc.number = PhoneNumberUtils.stringFromStringAndTOA(dc.number, dc.TOA);
                dc.redirectNumber = calls.get(i).redirectNumber;
                dc.redirectNumberTOA = calls.get(i).redirectNumberToa;
                dc.redirectNumberPresentation = DriverCall.presentationFromCLIP(calls.get(i).redirectNumberPresentation);
                dc.redirectNumber = PhoneNumberUtils.stringFromStringAndTOA(dc.redirectNumber, dc.redirectNumberTOA);
                dcCalls.add(dc);
                if (dc.isVoicePrivacy) {
                    this.mRil.mVoicePrivacyOnRegistrants.notifyRegistrants();
                    this.mRil.riljLog("InCall VoicePrivacy is enabled");
                } else {
                    this.mRil.mVoicePrivacyOffRegistrants.notifyRegistrants();
                    this.mRil.riljLog("InCall VoicePrivacy is disabled");
                }
            }
            Collections.sort(dcCalls);
            if (num == 0 && this.mRil.mTestingEmergencyCall.getAndSet(false) && this.mRil.mEmergencyCallbackModeRegistrant != null) {
                this.mRil.riljLog("responseCurrentCalls: call ended, testing emergency call, notify ECM Registrants");
                this.mRil.mEmergencyCallbackModeRegistrant.notifyRegistrant();
            }
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, dcCalls);
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, dcCalls);
        }
    }

    public void uiccAuthResponse(RadioResponseInfo responseInfo, RILUICCAUTHRESPONSE uiccAuthRst) {
        responseUiccAuth(responseInfo, uiccAuthRst);
    }

    private void responseUiccAuth(RadioResponseInfo responseInfo, RILUICCAUTHRESPONSE uiccAuthRst) {
        Rlog.d(this.mTag, "Response of RIL_REQUEST_HW_VOWIFI_UICC_AUTH");
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            Object ret = null;
            if (responseInfo.error == 0) {
                Rlog.d(this.mTag, "NO ERROR,start to process GbaAuth");
                UiccAuthResponse uiccResponse = new UiccAuthResponse();
                uiccResponse.mResult = uiccAuthRst.authStatus;
                String str = this.mTag;
                Rlog.d(str, "responseUiccAuth, mStatus=" + uiccResponse.mResult);
                if (uiccResponse.mResult == 0) {
                    RILUICCAUTHRESPCHALLENGETYPE authChang = uiccAuthRst.authChallenge;
                    RILUICCAUTHRESTYPE resDatas = authChang.resData;
                    uiccResponse.mUiccAuthChallenge.mResData = new UiccAuthResponse.UiccAuthResponseData();
                    uiccResponse.mUiccAuthChallenge.mResData.present = resDatas.resPresent;
                    uiccResponse.mUiccAuthChallenge.mResData.data = IccUtils.hexStringToBytes(resDatas.res);
                    uiccResponse.mUiccAuthChallenge.mResData.len = uiccResponse.mUiccAuthChallenge.mResData.data.length;
                    RILUICCAUTHIKTYPE ikType = authChang.ikData;
                    uiccResponse.mUiccAuthChallenge.mIkData = new UiccAuthResponse.UiccAuthResponseData();
                    uiccResponse.mUiccAuthChallenge.mIkData.present = ikType.ikPresent;
                    uiccResponse.mUiccAuthChallenge.mIkData.data = IccUtils.hexStringToBytes(ikType.ik);
                    uiccResponse.mUiccAuthChallenge.mIkData.len = uiccResponse.mUiccAuthChallenge.mIkData.data.length;
                    RILUICCAUTHCKTYPE ckDatas = authChang.ckData;
                    uiccResponse.mUiccAuthChallenge.mCkData = new UiccAuthResponse.UiccAuthResponseData();
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
            this.mRil.processResponseDoneEx(rr, responseInfo, ret);
        }
    }

    public void sendSimChgTypeInfoResponse(RadioResponseInfo responseInfo) {
        Rlog.d(this.mTag, "sendSimChgTypeInfoResponse");
        responseVoid(responseInfo);
    }

    public void setUlfreqEnableResponse(RadioResponseInfo info) {
        Rlog.d(this.mTag, "setUplinkfreqEnableResponse received");
        responseVoid(info);
    }

    public void getCardTrayInfoResponse(RadioResponseInfo responseInfo, ArrayList<Byte> cardTrayInfo) {
        if (cardTrayInfo == null) {
            Rlog.e(this.mTag, "getCardTrayInfoResponse: cardTrayInfo is null.");
            processRadioResponse(responseInfo, null);
            return;
        }
        processRadioResponse(responseInfo, RIL.arrayListToPrimitiveArray(cardTrayInfo));
    }

    public void getNvcfgMatchedResultResponse(RadioResponseInfo responseInfo, String nvcfgName) {
        Rlog.d(this.mTag, "getNvcfgMatchedResultResponse");
        processRadioResponse(responseInfo, nvcfgName);
    }

    public void getCapOfRecPseBaseStationResponse(RadioResponseInfo responseInfo) {
        Rlog.d(this.mTag, "getCapOfRecPseBaseStationResponse ");
        responseVoid(responseInfo);
    }

    private void responseLteAttachInfo(RadioResponseInfo responseInfo, LteAttachInfo attachInfo) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            if (responseInfo.error == 0) {
                Rlog.d("responseLteAttachInfo", "responseLteAttachInfo, sendMessageResponse. apn name is:" + attachInfo.apn);
                sendMessageResponse(rr.mResult, attachInfo);
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, attachInfo);
        }
    }

    public void getLteAttachInfoResponse(RadioResponseInfo info, LteAttachInfo attachInfo) {
        Rlog.d("HwHisiRadioResponse::", "getLteAttachInfoResponse ");
        responseLteAttachInfo(info, attachInfo);
    }

    public void getHwPreferredNetworkTypeResponse_1_1(RadioResponseInfo responseInfo, int nwType) {
        Rlog.d(this.mTag, "getHwPreferredNetworkTypeResponse_1_1");
        this.mRil.mPreferredNetworkType = nwType;
        processRadioResponse(responseInfo, new int[]{nwType});
    }

    public void getHwSignalStrengthResponse_1_1(RadioResponseInfo responseInfo, HwSignalStrength_1_1 signalStrength) {
        Rlog.d(this.mTag, "getHwSignalStrengthResponse_1_1");
        processRadioResponse(responseInfo, this.mRil.convertHalSignalStrength_1_1(signalStrength, this.mRil.mPhoneId.intValue()));
    }

    public void getVoiceRegistrationStateResponse_1_1(RadioResponseInfo responseInfo, HwVoiceRegStateResult_1_1 voiceRegResponse) {
        Rlog.d(this.mTag, "getVoiceRegistrationStateResponse_1_1");
        processRadioResponse(responseInfo, voiceRegResponse);
    }

    public void getDataRegistrationStateResponse_1_1(RadioResponseInfo responseInfo, HwDataRegStateResult_1_1 dataRegResponse) {
        Rlog.d(this.mTag, "getDataRegistrationStateResponse_1_1");
        processRadioResponse(responseInfo, dataRegResponse);
    }

    public void setTemperatureControlResponse(RadioResponseInfo responseInfo) {
        Rlog.d(this.mTag, "setTemperatureControlResponse");
        responseVoid(responseInfo);
    }
}
