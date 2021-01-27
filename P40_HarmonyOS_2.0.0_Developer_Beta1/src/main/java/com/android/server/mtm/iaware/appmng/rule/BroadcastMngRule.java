package com.android.server.mtm.iaware.appmng.rule;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.content.Intent;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.mtm.iaware.appmng.rule.RuleParserUtil;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;

public class BroadcastMngRule extends Config {
    private static final int DEFAULT_POLICY = 0;
    private static final String TAG = "BroadcastMngRule";
    private static final int UNINIT_VALUE = -1;

    public BroadcastMngRule(ArrayMap<String, String> prop, RuleNode rules) {
        super(prop, rules);
    }

    public int apply(AppMngConstant.BroadcastSource source, String id, Intent intent, AwareProcessInfo processInfo) {
        if (processInfo == null) {
            return AwareBroadcastPolicy.BrCtrlType.NONE.ordinal();
        }
        return toApply(source, id, intent, processInfo, this.mRules);
    }

    public ArrayMap<String, RuleNode> getBrRules() {
        RuleNode node = this.mRules;
        if (node == null || !node.hasChild()) {
            return null;
        }
        return node.getChilds().getStringMap();
    }

    public int updateApply(AppMngConstant.BroadcastSource source, String id, Intent intent, AwareProcessInfo processInfo, RuleNode node) {
        if (processInfo == null) {
            return AwareBroadcastPolicy.BrCtrlType.NONE.ordinal();
        }
        return toApply(source, id, intent, processInfo, node);
    }

    private int toApply(AppMngConstant.BroadcastSource source, String id, Intent intent, AwareProcessInfo processInfo, RuleNode node) {
        RuleParserUtil.BroadcastTag childType;
        if (processInfo.procProcInfo == null || processInfo.procProcInfo.mPackageName == null || processInfo.procProcInfo.mPackageName.isEmpty()) {
            return AwareBroadcastPolicy.BrCtrlType.NONE.ordinal();
        }
        RuleNode.XmlValue value = null;
        while (true) {
            childType = null;
            if (node == null || !node.hasChild()) {
                break;
            }
            RuleParserUtil.TagEnum temp = node.getChildType();
            if (temp instanceof RuleParserUtil.BroadcastTag) {
                childType = (RuleParserUtil.BroadcastTag) temp;
            }
            if (childType != null) {
                RuleParserUtil.BroadcastValue broadcastValue = new RuleParserUtil.BroadcastValue();
                broadcastValue.source = source;
                broadcastValue.id = id;
                broadcastValue.intent = intent;
                broadcastValue.processInfo = processInfo;
                broadcastValue.tristate = AppMngRule.getTriState((String) processInfo.procProcInfo.mPackageName.get(0));
                node = childType.getAppliedValue(node.getChilds(), broadcastValue);
                if (node != null) {
                    value = node.getValue();
                }
            } else {
                node = null;
            }
        }
        if (!(node == null || value == null)) {
            RuleParserUtil.TagEnum temp2 = node.getCurrentType();
            if (temp2 instanceof RuleParserUtil.BroadcastTag) {
                childType = (RuleParserUtil.BroadcastTag) temp2;
            }
            if (childType != null && childType.equals(RuleParserUtil.BroadcastTag.POLICY)) {
                return value.getIntValue();
            }
        }
        return AwareBroadcastPolicy.BrCtrlType.NONE.ordinal();
    }
}
