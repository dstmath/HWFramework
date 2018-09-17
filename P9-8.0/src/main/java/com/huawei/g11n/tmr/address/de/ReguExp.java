package com.huawei.g11n.tmr.address.de;

import com.huawei.g11n.tmr.util.Regexs;

public class ReguExp extends Regexs {
    private static String ED = ("((" + wordsBig + keyBui + ")" + "|(" + keyBui + "(\\s*-\\s*|\\s*)" + wordsBig + "))" + "(?:,\\s*)?" + "\\b");
    private static String ED_Independ = ("((" + wordsBig + keyBui_independ + ")" + "|(" + keyBui_independ + "(\\s*-\\s*|\\s*)" + wordsBig + "))" + "(?:,\\s*)?" + "\\b");
    private static String ST = (wordsBig + keyStr + "(?:(\\s*(?<!\\d)\\d{1,4}(?!\\d))(?:-\\d{1,4})?)?");
    private static String _city = ("(?:(?:\\s+|\\s*,\\s*)(?:" + keyPre + "\\s+)?" + "(\\s*#?\\d{5}\\s*)?#?" + keyCity + "(?:,\\s+(?:" + wordBig + ",\\s+)?Germany|Deutschland?)?" + ")?");
    private static String blackkey = "\\b((?i)einen|kleinen|wurde|diese|gibt|viele|dieses|dass|meine|Ihre|diejenigen|unsere|seine|sie|Bezug|aufs|Nähe|eine|gerne|Zimmer|Einbahnstraße|Passstraßen|Jedes|Jene|Welche|Manche|Neue|Alte|Beide|Nächste|den)\\b";
    private static String blackkey_indi = "(?:(?i)einen|kleinen|diese|viele|dieses|dass|Ihre|diejenigen|unsere|seine|sie|aufs|Nähe|eine|Jedes|Jene|Welche|Manche|Neue|Alte|Beide|Nächste|den)\\b";
    private static String blackkey_noSingal = "(?:(?i)platz|bahnhof|Schule|Kirche|Messe)";
    private static String blackkey_unindi = "(?:(?i)Einbahn|Pass|Schnell)";
    private static String citykey0 = "(Bad\\s{1,3})?[A-Z][a-zäüßö]{2,16}";
    private static String citykey1 = (citykey0 + "(-" + citykey0 + ")?");
    private static String countrykey = "((?i)Germany|Deutschland)";
    private static String keyBui = "((?i)Apotheke|Altersheim|Amtsgericht|Anlegeplatz|Bank|Buchladen|Bibliothek|Bücherei|Bistro|Blumengeschäft|Buchhandlung|Boutique|Büchersammlung|Cafeteria|Cantina|Drogerie|Entbindungsklinik|Einkaufszentrum|Flughafen|Gasthof|Gasthaus|Gastshaus|Garten|Gartenanlagen|Grundschule|Gaststätte|Heilanstalt|Herberge|Hospital|Hotel|Hotelkette|Institut|Jugendherberge|Kantine|Klinik|Kaufhaus|Kaufhalle|Kaufhäuser|Krankenhaus|Kneipe|Lazarett|Mall|Motel|Messehalle|Markthalle|Museum|Markt|Metrostation|Plaza|Postamt|Pharmazie|Privatklinik|Postbank|Reiseagentur|Restaurant|Reisebüro|Schauplatz|Schaubühne|Schauspielhaus|Schankwirtschaft|Shopping\\s*-\\s*zentrum|Schenke|Schänke|Stattliches\\s+wohnhaus|Shopping\\s*-\\s*Center|Spital|Spielplatz|Supermarkt|Sparkasse|Tankstelle|Tussauds|Tiergarten|U\\s*-\\s*bahn\\s+station|Vergnügungspark|Wharf|Weingeschäft|Wirtshaus|Shop|Seebrücke|Park|Zoo|Station|Theater|College|Universität|Gymnasium|Kindergarten|Fitness\\s*-\\s*studio|Bushaltestelle|Tierpark|Hochschule|Gynasium|Hbf|Galerie|bahnhof|Gallery|Stadion|Rathaus|Studio|Wohnung|brücke|University)";
    private static String keyBui_independ = "((?i)Platz|Bureau|Dom|Fabrik|Halle|Hall|Kirche|Messe|Turm|Flohmarkt|Fitnessstudio|Fitnessraum|Trainingsraum|Center|Geldinstitut|Gerichtshof|Gebäude|Hochhaus|Kreditinstitut|Villa|Zenturm|Areal|GmbH)";
    private static String keyCity = ("(" + citykey1 + "|[A-ZÄÖÜ]{1,3}[^a-zäüßö])" + "((" + tokenkey + statekey0 + ")?" + "(" + tokenkey + countrykey + "))?");
    private static String keyPre = "(?:(?i)at|in|im|am|aus|bei|auf|zum|zu)";
    private static String keyStr = "((?i)straße|avenue|allee|strasse|str\\.|gasse|Steinweg|damm|street|Landstraße)";
    private static String regPrep = ("\\s+" + keyPre + "\\s+");
    private static String statekey0 = "[A-Z][a-zäüßö]{2,16}";
    private static String tokenkey = "(\\s|-|:|#|,|\\s\\(.*\\)\\s)+";
    private static String whitekey = "(?:(?i)Brandenburger\\s+Tor|Gezi\\s*-\\s*Park|Gezi\\s+Park|Otto\\s*-\\s*Beck\\s*-\\s*Straße|Tel\\s+Aviv\\s+Museum|Red\\s+Roof\\s+Inn|Elbe\\s*-\\s*Einkaufszentrum|Toon\\s+Arena|Ausm\\s+zoo|Ruhr\\s*-\\s*Universität|Landkreis\\s*Rottal-\\s*Inn|Istanbul\\s+Holiday\\s+Inn|Hyde\\s+Park|Gabis\\s+Art\\s+Galerie|Otto\\s*-\\s*Hahn\\s*-\\s*Straße|Rega\\s+Hotel\\s+Stuttgart|Opel\\s*-\\s*Zoo|Stadttheater|Essl\\s+Museum|MesseStar\\s+Tankstelle|Karl\\s*-\\s*Marx\\s*-\\s*Straße|Wall\\s*-\\s*Street|Croke\\s+Park|City\\s*-\\s*Galerie\\s+Augsburg|Festung\\s+Königstein|Karl\\s*-\\s*Marx\\s*-\\s*Allee|Modern\\s+Art\\s+Museum|Adam\\s*-\\s*Ries\\s*-\\s*Strasse|Louvre\\s+Museum|Ditmar\\s*-\\s*Koel\\s*-\\s*Straße|MAMAG\\s+Museum|Gabis\\s+Art\\s+Galerie|Karl\\s*-\\s*May\\s*-\\s*Museum|Red\\s+Bull\\s*-\\s*Arena|Van\\s+Gogh\\s+Museum|Disneyland|Universität\\s+Harvard|Stanford|Eiffelturm)";
    private static String wordBig = "\\b[A-Z][A-Za-zäüßö]";
    private static String wordsBig = ("((?:" + wordBig + "{3,25}" + "(?:\\s*-\\s*|\\s+)){0,2})(" + wordBig + "{2,25}" + ")(\\s*-\\s*|\\s*)");

    public void init() {
        put("pBlackKey", blackkey);
        put("pRegWT", whitekey);
        put("pRegED_Independ", ED_Independ);
        put("pRegED", ED);
        put("pReg_city", _city);
        put("pRegST", ST);
        put("pRegPrep", regPrep);
        put("pRegBlackKeyIndi", blackkey_indi);
        put("pRegBlackKeyIndi_withBlank", blackkey_indi + "(?:\\s*-\\s*|\\s+)");
        put("pRegBlackKeyUnIndi", blackkey_unindi);
        put("pRegBlackKeyNoSingal", blackkey_noSingal);
    }
}
