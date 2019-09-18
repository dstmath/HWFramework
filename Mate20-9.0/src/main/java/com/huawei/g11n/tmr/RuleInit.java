package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.detect.LocaleRules;
import com.huawei.g11n.tmr.datetime.detect.Rules;
import com.huawei.g11n.tmr.datetime.detect.UniverseRule;
import com.huawei.g11n.tmr.datetime.parse.DateConvert;
import com.huawei.g11n.tmr.datetime.parse.DateParse;
import com.huawei.g11n.tmr.datetime.parse.ParseRules;
import com.huawei.g11n.tmr.datetime.utils.DatePeriod;
import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class RuleInit {
    private String DTBridgeString;
    private RulesEngine clear;
    private List<RulesEngine> detects;
    private String locale;
    private String locale_bk;
    private HashMap<Integer, String> parses;
    private RulesEngine past;
    private String periodString;
    private DateParse rp;
    private HashMap<Integer, RulesEngine> subDetectsMap;

    public String getDTBridgeString() {
        return this.DTBridgeString;
    }

    public void setDTBridgeString(String dTBridgeString) {
        this.DTBridgeString = dTBridgeString;
    }

    public String getPeriodString() {
        return this.periodString;
    }

    public void setPeriodString(String periodString2) {
        this.periodString = periodString2;
    }

    public RuleInit(String locale2, String localeBk) {
        this.locale = locale2;
        this.locale_bk = localeBk;
        init();
        this.rp = new DateParse(locale2, localeBk, this);
    }

    private void init() {
        LocaleParam param;
        HashMap<Integer, String> bkLocaleRules;
        HashMap<Integer, String> rules = new HashMap<>();
        UniverseRule ur = new UniverseRule();
        rules.putAll(ur.getRules());
        Rules rl = new Rules();
        HashMap<Integer, String> localeRules = new HashMap<>();
        HashMap<Integer, String> bkLocaleRules2 = new HashMap<>();
        LocaleRules lr = new LocaleRules(rl);
        localeRules.putAll(lr.getLocaleRules(this.locale));
        bkLocaleRules2.putAll(lr.getLocaleRules(this.locale_bk));
        LocaleParam param2 = new LocaleParam(this.locale);
        LocaleParam param_bk = new LocaleParam(this.locale_bk);
        setPeriodString("");
        if (this.locale.equals("jv") || this.locale.equals("fil")) {
            setPeriodString(param2.get("param_period").replace("\\b", ""));
        }
        setDTBridgeString("");
        if (this.locale.equals("be") || this.locale.equals("fil") || this.locale.equals("kk")) {
            setDTBridgeString(param2.get("param_DateTimeBridge").replace("\\b", ""));
        }
        RulesEngine uniDetects = new RulesEngine(this.locale, rules, rl.getSubRules(), param2, param_bk);
        this.detects = new ArrayList();
        this.detects.add(uniDetects);
        this.subDetectsMap = new HashMap<>();
        for (Integer name : ur.getSubRulesMaps().keySet()) {
            LocaleParam param3 = param2;
            LocaleRules localeRules2 = lr;
            HashMap<Integer, String> hashMap = bkLocaleRules2;
            HashMap<Integer, String> hashMap2 = localeRules;
            RulesEngine rulesEngine = new RulesEngine(this.locale, ur.getSubRulesMaps().get(name), rl.getSubRules(), param3, param_bk);
            this.subDetectsMap.put(name, rulesEngine);
            param2 = param3;
        }
        if (!localeRules.isEmpty()) {
            param = param2;
            LocaleRules localeRules3 = lr;
            bkLocaleRules = bkLocaleRules2;
            HashMap<Integer, String> hashMap3 = localeRules;
            RulesEngine rulesEngine2 = new RulesEngine(this.locale, localeRules, rl.getSubRules(), param, null);
            this.detects.add(rulesEngine2);
        } else {
            param = param2;
            LocaleRules localeRules4 = lr;
            bkLocaleRules = bkLocaleRules2;
            HashMap<Integer, String> hashMap4 = localeRules;
        }
        if (!bkLocaleRules.isEmpty()) {
            RulesEngine rulesEngine3 = new RulesEngine(this.locale_bk, bkLocaleRules, rl.getSubRules(), param_bk, null);
            this.detects.add(rulesEngine3);
        }
        ParseRules pr = new ParseRules();
        LocaleParam localeParam = param;
        LocaleParam localeParam2 = param_bk;
        RulesEngine rulesEngine4 = new RulesEngine(this.locale, pr.getRules(), rl.getSubRules(), localeParam, localeParam2, false);
        this.parses = rulesEngine4.getRegexs();
        RulesEngine rulesEngine5 = new RulesEngine(this.locale, rl.getFilterRegex(), rl.getSubRules(), localeParam, localeParam2);
        this.clear = rulesEngine5;
        RulesEngine rulesEngine6 = new RulesEngine(this.locale, rl.getPastRegex(), null, localeParam, localeParam2);
        this.past = rulesEngine6;
    }

    public List<Match> detect(String msg) {
        List<Match> r1 = new ArrayList<>();
        for (RulesEngine detect : this.detects) {
            r1.addAll(detect.match(msg));
        }
        Iterator<Match> r1it = r1.iterator();
        List<Match> subResult = new ArrayList<>();
        while (r1it.hasNext()) {
            Match c = r1it.next();
            if (this.subDetectsMap.containsKey(Integer.valueOf(c.regex))) {
                List<Match> rsub = this.subDetectsMap.get(Integer.valueOf(c.regex)).match(msg.substring(c.begin, c.end));
                for (Match ma : rsub) {
                    ma.setBegin(ma.getBegin() + c.getBegin());
                    ma.setEnd(ma.getEnd() + c.getBegin());
                }
                subResult.addAll(rsub);
                r1it.remove();
            }
        }
        r1.addAll(subResult);
        return new Filter(this.locale).filter(msg, r1, this);
    }

    public DatePeriod parse(String msg, long defaultTime) {
        List<Match> r1 = new ArrayList<>();
        for (RulesEngine detect : this.detects) {
            r1.addAll(detect.match(msg));
        }
        Iterator<Match> r1it = r1.iterator();
        List<Match> subResult = new ArrayList<>();
        while (r1it.hasNext()) {
            Match c = r1it.next();
            if (this.subDetectsMap.containsKey(Integer.valueOf(c.regex))) {
                List<Match> rsub = this.subDetectsMap.get(Integer.valueOf(c.regex)).match(msg.substring(c.begin, c.end));
                for (Match ma : rsub) {
                    ma.setBegin(ma.getBegin() + c.getBegin());
                    ma.setEnd(ma.getEnd() + c.getBegin());
                }
                subResult.addAll(rsub);
                r1it.remove();
            }
        }
        r1.addAll(subResult);
        List<Match> r12 = new Filter(this.locale).filterOverlay(r1);
        for (Match m : r12) {
            DatePeriod tdp = this.rp.parse(msg.substring(m.begin, m.end), m.getRegex(), defaultTime);
            if (tdp != null) {
                m.setDp(tdp);
            }
        }
        return new DateConvert(this.locale).filterByParse(msg, r12, getPeriodString(), getDTBridgeString());
    }

    public Pattern getParseByKey(Integer key) {
        return Pattern.compile(this.parses.get(key), 2);
    }

    public Pattern getDetectByKey(Integer key) {
        Pattern result = null;
        for (RulesEngine detect : this.detects) {
            result = detect.getPatterns(key);
            if (result != null) {
                break;
            }
        }
        if (result == null) {
            for (Integer name : this.subDetectsMap.keySet()) {
                result = this.subDetectsMap.get(name).getPatterns(key);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    public List<Match> clear(String msg) {
        return this.clear.match(msg);
    }

    public List<Match> pastFind(String msg) {
        return this.past.match(msg);
    }

    public String getLocale() {
        return this.locale;
    }

    public String getLocale_bk() {
        return this.locale_bk;
    }
}
