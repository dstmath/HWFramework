package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
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
        if (this.patterns != null && this.patterns.containsKey(key)) {
            return this.patterns.get(key);
        }
        return null;
    }

    private void init(String locale2, HashMap<Integer, String> rules, HashMap<String, String> subRules, LocaleParam param, LocaleParam param_bk, boolean isPat) {
        String pmo2;
        int rindex;
        RulesEngine rulesEngine = this;
        HashMap<String, String> hashMap = subRules;
        LocaleParam localeParam = param;
        LocaleParam localeParam2 = param_bk;
        rulesEngine.patterns = new HashMap<>();
        rulesEngine.regexs = new HashMap<>();
        Pattern pattern = Pattern.compile("\\[(param_\\w+)\\]");
        Pattern pattern2 = Pattern.compile("\\[regex_(\\w+)\\]");
        Pattern pattern3 = Pattern.compile("\\[paramopt_(\\w+)\\]");
        for (Map.Entry<Integer, String> entry : rules.entrySet()) {
            boolean valid = true;
            Integer name = entry.getKey();
            String rule = entry.getValue();
            int i = 1;
            if (hashMap != null && !subRules.isEmpty()) {
                Matcher match2 = pattern2.matcher(rule);
                while (match2.find()) {
                    rule = rule.replace(match2.group(), hashMap.get(match2.group(i)));
                    i = 1;
                }
            }
            if (!(localeParam == null && localeParam2 == null)) {
                Matcher match3 = pattern3.matcher(rule);
                while (match3.find()) {
                    String value = "param_" + match3.group(1);
                    int findex = match3.start();
                    int i2 = findex;
                    int fend = match3.end();
                    String pmo = (localeParam == null || localeParam.get(value) == null) ? "" : localeParam.get(value);
                    String pmo22 = (localeParam2 == null || localeParam2.get(value) == null) ? "" : localeParam2.get(value);
                    if (pmo.isEmpty()) {
                        String str = value;
                        pmo2 = pmo22;
                        if (!pmo2.isEmpty()) {
                            pmo = pmo2;
                            if (pmo.isEmpty() || !pmo2.isEmpty()) {
                            } else {
                                String str2 = pmo2;
                                if (rule.substring(findex - 1, findex).equals("|")) {
                                    rindex = findex - 1;
                                } else {
                                    rindex = findex;
                                }
                                rule = rule.replace(rule.substring(rindex, fend), "");
                            }
                            int i3 = findex;
                            rule = rule.replace("[".concat("paramopt_" + match3.group(1)).concat("]"), pmo);
                            rulesEngine = this;
                            HashMap<String, String> hashMap2 = subRules;
                            localeParam = param;
                            localeParam2 = param_bk;
                        }
                    } else {
                        pmo2 = pmo22;
                    }
                    if (!pmo.trim().isEmpty() && !pmo2.trim().isEmpty()) {
                        pmo = pmo.concat("|").concat(pmo2);
                    }
                    if (pmo.isEmpty()) {
                    }
                    int i32 = findex;
                    rule = rule.replace("[".concat("paramopt_" + match3.group(1)).concat("]"), pmo);
                    rulesEngine = this;
                    HashMap<String, String> hashMap22 = subRules;
                    localeParam = param;
                    localeParam2 = param_bk;
                }
            }
            if (localeParam != null || localeParam2 != null) {
                Matcher match = pattern.matcher(rule);
                while (true) {
                    if (!match.find()) {
                        break;
                    }
                    String value2 = match.group(1);
                    String pmv = (localeParam == null || localeParam.get(value2) == null) ? "" : localeParam.get(value2);
                    String pmv2 = (localeParam2 == null || localeParam2.get(value2) == null) ? "" : localeParam2.get(value2);
                    if (pmv.isEmpty() && !pmv2.isEmpty()) {
                        pmv = pmv2;
                    } else if (!pmv.trim().isEmpty() && !pmv2.trim().isEmpty() && !pmv.endsWith("]") && !pmv.endsWith("]\\b") && rulesEngine.isConactBkParam(name)) {
                        pmv = pmv.concat("|").concat(pmv2);
                    }
                    if (pmv.trim().isEmpty()) {
                        valid = false;
                        break;
                    }
                    rule = rule.replace("[".concat(value2).concat("]"), pmv);
                    rulesEngine = this;
                    HashMap<String, String> hashMap3 = subRules;
                }
            }
            if (rule != null && !rule.trim().equals("") && valid) {
                if (isPat) {
                    rulesEngine.patterns.put(name, Pattern.compile(rule, 2));
                } else {
                    rulesEngine.regexs.put(name, rule);
                }
            }
            hashMap = subRules;
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
        if (this.patterns == null || !this.patterns.containsKey(id)) {
            return null;
        }
        return this.patterns.get(id);
    }

    public String getLocale() {
        return this.locale;
    }
}
