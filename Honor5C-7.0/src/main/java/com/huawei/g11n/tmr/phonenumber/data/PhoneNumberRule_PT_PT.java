package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_PT_PT extends PhoneNumberRule {

    /* renamed from: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_PT_PT.1 */
    class AnonymousClass1 extends RegexRule {
        AnonymousClass1(PhoneNumberRule phoneNumberRule, String str) {
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

    public PhoneNumberRule_PT_PT(String str) {
        super(str);
        init();
    }

    public void init() {
        List arrayList = new ArrayList();
        String str = "FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?";
        ConstantsUtils constantsUtils = new ConstantsUtils();
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.AI)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.URL)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE1)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE2)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DP)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EXP)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.FLOAT_1)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.YEAR_PERIOD)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EMAIL)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.TIME)));
        String str2 = "([012]?\\d|3[01])(.|-|\\p{Blank}){0,2}(" + "jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez|janeiro|fevereiro|mar\u00e7o|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro" + ")(.|-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}{0,2})(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?";
        arrayList.add(new RegexRule("[0-9.,]+(?<![.,])\\p{Blank}*(\u2030|%|\\p{Sc}|" + str + "(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(" + str + "))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        arrayList.add(new RegexRule("\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(" + str + ")", 66));
        str = "(\\d{1,16}[\\p{Blank}.,-]?){1,4}\\d{1,16}\\p{Blank}{0,4}(" + "minutos|segundos|kilobytes|megabytes|toneladas|dias|kilohertz|byte|metros\\p{Blank}+quadrados|quil\u00f3metros|MB|decilitros|m\u00b2|MW|mililitros|gigahertz|pints|oz|ha|bit|hectopascais|Tb|pm|gal|Mb|milissegundos|bytes|quilocalorias|A|hPa|GB|jardas|J|K|pic\u00f3metros|W|V|quilojoules|kelvins|mg|dm|dl|megawatts|ml|kHz|joules|min|quilogramas|g|sem\\.|mm|graus|miliamperes|miligramas|Gb|cent\u00edmetros|ms|l|milhas\\p{Blank}+n\u00e1uticas|m|nmi|h|kWh|s|quilates|semanas|volts|kW|horas|cm\u00b2|calorias|libras|meses|anos|kg|\u03a9|quilowatts|amperes|kb|quilowatts-hora|gigabytes|hertz|megahertz|watts|kilobits|on\u00e7as|ct|km|mil\u00edmetros|Hz|cm|GHz|gigabits|mA|graus\\p{Blank}+Celsius|\u00b0|cent\u00edmetros\\p{Blank}+quadrados|lb|metros|ohms|litros|hectares|dec\u00edmetros|bits|ton|cal|MHz|TB|megabits|\u00b0C|\u00b0F|yd|terabits|kcal|gal\u00f5es|kJ|pt|gramas|terabytes|graus\\p{Blank}+Fahrenheit|kB" + ")(?![\\p{L}0-9])";
        arrayList.add(new RegexRule("(?<![\\p{L}])((ref|id|num|qq|ICQ|tweets?|twitter|(decreto|consulta)\\p{Blank}+n\u00ba|S\u00e9rie|nif|FM|contas)\\p{Blank}*:?)\\p{Blank}*[0-9]{4,16}", 66));
        arrayList.add(new RegexRule(str2, 2));
        arrayList.add(new RegexRule(str, 2));
        arrayList.add(new RegexRule("(\\d{1,16}[\\p{Blank}.,-]?){1,4}\\d{1,16}\\p{Blank}{0,4}(vezes|(de\\p{Blank}+)?tweets?|twittar|rts|temporada|contas|capitulos|p\u00e1ginas|no\\p{Blank}+total\\p{Blank}+em\\p{Blank}+dinheiro|milhoes|mil)(?!\\p{L})", 2));
        this.negativeRules = arrayList;
        arrayList = new ArrayList();
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        this.borderRules = arrayList;
        this.codesRules = new ArrayList();
        this.codesRules.add(new AnonymousClass1(this, ""));
    }
}
