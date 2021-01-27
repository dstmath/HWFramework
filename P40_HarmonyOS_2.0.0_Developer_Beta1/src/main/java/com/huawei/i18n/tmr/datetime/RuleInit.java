package com.huawei.i18n.tmr.datetime;

import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.datetime.data.LocaleParam;
import com.huawei.i18n.tmr.datetime.detect.LocaleRules;
import com.huawei.i18n.tmr.datetime.detect.Rules;
import com.huawei.i18n.tmr.datetime.detect.UniverseRule;
import com.huawei.i18n.tmr.datetime.parse.DateConvert;
import com.huawei.i18n.tmr.datetime.parse.DateParse;
import com.huawei.i18n.tmr.datetime.parse.ParseRules;
import com.huawei.i18n.tmr.datetime.utils.DatePeriod;
import com.huawei.i18n.tmr.datetime.utils.RulesSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class RuleInit {
    private RulesEngine clearRulesEngine;
    private DateParse dateParse;
    private String dateTimeBridge;
    private String locale;
    private String localeBackup;
    private HashMap<Integer, String> parses;
    private RulesEngine past;
    private String periodString;
    private HashMap<Integer, RulesEngine> subDetectsMap;
    private List<RulesEngine> universalAndLocaleRules;

    RuleInit(String locale2, String localeBackup2) {
        this.locale = locale2;
        this.localeBackup = localeBackup2;
        init();
        this.dateParse = new DateParse(locale2, localeBackup2, this);
    }

    public String getDateTimeBridge() {
        return this.dateTimeBridge;
    }

    public void setDateTimeBridge(String dateTimeBridge2) {
        this.dateTimeBridge = dateTimeBridge2;
    }

    public String getPeriodString() {
        return this.periodString;
    }

    public void setPeriodString(String periodString2) {
        this.periodString = periodString2;
    }

    private void init() {
        UniverseRule classUniverseRule = new UniverseRule();
        Rules classRules = new Rules();
        LocaleRules classLocaleRules = new LocaleRules(classRules);
        LocaleParam param = new LocaleParam(this.locale);
        LocaleParam paramBackup = new LocaleParam(this.localeBackup);
        this.universalAndLocaleRules = new ArrayList();
        this.universalAndLocaleRules.add(new RulesEngine(this.locale, new RulesSet(classUniverseRule.getRules(), classRules.getSubRules(), param, paramBackup)));
        if (!classLocaleRules.getLocaleRules(this.locale).isEmpty()) {
            List<RulesEngine> list = this.universalAndLocaleRules;
            String str = this.locale;
            list.add(new RulesEngine(str, new RulesSet(classLocaleRules.getLocaleRules(str), classRules.getSubRules(), param, null)));
        }
        if (!classLocaleRules.getLocaleRules(this.localeBackup).isEmpty()) {
            List<RulesEngine> list2 = this.universalAndLocaleRules;
            String str2 = this.localeBackup;
            list2.add(new RulesEngine(str2, new RulesSet(classLocaleRules.getLocaleRules(str2), classRules.getSubRules(), paramBackup, null)));
        }
        this.subDetectsMap = new HashMap<>();
        for (Integer name : classUniverseRule.getSubRulesMaps().keySet()) {
            this.subDetectsMap.put(name, new RulesEngine(this.locale, new RulesSet(classUniverseRule.getSubRulesMaps().get(name), classRules.getSubRules(), param, paramBackup)));
        }
        this.parses = new RulesEngine(this.locale, new RulesSet(new ParseRules().getRules(), classRules.getSubRules(), param, paramBackup), false).getRegexps();
        this.clearRulesEngine = new RulesEngine(this.locale, new RulesSet(classRules.getFilterRegex(), classRules.getSubRules(), param, paramBackup));
        this.past = new RulesEngine(this.locale, new RulesSet(classRules.getPastRegex(), null, param, paramBackup));
        setPeriodString(StorageManagerExt.INVALID_KEY_DESC);
        if ("jv".equals(this.locale) || "fil".equals(this.locale)) {
            setPeriodString(param.get("param_period").replace("\\b", StorageManagerExt.INVALID_KEY_DESC));
        }
        setDateTimeBridge(StorageManagerExt.INVALID_KEY_DESC);
        if ("be".equals(this.locale) || "fil".equals(this.locale) || "kk".equals(this.locale)) {
            setDateTimeBridge(param.get("param_DateTimeBridge").replace("\\b", StorageManagerExt.INVALID_KEY_DESC));
        }
    }

    /* access modifiers changed from: package-private */
    public List<Match> detect(String msg) {
        return new Filter(this.locale).filter(msg, getMatches(msg), this);
    }

    /* access modifiers changed from: package-private */
    public DatePeriod parse(String msg, long defaultTime) {
        List<Match> matchList = new Filter(this.locale).filterOverlay(getMatches(msg));
        for (Match match : matchList) {
            Optional<DatePeriod> optional = this.dateParse.parse(msg.substring(match.begin, match.end), match.getRegex(), defaultTime);
            if (optional.isPresent()) {
                match.setDp(optional.get());
            }
        }
        Optional<DatePeriod> optional2 = new DateConvert(this.locale).filterByParse(msg, matchList, getPeriodString(), getDateTimeBridge());
        if (optional2.isPresent()) {
            return optional2.get();
        }
        return null;
    }

    private List<Match> getMatches(String msg) {
        List<Match> matchList = new ArrayList<>();
        for (RulesEngine detect : this.universalAndLocaleRules) {
            matchList.addAll(detect.match(msg));
        }
        Iterator<Match> matchIterator = matchList.iterator();
        List<Match> subResult = new ArrayList<>();
        while (matchIterator.hasNext()) {
            Match match = matchIterator.next();
            if (this.subDetectsMap.containsKey(Integer.valueOf(match.regex))) {
                List<Match> subMatchList = this.subDetectsMap.get(Integer.valueOf(match.regex)).match(msg.substring(match.begin, match.end));
                for (Match subMatch : subMatchList) {
                    subMatch.setBegin(subMatch.getBegin() + match.getBegin());
                    subMatch.setEnd(subMatch.getEnd() + match.getBegin());
                }
                subResult.addAll(subMatchList);
                matchIterator.remove();
            }
        }
        matchList.addAll(subResult);
        return matchList;
    }

    public Pattern getParseByKey(Integer key) {
        return Pattern.compile(this.parses.get(key), 2);
    }

    public Pattern getDetectByKey(Integer key) {
        Pattern result = null;
        Iterator<RulesEngine> it = this.universalAndLocaleRules.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Optional<Pattern> optionalPattern = it.next().getPatterns(key);
            if (optionalPattern.isPresent()) {
                result = optionalPattern.get();
                break;
            }
        }
        if (result != null) {
            return result;
        }
        for (Integer name : this.subDetectsMap.keySet()) {
            Optional<Pattern> optionalPattern2 = this.subDetectsMap.get(name).getPatterns(key);
            if (optionalPattern2.isPresent()) {
                return optionalPattern2.get();
            }
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public List<Match> clear(String msg) {
        return this.clearRulesEngine.match(msg);
    }

    /* access modifiers changed from: package-private */
    public List<Match> pastFind(String msg) {
        return this.past.match(msg);
    }

    public String getLocale() {
        return this.locale;
    }

    public String getLocaleBackup() {
        return this.localeBackup;
    }
}
