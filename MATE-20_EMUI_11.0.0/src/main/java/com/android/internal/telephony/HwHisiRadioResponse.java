package com.android.internal.telephony;

import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Message;
import android.telephony.NeighboringCellSsbInfos;
import android.telephony.NrCellSsbId;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SsbIdInfos;
import android.telephony.UiccAuthResponse;
import android.telephony.data.ApnSetting;
import android.telephony.data.DataCallResponse;
import android.text.TextUtils;
import com.android.internal.telephony.uicc.HwIccUtils;
import com.android.internal.telephony.uicc.IccUtils;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
import vendor.huawei.hardware.hisiradio.V1_1.LteAttachInfo;
import vendor.huawei.hardware.hisiradio.V1_2.HwCellInfo_1_2;
import vendor.huawei.hardware.hisiradio.V1_2.HwDataRegStateResult13;
import vendor.huawei.hardware.hisiradio.V1_2.HwDataRegStateResult_1_2;
import vendor.huawei.hardware.hisiradio.V1_2.HwVoiceRegStateResult_1_2;
import vendor.huawei.hardware.hisiradio.V1_3.IHisiRadioResponse;
import vendor.huawei.hardware.hisiradio.V1_3.NeighboringCellSsbInfo;
import vendor.huawei.hardware.hisiradio.V1_3.NrCellSsbIds;
import vendor.huawei.hardware.hisiradio.V1_3.SsbIdInfo;

public class HwHisiRadioResponse extends IHisiRadioResponse.Stub {
    private static final int DEVICE_VERSION_LENGTH = 11;
    private static final int IMS_DOMAIN_ARRAY_LENGTH = 1;
    private static final int INET_ADDRESS_IPV4 = 32;
    private static final int INET_ADDRESS_IPV6 = 64;
    private static final String LOG_TAG = "HwHisiRadioResponse";
    private static final int SYSTEM_INFO_EX_LENGTH = 7;
    private static final int TRAFFIC_DATA_LENGTH = 6;
    HwHisiRIL mRil;

    public HwHisiRadioResponse() {
    }

