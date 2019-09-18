package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class SpecialAppStartConfig extends CPUCustBaseConfig {
    private static final String CMDID = "cmdid";
    private static final String PKG_NAME = "pkgName";
    private static final String SPECIAL_APP_CONFIG_NAME = "special_app_start";
    private static final String TAG = "SpecialAppStartConfig";

    SpecialAppStartConfig() {
    }

    public void setConfig(CPUFeature feature) {
        loadConfig(SPECIAL_APP_CONFIG_NAME);
    }

    private void loadConfig(String configName) {
        List<AwareConfig.Item> awareConfigItemList = getItemList(configName);
        if (awareConfigItemList == null) {
            AwareLog.w(TAG, "config property is null!");
            return;
        }
        int size = awareConfigItemList.size();
        for (int i = 0; i < size; i++) {
            AwareConfig.Item item = awareConfigItemList.get(i);
            if (item == null) {
                AwareLog.w(TAG, "can not find special app item!");
            } else {
                Map<String, String> itemProps = item.getProperties();
                if (itemProps == null) {
                    AwareLog.w(TAG, "can not find pkgName property!");
                } else {
                    int cmdId = -1;
                    String pkgName = itemProps.get("pkgName");
                    if (pkgName == null) {
                        AwareLog.w(TAG, "pkgName is null!");
                    } else {
                        List<AwareConfig.SubItem> subItemList = getSubItem(item);
                        if (subItemList == null) {
                            AwareLog.w(TAG, "get subItem failed!");
                        } else {
                            Iterator<AwareConfig.SubItem> it = subItemList.iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    break;
                                }
                                AwareConfig.SubItem subItem = it.next();
                                String itemName = subItem.getName();
                                String itemValue = subItem.getValue();
                                if (itemName != null && itemValue != null && CMDID.equals(itemName)) {
                                    cmdId = parseInt(itemValue);
                                    break;
                                }
                            }
                            CPUFeatureAMSCommunicator.getInstance().updateSpecilaAppMap(pkgName, cmdId);
                        }
                    }
                }
            }
        }
    }

    private int parseInt(String intStr) {
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            AwareLog.e(TAG, "parse cmdId failed:" + intStr);
            return -1;
        }
    }
}
