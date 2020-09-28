package com.huawei.android.content.res;

import android.content.res.Configuration;
import android.content.res.IHwConfiguration;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ConfigurationAdapter {
    private ConfigurationAdapter() {
    }

    public static IHwConfiguration getExtraConfig(Configuration configuration) {
        return configuration.extraConfig;
    }

    public static int getNonFullScreen(Configuration configuration) {
        return configuration.nonFullScreen;
    }
}
