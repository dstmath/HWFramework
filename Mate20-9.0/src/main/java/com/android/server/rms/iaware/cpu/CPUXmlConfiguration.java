package com.android.server.rms.iaware.cpu;

import java.util.ArrayList;
import java.util.List;

public class CPUXmlConfiguration {
    private List<CPUCustBaseConfig> configDataList = new ArrayList();

    public CPUXmlConfiguration() {
        this.configDataList.add(new SubSwitchConfig());
        this.configDataList.add(new ThreadBoostConfig());
        this.configDataList.add(new EnablePowerGenieConfig());
        this.configDataList.add(new ScrollerBoostConfig());
        this.configDataList.add(new CpusetScreenConfig());
        this.configDataList.add(new VirtualRealityConfig());
        this.configDataList.add(new CPUVipParaConfig());
        this.configDataList.add(new SchedLevelBoostConfig());
        this.configDataList.add(new GameLevelMapConfig());
        this.configDataList.add(new OnDemandBoostConfig());
        this.configDataList.add(new SpecialAppStartConfig());
    }

    public void startSetProperty(CPUFeature feature) {
        for (CPUCustBaseConfig configData : this.configDataList) {
            configData.setConfig(feature);
        }
    }
}
