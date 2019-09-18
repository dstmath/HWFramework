package com.android.server.mtm.iaware.appmng.rule;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.os.UserHandle;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.policy.AppStartPolicy;
import com.android.server.mtm.iaware.appmng.policy.Policy;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.rms.iaware.appmng.AwareAppStartStatusCache;
import com.android.server.rms.iaware.appmng.AwareIntelligentRecg;

public class AppStartRule extends Config {
    private static final int DEFAULT_POLICY = 0;
    private static final String TAG = "AppStartRule";
    private static final int UNINIT_VALUE = -1;

    public AppStartRule(ArrayMap<String, String> prop, RuleNode rules) {
        super(prop, rules);
    }

    public Policy apply(String packageName, AppMngConstant.AppStartSource source, AwareAppStartStatusCache status, int tristate) {
        RuleNode node = this.mRules;
        RuleNode.XmlValue value = null;
        while (node != null && node.hasChild()) {
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
        if (!(node == null || value == null)) {
            RuleParserUtil.AppStartTag type = (RuleParserUtil.AppStartTag) node.getCurrentType();
            if (type != null && type.equals(RuleParserUtil.AppStartTag.POLICY)) {
                if (value.getHwStop() == -1 || !AwareIntelligentRecg.getInstance().isPkgHasHwStopFlag(UserHandle.getUserId(status.mUid), packageName)) {
                    return new AppStartPolicy(packageName, value.getIntValue(), value.getIndex());
                }
                return new AppStartPolicy(packageName, value.getHwStop(), value.getIndex());
            }
        }
        return new AppStartPolicy(packageName, 0, AppMngConstant.AppStartReason.DEFAULT.getDesc());
    }
}
