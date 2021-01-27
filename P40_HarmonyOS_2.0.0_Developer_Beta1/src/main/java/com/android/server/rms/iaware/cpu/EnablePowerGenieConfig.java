package com.android.server.rms.iaware.cpu;

import android.util.ArrayMap;
import java.util.Map;

/* compiled from: CpuXmlConfiguration */
class EnablePowerGenieConfig extends CpuCustBaseConfig {
    private static final String CONFIG_EANBLE_PG = "enable_pg_freq";
    private static final String ITEM_PG_EANBLE = "enable";
    private Map<String, CpuPropInfoItem> mPgInfoMap = new ArrayMap();
    private Map<String, String> mPropMap = new ArrayMap();

    EnablePowerGenieConfig() {
        init();
    }

    @Override // com.android.server.rms.iaware.cpu.CpuCustBaseConfig
    public void setConfig(CpuFeature feature) {
        applyConfig(this.mPgInfoMap);
    }

    private void init() {
        this.mPropMap.put(ITEM_PG_EANBLE, "persist.sys.iaware.cpuenable");
        obtainConfigInfo(CONFIG_EANBLE_PG, this.mPropMap, this.mPgInfoMap);
    }
}
