package com.android.server.rms.iaware.cpu;

import java.util.HashMap;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class EnablePowerGenieConfig extends CPUCustBaseConfig {
    private static final String CONFIG_EANBLE_PG = "enable_pg_freq";
    private static final String ITEM_PG_EANBLE = "enable";
    private Map<String, String> mPGInfoItem2PropMap;
    private Map<String, CPUPropInfoItem> mPGInfoMap;

    public EnablePowerGenieConfig() {
        this.mPGInfoItem2PropMap = new HashMap();
        this.mPGInfoMap = new HashMap();
        init();
    }

    public void setConfig(CPUFeature feature) {
        applyConfig(this.mPGInfoMap);
    }

    void init() {
        this.mPGInfoItem2PropMap.put(ITEM_PG_EANBLE, "persist.sys.iaware.cpuenable");
        obtainConfigInfo(CONFIG_EANBLE_PG, this.mPGInfoItem2PropMap, this.mPGInfoMap);
    }
}
