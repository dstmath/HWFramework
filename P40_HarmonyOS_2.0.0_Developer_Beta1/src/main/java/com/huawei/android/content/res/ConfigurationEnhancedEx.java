package com.huawei.android.content.res;

import android.content.res.Configuration;

public class ConfigurationEnhancedEx {
    public static Configuration generateDelta(Configuration base, Configuration change) {
        return Configuration.generateDelta(base, change);
    }
}
