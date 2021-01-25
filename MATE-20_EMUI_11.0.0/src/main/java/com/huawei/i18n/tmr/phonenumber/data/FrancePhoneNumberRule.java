package com.huawei.i18n.tmr.phonenumber.data;

import com.android.i18n.phonenumbers.PhoneNumberMatch;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule;
import java.util.Optional;

public class FrancePhoneNumberRule extends PhoneNumberRule {
    private final String money = "FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS";
    private final String negativeRule1 = "[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS))\\p{Blank}*[0-9.,]+(?<![.,])";
    private final String negativeRule2 = "(?<![\\p{L}])(plus(\\p{Blank}+de)?|faire|n°(\\p{Blank}+d'entreprise)|fais|(ref|id|num|qq|ICQ|tweets?|twitter)[\\p{Blank}:]*|du|prime|dépassé\\p{Blank}les|yen|tapes)\\p{Blank}*[0-9\\p{P}]{4,16}";
    private final String negativeRule3 = "[0-9.,]{4,16}\\p{Blank}*(((de|i?[èe]me)\\p{Blank}+)?fois|gallons|degrés\\p{Blank}+Celsius|pintes|kilojoules|kilohertz|calories|tonnes\\p{Blank}+courtes|m²|MW|gigaoctets|millisecondes|To|gigahertz|mégaoctets|oz|ha|secondes|bit|octets|Tb|centimètres\\p{Blank}+carrés|décilitres|pm|gal|Mb|kilogrammes|A|hPa|onces|degrés|carats|Mo|J|K|degrés\\p{Blank}+Fahrenheit|mètres|W|V|mégabits|kelvins|mg|dm|dl|hectopascals|térabits|ml|kHz|téraoctets|joules|min|sem\\.|g|mm|Gb|ms|kilooctets|l|m|j|nmi|h|yards|kWh|s|millimètres|octet|volts|kilocalories|kW|kilomètres|cm²|Go|grammes|décimètres|kg|Ω|kb|mois|ans|kilowatts|sh\\p{Blank}+tn|hertz|kilowattheures|watts|ko|kilobits|litres|jours|ct|km|pte|ampères|milligrammes|Hz|cm|GHz|gigabits|mA|°|lb|milliampères|mètres\\p{Blank}+carrés|millilitres|picomètres|ohms|hectares|centimètres|heures|bits|milles\\p{Blank}+marins|semaines|cal|MHz|°C|minutes|°F|yd|kcal|mégahertz|kJ|mégawatts|livres)(?![\\p{L}0-9])";
    private final String negativeRule4 = "([012]?\\d|3[01])(-|\\p{Blank}){0,2}(janv\\.|févr\\.|mars|avr\\.|mai|juin|juil\\.|août|sept\\.|oct\\.|nov\\.|déc\\.|janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)(-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)\\p{Blank}{0,2}(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?";
    private final String units = "gallons|degrés\\p{Blank}+Celsius|pintes|kilojoules|kilohertz|calories|tonnes\\p{Blank}+courtes|m²|MW|gigaoctets|millisecondes|To|gigahertz|mégaoctets|oz|ha|secondes|bit|octets|Tb|centimètres\\p{Blank}+carrés|décilitres|pm|gal|Mb|kilogrammes|A|hPa|onces|degrés|carats|Mo|J|K|degrés\\p{Blank}+Fahrenheit|mètres|W|V|mégabits|kelvins|mg|dm|dl|hectopascals|térabits|ml|kHz|téraoctets|joules|min|sem\\.|g|mm|Gb|ms|kilooctets|l|m|j|nmi|h|yards|kWh|s|millimètres|octet|volts|kilocalories|kW|kilomètres|cm²|Go|grammes|décimètres|kg|Ω|kb|mois|ans|kilowatts|sh\\p{Blank}+tn|hertz|kilowattheures|watts|ko|kilobits|litres|jours|ct|km|pte|ampères|milligrammes|Hz|cm|GHz|gigabits|mA|°|lb|milliampères|mètres\\p{Blank}+carrés|millilitres|picomètres|ohms|hectares|centimètres|heures|bits|milles\\p{Blank}+marins|semaines|cal|MHz|°C|minutes|°F|yd|kcal|mégahertz|kJ|mégawatts|livres";

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initNegativeRules() {
        initCommonNegativeRules();
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, this.utils.getValues(ConstantsUtils.FLOAT_1)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, this.utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<![\\p{L}])(plus(\\p{Blank}+de)?|faire|n°(\\p{Blank}+d'entreprise)|fais|(ref|id|num|qq|ICQ|tweets?|twitter)[\\p{Blank}:]*|du|prime|dépassé\\p{Blank}les|yen|tapes)\\p{Blank}*[0-9\\p{P}]{4,16}", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "[0-9.,]{4,16}\\p{Blank}*(((de|i?[èe]me)\\p{Blank}+)?fois|gallons|degrés\\p{Blank}+Celsius|pintes|kilojoules|kilohertz|calories|tonnes\\p{Blank}+courtes|m²|MW|gigaoctets|millisecondes|To|gigahertz|mégaoctets|oz|ha|secondes|bit|octets|Tb|centimètres\\p{Blank}+carrés|décilitres|pm|gal|Mb|kilogrammes|A|hPa|onces|degrés|carats|Mo|J|K|degrés\\p{Blank}+Fahrenheit|mètres|W|V|mégabits|kelvins|mg|dm|dl|hectopascals|térabits|ml|kHz|téraoctets|joules|min|sem\\.|g|mm|Gb|ms|kilooctets|l|m|j|nmi|h|yards|kWh|s|millimètres|octet|volts|kilocalories|kW|kilomètres|cm²|Go|grammes|décimètres|kg|Ω|kb|mois|ans|kilowatts|sh\\p{Blank}+tn|hertz|kilowattheures|watts|ko|kilobits|litres|jours|ct|km|pte|ampères|milligrammes|Hz|cm|GHz|gigabits|mA|°|lb|milliampères|mètres\\p{Blank}+carrés|millilitres|picomètres|ohms|hectares|centimètres|heures|bits|milles\\p{Blank}+marins|semaines|cal|MHz|°C|minutes|°F|yd|kcal|mégahertz|kJ|mégawatts|livres)(?![\\p{L}0-9])", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "([012]?\\d|3[01])(-|\\p{Blank}){0,2}(janv\\.|févr\\.|mars|avr\\.|mai|juin|juil\\.|août|sept\\.|oct\\.|nov\\.|déc\\.|janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)(-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)\\p{Blank}{0,2}(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?", 66));
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initCodeRules() {
        this.codesRules.add(getRegexRuleFromRawString());
        this.codesRules.add(getRegexRuleFromPrefixAndSuffix());
    }

    private PhoneNumberRule.RegexRule getRegexRuleFromRawString() {
        return new PhoneNumberRule.RegexRule(StorageManagerExt.INVALID_KEY_DESC) {
            /* class com.huawei.i18n.tmr.phonenumber.data.FrancePhoneNumberRule.AnonymousClass1 */

            @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule.RegexRule
            public Optional<PhoneNumberMatch> isValid(PhoneNumberMatch possibleNumber, String msg) {
                String rawString = possibleNumber.rawString();
                if (rawString.trim().startsWith("+") || rawString.trim().startsWith("0") || rawString.trim().startsWith("(+") || rawString.trim().startsWith("(0")) {
                    return Optional.of(possibleNumber);
                }
                return Optional.empty();
            }
        };
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initBoarderRules() {
        this.borderRules.add(new PhoneNumberRule.RegexRule(this.utils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
    }
}
