package com.android.server.mtm.iaware.appmng.rule;

import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.taskstatus.ProcessCleaner;

public class AppMngRule extends Config {
    private static final int MAX_DEPTH = 7;
    private static final String TAG = "AppMngRule";
    private static final String TAG_SCOPE = "scope";
    private static final String VALUE_TRI_ONLY = "tri_only";
    private boolean mIsScopeAll;

    public AppMngRule(ArrayMap<String, String> prop, RuleNode rules) {
        super(prop, rules);
        if (this.mProperties == null) {
            return;
        }
        if (VALUE_TRI_ONLY.equals((String) this.mProperties.get("scope"))) {
            this.mIsScopeAll = false;
        } else {
            this.mIsScopeAll = true;
        }
    }

    private static int getTriState(HwAppStartupSetting setting) {
        if (1 == setting.getPolicy(0)) {
            if (1 != setting.getPolicy(3)) {
                return 1;
            }
            if (2 == setting.getModifier(0)) {
                return 4;
            }
            return 3;
        } else if (1 == setting.getPolicy(3)) {
            return 2;
        } else {
            return 0;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00a9  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00bf  */
    public AwareProcessBlockInfo apply(AwareProcessInfo processInfo, String packageName, int level, boolean shouldConsiderList) {
        String str;
        int tristate;
        RuleNode node;
        RuleParserUtil.AppMngTag type;
        RuleParserUtil.AppMngTag childType;
        RuleNode node2;
        AwareProcessInfo awareProcessInfo = processInfo;
        RuleNode ruleNode = this.mRules;
        StringBuilder reason = new StringBuilder();
        HwAppStartupSetting detailedReason = new ArrayMap(7);
        if (shouldConsiderList) {
            HwAppStartupSetting setting = null;
            AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
            if (policy != null) {
                str = packageName;
                setting = policy.getAppStartupSetting(str);
            } else {
                str = packageName;
            }
            HwAppStartupSetting setting2 = setting;
            if (setting2 != null) {
                tristate = getTriState(setting2);
                RuleNode.XmlValue value = null;
                node = ruleNode;
                while (node != null && node.hasChild()) {
                    childType = (RuleParserUtil.AppMngTag) node.getChildType();
                    if (childType == null) {
                        node2 = childType.getAppliedValue(node.getChilds(), awareProcessInfo, level, str, tristate);
                        if (node2 != null) {
                            RuleNode.XmlValue value2 = node2.getValue();
                            collectReason(reason, detailedReason, node2.getCurrentType(), value2);
                            node = node2;
                            value = value2;
                        }
                    } else {
                        node2 = null;
                    }
                    node = node2;
                }
                if (!(node == null || value == null)) {
                    type = (RuleParserUtil.AppMngTag) node.getCurrentType();
                    if (RuleParserUtil.AppMngTag.POLICY.equals(type)) {
                        RuleParserUtil.AppMngTag appMngTag = type;
                        AwareProcessBlockInfo awareProcessBlockInfo = new AwareProcessBlockInfo(reason.toString(), awareProcessInfo.mProcInfo.mUid, awareProcessInfo, value.getIntValue(), detailedReason, value.getWeight());
                        return awareProcessBlockInfo;
                    }
                }
                detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.NONE.ordinal()));
                detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.CONFIG_INVALID.ordinal()));
                AwareProcessBlockInfo awareProcessBlockInfo2 = new AwareProcessBlockInfo(AppMngConstant.CleanReason.CONFIG_INVALID.getCode(), awareProcessInfo.mProcInfo.mUid, awareProcessInfo, ProcessCleaner.CleanType.NONE.ordinal(), detailedReason);
                return awareProcessBlockInfo2;
            } else if (this.mIsScopeAll == 0) {
                detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.NONE.ordinal()));
                detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.OUT_OF_SCOPE.ordinal()));
                HwAppStartupSetting hwAppStartupSetting = setting2;
                AwareProcessBlockInfo awareProcessBlockInfo3 = new AwareProcessBlockInfo(AppMngConstant.CleanReason.OUT_OF_SCOPE.getCode(), awareProcessInfo.mProcInfo.mUid, awareProcessInfo, ProcessCleaner.CleanType.NONE.ordinal(), detailedReason);
                return awareProcessBlockInfo3;
            }
        } else {
            str = packageName;
        }
        tristate = -1;
        RuleNode.XmlValue value3 = null;
        node = ruleNode;
        while (node != null) {
            childType = (RuleParserUtil.AppMngTag) node.getChildType();
            if (childType == null) {
            }
            node = node2;
        }
        type = (RuleParserUtil.AppMngTag) node.getCurrentType();
        if (RuleParserUtil.AppMngTag.POLICY.equals(type)) {
        }
        detailedReason.put(RuleParserUtil.AppMngTag.POLICY.getDesc(), Integer.valueOf(ProcessCleaner.CleanType.NONE.ordinal()));
        detailedReason.put("spec", Integer.valueOf(AppMngConstant.CleanReason.CONFIG_INVALID.ordinal()));
        AwareProcessBlockInfo awareProcessBlockInfo22 = new AwareProcessBlockInfo(AppMngConstant.CleanReason.CONFIG_INVALID.getCode(), awareProcessInfo.mProcInfo.mUid, awareProcessInfo, ProcessCleaner.CleanType.NONE.ordinal(), detailedReason);
        return awareProcessBlockInfo22;
    }

    private void collectReason(StringBuilder reason, ArrayMap<String, Integer> detailedReason, RuleParserUtil.TagEnum type, RuleNode.XmlValue value) {
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
