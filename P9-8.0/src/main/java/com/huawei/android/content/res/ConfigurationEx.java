package com.huawei.android.content.res;

import android.content.res.Configuration;

public final class ConfigurationEx {
    public static final int SIMPLEUIMODE_YES = 2;
    private Configuration configuration = new Configuration();

    public ConfigurationEx(Configuration configuration) {
        this.configuration = configuration;
    }

    public android.content.res.ConfigurationEx getExtraConfig() {
        return (android.content.res.ConfigurationEx) this.configuration.extraConfig;
    }

    public int getSimpleuiMode() {
        android.content.res.ConfigurationEx config = getExtraConfig();
        return config != null ? config.simpleuiMode : -1;
    }

    public void setSimpleuiMode(int launcherType) {
        android.content.res.ConfigurationEx config = getExtraConfig();
        if (config != null) {
            config.simpleuiMode = launcherType;
        }
    }

    public int getExtraConfigTheme() {
        android.content.res.ConfigurationEx config = getExtraConfig();
        if (config == null) {
            return 0;
        }
        return config.hwtheme;
    }
}
