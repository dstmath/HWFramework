package com.android.internal.os;

import java.util.HashMap;

public class HwPowerProfileManagerDummy implements IHwPowerProfileManager {
    private static IHwPowerProfileManager mHwPowerProfileManager = null;

    public static IHwPowerProfileManager getDefault() {
        if (mHwPowerProfileManager == null) {
            mHwPowerProfileManager = new HwPowerProfileManagerDummy();
        }
        return mHwPowerProfileManager;
    }

    @Override // com.android.internal.os.IHwPowerProfileManager
    public boolean readHwPowerValuesFromXml(HashMap<String, Double> hashMap, HashMap<String, Double[]> hashMap2) {
        return false;
    }
}
