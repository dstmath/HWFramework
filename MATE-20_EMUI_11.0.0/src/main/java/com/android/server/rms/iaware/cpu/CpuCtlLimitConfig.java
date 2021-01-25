package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.List;
import java.util.Map;

/* compiled from: CpuXmlConfiguration */
class CpuCtlLimitConfig extends CpuCustBaseConfig {
    private static final String CONFIG_CPUCTL_FG = "cpuctl_limit";
    private static final String TAG = "CpuCtlLimitConfig";
    private Map<String, Integer> mCpuCtlFgValueMap = new ArrayMap();

    CpuCtlLimitConfig() {
        init();
    }

    @Override // com.android.server.rms.iaware.cpu.CpuCustBaseConfig
    public void setConfig(CpuFeature feature) {
    }

    private void init() {
        obtainCpuCtlForegroundArray(CONFIG_CPUCTL_FG);
    }

    public Map<String, Integer> getCpuCtlFgValueMap() {
        Map<String, Integer> tempCpuCtlFgValueMap = new ArrayMap<>();
        for (Map.Entry<String, Integer> entry : this.mCpuCtlFgValueMap.entrySet()) {
            tempCpuCtlFgValueMap.put(entry.getKey(), entry.getValue());
        }
        return tempCpuCtlFgValueMap;
    }

    private void obtainCpuCtlForegroundArray(String configName) {
        List<AwareConfig.Item> awareConfigItemList = getItemList(configName);
        if (awareConfigItemList != null) {
            this.mCpuCtlFgValueMap.clear();
            for (AwareConfig.Item item : awareConfigItemList) {
                List<AwareConfig.SubItem> subItemList = getSubItem(item);
                if (subItemList != null) {
                    for (AwareConfig.SubItem subItem : subItemList) {
                        String itemName = subItem.getName();
                        String tempItemValue = subItem.getValue();
                        if (!(itemName == null || tempItemValue == null)) {
                            try {
                                this.mCpuCtlFgValueMap.put(itemName, Integer.valueOf(Integer.parseInt(tempItemValue)));
                            } catch (NumberFormatException e) {
                                AwareLog.e(TAG, "itemValue string to int error!");
                            }
                        }
                    }
                }
            }
        }
    }
}
