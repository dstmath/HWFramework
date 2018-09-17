package com.huawei.g11n.tmr.address.fr;

import com.huawei.g11n.tmr.util.Regexs;

public class ReguExp extends Regexs {
    private static String bigLetter = "[A-ZÀÁÂÄÇÈÉÊËÌÎÏÚÛÜÙÒÓÔÖQÔ]";
    private static String blank = "(?:\\s*-\\s*|\\s*,\\s*|\\s+)";
    private static String blank1 = "(?:\\s+)";
    private static String blank2 = "(?:-|\\s+)";
    private static String boundL = "(?:(?<![a-zA-ZāáâàōóòöôêëéèīíîìïûūúùǖǘǜüÀÁÂÄÇÈÉÊËÌÎÏÚÛÜÙÒÓÔœŒÖç0-9&_@()'’-]))";
    private static String letter = "[a-zA-ZāáâàōóòöôêëéèīíîìïûūúùǖǘǜüÀÁÂÄÇÈÉÊËÌÎÏÚÛÜÙÒÓÔœŒÖçQÔ]";
    private static String letterNum = "[a-zA-ZāáâàōóòöôêëéèīíîìïûūúùǖǘǜüÀÁÂÄÇÈÉÊËÌÎÏÚÛÜÙÒÓÔœŒÖçQÔ0-9]";
    private static String number = "(\\d{1,5}\\b)|(\\d{1,4}(\\s*-\\s*|\\s+)\\d{1,4})|(\\d{1,3}(\\s+et\\s+)\\d{1,3})|(L\\d{1,2})|(\\d{1,2}e)|(\\d{1,3}bis)";
    private static String pArt2 = "((((?i)la|Les|le)\\s)+(?i)|l'|l’)";
    private static String pBui = (boundL + "(" + pArt2 + ")" + "?" + pBui1 + blank1 + "(" + "((?i)de|des)" + blank1 + pArt2 + word + "(?:" + "(?:-|\\s+)" + word + ")" + "{0,5}" + "(?:\\s+\\(CT\\))" + "?" + "|" + pPre3 + "(" + word + ")" + "(?:" + "(?:-|\\s+)" + word + ")" + "{0,5}" + "(?:\\s+\\(CT\\))" + "?" + "(" + blank1 + pPre3 + word + "(?:" + blank2 + word + ")" + "{0,4}" + ")" + "?" + "|" + word + "(?:" + "(?:-|\\s+)" + word + ")" + "{0,5}" + "(?:\\s+\\(CT\\))" + "?" + ")" + "(?:(-)" + "(?:" + letter + "*" + "-" + "?" + ")" + "{1,2})" + "?" + "((\\s+(et|st)\\s+)" + "(?:" + word + blank2 + "?" + ")" + "{1,3})" + "?");
    public static final String pBui1 = "((?i)Aéroport|Aeroporto|Champs-Elysees|Champs-Élysées|Club|carrés|Ecole|Exportations|Galeries|Galeries nationales|Gare|Garden|Institue|Institut|Jardin|Maternelle|Mosquee|Mosquée|Monument|Marché|Notre-Dame|Notre Dame|Pont|Parc|Park|Poste|Place|Quai|Station|Square|Studio|Université|Apartment|Aabbaye|Auberge|Basilique|Banque|Bibliothèque|Building|Boutique|Bar|Cathedrale|Cathédrale|Chapelle|Cafétéria|Château|Chateau|Cirque|Cafe|Café|Eglise|Gymnase|Hotel|Hôtel|Hôpital|Hopital|Hypermarché|Librairie|Maternelle|Musee|Musée|Mohegan|Magasin|Museum|Opéra|Opera|Palais|Palace|Restaurant|Residence|Supérette|Supermarché|Stade|Théâtre|Theater|Theatre|Taverne|Usine|Compagnie|Entreprise|Laverie)\\b";
    public static final String pBuiCity = (boundL + "((" + pBui + "(" + "(\\s+|\\s*,\\s*)" + pPre1 + "(#)?" + "(((?:" + word + "(\\s+|-|\\s*-#\\s*)" + "?" + ")" + "{0,2}" + "(" + word + "))" + "|" + "(\\(.*\\))" + ")" + "(\\s+(\\(.*\\)))?" + "(\\s*-\\s*)" + "(" + pPos1 + ")" + "(\\s+)" + "(?:" + pPre1 + ")" + "?" + "(?:" + word + blank2 + "?" + ")" + "{1,4}" + "|" + "(\\s+|\\s*,\\s*|/)" + "(" + pPre1 + ")?" + "(#)?" + "(((?:" + word + "(\\s+|-|\\s*-#\\s*)" + "?" + ")" + "{0,2}" + "(" + word + "))" + "|" + "(\\(.*\\))" + "|" + "(" + "(\\s*-\\s*)" + pPre2 + "(\\s+)" + "(?:" + wordSmall + "(-)" + "?" + ")" + "{1,3})" + ")" + "))" + "|" + pBui + "((\\s+|\\s*,\\s*|/)" + "(" + pPre1 + ")?" + "(" + pArt2 + ")?" + "(#)?" + "(((?:" + word + blank2 + "?" + ")" + "{0,2}" + "(" + word + "))" + "|" + "(\\(.*\\))" + "|" + "(" + "(\\s*-\\s*)" + pPre2 + "(\\s+)" + "(?:" + wordSmall + "(-)" + "?" + ")" + "{1,3})" + ")" + "(" + blank1 + pPre1 + word + ")?)?" + ")");
    private static String pBuiStrCity = (boundL + "(?:" + pBui + ")" + "(" + "(" + "(?:\\s+|\\s*,\\s*)" + "(" + pStr + ")" + "(?:\\s+|\\s*,\\s*)" + pPre1 + "?" + "(?:" + word + blank2 + "?" + ")" + "{1,2}" + ")" + "|" + "(?:\\s+|\\s*,\\s*)" + "(?:" + pStr + ")" + "(?:\\s+|\\s*-\\s*)" + "(?:" + "(?:" + pPos1 + ")" + "(?:\\s+)" + "(?:" + word + "(\\s+)" + "?" + ")" + "{1,2})" + "|" + "(?:\\s+|\\s*,\\s*|\\s*-\\s*)" + "(?:" + pStr + ")" + ")");
    private static String pBuiStrPos = (boundL + pBui + "(?:\\s+|\\s*,\\s*)" + "(" + pStr + ")" + blank1 + "(" + pPos1 + ")(" + blank1 + "(?:" + word + blank2 + "?" + ")" + "{1,2})");
    private static String pCity = "(?:(?i)Amiens|Arras|Alençon|Auxerre|Angers|Annecy|Angoulême|Agen|Auch|Albi|Aurillac|Avignon|Ajaccio|Bobigny|Belfort|Bar-le-Duc|Bourges|Beauvais|Besançon|Blois|Bordeaux|Bourg-en-Bresse|Bastia|Basse-Terre|Créteil|Cayenne|Colmar|Chaumont|Chartres|Châteauroux|Châlons-en-Champagne|Charleville-Mézières|Caen|Cahors|Chambéry|Clerment-Ferrand|Carcassonne|Dijon|Digne|Evry|Évreux|Épinal|Guéret|Grenoble|Gap|Foix|Fort-de-France|Laon|Lille|Laval|Le Mans|La Roche-sur-Yon|Lons-le-Saunier|La Rochelle|Limoges|Lyon|Le Puy|Melun|Metz|Macon|Montauban|Mende|Nanterre|Mont-de-Marsan|Moulins|Montpellier|Marseille|Nancy|Nevers|Nîmes|Nimes|Nice|Nantes|Niort|Orléans|Ouville|Paris|Pontoise|Poitiers|Pau|Privas|Perpignan|Pamiers|Quimper|Rouen|Rennes|Rodez|Saint-Lô|Saint-Brieuc|Strasbourg|Saint-Étienne|Saint-Denis|Tours|Troyes|Toulon|Toulouse|Tarbes|Tulle|Versailles|Vesoul|Vannes|Valence)";
    private static String pPos = (boundL + "\\(\\d{5}\\)");
    private static String pPos1 = "\\b\\d{5}\\b|\\(?:\\d{5}\\)";
    public static final String pPosCity = (boundL + "(" + "[A-ZÀÁÂÄÇÈÉÊËÌÎÏÚÛÜÙÒÓÔÖQ](?:(?!- |:| #).)+\\s+[a-zA-ZāáâàōóòöôêëéèīíîìïûūúùǖǘǜüÀÁÂÄÇÈÉÊËÌÎÏÚÛÜÙÒÓÔœŒÖçQ0-9]+\\s+\\([0-9]{5}\\)" + "|" + "[A-ZÀÁÂÄÇÈÉÊËÌÎÏÚÛÜÙÒÓÔÖQ](?:(?!- |:| #).)+\\s+\\([0-9]{5}\\)" + "|" + pCity + "(\\s+)" + pPos + ")");
    private static String pPre1 = "(?:(?:(?i)aux|àux|à|au|àu|en|pour|de|des|du|d'|d’|dans)\\s+|d'|D’|D'|d’)";
    private static String pPre2 = "(?:aux|à|au|de|du|dans|devant|des)";
    private static String pPre3 = "(?:(?:(?i)de|des|du|D'|D’)\\s+|d'|D’|D'|d’)";
    private static String pPreAll = "(?:(?:(?i)aux|àux|à|au|àu|en|dans|pour|par|chez|vers|sous|entre|devant|sur|avant|contre|derrière|près de|hors de|du|de|des|at|in|on)\\s+|d'|D’|D'|d’)";
    public static final String pSpe = "(?:(?:(?i)la|Les|le)\\s+(?i)|l'|l’|\\s*,\\s*|\\s*-\\s*)\\s*(.*)";
    private static String pStr = ("\\b((" + number + ")" + ")?" + "(" + "(\\s*|\\s*,\\s*|\\s*，\\s*)" + pStr1 + blank1 + pPreAll + pArt2 + pStr1 + blank1 + pPre3 + "?" + pArt2 + "?" + "(?:" + word + blank2 + "?" + ")" + "{1,3}" + "(" + "\\s+" + "et\\s+" + "(?:" + word + "(\\s*-\\s*|\\s+)" + "?" + ")" + "{1,3})" + "?" + "(/" + "(?:" + word + "(\\s*-\\s*|\\s+)" + "?" + ")" + "{1,2})?" + "|" + "((\\s+)" + "(bis\\s+))?" + "(\\s*|\\s*,\\s*|\\s*，\\s*)" + pStr1 + blank1 + "((?i)(de|des))" + blank1 + pArt2 + "(?:" + word + blank2 + "?" + ")" + "{1,3}" + "(/" + "(?:" + word + "(\\s*-\\s*|\\s+)" + "?" + ")" + "{1,2})?" + "|" + "(\\s*|\\s*,\\s*|\\s*，\\s*)" + pStr1 + blank1 + pPre3 + "(?:" + word + blank2 + "?" + ")" + "{1,3}" + "(" + "\\s+" + pPre3 + "(?:" + word + blank2 + "?" + ")" + "{1,3})" + "?" + "(/" + "(?:" + word + "(\\s*-\\s*|\\s+)" + "?" + ")" + "{1,2})?" + "|" + "(\\s*|\\s*,\\s*|\\s*，\\s*)" + pStr1 + blank1 + pPre3 + "(?:" + word + blank2 + "?" + ")" + "{1,3}" + "(" + "\\s+" + pPre3 + "(?:" + word + blank2 + "?" + ")" + "{1,3})" + "?" + "(/" + "(?:" + word + "(\\s*-\\s*|\\s+)" + "?" + ")" + "{1,2})?" + "|" + "((\\s+)" + "(bis\\s+))?" + "(\\s*|\\s*,\\s*|\\s*，\\s*)" + pStr1 + blank1 + pPre3 + "(?:" + word + blank2 + "?" + ")" + "{1,3}" + "(/" + "(?:" + word + "(\\s*-\\s*|\\s+)" + "?" + ")" + "{1,2})?" + "|" + "(\\s*|\\s*,\\s*|\\s*，\\s*)" + pStr1 + blank1 + pPre3 + "(?:" + word + blank2 + "?" + ")" + "{1,3}" + "(/" + "(?:" + word + "(\\s*-\\s*|\\s+)" + "?" + ")" + "{1,2})?" + "|" + "((\\s+)" + "(bis)" + "(\\s+))?" + "(\\s*|\\s*,\\s*|\\s*，\\s*)" + pStr1 + blank1 + "(?:" + word + blank2 + "?" + ")" + "{1,4}" + "(" + "\\s+" + pPre3 + "(?:" + word + blank2 + "?" + ")" + "{1,3})" + "?" + "(/" + "(?:" + word + "(\\s*-\\s*|\\s+)" + "?" + ")" + "{1,2})?" + ")");
    public static final String pStr1 = "(?:(?i)Avenue|Ave|chaussée|chemin de fer|chemin|route|rue|Street|voie|bd|boulevard)";
    public static final String pStrCity = (boundL + "((" + pStr + "(" + "(?:\\s+|\\s*,\\s*)" + pPre1 + "#?" + "(?:" + word + "(-)" + "?" + ")" + "{0,2}" + word + "(?:" + wordSmall + "(-)" + "?" + ")" + "{1,3}" + "|" + "(\\s+)" + pPreAll + pArt2 + "(\\d{1,2}+ème)" + "))" + "|" + pStr + "((?:\\s+|\\s*,\\s*)" + "(?:" + pPre1 + ")?" + "#?" + "(?:" + word + "(\\s+|-)" + "?" + ")" + "{0,2}" + word + "(?:" + wordSmall + "(-)" + "?" + ")" + "{1,3})?" + ")");
    private static String pStrPos = (boundL + "(" + "(" + pStr + ")" + blank1 + "BP" + blank1 + "(" + pPos1 + ")" + blank1 + "PO" + blank1 + "(" + pPos1 + ")" + blank1 + "(?:" + word + blank2 + "?" + ")" + "{1,3}" + "|" + "(" + pStr + ")" + blank1 + "BP" + blank1 + "(" + pPos1 + ")" + blank1 + "(?:" + word + blank2 + "?" + ")" + "{1,3}" + blank + "PO" + blank1 + "(" + pPos1 + ")" + blank1 + "(?:" + word + blank2 + "?" + ")" + "{1,2}" + blank1 + "\\d{1,3}" + blank1 + "(?:" + word + blank2 + "?" + ")" + "{1,2}" + "|" + "(" + pArt2 + ")" + "(?:" + word + blank2 + "?" + ")" + "{1,2}" + blank1 + "(" + pPos1 + ")" + blank1 + "(?:" + word + blank2 + "?" + ")" + "{1,3}" + "|" + "(" + pStr + ")" + "(\\s+|\\s*-\\s*)" + "(" + pPos1 + ")" + "(\\s+|\\s*-\\s*)" + "(?:(?:" + word + blank2 + "?" + ")" + "{1,2})" + "?" + "((?:\\s+)" + "\\b\\d{1,2}" + "(?:\\s+)" + "(?:eme|ème)" + "(?:\\s+)" + "(?:étage))" + "|" + "(" + pStr + ")" + "(\\s+|\\s*-\\s*|\\s*,\\s*)" + "(" + pPos1 + ")" + "(?:(\\s+|\\s*-\\s*)" + "(?:" + word + "(\\s+|-)" + "?" + ")" + "{1,3}" + "(\\s*" + pPre2 + "\\s+(" + word + "\\s*" + "){1,3}" + ")?" + ")" + "?" + ")");
    public static final String reg2 = (pBuiCity + "|" + pBuiStrPos + "|" + pBuiStrCity + "|" + pStrPos + "|" + pStrCity);
    public static final String reg2_noBui1 = (pStrPos + "|" + pStrCity);
    public static final String reg2_noPos = (pBuiStrCity + "|" + pBuiCity + "|" + pStrCity);
    private static String reg2_noStr1 = pBuiCity;
    private static String word = (bigLetter + letter + "*" + "(" + blank + "France" + ")?");
    private static String wordSmall = ("(?:" + letterNum + "*" + ")");

    public void init() {
        put("pBui1", pBui1);
        put("pStr1", pStr1);
        put("reg2", reg2);
        put("reg2_noPos", reg2_noPos);
        put("reg2_noStr1", reg2_noStr1);
        put("pBuiCity", pBuiCity);
        put("reg2_noBui1", reg2_noBui1);
        put("pStrCity", pStrCity);
        put("pPosCity", pPosCity);
        put("pSpe", pSpe);
        put("mPos1", "[0-9]{5}");
        put("pNumber", number);
        put("pBuiStrPos", pBuiStrPos);
        put("pBuiStrCity", pBuiStrCity);
        put("pStrPos", pStrPos);
        put("cat", "(\\s*,\\s*|\\s*)");
    }
}
