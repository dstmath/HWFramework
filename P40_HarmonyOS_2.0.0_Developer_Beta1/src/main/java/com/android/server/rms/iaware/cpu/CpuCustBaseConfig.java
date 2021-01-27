package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
/* compiled from: CpuXmlConfiguration */
public class CpuCustBaseConfig {
    public static final String CPUCONFIG_GAP_IDENTIFIER = ";";
    public static final String CPUCONFIG_INVALID_STR = "#";
    protected static final String[] CPUSET_ITEM_INDEX = {ITEM_FG, ITEM_BG, ITEM_KEYBG, ITEM_SYSBG, ITEM_TABOOST, ITEM_BOOST, ITEM_TOP_APP};
    private static final String CPU_FEATURE = "CPU";
    public static final int IAWARED_SEND_MAXLEN = 512;
    protected static final String ITEM_BG = "bg";
    protected static final String ITEM_BOOST = "boost";
    protected static final String ITEM_BOOST_BY_EACH_FLING = "boost_by_each_fling";
    protected static final String ITEM_ENABLE_SKIPPED_FRAME = "enable_skipped_frame";
    protected static final String ITEM_FG = "fg";
    protected static final String ITEM_FLING_BOOST_DURATION = "boost_duration";
    protected static final String ITEM_KEYBG = "key_bg";
    protected static final String ITEM_SYSBG = "sys_bg";
    protected static final String ITEM_TABOOST = "ta_boost";
    protected static final String ITEM_TOP_APP = "top_app";
    private static final String TAG = "CpuCustBaseConfig";

    CpuCustBaseConfig() {
    }

    /* access modifiers changed from: protected */
    public void setConfig(CpuFeature feature) {
    }

    /* access modifiers changed from: protected */
    public List<AwareConfig.Item> getItemList(String configName) {
        AwareConfig awareConfig = CpuResourceConfigControl.getInstance().getAwareCustConfig(CPU_FEATURE, configName);
        if (awareConfig == null) {
            return null;
        }
        return awareConfig.getConfigList();
    }

    /* access modifiers changed from: protected */
    public List<AwareConfig.SubItem> getSubItem(AwareConfig.Item item) {
        if (item == null) {
            return null;
        }
        return item.getSubItemList();
    }

    /* access modifiers changed from: protected */
    public void obtainConfigInfo(String configName, Map<String, String> propMap, Map<String, CpuPropInfoItem> outInfoMap) {
        String propName;
        outInfoMap.clear();
        List<AwareConfig.Item> awareConfigItemList = getItemList(configName);
        if (awareConfigItemList != null) {
            for (AwareConfig.Item item : awareConfigItemList) {
                List<AwareConfig.SubItem> subItemList = getSubItem(item);
                if (subItemList != null) {
                    for (AwareConfig.SubItem subItem : subItemList) {
                        String itemName = subItem.getName();
                        String itemValue = subItem.getValue();
                        if (!(itemName == null || itemValue == null || (propName = propMap.get(itemName)) == null)) {
                            outInfoMap.put(itemName, new CpuPropInfoItem(propName, itemValue));
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void applyConfig(Map<String, CpuPropInfoItem> infoMap) {
        for (Map.Entry<String, CpuPropInfoItem> entry : infoMap.entrySet()) {
            CpuPropInfoItem item = entry.getValue();
            try {
                SystemPropertiesEx.set(item.mProp, item.mValue);
            } catch (IllegalArgumentException e) {
                AwareLog.e(TAG, "applyConfig failed, name:" + item.mProp + " value:" + item.mValue + " " + e.toString());
            }
        }
    }
}
