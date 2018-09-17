package com.android.server.rms.iaware.cpu;

import android.os.SystemProperties;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: CPUXmlConfiguration */
class CPUCustBaseConfig {
    public static final String CPUCONFIG_GAP_IDENTIFIER = ";";
    public static final String CPUCONFIG_INVALID_STR = "#";
    private static final String CPU_FEATURE = "CPU";
    public static final int IAWARED_SEND_MAXLEN = 512;
    protected static final String ITEM_BG = "bg";
    protected static final String ITEM_BOOST = "boost";
    protected static final String ITEM_BOOST_BY_EACH_FLING = "boost_by_each_fling";
    protected static final String ITEM_ENABLE_SKIPPED_FRAME = "enable_skipped_frame";
    protected static final String ITEM_FG = "fg";
    protected static final String ITEM_FLING_BOOST_DURATION = "boost_duration";
    protected static final String ITEM_GO_HISPEED_LOAD_B = "go_hispeed_load_b";
    protected static final String ITEM_GO_HISPEED_LOAD_L = "go_hispeed_load_l";
    protected static final String ITEM_HISPEED_FREQ_B = "hispeed_freq_b";
    protected static final String ITEM_HISPEED_FREQ_L = "hispeed_freq_l";
    protected static final String ITEM_KEYBG = "key_bg";
    protected static final String ITEM_MOVE_BOOST_DIF = "move_boost_dif";
    protected static final String ITEM_SYSBG = "sys_bg";
    protected static final String ITEM_TABOOST = "ta_boost";
    protected static final String ITEM_TARGET_LOAD_FREQ_B = "target_load_freq_b";
    protected static final String ITEM_TARGET_LOAD_FREQ_L = "target_load_freq_l";
    protected static final String ITEM_TOP_APP = "top_app";
    private static final String TAG = "CPUCustBaseConfig";
    public static final String[] mCpusetItemIndex = new String[]{ITEM_FG, ITEM_BG, ITEM_KEYBG, ITEM_SYSBG, ITEM_TABOOST, ITEM_BOOST, ITEM_TOP_APP};
    public static final String[] mInteractiveItemIndex = new String[]{ITEM_GO_HISPEED_LOAD_L, ITEM_HISPEED_FREQ_L, ITEM_TARGET_LOAD_FREQ_L, ITEM_GO_HISPEED_LOAD_B, ITEM_HISPEED_FREQ_B, ITEM_TARGET_LOAD_FREQ_B};

    CPUCustBaseConfig() {
    }

    protected void setConfig(CPUFeature feature) {
    }

    protected List<Item> getItemList(String configName) {
        AwareConfig awareConfig = CPUResourceConfigControl.getInstance().getAwareCustConfig(CPU_FEATURE, configName);
        if (awareConfig == null) {
            return null;
        }
        return awareConfig.getConfigList();
    }

    protected List<SubItem> getSubItem(Item item) {
        if (item == null) {
            return null;
        }
        return item.getSubItemList();
    }

    protected void obtainConfigInfo(String configName, Map<String, String> item2PropMap, Map<String, CPUPropInfoItem> outInfoMap) {
        outInfoMap.clear();
        List<Item> awareConfigItemList = getItemList(configName);
        if (awareConfigItemList != null) {
            for (Item item : awareConfigItemList) {
                List<SubItem> subItemList = getSubItem(item);
                if (subItemList != null) {
                    for (SubItem subItem : subItemList) {
                        String itemName = subItem.getName();
                        String itemValue = subItem.getValue();
                        if (!(itemName == null || itemValue == null)) {
                            String propName = (String) item2PropMap.get(itemName);
                            if (propName != null) {
                                outInfoMap.put(itemName, new CPUPropInfoItem(propName, itemValue));
                            }
                        }
                    }
                }
            }
        }
    }

    protected void applyConfig(Map<String, CPUPropInfoItem> infoMap) {
        for (Entry<String, CPUPropInfoItem> entry : infoMap.entrySet()) {
            CPUPropInfoItem item = (CPUPropInfoItem) entry.getValue();
            try {
                SystemProperties.set(item.mProp, item.mValue);
            } catch (IllegalArgumentException e) {
                AwareLog.e(TAG, "applyConfig failed, name:" + item.mProp + " value:" + item.mValue + " " + e.toString());
            }
        }
    }
}
