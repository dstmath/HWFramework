package com.android.internal.os;

import java.util.HashMap;

public interface IHwPowerProfileManager {
    boolean readHwPowerValuesFromXml(HashMap<String, Double> hashMap, HashMap<String, Double[]> hashMap2);
}
