package com.huawei.android.content.res;

import android.content.res.Configuration;

public final class ConfigurationEx {
    public static final int SIMPLEUIMODE_DRAWER = 4;
    public static final int SIMPLEUIMODE_NO = 1;
    public static final int SIMPLEUIMODE_YES = 2;
    public static final int THEME_MODE = 1;
    private Configuration configuration = new Configuration();

    public ConfigurationEx(Configuration configuration2) {
        this.configuration = configuration2;
    }

    public android.content.res.ConfigurationEx getExtraConfig() {
        return this.configuration.extraConfig;
    }

    public int getSimpleuiMode() {
        android.content.res.ConfigurationEx config = getExtraConfig();
        if (config != null) {
            return config.simpleuiMode;
        }
        return -1;
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
