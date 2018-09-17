package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_sl extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "Ključ sporočila ''{0}'' ni v rezredu sporočila ''{1}''"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "Format sporočila ''{0}'' v razredu sporočila ''{1}'' je spodletel."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "Razred serializerja ''{0}'' ne izvede org.xml.sax.ContentHandler."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "Vira [ {0} ] ni mogoče najti.\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "Sredstva [ {0} ] ni bilo mogoče naložiti: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Velikost medpomnilnika <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "Zaznan neveljaven nadomestek UTF-16: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "Napaka V/I"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "Atributa {0} ne morem dodati za podrejenimi vozlišči ali pred izdelavo elementa.  Atribut bo prezrt."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "Imenski prostor za predpono ''{0}'' ni bil naveden."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "Atribut ''{0}'' je zunaj elementa."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "Deklaracija imenskega prostora ''{0}''=''{1}'' je zunaj elementa."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "Ni bilo mogoče naložiti ''{0}'' (preverite CLASSPATH), trenutno se uporabljajo samo privzete vrednosti"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Poskus izpisa znaka integralne vrednosti {0}, ki v navedenem izhodnem kodiranju {1} ni zastopan."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "Datoteke z lastnostmi ''{0}'' ni bilo mogoče naložiti za izhodno metodo ''{1}'' (preverite CLASSPATH)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Neveljavna številka vrat"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "Ko je gostitelj NULL, nastavitev vrat ni mogoča"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "Naslov gostitelja ni pravilno oblikovan"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "Shema ni skladna."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Ni mogoče nastaviti sheme iz niza NULL"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "Pot vsebuje neveljavno zaporedje za izhod"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "Pot vsebuje neveljaven znak: {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "Fragment vsebuje neveljaven znak"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "Ko je pot NULL, nastavitev fragmenta ni mogoča"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Fragment je lahko nastavljen samo za splošni URI"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "Ne najdem sheme v URI"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "Ni mogoče inicializirati URI-ja s praznimi parametri"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Fragment ne more biti hkrati naveden v poti in v fragmentu"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "Poizvedbeni niz ne more biti naveden v nizu poti in poizvedbenem nizu"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "Vrata ne morejo biti navedena, če ni naveden gostitelj"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Informacije o uporabniku ne morejo biti navedene, če ni naveden gostitelj"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Opozorilo: Zahtevana različica izhodnega dokumenta je ''{0}''.  Ta različica XML ni podprta.  Različica izhodnega dokumenta bo ''1.0''."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "Zahtevana je shema!"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "Predmet Properties (lastnosti), ki je prenešen v SerializerFactory, nima lastnosti ''{0}''."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Opozorilo:  Izvajalno okolje Java ne podpira kodiranja ''{0}''."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "Parameter ''{0}'' ni prepoznan."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "Parameter ''{0}'' je prepoznan, vendar pa zahtevane vrednosti ni mogoče nastaviti."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "Nastali niz je predolg za DOMString: ''{0}''."};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "Tip vrednosti za to ime parametra je nezdružljiv s pričakovanim tipom vrednosti."};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "Izhodno mesto za vpisovanje podatkov je bilo nič."};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "Odkrito je nepodprto kodiranje."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "Vozlišča ni mogoče serializirati."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "Odsek CDATA vsebuje enega ali več označevalnikov prekinitve ']]>'."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "Primerka preverjevalnika Well-Formedness ni bilo mogoče ustvariti.  Parameter well-formed je bil nastavljen na True, ampak ni mogoče preveriti well-formedness."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "Vozlišče ''{0}'' vsebuje neveljavne znake XML."};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "V komentarju je bil najden neveljaven XML znak (Unicode: 0x{0})."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "V podatkih navodila za obdelavo je bil najden neveljaven znak XML (Unicode: 0x{0})."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "V vsebini odseka CDATASection je bil najden neveljaven znak XML (Unicode: 0x{0})."};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "V podatkovni vsebini znaka vozlišča je bil najden neveljaven znak XML (Unicode: 0x{0})."};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "V vozlišču {0} z imenom ''{1}'' je bil najden neveljaven znak XML."};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "Niz \"--\" ni dovoljen v komentarjih."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "Vrednost atributa \"{1}\", ki je povezan s tipom elementa \"{0}\", ne sme vsebovati znaka ''<''."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "Nerazčlenjeni sklic entitete \"&{0};\" ni dovoljen."};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "Zunanji sklic entitete \"&{0};\" ni dovoljen v vrednosti atributa."};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "Predpona \"{0}\" ne more biti povezana z imenskim prostorom \"{1}\"."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "Lokalno ime elementa \"{0}\" je nič."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "Lokalno ime atributa \"{0}\" je nič."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "Besedilo za zamenjavo za vozlišče entitete \"{0}\" vsebuje vozlišče elementa \"{1}\" z nevezano predpono \"{2}\"."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "Besedilo za zamenjavo za vozlišče entitete \"{0}\" vsebuje vozlišče atributa \"{1}\" z nevezano predpono \"{2}\"."};
        return contents;
    }
}
