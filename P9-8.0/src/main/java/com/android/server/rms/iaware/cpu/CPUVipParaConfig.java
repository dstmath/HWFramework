package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: CPUXmlConfiguration */
class CPUVipParaConfig extends CPUCustBaseConfig {
    private static final String CONFIG_VIP = "vip_thread_param";
    private static final String CONFIG_VIP_DYM_GRAN = "vip_max_dynamic_granularity";
    private static final String CONFIG_VIP_MIGRATION = "vip_min_migration_delay";
    private static final String CONFIG_VIP_SCHED_DELAY = "vip_min_sched_delay_granularity";
    private static final String TAG = "CPUVipParaConfig";
    private CPUFeature mCPUFeatureInstance;
    private Map<String, Integer> mVipThreadParams = new ArrayMap();
    private Map<String, Integer> mVipThreadParamsOrder = new ArrayMap();

    public CPUVipParaConfig() {
        init();
    }

    public void setConfig(CPUFeature feature) {
        if (feature != null) {
            this.mCPUFeatureInstance = feature;
            int size = this.mVipThreadParams.size();
            ByteBuffer buffer = ByteBuffer.allocate(((size * 2) * 4) + 8);
            buffer.putInt(CPUFeature.MSG_SET_VIP_THREAD_PARAMS);
            buffer.putInt(size);
            for (Entry<String, Integer> e : this.mVipThreadParams.entrySet()) {
                Integer msgId = (Integer) this.mVipThreadParamsOrder.get(e.getKey());
                if (msgId != null) {
                    buffer.putInt(msgId.intValue());
                    buffer.putInt(((Integer) e.getValue()).intValue());
                } else {
                    AwareLog.e(TAG, "find a unknown params:" + ((String) e.getKey()) + ", return directly");
                    return;
                }
            }
            int resCode = this.mCPUFeatureInstance.sendPacket(buffer);
            if (resCode != 1) {
                AwareLog.e(TAG, "sendConfig sendPacket failed, send error code:" + resCode);
            }
        }
    }

    private void init() {
        obtainVipThreadParams();
        this.mVipThreadParamsOrder.put(CONFIG_VIP_SCHED_DELAY, Integer.valueOf(1));
        this.mVipThreadParamsOrder.put(CONFIG_VIP_DYM_GRAN, Integer.valueOf(2));
        this.mVipThreadParamsOrder.put(CONFIG_VIP_MIGRATION, Integer.valueOf(3));
    }

    private void obtainVipThreadParams() {
        List<Item> awareConfigItemList = getItemList(CONFIG_VIP);
        if (awareConfigItemList != null) {
            this.mVipThreadParams.clear();
            for (Item item : awareConfigItemList) {
                List<SubItem> subItemList = getSubItem(item);
                if (subItemList != null) {
                    for (SubItem subItem : subItemList) {
                        String itemName = subItem.getName();
                        String tempItemValue = subItem.getValue();
                        if (!(itemName == null || tempItemValue == null)) {
                            try {
                                this.mVipThreadParams.put(itemName, Integer.valueOf(Integer.parseInt(tempItemValue)));
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
