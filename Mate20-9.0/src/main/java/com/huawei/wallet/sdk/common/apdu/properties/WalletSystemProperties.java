package com.huawei.wallet.sdk.common.apdu.properties;

import com.huawei.wallet.sdk.common.log.LogC;
import java.util.Properties;

public class WalletSystemProperties {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile WalletSystemProperties instance;
    private Properties appProperties = null;
    private Properties moduleProperties = null;

    public static WalletSystemProperties getInstance() {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new WalletSystemProperties();
                }
            }
        }
        return instance;
    }

    public String getProperty(String key, String defaultValue) {
        if (this.appProperties != null && this.appProperties.containsKey(key)) {
            return this.appProperties.getProperty(key);
        }
        if (this.moduleProperties == null || !this.moduleProperties.containsKey(key)) {
            return defaultValue;
        }
        return this.moduleProperties.getProperty(key);
    }

    public boolean containsProperty(String key) {
        if (key == null) {
            LogC.e("key is null", false);
            return false;
        } else if (this.appProperties != null && this.appProperties.containsKey(key)) {
            return true;
        } else {
            if (this.moduleProperties == null || !this.moduleProperties.containsKey(key)) {
                return false;
            }
            return true;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return "True".equalsIgnoreCase(getProperty(key, String.valueOf(defaultValue)));
    }

    public void setAppProperties(Properties appProperties2) {
        this.appProperties = appProperties2;
    }

    public void setModuleProperties(Properties moduleProperties2) {
        this.moduleProperties = moduleProperties2;
    }
}
