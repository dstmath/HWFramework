package com.android.server.mtm.iaware.appmng.rule;

import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.rule.RuleNode;
import com.android.server.rms.memrepair.ProcStateStatisData;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Config {
    ArrayMap<String, String> mProperties;
    RuleNode mRules;

    public Config(ArrayMap<String, String> prop, RuleNode rules) {
        this.mProperties = prop;
        this.mRules = rules;
    }

    private void printRuleNodeValue(PrintWriter pw, RuleNode node, int depth, RuleNode.XmlValue value) {
        if (value.isString()) {
            pw.println(ProcStateStatisData.SEPERATOR_CHAR + getDepthString(depth) + node.getCurrentType() + ":" + value.getStringValue());
        } else {
            pw.println(ProcStateStatisData.SEPERATOR_CHAR + getDepthString(depth) + node.getCurrentType() + ":" + value.getIntValue());
        }
        String index = value.getIndex();
        if (index != null) {
            pw.println(ProcStateStatisData.SEPERATOR_CHAR + getDepthString(depth) + "index:" + index);
        }
        int hwStop = value.getHwStop();
        if (hwStop != -1) {
            pw.println(ProcStateStatisData.SEPERATOR_CHAR + getDepthString(depth) + "hwStop:" + hwStop);
        }
    }

    private void printChildRuleItegerMap(PrintWriter pw, RuleNode.StringIntegerMap childs, int depth) {
        ArrayList<Integer> sortedList = childs.getSortedList();
        if (sortedList != null) {
            StringBuilder sb = new StringBuilder();
            int listSize = sortedList.size();
            for (int i = 0; i < listSize; i++) {
                sb.append(sortedList.get(i));
                sb.append(" ");
            }
            pw.println(ProcStateStatisData.SEPERATOR_CHAR + getDepthString(depth) + "sorted child:" + sb.toString());
        }
        LinkedHashMap<Integer, RuleNode> integerMap = childs.getIntegerMap();
        if (integerMap != null) {
            for (Integer key : integerMap.keySet()) {
                if (key == null) {
                    pw.println("bad config key == null");
                    return;
                }
                printRuleNode(pw, integerMap.get(key), depth + 1);
            }
        }
    }

    private void printRuleNode(PrintWriter pw, RuleNode node, int depth) {
        if (!(node == null || pw == null)) {
            RuleNode.XmlValue value = node.getValue();
            if (value != null) {
                printRuleNodeValue(pw, node, depth, value);
            }
            if (node.hasChild()) {
                RuleNode.StringIntegerMap childs = node.getChilds();
                if (childs.isStringMap()) {
                    for (Map.Entry<String, RuleNode> entry : childs.getStringMap().entrySet()) {
                        printRuleNode(pw, entry.getValue(), depth + 1);
                    }
                    return;
                }
                printChildRuleItegerMap(pw, childs, depth);
            }
        }
    }

    private String getDepthString(int depth) {
        StringBuilder sb = new StringBuilder(depth);
        for (int i = 0; i < depth; i++) {
            sb.append("--");
        }
        return sb.toString();
    }

    public void dump(PrintWriter pw) {
        printRuleNode(pw, this.mRules, 0);
    }
}
