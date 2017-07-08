package com.android.server.rms.iaware.cpu;

import android.os.Process;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* compiled from: CPUXmlConfiguration */
class ThreadBoostConfig extends CPUCustBaseConfig {
    private static final String CONFIG_KEYTHREAD_NAME = "boost_key_thread_list";
    public static final String GAP_IDENTIFIER = "!@#$";
    private static final String ITEM_PROP_PROCNAME = "process_name";
    private static final String TAG = "ThreadBoostConfig";
    private ArrayList<String> mthreadNameList;

    public ThreadBoostConfig() {
        this.mthreadNameList = new ArrayList();
        init();
    }

    public void setConfig(CPUFeature feature) {
        if (this.mthreadNameList == null || this.mthreadNameList.size() == 0) {
            AwareLog.d(TAG, "thread name is not set");
            return;
        }
        String[] threadNameList = (String[]) this.mthreadNameList.toArray(new String[this.mthreadNameList.size()]);
        Process.setThreadBoostInfo(threadNameList);
        CpuThreadBoost.getInstance().setBoostThreadsList(threadNameList);
    }

    private void init() {
        obtainKeyThreadNameArray();
    }

    private void obtainKeyThreadNameArray() {
        List<Item> awareConfigItemList = getItemList(CONFIG_KEYTHREAD_NAME);
        if (awareConfigItemList == null) {
            AwareLog.d(TAG, "can not get threadboost info");
            return;
        }
        if (this.mthreadNameList == null) {
            this.mthreadNameList = new ArrayList();
        }
        for (Item item : awareConfigItemList) {
            Map<String, String> itemProp = item.getProperties();
            if (itemProp != null) {
                String procname = (String) itemProp.get(ITEM_PROP_PROCNAME);
                if (procname != null) {
                    this.mthreadNameList.add(GAP_IDENTIFIER);
                    this.mthreadNameList.add(procname);
                    obtainThreadList(item, this.mthreadNameList);
                }
            }
        }
    }

    private void obtainThreadList(Item item, ArrayList<String> boostList) {
        List<SubItem> subItemList = getSubItem(item);
        if (subItemList != null) {
            for (SubItem subItem : subItemList) {
                String itemValue = subItem.getValue();
                if (itemValue != null) {
                    boostList.add(itemValue);
                }
            }
        }
    }
}
