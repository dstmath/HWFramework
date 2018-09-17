package com.android.server.mtm.iaware.appmng.rule;

import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.appmng.AppMngConstant.CleanReason;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.iaware.appmng.rule.RuleNode.XmlValue;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.AppMngTag;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil.TagEnum;
import com.android.server.mtm.taskstatus.ProcessCleaner.CleanType;
import java.util.HashMap;

public class AppMngRule extends Config {
    private static final int MAX_DEPTH = 7;
    private static final String TAG = "AppMngRule";
    private static final String TAG_SCOPE = "scope";
    private static final String VALUE_TRI_ONLY = "tri_only";
    private boolean mIsScopeAll;

    public AppMngRule(HashMap<String, String> prop, RuleNode rules) {
        super(prop, rules);
        if (this.mProperties != null) {
            if (VALUE_TRI_ONLY.equals((String) this.mProperties.get("scope"))) {
                this.mIsScopeAll = false;
            } else {
                this.mIsScopeAll = true;
            }
        }
    }

    private int getTriState(HwAppStartupSetting setting) {
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

    public AwareProcessBlockInfo apply(AwareProcessInfo processInfo, String packageName, int level, boolean shouldConsiderList) {
        RuleNode node = this.mRules;
        XmlValue value = null;
        StringBuilder reason = new StringBuilder();
        HashMap<String, Integer> detailedReason = new HashMap(7);
        int tristate = -1;
        if (shouldConsiderList) {
            HwAppStartupSetting setting = null;
            AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
            if (policy != null) {
                setting = policy.getAppStartupSetting(packageName);
            }
            if (setting != null) {
                tristate = getTriState(setting);
            } else if (!this.mIsScopeAll) {
                detailedReason.put(AppMngTag.POLICY.getDesc(), Integer.valueOf(CleanType.NONE.ordinal()));
                detailedReason.put("spec", Integer.valueOf(CleanReason.OUT_OF_SCOPE.ordinal()));
                return new AwareProcessBlockInfo(CleanReason.OUT_OF_SCOPE.getCode(), processInfo.mProcInfo.mUid, processInfo, CleanType.NONE.ordinal(), detailedReason);
            }
        }
        while (node != null && node.hasChild()) {
            AppMngTag childType = (AppMngTag) node.getChildType();
            if (childType != null) {
                node = childType.getAppliedValue(node.getChilds(), processInfo, level, packageName, tristate);
                if (node != null) {
                    value = node.getValue();
                    collectReason(reason, detailedReason, node.getCurrentType(), value);
                }
            } else {
                node = null;
            }
        }
        if (!(node == null || value == null)) {
            if (AppMngTag.POLICY.equals((AppMngTag) node.getCurrentType())) {
                return new AwareProcessBlockInfo(reason.toString(), processInfo.mProcInfo.mUid, processInfo, value.getIntValue(), detailedReason, value.getWeight());
            }
        }
        detailedReason.put(AppMngTag.POLICY.getDesc(), Integer.valueOf(CleanType.NONE.ordinal()));
        detailedReason.put("spec", Integer.valueOf(CleanReason.CONFIG_INVALID.ordinal()));
        return new AwareProcessBlockInfo(CleanReason.CONFIG_INVALID.getCode(), processInfo.mProcInfo.mUid, processInfo, CleanType.NONE.ordinal(), detailedReason);
    }

    private void collectReason(StringBuilder reason, HashMap<String, Integer> detailedReason, TagEnum type, XmlValue value) {
        if (type != null && value != null) {
            reason.append(type);
            reason.append(":");
            reason.append(value);
            reason.append(" ");
            detailedReason.put(type.getDesc(), Integer.valueOf(value.getIntValue()));
        }
    }
}
