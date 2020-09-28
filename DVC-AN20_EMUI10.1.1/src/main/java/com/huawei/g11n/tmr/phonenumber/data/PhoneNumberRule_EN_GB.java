package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.MatchedNumberInfo;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberRule_EN_GB extends PhoneNumberRule {
    public PhoneNumberRule_EN_GB(String country) {
        super(country);
        init();
    }

    @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule
    public void init() {
        ConstantsUtils utils = new ConstantsUtils();
        this.negativeRules = new ArrayList();
        for (String regex : new String[]{"\\p{Sc}\\d+[.]?\\d+", "4x\\d0+", "&amp;#\\d{2,5}", "\\d+((th)|%)", "(?<!(call))[iI]n \\d{4}", "([fF]rom|[uU]ntil) \\d{4}", "\\d{4,}[+]"}) {
            this.negativeRules.add(new PhoneNumberRule.RegexRule(regex));
        }
        this.negativeRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.FLOAT_2)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.AI)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE1)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE2)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DP)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.EMAIL)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.EXP)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.TIME)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.URL)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule("\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(kilobytes|kilowatt-hours|megabytes|kilojoules|square\\p{Blank}+centimetres|gal\\p{Blank}+US|kilohertz|byte|calories|milliamperes|MB|m²|MW|hours|gigahertz|kilograms|pints|oz|ha|bit|Tb|pm|years|Mb|bytes|A|hPa|pounds|GB|carats|degrees\\p{Blank}+Celsius|J|K|picometres|W|V|secs|deg|mg|dm|dl|decimetres|hectopascals|wks|megawatts|ml|centimetres|kHz|joules|ounces|g|decilitres|mm|Gb|ms|l|mins|m|nmi|millimetres|weeks|yards|kWh|days|seconds|grams|volts|tn|nautical\\p{Blank}+miles|kilocalories|kW|Calories|yrs|US\\p{Blank}+gallons|cm²|degrees\\p{Blank}+Fahrenheit|kg|Ω|amperes|kilometres|kb|milliseconds|tons|kilowatts|gigabytes|hertz|megahertz|mths|watts|kilobits|litres|km|Hz|cm|GHz|gigabits|mA|°|lb|millilitres|Cal|metres|ohms|hectares|milligrams|hrs|kelvin|bits|cal|MHz|TB|megabits|°C|square\\p{Blank}+metres|minutes|°F|yd|terabits|kcal|degrees|months|kJ|CD|pt|terabytes|kB)(?!\\p{L})", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule("(?<!\\p{L})(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|January|February|March|April|May|June|July|August|September|October|November|December)\\p{Blank}*(-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?", 2));
        List<PhoneNumberRule.RegexRule> bRules = new ArrayList<>();
        bRules.add(new PhoneNumberRule.RegexRule("(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,})", 2, 9));
        bRules.add(new PhoneNumberRule.RegexRule("[\\(\\[]?1\\d(0\\d|1[012])([012]\\d|3[01])", 2, 9));
        bRules.add(new PhoneNumberRule.RegexRule("[1-9]0{3,10}", 2, 9));
        this.borderRules = bRules;
        List<PhoneNumberRule.RegexRule> cRules = new ArrayList<>();
        cRules.add(new PhoneNumberRule.RegexRule(this, BuildConfig.FLAVOR) {
            /* class com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_EN_GB.AnonymousClass1 */

            @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public PhoneNumberMatch isValid(PhoneNumberMatch possibleNumber, String msg) {
                boolean isvalid = true;
                String number = possibleNumber.rawString();
                int ind = number.trim().indexOf(";ext=");
                if (ind != -1) {
                    number = number.trim().substring(0, ind);
                }
                if (number.startsWith("(") || number.startsWith("[")) {
                    number = number.substring(1);
                }
                if (!number.startsWith("0") && PhoneNumberRule_EN_GB.countDigits(number) == 8) {
                    isvalid = false;
                }
                if (PhoneNumberRule_EN_GB.countDigits(number) <= 4) {
                    isvalid = false;
                }
                if (isvalid) {
                    return possibleNumber;
                }
                return null;
            }
        });
        this.codesRules = cRules;
        this.codesRules.add(new PhoneNumberRule.RegexRule(this, BuildConfig.FLAVOR) {
            /* class com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_EN_GB.AnonymousClass2 */

            @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public PhoneNumberMatch isValid(PhoneNumberMatch possibleNumber, String msg) {
                if (possibleNumber.start() - 1 >= 0) {
                    char[] beChars = msg.substring(0, possibleNumber.start()).toCharArray();
                    boolean isTwo = true;
                    int i = 0;
                    while (true) {
                        if (i < beChars.length) {
                            char t = beChars[(beChars.length - 1) - i];
                            if (i != 0 || Character.isUpperCase(t)) {
                                if (i < 2 && Character.isLetter(t)) {
                                    if (!Character.isUpperCase(t)) {
                                        isTwo = false;
                                        break;
                                    }
                                    i++;
                                } else if (t == '-' || t == '\'') {
                                    isTwo = false;
                                } else if (!Character.isDigit(t) && !Character.isWhitespace(t) && Character.isLetter(t)) {
                                    isTwo = false;
                                }
                            } else {
                                isTwo = false;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    if (!isTwo) {
                        return possibleNumber;
                    }
                    return null;
                } else if (possibleNumber.end() > msg.length() - 1) {
                    return possibleNumber;
                } else {
                    char[] beChars2 = msg.substring(possibleNumber.end()).toCharArray();
                    boolean isTwo2 = true;
                    int i2 = 0;
                    while (true) {
                        if (i2 < beChars2.length) {
                            char t2 = beChars2[i2];
                            if (i2 == 0 && !Character.isUpperCase(t2)) {
                                isTwo2 = false;
                                break;
                            }
                            if (i2 < 2 && Character.isLetter(t2)) {
                                if (!Character.isUpperCase(t2)) {
                                    isTwo2 = false;
                                    break;
                                }
                            } else if (i2 == 1 || i2 == 2) {
                                if (t2 == '-' || t2 == '\'') {
                                    isTwo2 = false;
                                } else if (!Character.isDigit(t2) && !Character.isWhitespace(t2) && Character.isLetter(t2)) {
                                    isTwo2 = false;
                                }
                            }
                            i2++;
                        } else {
                            break;
                        }
                    }
                    if (!isTwo2) {
                        return possibleNumber;
                    }
                    return null;
                }
            }
        });
        this.positiveRules = new ArrayList();
        this.positiveRules.add(new PhoneNumberRule.RegexRule(this, "\\d{5,}+\\s*+[(]?mob", 2) {
            /* class com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_EN_GB.AnonymousClass3 */

            @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                return PhoneNumberRule_EN_GB.this.handlePossibleNumberWithPattern(getPattern(), possibleNumber, msg, true);
            }
        });
        this.positiveRules.add(new PhoneNumberRule.RegexRule(this, "([mM]ob(ile)?|[cC]all|landline|fixedline)[:]?\\s*+\\d{5,}+", 2) {
            /* class com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_EN_GB.AnonymousClass4 */

            @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                return PhoneNumberRule_EN_GB.this.handlePossibleNumberWithPattern(getPattern(), possibleNumber, msg, false);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<MatchedNumberInfo> handlePossibleNumberWithPattern(Pattern p, PhoneNumberMatch possibleNumber, String msg, boolean startsWith) {
        boolean ok;
        String possible = possibleNumber.rawString();
        Matcher m = p.matcher(msg);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String matched = msg.subSequence(start, end).toString();
            if (startsWith) {
                ok = matched.startsWith(possible);
                continue;
            } else {
                ok = matched.endsWith(possible);
                continue;
            }
            if (ok) {
                List<MatchedNumberInfo> ret = new ArrayList<>();
                MatchedNumberInfo info = new MatchedNumberInfo();
                info.setBegin(startsWith ? start : end - possible.length());
                info.setEnd(startsWith ? possible.length() + start : end);
                info.setContent(possible);
                ret.add(info);
                return ret;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static int countDigits(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                count++;
            }
        }
        return count;
    }
}
