package com.android.server.mtm.iaware.appmng.rule;

import com.android.server.mtm.iaware.appmng.rule.RuleNode.StringIntegerMap;
import com.android.server.mtm.iaware.appmng.rule.RuleNode.XmlValue;
import com.android.server.rms.memrepair.ProcStateStatisData;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public abstract class Config {
    HashMap<String, String> mProperties;
    RuleNode mRules;

    public Config(HashMap<String, String> prop, RuleNode rules) {
        this.mProperties = prop;
        this.mRules = rules;
    }

    private void printRuleNode(PrintWriter pw, RuleNode node, int depth) {
        if (node != null && pw != null) {
            XmlValue value = node.getValue();
            if (value != null) {
                if (value.isString()) {
                    pw.println(ProcStateStatisData.SEPERATOR_CHAR + getDepthString(depth) + node.getCurrentType() + ":" + value.getStringValue());
                } else {
                    pw.println(ProcStateStatisData.SEPERATOR_CHAR + getDepthString(depth) + node.getCurrentType() + ":" + value.getIntValue());
                }
                String index = value.getIndex();
                if (index != null) {
                    pw.println(ProcStateStatisData.SEPERATOR_CHAR + getDepthString(depth) + "index:" + index);
                }
            }
            if (node.hasChild()) {
                StringIntegerMap childs = node.getChilds();
                if (childs.isStringMap()) {
                    for (Entry<String, RuleNode> entry : childs.getStringMap().entrySet()) {
                        printRuleNode(pw, (RuleNode) entry.getValue(), depth + 1);
                    }
                } else {
                    ArrayList<Integer> sortedList = childs.getSortedList();
                    if (sortedList != null) {
                        StringBuilder sb = new StringBuilder();
                        int listSize = sortedList.size();
                        for (int i = 0; i < listSize; i++) {
                            sb.append((Integer) sortedList.get(i));
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
                            printRuleNode(pw, (RuleNode) integerMap.get(key), depth + 1);
                        }
                    }
                }
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
