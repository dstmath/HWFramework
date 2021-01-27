package com.android.internal.telephony;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.uicc.HwIccUtils;
import vendor.huawei.hardware.qcomradio.V1_0.IQcomRadioResponse;
import vendor.huawei.hardware.qcomradio.V1_0.RadioResponseInfo;
import vendor.huawei.hardware.qcomradio.V1_0.RspMsgPayload;

public class HwQualcommRadioResponse extends IQcomRadioResponse.Stub {
    private static final String TAG = "HwQualcommRadioResponse";
    private HwQualcommRIL mQcomRil;

    public HwQualcommRadioResponse(RIL ril) {
        this.mQcomRil = (HwQualcommRIL) ril;
    }

    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, (Throwable) null);
            msg.sendToTarget();
        }
    }

    public void RspMsg(RadioResponseInfo responseInfo, int msgId, RspMsgPayload payload) {
        Object ret = null;
        if (payload == null) {
            HwQualcommRIL hwQualcommRIL = this.mQcomRil;
            hwQualcommRIL.riljLog("got null payload msgId = " + msgId);
            return;
        }
        HwQualcommRIL hwQualcommRIL2 = this.mQcomRil;
        hwQualcommRIL2.riljLog("RspMsg msgId = " + msgId);
        if (msgId == 611) {
            ret = responseICCID(payload.strData);
        } else if (msgId != 690) {
            HwQualcommRIL hwQualcommRIL3 = this.mQcomRil;
            hwQualcommRIL3.riljLog("got invalid msgId = " + msgId);
        } else if (processInts(payload) instanceof int[]) {
            ret = HwQualcommRadioIndication.convertHwHalSignalStrength((int[]) processInts(payload));
        }
        if (1 != 0) {
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

    private void processRadioResponse(RadioResponseInfo responseInfo, Object ret) {
        RILRequest rr = this.mQcomRil.processResponseEx(responseInfo);
        if (rr != null) {
            if (responseInfo.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mQcomRil.processResponseDoneEx(rr, responseInfo, ret);
            return;
        }
        this.mQcomRil.qcomRiljLoge("processRadioResponse, rr is null");
    }
}
