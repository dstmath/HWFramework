package com.android.server.mtm.iaware.appmng.rule;

import android.util.ArrayMap;
import java.io.PrintWriter;
import java.util.Map;

public class ListItem {
    private static final int UNINIT_VALUE = -1;
    private ArrayMap<String, Integer> mComplicatePolicy = null;
    private String mIndex = null;
    private int mPolicy = -1;
    private Config mRules = null;
    private ArrayMap<String, Config> mSysBroadcastRules = null;
    private int mWeight = -1;

    public int getPolicy() {
        return this.mPolicy;
    }

    public int getPolicy(String key) {
        Integer policy;
        ArrayMap<String, Integer> arrayMap = this.mComplicatePolicy;
        if (arrayMap == null || (policy = arrayMap.get(key)) == null) {
            return -1;
        }
        return policy.intValue();
    }

    public Config getListRule() {
        return this.mRules;
    }

    public Config getSysBroadcastRule(String key) {
        ArrayMap<String, Config> arrayMap = this.mSysBroadcastRules;
        if (arrayMap != null) {
            return arrayMap.get(key);
        }
        return null;
    }

    public void setPolicy(int policy) {
        this.mPolicy = policy;
    }

    public void setComplicatePolicy(ArrayMap<String, Integer> policy) {
        this.mComplicatePolicy = policy;
    }

    public void setSysBroadcastRule(ArrayMap<String, Config> rules) {
        this.mSysBroadcastRules = rules;
    }

    public void setListRule(Config rule) {
        this.mRules = rule;
    }

    public void dump(PrintWriter pw) {
        if (this.mPolicy != -1) {
            pw.println("    policy = " + this.mPolicy + ", weight = " + this.mWeight);
        }
        if (this.mRules != null) {
            pw.println("    rule = ");
            this.mRules.dump(pw);
        }
        ArrayMap<String, Integer> arrayMap = this.mComplicatePolicy;
        if (arrayMap != null) {
            for (Map.Entry<String, Integer> entry : arrayMap.entrySet()) {
                pw.println("    policy = " + entry.getKey() + ":" + entry.getValue() + ", weight = " + this.mWeight);
            }
            ArrayMap<String, Config> arrayMap2 = this.mSysBroadcastRules;
            if (arrayMap2 != null) {
                for (Map.Entry<String, Config> entry2 : arrayMap2.entrySet()) {
                    Config config = entry2.getValue();
                    if (config != null) {
                        pw.println("     rules = " + entry2.getKey() + ":");
                        config.dump(pw);
                    }
                }
            }
        }
    }

    public void setIndex(String index) {
        this.mIndex = index;
    }

    public String getIndex() {
        return this.mIndex;
    }

    public void setWeight(int weight) {
        this.mWeight = weight;
    }

    public int getWeight() {
        return this.mWeight;
    }

    public ArrayMap<String, Integer> getComplicatePolicy() {
        return this.mComplicatePolicy;
    }
}
