package com.android.systemui.shared.plugins;

import android.content.Context;
import android.os.Looper;

public interface PluginInitializer {
    Looper getBgLooper();

    PluginEnabler getPluginEnabler(Context context);

    String[] getWhitelistedPlugins(Context context);

    void handleWtfs();

    void onPluginManagerInit();
}
