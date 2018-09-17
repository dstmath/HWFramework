package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_de extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "Der Nachrichtenschlüssel ''{0}'' ist nicht in der Nachrichtenklasse ''{1}'' enthalten."};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "Das Format der Nachricht ''{0}'' in der Nachrichtenklasse ''{1}'' ist fehlgeschlagen."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "Die Parallel-Seriell-Umsetzerklasse ''{0}'' implementiert org.xml.sax.ContentHandler nicht."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "Die Ressource [ {0} ] konnte nicht gefunden werden.\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "Die Ressource [ {0} ] konnte nicht geladen werden: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Puffergröße <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "Ungültige UTF-16-Ersetzung festgestellt: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "E/A-Fehler"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "Attribut {0} kann nicht nach Kindknoten oder vor dem Erstellen eines Elements hinzugefügt werden.  Das Attribut wird ignoriert."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "Der Namensbereich für Präfix ''{0}'' wurde nicht deklariert."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "Attribut ''{0}'' befindet sich nicht in einem Element."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "Namensbereichdeklaration ''{0}''=''{1}'' befindet sich nicht in einem Element."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "''{0}'' konnte nicht geladen werden (CLASSPATH prüfen). Es werden die Standardwerte verwendet."};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Es wurde versucht, ein Zeichen des Integralwerts {0} auszugeben, der nicht in der angegebenen Ausgabeverschlüsselung von {1} dargestellt ist."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "Die Merkmaldatei ''{0}'' konnte für die Ausgabemethode ''{1}'' nicht geladen werden (CLASSPATH prüfen)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Ungültige Portnummer"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "Der Port kann nicht festgelegt werden, wenn der Host gleich Null ist."};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "Der Host ist keine syntaktisch korrekte Adresse."};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "Das Schema ist nicht angepasst."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Schema kann nicht von Nullzeichenfolge festgelegt werden."};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "Der Pfad enthält eine ungültige Escapezeichenfolge."};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "Pfad enthält ungültiges Zeichen: {0}."};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "Fragment enthält ein ungültiges Zeichen."};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "Fragment kann nicht festgelegt werden, wenn der Pfad gleich Null ist."};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Fragment kann nur für eine generische URI (Uniform Resource Identifier) festgelegt werden."};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "Kein Schema gefunden in URI"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "URI (Uniform Resource Identifier) kann nicht mit leeren Parametern initialisiert werden."};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Fragment kann nicht im Pfad und im Fragment angegeben werden."};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "Abfragezeichenfolge kann nicht im Pfad und in der Abfragezeichenfolge angegeben werden."};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "Der Port kann nicht angegeben werden, wenn der Host nicht angegeben wurde."};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Benutzerinformationen können nicht angegeben werden, wenn der Host nicht angegeben wurde."};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Warnung: Die Version des Ausgabedokuments muss ''{0}'' lauten.  Diese XML-Version wird nicht unterstützt.  Die Version des Ausgabedokuments ist ''1.0''."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "Schema ist erforderlich!"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "Das an SerializerFactory übermittelte Merkmalobjekt weist kein Merkmal ''{0}'' auf."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Warnung:  Die Codierung ''{0}'' wird von Java Runtime nicht unterstützt."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "Der Parameter ''{0}'' wird nicht erkannt."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "Der Parameter ''{0}'' wird erkannt, der angeforderte Wert kann jedoch nicht festgelegt werden."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "Die Ergebniszeichenfolge ist zu lang für eine DOM-Zeichenfolge: ''{0}''."};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "Der Werttyp für diesen Parameternamen ist nicht kompatibel mit dem erwarteten Werttyp."};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "Das Ausgabeziel für die zu schreibenden Daten war leer."};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "Eine nicht unterstützte Codierung wurde festgestellt."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "Der Knoten konnte nicht serialisiert werden."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "Der Abschnitt CDATA enthält mindestens eine Beendigungsmarkierung ']]>'."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "Eine Instanz des Prüfprogramms für korrekte Formatierung konnte nicht erstellt werden.  Für den korrekt formatierten Parameter wurde der Wert 'True' festgelegt, die Prüfung auf korrekte Formatierung kann jedoch nicht durchgeführt werden."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "Der Knoten ''{0}'' enthält ungültige XML-Zeichen."};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "Im Kommentar wurde ein ungültiges XML-Zeichen (Unicode: 0x{0}) gefunden."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "In der Verarbeitungsanweisung wurde ein ungültiges XML-Zeichen (Unicode: 0x{0}) gefunden."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "Im Inhalt von CDATASection wurde ein ungültiges XML-Zeichen (Unicode: 0x{0}) gefunden."};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "Ein ungültiges XML-Zeichen  (Unicode: 0x{0}) wurde im Inhalt der Zeichendaten des Knotens gefunden."};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "Ungültige XML-Zeichen wurden gefunden in {0} im Knoten ''{1}''."};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "Die Zeichenfolge \"--\" ist innerhalb von Kommentaren nicht zulässig."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "Der Wert des Attributs \"{1}\" mit einem Elementtyp \"{0}\" darf nicht das Zeichen ''<'' enthalten."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "Der syntaktisch nicht analysierte Entitätenverweis \"&{0};\" ist nicht zulässig."};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "Der externe Entitätenverweis \"&{0};\" ist in einem Attributwert nicht zulässig."};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "Das Präfix \"{0}\" kann nicht an den Namensbereich \"{1}\" gebunden werden."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "Der lokale Name von Element \"{0}\" ist nicht angegeben."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "Der lokale Name des Attributs \"{0}\" ist nicht angegeben."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "Der Ersatztext des Entitätenknotens \"{0}\" enthält einen Elementknoten \"{1}\" mit einem nicht gebundenen Präfix \"{2}\"."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "Der Ersatztext des Entitätenknotens \"{0}\" enthält einen Attributknoten \"{1}\" mit einem nicht gebundenen Präfix \"{2}\"."};
        return contents;
    }
}
