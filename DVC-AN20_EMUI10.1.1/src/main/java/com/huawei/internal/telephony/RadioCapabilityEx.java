package com.huawei.internal.telephony;

import com.android.internal.telephony.RadioCapability;

public class RadioCapabilityEx {
    private RadioCapability mRadioCapability;

    public void setRadioCapability(RadioCapability radioCapability) {
        this.mRadioCapability = radioCapability;
    }

    public RadioCapability getRadioCapability() {
        return this.mRadioCapability;
    }

    public int getRadioAccessFamily() {
        RadioCapability radioCapability = this.mRadioCapability;
        if (radioCapability != null) {
            return radioCapability.getRadioAccessFamily();
        }
        return 0;
    }

    public String getLogicalModemUuid() {
        RadioCapability radioCapability = this.mRadioCapability;
        if (radioCapability != null) {
            return radioCapability.getLogicalModemUuid();
        }
        return null;
    }
}
