package com.android.server.am;

import android.content.res.Configuration;

abstract class ConfigurationContainer<E extends ConfigurationContainer> {
    private Configuration mFullConfiguration = new Configuration();
    private Configuration mMergedOverrideConfiguration = new Configuration();
    private Configuration mOverrideConfiguration = new Configuration();

    protected abstract E getChildAt(int i);

    protected abstract int getChildCount();

    protected abstract ConfigurationContainer getParent();

    ConfigurationContainer() {
    }

    Configuration getConfiguration() {
        return this.mFullConfiguration;
    }

    void onConfigurationChanged(Configuration newParentConfig) {
        this.mFullConfiguration.setTo(newParentConfig);
        this.mFullConfiguration.updateFrom(this.mOverrideConfiguration);
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).onConfigurationChanged(this.mFullConfiguration);
        }
    }

    Configuration getOverrideConfiguration() {
        return this.mOverrideConfiguration;
    }

    void onOverrideConfigurationChanged(Configuration overrideConfiguration) {
        this.mOverrideConfiguration.setTo(overrideConfiguration);
        ConfigurationContainer parent = getParent();
        onConfigurationChanged(parent != null ? parent.getConfiguration() : Configuration.EMPTY);
        onMergedOverrideConfigurationChanged();
    }

    Configuration getMergedOverrideConfiguration() {
        return this.mMergedOverrideConfiguration;
    }

    private void onMergedOverrideConfigurationChanged() {
        ConfigurationContainer parent = getParent();
        if (parent != null) {
            this.mMergedOverrideConfiguration.setTo(parent.getMergedOverrideConfiguration());
            this.mMergedOverrideConfiguration.updateFrom(this.mOverrideConfiguration);
        } else {
            this.mMergedOverrideConfiguration.setTo(this.mOverrideConfiguration);
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).onMergedOverrideConfigurationChanged();
        }
    }

    void onParentChanged() {
        ConfigurationContainer parent = getParent();
        if (parent != null) {
            onConfigurationChanged(parent.mFullConfiguration);
            onMergedOverrideConfigurationChanged();
        }
    }
}
