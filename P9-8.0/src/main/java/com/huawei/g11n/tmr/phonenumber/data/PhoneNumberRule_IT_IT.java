package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_IT_IT extends PhoneNumberRule {
    public PhoneNumberRule_IT_IT(String country) {
        super(country);
        init();
    }

    public void init() {
        List<RegexRule> nRules = new ArrayList();
        ConstantsUtils utils = new ConstantsUtils();
        nRules.add(new RegexRule("(?<!\\p{L})\\p{L}{1,2}-(\\d{1,16}[-]?){0,4}\\d{1,16}|(\\d{1,16}[-]?){0,4}\\d{1,16}-\\p{L}{1,2}(?!\\p{L})", 2));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.URL)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.DATE)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.DATE1)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.DATE2)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.EMAIL)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.TIME)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.DP)));
        nRules.add(new RegexRule("(?<!\\d)([0-2]?\\d|3[01])/(0?\\d|1[0-2])\\p{Blank}*-\\p{Blank}*([0-2]?\\d|3[01])/(0?\\d|1[0-2])(?!\\d)"));
        nRules.add(new RegexRule("(?<!\\d)(0?\\d|1[0-2])/(20[01][0-9]|19\\d{2})\\p{Blank}*-\\p{Blank}*(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})(?!\\d)"));
        nRules.add(new RegexRule("(?<!(\\d|[-\\p{Blank}]\\d))([0-2]?\\d|3[01])[\\p{Blank}-]+(0?\\d|1[0-2])[\\p{Blank}-]+201\\d(?!(\\d|[-\\p{Blank}]\\d))"));
        nRules.add(new RegexRule("(?<!\\p{L})(gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?", 2));
        nRules.add(new RegexRule("([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?", 2));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.FLOAT_1)));
        nRules.add(new RegexRule("(\\d{1,16}[.,]?){1,4}\\d{1,16}\\p{Blank}{0,4}(‰|%|\\p{Sc}|FCFA|EUR|XAF|USD|Pfund|Euro?)|(%|\\p{Sc}|FCFA|EUR|XAF|USD|Prezzo:?|Euro?)\\p{Blank}{0,4}(\\d{1,16}[.,]?){1,4}\\d{1,16}", 2));
        nRules.add(new RegexRule("(?<!\\p{L})(almeno|riduzione|A\\p{Blank}+proposito\\p{Blank}+di|alle|Nuovi|da|Più\\p{Blank}+di|altro|oltre|meno|fino\\p{Blank}+a|aumentati\\p{Blank}+a|minimo|Siamo\\p{Blank}+arrivati\\p{Blank}+a|P\\.IVA|Riferimento\\p{Blank}+offerta|intercity|Weibo|episodi|moltiplicati per|Prezzo|N°)\\p{Blank}{0,4}[:-]?\\p{Blank}{0,4}(\\d{1,16}[-]?){1,4}\\d{1,16}", 66));
        nRules.add(new RegexRule("(\\d{1,16}[\\p{Blank}.,-]?){1,4}\\d{1,16}\\p{Blank}{0,4}(litri|chilogrammi|chilometri|galloni|kilohertz|byte|ettari|MB|m²|MW|once|kilobit|gigahertz|millimetri|grammi|oz|ohm|ha|millilitri|bit|Tb|ettopascal|kilojoule|pm|gal|Mb|pinte|miglia\\p{Blank}+nautiche|carati|hPa|Kelvin|GB|ore|megawatt|kilobyte|gradi|mg|dm|dl|ml|kHz|min|mm|Gb|ms|nmi|gigabit|kWh|secondi|megabit|tn|kW|kilowatt|decilitri|chilowattora|millisecondi|gradi\\p{Blank}+Fahrenheit|giorni|watt|decimetri|cm²|calorie|tonnellate|libbre|kg|terabit|Ω|kb|mesi|megabyte|hertz|centimetri\\p{Blank}+quadrati|kt|megahertz|gradi\\p{Blank}+Celsius|km|chilocalorie|centimetri|iarde|metri\\p{Blank}+quadrati|Hz|cm|terabyte|GHz|mA|°|lb|settimane|picometri|volt|gigabyte|joule|Cal|cal|MHz|minuti|TB|°C|°F|milligrammi|yd|kcal|metri|anni|milliampere|kJ|ampere|pt|kB)(?!\\p{L})", 2));
        nRules.add(new RegexRule("(\\d{1,16}[-]?){1,4}\\d{1,16}\\p{Blank}*(punti|di|blocco|donne|Utenza|foto|intercity|Weibo|volte|volta|esimo|pensioni|figli|cani|Offerte|capelli|accounts|ragazzi|messaggi|amici|cose|tweet|tweets|pagine|contribuenti|notifiche|ragazze|medaglie|esima|screenshot|robe|eseguito|moltiplicati\\p{Blank}+per|bambini|anni|ore|minuti|secondi|-\\p{Blank}*Twitter|giorni)(?!\\p{L})", 2));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.AI)));
        nRules.add(new RegexRule(utils.getValues(ConstantsUtils.EXP)));
        nRules.add(new RegexRule("(?<!\\p{L})(ICQ|Codice|(Codice\\p{Blank}*\\p{Alpha}{0,2}\\d{0,3})|Rif\\.|art\\.|numero\\p{Blank}+cliente|Id\\p{Blank}+FASTWEB|cod|Pin|id|codice\\p{Blank}+fiscale|Rolex\\p{Blank}+Datejust|pt\\.|ISBN|legge\\p{Blank}+numero)\\p{Blank}*:?\\p{Blank}*(\\d{1,16}[.,]){0,4}\\d{1,16}", 2));
        nRules.add(new RegexRule("\\d{0,10}\\p{Blank}{0,2}\\(?\\d{2,10}\\)?\\p{Blank}{0,2}\\d{1,10}[xX*]{1,10}", 2));
        nRules.add(new RegexRule("\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|EUR|XAF|USD|Pfund|EURO?|eur)", 2));
        nRules.add(new RegexRule("(?<!\\p{L})uuid\\p{Blank}*:?\\p{Blank}*[\\p{Alpha}\\d-]+", 2));
        this.negativeRules = nRules;
        List<RegexRule> bRules = new ArrayList();
        bRules.add(new RegexRule(utils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        bRules.add(new RegexRule("[\\(\\[]?(\\d{1,2}\\p{Blank}*-?\\p{Blank}*\\d{1,2}|\\d{2,5}|[1-9]{13,})", 2, 9));
        bRules.add(new RegexRule("[\\(\\[]?\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}", 2, 9));
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
