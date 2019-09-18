package com.android.server.mtm.iaware.appmng.rule;

import android.util.ArrayMap;
import java.io.PrintWriter;
import java.util.Map;

public class ListItem {
    private static final int UNINIT_VALUE = -1;
    private ArrayMap<String, Integer> mComplicatePolicy = null;
    private String mIndex = null;
    private int mPolicy = -1;
    private int mWeight = -1;

    public int getPolicy() {
        return this.mPolicy;
    }

    public int getPolicy(String key) {
        if (this.mComplicatePolicy == null) {
            return -1;
        }
        Integer policy = this.mComplicatePolicy.get(key);
        if (policy == null) {
            return -1;
        }
        return policy.intValue();
    }

    public void setPolicy(int policy) {
        this.mPolicy = policy;
    }

    public void setComplicatePolicy(ArrayMap<String, Integer> policy) {
        this.mComplicatePolicy = policy;
    }

    public void dump(PrintWriter pw) {
        if (this.mPolicy != -1) {
            pw.println("    policy = " + this.mPolicy + ", weight = " + this.mWeight);
        }
        if (this.mComplicatePolicy != null) {
            for (Map.Entry<String, Integer> entry : this.mComplicatePolicy.entrySet()) {
                pw.println("    policy = " + entry.getKey() + ":" + entry.getValue() + ", weight = " + this.mWeight);
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
