package com.android.server.wifi;

import android.os.SystemProperties;

/* access modifiers changed from: package-private */
public class SystemPropertyService implements PropertyService {
    SystemPropertyService() {
    }

    @Override // com.android.server.wifi.PropertyService
    public String get(String key, String defaultValue) {
        return SystemProperties.get(key, defaultValue);
    }

    @Override // com.android.server.wifi.PropertyService
    public void set(String key, String val) {
        SystemProperties.set(key, val);
    }

    @Override // com.android.server.wifi.PropertyService
    public boolean getBoolean(String key, boolean defaultValue) {
        return SystemProperties.getBoolean(key, defaultValue);
    }

    @Override // com.android.server.wifi.PropertyService
    public String getString(String key, String defaultValue) {
        return SystemProperties.get(key, defaultValue);
    }
}
