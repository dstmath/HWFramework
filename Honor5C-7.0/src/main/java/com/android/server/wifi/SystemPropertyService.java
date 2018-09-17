package com.android.server.wifi;

import android.os.SystemProperties;

class SystemPropertyService implements PropertyService {
    SystemPropertyService() {
    }

    public String get(String key, String defaultValue) {
        return SystemProperties.get(key, defaultValue);
    }

    public void set(String key, String val) {
        SystemProperties.set(key, val);
    }
}
