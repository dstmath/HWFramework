package com.huawei.chr;

import vendor.huawei.hardware.radio_radar.V1_0.RadioResponseInfo;

public class RadioResponseInfoEx {
    private RadioResponseInfo radioResponseInfo;

    RadioResponseInfoEx(RadioResponseInfo radioResponseInfo2) {
        this.radioResponseInfo = radioResponseInfo2;
    }

    RadioResponseInfoEx(vendor.huawei.hardware.radio.chr.V1_0.RadioResponseInfo radioResponseInfo2) {
        this.radioResponseInfo = new RadioResponseInfo();
        this.radioResponseInfo.type = radioResponseInfo2.type;
        this.radioResponseInfo.serial = radioResponseInfo2.serial;
        this.radioResponseInfo.error = radioResponseInfo2.error;
    }

    public int getType() {
        return this.radioResponseInfo.type;
    }

    public int getSerial() {
        return this.radioResponseInfo.serial;
    }

    public int getError() {
        return this.radioResponseInfo.error;
    }
}
