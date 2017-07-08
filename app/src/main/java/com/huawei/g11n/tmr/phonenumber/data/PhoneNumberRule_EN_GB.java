package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.MatchedNumberInfo;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import huawei.android.provider.HwSettings.System;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberRule_EN_GB extends PhoneNumberRule {

    /* renamed from: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_EN_GB.1 */
    class AnonymousClass1 extends RegexRule {
        AnonymousClass1(PhoneNumberRule phoneNumberRule, String str) {
            super(str);
        }

        public PhoneNumberMatch isValid(PhoneNumberMatch phoneNumberMatch, String str) {
            int i = 1;
            String rawString = phoneNumberMatch.rawString();
            int indexOf = rawString.trim().indexOf(";ext=");
            if (indexOf != -1) {
                rawString = rawString.trim().substring(0, indexOf);
            }
            if (rawString.startsWith("(") || rawString.startsWith("[")) {
                rawString = rawString.substring(1);
            }
            if (!rawString.startsWith(System.FINGERSENSE_KNUCKLE_GESTURE_OFF) && PhoneNumberRule_EN_GB.countDigits(rawString) == 8) {
                i = 0;
            }
            if (PhoneNumberRule_EN_GB.countDigits(rawString) <= 4) {
                i = 0;
            }
            if (i == 0) {
                return null;
            }
            return phoneNumberMatch;
        }
    }

    /* renamed from: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_EN_GB.2 */
    class AnonymousClass2 extends RegexRule {
        AnonymousClass2(PhoneNumberRule phoneNumberRule, String str) {
            super(str);
        }

        public PhoneNumberMatch isValid(PhoneNumberMatch phoneNumberMatch, String str) {
            int i = 0;
            char[] toCharArray;
            int i2;
            char c;
            if (phoneNumberMatch.start() - 1 >= 0) {
                toCharArray = str.substring(0, phoneNumberMatch.start()).toCharArray();
                i2 = 0;
                while (i2 < toCharArray.length) {
                    c = toCharArray[(toCharArray.length - 1) - i2];
                    if (i2 == 0) {
                        if (!Character.isUpperCase(c)) {
                            break;
                        }
                    }
                    if (i2 < 2 && Character.isLetter(c)) {
                        if (!Character.isUpperCase(c)) {
                            break;
                        }
                        i2++;
                    } else if (!(c == '-' || c == '\'')) {
                        if (Character.isDigit(c)) {
                            i = 1;
                        } else if (Character.isWhitespace(c)) {
                            i = 1;
                        } else if (!Character.isLetter(c)) {
                            i = 1;
                        }
                    }
                }
                i = 1;
                if (i != 0) {
                    return null;
                }
                return phoneNumberMatch;
            } else if (phoneNumberMatch.end() > str.length() - 1) {
                return phoneNumberMatch;
            } else {
                toCharArray = str.substring(phoneNumberMatch.end()).toCharArray();
                i2 = 0;
                while (i2 < toCharArray.length) {
                    c = toCharArray[i2];
                    if (i2 == 0) {
                        if (!Character.isUpperCase(c)) {
                            break;
                        }
                    }
                    if (i2 < 2 && Character.isLetter(c)) {
                        if (!Character.isUpperCase(c)) {
                            break;
                        }
                    } else if (i2 == 1 || i2 == 2) {
                        if (!(c == '-' || c == '\'')) {
                            if (Character.isDigit(c)) {
                                i = 1;
                            } else if (Character.isWhitespace(c)) {
                                i = 1;
                            } else if (!Character.isLetter(c)) {
                                i = 1;
                            }
                        }
                    }
                    i2++;
                }
                i = 1;
                if (i != 0) {
                    return null;
                }
                return phoneNumberMatch;
            }
        }
    }

    /* renamed from: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_EN_GB.3 */
    class AnonymousClass3 extends RegexRule {
        AnonymousClass3(PhoneNumberRule phoneNumberRule, String str, int i) {
            super(str, i);
        }

        public List<MatchedNumberInfo> handle(PhoneNumberMatch phoneNumberMatch, String str) {
            return PhoneNumberRule_EN_GB.this.handlePossibleNumberWithPattern(getPattern(), phoneNumberMatch, str, true);
        }
    }

    /* renamed from: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_EN_GB.4 */
    class AnonymousClass4 extends RegexRule {
        AnonymousClass4(PhoneNumberRule phoneNumberRule, String str, int i) {
            super(str, i);
        }

        public List<MatchedNumberInfo> handle(PhoneNumberMatch phoneNumberMatch, String str) {
            return PhoneNumberRule_EN_GB.this.handlePossibleNumberWithPattern(getPattern(), phoneNumberMatch, str, false);
        }
    }

    public PhoneNumberRule_EN_GB(String str) {
        super(str);
        init();
    }

    public void init() {
        int i = 0;
        ConstantsUtils constantsUtils = new ConstantsUtils();
        String[] strArr = new String[]{"\\p{Sc}\\d+[.]?\\d+", "4x\\d0+", "&amp;#\\d{2,5}", "\\d+((th)|%)", "(?<!(call))[iI]n \\d{4}", "([fF]rom|[uU]ntil) \\d{4}", "\\d{4,}[+]"};
        this.negativeRules = new ArrayList();
        int length = strArr.length;
        while (i < length) {
            this.negativeRules.add(new RegexRule(strArr[i]));
            i++;
        }
        this.negativeRules.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.FLOAT_1)));
        this.negativeRules.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.AI)));
        this.negativeRules.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE)));
        this.negativeRules.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE1)));
        this.negativeRules.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE2)));
        this.negativeRules.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DP)));
        this.negativeRules.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.YEAR_PERIOD)));
        this.negativeRules.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EMAIL)));
        this.negativeRules.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EXP)));
        this.negativeRules.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.TIME)));
        this.negativeRules.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.URL)));
        this.negativeRules.add(new RegexRule("(\\d{1,16}[\\p{Blank}.,-]?){1,4}\\d{1,16}\\p{Blank}{0,4}(kilobytes|kilowatt-hours|megabytes|kilojoules|square\\p{Blank}+centimetres|gal\\p{Blank}+US|kilohertz|byte|calories|milliamperes|MB|m\u00b2|MW|hours|gigahertz|kilograms|pints|oz|ha|bit|Tb|pm|years|Mb|bytes|A|hPa|pounds|GB|carats|degrees\\p{Blank}+Celsius|J|K|picometres|W|V|secs|deg|mg|dm|dl|decimetres|hectopascals|wks|megawatts|ml|centimetres|kHz|joules|ounces|g|decilitres|mm|Gb|ms|l|mins|m|nmi|millimetres|weeks|yards|kWh|days|seconds|grams|volts|tn|nautical\\p{Blank}+miles|kilocalories|kW|Calories|yrs|US\\p{Blank}+gallons|cm\u00b2|degrees\\p{Blank}+Fahrenheit|kg|\u03a9|amperes|kilometres|kb|milliseconds|tons|kilowatts|gigabytes|hertz|megahertz|mths|watts|kilobits|litres|km|Hz|cm|GHz|gigabits|mA|\u00b0|lb|millilitres|Cal|metres|ohms|hectares|milligrams|hrs|kelvin|bits|cal|MHz|TB|megabits|\u00b0C|square\\p{Blank}+metres|minutes|\u00b0F|yd|terabits|kcal|degrees|months|kJ|CD|pt|terabytes|kB)(?!\\p{L})", 2));
        this.negativeRules.add(new RegexRule("(?<!\\p{L})(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|January|February|March|April|May|June|July|August|September|October|November|December)\\p{Blank}*(-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?", 2));
        List arrayList = new ArrayList();
        arrayList.add(new RegexRule("(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,})", 2, 9));
        arrayList.add(new RegexRule("[\\(\\[]?1\\d(0\\d|1[012])([012]\\d|3[01])", 2, 9));
        arrayList.add(new RegexRule("[1-9]0{3,10}", 2, 9));
        this.borderRules = arrayList;
        arrayList = new ArrayList();
        arrayList.add(new AnonymousClass1(this, ""));
        this.codesRules = arrayList;
        this.codesRules.add(new AnonymousClass2(this, ""));
        this.positiveRules = new ArrayList();
        this.positiveRules.add(new AnonymousClass3(this, "\\d{5,}+\\s*+[(]?mob", 2));
        this.positiveRules.add(new AnonymousClass4(this, "([mM]ob(ile)?|[cC]all|landline|fixedline)[:]?\\s*+\\d{5,}+", 2));
    }

    private List<MatchedNumberInfo> handlePossibleNumberWithPattern(Pattern pattern, PhoneNumberMatch phoneNumberMatch, String str, boolean z) {
        String rawString = phoneNumberMatch.rawString();
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            boolean startsWith;
            int start = matcher.start();
            int end = matcher.end();
            String charSequence = str.subSequence(start, end).toString();
            if (z) {
                startsWith = charSequence.startsWith(rawString);
                continue;
            } else {
                startsWith = charSequence.endsWith(rawString);
                continue;
            }
            if (startsWith) {
                int i;
                List<MatchedNumberInfo> arrayList = new ArrayList();
                MatchedNumberInfo matchedNumberInfo = new MatchedNumberInfo();
                if (z) {
                    i = start;
                } else {
                    i = end - rawString.length();
                }
                matchedNumberInfo.setBegin(i);
                matchedNumberInfo.setEnd(!z ? end : rawString.length() + start);
                matchedNumberInfo.setContent(rawString);
                arrayList.add(matchedNumberInfo);
                return arrayList;
            }
        }
        return null;
    }

    private static int countDigits(String str) {
        int i = 0;
        for (char isDigit : str.toCharArray()) {
            if (Character.isDigit(isDigit)) {
                i++;
            }
        }
        return i;
    }
}
