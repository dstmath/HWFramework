package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_cs extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "Klíč zprávy ''{0}'' není obsažen ve třídě zpráv ''{1}''"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "Formát zprávy ''{0}'' ve třídě zpráv ''{1}'' selhal. "};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "Třída serializace ''{0}'' neimplementuje obslužný program org.xml.sax.ContentHandler."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "Nelze najít zdroj [ {0} ].\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "Nelze zavést zdroj [ {0} ]: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Velikost vyrovnávací paměti <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "Byla zjištěna neplatná náhrada UTF-16: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "Chyba vstupu/výstupu"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "Nelze přidat atribut {0} po uzlech potomků ani před tím, než je vytvořen prvek. Atribut bude ignorován."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "Obor názvů pro předponu ''{0}'' nebyl deklarován."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "Atribut ''{0}'' se nachází vně prvku."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "Deklarace oboru názvů ''{0}''=''{1}'' se nachází vně prvku."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "Nelze zavést prostředek ''{0}'' (zkontrolujte proměnnou CLASSPATH) - budou použity pouze výchozí prostředky"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Byl proveden pokus o výstup znaku s celočíselnou hodnotou {0}, která není reprezentována v určeném výstupním kódování {1}."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "Nelze načíst soubor vlastností ''{0}'' pro výstupní metodu ''{1}'' (zkontrolujte proměnnou CLASSPATH)."};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Neplatné číslo portu."};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "Má-li hostitel hodnotu null, nelze nastavit port."};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "Adresa hostitele má nesprávný formát."};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "Schéma nevyhovuje."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Nelze nastavit schéma řetězce s hodnotou null."};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "Cesta obsahuje neplatnou escape sekvenci"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "Cesta obsahuje neplatný znak: {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "Fragment obsahuje neplatný znak."};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "Má-li cesta hodnotu null, nelze nastavit fragment."};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Fragment lze nastavit jen u generického URI."};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "V URI nebylo nalezeno žádné schéma"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "URI nelze inicializovat s prázdnými parametry."};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Fragment nelze určit zároveň v cestě i ve fragmentu."};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "V řetězci cesty a dotazu nelze zadat řetězec dotazu."};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "Není-li určen hostitel, nelze zadat port."};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Není-li určen hostitel, nelze zadat údaje o uživateli."};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Varování: Je požadována verze ''{0}'' výstupního dokumentu. Tato verze formátu XML není podporována. Bude použita verze ''1.0'' výstupního dokumentu. "};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "Je vyžadováno schéma!"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "Objekt vlastností předaný faktorii SerializerFactory neobsahuje vlastnost ''{0}''. "};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Varování: Kódování ''{0}'' není v běhovém prostředí Java podporováno."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "Parametr ''{0}'' nebyl rozpoznán."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "Parametr ''{0}'' byl rozpoznán, ale nelze nastavit požadovanou hodnotu."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "Výsledný řetězec je příliš dlouhý pro řetězec DOMString: ''{0}''."};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "Typ hodnoty pro tento název parametru není kompatibilní s očekávaným typem hodnoty."};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "Cílové umístění výstupu pro data určená k zápisu je rovno hodnotě Null. "};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "Bylo nalezeno nepodporované kódování."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "Nelze provést serializaci uzlu. "};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "Sekce CDATA obsahuje jednu nebo více ukončovacích značek ']]>'."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "Nelze vytvořit instanci modulu pro kontrolu správného utvoření. Parametr správného utvoření byl nastaven na hodnotu true, nepodařilo se však zkontrolovat správnost utvoření. "};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "Uzel ''{0}'' obsahuje neplatné znaky XML. "};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "V poznámce byl zjištěn neplatný znak XML (Unicode: 0x{0})."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "V datech instrukce zpracování byl nalezen neplatný znak XML (Unicode: 0x{0})."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "V oddílu CDATASection byl nalezen neplatný znak XML (Unicode: 0x{0})."};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "V obsahu znakových dat uzlu byl nalezen neplatný znak XML (Unicode: 0x{0})."};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "V objektu {0} s názvem ''{1}'' byl nalezen neplatný znak XML. "};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "V poznámkách není povolen řetězec \"--\"."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "Hodnota atributu \"{1}\" souvisejícího s typem prvku \"{0}\" nesmí obsahovat znak ''<''."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "Odkaz na neanalyzovanou entitu \"&{0};\" není povolen."};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "Externí odkaz na entitu \"&{0};\" není v hodnotě atributu povolen."};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "Předpona \"{0}\" nesmí být vázaná k oboru názvů \"{1}\"."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "Lokální název prvku \"{0}\" má hodnotu Null. "};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "Lokální název atributu \"{0}\" má hodnotu Null. "};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "Nový text uzlu entity \"{0}\" obsahuje uzel prvku \"{1}\" s nesvázanou předponou \"{2}\"."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "Nový text uzlu entity \"{0}\" obsahuje uzel atributu \"{1}\" s nesvázanou předponou \"{2}\". "};
        return contents;
    }
}
