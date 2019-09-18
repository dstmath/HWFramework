package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_ES_ES extends PhoneNumberRule {
    public PhoneNumberRule_ES_ES(String country) {
        super(country);
        init();
    }

    public void init() {
        List<PhoneNumberRule.RegexRule> nRules = new ArrayList<>();
        List<PhoneNumberRule.RegexRule> bRules = new ArrayList<>();
        nRules.add(new PhoneNumberRule.RegexRule("[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|" + "FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS" + "(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(" + "FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS" + "))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        nRules.add(new PhoneNumberRule.RegexRule("(?<![\\p{L}])(ci|ref|plus(\\p{Blank}+de)?|i\\.?d|sn|tweets?|twitter|icq|qq)[\\p{Blank}:]*[0-9]{4,16}", 66));
        nRules.add(new PhoneNumberRule.RegexRule("[0-9.,]{4,16}(?<![.,])\\p{Blank}*(" + "minutos|segundos|kilobytes|megavatios|megabytes|kilojulios|toneladas|kilómetros|MB|m²|decilitros|hectopascales|MW|mililitros|oz|ha|Tb|pm|gal|Mb|grados|amperios|bytes|A|B|hPa|kilohercios|GB|M|J|K|picómetros|W|V|millas\\p{Blank}+náuticas|voltios|mg|dm|dl|ml|kHz|min|sem\\.|g|mm|d|b|miliamperios|c|onzas|Gb|centímetros|gramos|ms|a|l|m\\.|m|kilogramos|hectáreas|h|kilovatios|kWh|s|quilates|semanas|grados\\p{Blank}+Fahrenheit|megahercios|kW|ohmios|horas|kilovatios-hora|cm²|galones|centímetros\\p{Blank}+cuadrados|miligramos|libras|meses|kg|Ω|kb|días|julios|gigabytes|pintas|kilobits|km|milímetros|Hz|cm|vatios|GHz|gigahercios|gigabits|mA|°|lb|yardas|calorías|metros|litros|decímetros|kelvin|bits|ton|cal|MHz|TB|megabits|kilocalorías|°C|metros\\p{Blank}+cuadrados|grados\\p{Blank}+Celsius|°F|yd|milisegundos|terabits|kcal|años|kJ|pt|terabytes|kB|hercios" + "|" + "tweet|millones|mil" + ")(?![\\p{L}0-9])", 66));
        StringBuilder sb = new StringBuilder("([012]?\\d|3[01])(.|-|\\p{Blank}){0,2}(");
        sb.append("ene\\.|feb\\.|mar\\.|abr\\.|may\\.|jun\\.|jul\\.|ago\\.|sept\\.|oct\\.|nov\\.|dic\\.|enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre");
        sb.append(")(.|-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}{0,2})(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?");
        String nRegex_ShortMonthDate2 = sb.toString();
        ConstantsUtils utils = new ConstantsUtils();
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.AI)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.URL)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE1)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE2)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DP)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.EXP)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.FLOAT_1)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.EMAIL)));
        nRules.add(new PhoneNumberRule.RegexRule(nRegex_ShortMonthDate2));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.TIME)));
        this.negativeRules = nRules;
        bRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        this.borderRules = bRules;
        this.codesRules = new ArrayList();
        this.codesRules.add(new PhoneNumberRule.RegexRule(this, "") {
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
    }
}
