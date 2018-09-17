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
        private int type;

        public String getRegex() {
            return this.regex;
        }

        public int getType() {
            return this.type;
        }

        public void setType(int i) {
            this.type = i;
        }

        public void setRegex(String str) {
            this.regex = str;
        }

        public Pattern getPattern() {
            return this.pattern;
        }

        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        public RegexRule(String str) {
            this.type = 0;
            if (str != null && !str.isEmpty()) {
                this.regex = str;
                this.pattern = Pattern.compile(this.regex);
            }
        }

        public RegexRule(String str, int i) {
            this.type = 0;
            if (str != null && !str.isEmpty()) {
                this.regex = str;
                this.pattern = Pattern.compile(this.regex, i);
            }
        }

        public RegexRule(String str, int i, int i2) {
            this.type = 0;
            if (str != null && !str.isEmpty()) {
                this.regex = str;
                this.type = i2;
                this.pattern = Pattern.compile(this.regex, i);
            }
        }

        public List<MatchedNumberInfo> handle(PhoneNumberMatch phoneNumberMatch, String str) {
            MatchedNumberInfo matchedNumberInfo = new MatchedNumberInfo();
            matchedNumberInfo.setBegin(0);
            matchedNumberInfo.setEnd(1);
            matchedNumberInfo.setContent("");
            List<MatchedNumberInfo> arrayList = new ArrayList();
            arrayList.add(matchedNumberInfo);
            return arrayList;
        }

        public PhoneNumberMatch isValid(PhoneNumberMatch phoneNumberMatch, String str) {
            return phoneNumberMatch;
        }
    }

    protected synchronized Pattern getPatternFromCache(String str) {
        Pattern compile;
        if (this.patternCache == null) {
            this.patternCache = new HashMap();
            compile = Pattern.compile(new ConstantsUtils().getValues(str));
            this.patternCache.put(str, compile);
            return compile;
        } else if (this.patternCache.containsKey(str)) {
            return (Pattern) this.patternCache.get(str);
        } else {
            compile = Pattern.compile(new ConstantsUtils().getValues(str));
            this.patternCache.put(str, compile);
            return compile;
        }
    }

    public void init() {
    }

    public PhoneNumberRule(String str) {
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
