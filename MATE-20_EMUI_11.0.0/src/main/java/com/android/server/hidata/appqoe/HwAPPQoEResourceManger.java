package com.android.server.hidata.appqoe;

import android.net.wifi.ScanResult;
import java.util.List;

public class HwAPPQoEResourceManger {
    private static final String TAG = "HwAPPQoEResourceManger";
    private static HwAPPQoEResourceManger mHwAPPQoEResourceManger = null;
    private HwAPPQoEResourceMangerImpl mHwAPPQoEResourceMangerImpl;

    private HwAPPQoEResourceManger() {
        this.mHwAPPQoEResourceMangerImpl = null;
        this.mHwAPPQoEResourceMangerImpl = new HwAPPQoEResourceMangerImpl();
    }

    protected static HwAPPQoEResourceManger createHwAPPQoEResourceManger() {
        if (mHwAPPQoEResourceManger == null) {
            mHwAPPQoEResourceManger = new HwAPPQoEResourceManger();
        }
        return mHwAPPQoEResourceManger;
    }

    public static synchronized HwAPPQoEResourceManger getInstance() {
        HwAPPQoEResourceManger hwAPPQoEResourceManger;
        synchronized (HwAPPQoEResourceManger.class) {
            if (mHwAPPQoEResourceManger == null) {
                mHwAPPQoEResourceManger = new HwAPPQoEResourceManger();
            }
            hwAPPQoEResourceManger = mHwAPPQoEResourceManger;
        }
        return hwAPPQoEResourceManger;
    }

    public void onConfigFilePathChanged() {
        this.mHwAPPQoEResourceMangerImpl.onConfigFilePathChanged();
    }

    public HwAPPQoEAPKConfig checkIsMonitorAPKScence(String packageName, String className) {
        return this.mHwAPPQoEResourceMangerImpl.checkIsMonitorAPKScence(packageName, className);
    }

    public HwAPPQoEAPKConfig checkIsMonitorVideoScence(String packageName, String className) {
        return this.mHwAPPQoEResourceMangerImpl.checkIsMonitorVideoScence(packageName, className);
    }

    public HwAPPQoEGameConfig checkIsMonitorGameScence(String packageName) {
        return this.mHwAPPQoEResourceMangerImpl.checkIsMonitorGameScence(packageName);
    }

    public HwAPPQoEGameConfig getGameScenceConfig(int appId) {
        return this.mHwAPPQoEResourceMangerImpl.getGameScenceConfig(appId);
    }

    public boolean isInBlackListScene(String packageName) {
        return this.mHwAPPQoEResourceMangerImpl.isInBlackListScene(packageName);
    }

    public boolean isInWhiteListScene(String packageName) {
        return this.mHwAPPQoEResourceMangerImpl.isInWhiteListScene(packageName);
    }

    public boolean isInRouterBlackList(ScanResult.InformationElement ie) {
        return this.mHwAPPQoEResourceMangerImpl.isInRouterBlackList(ie);
    }

    public int getScenceAction(int appType, int appId, int scenceId) {
        return this.mHwAPPQoEResourceMangerImpl.getScenceAction(appType, appId, scenceId);
    }

    public HwAPPQoEAPKConfig getAPKScenceConfig(int scenceId) {
        return this.mHwAPPQoEResourceMangerImpl.getAPKScenceConfig(scenceId);
    }

    public HwPowerParameterConfig getPowerParameterConfig(String powerParameter) {
        return this.mHwAPPQoEResourceMangerImpl.getPowerParameterConfig(powerParameter);
    }

    public List<HwPowerParameterConfig> getPowerParameterConfigList() {
        return this.mHwAPPQoEResourceMangerImpl.getPowerParameterConfigList();
    }

    public List<HwAPPQoEAPKConfig> getAPKConfigList() {
        return this.mHwAPPQoEResourceMangerImpl.getAPKConfigList();
    }

    public List<HwAPPQoEGameConfig> getGameConfigList() {
        return this.mHwAPPQoEResourceMangerImpl.getGameConfigList();
    }
}
