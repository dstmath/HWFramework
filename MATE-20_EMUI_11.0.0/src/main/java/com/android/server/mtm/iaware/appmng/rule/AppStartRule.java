package com.android.server.mtm.iaware.appmng.rule;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.policy.AppStartPolicy;
import com.android.server.mtm.iaware.appmng.policy.Policy;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.rms.iaware.appmng.AwareAppStartStatusCache;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;
import com.huawei.android.os.UserHandleEx;

public class AppStartRule extends Config {
    private static final int DEFAULT_POLICY = 0;

    public AppStartRule(ArrayMap<String, String> prop, RuleNode rules) {
        super(prop, rules);
    }

    public Policy apply(String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
        RuleParserUtil.AppStartTag type;
        RuleNode node = this.mRules;
        RuleNode.XmlValue value = null;
        while (node != null && node.hasChild()) {
            if (node.getChildType() instanceof RuleParserUtil.AppStartTag) {
                RuleParserUtil.AppStartTag childType = (RuleParserUtil.AppStartTag) node.getChildType();
                if (childType != null) {
                    node = childType.getAppliedValue(node.getChilds(), packageName, source, status, tristate);
                    if (node != null) {
                        value = node.getValue();
                    }
                } else {
                    node = null;
                }
            }
        }
        if (node == null || value == null || !(node.getCurrentType() instanceof RuleParserUtil.AppStartTag) || (type = (RuleParserUtil.AppStartTag) node.getCurrentType()) == null || !RuleParserUtil.AppStartTag.POLICY.equals(type)) {
            return new AppStartPolicy(packageName, 0, AppMngConstant.AppStartReason.DEFAULT.getDesc());
        }
        if (value.getHwStop() == -1 || !AwareIntelligentRecg.getInstance().isPkgHasHwStopFlag(UserHandleEx.getUserId(status.cacheUid), packageName)) {
            return new AppStartPolicy(packageName, value.getIntValue(), value.getIndex());
        }
        return new AppStartPolicy(packageName, value.getHwStop(), value.getIndex());
    }
}
