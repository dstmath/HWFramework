package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;
import com.android.i18n.phonenumbers.ShortNumberInfo;
import com.huawei.g11n.tmr.phonenumber.MatchedNumberInfo;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberRule {
    protected List<RegexRule> borderRules;
    protected List<RegexRule> codesRules;
    protected String extraShortPattern = BuildConfig.FLAVOR;
    protected List<RegexRule> negativeRules;
    protected HashMap<String, Pattern> patternCache;
    protected List<RegexRule> positiveRules;

    /* access modifiers changed from: protected */
    public synchronized Pattern getPatternFromCache(String key) {
        if (this.patternCache == null) {
            this.patternCache = new HashMap<>();
            Pattern t = Pattern.compile(new ConstantsUtils().getValues(key));
            this.patternCache.put(key, t);
            return t;
        } else if (this.patternCache.containsKey(key)) {
            return this.patternCache.get(key);
        } else {
            Pattern t2 = Pattern.compile(new ConstantsUtils().getValues(key));
            this.patternCache.put(key, t2);
            return t2;
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

        public void setType(int type2) {
            this.type = type2;
        }

        public void setRegex(String regex2) {
            this.regex = regex2;
        }

        public Pattern getPattern() {
            return this.pattern;
        }

        public void setPattern(Pattern pattern2) {
            this.pattern = pattern2;
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

        public RegexRule(String regex1, int flag, int type2) {
            if (regex1 != null && !regex1.isEmpty()) {
                this.regex = regex1;
                this.type = type2;
                this.pattern = Pattern.compile(this.regex, flag);
            }
        }

        public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
            MatchedNumberInfo matcher = new MatchedNumberInfo();
            matcher.setBegin(0);
            matcher.setEnd(1);
            matcher.setContent(BuildConfig.FLAVOR);
            List<MatchedNumberInfo> ret = new ArrayList<>();
            ret.add(matcher);
            return ret;
        }

        public PhoneNumberMatch isValid(PhoneNumberMatch possibleNumber, String msg) {
            return possibleNumber;
        }
    }

    public List<MatchedNumberInfo> handleShortPhoneNumbers(String msg, String country) {
        Phonenumber.PhoneNumber pn = null;
        PhoneNumberUtil putil = PhoneNumberUtil.getInstance();
        ShortNumberInfo info = ShortNumberInfo.getInstance();
        List<MatchedNumberInfo> ret = new ArrayList<>();
        Pattern shortPattern = Pattern.compile("(?<!(\\d|\\*|-))\\d{2,7}(?!(\\d|\\*|-))");
        Matcher eShortMatch = Pattern.compile(this.extraShortPattern).matcher(msg);
        Matcher shortMatch = shortPattern.matcher(msg);
        while (shortMatch.find()) {
            try {
                pn = putil.parseAndKeepRawInput(shortMatch.group(), country);
            } catch (NumberParseException e) {
                e.printStackTrace();
            }
            if (pn != null && info.isPossibleShortNumberForRegion(pn, country)) {
                MatchedNumberInfo matcher = new MatchedNumberInfo();
                matcher.setBegin(shortMatch.start());
                matcher.setEnd(shortMatch.end());
                matcher.setContent(shortMatch.group());
                ret.add(matcher);
            }
        }
        if (!this.extraShortPattern.equals(BuildConfig.FLAVOR) && this.extraShortPattern != null) {
            while (eShortMatch.find()) {
                MatchedNumberInfo matcher2 = new MatchedNumberInfo();
                matcher2.setBegin(eShortMatch.start());
                matcher2.setEnd(eShortMatch.end());
                matcher2.setContent(eShortMatch.group());
                ret.add(matcher2);
            }
        }
        return ret;
    }
}
