package com.huawei.g11n.tmr.phonenumber.data;

public class PhoneNumberRule_IT_IT extends PhoneNumberRule {
    public PhoneNumberRule_IT_IT(String country) {
        super(country);
        init();
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
        */
    public void init() {
        /*
            r25 = this;
            r0 = r25
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            com.huawei.g11n.tmr.phonenumber.data.ConstantsUtils r2 = new com.huawei.g11n.tmr.phonenumber.data.ConstantsUtils
            r2.<init>()
            java.lang.String r3 = "(?<!\\p{L})(gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?"
            java.lang.String r4 = "([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(gen|feb|mar|apr|mag|giu|lug|ago|set|ott|nov|dic)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01][0-9])(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d)?)?"
            java.lang.String r5 = "\\d{0,10}\\p{Blank}{0,2}\\(?\\d{2,10}\\)?\\p{Blank}{0,2}\\d{1,10}[xX*]{1,10}"
            java.lang.String r6 = "\\d[0-9.,]*\\d\\p{Blank}{0,4}(‰|%|\\p{Sc}|FCFA|EUR|XAF|USD|Pfund|Euro?)|(%|\\p{Sc}|FCFA|EUR|XAF|USD|Prezzo:?|Euro?)\\p{Blank}{0,4}(\\d{1,16}[.,]?){1,4}\\d{1,16}"
            java.lang.String r7 = "\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|EUR|XAF|USD|Pfund|EURO?|eur)"
            java.lang.String r8 = "(?<!\\p{L})(almeno|riduzione|A\\p{Blank}+proposito\\p{Blank}+di|alle|Nuovi|da|Più\\p{Blank}+di|altro|oltre|meno|fino\\p{Blank}+a|aumentati\\p{Blank}+a|minimo|Siamo\\p{Blank}+arrivati\\p{Blank}+a|P\\.IVA|Riferimento\\p{Blank}+offerta|intercity|Weibo|episodi|moltiplicati per|Prezzo|N°)\\p{Blank}{0,4}[:-]?\\p{Blank}{0,4}(\\d{1,16}[-]?){1,4}\\d{1,16}"
            java.lang.String r9 = "\\d[0-9-]*\\d\\p{Blank}*(punti|di|blocco|donne|Utenza|foto|intercity|Weibo|volte|volta|esimo|pensioni|figli|cani|Offerte|capelli|accounts|ragazzi|messaggi|amici|cose|tweet|tweets|pagine|contribuenti|notifiche|ragazze|medaglie|esima|screenshot|robe|eseguito|moltiplicati\\p{Blank}+per|bambini|anni|ore|minuti|secondi|-\\p{Blank}*Twitter|giorni)(?!\\p{L})"
            java.lang.String r10 = "\\d[0-9\\p{Blank}.,-]*\\d\\p{Blank}{0,4}(litri|chilogrammi|chilometri|galloni|kilohertz|byte|ettari|MB|m²|MW|once|kilobit|gigahertz|millimetri|grammi|oz|ohm|ha|millilitri|bit|Tb|ettopascal|kilojoule|pm|gal|Mb|pinte|miglia\\p{Blank}+nautiche|carati|hPa|Kelvin|GB|ore|megawatt|kilobyte|gradi|mg|dm|dl|ml|kHz|min|mm|Gb|ms|nmi|gigabit|kWh|secondi|megabit|tn|kW|kilowatt|decilitri|chilowattora|millisecondi|gradi\\p{Blank}+Fahrenheit|giorni|watt|decimetri|cm²|calorie|tonnellate|libbre|kg|terabit|Ω|kb|mesi|megabyte|hertz|centimetri\\p{Blank}+quadrati|kt|megahertz|gradi\\p{Blank}+Celsius|km|chilocalorie|centimetri|iarde|metri\\p{Blank}+quadrati|Hz|cm|terabyte|GHz|mA|°|lb|settimane|picometri|volt|gigabyte|joule|Cal|cal|MHz|minuti|TB|°C|°F|milligrammi|yd|kcal|metri|anni|milliampere|kJ|ampere|pt|kB)(?!\\p{L})"
            java.lang.String r11 = "(?<!\\p{L})(ICQ|Codice|(Codice\\p{Blank}*\\p{Alpha}{0,2}\\d{0,3})|Rif\\.|art\\.|numero\\p{Blank}+cliente|Id\\p{Blank}+FASTWEB|cod|Pin|id|codice\\p{Blank}+fiscale|Rolex\\p{Blank}+Datejust|pt\\.|ISBN|legge\\p{Blank}+numero)\\p{Blank}*:?\\p{Blank}*(\\d{1,16}[.,]){0,4}\\d{1,16}"
            java.lang.String r12 = "(?<!\\p{L})\\p{L}{1,2}-(\\d{1,16}[-]?){0,4}\\d{1,16}|(\\d{1,16}[-]?){0,4}\\d{1,16}-\\p{L}{1,2}(?!\\p{L})"
            java.lang.String r13 = "(?<!\\p{L})uuid\\p{Blank}*:?\\p{Blank}*[\\p{Alpha}\\d-]+"
            java.lang.String r14 = "(?<!\\d)([0-2]?\\d|3[01])/(0?\\d|1[0-2])\\p{Blank}*-\\p{Blank}*([0-2]?\\d|3[01])/(0?\\d|1[0-2])(?!\\d)"
            java.lang.String r15 = "(?<!\\d)(0?\\d|1[0-2])/(20[01][0-9]|19\\d{2})\\p{Blank}*-\\p{Blank}*(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})(?!\\d)"
            r16 = r13
            java.lang.String r13 = "(?<!(\\d|[-\\p{Blank}]\\d))([0-2]?\\d|3[01])[\\p{Blank}-]+(0?\\d|1[0-2])[\\p{Blank}-]+201\\d(?!(\\d|[-\\p{Blank}]\\d))"
            r17 = r7
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r7 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r18 = r5
            r5 = 2
            r7.<init>(r12, r5)
            r1.add(r7)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r7 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r5 = "url"
            java.lang.String r5 = r2.getValues(r5)
            r7.<init>(r5)
            r1.add(r7)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "yearperiod"
            java.lang.String r7 = r2.getValues(r7)
            r5.<init>(r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "date"
            java.lang.String r7 = r2.getValues(r7)
            r5.<init>(r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "date1"
            java.lang.String r7 = r2.getValues(r7)
            r5.<init>(r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "date2"
            java.lang.String r7 = r2.getValues(r7)
            r5.<init>(r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "email"
            java.lang.String r7 = r2.getValues(r7)
            r5.<init>(r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "time"
            java.lang.String r7 = r2.getValues(r7)
            r5.<init>(r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "dateperiod"
            java.lang.String r7 = r2.getValues(r7)
            r5.<init>(r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r5.<init>(r14)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r5.<init>(r15)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r5.<init>(r13)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r7 = 2
            r5.<init>(r3, r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r5.<init>(r4, r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "float_1"
            java.lang.String r7 = r2.getValues(r7)
            r5.<init>(r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r7 = 2
            r5.<init>(r6, r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r7 = 66
            r5.<init>(r8, r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r7 = 2
            r5.<init>(r10, r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r5.<init>(r9, r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "aite"
            java.lang.String r7 = r2.getValues(r7)
            r5.<init>(r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "exp"
            java.lang.String r7 = r2.getValues(r7)
            r5.<init>(r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r7 = 2
            r5.<init>(r11, r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r19 = r3
            r3 = r18
            r5.<init>(r3, r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r20 = r3
            r3 = r17
            r5.<init>(r3, r7)
            r1.add(r5)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r5 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r21 = r3
            r3 = r16
            r5.<init>(r3, r7)
            r1.add(r5)
            r0.negativeRules = r1
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r7 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r22 = r1
            java.lang.String r1 = "samenum"
            java.lang.String r1 = r2.getValues(r1)
            r23 = r2
            r2 = 9
            r24 = r3
            r3 = 2
            r7.<init>(r1, r3, r2)
            r5.add(r7)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r1 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "[\\(\\[]?(\\d{1,2}\\p{Blank}*-?\\p{Blank}*\\d{1,2}|\\d{2,5}|[1-9]{13,})"
            r1.<init>(r7, r3, r2)
            r5.add(r1)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r1 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r7 = "[\\(\\[]?\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}"
            r1.<init>(r7, r3, r2)
            r5.add(r1)
            r0.borderRules = r5
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            r0.codesRules = r1
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_IT_IT$1 r1 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_IT_IT$1
            java.lang.String r2 = ""
            r1.<init>(r0, r2)
            java.util.List r2 = r0.codesRules
            r2.add(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_IT_IT.init():void");
    }
}
