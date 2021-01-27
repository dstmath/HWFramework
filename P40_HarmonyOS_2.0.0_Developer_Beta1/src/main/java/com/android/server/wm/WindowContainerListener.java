package com.android.server.wm;

import android.content.res.Configuration;

public interface WindowContainerListener {
    void registerConfigurationChangeListener(ConfigurationContainerListener configurationContainerListener);

    void unregisterConfigurationChangeListener(ConfigurationContainerListener configurationContainerListener);

    default void onInitializeOverrideConfiguration(Configuration config) {
    }
}
