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
import java.util.Collection;
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

    public void setDTBridgeString(String str) {
        this.DTBridgeString = str;
    }

    public String getPeriodString() {
        return this.periodString;
    }

    public void setPeriodString(String str) {
        this.periodString = str;
    }

    public RuleInit(String str, String str2) {
        this.locale = str;
        this.locale_bk = str2;
        init();
        this.rp = new DateParse(str, str2, this);
    }

    private void init() {
        HashMap hashMap = new HashMap();
        UniverseRule universeRule = new UniverseRule();
        hashMap.putAll(universeRule.getRules());
        Rules rules = new Rules();
        HashMap hashMap2 = new HashMap();
        HashMap hashMap3 = new HashMap();
        LocaleRules localeRules = new LocaleRules(rules);
        hashMap2.putAll(localeRules.getLocaleRules(this.locale));
        hashMap3.putAll(localeRules.getLocaleRules(this.locale_bk));
        LocaleParam localeParam = new LocaleParam(this.locale);
        LocaleParam localeParam2 = new LocaleParam(this.locale_bk);
        setPeriodString("");
        if (this.locale.equals("jv") || this.locale.equals("fil")) {
            setPeriodString(localeParam.get("param_period").replace("\\b", ""));
        }
        setDTBridgeString("");
        if (this.locale.equals("be") || this.locale.equals("fil") || this.locale.equals("kk")) {
            setDTBridgeString(localeParam.get("param_DateTimeBridge").replace("\\b", ""));
        }
        RulesEngine rulesEngine = new RulesEngine(this.locale, hashMap, rules.getSubRules(), localeParam, localeParam2);
        this.detects = new ArrayList();
        this.detects.add(rulesEngine);
        this.subDetectsMap = new HashMap();
        for (Integer num : universeRule.getSubRulesMaps().keySet()) {
            this.subDetectsMap.put(num, new RulesEngine(this.locale, (HashMap) universeRule.getSubRulesMaps().get(num), rules.getSubRules(), localeParam, localeParam2));
        }
        if (!hashMap2.isEmpty()) {
            this.detects.add(new RulesEngine(this.locale, hashMap2, rules.getSubRules(), localeParam, null));
        }
        if (!hashMap3.isEmpty()) {
            this.detects.add(new RulesEngine(this.locale_bk, hashMap3, rules.getSubRules(), localeParam2, null));
        }
        this.parses = new RulesEngine(this.locale, new ParseRules().getRules(), rules.getSubRules(), localeParam, localeParam2, false).getRegexs();
        this.clear = new RulesEngine(this.locale, rules.getFilterRegex(), rules.getSubRules(), localeParam, localeParam2);
        this.past = new RulesEngine(this.locale, rules.getPastRegex(), null, localeParam, localeParam2);
    }

    public List<Match> detect(String str) {
        List arrayList = new ArrayList();
        for (RulesEngine match : this.detects) {
            arrayList.addAll(match.match(str));
        }
        Iterator it = arrayList.iterator();
        Collection arrayList2 = new ArrayList();
        while (it.hasNext()) {
            Match match2 = (Match) it.next();
            if (this.subDetectsMap.containsKey(Integer.valueOf(match2.regex))) {
                Collection<Match> match3 = ((RulesEngine) this.subDetectsMap.get(Integer.valueOf(match2.regex))).match(str.substring(match2.begin, match2.end));
                for (Match match4 : match3) {
                    match4.setBegin(match4.getBegin() + match2.getBegin());
                    match4.setEnd(match4.getEnd() + match2.getBegin());
                }
                arrayList2.addAll(match3);
                it.remove();
            }
        }
        arrayList.addAll(arrayList2);
        return new Filter(this.locale).filter(str, arrayList, this);
    }

    public DatePeriod parse(String str, long j) {
        List arrayList = new ArrayList();
        for (RulesEngine match : this.detects) {
            arrayList.addAll(match.match(str));
        }
        Iterator it = arrayList.iterator();
        Collection arrayList2 = new ArrayList();
        while (it.hasNext()) {
            Match match2 = (Match) it.next();
            if (this.subDetectsMap.containsKey(Integer.valueOf(match2.regex))) {
                Collection<Match> match3 = ((RulesEngine) this.subDetectsMap.get(Integer.valueOf(match2.regex))).match(str.substring(match2.begin, match2.end));
                for (Match match4 : match3) {
                    match4.setBegin(match4.getBegin() + match2.getBegin());
                    match4.setEnd(match4.getEnd() + match2.getBegin());
                }
                arrayList2.addAll(match3);
                it.remove();
            }
        }
        arrayList.addAll(arrayList2);
        List<Match> filterOverlay = new Filter(this.locale).filterOverlay(arrayList);
        for (Match match22 : filterOverlay) {
            DatePeriod parse = this.rp.parse(str.substring(match22.begin, match22.end), match22.getRegex(), j);
            if (parse != null) {
                match22.setDp(parse);
            }
        }
        return new DateConvert(this.locale).filterByParse(str, filterOverlay, getPeriodString(), getDTBridgeString());
    }

    public Pattern getParseByKey(Integer num) {
        return Pattern.compile((String) this.parses.get(num), 2);
    }

    public Pattern getDetectByKey(Integer num) {
        Pattern pattern = null;
        for (RulesEngine patterns : this.detects) {
            pattern = patterns.getPatterns(num);
            if (pattern != null) {
                break;
            }
        }
        if (pattern == null) {
            for (Integer num2 : this.subDetectsMap.keySet()) {
                pattern = ((RulesEngine) this.subDetectsMap.get(num2)).getPatterns(num);
                if (pattern != null) {
                    break;
                }
            }
        }
        return pattern;
    }

    public List<Match> clear(String str) {
        return this.clear.match(str);
    }

    public List<Match> pastFind(String str) {
        return this.past.match(str);
    }

    public String getLocale() {
        return this.locale;
    }

    public String getLocale_bk() {
        return this.locale_bk;
    }
}
