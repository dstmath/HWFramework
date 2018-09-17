package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_ES_ES extends PhoneNumberRule {
    public PhoneNumberRule_ES_ES(String country) {
        super(country);
        init();
    }

    public void init() {
        String money = "FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS";
        List<RegexRule> nRules = new ArrayList();
        List<RegexRule> bRules = new ArrayList();
        nRules.add(new RegexRule("[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|" + money + "(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(" + money + "))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        nRules.add(new RegexRule("(?<![\\p{L}])(ci|ref|plus(\\p{Blank}+de)?|i\\.?d|sn|tweets?|twitter|icq|qq)[\\p{Blank}:]*[0-9]{4,16}", 66));
        nRules.add(new RegexRule("[0-9.,]{4,16}(?<![.,])\\p{Blank}*(" + "minutos|segundos|kilobytes|megavatios|megabytes|kilojulios|toneladas|kilómetros|MB|m²|decilitros|hectopascales|MW|mililitros|oz|ha|Tb|pm|gal|Mb|grados|amperios|bytes|A|B|hPa|kilohercios|GB|M|J|K|picómetros|W|V|millas\\p{Blank}+náuticas|voltios|mg|dm|dl|ml|kHz|min|sem\\.|g|mm|d|b|miliamperios|c|onzas|Gb|centímetros|gramos|ms|a|l|m\\.|m|kilogramos|hectáreas|h|kilovatios|kWh|s|quilates|semanas|grados\\p{Blank}+Fahrenheit|megahercios|kW|ohmios|horas|kilovatios-hora|cm²|galones|centímetros\\p{Blank}+cuadrados|miligramos|libras|meses|kg|Ω|kb|días|julios|gigabytes|pintas|kilobits|km|milímetros|Hz|cm|vatios|GHz|gigahercios|gigabits|mA|°|lb|yardas|calorías|metros|litros|decímetros|kelvin|bits|ton|cal|MHz|TB|megabits|kilocalorías|°C|metros\\p{Blank}+cuadrados|grados\\p{Blank}+Celsius|°F|yd|milisegundos|terabits|kcal|años|kJ|pt|terabytes|kB|hercios" + "|" + "tweet|millones|mil" + ")(?![\\p{L}0-9])", 66));
        String nRegex_ShortMonthDate2 = "([012]?\\d|3[01])(.|-|\\p{Blank}){0,2}(" + "ene\\.|feb\\.|mar\\.|abr\\.|may\\.|jun\\.|jul\\.|ago\\.|sept\\.|oct\\.|nov\\.|dic\\.|enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre" + ")(.|-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}{0,2})(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?";
        ConstantsUtils utils = new ConstantsUtils();
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.AI)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.URL)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.DATE)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.DATE1)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.DATE2)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.DP)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.EXP)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.FLOAT_1)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.EMAIL)));
        nRules.add(new RegexRule(nRegex_ShortMonthDate2));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.TIME)));
        this.negativeRules = nRules;
        bRules.add(new RegexRule(utils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        this.borderRules = bRules;
        this.codesRules = new ArrayList();
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
    }
}
