package com.android.server.wm;

import android.content.res.Configuration;

public interface ConfigurationContainerListener {
    void onRequestedOverrideConfigurationChanged(Configuration configuration);
}
