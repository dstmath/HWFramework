package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RulesEngine {
    private String locale;
    private HashMap<Integer, Pattern> patterns;
    private HashMap<Integer, String> regexs;

    public RulesEngine(String locale, HashMap<Integer, String> rules, HashMap<String, String> subRules, LocaleParam param, LocaleParam param_en, boolean isPat) {
        this.locale = locale;
        init(locale, rules, subRules, param, param_en, isPat);
    }

    public RulesEngine(String locale, HashMap<Integer, String> rules, HashMap<String, String> subRules, LocaleParam param, LocaleParam param_en) {
        this.locale = locale;
        init(locale, rules, subRules, param, param_en, true);
    }

    public Pattern getPatterns(Integer key) {
        if (this.patterns == null || !this.patterns.containsKey(key)) {
            return null;
        }
        return (Pattern) this.patterns.get(key);
    }

    private void init(String locale, HashMap<Integer, String> rules, HashMap<String, String> subRules, LocaleParam param, LocaleParam param_bk, boolean isPat) {
        this.patterns = new HashMap();
        this.regexs = new HashMap();
        Pattern pattern = Pattern.compile("\\[(param_\\w+)\\]");
        Pattern pattern2 = Pattern.compile("\\[regex_(\\w+)\\]");
        Pattern pattern3 = Pattern.compile("\\[paramopt_(\\w+)\\]");
        for (Entry<Integer, String> entry : rules.entrySet()) {
            String value;
            boolean valid = true;
            Integer name = (Integer) entry.getKey();
            String rule = (String) entry.getValue();
            if (!(subRules == null || subRules.isEmpty())) {
                Matcher match2 = pattern2.matcher(rule);
                while (match2.find()) {
                    rule = rule.replace(match2.group(), (CharSequence) subRules.get(match2.group(1)));
                }
            }
            if (!(param == null && param_bk == null)) {
                Matcher match3 = pattern3.matcher(rule);
                while (match3.find()) {
                    value = "param_" + match3.group(1);
                    int findex = match3.start();
                    int rindex = findex;
                    int fend = match3.end();
                    String pmo = (param == null || param.get(value) == null) ? "" : param.get(value);
                    String pmo2 = (param_bk == null || param_bk.get(value) == null) ? "" : param_bk.get(value);
                    if (pmo.isEmpty() && !pmo2.isEmpty()) {
                        pmo = pmo2;
                    } else if (!(pmo.trim().isEmpty() || pmo2.trim().isEmpty())) {
                        pmo = pmo.concat("|").concat(pmo2);
                    }
                    if (pmo.isEmpty() && pmo2.isEmpty()) {
                        if (rule.substring(findex - 1, findex).equals("|")) {
                            rindex = findex - 1;
                        } else {
                            rindex = findex;
                        }
                        rule = rule.replace(rule.substring(rindex, fend), "");
                    }
                    rule = rule.replace("[".concat("paramopt_" + match3.group(1)).concat("]"), pmo);
                }
            }
            if (param != null || param_bk != null) {
                Matcher match = pattern.matcher(rule);
                while (match.find()) {
                    value = match.group(1);
                    String pmv = (param == null || param.get(value) == null) ? "" : param.get(value);
                    String pmv2 = (param_bk == null || param_bk.get(value) == null) ? "" : param_bk.get(value);
                    if (pmv.isEmpty() && !pmv2.isEmpty()) {
                        pmv = pmv2;
                    } else if (!(pmv.trim().isEmpty() || pmv2.trim().isEmpty() || pmv.endsWith("]") || pmv.endsWith("]\\b") || !isConactBkParam(name))) {
                        pmv = pmv.concat("|").concat(pmv2);
                    }
                    if (pmv.trim().isEmpty()) {
                        valid = false;
                        break;
                    }
                    rule = rule.replace("[".concat(value).concat("]"), pmv);
                }
            }
            if (!(rule == null || rule.trim().equals("") || !valid)) {
                if (isPat) {
                    this.patterns.put(name, Pattern.compile(rule, 2));
                } else {
                    this.regexs.put(name, rule);
                }
            }
        }
    }

    private boolean isConactBkParam(Integer rNum) {
        if ((rNum.intValue() != 20009 && rNum.intValue() != 20010 && rNum.intValue() != 20011) || this.locale.equals("zh_hans") || this.locale.equals("en")) {
            return true;
        }
        return false;
    }

    public HashMap<Integer, String> getRegexs() {
        return this.regexs;
    }

    public List<Match> match(String msg) {
        List<Match> matchs = new ArrayList();
        List<Integer> keys = new ArrayList();
        keys.addAll(this.patterns.keySet());
        Collections.sort(keys);
        for (Integer name : keys) {
            Matcher match = ((Pattern) this.patterns.get(name)).matcher(msg);
            while (match.find()) {
                matchs.add(new Match(match.start(), match.end(), String.valueOf(name)));
            }
        }
        return matchs;
    }

    public Pattern getPattenById(Integer id) {
        if (this.patterns != null && this.patterns.containsKey(id)) {
            return (Pattern) this.patterns.get(id);
        }
        return null;
    }

    public String getLocale() {
        return this.locale;
    }
}
