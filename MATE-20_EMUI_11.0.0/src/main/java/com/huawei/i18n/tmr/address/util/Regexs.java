package com.huawei.i18n.tmr.address.util;

import java.util.HashMap;

public abstract class Regexs {
    private HashMap<String, String> regexMap = null;

    public abstract void init();

    /* access modifiers changed from: package-private */
    public String getRegex(String name) {
        if (this.regexMap.containsKey(name)) {
            return this.regexMap.get(name);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void put(String name, String regex) {
        if (this.regexMap == null) {
            this.regexMap = new HashMap<>();
        }
        this.regexMap.put(name, regex);
    }
}
