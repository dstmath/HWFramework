package com.huawei.android.content.res;

import android.content.res.Configuration;

public final class ConfigurationEx {
    private Configuration configuration;

    public ConfigurationEx(Configuration configuration) {
        this.configuration = new Configuration();
        this.configuration = configuration;
    }

    public android.content.res.ConfigurationEx getExtraConfig() {
        return (android.content.res.ConfigurationEx) this.configuration.extraConfig;
    }
}