    public HwHisiRadioResponse(HwHisiRIL ril) {
        this.mRil = ril;
    }

    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, (Throwable) null);
            msg.sendToTarget();
        }
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
        Rlog.d("HwHisiRadioResponse ", "converInfoToString not match.");
        return "";
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
            logi("got null payload msgId = " + msgId);
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
        if (!(msgId == 670 || msgId == 706 || msgId == 715 || msgId == 717)) {
            if (msgId == 721) {
                ret = processInts(payload);
            } else if (!(msgId == 732 || msgId == 711 || msgId == 712 || msgId == 735 || msgId == 736)) {
                validMsgId = false;
                this.mRil.processResponseDoneEx(this.mRil.processResponseEx(responseInfo), responseInfo, null);
                logi("got invalid msgId = " + msgId);
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
            logi("getDeviceVersionResponse: deviceVersion is null.");
        } else {
            processRadioResponse(responseInfo, new String[]{deviceVersion.buildTime, deviceVersion.externalSwVersion, deviceVersion.internalSwVersion, deviceVersion.externalDbVersion, deviceVersion.internalDbVersion, deviceVersion.externalHwVersion, deviceVersion.internalHwVersion, deviceVersion.externalDutName, deviceVersion.internalDutName, deviceVersion.configurateVersion, deviceVersion.prlVersion});
        }
    }

    public void getDsFlowInfoResponse(RadioResponseInfo responseInfo, RILDsFlowInfoResponse dsFlowInfo) {
        if (dsFlowInfo == null) {
            logi("getDsFlowInfoResponse: dsFlowInfo is null.");
        } else {
            processRadioResponse(responseInfo, new String[]{dsFlowInfo.lastDsTime, dsFlowInfo.lastTxFlow, dsFlowInfo.lastRxFlow, dsFlowInfo.totalDsTime, dsFlowInfo.totalTxFlow, dsFlowInfo.totalRxFlow});
        }
    }

    public void getSystemInfoExResponse(RadioResponseInfo responseInfo, RILRADIOSYSINFO sysInfo) {
        if (sysInfo == null) {
            logi("getSystemInfoExResponse: sysInfo is null.");
        } else {
            processRadioResponse(responseInfo, new int[]{sysInfo.sysSubmode, sysInfo.srvStatus, sysInfo.srvDomain, sysInfo.roamStatus, sysInfo.sysMode, sysInfo.simState, sysInfo.lockState});
        }
    }

    public void getAvailableCsgIdsResponse(RadioResponseInfo responseInfo, ArrayList<CsgNetworkInfo> csgNetworkInfos) {
        responseCsgNetworkInfos(responseInfo, csgNetworkInfos);
    }

    public void manualSelectionCsgIdResponse(RadioResponseInfo responseInfo) {
        processRadioResponse(responseInfo, null);
    }

    private void responseCsgNetworkInfos(RadioResponseInfo responseInfo, ArrayList<CsgNetworkInfo> csgNetworkInfos) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        logi("Response of AvailableCsgIds rr: " + rr + "error: " + responseInfo.error + " csgNetworkInfos: " + csgNetworkInfos.size());
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
        logi("deactivateDataCallEmergencyResponse");
        deactivateDataCallResponse(responseInfo);
    }

    public void setupDataCallEmergencyResponse(RadioResponseInfo responseInfo, SetupDataCallResult setupDataCallResult) {
        logi("setupDataCallEmergencyResponse");
        processRadioResponse(responseInfo, convertToRadioSetupDataCallResponse(setupDataCallResult));
    }

    private DataCallResponse convertToRadioSetupDataCallResponse(SetupDataCallResult result) {
        if (result == null) {
            return null;
        }
        String[] addresses = null;
        String[] dnses = null;
        String[] gateways = null;
        String[] pcscfs = null;
        if (!TextUtils.isEmpty(result.addresses)) {
            addresses = result.addresses.split("\\s+");
        }
        if (!TextUtils.isEmpty(result.dnses)) {
            dnses = result.dnses.split("\\s+");
        }
        if (!TextUtils.isEmpty(result.gateways)) {
            gateways = result.gateways.split("\\s+");
        }
        if (!TextUtils.isEmpty(result.pcscf)) {
            pcscfs = result.pcscf.split("\\s+");
        }
        List<LinkAddress> linkAddressList = parseLinkAddressResponse(addresses);
        List<InetAddress> dnsList = parseDataCallResultResponse(dnses);
        List<InetAddress> gatewayList = parseDataCallResultResponse(gateways);
        List<InetAddress> pcscfList = parseDataCallResultResponse(pcscfs);
        return new DataCallResponse(result.status, result.suggestedRetryTime, result.cid, result.active, ApnSetting.getProtocolIntFromString(result.type), result.ifname, linkAddressList, dnsList, gatewayList, pcscfList, result.mtu);
    }

    private List<LinkAddress> parseLinkAddressResponse(String[] parseLinkAddressInfos) {
        LinkAddress linkAddress;
        List<LinkAddress> linkAddressList = new ArrayList<>();
        if (parseLinkAddressInfos == null) {
            return linkAddressList;
        }
        for (String address : parseLinkAddressInfos) {
            String address2 = address.trim();
            if (!address2.isEmpty()) {
                try {
                    if (address2.split("/").length == 2) {
                        linkAddress = new LinkAddress(address2);
                    } else {
                        InetAddress inetAddress = NetworkUtils.numericToInetAddress(address2);
                        linkAddress = new LinkAddress(inetAddress, inetAddress instanceof Inet4Address ? INET_ADDRESS_IPV4 : INET_ADDRESS_IPV6);
                    }
                    linkAddressList.add(linkAddress);
                } catch (IllegalArgumentException e) {
                    Rlog.e(LOG_TAG, "Unknown LinkAddress information. ");
                }
            }
        }
        return linkAddressList;
    }

    private List<InetAddress> parseDataCallResultResponse(String[] parseInetAddressInfos) {
        List<InetAddress> inetAddressList = new ArrayList<>();
        if (parseInetAddressInfos != null) {
            for (String addressInfo : parseInetAddressInfos) {
                try {
                    inetAddressList.add(NetworkUtils.numericToInetAddress(addressInfo.trim()));
                } catch (IllegalArgumentException e) {
                    Rlog.e(LOG_TAG, "Unknown InetAddress information. ");
                }
            }
        }
        return inetAddressList;
    }

    public void getCellInfoListOtdoaResponse(RadioResponseInfo responseInfo, ArrayList<CellInfo> cellInfo) {
        getCellInfoListResponse(responseInfo, cellInfo);
    }

    public void getAvailableCsgIdsResponse_1_1(RadioResponseInfo responseInfo, ArrayList<CsgNetworkInfo_1_1> csgNetworkInfos) {
        logi("csg...getAvailableCsgIdsResponse_1_1");
        responseExtendersCsgNetworkInfos(responseInfo, csgNetworkInfos);
    }

    private void responseExtendersCsgNetworkInfos(RadioResponseInfo responseInfo, ArrayList<CsgNetworkInfo_1_1> csgNetworkInfos) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        logi("Response of AvailableCsgIds rr: " + rr + "error: " + responseInfo.error + " csgNetworkInfos: " + csgNetworkInfos.size());
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
        return new HwHisiCsgNetworkInfo(csginfo.plmn, csginfo.csgId, csginfo.networkRat, csginfo.csgId_type, csginfo.csgId_name, csginfo.longName, csginfo.shortName, csginfo.rsrp, csginfo.rsrq, csginfo.isConnected, converInfoToString(csginfo.csgType));
    }

    public void getCurrentCallsResponseHwV1_2(RadioResponseInfo responseInfo, ArrayList<HwCall_V1_2> calls) {
        responseCurrentCallsEx(responseInfo, calls);
    }

    private void responseCurrentCallsEx(RadioResponseInfo responseInfo, ArrayList<HwCall_V1_2> calls) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            int num = calls.size();
            ArrayList<DriverCall> dcCalls = new ArrayList<>(num);
            addDcCalls(dcCalls, num, calls);
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

    private void addDcCalls(ArrayList<DriverCall> dcCalls, int num, ArrayList<HwCall_V1_2> calls) {
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
                this.mRil.riljLogv(String.format("Incoming UUS : type=%d, dcs=%d, length=%d", Integer.valueOf(dc.uusInfo.getType()), Integer.valueOf(dc.uusInfo.getDcs()), Integer.valueOf(dc.uusInfo.getUserData().length)));
                HwHisiRIL hwHisiRIL = this.mRil;
                hwHisiRIL.riljLogv("Incoming UUS : data (hex): " + IccUtils.bytesToHexString(dc.uusInfo.getUserData()));
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
    }

    public void uiccAuthResponse(RadioResponseInfo responseInfo, RILUICCAUTHRESPONSE uiccAuthRst) {
        responseUiccAuth(responseInfo, uiccAuthRst);
    }

    private void responseUiccAuth(RadioResponseInfo responseInfo, RILUICCAUTHRESPONSE uiccAuthRst) {
        logi("Response of RIL_REQUEST_HW_VOWIFI_UICC_AUTH");
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            Object ret = null;
            if (responseInfo.error == 0) {
                UiccAuthResponse uiccResponse = new UiccAuthResponse();
                uiccResponse.mResult = uiccAuthRst.authStatus;
                logi("responseUiccAuth, mStatus=" + uiccResponse.mResult);
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
        logi("sendSimChgTypeInfoResponse");
        responseVoid(responseInfo);
    }

    public void setUlfreqEnableResponse(RadioResponseInfo info) {
        logi("setUplinkfreqEnableResponse received");
        responseVoid(info);
    }

    public void getCardTrayInfoResponse(RadioResponseInfo responseInfo, ArrayList<Byte> cardTrayInfo) {
        if (cardTrayInfo == null) {
            Rlog.e(LOG_TAG, "getCardTrayInfoResponse: cardTrayInfo is null.");
            processRadioResponse(responseInfo, null);
            return;
        }
        processRadioResponse(responseInfo, RIL.arrayListToPrimitiveArray(cardTrayInfo));
    }

    public void getNvcfgMatchedResultResponse(RadioResponseInfo responseInfo, String nvcfgName) {
        logi("getNvcfgMatchedResultResponse");
        processRadioResponse(responseInfo, nvcfgName);
    }

    public void getCapOfRecPseBaseStationResponse(RadioResponseInfo responseInfo) {
        logi("getCapOfRecPseBaseStationResponse ");
        responseVoid(responseInfo);
    }

    private void responseLteAttachInfo(RadioResponseInfo responseInfo, LteAttachInfo attachInfo) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            if (responseInfo.error == 0) {
                Rlog.d("responseLteAttachInfo", "responseLteAttachInfo, sendMessageResponse. apn name is:" + attachInfo.apn);
                Bundle bundle = new Bundle();
                bundle.putString("apn", attachInfo.apn);
                bundle.putInt("protocol", attachInfo.protocol);
                sendMessageResponse(rr.mResult, bundle);
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, attachInfo);
        }
    }

    public void getLteAttachInfoResponse(RadioResponseInfo info, LteAttachInfo attachInfo) {
        Rlog.d("HwHisiRadioResponse::", "getLteAttachInfoResponse ");
        responseLteAttachInfo(info, attachInfo);
    }

    public void getHwPreferredNetworkTypeResponse_1_1(RadioResponseInfo responseInfo, int nwType) {
        logi("getHwPreferredNetworkTypeResponse_1_1");
        this.mRil.mPreferredNetworkType = nwType;
        processRadioResponse(responseInfo, new int[]{nwType});
    }

    public void getHwSignalStrengthResponse_1_1(RadioResponseInfo responseInfo, HwSignalStrength_1_1 signalStrength) {
        logi("getHwSignalStrengthResponse_1_1");
        HwHisiRIL hwHisiRIL = this.mRil;
        processRadioResponse(responseInfo, hwHisiRIL.convertHalSignalStrength_1_1(signalStrength, hwHisiRIL.mPhoneId.intValue()));
    }

    public void getVoiceRegistrationStateResponse_1_1(RadioResponseInfo responseInfo, HwVoiceRegStateResult_1_1 voiceRegResponse) {
        logi("getVoiceRegistrationStateResponse_1_1, don't use it in Q.");
        processRadioResponse(responseInfo, voiceRegResponse);
    }

    public void getHwVoiceRegistrationStateResponse_1_2(RadioResponseInfo responseInfo, HwVoiceRegStateResult_1_2 voiceRegResponse) {
        logi("getHwVoiceRegistrationStateResponse_1_2");
    }

    public void getDataRegistrationStateResponse_1_1(RadioResponseInfo responseInfo, HwDataRegStateResult_1_1 dataRegResponse) {
        processRadioResponse(responseInfo, dataRegResponse);
        logi("getDataRegistrationStateResponse_1_1, don't use it in Q.");
    }

    public void getHwDataRegistrationStateResponse_1_2(RadioResponseInfo responseInfo, HwDataRegStateResult_1_2 dataRegResponse) {
        logi("getDataRegistrationStateResponse_1_2");
    }

    public void setTemperatureControlResponse(RadioResponseInfo responseInfo) {
        logi("setTemperatureControlResponse");
        responseVoid(responseInfo);
    }

    public void getImsDomainResponseV1_1(RadioResponseInfo info, int imsDomain) {
        logi("getImsDomainResponseV1_1::imsDomain=" + imsDomain);
        processRadioResponse(info, new int[]{imsDomain});
    }

    public void getHwCellInfoListResponse_1_2(RadioResponseInfo responseInfo, ArrayList<HwCellInfo_1_2> arrayList) {
        logi("getHwCellInfoListResponse_1_2, don't use it in Q.");
        processRadioResponse(responseInfo, new ArrayList());
    }

    public void getHwDataRegistrationStateResponse13(RadioResponseInfo info, HwDataRegStateResult13 dataRegResponse) {
        logi("getHwDataRegistrationStateResponse13");
        processRadioResponse(info, dataRegResponse);
    }

    public void setHwNrOptionModeResponse(RadioResponseInfo responseInfo) {
        logi("setHwNrOptionModeResponse");
        responseVoid(responseInfo);
    }

    public void getHwNrOptionModeResponse(RadioResponseInfo responseInfo, int mode) {
        logi("getHwNrOptionModeResponse");
        processRadioResponse(responseInfo, Integer.valueOf(mode));
    }

    public void setHwNrSaStateResponse(RadioResponseInfo responseInfo) {
        logi("setHwNrSaStateResponse");
        responseVoid(responseInfo);
    }

    public void getHwNrSaStateResponse(RadioResponseInfo responseInfo, int on) {
        logi("getHwNrSaStateResponse");
        processRadioResponse(responseInfo, Integer.valueOf(on));
    }

    public void sendMutiChipSessionConfigResponse(RadioResponseInfo responseInfo) {
        logi("activateHwVsimCaResponse");
        responseVoid(responseInfo);
    }

    public void sendVsimDataToModemResponse(RadioResponseInfo responseInfo) {
        logi("sendVsimDataToModemResponse");
        responseVoid(responseInfo);
    }

    public void getHwNrSsbInfoResponse(RadioResponseInfo responseInfo, NrCellSsbIds ssbIds) {
        logi("getHwNrSsbInfoResponse");
        responseHwNrSsbInfo(responseInfo, ssbIds);
    }

    private void responseHwNrSsbInfo(RadioResponseInfo responseInfo, NrCellSsbIds ssbIds) {
        logi("Response of RIL_REQUEST_GET_NR_CELL_SSBID");
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            Object ret = null;
            if (responseInfo.error == 0) {
                logi("NO ERROR,start to process NrSsbInfo");
                ArrayList<SsbIdInfos> rpSsbCellSsbList = new ArrayList<>(ssbIds.sCellSsbList.size());
                Iterator it = ssbIds.sCellSsbList.iterator();
                while (it.hasNext()) {
                    SsbIdInfo ssbId = (SsbIdInfo) it.next();
                    rpSsbCellSsbList.add(new SsbIdInfos(ssbId.ssbId, ssbId.rsrp));
                }
                ArrayList<NeighboringCellSsbInfos> rpNbCellSsbList = new ArrayList<>(ssbIds.nbCellSsbList.size());
                Iterator it2 = ssbIds.nbCellSsbList.iterator();
                while (it2.hasNext()) {
                    NeighboringCellSsbInfo nbCellInfo = (NeighboringCellSsbInfo) it2.next();
                    ArrayList<SsbIdInfos> rpSsbIdList = new ArrayList<>(nbCellInfo.ssbIdList.size());
                    Iterator it3 = nbCellInfo.ssbIdList.iterator();
                    while (it3.hasNext()) {
                        SsbIdInfo nbSsbId = (SsbIdInfo) it3.next();
                        rpSsbIdList.add(new SsbIdInfos(nbSsbId.ssbId, nbSsbId.rsrp));
                    }
                    rpNbCellSsbList.add(new NeighboringCellSsbInfos(nbCellInfo.pci, nbCellInfo.arfcn, nbCellInfo.rsrp, nbCellInfo.sinr, rpSsbIdList));
                }
                Object rpNrCellSsbId = new NrCellSsbId(ssbIds.arfcn, ssbIds.cid, ssbIds.pci, ssbIds.rsrp, ssbIds.sinr, ssbIds.timingAdvance, rpSsbCellSsbList, ssbIds.nbCellCount, rpNbCellSsbList);
                logi("responseHwNrSsbInfo done the NrCellSsbId is: " + rpNrCellSsbId);
                sendMessageResponse(rr.mResult, rpNrCellSsbId);
                ret = rpNrCellSsbId;
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, ret);
        }
    }

    public void processSmsAntiAttackResponse(RadioResponseInfo responseInfo, ArrayList<Integer> response) {
        logi("processSmsAntiAttackResponse");
        processRadioResponse(responseInfo, response);
    }

    private void logi(String msg) {
        Rlog.i("HwHisiRadioResponse[" + this.mRil.mPhoneId + "]", msg);
    }

    public void getHwRrcConnectionStateResponse(RadioResponseInfo responseInfo, int state) {
        Rlog.i(LOG_TAG, "getHwRrcConnectionStateResponse: " + state);
        processRadioResponse(responseInfo, Integer.valueOf(state));
    }

    public void setVonrSwitchResponse(RadioResponseInfo responseInfo) {
        logi("setVonrSwitchResponse");
        responseVoid(responseInfo);
    }
}
