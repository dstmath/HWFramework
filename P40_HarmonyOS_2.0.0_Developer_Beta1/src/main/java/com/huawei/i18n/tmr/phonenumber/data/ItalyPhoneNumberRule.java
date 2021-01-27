package com.huawei.i18n.tmr.phonenumber.data;

import com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule;

public class ItalyPhoneNumberRule extends PhoneNumberRule {
    private final String negativeRule1 = "(?<!\\p{L})\\p{L}{1,2}-(\\d{1,16}[-]?){0,4}\\d{1,16}|(\\d{1,16}[-]?){0,4}\\d{1,16}-\\p{L}{1,2}(?!\\p{L})";
    private final String negativeRule10 = "\\d[0-9-]*\\d\\p{Blank}*(punti|di|blocco|donne|Utenza|foto|intercity|Weibo|volte|volta|esimo|pensioni|figli|cani|Offerte|capelli|accounts|ragazzi|messaggi|amici|cose|tweet|tweets|pagine|contribuenti|notifiche|ragazze|medaglie|esima|screenshot|robe|eseguito|moltiplicati\\p{Blank}+per|bambini|anni|ore|minuti|secondi|-\\p{Blank}*Twitter|giorni)(?!\\p{L})";
    private final String negativeRule11 = "(?<!\\p{L})(ICQ|Codice|(Codice\\p{Blank}*\\p{Alpha}{0,2}\\d{0,3})|Rif\\.|art\\.|numero\\p{Blank}+cliente|Id\\p{Blank}+FASTWEB|cod|Pin|id|codice\\p{Blank}+fiscale|Rolex\\p{Blank}+Datejust|pt\\.|ISBN|legge\\p{Blank}+numero)\\p{Blank}*:?\\p{Blank}*(\\d{1,16}[.,]){0,4}\\d{1,16}";
    private final String negativeRule12 = "\\d{0,10}\\p{Blank}{0,2}\\(?\\d{2,10}\\)?\\p{Blank}{0,2}\\d{1,10}[xX*]{1,10}";
    private final String negativeRule13 = "\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|EUR|XAF|USD|Pfund|EURO?|eur)";
    private final String negativeRule14 = "(?<!\\p{L})uuid\\p{Blank}*:?\\p{Blank}*[\\p{Alpha}\\d-]+";
    private final String negativeRule2 = "(?<!\\d)([0-2]?\\d|3[01])/(0?\\d|1[0-2])\\p{Blank}*-\\p{Blank}*([0-2]?\\d|3[01])/(0?\\d|1[0-2])(?!\\d)";
    private final String negativeRule3 = "(?<!\\d)(0?\\d|1[0-2])/(20[01][0-9]|19\\d{2})\\p{Blank}*-\\p{Blank}*(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})(?!\\d)";
    private final String negativeRule4 = "(?<!(\\d|[-\\p{Blank}]\\d))([0-2]?\\d|3[01])[\\p{Blank}-]+(0?\\d|1[0-2])[\\p{Blank}-]+201\\d(?!(\\d|[-\\p{Blank}]\\d))";
    private final String negativeRule5 = "(?<!\\p{L})(gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?";
    private final String negativeRule6 = "([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?";
    private final String negativeRule7 = "\\d[0-9.,]*\\d\\p{Blank}{0,4}(‰|%|\\p{Sc}|FCFA|EUR|XAF|USD|Pfund|Euro?)|(%|\\p{Sc}|FCFA|EUR|XAF|USD|Prezzo:?|Euro?)\\p{Blank}{0,4}(\\d{1,16}[.,]?){1,4}\\d{1,16}";
    private final String negativeRule8 = "(?<!\\p{L})(almeno|riduzione|A\\p{Blank}+proposito\\p{Blank}+di|alle|Nuovi|da|Più\\p{Blank}+di|altro|oltre|meno|fino\\p{Blank}+a|aumentati\\p{Blank}+a|minimo|Siamo\\p{Blank}+arrivati\\p{Blank}+a|P\\.IVA|Riferimento\\p{Blank}+offerta|intercity|Weibo|episodi|moltiplicati per|Prezzo|N°)\\p{Blank}{0,4}[:-]?\\p{Blank}{0,4}(\\d{1,16}[-]?){1,4}\\d{1,16}";
    private final String negativeRule9 = "\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(litri|chilogrammi|chilometri|galloni|kilohertz|byte|ettari|MB|m²|MW|once|kilobit|gigahertz|millimetri|grammi|oz|ohm|ha|millilitri|bit|Tb|ettopascal|kilojoule|pm|gal|Mb|pinte|miglia\\p{Blank}+nautiche|carati|hPa|Kelvin|GB|ore|megawatt|kilobyte|gradi|mg|dm|dl|ml|kHz|min|mm|Gb|ms|nmi|gigabit|kWh|secondi|megabit|tn|kW|kilowatt|decilitri|chilowattora|millisecondi|gradi\\p{Blank}+Fahrenheit|giorni|watt|decimetri|cm²|calorie|tonnellate|libbre|kg|terabit|Ω|kb|mesi|megabyte|hertz|centimetri\\p{Blank}+quadrati|kt|megahertz|gradi\\p{Blank}+Celsius|km|chilocalorie|centimetri|iarde|metri\\p{Blank}+quadrati|Hz|cm|terabyte|GHz|mA|°|lb|settimane|picometri|volt|gigabyte|joule|Cal|cal|MHz|minuti|TB|°C|°F|milligrammi|yd|kcal|metri|anni|milliampere|kJ|ampere|pt|kB)(?!\\p{L})";

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initNegativeRules() {
        initCommonNegativeRules();
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, this.utils.getValues(ConstantsUtils.FLOAT_1)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, this.utils.getValues(ConstantsUtils.YEAR_PERIOD)));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})\\p{L}{1,2}-(\\d{1,16}[-]?){0,4}\\d{1,16}|(\\d{1,16}[-]?){0,4}\\d{1,16}-\\p{L}{1,2}(?!\\p{L})", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\d)([0-2]?\\d|3[01])/(0?\\d|1[0-2])\\p{Blank}*-\\p{Blank}*([0-2]?\\d|3[01])/(0?\\d|1[0-2])(?!\\d)"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\d)(0?\\d|1[0-2])/(20[01][0-9]|19\\d{2})\\p{Blank}*-\\p{Blank}*(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})(?!\\d)"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!(\\d|[-\\p{Blank}]\\d))([0-2]?\\d|3[01])[\\p{Blank}-]+(0?\\d|1[0-2])[\\p{Blank}-]+201\\d(?!(\\d|[-\\p{Blank}]\\d))"));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})(gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d[0-9.,]*\\d\\p{Blank}{0,4}(‰|%|\\p{Sc}|FCFA|EUR|XAF|USD|Pfund|Euro?)|(%|\\p{Sc}|FCFA|EUR|XAF|USD|Prezzo:?|Euro?)\\p{Blank}{0,4}(\\d{1,16}[.,]?){1,4}\\d{1,16}", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})(almeno|riduzione|A\\p{Blank}+proposito\\p{Blank}+di|alle|Nuovi|da|Più\\p{Blank}+di|altro|oltre|meno|fino\\p{Blank}+a|aumentati\\p{Blank}+a|minimo|Siamo\\p{Blank}+arrivati\\p{Blank}+a|P\\.IVA|Riferimento\\p{Blank}+offerta|intercity|Weibo|episodi|moltiplicati per|Prezzo|N°)\\p{Blank}{0,4}[:-]?\\p{Blank}{0,4}(\\d{1,16}[-]?){1,4}\\d{1,16}", 66));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(litri|chilogrammi|chilometri|galloni|kilohertz|byte|ettari|MB|m²|MW|once|kilobit|gigahertz|millimetri|grammi|oz|ohm|ha|millilitri|bit|Tb|ettopascal|kilojoule|pm|gal|Mb|pinte|miglia\\p{Blank}+nautiche|carati|hPa|Kelvin|GB|ore|megawatt|kilobyte|gradi|mg|dm|dl|ml|kHz|min|mm|Gb|ms|nmi|gigabit|kWh|secondi|megabit|tn|kW|kilowatt|decilitri|chilowattora|millisecondi|gradi\\p{Blank}+Fahrenheit|giorni|watt|decimetri|cm²|calorie|tonnellate|libbre|kg|terabit|Ω|kb|mesi|megabyte|hertz|centimetri\\p{Blank}+quadrati|kt|megahertz|gradi\\p{Blank}+Celsius|km|chilocalorie|centimetri|iarde|metri\\p{Blank}+quadrati|Hz|cm|terabyte|GHz|mA|°|lb|settimane|picometri|volt|gigabyte|joule|Cal|cal|MHz|minuti|TB|°C|°F|milligrammi|yd|kcal|metri|anni|milliampere|kJ|ampere|pt|kB)(?!\\p{L})", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d[0-9-]*\\d\\p{Blank}*(punti|di|blocco|donne|Utenza|foto|intercity|Weibo|volte|volta|esimo|pensioni|figli|cani|Offerte|capelli|accounts|ragazzi|messaggi|amici|cose|tweet|tweets|pagine|contribuenti|notifiche|ragazze|medaglie|esima|screenshot|robe|eseguito|moltiplicati\\p{Blank}+per|bambini|anni|ore|minuti|secondi|-\\p{Blank}*Twitter|giorni)(?!\\p{L})", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})(ICQ|Codice|(Codice\\p{Blank}*\\p{Alpha}{0,2}\\d{0,3})|Rif\\.|art\\.|numero\\p{Blank}+cliente|Id\\p{Blank}+FASTWEB|cod|Pin|id|codice\\p{Blank}+fiscale|Rolex\\p{Blank}+Datejust|pt\\.|ISBN|legge\\p{Blank}+numero)\\p{Blank}*:?\\p{Blank}*(\\d{1,16}[.,]){0,4}\\d{1,16}", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d{0,10}\\p{Blank}{0,2}\\(?\\d{2,10}\\)?\\p{Blank}{0,2}\\d{1,10}[xX*]{1,10}", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|EUR|XAF|USD|Pfund|EURO?|eur)", 2));
        this.negativeRules.add(new PhoneNumberRule.RegexRule(this, "(?<!\\p{L})uuid\\p{Blank}*:?\\p{Blank}*[\\p{Alpha}\\d-]+", 2));
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initCodeRules() {
        this.codesRules.add(getRegexRuleFromPrefixAndSuffix());
    }

    @Override // com.huawei.i18n.tmr.phonenumber.data.PhoneNumberRule
    public void initBoarderRules() {
        this.borderRules.add(new PhoneNumberRule.RegexRule(this.utils.getValues(ConstantsUtils.SAME_NUM), 2, 9));
        this.borderRules.add(new PhoneNumberRule.RegexRule("[\\(\\[]?(\\d{1,2}\\p{Blank}*-?\\p{Blank}*\\d{1,2}|\\d{2,5}|[1-9]{13,})", 2, 9));
        this.borderRules.add(new PhoneNumberRule.RegexRule("[\\(\\[]?\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}", 2, 9));
    }
}
