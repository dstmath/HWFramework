package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_sk extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "Kľúč správy ''{0}'' sa nenachádza v triede správ ''{1}''"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "Zlyhal formát správy ''{0}'' v triede správ ''{1}''."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "Trieda serializátora ''{0}'' neimplementuje org.xml.sax.ContentHandler."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "Prostriedok [ {0} ] nemohol byť nájdený.\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "Prostriedok [ {0} ] sa nedal načítať: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Veľkosť vyrovnávacej pamäte <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "Bolo zistené neplatné nahradenie UTF-16: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "chyba IO"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "Nie je možné pridať atribút {0} po uzloch potomka alebo pred vytvorením elementu.  Atribút bude ignorovaný."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "Názvový priestor pre predponu ''{0}'' nebol deklarovaný."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "Atribút ''{0}'' je mimo prvku."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "Deklarácia názvového priestoru ''{0}''=''{1}'' je mimo prvku."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "Nebolo možné zaviesť ''{0}'' (skontrolujte CLASSPATH), teraz sa používajú iba štandardné nastavenia"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Pokus o výstup znaku integrálnej hodnoty {0}, ktorá nie je reprezentovaná v zadanom výstupnom kódovaní {1}."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "Nebolo možné zaviesť súbor vlastností ''{0}'' pre výstupnú metódu ''{1}'' (skontrolujte CLASSPATH)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Neplatné číslo portu"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "Nemôže byť stanovený port, ak je hostiteľ nulový"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "Hostiteľ nie je správne formátovaná adresa"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "Nezhodná schéma."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Nie je možné stanoviť schému z nulového reťazca"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "Cesta obsahuje neplatnú únikovú sekvenciu"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "Cesta obsahuje neplatný znak: {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "Fragment obsahuje neplatný znak"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "Ak je cesta nulová, nemôže byť stanovený fragment"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Fragment môže byť stanovený len pre všeobecné URI"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "V URI nebola nájdená žiadna schéma"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "Nie je možné inicializovať URI s prázdnymi parametrami"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Fragment nemôže byť zadaný v ceste, ani vo fragmente"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "Reťazec dotazu nemôže byť zadaný v ceste a reťazci dotazu"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "Ak nebol zadaný hostiteľ, možno nebol zadaný port"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Ak nebol zadaný hostiteľ, možno nebolo zadané userinfo"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Varovanie:  Verzia výstupného dokumentu musí byť povinne ''{0}''.  Táto verzia XML nie je podporovaná.  Verzia výstupného dokumentu bude ''1.0''."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "Je požadovaná schéma!"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "Objekt Properties, ktorý prešiel do SerializerFactory, nemá vlastnosť ''{0}''."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Varovanie:  Java runtime nepodporuje kódovanie ''{0}''."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "Parameter ''{0}'' nebol rozpoznaný."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "Parameter ''{0}'' bol rozpoznaný, ale vyžadovaná hodnota sa nedá nastaviť."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "Výsledný reťazec je príliš dlhý a nezmestí sa do DOMString: ''{0}''."};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "Typ hodnoty pre tento názov parametra je nekompatibilný s očakávaným typom hodnoty."};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "Cieľ výstupu pre zapísanie údajov bol null."};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "Bolo zaznamenané nepodporované kódovanie."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "Uzol nebolo možné serializovať."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "Časť CDATA obsahuje jeden alebo viaceré označovače konca ']]>'."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "Nebolo možné vytvoriť inštanciu kontrolóra Well-Formedness.  Parameter well-formed bol nastavený na hodnotu true, ale kontrola well-formedness sa nedá vykonať."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "Uzol ''{0}'' obsahuje neplatné znaky XML."};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "V komentári bol nájdený neplatný znak XML (Unicode: 0x{0})."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "Pri spracovaní dát inštrukcií sa našiel neplatný znak XML (Unicode: 0x{0})."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "V obsahu CDATASection sa našiel neplatný znak XML (Unicode: 0x{0})."};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "V obsahu znakových dát uzla sa našiel neplatný znak XML (Unicode: 0x{0})."};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "V uzle {0} s názvom ''{1}'' sa našiel neplatný znak XML."};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "Reťazec \"--\" nie je povolený v rámci komentárov."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "Hodnota atribútu \"{1}\", ktorá je priradená k prvku typu \"{0}\", nesmie obsahovať znak ''<''."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "Neanalyzovaný odkaz na entitu \"&{0};\" nie je povolený."};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "Odkaz na externú entitu \"&{0};\" nie je povolený v hodnote atribútu."};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "Predpona \"{0}\" nemôže byť naviazaná na názvový priestor \"{1}\"."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "Lokálny názov prvku \"{0}\" je null."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "Lokálny názov atribútu \"{0}\" je null."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "Náhradný text pre uzol entity \"{0}\" obsahuje uzol prvku \"{1}\" s nenaviazanou predponou \"{2}\"."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "Náhradný text uzla entity \"{0}\" obsahuje uzol atribútu \"{1}\" s nenaviazanou predponou \"{2}\"."};
        return contents;
    }
}
