package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import huawei.android.provider.HwSettings.System;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_FR_FR extends PhoneNumberRule {
    public PhoneNumberRule_FR_FR(String country) {
        super(country);
        init();
    }

    public void init() {
        String money = "FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS";
        List<RegexRule> nRules = new ArrayList();
        List<RegexRule> bRules = new ArrayList();
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
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.TIME)));
        nRules.add(new RegexRule("[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|" + money + "(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(" + money + "))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        nRules.add(new RegexRule("(?<![\\p{L}])(plus(\\p{Blank}+de)?|faire|n°(\\p{Blank}+d'entreprise)|fais|(ref|id|num|qq|ICQ|tweets?|twitter)[\\p{Blank}:]*|du|prime|dépassé\\p{Blank}les|yen|tapes)\\p{Blank}*[0-9\\p{P}]{4,16}", 66));
        nRules.add(new RegexRule("[0-9.,]{4,16}\\p{Blank}*(((de|i?[èe]me)\\p{Blank}+)?fois|" + "gallons|degrés\\p{Blank}+Celsius|pintes|kilojoules|kilohertz|calories|tonnes\\p{Blank}+courtes|m²|MW|gigaoctets|millisecondes|To|gigahertz|mégaoctets|oz|ha|secondes|bit|octets|Tb|centimètres\\p{Blank}+carrés|décilitres|pm|gal|Mb|kilogrammes|A|hPa|onces|degrés|carats|Mo|J|K|degrés\\p{Blank}+Fahrenheit|mètres|W|V|mégabits|kelvins|mg|dm|dl|hectopascals|térabits|ml|kHz|téraoctets|joules|min|sem\\.|g|mm|Gb|ms|kilooctets|l|m|j|nmi|h|yards|kWh|s|millimètres|octet|volts|kilocalories|kW|kilomètres|cm²|Go|grammes|décimètres|kg|Ω|kb|mois|ans|kilowatts|sh\\p{Blank}+tn|hertz|kilowattheures|watts|ko|kilobits|litres|jours|ct|km|pte|ampères|milligrammes|Hz|cm|GHz|gigabits|mA|°|lb|milliampères|mètres\\p{Blank}+carrés|millilitres|picomètres|ohms|hectares|centimètres|heures|bits|milles\\p{Blank}+marins|semaines|cal|MHz|°C|minutes|°F|yd|kcal|mégahertz|kJ|mégawatts|livres" + ")(?![\\p{L}0-9])", 66));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        nRules.add(new RegexRule("([012]?\\d|3[01])(-|\\p{Blank}){0,2}(janv\\.|févr\\.|mars|avr\\.|mai|juin|juil\\.|août|sept\\.|oct\\.|nov\\.|déc\\.|janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)(-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)\\p{Blank}{0,2}(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?", 66));
        this.negativeRules = nRules;
        bRules.add(new RegexRule(utils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        this.borderRules = bRules;
        this.codesRules = new ArrayList();
        this.codesRules.add(new RegexRule(this, "") {
            public PhoneNumberMatch isValid(PhoneNumberMatch possibleNumber, String msg) {
                String p = possibleNumber.rawString();
                if (p.trim().startsWith("+") || p.trim().startsWith(System.FINGERSENSE_KNUCKLE_GESTURE_OFF) || p.trim().startsWith("(+") || p.trim().startsWith("(0")) {
                    return possibleNumber;
                }
                return null;
            }
        });
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
