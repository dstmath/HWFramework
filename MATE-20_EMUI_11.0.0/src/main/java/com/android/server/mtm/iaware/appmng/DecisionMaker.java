package com.android.server.mtm.iaware.appmng;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Context;
import android.rms.iaware.AwareLog;
import android.rms.utils.Utils;
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
import com.android.server.rms.iaware.feature.AppSceneMngFeature;
import com.android.server.rms.iaware.memory.policy.CachedMemoryCleanPolicy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class DecisionMaker {
    private static final int APPSTART_POLICY_NOT_ALLOW = 0;
    private static final int APPSTART_XML_POLICY_ALLOW_ALL = 2;
    private static final int APPSTART_XML_POLICY_ALLOW_SMART = 1;
    private static final int DEFAULT_APPMNG_POLICY = ProcessCleaner.CleanType.NONE.ordinal();
    private static final int DEFAULT_APPSTART_POLICY = 1;
    private static final String DEFAULT_STRING = "default";
    private static final int DEFAULT_UID = 0;
    private static final Object LOCK = new Object();
    private static final int MAX_HISTORY_LENGTH = 50;
    private static final String TAG = "DecisionMaker";
    private static final int UNINIT_VALUE = -1;
    private static DecisionMaker sDecisionMaker = null;
    private ConcurrentHashMap<AppMngConstant.AppMngFeature, ArrayMap<String, String>> mAllCommonCfg = new ConcurrentHashMap<>();
    private ConcurrentHashMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, Config>> mAllConfig = new ConcurrentHashMap<>();
    private ConcurrentHashMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> mAllList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<AppMngConstant.AppMngFeature, ArrayMap<String, ArrayList<String>>> mAllMisc = new ConcurrentHashMap<>();
    private ConcurrentHashMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> mAllProcessList = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<AppMngConstant.AppCleanSource, Queue<String>> mCleanHistory = new ConcurrentHashMap<>();
    private int mVersion;

    public static DecisionMaker getInstance() {
        DecisionMaker decisionMaker;
        synchronized (LOCK) {
            if (sDecisionMaker == null) {
                sDecisionMaker = new DecisionMaker();
            }
            decisionMaker = sDecisionMaker;
        }
        return decisionMaker;
    }

    /* access modifiers changed from: package-private */
    public static class DecideConfigInfo {
        AppMngConstant.EnumWithDesc config;
        AppMngConstant.AppMngFeature feature;
        int level;

        public DecideConfigInfo(int level2, AppMngConstant.AppMngFeature feature2, AppMngConstant.EnumWithDesc config2) {
            this.level = level2;
            this.feature = feature2;
            this.config = config2;
        }
    }

    public List<AwareProcessBlockInfo> decideAll(List<AwareProcessInfo> processInfoList, DecideConfigInfo decideConfigInfo, ArraySet<String> listFilter, boolean isConsiterPartialList) {
        return decideAllInternal(processInfoList, decideConfigInfo, true, listFilter, isConsiterPartialList);
    }

    public List<AwareProcessBlockInfo> decideAll(List<AwareProcessInfo> processInfoList, int level, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config) {
        return decideAllInternal(processInfoList, new DecideConfigInfo(level, feature, config), true, null, false);
    }

    public List<AwareProcessBlockInfo> decideAllWithoutList(List<AwareProcessInfo> processInfoList, int level, AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config) {
        return decideAllInternal(processInfoList, new DecideConfigInfo(level, feature, config), false, null, false);
    }

    private List<AwareProcessBlockInfo> decideAllInternal(List<AwareProcessInfo> processInfoList, DecideConfigInfo decideConfigInfo, boolean shouldConsiderList, ArraySet<String> listFilter, boolean isConsiterPartialList) {
        AwareProcessBlockInfo result;
        if (processInfoList == null) {
            return null;
        }
        List<AwareProcessBlockInfo> resultList = new ArrayList<>();
        for (AwareProcessInfo processInfo : processInfoList) {
            if (!(processInfo == null || (result = decide(processInfo, decideConfigInfo, shouldConsiderList, listFilter, isConsiterPartialList)) == null)) {
                resultList.add(result);
            }
        }
        return resultList;
    }

    private boolean decideSystemAppPolicy(String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int triState) {
        if (status == null || (status.cacheFlags & 8) != 0) {
            return true;
        }
        if (triState == 1 || AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(source) || AppMngConstant.AppStartSource.THIRD_BROADCAST.equals(source) || AppMngConstant.AppStartSource.SCHEDULE_RESTART.equals(source) || !status.cacheIsSystemApp) {
            return false;
        }
        if (!AwareAppStartupPolicy.isAppSelfStart(source) || !AwareIntelligentRecg.getInstance().isGmsAppAndNeedCtrl(packageName)) {
            return true;
        }
        return false;
    }

    private int decideProcessListRule(AwareProcessInfo processInfo, String packageName, int level, ArrayMap<String, ListItem> list, boolean shouldConsiderList) {
        ListItem item;
        if (AppSceneMngFeature.isEnable() && shouldConsiderList && list != null && (item = list.get(processInfo.procProcInfo.mProcessName)) != null) {
            Config config = item.getListRule();
            AppMngRule rule = null;
            if (config instanceof AppMngRule) {
                rule = (AppMngRule) config;
            }
            if (rule != null) {
                return !ProcessCleaner.CleanType.NONE.equals(rule.apply(processInfo, packageName, level, shouldConsiderList, true).procCleanType);
            }
        }
        return -1;
    }

    private AwareProcessBlockInfo decideSpecialReason(AwareProcessInfo processInfo, ProcessCleaner.CleanType cleanType, AppMngConstant.CleanReason cleanReason, boolean needReason) {
        ArrayMap<String, Integer> detailedReason = new ArrayMap<>();
        if (needReason) {
            detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(cleanType.ordinal()));
            detailedReason.put("spec", Integer.valueOf(cleanReason.ordinal()));
        }
        return new AwareProcessBlockInfo(cleanReason.getCode(), 0, processInfo, DEFAULT_APPMNG_POLICY, detailedReason);
    }

    private AwareProcessBlockInfo decideFeatureList(AwareProcessInfo processInfo, String packageName, DecideConfigInfo decideConfigInfo, boolean shouldConsiderList, boolean needReason) {
        ArrayMap<String, ListItem> list = this.mAllList.get(decideConfigInfo.feature).get(decideConfigInfo.config);
        if (list == null) {
            return null;
        }
        ListItem item = list.get(packageName);
        if (item != null) {
            if (!isListNeedRemovedForAppClean(item, processInfo, packageName, decideConfigInfo.level, shouldConsiderList)) {
                Map<String, Integer> detailedReason = null;
                if (needReason) {
                    detailedReason = new ArrayMap<>();
                    detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(item.getPolicy()));
                    detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.LIST.ordinal()));
                }
                return new AwareProcessBlockInfo(AppMngConstant.CleanReason.LIST.getCode(), processInfo.procProcInfo.mUid, processInfo, item.getPolicy(), detailedReason);
            }
        }
        return null;
    }

    private AwareProcessBlockInfo decideFeatureRule(AwareProcessInfo processInfo, String packageName, DecideConfigInfo decideConfigInfo, boolean shouldConsiderList, boolean needReason) {
        ArrayMap<AppMngConstant.EnumWithDesc, Config> featureRule = this.mAllConfig.get(decideConfigInfo.feature);
        if (featureRule != null) {
            Object obj = featureRule.get(decideConfigInfo.config);
            if (obj instanceof AppMngRule) {
                AppMngRule rule = (AppMngRule) obj;
                if (!rule.hasTriStateConfig()) {
                    return rule.apply(processInfo, packageName, decideConfigInfo.level, false, needReason);
                }
                return rule.apply(processInfo, packageName, decideConfigInfo.level, shouldConsiderList, needReason);
            }
        }
        return new AwareProcessBlockInfo(AppMngConstant.CleanReason.CONFIG_INVALID.getCode(), processInfo.procProcInfo.mUid, processInfo, DEFAULT_APPMNG_POLICY, null);
    }

    private AwareProcessBlockInfo decide(AwareProcessInfo processInfo, DecideConfigInfo decideConfigInfo, boolean shouldConsiderList, ArraySet<String> listFilter, boolean isConsiterPartialList) {
        ArrayMap<String, ListItem> processList;
        AwareProcessBlockInfo blockInfo;
        if (listFilter == null) {
            listFilter = new ArraySet<>();
        }
        boolean needReason = Utils.DEBUG || AppMngConstant.AppMngFeature.APP_CLEAN.equals(decideConfigInfo.feature);
        if (processInfo.procProcInfo == null || processInfo.procProcInfo.mPackageName == null || processInfo.procProcInfo.mPackageName.isEmpty()) {
            return decideSpecialReason(processInfo, ProcessCleaner.CleanType.NONE, AppMngConstant.CleanReason.MISSING_PROCESS_INFO, needReason);
        }
        String packageName = (String) processInfo.procProcInfo.mPackageName.get(0);
        if (AppStartupUtil.isCtsPackage(packageName)) {
            return decideSpecialReason(processInfo, ProcessCleaner.CleanType.NONE, AppMngConstant.CleanReason.CTS, needReason);
        }
        if (CloudPushManager.getInstance().isPkgInCloudData(packageName, decideConfigInfo.config)) {
            return decideSpecialReason(processInfo, ProcessCleaner.CleanType.NONE, AppMngConstant.CleanReason.CLOUD_PUSH_LIST, needReason);
        }
        if (shouldConsiderList && this.mAllList.get(decideConfigInfo.feature) != null && !listFilter.contains(packageName) && !isConsiterPartialList && (blockInfo = decideFeatureList(processInfo, packageName, decideConfigInfo, shouldConsiderList, needReason)) != null) {
            return blockInfo;
        }
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> processFeatureList = this.mAllProcessList.get(decideConfigInfo.feature);
        if (shouldConsiderList && processFeatureList != null && !listFilter.contains(packageName) && (processList = processFeatureList.get(decideConfigInfo.config)) != null) {
            processInfo.procListPolicy = decideProcessListRule(processInfo, packageName, decideConfigInfo.level, processList, shouldConsiderList);
        }
        AwareProcessBlockInfo cachedBlock = decideForCachedMemorySpec(decideConfigInfo, processInfo, needReason);
        if (cachedBlock != null) {
            return cachedBlock;
        }
        return decideFeatureRule(processInfo, packageName, decideConfigInfo, shouldConsiderList, needReason);
    }

    public ArrayMap<String, ListItem> getProcessList(AppMngConstant.AppMngFeature feature, AppMngConstant.EnumWithDesc config) {
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessList;
        if (feature == null || config == null || (featureProcessList = this.mAllProcessList.get(feature)) == null) {
            return null;
        }
        return featureProcessList.get(config);
    }

    public int getAppStartPolicy(String packageName, AppMngConstant.AppStartSource source) {
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList;
        ListItem item;
        if (CloudPushManager.getInstance().isPkgInCloudData(packageName, source) || (featureList = this.mAllList.get(AppMngConstant.AppMngFeature.APP_START)) == null) {
            return 1;
        }
        ArrayMap<String, ListItem> list = featureList.get(source);
        if (list == null || (item = list.get(packageName)) == null) {
            return 0;
        }
        return item.getPolicy();
    }

    public void addFeatureList(AppMngConstant.AppMngFeature feature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> addLists) {
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(feature);
        if (!(featureList == null || addLists == null || addLists.isEmpty())) {
            ConcurrentHashMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> allList = new ConcurrentHashMap<>();
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
        ConcurrentHashMap<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> allList = new ConcurrentHashMap<>();
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> removeLists = new ArrayMap<>();
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> newFeatureList = new ArrayMap<>();
        for (Map.Entry<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> entry : featureList.entrySet()) {
            AppMngConstant.EnumWithDesc configEnum = entry.getKey();
            ArrayMap<String, ListItem> policies = entry.getValue();
            if (!AppMngConstant.AppMngFeature.APP_CLEAN.equals(feature) || AppMngConstant.AppCleanSource.MEMORY.equals(configEnum) || AppMngConstant.AppCleanSource.SMART_CLEAN.equals(configEnum)) {
                ArrayMap<String, ListItem> newPolicies = new ArrayMap<>();
                ArrayMap<String, ListItem> needRemovePolicies = new ArrayMap<>();
                removePolicies(policies, pkgs, newPolicies, needRemovePolicies);
                newFeatureList.put(configEnum, newPolicies);
                if (!needRemovePolicies.isEmpty()) {
                    removeLists.put(configEnum, needRemovePolicies);
                }
            } else {
                newFeatureList.put(configEnum, policies);
            }
        }
        for (Map.Entry<AppMngConstant.AppMngFeature, ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>>> entry2 : this.mAllList.entrySet()) {
            AppMngConstant.AppMngFeature appMngFeature = entry2.getKey();
            ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> lists = entry2.getValue();
            if (appMngFeature.equals(feature)) {
                allList.put(appMngFeature, newFeatureList);
            } else {
                allList.put(appMngFeature, lists);
            }
        }
        this.mAllList = allList;
        return removeLists;
    }

    private void removePolicies(ArrayMap<String, ListItem> policiesList, ArraySet<String> pkgs, ArrayMap<String, ListItem> newPoliciesList, ArrayMap<String, ListItem> removePoliciesList) {
        if (policiesList != null) {
            for (Map.Entry<String, ListItem> entry : policiesList.entrySet()) {
                if (entry != null) {
                    String pkg = entry.getKey();
                    ListItem item = entry.getValue();
                    if (pkgs.contains(pkg)) {
                        removePoliciesList.put(pkg, item);
                    } else {
                        newPoliciesList.put(pkg, item);
                    }
                }
            }
        }
    }

    private void addPolicies(ArrayMap<String, ListItem> policiesList, ArrayMap<String, ListItem> addPoliciesList, ArrayMap<String, ListItem> newPoliciesList) {
        if (policiesList != null) {
            for (Map.Entry<String, ListItem> entry : policiesList.entrySet()) {
                newPoliciesList.put(entry.getKey(), entry.getValue());
            }
        }
        if (addPoliciesList != null) {
            for (Map.Entry<String, ListItem> entry2 : addPoliciesList.entrySet()) {
                newPoliciesList.put(entry2.getKey(), entry2.getValue());
            }
        }
    }

    private int getSystemBroadCastPolicy(AwareAppStartStatusCache status, ListItem item) {
        int policy = item.getPolicy(status.cacheAction);
        if (policy == -1) {
            return item.getPolicy("default");
        }
        return policy;
    }

    private int getAppStartPolicy(AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, ListItem item, boolean removeFlag) {
        if (AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(source)) {
            if (!removeFlag) {
                return getSystemBroadCastPolicy(status, item);
            }
            return -1;
        } else if (!removeFlag) {
            return item.getPolicy();
        } else {
            return -1;
        }
    }

    public Policy decide(String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int triState) {
        ListItem item;
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(AppMngConstant.AppMngFeature.APP_START);
        if (triState != 1 && CloudPushManager.getInstance().isPkgInCloudData(packageName, source)) {
            return new AppStartPolicy(packageName, 1, AppMngConstant.AppStartReason.CLOUD_PUSH_LIST.getDesc());
        }
        AppStartRule rule = null;
        ArrayMap<String, ListItem> list = featureList != null ? featureList.get(source) : null;
        if (!(list == null || (item = list.get(packageName)) == null || status == null)) {
            int policy = getAppStartPolicy(source, status, item, isListNeedRemovedForAppStart(item, packageName, source, status, triState));
            if (policy == 1) {
                policy = triState == 1 ? -1 : policy;
            } else if (policy == 2) {
                policy = 1;
            }
            if (policy != -1) {
                return new AppStartPolicy(packageName, policy, AppMngConstant.AppStartReason.LIST.getDesc());
            }
        }
        if (decideSystemAppPolicy(packageName, source, status, triState)) {
            return new AppStartPolicy(packageName, 1, AppMngConstant.AppStartReason.SYSTEM_APP.getDesc());
        }
        ArrayMap<AppMngConstant.EnumWithDesc, Config> featureRule = this.mAllConfig.get(AppMngConstant.AppMngFeature.APP_START);
        if (featureRule != null) {
            Config temp = featureRule.get(source);
            if (temp instanceof AppStartRule) {
                rule = (AppStartRule) temp;
            }
            if (rule != null) {
                return rule.apply(packageName, source, status, triState);
            }
        }
        return new AppStartPolicy(packageName, 1, AppMngConstant.AppStartReason.DEFAULT.getDesc());
    }

    public void updateRule(AppMngConstant.AppMngFeature feature, Context context) {
        ConfigReader reader = new ConfigReader();
        reader.parseFile(feature, context);
        if (feature == null) {
            this.mAllConfig = new ConcurrentHashMap<>(reader.getConfig());
            this.mAllList = new ConcurrentHashMap<>(reader.getList());
            this.mAllProcessList = new ConcurrentHashMap<>(reader.getProcessList());
            this.mAllMisc = new ConcurrentHashMap<>(reader.getMisc());
            this.mAllCommonCfg = new ConcurrentHashMap<>(reader.getCommonCfg());
            updateMiscCache();
        } else {
            if (reader.getConfig().get(feature) != null) {
                this.mAllConfig.put(feature, reader.getConfig().get(feature));
            }
            if (reader.getList().get(feature) != null) {
                this.mAllList.put(feature, reader.getList().get(feature));
            }
            if (reader.getProcessList().get(feature) != null) {
                this.mAllProcessList.put(feature, reader.getProcessList().get(feature));
            }
            if (reader.getMisc().get(feature) != null) {
                this.mAllMisc.put(feature, reader.getMisc().get(feature));
            }
            if (reader.getCommonCfg().get(feature) != null) {
                this.mAllCommonCfg.put(feature, reader.getCommonCfg().get(feature));
            }
        }
        if (feature == null || AppMngConstant.AppMngFeature.APP_CLEAN.equals(feature)) {
            AwareIntelligentRecg.getInstance().removeAppCleanFeatureGmsList();
            initHistory();
        }
        if (feature == null || AppMngConstant.AppMngFeature.APP_START.equals(feature)) {
            AwareIntelligentRecg.getInstance().updateBgCheckExcludedInfo(reader.getBgCheckExcludedPkg());
            AwareIntelligentRecg.getInstance().removeAppStartFeatureGmsList();
        }
        this.mVersion = reader.getVersion();
    }

    public ArrayList<String> getRawConfig(String feature, String config) {
        ArrayMap<String, ArrayList<String>> rawFeature;
        if (config == null || feature == null || (rawFeature = this.mAllMisc.get(AppMngConstant.AppMngFeature.fromString(feature))) == null) {
            return null;
        }
        ArrayList<String> items = rawFeature.get(config);
        if (items == null) {
            return new ArrayList<>();
        }
        return items;
    }

    public String getCommonCfg(String feature, String name) {
        ArrayMap<String, String> map;
        if (name == null || feature == null || (map = this.mAllCommonCfg.get(AppMngConstant.AppMngFeature.fromString(feature))) == null) {
            return null;
        }
        return map.get(name);
    }

    public int getVersion() {
        return this.mVersion;
    }

    public void dump(PrintWriter pw, AppMngConstant.AppMngFeature feature) {
        ArrayMap<String, ListItem> configList;
        ListItem item;
        if (pw != null) {
            ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(feature);
            if (this.mVersion != -1) {
                pw.println("config version:" + this.mVersion);
            }
            if (featureList == null) {
                pw.println("there is no [" + feature + "] list");
                executeDump(pw, feature);
                return;
            }
            pw.println("----------[" + feature + "] Lists----------");
            for (Map.Entry<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureListEntry : featureList.entrySet()) {
                if (!(featureListEntry == null || (configList = featureListEntry.getValue()) == null)) {
                    pw.println("==========[" + featureListEntry.getKey() + "] Lists==========");
                    for (Map.Entry<String, ListItem> configListEntry : configList.entrySet()) {
                        if (!(configListEntry == null || (item = configListEntry.getValue()) == null)) {
                            pw.println("  " + configListEntry.getKey() + ":");
                            item.dump(pw);
                        }
                    }
                }
            }
            executeDump(pw, feature);
        }
    }

    private void executeDump(PrintWriter pw, AppMngConstant.AppMngFeature feature) {
        Config config;
        ArrayMap<AppMngConstant.EnumWithDesc, Config> featureConfig = this.mAllConfig.get(feature);
        if (featureConfig == null) {
            pw.println("there is no [" + feature + "] config");
            return;
        }
        pw.println("----------[" + feature + "] Rules----------");
        for (Map.Entry<AppMngConstant.EnumWithDesc, Config> featureConfigEntry : featureConfig.entrySet()) {
            if (!(featureConfigEntry == null || (config = featureConfigEntry.getValue()) == null)) {
                pw.println("==========[" + featureConfigEntry.getKey() + "] Rules==========");
                config.dump(pw);
            }
        }
    }

    public void dumpList(PrintWriter pw, AppMngConstant.AppMngFeature feature) {
        ArrayMap<String, ListItem> configList;
        ListItem item;
        if (pw != null) {
            ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(feature);
            if (this.mVersion != -1) {
                pw.println("config version:" + this.mVersion);
            }
            if (featureList == null) {
                pw.println("there is no [" + feature + "] list");
                executeDumpList(pw, feature);
                return;
            }
            pw.println("----------[" + feature + "] Lists----------");
            for (Map.Entry<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureListEntry : featureList.entrySet()) {
                if (!(featureListEntry == null || (configList = featureListEntry.getValue()) == null)) {
                    pw.println("==========[" + featureListEntry.getKey() + "] Lists==========");
                    for (Map.Entry<String, ListItem> configListEntry : configList.entrySet()) {
                        if (!(configListEntry == null || (item = configListEntry.getValue()) == null)) {
                            pw.println("  " + configListEntry.getKey() + ":");
                            item.dump(pw);
                        }
                    }
                }
            }
            executeDumpList(pw, feature);
        }
    }

    private void executeDumpList(PrintWriter pw, AppMngConstant.AppMngFeature feature) {
        ArrayMap<String, ListItem> configProcessList;
        ListItem processItem;
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessList = this.mAllProcessList.get(feature);
        if (featureProcessList == null) {
            pw.println("there is no [" + feature + "] processlist");
            return;
        }
        pw.println("----------[" + feature + "] processlist----------");
        for (Map.Entry<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureProcessListEntry : featureProcessList.entrySet()) {
            if (!(featureProcessListEntry == null || (configProcessList = featureProcessListEntry.getValue()) == null)) {
                pw.println("==========[" + featureProcessListEntry.getKey() + "] ProcessLists==========");
                for (Map.Entry<String, ListItem> configProcessListEntry : configProcessList.entrySet()) {
                    if (!(configProcessListEntry == null || (processItem = configProcessListEntry.getValue()) == null)) {
                        pw.println("  " + configProcessListEntry.getKey() + ":");
                        processItem.dump(pw);
                    }
                }
            }
        }
    }

    private void initHistory() {
        AppMngConstant.AppCleanSource[] values = AppMngConstant.AppCleanSource.values();
        for (AppMngConstant.AppCleanSource source : values) {
            synchronized (this.mCleanHistory) {
                this.mCleanHistory.put(source, new LinkedList());
            }
        }
    }

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
        if (featureRule == null) {
            return null;
        }
        Config temp = featureRule.get(source);
        return temp instanceof BroadcastMngRule ? (BroadcastMngRule) temp : null;
    }

    public ArrayMap<String, ListItem> getBrListItem(AppMngConstant.AppMngFeature feature, AppMngConstant.BroadcastSource source) {
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList = this.mAllList.get(feature);
        if (featureList != null) {
            return featureList.get(source);
        }
        return null;
    }

    public void checkListForMemLowEnd(List<AwareProcessBlockInfo> needClean, List<AwareProcessBlockInfo> keep, int level) {
        ArrayMap<AppMngConstant.EnumWithDesc, ArrayMap<String, ListItem>> featureList;
        ArrayMap<String, ListItem> list;
        ListItem item;
        if (needClean != null && keep != null && (featureList = this.mAllList.get(AppMngConstant.AppMngFeature.APP_CLEAN)) != null && !featureList.isEmpty() && (list = featureList.get(AppMngConstant.AppCleanSource.MEMORY)) != null) {
            Iterator<AwareProcessBlockInfo> iterator = needClean.iterator();
            while (iterator.hasNext()) {
                AwareProcessBlockInfo info = iterator.next();
                if (!(info == null || (item = list.get(info.procPackageName)) == null)) {
                    int weight = item.getWeight();
                    if (weight != -1) {
                        info.procWeight = weight;
                    } else if (isListNeedKeepForLowLevel(item, info, level)) {
                        keep.add(info);
                        iterator.remove();
                        ArrayMap<String, Integer> detailedReason = new ArrayMap<>();
                        detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(item.getPolicy()));
                        detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.LIST.ordinal()));
                        info.procDetailedReason = detailedReason;
                        info.procReason = AppMngConstant.CleanReason.LIST.getCode();
                        int policy = item.getPolicy();
                        ProcessCleaner.CleanType[] values = ProcessCleaner.CleanType.values();
                        if (values.length > policy && policy >= 0) {
                            info.procCleanType = values[policy];
                        }
                    }
                }
            }
        }
    }

    private boolean isListNeedRemovedForAppClean(ListItem item, AwareProcessInfo processInfo, String packageName, int level, boolean shouldConsiderList) {
        Config config = item.getListRule();
        AppMngRule rule = null;
        if (config instanceof AppMngRule) {
            rule = (AppMngRule) config;
        }
        if (!AppSceneMngFeature.isEnable() || rule == null) {
            return false;
        }
        return rule.apply(processInfo, packageName, level, shouldConsiderList, true).procCleanType.ordinal() != 0;
    }

    private boolean isListNeedRemovedForAppStart(ListItem item, String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int triState) {
        Config config;
        if (AppMngConstant.AppStartSource.SYSTEM_BROADCAST.equals(source)) {
            config = item.getSysBroadcastRule(status.cacheAction);
            if (config == null) {
                config = item.getSysBroadcastRule("default");
            }
        } else {
            config = item.getListRule();
        }
        AppStartRule rule = null;
        if (config instanceof AppStartRule) {
            rule = (AppStartRule) config;
        }
        boolean isListNeedRemoved = false;
        if (AppSceneMngFeature.isEnable() && rule != null) {
            Policy info = rule.apply(packageName, source, status, triState);
            if (info instanceof AppStartPolicy) {
                isListNeedRemoved = ((AppStartPolicy) info).getPolicy() != 0;
            }
        }
        if (AwareIntelligentRecg.getInstance().isGmsControlApp(packageName)) {
            return true;
        }
        return isListNeedRemoved;
    }

    private boolean isListNeedKeepForLowLevel(ListItem item, AwareProcessBlockInfo info, int level) {
        List<AwareProcessInfo> procProcessList;
        AwareProcessBlockInfo resultInfo;
        if (!AppSceneMngFeature.isEnable() || item == null || info == null) {
            return true;
        }
        Config config = item.getListRule();
        AppMngRule rule = null;
        if (config instanceof AppMngRule) {
            rule = (AppMngRule) config;
        }
        if (rule == null || (procProcessList = info.procProcessList) == null) {
            return true;
        }
        for (AwareProcessInfo processInfo : procProcessList) {
            if (!(processInfo == null || (resultInfo = rule.apply(processInfo, info.procPackageName, level, true, true)) == null || resultInfo.procCleanType.ordinal() != 0)) {
                return true;
            }
        }
        return false;
    }

    public List<AwareProcessBlockInfo> decideForCachedMem(List<AwareProcessInfo> awareProcs) {
        return decideAllInternal(awareProcs, new DecideConfigInfo(0, AppMngConstant.AppMngFeature.APP_CLEAN, AppMngConstant.AppCleanSource.CACHED_MEMORY), true, null, false);
    }

    private boolean isCachedClean(DecideConfigInfo decideConfigInfo) {
        if (!AppMngConstant.AppMngFeature.APP_CLEAN.equals(decideConfigInfo.feature)) {
            return false;
        }
        return AppMngConstant.AppCleanSource.CACHED_MEMORY.equals(decideConfigInfo.config);
    }

    private boolean isCachedMemoryCleanFilter(AwareProcessInfo processInfo) {
        if (processInfo.procProcInfo == null) {
            return false;
        }
        return CachedMemoryCleanPolicy.getInstance().isFilterUid(processInfo.procProcInfo.mUid);
    }

    private AwareProcessBlockInfo decideForCachedMemorySpec(DecideConfigInfo decideConfigInfo, AwareProcessInfo processInfo, boolean needReason) {
        if (isCachedClean(decideConfigInfo) && isCachedMemoryCleanFilter(processInfo)) {
            return decideSpecialReason(processInfo, ProcessCleaner.CleanType.NONE, AppMngConstant.CleanReason.CACHED_KEY_APP, needReason);
        }
        return null;
    }
}
