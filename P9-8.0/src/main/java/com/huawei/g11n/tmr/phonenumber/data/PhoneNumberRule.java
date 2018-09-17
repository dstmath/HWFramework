package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.MatchedNumberInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class PhoneNumberRule {
    protected List<RegexRule> borderRules;
    protected List<RegexRule> codesRules;
    protected List<RegexRule> negativeRules;
    protected HashMap<String, Pattern> patternCache;
    protected List<RegexRule> positiveRules;

    public class RegexRule {
        private Pattern pattern;
        private String regex;
        private int type = 0;

        public String getRegex() {
            return this.regex;
        }

        public int getType() {
            return this.type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }

        public Pattern getPattern() {
            return this.pattern;
        }

        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        public RegexRule(String regex1) {
            if (regex1 != null && !regex1.isEmpty()) {
                this.regex = regex1;
                this.pattern = Pattern.compile(this.regex);
            }
        }

        public RegexRule(String regex1, int flag) {
            if (regex1 != null && !regex1.isEmpty()) {
                this.regex = regex1;
                this.pattern = Pattern.compile(this.regex, flag);
            }
        }

        public RegexRule(String regex1, int flag, int type) {
            if (regex1 != null && !regex1.isEmpty()) {
                this.regex = regex1;
                this.type = type;
                this.pattern = Pattern.compile(this.regex, flag);
            }
        }

        public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
            MatchedNumberInfo matcher = new MatchedNumberInfo();
            matcher.setBegin(0);
            matcher.setEnd(1);
            matcher.setContent("");
            List<MatchedNumberInfo> ret = new ArrayList();
            ret.add(matcher);
            return ret;
        }

        public PhoneNumberMatch isValid(PhoneNumberMatch possibleNumber, String msg) {
            return possibleNumber;
        }
    }

    protected synchronized Pattern getPatternFromCache(String key) {
        Pattern t;
        if (this.patternCache == null) {
            this.patternCache = new HashMap();
            t = Pattern.compile(new ConstantsUtils().getValues(key));
            this.patternCache.put(key, t);
            return t;
        } else if (this.patternCache.containsKey(key)) {
            return (Pattern) this.patternCache.get(key);
        } else {
            t = Pattern.compile(new ConstantsUtils().getValues(key));
            this.patternCache.put(key, t);
            return t;
        }
    }

    public void init() {
    }

    public PhoneNumberRule(String country) {
        init();
    }

    public List<RegexRule> getNegativeRules() {
        return this.negativeRules;
    }

    public List<RegexRule> getPositiveRules() {
        return this.positiveRules;
    }

    public List<RegexRule> getBorderRules() {
        return this.borderRules;
    }

    public List<RegexRule> getCodesRules() {
        return this.codesRules;
    }
}
