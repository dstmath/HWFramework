package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
/* compiled from: CpuXmlConfiguration */
public class FgHighLoadCtrlConfig extends CpuCustBaseConfig {
    private static final String CONFIG_PARAMS = "params";
    private static final String FG_HL_CTL_CONFIG_NAME = "fg_hl_ctl";
    private static final String ITEM_PROP_NAME = "name";
    private static final String TAG = "FgHighLoadCtrlConfig";

    FgHighLoadCtrlConfig() {
    }

    @Override // com.android.server.rms.iaware.cpu.CpuCustBaseConfig
    public void setConfig(CpuFeature feature) {
    }

    public void init() {
        loadConfig(FG_HL_CTL_CONFIG_NAME);
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
                List<AwareConfig.SubItem> subItemList = getSubItem(item);
                if (CONFIG_PARAMS.equals(itemProp.get("name"))) {
                    CpuHighLoadManager.getInstance().sendFgHighLoadCtlParams(loadSubConfig(subItemList));
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
}
