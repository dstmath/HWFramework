package com.android.internal.os;

import java.util.HashMap;

public class HwPowerProfileManagerDummy implements IHwPowerProfileManager {
    private static IHwPowerProfileManager sHwPowerProfileManager = null;

    public static IHwPowerProfileManager getDefault() {
        if (sHwPowerProfileManager == null) {
            sHwPowerProfileManager = new HwPowerProfileManagerDummy();
        }
        return sHwPowerProfileManager;
    }

    @Override // com.android.internal.os.IHwPowerProfileManager
    public boolean readHwPowerValuesFromXml(HashMap<String, Double> hashMap, HashMap<String, Double[]> hashMap2) {
        return false;
    }
}
