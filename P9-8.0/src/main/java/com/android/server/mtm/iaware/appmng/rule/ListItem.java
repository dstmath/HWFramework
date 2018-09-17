package com.android.server.mtm.iaware.appmng.rule;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public class ListItem {
    private static final int UNINIT_VALUE = -1;
    private HashMap<String, Integer> mComplicatePolicy = null;
    private String mIndex = null;
    private int mPolicy = -1;

    public int getPolicy() {
        return this.mPolicy;
    }

    public int getPolicy(String key) {
        if (this.mComplicatePolicy == null) {
            return -1;
        }
        Integer policy = (Integer) this.mComplicatePolicy.get(key);
        if (policy == null) {
            return -1;
        }
        return policy.intValue();
    }

    public void setPolicy(int policy) {
        this.mPolicy = policy;
    }

    public void setComplicatePolicy(HashMap<String, Integer> policy) {
        this.mComplicatePolicy = policy;
    }

    public void dump(PrintWriter pw) {
        if (this.mPolicy != -1) {
            pw.println("    policy = " + this.mPolicy);
        }
        if (this.mComplicatePolicy != null) {
            for (Entry<String, Integer> entry : this.mComplicatePolicy.entrySet()) {
                pw.println("    policy = " + ((String) entry.getKey()) + ":" + entry.getValue());
            }
        }
    }

    public void setIndex(String index) {
        this.mIndex = index;
    }

    public String getIndex() {
        return this.mIndex;
    }
}
