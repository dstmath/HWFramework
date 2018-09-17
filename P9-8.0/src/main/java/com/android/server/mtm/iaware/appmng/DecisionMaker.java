package com.android.server.mtm.iaware.appmng;

import android.app.mtm.iaware.appmng.AppMngConstant.AppCleanSource;
import android.app.mtm.iaware.appmng.AppMngConstant.AppMngFeature;
import android.app.mtm.iaware.appmng.AppMngConstant.AppStartReason;
import android.app.mtm.iaware.appmng.AppMngConstant.AppStartSource;
import android.app.mtm.iaware.appmng.AppMngConstant.CleanReason;
import android.app.mtm.iaware.appmng.AppMngConstant.EnumWithDesc;
import android.content.Context;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import com.android.server.mtm.iaware.appmng.appstart.comm.AppStartupUtil;
import com.android.server.mtm.iaware.appmng.policy.AppStartPolicy;
import com.android.server.mtm.iaware.appmng.policy.Policy;
import com.android.server.mtm.iaware.appmng.rule.AppMngRule;
import com.android.server.mtm.iaware.appmng.rule.AppStartRule;
import com.android.server.mtm.iaware.appmng.rule.Config;
import com.android.server.mtm.iaware.appmng.rule.ConfigReader;
import com.android.server.mtm.iaware.appmng.rule.ListItem;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag;
import com.android.server.mtm.taskstatus.ProcessCleaner.CleanType;
import com.android.server.rms.iaware.appmng.AwareAppStartStatusCache;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

public class DecisionMaker {
    private static final int APPSTART_POLICY_NOT_ALLOW = 0;
    private static final int APPSTART_XML_POLICY_ALLOW_ALL = 2;
    private static final int APPSTART_XML_POLICY_ALLOW_SMART = 1;
    private static final int DEFAULT_APPMNG_POLICY = CleanType.NONE.ordinal();
    private static final int DEFAULT_APPSTART_POLICY = 1;
    private static final String DEFAULT_STRING = "default";
    private static final int DEFAULT_UID = 0;
    private static final int MAX_HISTORY_LENGTH = 50;
    private static final String REASON_LIST = "list";
    private static final String TAG = "DecisionMaker";
    private static final int UNINI_VALUE = -1;
    private static DecisionMaker mDecisionMaker = null;
    private HashMap<AppMngFeature, HashMap<EnumWithDesc, Config>> mAllConfig = new HashMap();
    private HashMap<AppMngFeature, HashMap<EnumWithDesc, HashMap<String, ListItem>>> mAllList = new HashMap();
    private HashMap<AppMngFeature, HashMap<String, ArrayList<String>>> mAllMisc = new HashMap();
    private HashMap<AppCleanSource, Queue<String>> mCleanHistory = new HashMap();
    private int mVersion;

    private DecisionMaker() {
    }

    public static synchronized DecisionMaker getInstance() {
        DecisionMaker decisionMaker;
        synchronized (DecisionMaker.class) {
            if (mDecisionMaker == null) {
                mDecisionMaker = new DecisionMaker();
            }
            decisionMaker = mDecisionMaker;
        }
        return decisionMaker;
    }

    public List<AwareProcessBlockInfo> decideAll(List<AwareProcessInfo> processInfoList, int level, AppMngFeature feature, EnumWithDesc config, ArraySet<String> listFilter) {
        return decideAllInternal(processInfoList, level, feature, config, true, listFilter);
    }

    public List<AwareProcessBlockInfo> decideAll(List<AwareProcessInfo> processInfoList, int level, AppMngFeature feature, EnumWithDesc config) {
        return decideAllInternal(processInfoList, level, feature, config, true, null);
    }

    public List<AwareProcessBlockInfo> decideAllWithoutList(List<AwareProcessInfo> processInfoList, int level, AppMngFeature feature, EnumWithDesc config) {
        return decideAllInternal(processInfoList, level, feature, config, false, null);
    }

    private List<AwareProcessBlockInfo> decideAllInternal(List<AwareProcessInfo> processInfoList, int level, AppMngFeature feature, EnumWithDesc config, boolean shouldConsiderList, ArraySet<String> listFilter) {
        if (processInfoList == null) {
            return null;
        }
        List<AwareProcessBlockInfo> resultList = new ArrayList();
        for (AwareProcessInfo processInfo : processInfoList) {
            if (processInfo != null) {
                AwareProcessBlockInfo result = decide(processInfo, level, feature, config, shouldConsiderList, listFilter);
                if (result != null) {
                    resultList.add(result);
                }
            }
        }
        return resultList;
    }

