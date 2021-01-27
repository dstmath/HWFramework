package com.huawei.pluginmanager;

import android.content.Context;

public interface IPluginEntry {
    int getMinEmuiSdkVersion();

    int getMinPluginSdkVersion();

    int getTargetEmuiSdkVersion();

    int getTargetPluginSdkVersion();

    IPlugin loadPlugin(Context context);

    int releasePlugin();
}
