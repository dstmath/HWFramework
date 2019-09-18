package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule;
import huawei.android.provider.HwSettings;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_FR_FR extends PhoneNumberRule {
    public PhoneNumberRule_FR_FR(String country) {
        super(country);
        init();
    }

    public void init() {
        List<PhoneNumberRule.RegexRule> nRules = new ArrayList<>();
        List<PhoneNumberRule.RegexRule> bRules = new ArrayList<>();
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
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.TIME)));
        nRules.add(new PhoneNumberRule.RegexRule("[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|" + "FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS" + "(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(" + "FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS" + "))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        nRules.add(new PhoneNumberRule.RegexRule("(?<![\\p{L}])(plus(\\p{Blank}+de)?|faire|n°(\\p{Blank}+d'entreprise)|fais|(ref|id|num|qq|ICQ|tweets?|twitter)[\\p{Blank}:]*|du|prime|dépassé\\p{Blank}les|yen|tapes)\\p{Blank}*[0-9\\p{P}]{4,16}", 66));
        StringBuilder sb = new StringBuilder("[0-9.,]{4,16}\\p{Blank}*(((de|i?[èe]me)\\p{Blank}+)?fois|");
        sb.append("gallons|degrés\\p{Blank}+Celsius|pintes|kilojoules|kilohertz|calories|tonnes\\p{Blank}+courtes|m²|MW|gigaoctets|millisecondes|To|gigahertz|mégaoctets|oz|ha|secondes|bit|octets|Tb|centimètres\\p{Blank}+carrés|décilitres|pm|gal|Mb|kilogrammes|A|hPa|onces|degrés|carats|Mo|J|K|degrés\\p{Blank}+Fahrenheit|mètres|W|V|mégabits|kelvins|mg|dm|dl|hectopascals|térabits|ml|kHz|téraoctets|joules|min|sem\\.|g|mm|Gb|ms|kilooctets|l|m|j|nmi|h|yards|kWh|s|millimètres|octet|volts|kilocalories|kW|kilomètres|cm²|Go|grammes|décimètres|kg|Ω|kb|mois|ans|kilowatts|sh\\p{Blank}+tn|hertz|kilowattheures|watts|ko|kilobits|litres|jours|ct|km|pte|ampères|milligrammes|Hz|cm|GHz|gigabits|mA|°|lb|milliampères|mètres\\p{Blank}+carrés|millilitres|picomètres|ohms|hectares|centimètres|heures|bits|milles\\p{Blank}+marins|semaines|cal|MHz|°C|minutes|°F|yd|kcal|mégahertz|kJ|mégawatts|livres");
        sb.append(")(?![\\p{L}0-9])");
        nRules.add(new PhoneNumberRule.RegexRule(sb.toString(), 66));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        nRules.add(new PhoneNumberRule.RegexRule("([012]?\\d|3[01])(-|\\p{Blank}){0,2}(janv\\.|févr\\.|mars|avr\\.|mai|juin|juil\\.|août|sept\\.|oct\\.|nov\\.|déc\\.|janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)(-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)\\p{Blank}{0,2}(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?", 66));
        this.negativeRules = nRules;
        bRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        this.borderRules = bRules;
        this.codesRules = new ArrayList();
        this.codesRules.add(new PhoneNumberRule.RegexRule(this, "") {
            public PhoneNumberMatch isValid(PhoneNumberMatch possibleNumber, String msg) {
                String p = possibleNumber.rawString();
                if (p.trim().startsWith("+") || p.trim().startsWith(HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF) || p.trim().startsWith("(+") || p.trim().startsWith("(0")) {
                    return possibleNumber;
                }
                return null;
            }
        });
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
