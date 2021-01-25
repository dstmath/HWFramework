package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* compiled from: CpuXmlConfiguration */
class RtgSchedConfig extends CpuCustBaseConfig {
    private static final String AUX_RTG_CONFIG = "aux_rtg_config";
    private static final String FIFO_SCHED_CONFIG_NAME = "fifo_sched";
    private static final String ITEM_PROP_NAME = "name";
    private static final String KEY_THREAD_LIST = "rtg_key_thread_list";
    private static final String MARGIN = "margin";
    private static final String PLUGIN_CONFIG = "plugin_config";
    private static final String RTG_SCHED_CONFIG_NAME = "rtg_sched";
    private static final String SCHED_CONFIG = "sched_config";
    private static final String TAG = "RtgSchedConfig";

    RtgSchedConfig() {
    }

    @Override // com.android.server.rms.iaware.cpu.CpuCustBaseConfig
    public void setConfig(CpuFeature feature) {
        loadConfig(RTG_SCHED_CONFIG_NAME);
    }

    private void loadConfig(String configName) {
        Map<String, String> itemProp;
        List<AwareConfig.Item> awareConfigItemList = getItemList(configName);
        if (awareConfigItemList == null) {
            AwareLog.w(TAG, "loadConfig config prop is null!");
            return;
        }
        for (AwareConfig.Item item : awareConfigItemList) {
            if (!(item == null || (itemProp = item.getProperties()) == null)) {
                String name = itemProp.get("name");
                List<AwareConfig.SubItem> subItemList = getSubItem(item);
                if (PLUGIN_CONFIG.equals(name)) {
                    AwareRmsRtgSchedPlugin.getInstance().setPluginConfig(loadSubConfig(subItemList));
                } else if (SCHED_CONFIG.equals(name)) {
                    AwareRmsRtgSchedPlugin.getInstance().setSchedConfig(loadSubConfig(subItemList));
                } else if (MARGIN.equals(name)) {
                    AwareRmsRtgSchedPlugin.getInstance().setMargin(loadSubConfig(subItemList));
                } else if (AUX_RTG_CONFIG.equals(name)) {
                    AuxRtgSched.getInstance().setAuxRtgConfig(loadSubConfig(subItemList));
                } else if (KEY_THREAD_LIST.equals(name)) {
                    AuxRtgSched.getInstance().setKeyThreadConfig(loadKeyThreads(subItemList));
                } else if (FIFO_SCHED_CONFIG_NAME.equals(name)) {
                    CpuFifoSched.getInstance().setFifoSchedConfig(loadSubConfig(subItemList));
                }
            }
        }
    }

    private Map<String, String> loadSubConfig(List<AwareConfig.SubItem> subItemList) {
        Map<String, String> subConfigMap = new ArrayMap<>();
        if (subItemList == null) {
            return subConfigMap;
        }
        for (AwareConfig.SubItem subItem : subItemList) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (!(itemName == null || itemValue == null)) {
                    subConfigMap.put(itemName, itemValue);
                }
            }
        }
        return subConfigMap;
    }

    private List<String> loadKeyThreads(List<AwareConfig.SubItem> subItemList) {
        List<String> threadList = new ArrayList<>();
        if (subItemList == null) {
            return threadList;
        }
        for (AwareConfig.SubItem subItem : subItemList) {
            String itemValue = subItem.getValue();
            if (itemValue != null) {
                threadList.add(itemValue);
                AwareLog.i(TAG, "tiger rtg key thread " + itemValue);
            }
        }
        return threadList;
    }
}
