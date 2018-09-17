package com.android.server.rms.iaware.cpu;

import java.util.ArrayList;
import java.util.List;

public class CPUXmlConfiguration {
    private List<CPUCustBaseConfig> configDataList;

    public CPUXmlConfiguration() {
        this.configDataList = new ArrayList();
        this.configDataList.add(new SubSwitchConfig());
        this.configDataList.add(new ThreadBoostConfig());
        this.configDataList.add(new EnablePowerGenieConfig());
        this.configDataList.add(new ScrollerBoostConfig());
        this.configDataList.add(new FreqInteractiveConfig());
        this.configDataList.add(new CpusetScreenConfig());
        this.configDataList.add(new VirtualRealityConfig());
    }

    public void startSetProperty(CPUFeature feature) {
        for (CPUCustBaseConfig configData : this.configDataList) {
            configData.setConfig(feature);
        }
    }
}
