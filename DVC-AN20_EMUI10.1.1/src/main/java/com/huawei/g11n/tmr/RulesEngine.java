package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RulesEngine {
    private String locale;
    private HashMap<Integer, Pattern> patterns;
    private HashMap<Integer, String> regexs;

    public RulesEngine(String locale2, HashMap<Integer, String> rules, HashMap<String, String> subRules, LocaleParam param, LocaleParam param_en, boolean isPat) {
        this.locale = locale2;
        init(locale2, rules, subRules, param, param_en, isPat);
    }

    public RulesEngine(String locale2, HashMap<Integer, String> rules, HashMap<String, String> subRules, LocaleParam param, LocaleParam param_en) {
        this.locale = locale2;
        init(locale2, rules, subRules, param, param_en, true);
    }

    public Pattern getPatterns(Integer key) {
        HashMap<Integer, Pattern> hashMap = this.patterns;
        if (hashMap != null && hashMap.containsKey(key)) {
            return this.patterns.get(key);
        }
        return null;
    }

    /* JADX INFO: Multiple debug info for r0v25 'value'  java.lang.String: [D('pmo' java.lang.String), D('pmo2' java.lang.String)] */
    private void init(String locale2, HashMap<Integer, String> rules, HashMap<String, String> subRules, LocaleParam param, LocaleParam param_bk, boolean isPat) {
        Pattern pattern2;
        Pattern pattern;
        Matcher match;
        String pmv;
        String pmv2;
        String pmo;
        String value;
        int rindex;
        RulesEngine rulesEngine = this;
        HashMap<String, String> hashMap = subRules;
        LocaleParam localeParam = param;
        LocaleParam localeParam2 = param_bk;
        rulesEngine.patterns = new HashMap<>();
        rulesEngine.regexs = new HashMap<>();
        Pattern pattern3 = Pattern.compile("\\[(param_\\w+)\\]");
        Pattern pattern22 = Pattern.compile("\\[regex_(\\w+)\\]");
        Pattern pattern32 = Pattern.compile("\\[paramopt_(\\w+)\\]");
        for (Map.Entry<Integer, String> entry : rules.entrySet()) {
            boolean valid = true;
            Integer name = entry.getKey();
            String rule = entry.getValue();
            int i = 1;
            if (hashMap != null && !subRules.isEmpty()) {
                Matcher match2 = pattern22.matcher(rule);
                while (match2.find()) {
                    rule = rule.replace(match2.group(), hashMap.get(match2.group(i)));
                    i = 1;
                }
            }
            if (!(localeParam == null && localeParam2 == null)) {
                Matcher match3 = pattern32.matcher(rule);
                while (match3.find()) {
                    String value2 = "param_" + match3.group(1);
                    int findex = match3.start();
                    int fend = match3.end();
                    String pmo2 = (localeParam == null || localeParam.get(value2) == null) ? BuildConfig.FLAVOR : localeParam.get(value2);
                    String pmo22 = (localeParam2 == null || localeParam2.get(value2) == null) ? BuildConfig.FLAVOR : localeParam2.get(value2);
                    if (pmo2.isEmpty() && !pmo22.isEmpty()) {
                        pmo = pmo22;
                        value = pmo22;
                    } else if (pmo2.trim().isEmpty() || pmo22.trim().isEmpty()) {
                        value = pmo22;
                        pmo = pmo2;
                    } else {
                        value = pmo22;
                        pmo = pmo2.concat("|").concat(value);
                    }
                    if (!pmo.isEmpty() || !value.isEmpty()) {
                        rindex = findex;
                    } else {
                        rindex = rule.substring(findex - 1, findex).equals("|") ? findex - 1 : findex;
                        rule = rule.replace(rule.substring(rindex, fend), BuildConfig.FLAVOR);
                    }
                    rule = rule.replace("[".concat("paramopt_" + match3.group(1)).concat("]"), pmo);
                    rulesEngine = this;
                    localeParam = param;
                    localeParam2 = param_bk;
                    pattern3 = pattern3;
                    pattern22 = pattern22;
                }
            }
            if (localeParam == null && localeParam2 == null) {
                pattern = pattern3;
                pattern2 = pattern22;
            } else {
                Matcher match4 = pattern3.matcher(rule);
                while (true) {
                    if (!match4.find()) {
                        pattern = pattern3;
                        pattern2 = pattern22;
                        break;
                    }
                    pattern = pattern3;
                    pattern2 = pattern22;
                    String value3 = match4.group(1);
                    String pmv3 = (localeParam == null || localeParam.get(value3) == null) ? BuildConfig.FLAVOR : localeParam.get(value3);
                    String pmv22 = (localeParam2 == null || localeParam2.get(value3) == null) ? BuildConfig.FLAVOR : localeParam2.get(value3);
                    if (!pmv3.isEmpty() || pmv22.isEmpty()) {
                        if (pmv3.trim().isEmpty() || pmv22.trim().isEmpty() || pmv3.endsWith("]")) {
                            match = match4;
                            pmv2 = pmv3;
                        } else {
                            match = match4;
                            if (pmv3.endsWith("]\\b") || !rulesEngine.isConactBkParam(name)) {
                                pmv2 = pmv3;
                            } else {
                                pmv = pmv3.concat("|").concat(pmv22);
                            }
                        }
                        pmv = pmv2;
                    } else {
                        match = match4;
                        pmv = pmv22;
                    }
                    if (pmv.trim().isEmpty()) {
                        valid = false;
                        break;
                    }
                    rule = rule.replace("[".concat(value3).concat("]"), pmv);
                    rulesEngine = this;
                    pattern3 = pattern;
                    pattern22 = pattern2;
                    match4 = match;
                }
            }
            if (rule == null || rule.trim().equals(BuildConfig.FLAVOR) || !valid) {
                hashMap = subRules;
                pattern3 = pattern;
                pattern22 = pattern2;
            } else if (isPat) {
                rulesEngine.patterns.put(name, Pattern.compile(rule, 2));
                hashMap = subRules;
                pattern3 = pattern;
                pattern22 = pattern2;
            } else {
                rulesEngine.regexs.put(name, rule);
                hashMap = subRules;
                pattern3 = pattern;
                pattern22 = pattern2;
            }
        }
    }

    private boolean isConactBkParam(Integer rNum) {
        if ((rNum.intValue() == 20009 || rNum.intValue() == 20010 || rNum.intValue() == 20011) && !this.locale.equals("zh_hans") && !this.locale.equals("en")) {
            return false;
        }
        return true;
    }

    public HashMap<Integer, String> getRegexs() {
        return this.regexs;
    }

    public List<Match> match(String msg) {
        List<Match> matchs = new ArrayList<>();
        List<Integer> keys = new ArrayList<>();
        keys.addAll(this.patterns.keySet());
        Collections.sort(keys);
        for (Integer name : keys) {
            Matcher match = this.patterns.get(name).matcher(msg);
            while (match.find()) {
                matchs.add(new Match(match.start(), match.end(), String.valueOf(name)));
            }
        }
        return matchs;
    }

    public Pattern getPattenById(Integer id) {
        HashMap<Integer, Pattern> hashMap = this.patterns;
        if (hashMap == null || !hashMap.containsKey(id)) {
            return null;
        }
        return this.patterns.get(id);
    }

    public String getLocale() {
        return this.locale;
    }
}
