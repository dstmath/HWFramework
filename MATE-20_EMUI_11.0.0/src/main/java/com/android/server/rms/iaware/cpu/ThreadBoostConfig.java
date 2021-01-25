package com.android.server.rms.iaware.cpu;

import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import com.huawei.android.os.ProcessExt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
/* compiled from: CpuXmlConfiguration */
public class ThreadBoostConfig extends CpuCustBaseConfig {
    private static final String CONFIG_KEYTHREAD_NAME = "boost_key_thread_list";
    public static final String GAP_IDENTIFIER = "!@#$";
    private static final String ITEM_PROP_PROCNAME = "process_name";
    private static final String TAG = "ThreadBoostConfig";
    private static final int THREAD_INFO_MAX_SIZE = 2048;
    private int mThreadInfoSize = 0;
    private ArrayList<String> mThreadNameList = new ArrayList<>();

    ThreadBoostConfig() {
        init();
    }

    @Override // com.android.server.rms.iaware.cpu.CpuCustBaseConfig
    public void setConfig(CpuFeature feature) {
        ArrayList<String> arrayList = this.mThreadNameList;
        if (arrayList == null || arrayList.size() == 0) {
            AwareLog.i(TAG, "thread name is not set");
        } else if (this.mThreadInfoSize > THREAD_INFO_MAX_SIZE) {
            AwareLog.w(TAG, " thread capacity size is too large:" + this.mThreadInfoSize);
        } else {
            ArrayList<String> arrayList2 = this.mThreadNameList;
            String[] threadNameList = (String[]) arrayList2.toArray(new String[arrayList2.size()]);
            ProcessExt.setThreadBoostInfo(threadNameList);
            CpuThreadBoost.getInstance().setBoostThreadsList(threadNameList);
        }
    }

    private void init() {
        obtainKeyThreadNameArray();
    }

    private void obtainKeyThreadNameArray() {
        String procName;
        int totalLen = 0;
        List<AwareConfig.Item> awareConfigItemList = getItemList(CONFIG_KEYTHREAD_NAME);
        if (awareConfigItemList == null) {
            AwareLog.d(TAG, "can not get threadboost info");
            return;
        }
        if (this.mThreadNameList == null) {
            this.mThreadNameList = new ArrayList<>();
        }
        for (AwareConfig.Item item : awareConfigItemList) {
            Map<String, String> itemProp = item.getProperties();
            if (!(itemProp == null || (procName = itemProp.get(ITEM_PROP_PROCNAME)) == null)) {
                this.mThreadNameList.add(GAP_IDENTIFIER);
                this.mThreadNameList.add(procName);
                totalLen += GAP_IDENTIFIER.length() + procName.length() + obtainThreadList(item, this.mThreadNameList);
            }
        }
        this.mThreadInfoSize = totalLen;
    }

    private int obtainThreadList(AwareConfig.Item item, ArrayList<String> boostList) {
        int totalLen = 0;
        List<AwareConfig.SubItem> subItemList = getSubItem(item);
        if (subItemList == null) {
            return 0;
        }
        for (AwareConfig.SubItem subItem : subItemList) {
            String itemValue = subItem.getValue();
            if (itemValue != null) {
                boostList.add(itemValue);
                totalLen += itemValue.length();
            }
        }
        return totalLen;
    }
}
