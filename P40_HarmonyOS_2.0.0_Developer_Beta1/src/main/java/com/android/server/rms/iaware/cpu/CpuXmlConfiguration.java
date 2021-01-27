package com.android.server.rms.iaware.cpu;

import java.util.ArrayList;
import java.util.List;

public class CpuXmlConfiguration {
    private List<CpuCustBaseConfig> mConfigDataList = new ArrayList();

    public CpuXmlConfiguration() {
        this.mConfigDataList.add(new SubSwitchConfig());
        this.mConfigDataList.add(new ThreadBoostConfig());
        this.mConfigDataList.add(new EnablePowerGenieConfig());
        this.mConfigDataList.add(new CpusetScreenConfig());
        this.mConfigDataList.add(new CpuVipParaConfig());
        this.mConfigDataList.add(new GameLevelMapConfig());
        this.mConfigDataList.add(new OnDemandBoostConfig());
        this.mConfigDataList.add(new SpecialAppStartConfig());
        this.mConfigDataList.add(new RtgSchedConfig());
        this.mConfigDataList.add(new CpuMinUtilConfig());
    }

    public void startSetProperty(CpuFeature feature) {
        for (CpuCustBaseConfig configData : this.mConfigDataList) {
            configData.setConfig(feature);
        }
    }
}
