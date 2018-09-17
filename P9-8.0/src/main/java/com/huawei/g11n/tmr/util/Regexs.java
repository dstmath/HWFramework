package com.huawei.g11n.tmr.util;

import java.util.HashMap;

public abstract class Regexs {
    private HashMap<String, String> regexs = null;

    public abstract void init();

    public Regexs() {
        init();
    }

    public String getReg(String name) {
        if (this.regexs.containsKey(name)) {
            return (String) this.regexs.get(name);
        }
        return null;
    }

    protected void put(String name, String rule) {
        if (this.regexs == null) {
            this.regexs = new HashMap();
        }
        this.regexs.put(name, rule);
    }
}
