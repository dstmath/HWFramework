package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_IT_IT extends PhoneNumberRule {

    /* renamed from: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_IT_IT.1 */
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

    public PhoneNumberRule_IT_IT(String str) {
        super(str);
        init();
    }

    public void init() {
        List arrayList = new ArrayList();
        ConstantsUtils constantsUtils = new ConstantsUtils();
        arrayList.add(new RegexRule("(?<!\\p{L})\\p{L}{1,2}-(\\d{1,16}[-]?){0,4}\\d{1,16}|(\\d{1,16}[-]?){0,4}\\d{1,16}-\\p{L}{1,2}(?!\\p{L})", 2));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.URL)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.YEAR_PERIOD)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE1)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE2)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EMAIL)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.TIME)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DP)));
        arrayList.add(new RegexRule("(?<!\\d)([0-2]?\\d|3[01])/(0?\\d|1[0-2])\\p{Blank}*-\\p{Blank}*([0-2]?\\d|3[01])/(0?\\d|1[0-2])(?!\\d)"));
        arrayList.add(new RegexRule("(?<!\\d)(0?\\d|1[0-2])/(20[01][0-9]|19\\d{2})\\p{Blank}*-\\p{Blank}*(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})(?!\\d)"));
        arrayList.add(new RegexRule("(?<!(\\d|[-\\p{Blank}]\\d))([0-2]?\\d|3[01])[\\p{Blank}-]+(0?\\d|1[0-2])[\\p{Blank}-]+201\\d(?!(\\d|[-\\p{Blank}]\\d))"));
        arrayList.add(new RegexRule("(?<!\\p{L})(gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?", 2));
        arrayList.add(new RegexRule("([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?", 2));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.FLOAT_1)));
        arrayList.add(new RegexRule("(\\d{1,16}[.,]?){1,4}\\d{1,16}\\p{Blank}{0,4}(\u2030|%|\\p{Sc}|FCFA|EUR|XAF|USD|Pfund|Euro?)|(%|\\p{Sc}|FCFA|EUR|XAF|USD|Prezzo:?|Euro?)\\p{Blank}{0,4}(\\d{1,16}[.,]?){1,4}\\d{1,16}", 2));
        arrayList.add(new RegexRule("(?<!\\p{L})(almeno|riduzione|A\\p{Blank}+proposito\\p{Blank}+di|alle|Nuovi|da|Pi\u00f9\\p{Blank}+di|altro|oltre|meno|fino\\p{Blank}+a|aumentati\\p{Blank}+a|minimo|Siamo\\p{Blank}+arrivati\\p{Blank}+a|P\\.IVA|Riferimento\\p{Blank}+offerta|intercity|Weibo|episodi|moltiplicati per|Prezzo|N\u00b0)\\p{Blank}{0,4}[:-]?\\p{Blank}{0,4}(\\d{1,16}[-]?){1,4}\\d{1,16}", 66));
        arrayList.add(new RegexRule("(\\d{1,16}[\\p{Blank}.,-]?){1,4}\\d{1,16}\\p{Blank}{0,4}(litri|chilogrammi|chilometri|galloni|kilohertz|byte|ettari|MB|m\u00b2|MW|once|kilobit|gigahertz|millimetri|grammi|oz|ohm|ha|millilitri|bit|Tb|ettopascal|kilojoule|pm|gal|Mb|pinte|miglia\\p{Blank}+nautiche|carati|hPa|Kelvin|GB|ore|megawatt|kilobyte|gradi|mg|dm|dl|ml|kHz|min|mm|Gb|ms|nmi|gigabit|kWh|secondi|megabit|tn|kW|kilowatt|decilitri|chilowattora|millisecondi|gradi\\p{Blank}+Fahrenheit|giorni|watt|decimetri|cm\u00b2|calorie|tonnellate|libbre|kg|terabit|\u03a9|kb|mesi|megabyte|hertz|centimetri\\p{Blank}+quadrati|kt|megahertz|gradi\\p{Blank}+Celsius|km|chilocalorie|centimetri|iarde|metri\\p{Blank}+quadrati|Hz|cm|terabyte|GHz|mA|\u00b0|lb|settimane|picometri|volt|gigabyte|joule|Cal|cal|MHz|minuti|TB|\u00b0C|\u00b0F|milligrammi|yd|kcal|metri|anni|milliampere|kJ|ampere|pt|kB)(?!\\p{L})", 2));
        arrayList.add(new RegexRule("(\\d{1,16}[-]?){1,4}\\d{1,16}\\p{Blank}*(punti|di|blocco|donne|Utenza|foto|intercity|Weibo|volte|volta|esimo|pensioni|figli|cani|Offerte|capelli|accounts|ragazzi|messaggi|amici|cose|tweet|tweets|pagine|contribuenti|notifiche|ragazze|medaglie|esima|screenshot|robe|eseguito|moltiplicati\\p{Blank}+per|bambini|anni|ore|minuti|secondi|-\\p{Blank}*Twitter|giorni)(?!\\p{L})", 2));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.AI)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EXP)));
        arrayList.add(new RegexRule("(?<!\\p{L})(ICQ|Codice|(Codice\\p{Blank}*\\p{Alpha}{0,2}\\d{0,3})|Rif\\.|art\\.|numero\\p{Blank}+cliente|Id\\p{Blank}+FASTWEB|cod|Pin|id|codice\\p{Blank}+fiscale|Rolex\\p{Blank}+Datejust|pt\\.|ISBN|legge\\p{Blank}+numero)\\p{Blank}*:?\\p{Blank}*(\\d{1,16}[.,]){0,4}\\d{1,16}", 2));
        arrayList.add(new RegexRule("\\d{0,10}\\p{Blank}{0,2}\\(?\\d{2,10}\\)?\\p{Blank}{0,2}\\d{1,10}[xX*]{1,10}", 2));
        arrayList.add(new RegexRule("\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|EUR|XAF|USD|Pfund|EURO?|eur)", 2));
        arrayList.add(new RegexRule("(?<!\\p{L})uuid\\p{Blank}*:?\\p{Blank}*[\\p{Alpha}\\d-]+", 2));
        this.negativeRules = arrayList;
        arrayList = new ArrayList();
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        arrayList.add(new RegexRule("[\\(\\[]?(\\d{1,2}\\p{Blank}*-?\\p{Blank}*\\d{1,2}|\\d{2,5}|[1-9]{13,})", 2, 9));
        arrayList.add(new RegexRule("[\\(\\[]?\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}", 2, 9));
        this.borderRules = arrayList;
        this.codesRules = new ArrayList();
        this.codesRules.add(new AnonymousClass1(this, ""));
    }
}
