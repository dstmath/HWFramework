package com.android.server.hidata.appqoe;

import android.net.wifi.ScanResult;
import java.util.List;

public class HwAppQoeResourceManager {
    private static HwAppQoeResourceManager sHwAppQoeResourceManager = null;
    private HwAppQoeResourceManagerImpl mHwAppQoeResourceManagerImpl;

    private HwAppQoeResourceManager() {
        this.mHwAppQoeResourceManagerImpl = null;
        this.mHwAppQoeResourceManagerImpl = new HwAppQoeResourceManagerImpl();
    }

    protected static HwAppQoeResourceManager createHwAppQoeResourceManager() {
        if (sHwAppQoeResourceManager == null) {
            sHwAppQoeResourceManager = new HwAppQoeResourceManager();
        }
        return sHwAppQoeResourceManager;
    }

    public static synchronized HwAppQoeResourceManager getInstance() {
        HwAppQoeResourceManager hwAppQoeResourceManager;
        synchronized (HwAppQoeResourceManager.class) {
            if (sHwAppQoeResourceManager == null) {
                sHwAppQoeResourceManager = new HwAppQoeResourceManager();
            }
            hwAppQoeResourceManager = sHwAppQoeResourceManager;
        }
        return hwAppQoeResourceManager;
    }

    public void onConfigFilePathChanged() {
        this.mHwAppQoeResourceManagerImpl.onConfigFilePathChanged();
    }

    public HwAppQoeApkConfig checkIsMonitorApkScenes(String packageName, String className) {
        return this.mHwAppQoeResourceManagerImpl.checkIsMonitorApkScenes(packageName, className);
    }

    public HwAppQoeApkConfig checkIsMonitorVideoScenes(String packageName, String className) {
        return this.mHwAppQoeResourceManagerImpl.checkIsMonitorVideoScenes(packageName, className);
    }

    public HwAppQoeGameConfig checkIsMonitorGameScenes(String packageName) {
        return this.mHwAppQoeResourceManagerImpl.checkIsMonitorGameScenes(packageName);
    }

    public HwAppQoeGameConfig getGameScenesConfig(int appId) {
        return this.mHwAppQoeResourceManagerImpl.getGameScenesConfig(appId);
    }

    public boolean isInBlackListScenes(String packageName) {
        return this.mHwAppQoeResourceManagerImpl.isInBlackListScenes(packageName);
    }

    public boolean isInWhiteListScenes(String packageName) {
        return this.mHwAppQoeResourceManagerImpl.isInWhiteListScenes(packageName);
    }

    public boolean isInRouterBlackList(ScanResult.InformationElement ie) {
        return this.mHwAppQoeResourceManagerImpl.isInRouterBlackList(ie);
    }

    public int getScenesAction(int appType, int appId, int scenesId) {
        return this.mHwAppQoeResourceManagerImpl.getScenesAction(appType, appId, scenesId);
    }

    public HwAppQoeApkConfig getApkScenesConfig(int scenesId) {
        return this.mHwAppQoeResourceManagerImpl.getApkScenesConfig(scenesId);
    }

    public HwPowerParameterConfig getPowerParameterConfig(String powerParameter) {
        return this.mHwAppQoeResourceManagerImpl.getPowerParameterConfig(powerParameter);
    }

    public List<HwPowerParameterConfig> getPowerParameterConfigList() {
        return this.mHwAppQoeResourceManagerImpl.getPowerParameterConfigList();
    }

    public List<HwAppQoeApkConfig> getAPKConfigList() {
        return this.mHwAppQoeResourceManagerImpl.getApkConfigList();
    }

    public List<HwAppQoeGameConfig> getGameConfigList() {
        return this.mHwAppQoeResourceManagerImpl.getGameConfigList();
    }
}
