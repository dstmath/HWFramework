package com.huawei.chr;

import vendor.huawei.hardware.radio.chr.V1_0.IRadioChrIndication;
import vendor.huawei.hardware.radio.chr.V1_0.RILUnsolMsgPayload;

public class RadioChrIndicationEx {
    private IRadioChrIndication radioChrIndication = new IRadioChrIndication.Stub() {
        /* class com.huawei.chr.RadioChrIndicationEx.AnonymousClass1 */

        public void UnsolMsg(int indicationType, int msgId, RILUnsolMsgPayload rilUnsolMsgPayload) {
            RadioChrIndicationEx.this.onUnsolMsg(indicationType, msgId, new RilUnsolMsgPayloadEx(rilUnsolMsgPayload));
        }
    };

    public void onUnsolMsg(int indicationType, int msgId, RilUnsolMsgPayloadEx rilUnsolMsgPayload) {
    }

    /* access modifiers changed from: package-private */
    public IRadioChrIndication getRadioChrIndication() {
        return this.radioChrIndication;
    }
}
