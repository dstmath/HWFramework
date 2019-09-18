package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import android.telephony.Rlog;
import com.android.internal.telephony.uicc.IccIoResult;
import vendor.huawei.hardware.radio.V2_0.IRadioResponse;
import vendor.huawei.hardware.radio.V2_0.IccIoResultEx;
import vendor.huawei.hardware.radio.V2_0.RILPreferredPLMNSelector;
import vendor.huawei.hardware.radio.V2_0.RadioResponseInfo;
import vendor.huawei.hardware.radio.V2_0.RspMsgPayload;

public class HwCommonRadioResponse extends IRadioResponse.Stub {
    private static final int NETWORK_INFO_LENGTH = 6;
    private RIL mRil;
    private String mTag = ("HwCommonRadioResponse[" + this.mRil.mPhoneId + "]");

    HwCommonRadioResponse(RIL ril) {
        this.mRil = ril;
    }

    public void RspMsg(RadioResponseInfo responseInfo, int msgId, RspMsgPayload payload) {
        Object ret = null;
        boolean validMsgId = true;
        if (payload == null) {
            logd("got null payload msgId = " + msgId);
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
        Rlog.d(this.mTag, msg);
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
            AsyncResult.forMessage(msg, ret, null);
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
        Rlog.d(this.mTag, "getSimMatchedFileFromRilCacheResponse: IccIoResultEx.");
        responseIccIoEx(responseInfo, iccIo);
    }
}
