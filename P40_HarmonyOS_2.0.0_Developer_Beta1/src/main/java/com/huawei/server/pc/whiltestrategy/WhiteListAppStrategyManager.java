package com.huawei.server.pc.whiltestrategy;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import com.huawei.util.LogEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WhiteListAppStrategyManager {
    private static boolean DEBUG = LogEx.getLogHWInfo();
    private static final Object INSTANCE_SYNC = new Object();
    private static final String TAG = "DefaultXmlFileAppStrategy";
    private static volatile WhiteListAppStrategyManager mInstance;
    private Context mContext;
    private AppStrategy mDefaultXmlFileAppStrategy;
    private AppStrategy mMetaDataAppStategy = new MetaDataAppStrategy();

    private WhiteListAppStrategyManager(Context context) {
        this.mContext = context;
        this.mDefaultXmlFileAppStrategy = new DefaultXmlFileAppStrategy(context);
    }

    public static WhiteListAppStrategyManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (INSTANCE_SYNC) {
                if (mInstance == null) {
                    mInstance = new WhiteListAppStrategyManager(context);
                }
            }
        }
        return mInstance;
    }

    public List<String> getAllSupportPcAppList() {
        if (this.mContext == null) {
            return null;
        }
        List<String> supportPCAppList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : this.mDefaultXmlFileAppStrategy.getAppList(this.mContext).entrySet()) {
            String key = entry.getKey();
            if (this.mMetaDataAppStategy.getAppState(key, this.mContext) == 0) {
                supportPCAppList.add(key);
                if (DEBUG) {
                    Log.i(TAG, "support pc from white config:" + key);
                }
            }
        }
        for (Map.Entry<String, Integer> entry2 : this.mMetaDataAppStategy.getAppList(this.mContext).entrySet()) {
            String key2 = entry2.getKey();
            if (entry2.getValue().intValue() == 1) {
                supportPCAppList.add(key2);
                if (DEBUG) {
                    Log.i(TAG, "support pc from metadata:" + key2);
                }
            }
        }
        return supportPCAppList;
    }

    public int getAppSupportPCState(String packageName) {
        if (this.mContext == null || packageName == null) {
            return -1;
        }
        int appStateFromWhite = this.mDefaultXmlFileAppStrategy.getAppState(packageName, null);
        int appStateFromMeta = this.mMetaDataAppStategy.getAppState(packageName, this.mContext);
        if (DEBUG) {
            Log.i(TAG, "getAppSupportPCState packageName:" + packageName + " appStateFromWhite:" + appStateFromWhite + " appStateFromMeta:" + appStateFromMeta);
        }
        if (appStateFromMeta == 1) {
            return 1;
        }
        if (appStateFromMeta == -1) {
            return -1;
        }
        if (appStateFromMeta == 0 && appStateFromWhite == 1) {
            return 1;
        }
        return (appStateFromMeta != 0 || appStateFromWhite == -1) ? -1 : -1;
    }

    public List<Pair<String, Integer>> getSpecailWindowPolicyAppList() {
        return ((DefaultXmlFileAppStrategy) this.mDefaultXmlFileAppStrategy).getSpecailWindowPolicyAppList();
    }
}
