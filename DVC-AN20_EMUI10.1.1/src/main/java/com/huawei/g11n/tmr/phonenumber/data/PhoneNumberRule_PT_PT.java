package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_PT_PT extends PhoneNumberRule {
    public PhoneNumberRule_PT_PT(String country) {
        super(country);
        init();
    }

    @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule
    public void init() {
        this.extraShortPattern = "(?<!\\d)1414(?!\\d)";
        List<PhoneNumberRule.RegexRule> nRules = new ArrayList<>();
        ConstantsUtils utils = new ConstantsUtils();
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.AI)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.URL)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE1)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE2)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DP)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.EXP)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.FLOAT_1)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.EMAIL)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.TIME)));
        nRules.add(new PhoneNumberRule.RegexRule("[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|" + "FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?" + "(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(" + "FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?" + "))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        nRules.add(new PhoneNumberRule.RegexRule("\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(" + "FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?" + ")", 66));
        nRules.add(new PhoneNumberRule.RegexRule("(?<![\\p{L}])((ref|id|num|qq|ICQ|tweets?|twitter|(decreto|consulta)\\p{Blank}+nº|Série|nif|FM|contas)\\p{Blank}*:?)\\p{Blank}*[0-9]{4,16}", 66));
        nRules.add(new PhoneNumberRule.RegexRule("([012]?\\d|3[01])(.|-|\\p{Blank}){0,2}(" + "jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez|janeiro|fevereiro|março|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro" + ")(.|-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}{0,2})(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?", 2));
        nRules.add(new PhoneNumberRule.RegexRule("\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(" + "minutos|segundos|kilobytes|megabytes|toneladas|dias|kilohertz|byte|metros\\p{Blank}+quadrados|quilómetros|MB|decilitros|m²|MW|mililitros|gigahertz|pints|oz|ha|bit|hectopascais|Tb|pm|gal|Mb|milissegundos|bytes|quilocalorias|A|hPa|GB|jardas|J|K|picómetros|W|V|quilojoules|kelvins|mg|dm|dl|megawatts|ml|kHz|joules|min|quilogramas|g|sem\\.|mm|graus|miliamperes|miligramas|Gb|centímetros|ms|l|milhas\\p{Blank}+náuticas|m|nmi|h|kWh|s|quilates|semanas|volts|kW|horas|cm²|calorias|libras|meses|anos|kg|Ω|quilowatts|amperes|kb|quilowatts-hora|gigabytes|hertz|megahertz|watts|kilobits|onças|ct|km|milímetros|Hz|cm|GHz|gigabits|mA|graus\\p{Blank}+Celsius|°|centímetros\\p{Blank}+quadrados|lb|metros|ohms|litros|hectares|decímetros|bits|ton|cal|MHz|TB|megabits|°C|°F|yd|terabits|kcal|galões|kJ|pt|gramas|terabytes|graus\\p{Blank}+Fahrenheit|kB" + ")(?![\\p{L}0-9])", 2));
        nRules.add(new PhoneNumberRule.RegexRule("\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(vezes|(de\\p{Blank}+)?tweets?|twittar|rts|temporada|contas|capitulos|páginas|no\\p{Blank}+total\\p{Blank}+em\\p{Blank}+dinheiro|milhoes|mil)(?!\\p{L})", 2));
        this.negativeRules = nRules;
        List<PhoneNumberRule.RegexRule> bRules = new ArrayList<>();
        bRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        this.borderRules = bRules;
        this.codesRules = new ArrayList();
        this.codesRules.add(new PhoneNumberRule.RegexRule(this, BuildConfig.FLAVOR) {
            /* class com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_PT_PT.AnonymousClass1 */

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
    }
}