    /* JADX WARNING: Missing block: B:14:0x0027, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean decideSystemAppPolicy(String packageName, AppStartSource source, AwareAppStartStatusCache status, int tristate) {
        if (status != null && (status.mFlags & 8) == 0) {
            return (tristate == 1 || AppStartSource.SYSTEM_BROADCAST.equals(source) || AppStartSource.THIRD_BROADCAST.equals(source) || AppStartSource.SCHEDULE_RESTART.equals(source) || !status.mIsSystemApp) ? false : true;
        } else {
            return true;
        }
    }

    private AwareProcessBlockInfo decide(AwareProcessInfo processInfo, int level, AppMngFeature feature, EnumWithDesc config, boolean shouldConsiderList, ArraySet<String> listFilter) {
        HashMap<EnumWithDesc, Config> featureRule = (HashMap) this.mAllConfig.get(feature);
        HashMap<EnumWithDesc, HashMap<String, ListItem>> featureList = (HashMap) this.mAllList.get(feature);
        if (listFilter == null) {
            listFilter = new ArraySet();
        }
        HashMap<String, Integer> detailedReason;
        if (processInfo.mProcInfo == null || processInfo.mProcInfo.mPackageName == null || (processInfo.mProcInfo.mPackageName.isEmpty() ^ 1) == 0) {
            detailedReason = new HashMap();
            detailedReason.put(AppMngTag.POLICY.getDesc(), Integer.valueOf(CleanType.NONE.ordinal()));
            detailedReason.put("spec", Integer.valueOf(CleanReason.MISSING_PROCESS_INFO.ordinal()));
            return new AwareProcessBlockInfo(CleanReason.MISSING_PROCESS_INFO.getCode(), 0, processInfo, DEFAULT_APPMNG_POLICY, detailedReason);
        }
        String packageName = (String) processInfo.mProcInfo.mPackageName.get(0);
        if (AppStartupUtil.isCtsPackage(packageName)) {
            detailedReason = new HashMap();
            detailedReason.put(AppMngTag.POLICY.getDesc(), Integer.valueOf(CleanType.NONE.ordinal()));
            detailedReason.put("spec", Integer.valueOf(CleanReason.CTS.ordinal()));
            return new AwareProcessBlockInfo(CleanReason.CTS.getCode(), 0, processInfo, DEFAULT_APPMNG_POLICY, detailedReason);
        }
        if (!(!shouldConsiderList || featureList == null || (listFilter.contains(packageName) ^ 1) == 0)) {
            HashMap<String, ListItem> list = (HashMap) featureList.get(config);
            if (list != null) {
                ListItem item = (ListItem) list.get(packageName);
                if (item != null) {
                    detailedReason = new HashMap();
                    detailedReason.put(AppMngTag.POLICY.getDesc(), Integer.valueOf(item.getPolicy()));
                    detailedReason.put("spec", Integer.valueOf(CleanReason.LIST.ordinal()));
                    return new AwareProcessBlockInfo(CleanReason.LIST.getCode(), processInfo.mProcInfo.mUid, processInfo, item.getPolicy(), detailedReason);
                }
            }
        }
        if (featureRule != null) {
            AppMngRule rule = (AppMngRule) featureRule.get(config);
            if (rule != null) {
                return rule.apply(processInfo, packageName, level, shouldConsiderList);
            }
        }
        return new AwareProcessBlockInfo(CleanReason.CONFIG_INVALID.getCode(), processInfo.mProcInfo.mUid, processInfo, DEFAULT_APPMNG_POLICY, null);
    }

    public int getAppStartPolicy(String packageName, AppStartSource source) {
        HashMap<EnumWithDesc, HashMap<String, ListItem>> featureList = (HashMap) this.mAllList.get(AppMngFeature.APP_START);
        if (featureList != null) {
            HashMap<String, ListItem> list = (HashMap) featureList.get(source);
            if (list != null) {
                ListItem item = (ListItem) list.get(packageName);
                if (item != null) {
                    return item.getPolicy();
                }
                return 0;
            }
        }
        return 1;
    }

    public Policy decide(String packageName, AppStartSource source, AwareAppStartStatusCache status, int tristate) {
        HashMap<EnumWithDesc, HashMap<String, ListItem>> featureList = (HashMap) this.mAllList.get(AppMngFeature.APP_START);
        if (featureList != null) {
            HashMap<String, ListItem> list = (HashMap) featureList.get(source);
            if (list != null) {
                ListItem item = (ListItem) list.get(packageName);
                if (!(item == null || status == null)) {
                    int policy;
                    if (AppStartSource.SYSTEM_BROADCAST.equals(source)) {
                        policy = item.getPolicy(status.mAction);
                        if (policy == -1) {
                            policy = item.getPolicy("default");
                        }
                    } else {
                        policy = item.getPolicy();
                    }
                    switch (policy) {
                        case 1:
                            if (tristate == 1) {
                                policy = -1;
                                break;
                            }
                            break;
                        case 2:
                            policy = 1;
                            break;
                    }
                    if (policy != -1) {
                        return new AppStartPolicy(packageName, policy, AppStartReason.LIST.getDesc());
                    }
                }
            }
        }
        if (decideSystemAppPolicy(packageName, source, status, tristate)) {
            return new AppStartPolicy(packageName, 1, AppStartReason.SYSTEM_APP.getDesc());
        }
        HashMap<EnumWithDesc, Config> featureRule = (HashMap) this.mAllConfig.get(AppMngFeature.APP_START);
        if (featureRule != null) {
            AppStartRule rule = (AppStartRule) featureRule.get(source);
            if (rule != null) {
                return rule.apply(packageName, source, status, tristate);
            }
        }
        return new AppStartPolicy(packageName, 1, AppStartReason.DEFAULT.getDesc());
    }

    public void updateRule(AppMngFeature feature, Context context) {
        ConfigReader reader = new ConfigReader();
        reader.parseFile(feature, context);
        if (feature == null) {
            this.mAllConfig = reader.getConfig();
            this.mAllList = reader.getList();
            this.mAllMisc = reader.getMisc();
        } else {
            this.mAllConfig.put(feature, (HashMap) reader.getConfig().get(feature));
            this.mAllList.put(feature, (HashMap) reader.getList().get(feature));
            this.mAllMisc.put(feature, (HashMap) reader.getMisc().get(feature));
        }
        if (feature == null || AppMngFeature.APP_CLEAN.equals(feature)) {
            initHistory();
        }
        this.mVersion = reader.getVersion();
    }

    public ArrayList<String> getRawConfig(String feature, String config) {
        if (config == null || feature == null) {
            return null;
        }
        HashMap<String, ArrayList<String>> rawFeature = (HashMap) this.mAllMisc.get(AppMngFeature.fromString(feature));
        if (rawFeature == null) {
            return null;
        }
        ArrayList<String> items = (ArrayList) rawFeature.get(config);
        if (items == null) {
            return new ArrayList();
        }
        return items;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public void dump(PrintWriter pw, AppMngFeature feature) {
        if (pw != null) {
            HashMap<EnumWithDesc, HashMap<String, ListItem>> featureList = (HashMap) this.mAllList.get(feature);
            if (this.mVersion != -1) {
                pw.println("config version:" + this.mVersion);
            }
            if (featureList == null) {
                pw.println("there is no [" + feature + "] list");
            } else {
                pw.println("----------[" + feature + "] Lists----------");
                for (Entry<EnumWithDesc, HashMap<String, ListItem>> featureListEntry : featureList.entrySet()) {
                    if (featureListEntry != null) {
                        HashMap<String, ListItem> configList = (HashMap) featureListEntry.getValue();
                        if (configList != null) {
                            pw.println("==========[" + featureListEntry.getKey() + "] Lists==========");
                            for (Entry<String, ListItem> configListEntry : configList.entrySet()) {
                                if (configListEntry != null) {
                                    ListItem item = (ListItem) configListEntry.getValue();
                                    if (item != null) {
                                        pw.println("  " + ((String) configListEntry.getKey()) + ":");
                                        item.dump(pw);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            HashMap<EnumWithDesc, Config> featureConfig = (HashMap) this.mAllConfig.get(feature);
            if (featureConfig == null) {
                pw.println("there is no [" + feature + "] config");
            } else {
                pw.println("----------[" + feature + "] Rules----------");
                for (Entry<EnumWithDesc, Config> featureConfigEntry : featureConfig.entrySet()) {
                    if (featureConfigEntry != null) {
                        Config config = (Config) featureConfigEntry.getValue();
                        if (config != null) {
                            pw.println("==========[" + featureConfigEntry.getKey() + "] Rules==========");
                            config.dump(pw);
                        }
                    }
                }
            }
        }
    }

    private void initHistory() {
        for (AppCleanSource source : AppCleanSource.values()) {
            synchronized (this.mCleanHistory) {
                this.mCleanHistory.put(source, new LinkedList());
            }
        }
    }

    /* JADX WARNING: Missing block: B:18:0x0052, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateHistory(AppCleanSource source, String reason) {
        synchronized (this.mCleanHistory) {
            Queue<String> queue = (Queue) this.mCleanHistory.get(source);
            if (queue == null) {
                AwareLog.e(TAG, "bad request = " + source);
                return;
            }
            if (queue.size() == 50 && ((String) queue.poll()) == null) {
                AwareLog.e(TAG, "poll failed !");
            }
            if (!queue.offer(reason)) {
                AwareLog.e(TAG, "updateHistory failed !");
            }
        }
    }

    public void dumpHistory(PrintWriter pw, AppCleanSource source) {
        if (pw != null) {
            synchronized (this.mCleanHistory) {
                Queue<String> queue = (Queue) this.mCleanHistory.get(source);
                if (queue != null) {
                    for (String history : queue) {
                        pw.println(history);
                    }
                }
            }
        }
    }
}
