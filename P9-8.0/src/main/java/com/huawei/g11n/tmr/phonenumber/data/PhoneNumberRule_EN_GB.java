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
    public PhoneNumberRule_EN_GB(String country) {
        super(country);
        init();
    }

    public void init() {
        ConstantsUtils utils = new ConstantsUtils();
        String[] nRegex = new String[]{"\\p{Sc}\\d+[.]?\\d+", "4x\\d0+", "&amp;#\\d{2,5}", "\\d+((th)|%)", "(?<!(call))[iI]n \\d{4}", "([fF]rom|[uU]ntil) \\d{4}", "\\d{4,}[+]"};
        this.negativeRules = new ArrayList();
        String[] strArr = nRegex;
        for (String regex : nRegex) {
            this.negativeRules.add(new RegexRule(regex));
        }
        this.negativeRules.add(new RegexRule(utils.getValues(ConstantsUtils.FLOAT_1)));
        this.negativeRules.add(new RegexRule(utils.getValues(ConstantsUtils.AI)));
        this.negativeRules.add(new RegexRule(utils.getValues(ConstantsUtils.DATE)));
        this.negativeRules.add(new RegexRule(utils.getValues(ConstantsUtils.DATE1)));
        this.negativeRules.add(new RegexRule(utils.getValues(ConstantsUtils.DATE2)));
        this.negativeRules.add(new RegexRule(utils.getValues(ConstantsUtils.DP)));
        this.negativeRules.add(new RegexRule(utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        this.negativeRules.add(new RegexRule(utils.getValues(ConstantsUtils.EMAIL)));
        this.negativeRules.add(new RegexRule(utils.getValues(ConstantsUtils.EXP)));
        this.negativeRules.add(new RegexRule(utils.getValues(ConstantsUtils.TIME)));
        this.negativeRules.add(new RegexRule(utils.getValues(ConstantsUtils.URL)));
        this.negativeRules.add(new RegexRule("(\\d{1,16}[\\p{Blank}.,-]?){1,4}\\d{1,16}\\p{Blank}{0,4}(kilobytes|kilowatt-hours|megabytes|kilojoules|square\\p{Blank}+centimetres|gal\\p{Blank}+US|kilohertz|byte|calories|milliamperes|MB|m²|MW|hours|gigahertz|kilograms|pints|oz|ha|bit|Tb|pm|years|Mb|bytes|A|hPa|pounds|GB|carats|degrees\\p{Blank}+Celsius|J|K|picometres|W|V|secs|deg|mg|dm|dl|decimetres|hectopascals|wks|megawatts|ml|centimetres|kHz|joules|ounces|g|decilitres|mm|Gb|ms|l|mins|m|nmi|millimetres|weeks|yards|kWh|days|seconds|grams|volts|tn|nautical\\p{Blank}+miles|kilocalories|kW|Calories|yrs|US\\p{Blank}+gallons|cm²|degrees\\p{Blank}+Fahrenheit|kg|Ω|amperes|kilometres|kb|milliseconds|tons|kilowatts|gigabytes|hertz|megahertz|mths|watts|kilobits|litres|km|Hz|cm|GHz|gigabits|mA|°|lb|millilitres|Cal|metres|ohms|hectares|milligrams|hrs|kelvin|bits|cal|MHz|TB|megabits|°C|square\\p{Blank}+metres|minutes|°F|yd|terabits|kcal|degrees|months|kJ|CD|pt|terabytes|kB)(?!\\p{L})", 2));
        this.negativeRules.add(new RegexRule("(?<!\\p{L})(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|January|February|March|April|May|June|July|August|September|October|November|December)\\p{Blank}*(-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?", 2));
        List<RegexRule> bRules = new ArrayList();
        bRules.add(new RegexRule("(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,})", 2, 9));
        bRules.add(new RegexRule("[\\(\\[]?1\\d(0\\d|1[012])([012]\\d|3[01])", 2, 9));
        bRules.add(new RegexRule("[1-9]0{3,10}", 2, 9));
        this.borderRules = bRules;
        List<RegexRule> cRules = new ArrayList();
        cRules.add(new RegexRule(this, "") {
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
                if (!number.startsWith(System.FINGERSENSE_KNUCKLE_GESTURE_OFF) && PhoneNumberRule_EN_GB.countDigits(number) == 8) {
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
        this.codesRules.add(new RegexRule(this, "") {
            public PhoneNumberMatch isValid(PhoneNumberMatch possibleNumber, String msg) {
                char[] beChars;
                boolean isTwo;
                int i;
                char t;
                if (possibleNumber.start() - 1 >= 0) {
                    beChars = msg.substring(0, possibleNumber.start()).toCharArray();
                    isTwo = true;
                    i = 0;
                    while (i < beChars.length) {
                        t = beChars[(beChars.length - 1) - i];
                        if (i == 0 && !Character.isUpperCase(t)) {
                            isTwo = false;
                            break;
                        } else if (i < 2 && Character.isLetter(t)) {
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
                    }
                    if (isTwo) {
                        return null;
                    }
                    return possibleNumber;
                } else if (possibleNumber.end() > msg.length() - 1) {
                    return possibleNumber;
                } else {
                    beChars = msg.substring(possibleNumber.end()).toCharArray();
                    isTwo = true;
                    i = 0;
                    while (i < beChars.length) {
                        t = beChars[i];
                        if (i == 0 && !Character.isUpperCase(t)) {
                            isTwo = false;
                            break;
                        }
                        if (i < 2 && Character.isLetter(t)) {
                            if (!Character.isUpperCase(t)) {
                                isTwo = false;
                                break;
                            }
                        } else if (i == 1 || i == 2) {
                            if (t == '-' || t == '\'') {
                                isTwo = false;
                            } else if (!Character.isDigit(t) && !Character.isWhitespace(t) && Character.isLetter(t)) {
                                isTwo = false;
                            }
                        }
                        i++;
                    }
                    if (isTwo) {
                        return null;
                    }
                    return possibleNumber;
                }
            }
        });
        this.positiveRules = new ArrayList();
        this.positiveRules.add(new RegexRule(this, "\\d{5,}+\\s*+[(]?mob", 2) {
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                return PhoneNumberRule_EN_GB.this.handlePossibleNumberWithPattern(getPattern(), possibleNumber, msg, true);
            }
        });
        this.positiveRules.add(new RegexRule(this, "([mM]ob(ile)?|[cC]all|landline|fixedline)[:]?\\s*+\\d{5,}+", 2) {
            public List<MatchedNumberInfo> handle(PhoneNumberMatch possibleNumber, String msg) {
                return PhoneNumberRule_EN_GB.this.handlePossibleNumberWithPattern(getPattern(), possibleNumber, msg, false);
            }
        });
    }

    private List<MatchedNumberInfo> handlePossibleNumberWithPattern(Pattern p, PhoneNumberMatch possibleNumber, String msg, boolean startsWith) {
        String possible = possibleNumber.rawString();
        Matcher m = p.matcher(msg);
        while (m.find()) {
            boolean ok;
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
                int i;
                List<MatchedNumberInfo> ret = new ArrayList();
                MatchedNumberInfo info = new MatchedNumberInfo();
                if (startsWith) {
                    i = start;
                } else {
                    i = end - possible.length();
                }
                info.setBegin(i);
                if (startsWith) {
                    end = start + possible.length();
                }
                info.setEnd(end);
                info.setContent(possible);
                ret.add(info);
                return ret;
            }
        }
        return null;
    }

    private static int countDigits(String str) {
        int count = 0;
        char[] ch = str.toCharArray();
        char[] cArr = ch;
        int length = ch.length;
        for (int i = 0; i < length; i++) {
            if (Character.isDigit(cArr[i])) {
                count++;
            }
        }
        return count;
    }
}
