package com.huawei.i18n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.phonenumber.MatchedNumberInfo;
import com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UKPhoneNumberRule extends PhoneNumberRule {
    private final String negativeRule1 = "(?<!\\p{L})(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|January|February|March|April|May|June|July|August|September|October|November|December)\\p{Blank}*(-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?";
    private final String negativeRule2 = "\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(kilobytes|kilowatt-hours|megabytes|kilojoules|square\\p{Blank}+centimetres|gal\\p{Blank}+US|kilohertz|byte|calories|milliamperes|MB|m²|MW|hours|gigahertz|kilograms|pints|oz|ha|bit|Tb|pm|years|Mb|bytes|A|hPa|pounds|GB|carats|degrees\\p{Blank}+Celsius|J|K|picometres|W|V|secs|deg|mg|dm|dl|decimetres|hectopascals|wks|megawatts|ml|centimetres|kHz|joules|ounces|g|decilitres|mm|Gb|ms|l|mins|m|nmi|millimetres|weeks|yards|kWh|days|seconds|grams|volts|tn|nautical\\p{Blank}+miles|kilocalories|kW|Calories|yrs|US\\p{Blank}+gallons|cm²|degrees\\p{Blank}+Fahrenheit|kg|Ω|amperes|kilometres|kb|milliseconds|tons|kilowatts|gigabytes|hertz|megahertz|mths|watts|kilobits|litres|km|Hz|cm|GHz|gigabits|mA|°|lb|millilitres|Cal|metres|ohms|hectares|milligrams|hrs|kelvin|bits|cal|MHz|TB|megabits|°C|square\\p{Blank}+metres|minutes|°F|yd|terabits|kcal|degrees|months|kJ|CD|pt|terabytes|kB)(?!\\p{L})";

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initPositiveRules() {
        this.positiveRules.add(new PhoneNumberRule.RegexRule("\\d{5,}+\\s*+[(]?mob", 2) {
            /* class com.huawei.i18n.tmr.phonenumber.data.UKPhoneNumberRule.AnonymousClass1 */

            @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                return UKPhoneNumberRule.this.handlePossibleNumberWithPattern(getPattern(), possibleNumber, msg, true);
            }
        });
        this.positiveRules.add(new PhoneNumberRule.RegexRule("([mM]ob(ile)?|[cC]all|landline|fixedline)[:]?\\s*+\\d{5,}+", 2) {
            /* class com.huawei.i18n.tmr.phonenumber.data.UKPhoneNumberRule.AnonymousClass2 */

            @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                return UKPhoneNumberRule.this.handlePossibleNumberWithPattern(getPattern(), possibleNumber, msg, false);
            }
        });
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initNegativeRules() {
        initCommonNegativeRules();
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, this.utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\p{Sc}\\d+[.]?\\d+"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "4x\\d0+"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "&amp;#\\d{2,5}"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d+((th)|%)"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!(call))[iI]n \\d{4}"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "([fF]rom|[uU]ntil) \\d{4}"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d{4,}[+]"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|January|February|March|April|May|June|July|August|September|October|November|December)\\p{Blank}*(-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(kilobytes|kilowatt-hours|megabytes|kilojoules|square\\p{Blank}+centimetres|gal\\p{Blank}+US|kilohertz|byte|calories|milliamperes|MB|m²|MW|hours|gigahertz|kilograms|pints|oz|ha|bit|Tb|pm|years|Mb|bytes|A|hPa|pounds|GB|carats|degrees\\p{Blank}+Celsius|J|K|picometres|W|V|secs|deg|mg|dm|dl|decimetres|hectopascals|wks|megawatts|ml|centimetres|kHz|joules|ounces|g|decilitres|mm|Gb|ms|l|mins|m|nmi|millimetres|weeks|yards|kWh|days|seconds|grams|volts|tn|nautical\\p{Blank}+miles|kilocalories|kW|Calories|yrs|US\\p{Blank}+gallons|cm²|degrees\\p{Blank}+Fahrenheit|kg|Ω|amperes|kilometres|kb|milliseconds|tons|kilowatts|gigabytes|hertz|megahertz|mths|watts|kilobits|litres|km|Hz|cm|GHz|gigabits|mA|°|lb|millilitres|Cal|metres|ohms|hectares|milligrams|hrs|kelvin|bits|cal|MHz|TB|megabits|°C|square\\p{Blank}+metres|minutes|°F|yd|terabits|kcal|degrees|months|kJ|CD|pt|terabytes|kB)(?!\\p{L})", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, this.utils.getValues(ConstantsUtils.FLOAT_2)));
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initCodeRules() {
        this.codesRules.add(getRegexRuleFromPrefixAndSuffix());
        this.codesRules.add(getRegexRuleFromRawString());
    }

    private PhoneNumberRule.RegexRule getRegexRuleFromRawString() {
        return new PhoneNumberRule.RegexRule(StorageManagerExt.INVALID_KEY_DESC) {
            /* class com.huawei.i18n.tmr.phonenumber.data.UKPhoneNumberRule.AnonymousClass3 */

            @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public Optional<PhoneNumberMatch> isValid(PhoneNumberMatch possibleNumber, String msg) {
                boolean isValid = true;
                String number = possibleNumber.rawString();
                int ind = number.trim().indexOf(";ext=");
                if (ind != -1) {
                    number = number.trim().substring(0, ind);
                }
                if (number.startsWith("(") || number.startsWith("[")) {
                    number = number.substring(1);
                }
                if (!number.startsWith("0") && UKPhoneNumberRule.countDigits(number) == 8) {
                    isValid = false;
                }
                if (UKPhoneNumberRule.countDigits(number) <= 4) {
                    isValid = false;
                }
                if (isValid) {
                    return Optional.of(possibleNumber);
                }
                return Optional.empty();
            }
        };
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initBoarderRules() {
        this.borderRules.add(new PhoneNumberRule.RegexRule("(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,})", 2, 9));
        this.borderRules.add(new PhoneNumberRule.RegexRule("[\\(\\[]?1\\d(0\\d|1[012])([012]\\d|3[01])", 2, 9));
        this.borderRules.add(new PhoneNumberRule.RegexRule("[1-9]0{3,10}", 2, 9));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<MatchedNumberInfo> handlePossibleNumberWithPattern(Pattern pattern, PhoneNumberMatch possibleNumber, String msg, boolean isStartsWithNumber) {
        boolean isMatch;
        String possible = possibleNumber.rawString();
        Matcher matcher = pattern.matcher(msg);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String matched = msg.subSequence(start, end).toString();
            if (isStartsWithNumber) {
                isMatch = matched.startsWith(possible);
                continue;
            } else {
                isMatch = matched.endsWith(possible);
                continue;
            }
            if (isMatch) {
                MatchedNumberInfo info = new MatchedNumberInfo();
                info.setBegin(isStartsWithNumber ? start : end - possible.length());
                info.setEnd(isStartsWithNumber ? possible.length() + start : end);
                info.setContent(possible);
                List<MatchedNumberInfo> matchedNumberInfoList = new ArrayList<>();
                matchedNumberInfoList.add(info);
                return matchedNumberInfoList;
            }
        }
        return Collections.emptyList();
    }

    /* access modifiers changed from: private */
    public static int countDigits(String str) {
        int count = 0;
        for (char ch : str.toCharArray()) {
            if (Character.isDigit(ch)) {
                count++;
            }
        }
        return count;
    }
}
