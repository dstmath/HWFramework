package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.NeighboringCellSsbInfos;
import android.telephony.NrCellSsbId;
import android.telephony.Rlog;
import android.telephony.SsbIdInfos;
import com.android.internal.telephony.uicc.IccIoResult;
import java.util.ArrayList;
import java.util.Iterator;
import vendor.huawei.hardware.radio.V2_0.IccIoResultEx;
import vendor.huawei.hardware.radio.V2_0.RILPreferredPLMNSelector;
import vendor.huawei.hardware.radio.V2_0.RadioResponseInfo;
import vendor.huawei.hardware.radio.V2_0.RspMsgPayload;
import vendor.huawei.hardware.radio.V2_1.HwDataRegStateResult_2_1;
import vendor.huawei.hardware.radio.V2_1.HwSignalStrength_2_1;
import vendor.huawei.hardware.radio.V2_1.IRadioResponse;
import vendor.huawei.hardware.radio.V2_1.NeighboringCellSsbInfo;
import vendor.huawei.hardware.radio.V2_1.NrCellSsbIds;
import vendor.huawei.hardware.radio.V2_1.SsbIdInfo;

public class HwCommonRadioResponse extends IRadioResponse.Stub {
    private static final String LOG_TAG = "HwCommonRadioResponse";
    private static final int NETWORK_INFO_LENGTH = 6;
    private RIL mRil;

    HwCommonRadioResponse(RIL ril) {
        this.mRil = ril;
    }

    public void RspMsg(RadioResponseInfo responseInfo, int msgId, RspMsgPayload payload) {
        Object ret = null;
        boolean validMsgId = true;
        if (payload == null) {
            logi("got null payload msgId = " + msgId);
            return;
        }
        switch (msgId) {
            case 517:
            case 521:
            case 534:
            case 536:
            case 537:
            case 541:
            case 547:
            case 548:
            case 553:
            case 602:
            case 635:
            case 650:
            case 654:
            case 657:
            case 728:
                break;
            case 528:
                ret = processString(payload);
                break;
            case 551:
                ret = processInts(payload);
                break;
            case 568:
                ret = processString(payload);
                break;
            case 600:
                ret = processInts(payload);
                break;
            case 601:
                ret = responseNetworkInfoWithActs(payload);
                break;
            case 651:
                ret = processInts(payload);
                break;
            case 663:
                ret = processInts(payload);
                break;
            case 690:
                ret = HwHisiRadioIndication.convertHwHalSignalStrength((int[]) processInts(payload), this.mRil.mPhoneId.intValue());
                break;
            default:
                validMsgId = false;
                break;
        }
        if (validMsgId) {
            if (responseInfo.error != 0) {
                ret = null;
            }
            processRadioResponse(responseInfo, ret);
        }
    }

    private Object processInts(RspMsgPayload payload) {
        int numInts = payload.nDatas.size();
        int[] response = new int[numInts];
        for (int i = 0; i < numInts; i++) {
            response[i] = ((Integer) payload.nDatas.get(i)).intValue();
        }
        return response;
    }

    private Object processString(RspMsgPayload payload) {
        return payload.strData;
    }

    private void logd(String msg) {
        Rlog.d("HwCommonRadioResponse[" + this.mRil.mPhoneId + "]", msg);
    }

    private void logi(String msg) {
        Rlog.i("HwCommonRadioResponse[" + this.mRil.mPhoneId + "]", msg);
    }

    public void getPolListResponse(RadioResponseInfo responseInfo, RILPreferredPLMNSelector preferredplmnselector) {
    }

    private void processRadioResponse(RadioResponseInfo responseInfo, Object ret) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, ret);
        }
    }

    private static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, (Throwable) null);
            msg.sendToTarget();
        }
    }

    private Object responseNetworkInfoWithActs(RspMsgPayload payload) {
        int arrayLength = payload.nDatas.size() * 6;
        int[] response = new int[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            response[i] = ((Integer) payload.nDatas.get(i)).intValue();
        }
        return response;
    }

    private void responseIccIoEx(RadioResponseInfo responseInfo, IccIoResultEx result) {
        RILRequest rr = this.mRil.processResponseEx(responseInfo);
        if (rr != null) {
            IccIoResult ret = new IccIoResult(result.isValid, result.fileId, result.sw1, result.sw2, result.simResponse);
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mRil.processResponseDoneEx(rr, responseInfo, ret);
        }
    }

    public void getSimMatchedFileFromRilCacheResponse(RadioResponseInfo responseInfo, IccIoResultEx iccIo) {
        logi("getSimMatchedFileFromRilCacheResponse: IccIoResultEx.");
        responseIccIoEx(responseInfo, iccIo);
    }

    public void setHwNrSaStateResponse(RadioResponseInfo info) {
        logi("setHwNrSaStateResponse");
        processRadioResponse(info, null);
    }

    public void getHwNrSaStateResponse(RadioResponseInfo info, int on) {
        logi("getHwNrSaStateResponse");
        processRadioResponse(info, Integer.valueOf(on));
    }

    public void getHwNrSsbInfoResponse(RadioResponseInfo info, NrCellSsbIds ssbIds) {
        logi("getHwNrSsbInfoResponse");
        responseHwNrSsbInfo(info, ssbIds);
    }

    public void getHwRrcConnectionStateResponse(RadioResponseInfo info, int state) {
        logi("getHwRrcConnectionStateResponse");
        processRadioResponse(info, Integer.valueOf(state));
    }

    public void setHwNrOptionModeResponse(RadioResponseInfo info) {
        logi("setHwNrOptionModeResponse");
        processRadioResponse(info, null);
    }

    public void getHwNrOptionModeResponse(RadioResponseInfo info, int mode) {
        logi("getHwNrOptionModeResponse");
        processRadioResponse(info, Integer.valueOf(mode));
    }

    public void getHwDataRegistrationStateResponse_2_1(RadioResponseInfo info, HwDataRegStateResult_2_1 dataRegResponse) {
        logi("getHwDataRegistrationStateResponse_2_1");
        processRadioResponse(info, dataRegResponse);
    }

    public void setTemperatureControlResponse(RadioResponseInfo info) {
        logi("setTemperatureControlResponse");
        processRadioResponse(info, null);
    }

    public void getHwSignalStrengthResponse_2_1(RadioResponseInfo info, HwSignalStrength_2_1 signalStrength) {
        logi("convertHalSignalStrength_2_1");
        RIL ril = this.mRil;
        processRadioResponse(info, ril.convertHalSignalStrength_2_1(signalStrength, ril.mPhoneId.intValue()));
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
}
