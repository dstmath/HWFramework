package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.List;
import java.util.Map;

/* compiled from: CpuXmlConfiguration */
class CpuMinUtilConfig extends CpuCustBaseConfig {
    private static final String CONFIG_TYPE = "type";
    private static final String MIN_UTIL_CONFIG = "min_util_config";
    private static final String TAG = "CpuMinUtilConfig";

    CpuMinUtilConfig() {
    }

    @Override // com.android.server.rms.iaware.cpu.CpuCustBaseConfig
    public void setConfig(CpuFeature feature) {
        loadMinUtilConfig();
    }

    private void loadMinUtilConfig() {
        Map<String, String> itemProps;
        List<AwareConfig.Item> awareConfigItemList = getItemList(MIN_UTIL_CONFIG);
        if (awareConfigItemList == null) {
            AwareLog.w(TAG, "awareConfigItemList is null!");
            return;
        }
        Map<String, Integer> minUtilConfig = new ArrayMap<>();
        for (AwareConfig.Item item : awareConfigItemList) {
            if (!(item == null || (itemProps = item.getProperties()) == null)) {
                String type = itemProps.get(CONFIG_TYPE);
                List<AwareConfig.SubItem> subItemList = getSubItem(item);
                if (!(subItemList == null || type == null)) {
                    for (AwareConfig.SubItem subItem : subItemList) {
                        String itemName = subItem.getName();
                        String tempItemValue = subItem.getValue();
                        if (!(itemName == null || tempItemValue == null)) {
                            try {
                                minUtilConfig.put(itemName, Integer.valueOf(Integer.parseInt(tempItemValue)));
                            } catch (NumberFormatException e) {
                                AwareLog.e(TAG, "itemValue string to int error!");
                            }
                        }
                    }
                    CpuThreadMinUtilBoost.getInstance().setBoostConifg(minUtilConfig, type);
                    AwareLog.i(TAG, "minUtilConfig : " + minUtilConfig + ", type : " + type);
                    minUtilConfig.clear();
                }
            }
        }
        CpuThreadMinUtilBoost.getInstance().initBoostConifg();
    }
}
