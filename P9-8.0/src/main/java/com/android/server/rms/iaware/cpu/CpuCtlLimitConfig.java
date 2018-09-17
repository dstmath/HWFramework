package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: CPUXmlConfiguration */
class CpuCtlLimitConfig extends CPUCustBaseConfig {
    private static final String CONFIG_CPUCTL_FG = "cpuctl_limit";
    private static final String TAG = "CpuCtlLimitConfig";
    private Map<String, Integer> mCpuCtlFGValueMap = new ArrayMap();

    public CpuCtlLimitConfig() {
        init();
    }

    public void setConfig(CPUFeature feature) {
    }

    private void init() {
        obtainCpuCtlForegroundArray(CONFIG_CPUCTL_FG);
    }

    public Map<String, Integer> getCpuCtlFGValueMap() {
        Map<String, Integer> tempCpuCtlFGValueMap = new ArrayMap();
        for (Entry<String, Integer> e : this.mCpuCtlFGValueMap.entrySet()) {
            tempCpuCtlFGValueMap.put((String) e.getKey(), (Integer) e.getValue());
        }
        return tempCpuCtlFGValueMap;
    }

    private void obtainCpuCtlForegroundArray(String configName) {
        List<Item> awareConfigItemList = getItemList(configName);
        if (awareConfigItemList != null) {
            this.mCpuCtlFGValueMap.clear();
            for (Item item : awareConfigItemList) {
                List<SubItem> subItemList = getSubItem(item);
                if (subItemList != null) {
                    for (SubItem subItem : subItemList) {
                        String itemName = subItem.getName();
                        String tempItemValue = subItem.getValue();
                        if (!(itemName == null || tempItemValue == null)) {
                            try {
                                this.mCpuCtlFGValueMap.put(itemName, Integer.valueOf(Integer.parseInt(tempItemValue)));
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
