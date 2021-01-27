package com.android.server.mtm.iaware.appmng.rule;

import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.util.ArrayMap;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import java.util.Map;

public class AppMngRule extends Config {
    private static final int MAX_DEPTH = 7;
    private static final String TAG = "AppMngRule";
    public static final String TAG_SCOPE = "scope";
    public static final String VALUE_ALL = "all";
    private static final String VALUE_TRI_ONLY = "tri_only";
    private boolean mHasTriStateConfig;
    private boolean mIsScopeAll;

    public AppMngRule(ArrayMap<String, String> prop, RuleNode rules) {
        this(prop, rules, false);
    }

    public AppMngRule(ArrayMap<String, String> prop, RuleNode rules, boolean hasTriStateConfig) {
        super(prop, rules);
        this.mHasTriStateConfig = false;
        if (this.mProperties != null) {
            String scope = (String) this.mProperties.get(TAG_SCOPE);
            if (VALUE_TRI_ONLY.equals(scope)) {
                this.mIsScopeAll = false;
            } else if (VALUE_ALL.equals(scope)) {
                this.mIsScopeAll = true;
            } else {
                this.mIsScopeAll = true;
            }
        }
        this.mHasTriStateConfig = hasTriStateConfig;
    }

    public boolean hasTriStateConfig() {
        return this.mHasTriStateConfig;
    }

    private static int getTriState(HwAppStartupSetting setting) {
        if (setting == null) {
            return -1;
        }
        if (setting.getPolicy(0) == 1) {
            if (setting.getPolicy(3) != 1) {
                return 1;
            }
            if (setting.getModifier(0) == 2) {
                return 4;
            }
            return 3;
        } else if (setting.getPolicy(3) == 1) {
            return 2;
        } else {
            return 0;
        }
    }

    private AwareProcessBlockInfo getOutScopeProcessBlockInfo(AwareProcessInfo processInfo, Map<String, Integer> detailedReason, boolean needReason) {
        if (needReason) {
            detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.NONE.ordinal()));
            detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.OUT_OF_SCOPE.ordinal()));
        }
        return new AwareProcessBlockInfo(AppMngConstant.CleanReason.OUT_OF_SCOPE.getCode(), processInfo.procProcInfo.mUid, processInfo, ProcessCleaner.CleanType.NONE.ordinal(), detailedReason);
    }

    public AwareProcessBlockInfo apply(AwareProcessInfo processInfo, String packageName, int level, boolean shouldConsiderList, boolean needReason) {
        int tristate;
        RuleNode.XmlValue value;
        RuleNode node;
        RuleParserUtil.AppMngTag type;
        RuleNode node2 = this.mRules;
        StringBuilder reason = new StringBuilder();
        Map<String, Integer> detailedReason = new ArrayMap<>(7);
        if (shouldConsiderList) {
            HwAppStartupSetting setting = null;
            AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
            if (policy != null) {
                setting = policy.getAppStartupSetting(packageName);
            }
            int tristate2 = getProcessTristate(setting, processInfo, packageName);
            if (setting == null && !this.mIsScopeAll) {
                return getOutScopeProcessBlockInfo(processInfo, detailedReason, needReason);
            }
            node = node2;
            value = null;
            tristate = tristate2;
        } else {
            node = node2;
            value = null;
            tristate = -1;
        }
        while (node != null && node.hasChild()) {
            RuleParserUtil.AppMngTag childType = getAppMngChildType(node);
            if (childType == null) {
                node = null;
            } else {
                node = childType.getAppliedValue(node.getChilds(), processInfo, level, packageName, tristate);
                if (node != null) {
                    value = node.getValue();
                    if (needReason) {
                        collectReason(reason, detailedReason, node.getCurrentType(), value);
                    }
                }
            }
        }
        if (node == null || value == null || (type = getAppMngCurrentType(node)) == null || !RuleParserUtil.AppMngTag.POLICY.equals(type)) {
            if (needReason) {
                detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.NONE.ordinal()));
                detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.CONFIG_INVALID.ordinal()));
            }
            return new AwareProcessBlockInfo(AppMngConstant.CleanReason.CONFIG_INVALID.getCode(), processInfo.procProcInfo.mUid, processInfo, ProcessCleaner.CleanType.NONE.ordinal(), detailedReason);
        }
        AwareProcessBlockInfo blockInfo = new AwareProcessBlockInfo(reason.toString(), processInfo.procProcInfo.mUid, processInfo, value.getIntValue(), detailedReason);
        blockInfo.procWeight = value.getWeight();
        blockInfo.procSwapIndex = value.getSwap();
        return blockInfo;
    }

    private int getProcessTristate(HwAppStartupSetting setting, AwareProcessInfo processInfo, String packageName) {
        if (setting == null) {
            return -1;
        }
        int tristate = getTriState(setting);
        if (tristate != 2 || !AwareAppMngSort.getInstance().isCachedCleanApps(packageName) || processInfo.procProcInfo.mCurAdj < HwActivityManagerService.CACHED_APP_MIN_ADJ) {
            return tristate;
        }
        return 0;
    }

    private RuleParserUtil.AppMngTag getAppMngChildType(RuleNode node) {
        RuleParserUtil.TagEnum temp = node.getChildType();
        if (temp instanceof RuleParserUtil.AppMngTag) {
            return (RuleParserUtil.AppMngTag) temp;
        }
        return null;
    }

    private RuleParserUtil.AppMngTag getAppMngCurrentType(RuleNode node) {
        RuleParserUtil.TagEnum temp = node.getCurrentType();
        if (temp instanceof RuleParserUtil.AppMngTag) {
            return (RuleParserUtil.AppMngTag) temp;
        }
        return null;
    }

    private void collectReason(StringBuilder reason, Map<String, Integer> detailedReason, RuleParserUtil.TagEnum type, RuleNode.XmlValue value) {
        if (type != null && value != null) {
            reason.append(type);
            reason.append(":");
            reason.append(value);
            reason.append(" ");
            detailedReason.put(type.getDesc(), Integer.valueOf(value.getIntValue()));
        }
    }

    public static int getTriState(String pkgName) {
        AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
        HwAppStartupSetting setting = null;
        if (policy != null) {
            setting = policy.getAppStartupSetting(pkgName);
        }
        if (setting != null) {
            return getTriState(setting);
        }
        return -1;
    }
}
