package com.huawei.g11n.tmr.address.it;

import com.huawei.g11n.tmr.util.Regexs;

public class ReguExp extends Regexs {
    private static final String White = "(Torre\\s{1,6}di\\s{1,6}Pisa|Pizza\\s{1,6}Hut|Colosseo\\s{1,6}di\\s{1,6}Roma|Museo\\s{1,6}archeologico|Campanile\\s{1,6}di\\s{1,6}Giotto|Battistero\\s{1,6}di\\s{1,6}San\\s{1,6}Giovanni|Harvard|Pantheon|Fontana\\s{1,6}di\\s{1,6}Trevi)|";
    private static String blackBuilding = "(in|a)\\s([A-Z][a-z,A-Zè€éòóìàâe'ù]+)(\\s|\\.|,)?";
    private static String blackStreet = "(in|a)\\s([A-Z][a-z,A-Zè€éòóìàâe'ù]+)(\\s|\\.|,)?";
    private static String buildkeyED_New = "((?i)(Villa|piazzale|Garden|Giardino|Farmacia|Stazione|Aeroporto|Piazza|Parco|Lago|Basilica|Politecnico|Liceo|Scuola|Accademia|Chiesa|Duomo|Hotel|Locanda|Trattoria|Osteria|Quadrato|Mercato|Cattedrale|College|Collegio|Facoltà|Parrocchia|Mura|Teatro|Palazzo|Museo|Ufficio Postale|Aula|Sartoria|Cantina|Questura|Stadio|Municipio|Cinema|Ospedale|Spiaggia|Palestra|Bar|Banca|Ristorante|Pasticceria|Lido|Porto|Carica|Libreria|Biblioteca|Chiostro|Cantiere|Sala|Bottega|Laboratorio|Torre|Resort|Fotogallery|Pinacoteca|Reggia|Ponte|Zoo|Centro|Centrale|Istituto|Caffetteria|Tribunale|Campanile|Castello|Giardino|Galleria|Club|Casa)\\s)";
    private static String buildkeyED_Rest = "((?i)(Colosseo|Supermarcheto|Drugstore|Piscina|Stagno|Laghetto|Parrochia|Guesthouse|Località|Magazzino|Ateneo|Cafferia|Refettorio|Corporation|Monte|Opere\\s{1,6}Teatrali|Arte\\s{1,6}Drammatica|Casetta\\s{1,6}Giocattolo|Conversario|Nosocomio|Mensa|Parrucchiere|Confettiere|Autostazione|Fermata|Edicola|Chiosco|Cinematografo|Vestibolo|Tavola\\s{1,6}Fredda|Fioraio|Parcheggio|Agriturismo|Banchina|Cartoleria|Ginnasio|Barra|Sbarra|Barretta|Pontile|Imbarcadero|Lavanderia|Bucato|Arsenale|Panetteria|Fruttivendolo|Pescheria|Macelleria|Molo|Concerti|Bettola|Negozio|Panificio|Spaccio|Apartamento|Dimora|Motel|Taverna|Ostello|Asilo|Bistro|Clinica|Tavola\\s{1,6}Calda|Ufficio|pensione|Centrali|Albergo|Caffè|cartoleria|Università)\\s)";
    private static String citykey = "(\\s{0,6}(in|,)\\s{0,6})?([,-]?\\s?(IT-\\d{1,5}|I-\\d{1,5}|\\d{1,5})\\s?[,-]?)?\\s*(?:((?i)Milanello|CATANIA|Cortina\\s{1,6}D'Ampezzo|Bologna|Genova\\s{1,6}Pegli|Cortona|Città\\s{1,6}del\\s{1,6}Vaticano|Courmayeur|Cuneo|Abano\\s{1,6}Terme|Abruzzo\\s{0,6}-\\s{0,6}Teramo|Agrigento|Conca\\s{1,6}dei\\s{1,6}Marini|Badia|Roma|Alberobello|Alessandria\\s{1,6}&\\s{1,6}Monferrato|Amalfi\\s{1,6}Coast|Ancona|Aosta\\s{0,6}-\\s{0,6}Courmayeur|Monterosso|Sorrento|Aosta|Arezzo\\s{1,6}Province|Arezzo|Ascoli\\s{1,6}Piceno|Assisi|Avellino|Bari|Bergamo|Brescia|Brindisi|Calabria\\s{1,6}Seaside|Capri\\s{1,6}Island|Carpi|Caserta|Castiglioncello|Cefalu|Chianciano\\s{1,6}Terme|Cinque\\s{1,6}Terre|Como|Elba\\s{1,6}Island|Fabriano|Fano|Ferrara|Florence\\s{1,6}Province|Florence|Foligno|Forli|Garda\\s{1,6}Lake|Genova|Grosseto|Gubbio|Ischia\\s{1,6}Island|Italian\\s{1,6}Riviera|La\\s{1,6}Spezia|L'Aquila|Lecco|Livorno|Lucca|Macerata|Maggiore\\s{1,6}and\\s{1,6}Orta\\s{1,6}Lakes|Maratea|Matera|Melfi|Messina|Milan\\s{1,6}Province|Milano|Milan|Modena|Montecatini\\s{1,6}Terme|Montepulciano|Naples|Ostuni|Padova|Paestum|Palermo|Parma|Perugia|Pescasseroli|Piacenza|Pisa|Porto\\s{1,6}San\\s{1,6}Giorgio|Portofino\\s{1,6}and\\s{1,6}Tigullio|Positano|Prato|Puglia\\s{1,6}Seaside|Ragusa|Ravenna|Reggio\\s{1,6}Emilia|Riccione|Rieti|Rimini|Rome\\s{1,6}Province|Salerno|San\\s{1,6}Gimignano|San\\s{1,6}Giovanni\\s{1,6}Rotondo|Sardinia|Sicily|Siena|Siracusa|Sorrento|Spoleto|Taormina|Terni|The\\s{1,6}Alps\\s{0,6}-\\s{0,6}Dolomiti|Tirrenian\\s{1,6}Sea\\s{1,6}Coast|Torino|Trapani|Trento|Treviso|Trieste|Tropea|Turin|Tuscany\\s{0,6}-\\s{0,6}Chianti|Varese|Venice\\s{1,6}Province|Venice|Verona|Versilia|RomaInizio|Vicenza|Wine\\s{1,6}Route|Rome)(?:\\s*\\([A-Z]{2}\\))?|([(]?[A-Z][A-Z][)]?))[,.!]*";
    private static String country = "[,-]?\\s?(Italia)";
    private static String prepandwords = "((nella|nei|per|nello|negli|nelle|nel|nell'|al|ai|allo|agli|alla|alle|del|dei|dello|degli|della|delle|sul|sui|sullo|sugli|sulla|sulle|dalla|dai|dallo|dagli|dalle|dal|S\\.|di|dì|a|su|est|da)\\s+([A-Z][a-z,A-Zè€éòóìàâe'ù]*\\s*)|([A-Z][a-zA-Zè€éòóìàâe'ù]*\\s*)){1,4}(in\\s+([A-Z][a-zA-Zè€éòóìàâe'ù]*\\s*){1,4})?";
    static String prepandwords_ST = "((nella|nei|per|nello|negli|nelle|nel|nell'|al|ai|allo|agli|alla|alle|del|dei|dello|degli|della|delle|sul|sui|sullo|sugli|sulla|sulle|dalla|dai|dallo|dagli|dalle|dal|S\\.|di|dì|su|est|da)\\s+([A-Z][a-z,A-Zè€éòóìàâe'ù]*\\s*)|([A-Z][a-zA-Zè€éòóìàâe'ù]*\\s*)){1,4}((in|a)\\s+([A-Z][a-zA-Zè€éòóìàâe'ù]*\\s*){1,4})?";
    private static String regED1 = new StringBuilder(White).append(buildkeyED_New).append("(").append(prepandwords).append(")").toString();
    private static String regED2 = new StringBuilder(White).append(buildkeyED_Rest).append("(").append(words).append(")").toString();
    private static String regST = ("(\\s{0,6},\\s{0,6})?(" + stresskey1_New + "(" + prepandwords_ST + ")|(" + stresskey1_Rest + words + "))([,-]{0,1}\\s{0,6}(?<!\\d)\\d{1,4}(?!\\d)\\s{0,6}(-\\d{1,4})?)?([,-]*\\s*(IT-(?<!\\d)\\d{5}(?!\\d)|I-(?<!\\d)\\d{5}(?!\\d)|(?<!\\d)\\d{5}(?!\\d))\\s*[,-]*)?");
    private static String region = "\\s?(Abruzzi|Basilicata|Calabria|Campania|Emilia\\s{0,6}-\\s{0,6}Romagna|Friuli\\s{0,6}-\\s{0,6}Venezia\\s{1,6}Giulia|Lazio|Liguria|Lombardia|Marche|Molise|Piemonte|Puglia|Sardegna|Sicilia|Toscana|Trentino\\s{0,6}-\\s{0,6}Alto\\s{1,6}Adige|Umbria|Valle\\s{1,6}d'Aosta|Veneto)";
    private static String stresskey1_New = "((?i)(Via|Viale|Corso|Strada|Vicolo)\\s)";
    private static String stresskey1_Rest = "((?i)(Stradone)\\s)";
    private static String words = "(\\s*([A-Z][a-zA-Zè€éòóìàâe'ù]*\\s*)){1,4}([,-]|\\s)*";

    public void init() {
        put("patternED", regED1);
        put("patternEDnopre", regED2);
        put("patternPCcity", citykey);
        put("patternregion", region);
        put("patterncountry", country);
        put("patternST", regST);
        put("patternbb", blackBuilding);
        put("patternbs", blackStreet);
    }
}
