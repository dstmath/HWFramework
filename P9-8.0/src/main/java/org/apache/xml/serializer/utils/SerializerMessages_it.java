package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_it extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "La chiave messaggio ''{0}'' non si trova nella classe del messaggio ''{1}''"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "Il formato del messaggio ''{0}'' nella classe del messaggio ''{1}'' non è riuscito."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "La classe del serializzatore ''{0}'' non implementa org.xml.sax.ContentHandler."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "Risorsa [ {0} ] non trovata.\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "Impossibile caricare la risorsa [ {0} ]: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Dimensione buffer <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "Rilevato surrogato UTF-16 non valido: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "Errore IO"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "Impossibile aggiungere l''''attributo {0} dopo i nodi secondari o prima che sia prodotto un elemento.  L''''attributo verrà ignorato."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "Lo spazio nomi per il prefisso ''{0}'' non è stato dichiarato."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "L''''attributo ''{0}'' al di fuori dell''''elemento."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "Dichiarazione dello spazio nome ''{0}''=''{1}'' al di fuori dell''''elemento."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "Impossibile caricare ''{0}'' (verificare CLASSPATH), verranno utilizzati i valori predefiniti"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Tentare di generare l''''output del carattere di valor integrale {0} che non è rappresentato nella codifica di output specificata di {1}."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "Impossibile caricare il file delle proprietà ''{0}'' per il metodo di emissione ''{1}'' (verificare CLASSPATH)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Numero di porta non valido"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "La porta non può essere impostata se l'host è nullo"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "Host non è un'indirizzo corretto"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "Lo schema non è conforme."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Impossibile impostare lo schema da una stringa nulla"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "Il percorso contiene sequenza di escape non valida"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "Il percorso contiene un carattere non valido: {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "Il frammento contiene un carattere non valido"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "Il frammento non può essere impostato se il percorso è nullo"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Il frammento può essere impostato solo per un URI generico"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "Non è stato trovato alcuno schema nell'URI"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "Impossibile inizializzare l'URI con i parametri vuoti"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Il frammento non può essere specificato sia nel percorso che nel frammento"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "La stringa di interrogazione non può essere specificata nella stringa di interrogazione e percorso."};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "La porta non può essere specificata se l'host non S specificato"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Userinfo non può essere specificato se l'host non S specificato"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Attenzione:  La versione del documento di emissione è obbligatorio che sia ''{0}''.  Questa versione di XML non è supportata.  La versione del documento di emissione sarà ''1.0''."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "Lo schema è obbligatorio."};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "L''''oggetto Properties passato al SerializerFactory non ha una proprietà ''{0}''."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Avvertenza:  La codifica ''{0}'' non è supportata da Java runtime."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "Il parametro ''{0}'' non è riconosciuto."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "Il parametro ''{0}'' è riconosciuto ma non è possibile impostare il valore richiesto."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "La stringa risultante è troppo lunga per essere inserita in DOMString: ''{0}''."};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "Il tipo di valore per questo nome di parametro non è compatibile con il tipo di valore previsto."};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "La destinazione di output in cui scrivere i dati era nulla."};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "È stata rilevata una codifica non supportata."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "Impossibile serializzare il nodo."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "La Sezione CDATA contiene uno o più markers di termine ']]>'."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "Impossibile creare un'istanza del controllore Well-Formedness.  Il parametro well-formed è stato impostato su true ma non è possibile eseguire i controlli well-formedness."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "Il nodo ''{0}'' contiene caratteri XML non validi."};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "Trovato un carattere XML non valido (Unicode: 0x{0}) nel commento."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "Carattere XML non valido (Unicode: 0x{0}) rilevato nell''elaborazione di instructiondata."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "Carattere XML non valido (Unicode: 0x{0}) rilevato nel contenuto di CDATASection."};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "Carattere XML non valido (Unicode: 0x{0}) rilevato nel contenuto dati di caratteri del nodo. "};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "Carattere XML non valido rilevato nel nodo {0} denominato ''{1}''."};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "La stringa \"--\" non è consentita nei commenti."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "Il valore dell''''attributo \"{1}\" associato con un tipo di elemento \"{0}\" non deve contenere il carattere ''<''."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "Il riferimento entità non analizzata \"&{0};\" non è permesso."};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "Il riferimento all''''entità esterna \"&{0};\" non è permesso in un valore attributo."};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "Il prefisso \"{0}\" non può essere associato allo spazio nome \"{1}\"."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "Il nome locale dell''''elemento \"{0}\" è null."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "Il nome locale dell''''attributo \"{0}\" è  null."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "Il testo di sostituzione del nodo di entità \"{0}\" contiene un nodo di elemento \"{1}\" con un prefisso non associato \"{2}\"."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "Il testo di sostituzione del nodo di entità \"{0}\" contiene un nodo di attributo \"{1}\" con un prefisso non associato \"{2}\"."};
        return contents;
    }
}
