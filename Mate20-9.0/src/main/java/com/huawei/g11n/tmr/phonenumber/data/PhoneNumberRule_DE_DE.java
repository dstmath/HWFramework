package com.huawei.g11n.tmr.phonenumber.data;

public class PhoneNumberRule_DE_DE extends PhoneNumberRule {
    public PhoneNumberRule_DE_DE(String country) {
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
            r29 = this;
            r0 = r29
            java.lang.String r1 = "(?<!\\d)19222|11833|11837|11834(?!\\d)"
            r0.extraShortPattern = r1
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            com.huawei.g11n.tmr.phonenumber.data.ConstantsUtils r2 = new com.huawei.g11n.tmr.phonenumber.data.ConstantsUtils
            r2.<init>()
            java.lang.String r3 = "(?<!\\p{L})(Mar\\.|Jan|Feb|Mär|Apr|Mai|Jun|Jul|Aug|Sep|Okt|Nov|Dez|Jan\\.|Feb\\.|März|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.|M\\?r)\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*([012]?\\d|3[01])\\p{Blank}*(\\.|-|\\p{Blank})?\\p{Blank}*(1[4-9]\\d{2}|20[01]\\d)(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d))?"
            java.lang.String r4 = "([012]?\\d|3[01])(\\.|-|\\p{Blank})*(Mar\\.|Jan|Feb|Mär|Apr|Mai|Jun|Jul|Aug|Sep|Okt|Nov|Dez|Jan\\.|Feb\\.|März|Apr\\.|Mai|Juni|Juli|Aug\\.|Sep\\.|Okt\\.|Nov\\.|Dez\\.)(\\.|-|\\p{Blank})?(1[4-9]\\d{2}|20[01]\\d)(-\\d{2,16})?(\\p{Blank}*)(([01]?\\d|2[0-4])\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d(\\p{Blank}*[.:]\\p{Blank}*[0-5]\\d))?"
            java.lang.String r5 = "\\d{0,10}\\p{Blank}{0,2}\\(?\\d{2,10}\\)?\\p{Blank}{0,2}\\d{1,10}[xX*]{1,6}"
            java.lang.String r6 = "(\\d{1,16}\\p{Blank}{0,2}(\\.)\\p{Blank}{0,2}){1,4}\\d{1,16}"
            java.lang.String r7 = "\\d[0-9.,]*\\d\\p{Blank}{0,4}(‰|%|\\p{Sc}|FCFA|EUR|XAF|USD|Pfund|EURO?|Dollars?)|(%|\\p{Sc}|FCFA|EUR|XAF|USD|Preis:?|EURO?|Dollars?)\\p{Blank}{0,4}(\\d{1,16}[.,]?){1,4}\\d{1,16}"
            java.lang.String r8 = "\\d{2,16}\\p{Blank}{0,2},\\p{Blank}{0,2}-\\p{Blank}{0,2}(FCFA|EUR|XAF|USD|Pfund|EURO?)"
            java.lang.String r9 = "(?<!\\p{L})((Part\\.\\p{Blank}*No\\.|KdNr|Zufallszahl\\p{Blank}*-?|BLZ|Fehlercode|WKN|BMW|PLZ|Mobicool|Triebwagen|Bild|DIESEL|Pelikan|BIN|mal|Gr\\.|Drucksache|etwa|LEVIS|LEVI\\p{Blank}+S|Bosch|FC|ET|Bj\\.|TPH|Silit|PIRELLI\\p{Blank}+P|Artikelnummer|Ref|DL\\.|BMI|DWSG|Zum\\p{Blank}+Bild|POL\\p{Blank}*-\\p{Blank}*\\p{Alpha}{1,2}|CHE\\p{Blank}*-|K\\.Stand|PZN|Platzwit\\p{Blank}+loc|DB|ID|3ds|Boeing|BOSCH|Porsche|GM|Auftragsnummer|quittungsnummer|gechipt\\p{Blank}+mit\\p{Blank}+der\\p{Blank}+Nummer|Silber|Hot\\p{Blank}+Swap\\p{Blank}+Festplatte\\p{Blank}*-|GAV|RB|in|Kilometerstand|Artikelnummer|ICQ)\\p{Blank}{0,4}:?\\p{Blank}{0,4}|Einladungs\\p{Blank}*-\\p{Blank}*Code\\p{Blank}{0,2}\\p{Alpha}*)\\d{1,16}"
            java.lang.String r10 = "\\d{1,16}\\p{Blank}*(Monate|Kt|Wo\\.|Terabits|Grad\\p{Blank}+Celsius|Megabits|MB|m²|Kilokalorien|MW|Quadratzentimeter|Grad\\p{Blank}+Fahrenheit|oz|ha|Tb|Kalorien|Short\\p{Blank}+Tons|Watt|Hertz|pm|gal|Mb|Mon\\.|Kilojoule|Megawatt|hPa|Sekunden|Kelvin|GB|Grad|Hektopascal|Sek\\.|mg|dm|dl|ml|kHz|mm|Bits|Kilobits|Gb|Zentimeter|ms|Volt|Pfund|Kilogramm|Liter|kWh|Gallonen|Unzen|Gramm|Hektar|tn|kW|Stunden|cm²|Joule|Std\\.|kg|Ω|Wochen|Milliampere|kb|Terabytes|Millisekunden|Milligramm|Megabytes|Milliliter|Tage|Gigahertz|sm|Dezimeter|Pikometer|km|Kilobytes|Hz|Ohm|cm|GHz|mA|°|Millimeter|lb|Yards|Meter|Gigabytes|Kilometer|Kilowatt|Karat|Seemeilen|Kilowattstunden|Tg\\.|cal|MHz|TB|Gigabits|Bytes|°C|Megahertz|°F|yd|kcal|Ampere|Quadratmeter|Deziliter|Pints|kJ|Min\\.|pt|Minuten|Kilohertz|Jahre|kB)(?!\\p{L})"
            java.lang.String r11 = "\\d{1,16}\\p{Blank}*(TweetsWenn|mal|Einwohner|Pfanne|Ukrainer|Haushalte|Demonstranten|Abonnenten|Menschen|Tweets|Unterschriften|Flüchtlinge|Jahre|Fahrzeuge|Punkte|Abos|abos|Klicks|Obdachlosen|posts|Dosen|geschossenen|Einwohner|Trommel|Probleme|Notizbücher|Gewinne jetzt|BETRAGEN|No\\.|Stunden|mal|Stück|Bilder|Stunden)(?!\\p{L})"
            java.lang.String r12 = "(?<!\\p{L})\\p{L}{1,2}\\p{Blank}*-\\p{Blank}*\\d{4,16}|\\d{4,16}\\p{Blank}*-\\p{Blank}*\\p{L}{1,2}(?!\\p{L})"
            java.lang.String r13 = "(?<!\\p{L})(Straße|Strasse|dorf|Allee|Rail|Lok|Ferkeltaxi)\\p{Blank}*\\d{1,16}"
            java.lang.String r14 = "(?<!\\p{L})(auf|Rund|bis|mit)\\p{Blank}*(\\d{1,16}[-]?){1,4}\\d{1,16}"
            java.lang.String r15 = "(?<!\\p{L})(gelb|schwarz|weiß|rot|grün)(\\d{1,16}[-]?){1,4}\\d{1,16}"
            r16 = r15
            java.lang.String r15 = "(?<!\\d)([0-2]?\\d|3[01])/(0?\\d|1[0-2])\\p{Blank}*-\\p{Blank}*([0-2]?\\d|3[01])/(0?\\d|1[0-2])(?!\\d)"
            r17 = r14
            java.lang.String r14 = "(?<!\\d)(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})\\p{Blank}*-\\p{Blank}*(0?\\d|1[0-2])/(20[01][0-9]|19//d{2})(?!\\d)"
            r18 = r13
            java.lang.String r13 = "(?<![0])(20[01][0-9]|19\\d{2})\\p{Blank}*[-/]\\p{Blank}*(0?\\d|1[0-2])(\\p{Blank}*[-/]\\p{Blank}*([0-2]?\\d|3[01]))?(?!\\d)"
            r19 = r11
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r11 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r20 = r10
            r10 = 2
            r11.<init>(r12, r10)
            r1.add(r11)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r11 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r10 = "url"
            java.lang.String r10 = r2.getValues(r10)
            r11.<init>(r10)
            r1.add(r11)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r11 = "yearperiod"
            java.lang.String r11 = r2.getValues(r11)
            r10.<init>(r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r11 = "date"
            java.lang.String r11 = r2.getValues(r11)
            r10.<init>(r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r11 = "date1"
            java.lang.String r11 = r2.getValues(r11)
            r10.<init>(r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r11 = "date2"
            java.lang.String r11 = r2.getValues(r11)
            r10.<init>(r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r11 = "email"
            java.lang.String r11 = r2.getValues(r11)
            r10.<init>(r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r11 = "time"
            java.lang.String r11 = r2.getValues(r11)
            r10.<init>(r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r10.<init>(r15)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r10.<init>(r14)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r10.<init>(r13)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r11 = 66
            r10.<init>(r3, r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r10.<init>(r4, r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r10.<init>(r6)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r11 = "dateperiod"
            java.lang.String r11 = r2.getValues(r11)
            r10.<init>(r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r11 = 2
            r10.<init>(r5, r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r11 = 66
            r10.<init>(r7, r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r10.<init>(r8, r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r11 = 2
            r10.<init>(r9, r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r21 = r3
            r3 = r20
            r10.<init>(r3, r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r22 = r3
            r3 = r19
            r10.<init>(r3, r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r11 = "aite"
            java.lang.String r11 = r2.getValues(r11)
            r10.<init>(r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r11 = "exp"
            java.lang.String r11 = r2.getValues(r11)
            r10.<init>(r11)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r23 = r2
            r11 = r18
            r2 = 66
            r10.<init>(r11, r2)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r10 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r24 = r3
            r2 = r17
            r3 = 2
            r10.<init>(r2, r3)
            r1.add(r10)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r3 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r25 = r2
            r10 = r16
            r2 = 66
            r3.<init>(r10, r2)
            r1.add(r3)
            r0.negativeRules = r1
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r3 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            r26 = r1
            java.lang.String r1 = "(0{3,}|1{3,}|2{3,}|3{3,}|4{3,}|5{3,}|6{3,}|7{3,}|8{3,}|9{3,})"
            r27 = r4
            r4 = 9
            r28 = r5
            r5 = 2
            r3.<init>(r1, r5, r4)
            r2.add(r3)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r1 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r3 = "[\\(\\[]?(\\d{1,2}\\p{Blank}*[-.]?\\p{Blank}*\\d{1,2}|\\d{2,4})"
            r1.<init>(r3, r5, r4)
            r2.add(r1)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule r1 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule$RegexRule
            java.lang.String r3 = "[\\(\\[]?\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}\\p{Blank}{0,2}[-.]\\p{Blank}{0,2}\\d{2}"
            r1.<init>(r3, r5, r4)
            r2.add(r1)
            r0.borderRules = r2
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            r0.codesRules = r1
            java.util.List r1 = r0.codesRules
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE$1 r3 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE$1
            java.lang.String r4 = ""
            r3.<init>(r0, r4)
            r1.add(r3)
            com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE$2 r1 = new com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE$2
            java.lang.String r3 = ""
            r1.<init>(r0, r3)
            java.util.List r3 = r0.codesRules
            r3.add(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.g11n.tmr.phonenumber.data.PhoneNumberRule_DE_DE.init():void");
    }
}
