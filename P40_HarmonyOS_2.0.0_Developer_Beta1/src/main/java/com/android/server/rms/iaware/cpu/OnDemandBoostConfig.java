package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
/* compiled from: CpuXmlConfiguration */
public class OnDemandBoostConfig extends CpuCustBaseConfig {
    private static final String ON_DEMAND_BOOST_CONFIG_NAME = "on_demand_boost";
    private static final String TAG = "OnDemandBoostConfig";
    private Map<String, Integer> mOnDemandParaMap = new ArrayMap();

    OnDemandBoostConfig() {
    }

    @Override // com.android.server.rms.iaware.cpu.CpuCustBaseConfig
    public void setConfig(CpuFeature feature) {
        loadConfig(ON_DEMAND_BOOST_CONFIG_NAME);
    }

    private void loadConfig(String configName) {
        List<AwareConfig.Item> awareConfigItemList = getItemList(configName);
        if (awareConfigItemList == null) {
            AwareLog.w(TAG, "loadConfig config prop is null!");
            return;
        }
        for (AwareConfig.Item item : awareConfigItemList) {
            List<AwareConfig.SubItem> subItemList = getSubItem(item);
            if (subItemList != null) {
                for (AwareConfig.SubItem subItem : subItemList) {
                    String itemName = subItem.getName();
                    String tempItemValue = subItem.getValue();
                    if (!(itemName == null || tempItemValue == null)) {
                        try {
                            this.mOnDemandParaMap.put(itemName, Integer.valueOf(Integer.parseInt(tempItemValue)));
                        } catch (NumberFormatException e) {
                            AwareLog.e(TAG, "itemValue string to int error!");
                        }
                    }
                }
            }
        }
        OnDemandBoost.getInstance().setParams(this.mOnDemandParaMap);
    }
}
