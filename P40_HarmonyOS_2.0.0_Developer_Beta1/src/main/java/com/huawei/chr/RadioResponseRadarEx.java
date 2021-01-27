package com.huawei.chr;

import java.util.ArrayList;
import vendor.huawei.hardware.radio_radar.V1_0.IRadioResponseRadar;
import vendor.huawei.hardware.radio_radar.V1_0.RadioResponseInfo;

public class RadioResponseRadarEx {
    private IRadioResponseRadar radioResponseRadar = new IRadioResponseRadar.Stub() {
        /* class com.huawei.chr.RadioResponseRadarEx.AnonymousClass1 */

        public void sendResponse(RadioResponseInfo info, ArrayList<Byte> data) {
            RadioResponseRadarEx.this.sendResponse(new RadioResponseInfoEx(info), data);
        }
    };

    /* access modifiers changed from: package-private */
    public IRadioResponseRadar getRadioResponseRadar() {
        return this.radioResponseRadar;
    }

    public void sendResponse(RadioResponseInfoEx info, ArrayList<Byte> arrayList) {
    }
}
