package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import java.util.List;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class SchedLevelBoostConfig extends CPUCustBaseConfig {
    private static final String CONFIG_NAME = "sched_level_boost";
    private static final String ITEM_PARAM_CHANGE_PROP = "sched_level_change";
    private static final String ITEM_PROP_NAME = "type";
    private static final String ITEM_WHITELIST_PROP = "WhiteList";
    private static final String SUBITEM_PARAM_ENTER_CMDID_TAG = "enter_cmdid";
    private static final String SUBITEM_PARAM_EXIT_CMDID_TAG = "exit_cmdid";
    private static final String SUBITEM_PARAM_FREQ_CMDID_TAG = "freq_cmdid";
    private static final String SUBITEM_WHITE_TAG = "pkg";
    private static final String TAG = "SchedLevelBoostConfig";

    SchedLevelBoostConfig() {
    }

    public void setConfig(CPUFeature feature) {
        loadConfig();
    }

    private void loadConfig() {
        AwareLog.i(TAG, "loadConfig begin");
        List<Item> awareConfigItemList = getItemList(CONFIG_NAME);
        if (awareConfigItemList == null) {
            AwareLog.w(TAG, "loadConfig config prop is null!");
            return;
        }
        int size = awareConfigItemList.size();
        for (int i = 0; i < size; i++) {
            Item item = (Item) awareConfigItemList.get(i);
            if (item == null) {
                AwareLog.w(TAG, "loadConfig item is null!");
            } else {
                Map<String, String> properties = item.getProperties();
                if (properties == null) {
                    AwareLog.w(TAG, "loadConfig properties is null!");
                } else {
                    String itemProp = (String) properties.get("type");
                    if (itemProp == null) {
                        AwareLog.w(TAG, "loadConfig itemprop is null!");
                    } else {
                        List<SubItem> subItemList = item.getSubItemList();
                        if (subItemList == null) {
                            AwareLog.w(TAG, "loadConfig subItemList is null!");
                        } else if (ITEM_WHITELIST_PROP.equals(itemProp)) {
                            parseWhiteList(subItemList);
                        } else if (ITEM_PARAM_CHANGE_PROP.equals(itemProp)) {
                            parseParams(subItemList);
                        }
                    }
                }
            }
        }
    }

    private void parseWhiteList(List<SubItem> subItemList) {
        int subItemSize = subItemList.size();
        for (int i = 0; i < subItemSize; i++) {
            SubItem subItem = (SubItem) subItemList.get(i);
            if (subItem == null) {
                AwareLog.w(TAG, "parseWhiteList SubItem is null!");
            } else {
                if ("pkg".equals(subItem.getName())) {
                    SchedLevelBoost.getInstance().addItem(subItem.getValue());
                }
            }
        }
    }

    private void parseParams(List<SubItem> subItemList) {
        int subItemSize = subItemList.size();
        String strFreqValue = null;
        String strExitCmdid = null;
        String strEnterCmdid = null;
        for (int i = 0; i < subItemSize; i++) {
            SubItem subItem = (SubItem) subItemList.get(i);
            if (subItem == null) {
                AwareLog.w(TAG, "parseParams SubItem is null!");
            } else {
                String itemTag = subItem.getName();
                if (SUBITEM_PARAM_ENTER_CMDID_TAG.equals(itemTag)) {
                    strEnterCmdid = subItem.getValue();
                } else if (SUBITEM_PARAM_EXIT_CMDID_TAG.equals(itemTag)) {
                    strExitCmdid = subItem.getValue();
                } else if (SUBITEM_PARAM_FREQ_CMDID_TAG.equals(itemTag)) {
                    strFreqValue = subItem.getValue();
                }
            }
        }
        SchedLevelBoost.getInstance().setParams(strEnterCmdid, strExitCmdid, strFreqValue);
    }
}
