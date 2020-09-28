package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule;
import com.huawei.uikit.effect.BuildConfig;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_DE_DE extends PhoneNumberRule {
    public PhoneNumberRule_DE_DE(String country) {
        super(country);
        init();
    }

    @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule
    public void init() {
        this.extraShortPattern = "(?<!\\d)19222|11833|11837|11834(?!\\d)";
        List<PhoneNumberRule.RegexRule> nRules = new ArrayList<>();
        ConstantsUtils utils = new ConstantsUtils();
        nRules.add(new PhoneNumberRule.RegexRule("(?<!\\p{L})\\p{L}{1,2}\\p{Blank}*-\\p{Blank}*\\d{4,16}|\\d{4,16}\\p{Blank}*-\\p{Blank}*\\p{L}{1,2}(?!\\p{L})", 2));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.URL)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE1)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DATE2)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.EMAIL)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.TIME)));
        nRules.add(new PhoneNumberRule.RegexRule("(?<!\\d)([0-2]?\\d|3[01])/(0?\\d|1[0-2])\\p{Blank}*-\\p{Blank}*([0-2]?\\d|3[01])/(0?\\d|1[0-2])(?!\\d)"));
        nRules.add(new PhoneNumberRule.RegexRule("(?<!\\d)(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})\\p{Blank}*-\\p{Blank}*(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})(?!\\d)"));
        nRules.add(new PhoneNumberRule.RegexRule("(?<![0])(20[01][0-9]|19\\d{2})\\p{Blank}*[-/]\\p{Blank}*(0?\\d|1[0-2])(\\p{Blank}*[-/]\\p{Blank}*([0-2]?\\d|3[01]))?(?!\\d)"));
        nRules.add(new PhoneNumberRule.RegexRule("(?<!\\p{L})(Mar\\.|Jan|Feb|Mär|Apr|Mai|Jun|Jul|Aug|Sep|Okt|Nov|Dez|Jan\\.|Feb\\.|März|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.|M\\?r)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d))?", 66));
        nRules.add(new PhoneNumberRule.RegexRule("([012]?\\d|3[01])(\\.|-|\\p{Blank})*(Mar\\.|Jan|Feb|Mär|Apr|Mai|Jun|Jul|Aug|Sep|Okt|Nov|Dez|Jan\\.|Feb\\.|März|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.)(\\.|-|\\p{Blank})?(1[4-9]\\d{2}|20[01]\\d)(-\\d{2,16})?(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d))?", 66));
        nRules.add(new PhoneNumberRule.RegexRule("(\\d{1,16}\\p{Blank}{0,2}(\\.)\\p{Blank}{0,2}){1,4}\\d{1,16}"));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.DP)));
        nRules.add(new PhoneNumberRule.RegexRule("\\d{0,10}\\p{Blank}{0,2}\\(?\\d{2,10}\\)?\\p{Blank}{0,2}\\d{1,10}[xX*]{1,6}", 2));
        nRules.add(new PhoneNumberRule.RegexRule("\\d[0-9.,]*\\d\\p{Blank}{0,4}(‰|%|\\p{Sc}|FCFA|EUR|XAF|USD|Pfund|EURO?|Dollars?)|(%|\\p{Sc}|FCFA|EUR|XAF|USD|Preis:?|EURO?|Dollars?)\\p{Blank}{0,4}(\\d{1,16}[.,]?){1,4}\\d{1,16}", 66));
        nRules.add(new PhoneNumberRule.RegexRule("\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|EUR|XAF|USD|Pfund|EURO?)", 66));
        nRules.add(new PhoneNumberRule.RegexRule("(?<!\\p{L})((Part\\.\\p{Blank}*No\\.|KdNr|Zufallszahl\\p{Blank}*-?|BLZ|Fehlercode|WKN|BMW|PLZ|Mobicool|Triebwagen|Bild|DIESEL|Pelikan|BIN|mal|Gr\\.|Drucksache|etwa|LEVIS|LEVI\\p{Blank}+S|Bosch|FC|ET|Bj\\.|TPH|Silit|PIRELLI\\p{Blank}+P|Artikelnummer|Ref|DL\\.|BMI|DWSG|Zum\\p{Blank}+Bild|POL\\p{Blank}*-\\p{Blank}*\\p{Alpha}{1,2}|CHE\\p{Blank}*-|K\\.Stand|PZN|Platzwit\\p{Blank}+loc|DB|ID|3ds|Boeing|BOSCH|Porsche|GM|Auftragsnummer|quittungsnummer|gechipt\\p{Blank}+mit\\p{Blank}+der\\p{Blank}+Nummer|Silber|Hot\\p{Blank}+Swap\\p{Blank}+Festplatte\\p{Blank}*-|GAV|RB|in|Kilometerstand|Artikelnummer|ICQ)\\p{Blank}{0,4}:?\\p{Blank}{0,4}|Einladungs\\p{Blank}*-\\p{Blank}*Code\\p{Blank}{0,2}\\p{Alpha}*)\\d{1,16}", 2));
        nRules.add(new PhoneNumberRule.RegexRule("\\d{1,16}\\p{Blank}*(Monate|Kt|Wo\\.|Terabits|Grad\\p{Blank}+Celsius|Megabits|MB|m²|Kilokalorien|MW|Quadratzentimeter|Grad\\p{Blank}+Fahrenheit|oz|ha|Tb|Kalorien|Short\\p{Blank}+Tons|Watt|Hertz|pm|gal|Mb|Mon\\.|Kilojoule|Megawatt|hPa|Sekunden|Kelvin|GB|Grad|Hektopascal|Sek\\.|mg|dm|dl|ml|kHz|mm|Bits|Kilobits|Gb|Zentimeter|ms|Volt|Pfund|Kilogramm|Liter|kWh|Gallonen|Unzen|Gramm|Hektar|tn|kW|Stunden|cm²|Joule|Std\\.|kg|Ω|Wochen|Milliampere|kb|Terabytes|Millisekunden|Milligramm|Megabytes|Milliliter|Tage|Gigahertz|sm|Dezimeter|Pikometer|km|Kilobytes|Hz|Ohm|cm|GHz|mA|°|Millimeter|lb|Yards|Meter|Gigabytes|Kilometer|Kilowatt|Karat|Seemeilen|Kilowattstunden|Tg\\.|cal|MHz|TB|Gigabits|Bytes|°C|Megahertz|°F|yd|kcal|Ampere|Quadratmeter|Deziliter|Pints|kJ|Min\\.|pt|Minuten|Kilohertz|Jahre|kB)(?!\\p{L})", 2));
        nRules.add(new PhoneNumberRule.RegexRule("\\d{1,16}\\p{Blank}*(TweetsWenn|mal|Einwohner|Pfanne|Ukrainer|Haushalte|Demonstranten|Abonnenten|Menschen|Tweets|Unterschriften|Flüchtlinge|Jahre|Fahrzeuge|Punkte|Abos|abos|Klicks|Obdachlosen|posts|Dosen|geschossenen|Einwohner|Trommel|Probleme|Notizbücher|Gewinne jetzt|BETRAGEN|No\\.|Stunden|mal|Stück|Bilder|Stunden)(?!\\p{L})", 2));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.AI)));
        nRules.add(new PhoneNumberRule.RegexRule(utils.getValues(ConstantsUtils.EXP)));
        nRules.add(new PhoneNumberRule.RegexRule("(?<!\\p{L})(Straße|Strasse|dorf|Allee|Rail|Lok|Ferkeltaxi)\\p{Blank}*\\d{1,16}", 66));
        nRules.add(new PhoneNumberRule.RegexRule("(?<!\\p{L})(auf|Rund|bis|mit)\\p{Blank}*(\\d{1,16}[-]?){1,4}\\d{1,16}", 2));
        nRules.add(new PhoneNumberRule.RegexRule("(?<!\\p{L})(gelb|schwarz|weiß|rot|grün)(\\d{1,16}[-]?){1,4}\\d{1,16}", 66));
        this.negativeRules = nRules;
        List<PhoneNumberRule.RegexRule> bRules = new ArrayList<>();
        bRules.add(new PhoneNumberRule.RegexRule("(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,})", 2, 9));
        bRules.add(new PhoneNumberRule.RegexRule("[\\(\\[]?(\\d{1,2}\\p{Blank}*[-.]?\\p{Blank}*\\d{1,2}|\\d{2,4})", 2, 9));
        bRules.add(new PhoneNumberRule.RegexRule("[\\(\\[]?\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}", 2, 9));
        this.borderRules = bRules;
        this.codesRules = new ArrayList();
        this.codesRules.add(new PhoneNumberRule.RegexRule(this, BuildConfig.FLAVOR) {
            /* class com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE.AnonymousClass1 */

            @Override // com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public PhoneNumberMatch isValid(PhoneNumberMatch possibleNumber, String msg) {
                boolean isvalid;
                String p = possibleNumber.rawString();
                if (p.trim().startsWith("0") || p.trim().startsWith("(0")) {
                    if (p.replaceAll("[^0-9]+", BuildConfig.FLAVOR).length() > 8) {
                        isvalid = true;
                    } else {
                        isvalid = false;
                    }
                } else if (p.trim().startsWith("+") || p.trim().startsWith("(+")) {
                    isvalid = true;
                } else {
                    isvalid = false;
                }
                if (isvalid) {
                    return possibleNumber;
                }
                return null;
            }
        });
        this.codesRules.add(new PhoneNumberRule.RegexRule(this, BuildConfig.FLAVOR) {
            /* class com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE.AnonymousClass2 */

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
