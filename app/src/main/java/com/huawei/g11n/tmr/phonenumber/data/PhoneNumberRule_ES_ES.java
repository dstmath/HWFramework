package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_ES_ES extends PhoneNumberRule {

    /* renamed from: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_ES_ES.1 */
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

    public PhoneNumberRule_ES_ES(String str) {
        super(str);
        init();
    }

    public void init() {
        String str = "FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS";
        List arrayList = new ArrayList();
        List arrayList2 = new ArrayList();
        arrayList.add(new RegexRule("[0-9.,]+(?<![.,])\\p{Blank}*(\u2030|%|\\p{Sc}|" + str + "(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(" + str + "))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        arrayList.add(new RegexRule("(?<![\\p{L}])(ci|ref|plus(\\p{Blank}+de)?|i\\.?d|sn|tweets?|twitter|icq|qq)[\\p{Blank}:]*[0-9]{4,16}", 66));
        arrayList.add(new RegexRule("[0-9.,]{4,16}(?<![.,])\\p{Blank}*(" + "minutos|segundos|kilobytes|megavatios|megabytes|kilojulios|toneladas|kil\u00f3metros|MB|m\u00b2|decilitros|hectopascales|MW|mililitros|oz|ha|Tb|pm|gal|Mb|grados|amperios|bytes|A|B|hPa|kilohercios|GB|M|J|K|pic\u00f3metros|W|V|millas\\p{Blank}+n\u00e1uticas|voltios|mg|dm|dl|ml|kHz|min|sem\\.|g|mm|d|b|miliamperios|c|onzas|Gb|cent\u00edmetros|gramos|ms|a|l|m\\.|m|kilogramos|hect\u00e1reas|h|kilovatios|kWh|s|quilates|semanas|grados\\p{Blank}+Fahrenheit|megahercios|kW|ohmios|horas|kilovatios-hora|cm\u00b2|galones|cent\u00edmetros\\p{Blank}+cuadrados|miligramos|libras|meses|kg|\u03a9|kb|d\u00edas|julios|gigabytes|pintas|kilobits|km|mil\u00edmetros|Hz|cm|vatios|GHz|gigahercios|gigabits|mA|\u00b0|lb|yardas|calor\u00edas|metros|litros|dec\u00edmetros|kelvin|bits|ton|cal|MHz|TB|megabits|kilocalor\u00edas|\u00b0C|metros\\p{Blank}+cuadrados|grados\\p{Blank}+Celsius|\u00b0F|yd|milisegundos|terabits|kcal|a\u00f1os|kJ|pt|terabytes|kB|hercios" + "|" + "tweet|millones|mil" + ")(?![\\p{L}0-9])", 66));
        String str2 = "([012]?\\d|3[01])(.|-|\\p{Blank}){0,2}(" + "ene\\.|feb\\.|mar\\.|abr\\.|may\\.|jun\\.|jul\\.|ago\\.|sept\\.|oct\\.|nov\\.|dic\\.|enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre" + ")(.|-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}{0,2})(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?";
        ConstantsUtils constantsUtils = new ConstantsUtils();
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.AI)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.URL)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE1)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE2)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DP)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EXP)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.FLOAT_1)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EMAIL)));
        arrayList.add(new RegexRule(str2));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.TIME)));
        this.negativeRules = arrayList;
        arrayList2.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        this.borderRules = arrayList2;
        this.codesRules = new ArrayList();
        this.codesRules.add(new AnonymousClass1(this, ""));
    }
}
