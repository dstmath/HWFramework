package com.android.server.mtm.iaware.appmng;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.iaware.appmng.appstart.comm.AppStartupUtil;
import com.android.server.mtm.iaware.appmng.policy.AppStartPolicy;
import com.android.server.mtm.iaware.appmng.policy.Policy;
import com.android.server.mtm.iaware.appmng.rule.AppMngRule;
import com.android.server.mtm.iaware.appmng.rule.AppStartRule;
import com.android.server.mtm.iaware.appmng.rule.BroadcastMngRule;
import com.android.server.mtm.iaware.appmng.rule.Config;
import com.android.server.mtm.iaware.appmng.rule.ConfigReader;
import com.android.server.mtm.iaware.appmng.rule.ListItem;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.rms.iaware.appmng.AwareAppStartStatusCache;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.android.server.rms.iaware.appmng.AwareWakeUpManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class DecisionMaker {
    private static final int APPSTART_POLICY_NOT_ALLOW = 0;
    private static final int APPSTART_XML_POLICY_ALLOW_ALL = 2;
    private static final int APPSTART_XML_POLICY_ALLOW_SMART = 1;
    private static final int DEFAULT_APPMNG_POLICY = ProcessCleaner.CleanType.NONE.ordinal();
    private static final int DEFAULT_APPSTART_POLICY = 1;
    private static final String DEFAULT_STRING = "default";
    private static final int DEFAULT_UID = 0;
    private static final int MAX_HISTORY_LENGTH = 50;
    private static final String REASON_LIST = "list";
    private static final String TAG = "DecisionMaker";
    private static final int UNINIT_VALUE = -1;
    private static DecisionMaker mDecisionMaker = null;
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<String, String>> mAllCommonCfg = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, Config>> mAllConfig = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> mAllList = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<String, ArrayList<String>>> mAllMisc = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> mAllProcessList = new ArrayMap<>();
    private ArrayMap<AppMngConstant.AppCleanSource, Queue<String>> mCleanHistory = new ArrayMap<>();
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

    public List<AwareProcessBlockInfo> decideAll(List<AwareProcessInfo> processInfoList, int level, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config, ArraySet<String> listFilter, boolean noList) {
        return decideAllInternal(processInfoList, level, feature, config, true, listFilter, noList);
    }

    public List<AwareProcessBlockInfo> decideAll(List<AwareProcessInfo> processInfoList, int level, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config) {
        return decideAllInternal(processInfoList, level, feature, config, true, null, false);
    }

    public List<AwareProcessBlockInfo> decideAllWithoutList(List<AwareProcessInfo> processInfoList, int level, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config) {
        return decideAllInternal(processInfoList, level, feature, config, false, null, false);
    }

    private List<AwareProcessBlockInfo> decideAllInternal(List<AwareProcessInfo> processInfoList, int level, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config, boolean shouldConsiderList, ArraySet<String> listFilter, boolean noList) {
        if (processInfoList == null) {
            return null;
        }
        List<AwareProcessBlockInfo> resultList = new ArrayList<>();
        for (AwareProcessInfo processInfo : processInfoList) {
            if (processInfo != null) {
                AwareProcessBlockInfo result = decide(processInfo, level, feature, config, shouldConsiderList, listFilter, noList);
                if (result != null) {
                    resultList.add(result);
                }
            }
        }
        return resultList;
    }

    private boolean decideSystemAppPolicy(String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
        if (status == null || (status.mFlags & 8) != 0) {
            return true;
        }
        if (tristate == 1 || AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(source) || AppMngConstant.AppStartSource.THIRD_BROADCAST.equals(source) || AppMngConstant.AppStartSource.SCHEDULE_RESTART.equals(source) || !status.mIsSystemApp) {
            return false;
        }
        if (!AwareAppStartupPolicy.isAppSelfStart(source) || !AwareIntelligentRecg.getInstance().isGmsAppAndNeedCtrl(packageName)) {
            return true;
        }
        return false;
    }

    private AwareProcessBlockInfo decide(AwareProcessInfo processInfo, int level, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config, boolean shouldConsiderList, ArraySet<String> listFilter, boolean noList) {
        ArraySet<String> listFilter2;
        AwareProcessInfo awareProcessInfo = processInfo;
        AppMngConstant.AppMngFeature appMngFeature = feature;
        AppMngConstant.EnumWithDesc enumWithDesc = config;
        boolean z = shouldConsiderList;
        ArrayMap<AppMngConstant.EnumWithDesc, Config> featureRule = this.mAllConfig.get(appMngFeature);
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(appMngFeature);
        if (listFilter == null) {
            listFilter2 = new ArraySet<>();
        } else {
            listFilter2 = listFilter;
        }
        if (awareProcessInfo.mProcInfo == null || awareProcessInfo.mProcInfo.mPackageName == null || awareProcessInfo.mProcInfo.mPackageName.isEmpty()) {
            ArrayMap<String, Integer> detailedReason = new ArrayMap<>();
            detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.NONE.ordinal()));
            detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.MISSING_PROCESS_INFO.ordinal()));
            ArrayMap<String, Integer> arrayMap = detailedReason;
            AwareProcessBlockInfo awareProcessBlockInfo = new AwareProcessBlockInfo(AppMngConstant.CleanReason.MISSING_PROCESS_INFO.getCode(), 0, awareProcessInfo, DEFAULT_APPMNG_POLICY, detailedReason);
            return awareProcessBlockInfo;
        }
        String packageName = (String) awareProcessInfo.mProcInfo.mPackageName.get(0);
        if (AppStartupUtil.isCtsPackage(packageName)) {
            ArrayMap<String, Integer> detailedReason2 = new ArrayMap<>();
            detailedReason2.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.NONE.ordinal()));
            detailedReason2.put("spec", Integer.valueOf(AppMngConstant.CleanReason.CTS.ordinal()));
            AwareProcessBlockInfo awareProcessBlockInfo2 = new AwareProcessBlockInfo(AppMngConstant.CleanReason.CTS.getCode(), 0, awareProcessInfo, DEFAULT_APPMNG_POLICY, detailedReason2);
            return awareProcessBlockInfo2;
        }
        if (z && featureList != null && !listFilter2.contains(packageName) && !noList) {
            ArrayMap<String, ListItem> list = featureList.get(enumWithDesc);
            if (list != null) {
                ListItem item = list.get(packageName);
                if (item != null) {
                    ArrayMap<String, ListItem> detailedReason3 = new ArrayMap<>();
                    detailedReason3.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(item.getPolicy()));
                    detailedReason3.put("spec", Integer.valueOf(AppMngConstant.CleanReason.LIST.ordinal()));
                    ListItem listItem = item;
                    int policy = item.getPolicy();
                    ArrayMap<String, ListItem> arrayMap2 = list;
                    AwareProcessBlockInfo awareProcessBlockInfo3 = new AwareProcessBlockInfo(AppMngConstant.CleanReason.LIST.getCode(), awareProcessInfo.mProcInfo.mUid, awareProcessInfo, policy, detailedReason3);
                    return awareProcessBlockInfo3;
                }
            }
        }
        if (featureRule != null) {
            AppMngRule rule = (AppMngRule) featureRule.get(enumWithDesc);
            if (rule != null) {
                return rule.apply(awareProcessInfo, packageName, level, z);
            }
        }
        int i = level;
        AwareProcessBlockInfo awareProcessBlockInfo4 = new AwareProcessBlockInfo(AppMngConstant.CleanReason.CONFIG_INVALID.getCode(), awareProcessInfo.mProcInfo.mUid, awareProcessInfo, DEFAULT_APPMNG_POLICY, null);
        return awareProcessBlockInfo4;
    }

    public ArrayMap<String, ListItem> getProcessList(AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config) {
        if (feature == null || config == null) {
            return null;
        }
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessList = this.mAllProcessList.get(feature);
        if (featureProcessList == null) {
            return null;
        }
        return featureProcessList.get(config);
    }

    public int getAppStartPolicy(String packageName, AppMngConstant.AppStartSource source) {
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(AppMngConstant.AppMngFeature.APP_START);
        if (featureList == null) {
            return 1;
        }
        ArrayMap<String, ListItem> list = featureList.get(source);
        if (list == null) {
            return 0;
        }
        ListItem item = list.get(packageName);
        if (item != null) {
            return item.getPolicy();
        }
        return 0;
    }

    public void addFeatureList(AppMngConstant.AppMngFeature feature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> addLists) {
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(feature);
        if (featureList != null && addLists != null && !addLists.isEmpty()) {
            ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> allList = new ArrayMap<>();
            for (Map.Entry<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> entry : this.mAllList.entrySet()) {
                allList.put(entry.getKey(), entry.getValue());
            }
            ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> newFeatureList = allList.get(feature);
            if (newFeatureList == null) {
                newFeatureList = new ArrayMap<>();
                allList.put(feature, newFeatureList);
            }
            for (Map.Entry<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> entry2 : addLists.entrySet()) {
                AppMngConstant.EnumWithDesc configEnum = entry2.getKey();
                ArrayMap<String, ListItem> policiesList = featureList.get(configEnum);
                ArrayMap<String, ListItem> newPoliciesList = new ArrayMap<>();
                addPolicies(policiesList, entry2.getValue(), newPoliciesList);
                newFeatureList.put(configEnum, newPoliciesList);
            }
            this.mAllList = allList;
        }
    }

    public ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> removeFeatureList(AppMngConstant.AppMngFeature feature, ArraySet<String> pkgs) {
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(feature);
        if (featureList == null || pkgs == null || pkgs.isEmpty()) {
            return null;
        }
        ArrayMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> allList = new ArrayMap<>();
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> removeLists = new ArrayMap<>();
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> newFeatureList = new ArrayMap<>();
        for (Map.Entry<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> entry : featureList.entrySet()) {
            AppMngConstant.EnumWithDesc configEnum = entry.getKey();
            ArrayMap<String, ListItem> policies = entry.getValue();
            if (!AppMngConstant.AppMngFeature.APP_CLEAN.equals(feature) || AppMngConstant.AppCleanSource.MEMORY.equals(configEnum) || AppMngConstant.AppCleanSource.SMART_CLEAN.equals(configEnum)) {
                ArrayMap<String, ListItem> newPolicies = new ArrayMap<>();
                ArrayMap<String, ListItem> removePolicies = new ArrayMap<>();
                removePolicies(policies, pkgs, newPolicies, removePolicies);
                newFeatureList.put(configEnum, newPolicies);
                if (removePolicies.size() > 0) {
                    removeLists.put(configEnum, removePolicies);
                }
            } else {
                newFeatureList.put(configEnum, policies);
            }
        }
        for (Map.Entry<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> entry2 : this.mAllList.entrySet()) {
            AppMngConstant.AppMngFeature appmngFeature = entry2.getKey();
            ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> lists = entry2.getValue();
            if (appmngFeature.equals(feature)) {
                allList.put(appmngFeature, newFeatureList);
            } else {
                allList.put(appmngFeature, lists);
            }
        }
        this.mAllList = allList;
        return removeLists;
    }

    private void removePolicies(ArrayMap<String, ListItem> policiesList, ArraySet<String> pkgs, ArrayMap<String, ListItem> newPoliciesList, ArrayMap<String, ListItem> removePoliciesList) {
        for (Map.Entry<String, ListItem> entry : policiesList.entrySet()) {
            String pkg = entry.getKey();
            ListItem item = entry.getValue();
            if (pkgs.contains(pkg)) {
                removePoliciesList.put(pkg, item);
            } else {
                newPoliciesList.put(pkg, item);
            }
        }
    }

    private void addPolicies(ArrayMap<String, ListItem> policiesList, ArrayMap<String, ListItem> addPoliciesList, ArrayMap<String, ListItem> newPoliciesList) {
        for (Map.Entry<String, ListItem> entry : policiesList.entrySet()) {
            newPoliciesList.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, ListItem> entry2 : addPoliciesList.entrySet()) {
            newPoliciesList.put(entry2.getKey(), entry2.getValue());
        }
    }

    public Policy decide(String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
        int policy;
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(AppMngConstant.AppMngFeature.APP_START);
        if (featureList != null) {
            ArrayMap<String, ListItem> list = featureList.get(source);
            if (list != null) {
                ListItem item = list.get(packageName);
                if (!(item == null || status == null)) {
                    if (AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(source)) {
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
                        return new AppStartPolicy(packageName, policy, AppMngConstant.AppStartReason.LIST.getDesc());
                    }
                }
            }
        }
        if (decideSystemAppPolicy(packageName, source, status, tristate)) {
            return new AppStartPolicy(packageName, 1, AppMngConstant.AppStartReason.SYSTEM_APP.getDesc());
        }
        ArrayMap<AppMngConstant.EnumWithDesc, Config> featureRule = this.mAllConfig.get(AppMngConstant.AppMngFeature.APP_START);
        if (featureRule != null) {
            AppStartRule rule = (AppStartRule) featureRule.get(source);
            if (rule != null) {
                return rule.apply(packageName, source, status, tristate);
            }
        }
        return new AppStartPolicy(packageName, 1, AppMngConstant.AppStartReason.DEFAULT.getDesc());
    }

    public void updateRule(AppMngConstant.AppMngFeature feature, Context context) {
        ConfigReader reader = new ConfigReader();
        reader.parseFile(feature, context);
        if (feature == null) {
            this.mAllConfig = reader.getConfig();
            this.mAllList = reader.getList();
            this.mAllProcessList = reader.getProcessList();
            this.mAllMisc = reader.getMisc();
            this.mAllCommonCfg = reader.getCommonCfg();
            updateMiscCache();
        } else {
            this.mAllConfig.put(feature, reader.getConfig().get(feature));
            this.mAllList.put(feature, reader.getList().get(feature));
            this.mAllProcessList.put(feature, reader.getProcessList().get(feature));
            this.mAllMisc.put(feature, reader.getMisc().get(feature));
            this.mAllCommonCfg.put(feature, reader.getCommonCfg().get(feature));
        }
        if (feature == null || AppMngConstant.AppMngFeature.APP_CLEAN.equals(feature)) {
            AwareIntelligentRecg.getInstance().removeAppCleanFeatureGMSList();
            initHistory();
        }
        if (feature == null || AppMngConstant.AppMngFeature.APP_START.equals(feature)) {
            AwareIntelligentRecg.getInstance().updateBGCheckExcludedInfo(reader.getBGCheckExcludedPkg());
            AwareIntelligentRecg.getInstance().removeAppStartFeatureGMSList();
        }
        this.mVersion = reader.getVersion();
    }

    public ArrayList<String> getRawConfig(String feature, String config) {
        if (config == null || feature == null) {
            return null;
        }
        ArrayMap<String, ArrayList<String>> rawFeature = this.mAllMisc.get(AppMngConstant.AppMngFeature.fromString(feature));
        if (rawFeature == null) {
            return null;
        }
        ArrayList<String> items = rawFeature.get(config);
        if (items == null) {
            return new ArrayList<>();
        }
        return items;
    }

    public String getCommonCfg(String feature, String name) {
        if (name == null || feature == null) {
            return null;
        }
        ArrayMap<String, String> map = this.mAllCommonCfg.get(AppMngConstant.AppMngFeature.fromString(feature));
        if (map == null) {
            return null;
        }
        return map.get(name);
    }

    public int getVersion() {
        return this.mVersion;
    }

    public void dump(PrintWriter pw, AppMngConstant.AppMngFeature feature) {
        if (pw != null) {
            ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(feature);
            if (this.mVersion != -1) {
                pw.println("config version:" + this.mVersion);
            }
            if (featureList == null) {
                pw.println("there is no [" + feature + "] list");
            } else {
                pw.println("----------[" + feature + "] Lists----------");
                for (Map.Entry<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureListEntry : featureList.entrySet()) {
                    if (featureListEntry != null) {
                        ArrayMap<String, ListItem> configList = featureListEntry.getValue();
                        if (configList != null) {
                            pw.println("==========[" + featureListEntry.getKey() + "] Lists==========");
                            for (Map.Entry<String, ListItem> configListEntry : configList.entrySet()) {
                                if (configListEntry != null) {
                                    ListItem item = configListEntry.getValue();
                                    if (item != null) {
                                        pw.println("  " + configListEntry.getKey() + ":");
                                        item.dump(pw);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ArrayMap<AppMngConstant.EnumWithDesc, Config> featureConfig = this.mAllConfig.get(feature);
            if (featureConfig == null) {
                pw.println("there is no [" + feature + "] config");
            } else {
                pw.println("----------[" + feature + "] Rules----------");
                for (Map.Entry<AppMngConstant.EnumWithDesc, Config> featureConfigEntry : featureConfig.entrySet()) {
                    if (featureConfigEntry != null) {
                        Config config = featureConfigEntry.getValue();
                        if (config != null) {
                            pw.println("==========[" + featureConfigEntry.getKey() + "] Rules==========");
                            config.dump(pw);
                        }
                    }
                }
            }
        }
    }

    public void dumpList(PrintWriter pw, AppMngConstant.AppMngFeature feature) {
        if (pw != null) {
            ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(feature);
            if (this.mVersion != -1) {
                pw.println("config version:" + this.mVersion);
            }
            if (featureList == null) {
                pw.println("there is no [" + feature + "] list");
            } else {
                pw.println("----------[" + feature + "] Lists----------");
                for (Map.Entry<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureListEntry : featureList.entrySet()) {
                    if (featureListEntry != null) {
                        ArrayMap<String, ListItem> configList = featureListEntry.getValue();
                        if (configList != null) {
                            pw.println("==========[" + featureListEntry.getKey() + "] Lists==========");
                            for (Map.Entry<String, ListItem> configListEntry : configList.entrySet()) {
                                if (configListEntry != null) {
                                    ListItem item = configListEntry.getValue();
                                    if (!(item == null || item.getPolicy() == -1)) {
                                        pw.println("  " + configListEntry.getKey() + ":");
                                        item.dump(pw);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessList = this.mAllProcessList.get(feature);
            if (featureProcessList == null) {
                pw.println("there is no [" + feature + "] processlist");
            } else {
                pw.println("----------[" + feature + "] processlist----------");
                for (Map.Entry<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessListEntry : featureProcessList.entrySet()) {
                    if (featureProcessListEntry != null) {
                        ArrayMap<String, ListItem> configProcessList = featureProcessListEntry.getValue();
                        if (configProcessList != null) {
                            pw.println("==========[" + featureProcessListEntry.getKey() + "] ProcessLists==========");
                            for (Map.Entry<String, ListItem> configProcessListEntry : configProcessList.entrySet()) {
                                if (configProcessListEntry != null) {
                                    ListItem processItem = configProcessListEntry.getValue();
                                    if (processItem != null) {
                                        pw.println("  " + configProcessListEntry.getKey() + ":");
                                        processItem.dump(pw);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void initHistory() {
        for (AppMngConstant.AppCleanSource source : AppMngConstant.AppCleanSource.values()) {
            synchronized (this.mCleanHistory) {
                this.mCleanHistory.put(source, new LinkedList());
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004c, code lost:
        return;
     */
    public void updateHistory(AppMngConstant.AppCleanSource source, String reason) {
        synchronized (this.mCleanHistory) {
            Queue<String> queue = this.mCleanHistory.get(source);
            if (queue == null) {
                AwareLog.e(TAG, "bad request = " + source);
                return;
            }
            if (queue.size() == 50 && queue.poll() == null) {
                AwareLog.e(TAG, "poll failed !");
            }
            if (!queue.offer(reason)) {
                AwareLog.e(TAG, "updateHistory failed !");
            }
        }
    }

    public void dumpHistory(PrintWriter pw, AppMngConstant.AppCleanSource source) {
        if (pw != null) {
            synchronized (this.mCleanHistory) {
                Queue<String> queue = this.mCleanHistory.get(source);
                if (queue != null) {
                    for (String history : queue) {
                        pw.println(history);
                    }
                }
            }
        }
    }

    private void updateMiscCache() {
        AwareWakeUpManager.getInstance().updateControlParam();
        AwareWakeUpManager.getInstance().updateWhiteList();
    }

    public BroadcastMngRule getBroadcastMngRule(AppMngConstant.BroadcastSource source) {
        ArrayMap<AppMngConstant.EnumWithDesc, Config> featureRule = this.mAllConfig.get(AppMngConstant.AppMngFeature.BROADCAST);
        if (featureRule != null) {
            return (BroadcastMngRule) featureRule.get(source);
        }
        return null;
    }

    public ArrayMap<String, ListItem> getBrListItem(AppMngConstant.AppMngFeature feature, AppMngConstant.BroadcastSource source) {
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(feature);
        if (featureList != null) {
            return featureList.get(source);
        }
        return null;
    }

    public void checkListForMemLowEnd(List<AwareProcessBlockInfo> needClean, List<AwareProcessBlockInfo> notClean) {
        if (needClean != null && notClean != null) {
            ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(AppMngConstant.AppMngFeature.APP_CLEAN);
            if (featureList != null && !featureList.isEmpty()) {
                ArrayMap<String, ListItem> list = featureList.get(AppMngConstant.AppCleanSource.MEMORY);
                Iterator iterator = needClean.iterator();
                while (iterator.hasNext()) {
                    AwareProcessBlockInfo info = iterator.next();
                    if (list != null) {
                        ListItem item = list.get(info.mPackageName);
                        if (item != null) {
                            int weight = item.getWeight();
                            if (weight != -1) {
                                info.mWeight = weight;
                            } else {
                                notClean.add(info);
                                iterator.remove();
                                ArrayMap<String, Integer> detailedReason = new ArrayMap<>();
                                detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(item.getPolicy()));
                                detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.LIST.ordinal()));
                                info.mDetailedReason = detailedReason;
                                info.mReason = AppMngConstant.CleanReason.LIST.getCode();
                                int policy = item.getPolicy();
                                ProcessCleaner.CleanType[] values = ProcessCleaner.CleanType.values();
                                if (values.length > policy && policy >= 0) {
                                    info.mCleanType = values[policy];
                                }
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }
}
