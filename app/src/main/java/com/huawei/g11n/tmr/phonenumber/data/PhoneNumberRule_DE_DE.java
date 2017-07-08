package com.huawei.g11n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule.RegexRule;
import huawei.android.provider.HwSettings.System;
import java.util.ArrayList;
import java.util.List;

public class PhoneNumberRule_DE_DE extends PhoneNumberRule {

    /* renamed from: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE.1 */
    class AnonymousClass1 extends RegexRule {
        AnonymousClass1(PhoneNumberRule phoneNumberRule, String str) {
            super(str);
        }

        public PhoneNumberMatch isValid(PhoneNumberMatch phoneNumberMatch, String str) {
            Object obj = null;
            String rawString = phoneNumberMatch.rawString();
            int i;
            if (rawString.trim().startsWith(System.FINGERSENSE_KNUCKLE_GESTURE_OFF) || rawString.trim().startsWith("(0")) {
                if (rawString.replaceAll("[^0-9]+", "").length() > 8) {
                    i = 1;
                }
            } else if (rawString.trim().startsWith("+") || rawString.trim().startsWith("(+")) {
                i = 1;
            }
            if (obj == null) {
                return null;
            }
            return phoneNumberMatch;
        }
    }

    /* renamed from: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE.2 */
    class AnonymousClass2 extends RegexRule {
        AnonymousClass2(PhoneNumberRule phoneNumberRule, String str) {
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

    public PhoneNumberRule_DE_DE(String str) {
        super(str);
        init();
    }

    public void init() {
        List arrayList = new ArrayList();
        ConstantsUtils constantsUtils = new ConstantsUtils();
        arrayList.add(new RegexRule("(?<!\\p{L})\\p{L}{1,2}\\p{Blank}*-\\p{Blank}*\\d{4,16}|\\d{4,16}\\p{Blank}*-\\p{Blank}*\\p{L}{1,2}(?!\\p{L})", 2));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.URL)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.YEAR_PERIOD)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE1)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DATE2)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EMAIL)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.TIME)));
        arrayList.add(new RegexRule("(?<!\\d)([0-2]?\\d|3[01])/(0?\\d|1[0-2])\\p{Blank}*-\\p{Blank}*([0-2]?\\d|3[01])/(0?\\d|1[0-2])(?!\\d)"));
        arrayList.add(new RegexRule("(?<!\\d)(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})\\p{Blank}*-\\p{Blank}*(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})(?!\\d)"));
        arrayList.add(new RegexRule("(?<![0])(20[01][0-9]|19\\d{2})\\p{Blank}*[-/]\\p{Blank}*(0?\\d|1[0-2])(\\p{Blank}*[-/]\\p{Blank}*([0-2]?\\d|3[01]))?(?!\\d)"));
        arrayList.add(new RegexRule("(?<!\\p{L})(Mar\\.|Jan|Feb|M\u00e4r|Apr|Mai|Jun|Jul|Aug|Sep|Okt|Nov|Dez|Jan\\.|Feb\\.|M\u00e4rz|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.|M\\?r)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d))?", 66));
        arrayList.add(new RegexRule("([012]?\\d|3[01])(\\.|-|\\p{Blank})*(Mar\\.|Jan|Feb|M\u00e4r|Apr|Mai|Jun|Jul|Aug|Sep|Okt|Nov|Dez|Jan\\.|Feb\\.|M\u00e4rz|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.)(\\.|-|\\p{Blank})?(1[4-9]\\d{2}|20[01]\\d)(-\\d{2,16})?(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d))?", 66));
        arrayList.add(new RegexRule("(\\d{1,16}\\p{Blank}{0,2}(\\.)\\p{Blank}{0,2}){1,4}\\d{1,16}"));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.DP)));
        arrayList.add(new RegexRule("\\d{0,10}\\p{Blank}{0,2}\\(?\\d{2,10}\\)?\\p{Blank}{0,2}\\d{1,10}[xX*]{1,6}", 2));
        arrayList.add(new RegexRule("(\\d{1,16}[.,]?){1,4}\\d{1,16}\\p{Blank}{0,4}(\u2030|%|\\p{Sc}|FCFA|EUR|XAF|USD|Pfund|EURO?|Dollars?)|(%|\\p{Sc}|FCFA|EUR|XAF|USD|Preis:?|EURO?|Dollars?)\\p{Blank}{0,4}(\\d{1,16}[.,]?){1,4}\\d{1,16}", 66));
        arrayList.add(new RegexRule("\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|EUR|XAF|USD|Pfund|EURO?)", 66));
        arrayList.add(new RegexRule("(?<!\\p{L})((Part\\.\\p{Blank}*No\\.|KdNr|Zufallszahl\\p{Blank}*-?|BLZ|Fehlercode|WKN|BMW|PLZ|Mobicool|Triebwagen|Bild|DIESEL|Pelikan|BIN|mal|Gr\\.|Drucksache|etwa|LEVIS|LEVI\\p{Blank}+S|Bosch|FC|ET|Bj\\.|TPH|Silit|PIRELLI\\p{Blank}+P|Artikelnummer|Ref|DL\\.|BMI|DWSG|Zum\\p{Blank}+Bild|POL\\p{Blank}*-\\p{Blank}*\\p{Alpha}{1,2}|CHE\\p{Blank}*-|K\\.Stand|PZN|Platzwit\\p{Blank}+loc|DB|ID|3ds|Boeing|BOSCH|Porsche|GM|Auftragsnummer|quittungsnummer|gechipt\\p{Blank}+mit\\p{Blank}+der\\p{Blank}+Nummer|Silber|Hot\\p{Blank}+Swap\\p{Blank}+Festplatte\\p{Blank}*-|GAV|RB|in|Kilometerstand|Artikelnummer|ICQ)\\p{Blank}{0,4}:?\\p{Blank}{0,4}|Einladungs\\p{Blank}*-\\p{Blank}*Code\\p{Blank}{0,2}\\p{Alpha}*)\\d{1,16}", 2));
        arrayList.add(new RegexRule("\\d{1,16}\\p{Blank}*(Monate|Kt|Wo\\.|Terabits|Grad\\p{Blank}+Celsius|Megabits|MB|m\u00b2|Kilokalorien|MW|Quadratzentimeter|Grad\\p{Blank}+Fahrenheit|oz|ha|Tb|Kalorien|Short\\p{Blank}+Tons|Watt|Hertz|pm|gal|Mb|Mon\\.|Kilojoule|Megawatt|hPa|Sekunden|Kelvin|GB|Grad|Hektopascal|Sek\\.|mg|dm|dl|ml|kHz|mm|Bits|Kilobits|Gb|Zentimeter|ms|Volt|Pfund|Kilogramm|Liter|kWh|Gallonen|Unzen|Gramm|Hektar|tn|kW|Stunden|cm\u00b2|Joule|Std\\.|kg|\u03a9|Wochen|Milliampere|kb|Terabytes|Millisekunden|Milligramm|Megabytes|Milliliter|Tage|Gigahertz|sm|Dezimeter|Pikometer|km|Kilobytes|Hz|Ohm|cm|GHz|mA|\u00b0|Millimeter|lb|Yards|Meter|Gigabytes|Kilometer|Kilowatt|Karat|Seemeilen|Kilowattstunden|Tg\\.|cal|MHz|TB|Gigabits|Bytes|\u00b0C|Megahertz|\u00b0F|yd|kcal|Ampere|Quadratmeter|Deziliter|Pints|kJ|Min\\.|pt|Minuten|Kilohertz|Jahre|kB)(?!\\p{L})", 2));
        arrayList.add(new RegexRule("\\d{1,16}\\p{Blank}*(TweetsWenn|mal|Einwohner|Pfanne|Ukrainer|Haushalte|Demonstranten|Abonnenten|Menschen|Tweets|Unterschriften|Fl\u00fcchtlinge|Jahre|Fahrzeuge|Punkte|Abos|abos|Klicks|Obdachlosen|posts|Dosen|geschossenen|Einwohner|Trommel|Probleme|Notizb\u00fccher|Gewinne jetzt|BETRAGEN|No\\.|Stunden|mal|St\u00fcck|Bilder|Stunden)(?!\\p{L})", 2));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.AI)));
        arrayList.add(new RegexRule(constantsUtils.getValues(ConstantsUtils.EXP)));
        arrayList.add(new RegexRule("(?<!\\p{L})(Stra\u00dfe|Strasse|dorf|Allee|Rail|Lok|Ferkeltaxi)\\d{1,16}", 66));
        arrayList.add(new RegexRule("(?<!\\p{L})(auf|Rund|bis|mit)(\\d{1,16}[-]?){1,4}\\d{1,16}", 2));
        arrayList.add(new RegexRule("(?<!\\p{L})(gelb|schwarz|wei\u00df|rot|gr\u00fcn)(\\d{1,16}[-]?){1,4}\\d{1,16}", 66));
        this.negativeRules = arrayList;
        arrayList = new ArrayList();
        arrayList.add(new RegexRule("(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,})", 2, 9));
        arrayList.add(new RegexRule("[\\(\\[]?(\\d{1,2}\\p{Blank}*[-.]?\\p{Blank}*\\d{1,2}|\\d{2,4})", 2, 9));
        arrayList.add(new RegexRule("[\\(\\[]?\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}", 2, 9));
        this.borderRules = arrayList;
        this.codesRules = new ArrayList();
        this.codesRules.add(new AnonymousClass1(this, ""));
        this.codesRules.add(new AnonymousClass2(this, ""));
    }
}
