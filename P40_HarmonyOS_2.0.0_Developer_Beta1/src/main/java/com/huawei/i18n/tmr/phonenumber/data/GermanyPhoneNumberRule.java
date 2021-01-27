package com.huawei.i18n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule;
import huawei.android.provider.HwSettings;
import java.util.Optional;

public class GermanyPhoneNumberRule extends PhoneNumberRule {
    private final String negativeRules1 = "(?<!\\p{L})\\p{L}{1,2}\\p{Blank}*-\\p{Blank}*\\d{4,16}|\\d{4,16}\\p{Blank}*-\\p{Blank}*\\p{L}{1,2}(?!\\p{L})";
    private final String negativeRules10 = "\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|EUR|XAF|USD|Pfund|EURO?)";
    private final String negativeRules11 = "(?<!\\p{L})((Part\\.\\p{Blank}*No\\.|KdNr|Zufallszahl\\p{Blank}*-?|BLZ|Fehlercode|WKN|BMW|PLZ|Mobicool|Triebwagen|Bild|DIESEL|Pelikan|BIN|mal|Gr\\.|Drucksache|etwa|LEVIS|LEVI\\p{Blank}+S|Bosch|FC|ET|Bj\\.|TPH|Silit|PIRELLI\\p{Blank}+P|Artikelnummer|Ref|DL\\.|BMI|DWSG|Zum\\p{Blank}+Bild|POL\\p{Blank}*-\\p{Blank}*\\p{Alpha}{1,2}|CHE\\p{Blank}*-|K\\.Stand|PZN|Platzwit\\p{Blank}+loc|DB|ID|3ds|Boeing|BOSCH|Porsche|GM|Auftragsnummer|quittungsnummer|gechipt\\p{Blank}+mit\\p{Blank}+der\\p{Blank}+Nummer|Silber|Hot\\p{Blank}+Swap\\p{Blank}+Festplatte\\p{Blank}*-|GAV|RB|in|Kilometerstand|Artikelnummer|ICQ)\\p{Blank}{0,4}:?\\p{Blank}{0,4}|Einladungs\\p{Blank}*-\\p{Blank}*Code\\p{Blank}{0,2}\\p{Alpha}*)\\d{1,16}";
    private final String negativeRules12 = "\\d{1,16}\\p{Blank}*(Monate|Kt|Wo\\.|Terabits|Grad\\p{Blank}+Celsius|Megabits|MB|m²|Kilokalorien|MW|Quadratzentimeter|Grad\\p{Blank}+Fahrenheit|oz|ha|Tb|Kalorien|Short\\p{Blank}+Tons|Watt|Hertz|pm|gal|Mb|Mon\\.|Kilojoule|Megawatt|hPa|Sekunden|Kelvin|GB|Grad|Hektopascal|Sek\\.|mg|dm|dl|ml|kHz|mm|Bits|Kilobits|Gb|Zentimeter|ms|Volt|Pfund|Kilogramm|Liter|kWh|Gallonen|Unzen|Gramm|Hektar|tn|kW|Stunden|cm²|Joule|Std\\.|kg|Ω|Wochen|Milliampere|kb|Terabytes|Millisekunden|Milligramm|Megabytes|Milliliter|Tage|Gigahertz|sm|Dezimeter|Pikometer|km|Kilobytes|Hz|Ohm|cm|GHz|mA|°|Millimeter|lb|Yards|Meter|Gigabytes|Kilometer|Kilowatt|Karat|Seemeilen|Kilowattstunden|Tg\\.|cal|MHz|TB|Gigabits|Bytes|°C|Megahertz|°F|yd|kcal|Ampere|Quadratmeter|Deziliter|Pints|kJ|Min\\.|pt|Minuten|Kilohertz|Jahre|kB)(?!\\p{L})";
    private final String negativeRules13 = "\\d{1,16}\\p{Blank}*(TweetsWenn|mal|Einwohner|Pfanne|Ukrainer|Haushalte|Demonstranten|Abonnenten|Menschen|Tweets|Unterschriften|Flüchtlinge|Jahre|Fahrzeuge|Punkte|Abos|abos|Klicks|Obdachlosen|posts|Dosen|geschossenen|Einwohner|Trommel|Probleme|Notizbücher|Gewinne jetzt|BETRAGEN|No\\.|Stunden|mal|Stück|Bilder|Stunden)(?!\\p{L})";
    private final String negativeRules14 = "(?<!\\p{L})(Straße|Strasse|dorf|Allee|Rail|Lok|Ferkeltaxi)\\p{Blank}*\\d{1,16}";
    private final String negativeRules15 = "(?<!\\p{L})(auf|Rund|bis|mit)\\p{Blank}*(\\d{1,16}[-]?){1,4}\\d{1,16}";
    private final String negativeRules16 = "(?<!\\p{L})(gelb|schwarz|weiß|rot|grün)(\\d{1,16}[-]?){1,4}\\d{1,16}";
    private final String negativeRules2 = "(?<!\\d)([0-2]?\\d|3[01])/(0?\\d|1[0-2])\\p{Blank}*-\\p{Blank}*([0-2]?\\d|3[01])/(0?\\d|1[0-2])(?!\\d)";
    private final String negativeRules3 = "(?<!\\d)(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})\\p{Blank}*-\\p{Blank}*(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})(?!\\d)";
    private final String negativeRules4 = "(?<![0])(20[01][0-9]|19\\d{2})\\p{Blank}*[-/]\\p{Blank}*(0?\\d|1[0-2])(\\p{Blank}*[-/]\\p{Blank}*([0-2]?\\d|3[01]))?(?!\\d)";
    private final String negativeRules5 = "(?<!\\p{L})(Mar\\.|Jan|Feb|Mär|Apr|Mai|Jun|Jul|Aug|Sep|Okt|Nov|Dez|Jan\\.|Feb\\.|März|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.|M\\?r)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d))?";
    private final String negativeRules6 = "([012]?\\d|3[01])(\\.|-|\\p{Blank})*(Mar\\.|Jan|Feb|Mär|Apr|Mai|Jun|Jul|Aug|Sep|Okt|Nov|Dez|Jan\\.|Feb\\.|März|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.)(\\.|-|\\p{Blank})?(1[4-9]\\d{2}|20[01]\\d)(-\\d{2,16})?(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d))?";
    private final String negativeRules7 = "(\\d{1,16}\\p{Blank}{0,2}(\\.)\\p{Blank}{0,2}){1,4}\\d{1,16}";
    private final String negativeRules8 = "\\d{0,10}\\p{Blank}{0,2}\\(?\\d{2,10}\\)?\\p{Blank}{0,2}\\d{1,10}[xX*]{1,6}";
    private final String negativeRules9 = "\\d[0-9.,]*\\d\\p{Blank}{0,4}(‰|%|\\p{Sc}|FCFA|EUR|XAF|USD|Pfund|EURO?|Dollars?)|(%|\\p{Sc}|FCFA|EUR|XAF|USD|Preis:?|EURO?|Dollars?)\\p{Blank}{0,4}(\\d{1,16}[.,]?){1,4}\\d{1,16}";

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void init() {
        super.init();
        this.extraShortPattern = "(?<!\\d)19222|11833|11837|11834(?!\\d)";
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initNegativeRules() {
        initCommonNegativeRules();
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, this.utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})\\p{L}{1,2}\\p{Blank}*-\\p{Blank}*\\d{4,16}|\\d{4,16}\\p{Blank}*-\\p{Blank}*\\p{L}{1,2}(?!\\p{L})", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\d)([0-2]?\\d|3[01])/(0?\\d|1[0-2])\\p{Blank}*-\\p{Blank}*([0-2]?\\d|3[01])/(0?\\d|1[0-2])(?!\\d)"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\d)(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})\\p{Blank}*-\\p{Blank}*(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})(?!\\d)"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<![0])(20[01][0-9]|19\\d{2})\\p{Blank}*[-/]\\p{Blank}*(0?\\d|1[0-2])(\\p{Blank}*[-/]\\p{Blank}*([0-2]?\\d|3[01]))?(?!\\d)"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})(Mar\\.|Jan|Feb|Mär|Apr|Mai|Jun|Jul|Aug|Sep|Okt|Nov|Dez|Jan\\.|Feb\\.|März|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.|M\\?r)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d))?", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "([012]?\\d|3[01])(\\.|-|\\p{Blank})*(Mar\\.|Jan|Feb|Mär|Apr|Mai|Jun|Jul|Aug|Sep|Okt|Nov|Dez|Jan\\.|Feb\\.|März|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.)(\\.|-|\\p{Blank})?(1[4-9]\\d{2}|20[01]\\d)(-\\d{2,16})?(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d))?", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(\\d{1,16}\\p{Blank}{0,2}(\\.)\\p{Blank}{0,2}){1,4}\\d{1,16}"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d{0,10}\\p{Blank}{0,2}\\(?\\d{2,10}\\)?\\p{Blank}{0,2}\\d{1,10}[xX*]{1,6}", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d[0-9.,]*\\d\\p{Blank}{0,4}(‰|%|\\p{Sc}|FCFA|EUR|XAF|USD|Pfund|EURO?|Dollars?)|(%|\\p{Sc}|FCFA|EUR|XAF|USD|Preis:?|EURO?|Dollars?)\\p{Blank}{0,4}(\\d{1,16}[.,]?){1,4}\\d{1,16}", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|EUR|XAF|USD|Pfund|EURO?)", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})((Part\\.\\p{Blank}*No\\.|KdNr|Zufallszahl\\p{Blank}*-?|BLZ|Fehlercode|WKN|BMW|PLZ|Mobicool|Triebwagen|Bild|DIESEL|Pelikan|BIN|mal|Gr\\.|Drucksache|etwa|LEVIS|LEVI\\p{Blank}+S|Bosch|FC|ET|Bj\\.|TPH|Silit|PIRELLI\\p{Blank}+P|Artikelnummer|Ref|DL\\.|BMI|DWSG|Zum\\p{Blank}+Bild|POL\\p{Blank}*-\\p{Blank}*\\p{Alpha}{1,2}|CHE\\p{Blank}*-|K\\.Stand|PZN|Platzwit\\p{Blank}+loc|DB|ID|3ds|Boeing|BOSCH|Porsche|GM|Auftragsnummer|quittungsnummer|gechipt\\p{Blank}+mit\\p{Blank}+der\\p{Blank}+Nummer|Silber|Hot\\p{Blank}+Swap\\p{Blank}+Festplatte\\p{Blank}*-|GAV|RB|in|Kilometerstand|Artikelnummer|ICQ)\\p{Blank}{0,4}:?\\p{Blank}{0,4}|Einladungs\\p{Blank}*-\\p{Blank}*Code\\p{Blank}{0,2}\\p{Alpha}*)\\d{1,16}", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d{1,16}\\p{Blank}*(Monate|Kt|Wo\\.|Terabits|Grad\\p{Blank}+Celsius|Megabits|MB|m²|Kilokalorien|MW|Quadratzentimeter|Grad\\p{Blank}+Fahrenheit|oz|ha|Tb|Kalorien|Short\\p{Blank}+Tons|Watt|Hertz|pm|gal|Mb|Mon\\.|Kilojoule|Megawatt|hPa|Sekunden|Kelvin|GB|Grad|Hektopascal|Sek\\.|mg|dm|dl|ml|kHz|mm|Bits|Kilobits|Gb|Zentimeter|ms|Volt|Pfund|Kilogramm|Liter|kWh|Gallonen|Unzen|Gramm|Hektar|tn|kW|Stunden|cm²|Joule|Std\\.|kg|Ω|Wochen|Milliampere|kb|Terabytes|Millisekunden|Milligramm|Megabytes|Milliliter|Tage|Gigahertz|sm|Dezimeter|Pikometer|km|Kilobytes|Hz|Ohm|cm|GHz|mA|°|Millimeter|lb|Yards|Meter|Gigabytes|Kilometer|Kilowatt|Karat|Seemeilen|Kilowattstunden|Tg\\.|cal|MHz|TB|Gigabits|Bytes|°C|Megahertz|°F|yd|kcal|Ampere|Quadratmeter|Deziliter|Pints|kJ|Min\\.|pt|Minuten|Kilohertz|Jahre|kB)(?!\\p{L})", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d{1,16}\\p{Blank}*(TweetsWenn|mal|Einwohner|Pfanne|Ukrainer|Haushalte|Demonstranten|Abonnenten|Menschen|Tweets|Unterschriften|Flüchtlinge|Jahre|Fahrzeuge|Punkte|Abos|abos|Klicks|Obdachlosen|posts|Dosen|geschossenen|Einwohner|Trommel|Probleme|Notizbücher|Gewinne jetzt|BETRAGEN|No\\.|Stunden|mal|Stück|Bilder|Stunden)(?!\\p{L})", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})(Straße|Strasse|dorf|Allee|Rail|Lok|Ferkeltaxi)\\p{Blank}*\\d{1,16}", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})(auf|Rund|bis|mit)\\p{Blank}*(\\d{1,16}[-]?){1,4}\\d{1,16}", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})(gelb|schwarz|weiß|rot|grün)(\\d{1,16}[-]?){1,4}\\d{1,16}", 66));
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initCodeRules() {
        this.codesRules.add(getRegexRuleFromPrefixAndSuffix());
        this.codesRules.add(getRegexRuleFromRawString());
    }

    private PhoneNumberRule.RegexRule getRegexRuleFromRawString() {
        return new PhoneNumberRule.RegexRule(StorageManagerExt.INVALID_KEY_DESC) {
            /* class com.huawei.i18n.tmr.phonenumber.data.GermanyPhoneNumberRule.AnonymousClass1 */

            @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public Optional<PhoneNumberMatch> isValid(PhoneNumberMatch possibleNumber, String msg) {
                boolean isValid;
                String rawString = possibleNumber.rawString();
                boolean z = true;
                if (rawString.trim().startsWith(HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF) || rawString.trim().startsWith("(0")) {
                    if (rawString.replaceAll("[^0-9]+", StorageManagerExt.INVALID_KEY_DESC).length() <= 8) {
                        z = false;
                    }
                    isValid = z;
                } else {
                    if (!rawString.trim().startsWith("+") && !rawString.trim().startsWith("(+")) {
                        z = false;
                    }
                    isValid = z;
                }
                if (isValid) {
                    return Optional.of(possibleNumber);
                }
                return Optional.empty();
            }
        };
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initBoarderRules() {
        this.borderRules.add(new PhoneNumberRule.RegexRule("(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,})", 2, 9));
        this.borderRules.add(new PhoneNumberRule.RegexRule("[\\(\\[]?(\\d{1,2}\\p{Blank}*[-.]?\\p{Blank}*\\d{1,2}|\\d{2,4})", 2, 9));
        this.borderRules.add(new PhoneNumberRule.RegexRule("[\\(\\[]?\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}", 2, 9));
    }
}
