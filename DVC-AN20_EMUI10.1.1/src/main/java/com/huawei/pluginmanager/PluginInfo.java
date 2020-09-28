package com.huawei.pluginmanager;

import android.content.Context;
import android.os.Bundle;

public final class PluginInfo {
    private Bundle mBundle = null;
    private final Object mLock = new Object();
    private final int mMinEmuiSdkVersion;
    private final int mMinPluginSdkVersion;
    private final IPlugin mPlugin;
    private final Context mPluginContext;
    private final IPluginEntry mPluginEntry;
    private final String mSplitName;
    private final int mTargetEmuiSdkVersion;
    private final int mTargetPluginSdkVersion;

    PluginInfo(Context pluginContext, String splitName, IPluginEntry pluginEntry, IPlugin plugin) {
        this.mPluginContext = pluginContext;
        this.mSplitName = splitName;
        this.mPluginEntry = pluginEntry;
        this.mPlugin = plugin;
        this.mMinEmuiSdkVersion = pluginEntry.getMinEmuiSdkVersion();
        this.mTargetEmuiSdkVersion = pluginEntry.getTargetEmuiSdkVersion();
        this.mMinPluginSdkVersion = pluginEntry.getMinPluginSdkVersion();
        this.mTargetPluginSdkVersion = pluginEntry.getTargetPluginSdkVersion();
    }

    public String getSplitName() {
        return this.mSplitName;
    }

    public int getMinEmuiSdkVersion() {
        return this.mMinEmuiSdkVersion;
    }

    public int getTargetEmuiSdkVersion() {
        return this.mTargetEmuiSdkVersion;
    }

    public int getMinPluginSdkVersion() {
        return this.mMinPluginSdkVersion;
    }

    public int getTargetPluginSdkVersion() {
        return this.mTargetPluginSdkVersion;
    }

    public Context getPluginContext() {
        return this.mPluginContext;
    }

    public IPlugin getPlugin() {
        return this.mPlugin;
    }

    public Bundle getBundle() {
        Bundle bundle;
        synchronized (this.mLock) {
            if (this.mBundle == null) {
                this.mBundle = new Bundle();
            }
            bundle = this.mBundle;
        }
        return bundle;
    }

    public String toString() {
        return "PluginInfo{" + Integer.toHexString(System.identityHashCode(this)) + " " + this.mSplitName + ", minEmuiSdkVersion:" + this.mMinEmuiSdkVersion + ", targetEmuiSdkVersion:" + this.mTargetEmuiSdkVersion + ", minPluginSdkVersion:" + this.mMinPluginSdkVersion + ", targetPluginSdkVersion:" + this.mTargetPluginSdkVersion + "}";
    }

    /* access modifiers changed from: package-private */
    public IPluginEntry getPluginEntry() {
        return this.mPluginEntry;
    }
}
