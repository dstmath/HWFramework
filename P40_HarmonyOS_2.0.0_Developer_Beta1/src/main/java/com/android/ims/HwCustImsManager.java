package com.android.ims;

import android.content.Context;

public class HwCustImsManager {
    public HwCustImsManager(Context context, int phoneId) {
    }

    public boolean shouldNotTurnOnImsForCust() {
        return false;
    }

    public boolean shouldNotTurnOffImsForCust() {
        return false;
    }

    public boolean isImsTurnOffAllowedForCust() {
        return false;
    }

    public void changeMmTelCapWithWfcForCust(boolean turnOn) {
    }

    public boolean isForceSetLteFeature() {
        return false;
    }
}
