package com.huawei.i18n.tmr.phonenumber.data;

import com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule;

public class SpainPhoneNumberRule extends PhoneNumberRule {
    private final String money = "FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS";
    private final String month = "ene\\.|feb\\.|mar\\.|abr\\.|may\\.|jun\\.|jul\\.|ago\\.|sept\\.|oct\\.|nov\\.|dic\\.|enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre";
    private final String negativeRule1 = "[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS))\\p{Blank}*[0-9.,]+(?<![.,])";
    private final String negativeRule2 = "(?<![\\p{L}])(ci|ref|plus(\\p{Blank}+de)?|i\\.?d|sn|tweets?|twitter|icq|qq)[\\p{Blank}:]*[0-9]{4,16}";
    private final String negativeRule3 = "[0-9.,]{4,16}(?<![.,])\\p{Blank}*(minutos|segundos|kilobytes|megavatios|megabytes|kilojulios|toneladas|kilómetros|MB|m²|decilitros|hectopascales|MW|mililitros|oz|ha|Tb|pm|gal|Mb|grados|amperios|bytes|A|B|hPa|kilohercios|GB|M|J|K|picómetros|W|V|millas\\p{Blank}+náuticas|voltios|mg|dm|dl|ml|kHz|min|sem\\.|g|mm|d|b|miliamperios|c|onzas|Gb|centímetros|gramos|ms|a|l|m\\.|m|kilogramos|hectáreas|h|kilovatios|kWh|s|quilates|semanas|grados\\p{Blank}+Fahrenheit|megahercios|kW|ohmios|horas|kilovatios-hora|cm²|galones|centímetros\\p{Blank}+cuadrados|miligramos|libras|meses|kg|Ω|kb|días|julios|gigabytes|pintas|kilobits|km|milímetros|Hz|cm|vatios|GHz|gigahercios|gigabits|mA|°|lb|yardas|calorías|metros|litros|decímetros|kelvin|bits|ton|cal|MHz|TB|megabits|kilocalorías|°C|metros\\p{Blank}+cuadrados|grados\\p{Blank}+Celsius|°F|yd|milisegundos|terabits|kcal|años|kJ|pt|terabytes|kB|hercios|tweet|millones|mil)(?![\\p{L}0-9])";
    private final String negativeRule4 = "([012]?\\d|3[01])(.|-|\\p{Blank}){0,2}(ene\\.|feb\\.|mar\\.|abr\\.|may\\.|jun\\.|jul\\.|ago\\.|sept\\.|oct\\.|nov\\.|dic\\.|enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)(.|-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}{0,2})(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?";
    private final String shuffix = "tweet|millones|mil";
    private final String units = "minutos|segundos|kilobytes|megavatios|megabytes|kilojulios|toneladas|kilómetros|MB|m²|decilitros|hectopascales|MW|mililitros|oz|ha|Tb|pm|gal|Mb|grados|amperios|bytes|A|B|hPa|kilohercios|GB|M|J|K|picómetros|W|V|millas\\p{Blank}+náuticas|voltios|mg|dm|dl|ml|kHz|min|sem\\.|g|mm|d|b|miliamperios|c|onzas|Gb|centímetros|gramos|ms|a|l|m\\.|m|kilogramos|hectáreas|h|kilovatios|kWh|s|quilates|semanas|grados\\p{Blank}+Fahrenheit|megahercios|kW|ohmios|horas|kilovatios-hora|cm²|galones|centímetros\\p{Blank}+cuadrados|miligramos|libras|meses|kg|Ω|kb|días|julios|gigabytes|pintas|kilobits|km|milímetros|Hz|cm|vatios|GHz|gigahercios|gigabits|mA|°|lb|yardas|calorías|metros|litros|decímetros|kelvin|bits|ton|cal|MHz|TB|megabits|kilocalorías|°C|metros\\p{Blank}+cuadrados|grados\\p{Blank}+Celsius|°F|yd|milisegundos|terabits|kcal|años|kJ|pt|terabytes|kB|hercios";

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initNegativeRules() {
        initCommonNegativeRules();
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, this.utils.getValues(ConstantsUtils.FLOAT_1)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(FCFA|XAF|USD|Pfund|Won|EURO?S?|de\\p{Blank}+francs|(de\\p{Blank}+)?dollars|d'EUROS))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<![\\p{L}])(ci|ref|plus(\\p{Blank}+de)?|i\\.?d|sn|tweets?|twitter|icq|qq)[\\p{Blank}:]*[0-9]{4,16}", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "[0-9.,]{4,16}(?<![.,])\\p{Blank}*(minutos|segundos|kilobytes|megavatios|megabytes|kilojulios|toneladas|kilómetros|MB|m²|decilitros|hectopascales|MW|mililitros|oz|ha|Tb|pm|gal|Mb|grados|amperios|bytes|A|B|hPa|kilohercios|GB|M|J|K|picómetros|W|V|millas\\p{Blank}+náuticas|voltios|mg|dm|dl|ml|kHz|min|sem\\.|g|mm|d|b|miliamperios|c|onzas|Gb|centímetros|gramos|ms|a|l|m\\.|m|kilogramos|hectáreas|h|kilovatios|kWh|s|quilates|semanas|grados\\p{Blank}+Fahrenheit|megahercios|kW|ohmios|horas|kilovatios-hora|cm²|galones|centímetros\\p{Blank}+cuadrados|miligramos|libras|meses|kg|Ω|kb|días|julios|gigabytes|pintas|kilobits|km|milímetros|Hz|cm|vatios|GHz|gigahercios|gigabits|mA|°|lb|yardas|calorías|metros|litros|decímetros|kelvin|bits|ton|cal|MHz|TB|megabits|kilocalorías|°C|metros\\p{Blank}+cuadrados|grados\\p{Blank}+Celsius|°F|yd|milisegundos|terabits|kcal|años|kJ|pt|terabytes|kB|hercios|tweet|millones|mil)(?![\\p{L}0-9])", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "([012]?\\d|3[01])(.|-|\\p{Blank}){0,2}(ene\\.|feb\\.|mar\\.|abr\\.|may\\.|jun\\.|jul\\.|ago\\.|sept\\.|oct\\.|nov\\.|dic\\.|enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)(.|-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}{0,2})(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?"));
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initCodeRules() {
        this.codesRules.add(getRegexRuleFromPrefixAndSuffix());
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initBoarderRules() {
        this.borderRules.add(new PhoneNumberRule.RegexRule(this.utils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
    }
}
