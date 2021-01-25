package com.huawei.i18n.tmr.phonenumber.data;

import com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule;

public class PortugalPhoneNumberRule extends PhoneNumberRule {
    private final String money = "FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?";
    private final String month = "jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez|janeiro|fevereiro|março|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro";
    private final String negativeRule1 = "[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?))\\p{Blank}*[0-9.,]+(?<![.,])";
    private final String negativeRule2 = "\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?)";
    private final String negativeRule3 = "(?<![\\p{L}])((ref|id|num|qq|ICQ|tweets?|twitter|(decreto|consulta)\\p{Blank}+nº|Série|nif|FM|contas)\\p{Blank}*:?)\\p{Blank}*[0-9]{4,16}";
    private final String negativeRule4 = "([012]?\\d|3[01])(.|-|\\p{Blank}){0,2}(jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez|janeiro|fevereiro|março|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro)(.|-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}{0,2})(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?";
    private final String negativeRule5 = "\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(minutos|segundos|kilobytes|megabytes|toneladas|dias|kilohertz|byte|metros\\p{Blank}+quadrados|quilómetros|MB|decilitros|m²|MW|mililitros|gigahertz|pints|oz|ha|bit|hectopascais|Tb|pm|gal|Mb|milissegundos|bytes|quilocalorias|A|hPa|GB|jardas|J|K|picómetros|W|V|quilojoules|kelvins|mg|dm|dl|megawatts|ml|kHz|joules|min|quilogramas|g|sem\\.|mm|graus|miliamperes|miligramas|Gb|centímetros|ms|l|milhas\\p{Blank}+náuticas|m|nmi|h|kWh|s|quilates|semanas|volts|kW|horas|cm²|calorias|libras|meses|anos|kg|Ω|quilowatts|amperes|kb|quilowatts-hora|gigabytes|hertz|megahertz|watts|kilobits|onças|ct|km|milímetros|Hz|cm|GHz|gigabits|mA|graus\\p{Blank}+Celsius|°|centímetros\\p{Blank}+quadrados|lb|metros|ohms|litros|hectares|decímetros|bits|ton|cal|MHz|TB|megabits|°C|°F|yd|terabits|kcal|galões|kJ|pt|gramas|terabytes|graus\\p{Blank}+Fahrenheit|kB)(?![\\p{L}0-9])";
    private final String negativeRule6 = "\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(vezes|(de\\p{Blank}+)?tweets?|twittar|rts|temporada|contas|capitulos|páginas|no\\p{Blank}+total\\p{Blank}+em\\p{Blank}+dinheiro|milhoes|mil)(?!\\p{L})";
    private final String units = "minutos|segundos|kilobytes|megabytes|toneladas|dias|kilohertz|byte|metros\\p{Blank}+quadrados|quilómetros|MB|decilitros|m²|MW|mililitros|gigahertz|pints|oz|ha|bit|hectopascais|Tb|pm|gal|Mb|milissegundos|bytes|quilocalorias|A|hPa|GB|jardas|J|K|picómetros|W|V|quilojoules|kelvins|mg|dm|dl|megawatts|ml|kHz|joules|min|quilogramas|g|sem\\.|mm|graus|miliamperes|miligramas|Gb|centímetros|ms|l|milhas\\p{Blank}+náuticas|m|nmi|h|kWh|s|quilates|semanas|volts|kW|horas|cm²|calorias|libras|meses|anos|kg|Ω|quilowatts|amperes|kb|quilowatts-hora|gigabytes|hertz|megahertz|watts|kilobits|onças|ct|km|milímetros|Hz|cm|GHz|gigabits|mA|graus\\p{Blank}+Celsius|°|centímetros\\p{Blank}+quadrados|lb|metros|ohms|litros|hectares|decímetros|bits|ton|cal|MHz|TB|megabits|°C|°F|yd|terabits|kcal|galões|kJ|pt|gramas|terabytes|graus\\p{Blank}+Fahrenheit|kB";

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void init() {
        super.init();
        this.extraShortPattern = "(?<!\\d)1414(?!\\d)";
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initNegativeRules() {
        initCommonNegativeRules();
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, this.utils.getValues(ConstantsUtils.FLOAT_1)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, this.utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "[0-9.,]+(?<![.,])\\p{Blank}*(‰|%|\\p{Sc}|FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?(?!\\p{L}))|(\\p{Sc}|(?<!\\p{L})(FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?))\\p{Blank}*[0-9.,]+(?<![.,])", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|XAF|USD|Pfund|Won|euro|eur|Dollars?)", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<![\\p{L}])((ref|id|num|qq|ICQ|tweets?|twitter|(decreto|consulta)\\p{Blank}+nº|Série|nif|FM|contas)\\p{Blank}*:?)\\p{Blank}*[0-9]{4,16}", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "([012]?\\d|3[01])(.|-|\\p{Blank}){0,2}(jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez|janeiro|fevereiro|março|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro)(.|-|\\p{Blank}){0,2}(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}{0,2})(([01]?\\d|2[0-4])\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d(\\p{Blank}{0,2}[.:]\\p{Blank}{0,2}[0-5]\\d))?", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(minutos|segundos|kilobytes|megabytes|toneladas|dias|kilohertz|byte|metros\\p{Blank}+quadrados|quilómetros|MB|decilitros|m²|MW|mililitros|gigahertz|pints|oz|ha|bit|hectopascais|Tb|pm|gal|Mb|milissegundos|bytes|quilocalorias|A|hPa|GB|jardas|J|K|picómetros|W|V|quilojoules|kelvins|mg|dm|dl|megawatts|ml|kHz|joules|min|quilogramas|g|sem\\.|mm|graus|miliamperes|miligramas|Gb|centímetros|ms|l|milhas\\p{Blank}+náuticas|m|nmi|h|kWh|s|quilates|semanas|volts|kW|horas|cm²|calorias|libras|meses|anos|kg|Ω|quilowatts|amperes|kb|quilowatts-hora|gigabytes|hertz|megahertz|watts|kilobits|onças|ct|km|milímetros|Hz|cm|GHz|gigabits|mA|graus\\p{Blank}+Celsius|°|centímetros\\p{Blank}+quadrados|lb|metros|ohms|litros|hectares|decímetros|bits|ton|cal|MHz|TB|megabits|°C|°F|yd|terabits|kcal|galões|kJ|pt|gramas|terabytes|graus\\p{Blank}+Fahrenheit|kB)(?![\\p{L}0-9])", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(vezes|(de\\p{Blank}+)?tweets?|twittar|rts|temporada|contas|capitulos|páginas|no\\p{Blank}+total\\p{Blank}+em\\p{Blank}+dinheiro|milhoes|mil)(?!\\p{L})", 2));
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
