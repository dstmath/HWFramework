package com.huawei.chr;

import java.util.ArrayList;
import vendor.huawei.hardware.radio_radar.V1_0.IRadioIndicationRadar;

public class RadioIndicationRadarEx {
    private IRadioIndicationRadar radioIndicationRadar = new IRadioIndicationRadar.Stub() {
        /* class com.huawei.chr.RadioIndicationRadarEx.AnonymousClass1 */

        public void radarInd(int var1, ArrayList<Byte> var2) {
            RadioIndicationRadarEx.this.radarInd(var1, var2);
        }

        public void rilUnsolChrMsgInd(int var1, ArrayList<Integer> var2) {
            RadioIndicationRadarEx.this.rilUnsolChrMsgInd(var1, var2);
        }
    };

    /* access modifiers changed from: package-private */
    public IRadioIndicationRadar getRadioIndicationRadar() {
        return this.radioIndicationRadar;
    }

    public void radarInd(int type, ArrayList<Byte> arrayList) {
    }

    public void rilUnsolChrMsgInd(int type, ArrayList<Integer> arrayList) {
    }
}
