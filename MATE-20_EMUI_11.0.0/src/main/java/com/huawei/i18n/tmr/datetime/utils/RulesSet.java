package com.huawei.i18n.tmr.datetime.utils;

import com.huawei.i18n.tmr.datetime.data.LocaleParam;
import java.util.HashMap;

public class RulesSet {
    private final LocaleParam param;
    private final LocaleParam paramBackup;
    private final HashMap<Integer, String> rulesMap;
    private final HashMap<String, String> subRules;

    public RulesSet(HashMap<Integer, String> rulesMap2, HashMap<String, String> subRules2, LocaleParam param2, LocaleParam paramBackup2) {
        this.rulesMap = rulesMap2;
        this.subRules = subRules2;
        this.param = param2;
        this.paramBackup = paramBackup2;
    }

    public HashMap<Integer, String> getRulesMap() {
        return this.rulesMap;
    }

    public HashMap<String, String> getSubRules() {
        return this.subRules;
    }

    public LocaleParam getParam() {
        return this.param;
    }

    public LocaleParam getParamBackup() {
        return this.paramBackup;
    }
}
