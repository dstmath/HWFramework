package com.huawei.i18n.tmr.phonenumber.data;

import android.util.Log;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;
import com.android.i18n.phonenumbers.ShortNumberInfo;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.phonenumber.MatchedNumberInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberRule {
    private static final String TAG = "PhoneNumberRule";
    List<RegexRule> borderRules = new ArrayList();
    List<RegexRule> codesRules = new ArrayList();
    String extraShortPattern = StorageManagerExt.INVALID_KEY_DESC;
    List<RegexRule> negativeRules = new ArrayList();
    private HashMap<String, Pattern> patternCache;
    List<RegexRule> positiveRules = new ArrayList();
    ConstantsUtils utils;

    /* access modifiers changed from: protected */
    public synchronized Pattern getPatternFromCache(String key) {
        if (this.patternCache == null) {
            this.patternCache = new HashMap<>(1);
            Pattern pattern = Pattern.compile(this.utils.getValues(key));
            this.patternCache.put(key, pattern);
            return pattern;
        } else if (this.patternCache.containsKey(key)) {
            return this.patternCache.get(key);
        } else {
            Pattern pattern2 = Pattern.compile(this.utils.getValues(key));
            this.patternCache.put(key, pattern2);
            return pattern2;
        }
    }

    public void init() {
        this.utils = new ConstantsUtils();
        initNegativeRules();
        initPositiveRules();
        initCodeRules();
        initBoarderRules();
    }

    /* access modifiers changed from: package-private */
    public RegexRule getRegexRuleFromPrefixAndSuffix() {
        return new RegexRule(StorageManagerExt.INVALID_KEY_DESC) {
            /* class com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule.AnonymousClass1 */

            @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public Optional<PhoneNumberMatch> isValid(PhoneNumberMatch possibleNumber, String msg) {
                if (possibleNumber.start() - 1 >= 0) {
                    return PhoneNumberRule.this.isValidStart(possibleNumber, msg);
                }
                if (possibleNumber.end() <= msg.length() - 1) {
                    return PhoneNumberRule.this.isValidEnd(possibleNumber, msg);
                }
                return Optional.of(possibleNumber);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003a, code lost:
        if (r4 == '-') goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003e, code lost:
        if (r4 != '\'') goto L_0x0041;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0045, code lost:
        if (java.lang.Character.isDigit(r4) != false) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004b, code lost:
        if (java.lang.Character.isWhitespace(r4) == false) goto L_0x004e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0052, code lost:
        if (java.lang.Character.isLetter(r4) != false) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0055, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0057, code lost:
        r2 = false;
     */
    private Optional<PhoneNumberMatch> isValidEnd(PhoneNumberMatch possibleNumber, String msg) {
        char[] afterChars = msg.substring(possibleNumber.end()).toCharArray();
        boolean isTwo = true;
        int i = 0;
        while (true) {
            if (i >= afterChars.length) {
                break;
            }
            char afterChar = afterChars[i];
            if (i == 0 && !Character.isUpperCase(afterChar)) {
                isTwo = false;
                break;
            }
            if (i < 2 && Character.isLetter(afterChar)) {
                if (!Character.isUpperCase(afterChar)) {
                    isTwo = false;
                    break;
                }
            } else if (i == 1 || i == 2) {
                break;
            }
            i++;
        }
        if (!isTwo) {
            return Optional.of(possibleNumber);
        }
        return Optional.empty();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
        if (r4 == '-') goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003d, code lost:
        if (r4 != '\'') goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0044, code lost:
        if (java.lang.Character.isDigit(r4) != false) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004a, code lost:
        if (java.lang.Character.isWhitespace(r4) == false) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0051, code lost:
        if (java.lang.Character.isLetter(r4) != false) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0054, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0056, code lost:
        r2 = false;
     */
    private Optional<PhoneNumberMatch> isValidStart(PhoneNumberMatch possibleNumber, String msg) {
        char[] beforeChars = msg.substring(0, possibleNumber.start()).toCharArray();
        boolean isTwo = true;
        int i = 0;
        while (true) {
            if (i >= beforeChars.length) {
                break;
            }
            char beforeChar = beforeChars[(beforeChars.length - 1) - i];
            if (i != 0 || Character.isUpperCase(beforeChar)) {
                if (i < 2 && Character.isLetter(beforeChar)) {
                    if (!Character.isUpperCase(beforeChar)) {
                        isTwo = false;
                        break;
                    }
                    i++;
                } else {
                    break;
                }
            } else {
                isTwo = false;
                break;
            }
        }
        if (!isTwo) {
            return Optional.of(possibleNumber);
        }
        return Optional.empty();
    }

    public void initBoarderRules() {
    }

    public void initCodeRules() {
    }

    public void initPositiveRules() {
    }

    public void initNegativeRules() {
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
        public static final int CONTAIN = 9;
        public static final int CONTAIN_OR_INTERSECT = 8;
        private Pattern pattern;
        private String regex;
        private int type;

        RegexRule(PhoneNumberRule this$02, String regex2) {
            this(this$02, regex2, 0);
        }

        RegexRule(PhoneNumberRule this$02, String regex2, int flag) {
            this(regex2, flag, 0);
        }

        RegexRule(String regex2, int flag, int type2) {
            this.type = 0;
            if (regex2 != null && !regex2.isEmpty()) {
                this.regex = regex2;
                this.type = type2;
                this.pattern = Pattern.compile(regex2, flag);
            }
        }

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

        public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
            MatchedNumberInfo matcher = new MatchedNumberInfo();
            matcher.setBegin(0);
            matcher.setEnd(1);
            matcher.setContent(StorageManagerExt.INVALID_KEY_DESC);
            List<MatchedNumberInfo> matchedNumberInfoList = new ArrayList<>(1);
            matchedNumberInfoList.add(matcher);
            return matchedNumberInfoList;
        }

        public Optional<PhoneNumberMatch> isValid(PhoneNumberMatch possibleNumber, String msg) {
            return Optional.ofNullable(possibleNumber);
        }
    }

    public List<MatchedNumberInfo> handleShortPhoneNumbers(String msg, String country) {
        Phonenumber.PhoneNumber phoneNumber = null;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        ShortNumberInfo shortNumberInfo = ShortNumberInfo.getInstance();
        List<MatchedNumberInfo> matchedNumberInfoList = new ArrayList<>();
        Pattern shortPattern = Pattern.compile("(?<!(\\d|\\*|-))\\d{2,7}(?!(\\d|\\*|-|%|分钟|版本))");
        Matcher extraShortMatch = Pattern.compile(this.extraShortPattern).matcher(msg);
        Matcher shortMatch = shortPattern.matcher(msg);
        while (shortMatch.find()) {
            try {
                phoneNumber = phoneNumberUtil.parseAndKeepRawInput(shortMatch.group(), country);
            } catch (NumberParseException e) {
                Log.e(TAG, "handleShortPhoneNumbers NumberParseException");
            }
            if (phoneNumber != null && shortNumberInfo.isPossibleShortNumberForRegion(phoneNumber, country)) {
                MatchedNumberInfo matcher = new MatchedNumberInfo();
                matcher.setBegin(shortMatch.start());
                matcher.setEnd(shortMatch.end());
                matcher.setContent(shortMatch.group());
                matchedNumberInfoList.add(matcher);
            }
        }
        if (!StorageManagerExt.INVALID_KEY_DESC.equals(this.extraShortPattern)) {
            while (extraShortMatch.find()) {
                MatchedNumberInfo matcher2 = new MatchedNumberInfo();
                matcher2.setBegin(extraShortMatch.start());
                matcher2.setEnd(extraShortMatch.end());
                matcher2.setContent(extraShortMatch.group());
                matchedNumberInfoList.add(matcher2);
            }
        }
        return matchedNumberInfoList;
    }

    /* access modifiers changed from: protected */
    public void initCommonNegativeRules() {
        this.negativeRules.add(new RegexRule(this, this.utils.getValues(ConstantsUtils.AI)));
        this.negativeRules.add(new RegexRule(this, this.utils.getValues(ConstantsUtils.DATE)));
        this.negativeRules.add(new RegexRule(this, this.utils.getValues(ConstantsUtils.DATE1)));
        this.negativeRules.add(new RegexRule(this, this.utils.getValues(ConstantsUtils.DATE2)));
        this.negativeRules.add(new RegexRule(this, this.utils.getValues(ConstantsUtils.DP)));
        this.negativeRules.add(new RegexRule(this, this.utils.getValues(ConstantsUtils.EMAIL)));
        this.negativeRules.add(new RegexRule(this, this.utils.getValues(ConstantsUtils.EXP)));
        this.negativeRules.add(new RegexRule(this, this.utils.getValues(ConstantsUtils.TIME)));
        this.negativeRules.add(new RegexRule(this, this.utils.getValues(ConstantsUtils.URL)));
    }
}
