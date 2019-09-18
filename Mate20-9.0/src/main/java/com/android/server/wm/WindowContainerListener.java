package com.android.server.wm;

public interface WindowContainerListener {
    void registerConfigurationChangeListener(ConfigurationContainerListener configurationContainerListener);

    void unregisterConfigurationChangeListener(ConfigurationContainerListener configurationContainerListener);
}
