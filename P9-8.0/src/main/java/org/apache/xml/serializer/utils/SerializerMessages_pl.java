package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_pl extends ListResourceBundle {
    public Object[][] getContents() {
        contents = new Object[59][];
        contents[0] = new Object[]{MsgKey.BAD_MSGKEY, "Klucz komunikatu ''{0}'' nie znajduje się w klasie komunikatów ''{1}''"};
        contents[1] = new Object[]{MsgKey.BAD_MSGFORMAT, "Nie powiodło się sformatowanie komunikatu ''{0}'' w klasie komunikatów ''{1}''."};
        contents[2] = new Object[]{MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER, "Klasa szeregująca ''{0}'' nie implementuje org.xml.sax.ContentHandler."};
        contents[3] = new Object[]{"ER_RESOURCE_COULD_NOT_FIND", "Nie można znaleźć zasobu [ {0} ].\n {1}"};
        contents[4] = new Object[]{"ER_RESOURCE_COULD_NOT_LOAD", "Zasób [ {0} ] nie mógł załadować: {1} \n {2} \t {3}"};
        contents[5] = new Object[]{"ER_BUFFER_SIZE_LESSTHAN_ZERO", "Wielkość buforu <=0"};
        contents[6] = new Object[]{"ER_INVALID_UTF16_SURROGATE", "Wykryto niepoprawny odpowiednik UTF-16: {0} ?"};
        contents[7] = new Object[]{"ER_OIERROR", "Błąd we/wy"};
        contents[8] = new Object[]{MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION, "Nie można dodać atrybutu {0} po bezpośrednich węzłach potomnych ani przed wyprodukowaniem elementu.  Atrybut zostanie zignorowany."};
        contents[9] = new Object[]{MsgKey.ER_NAMESPACE_PREFIX, "Nie zadeklarowano przestrzeni nazw dla przedrostka ''{0}''."};
        contents[10] = new Object[]{MsgKey.ER_STRAY_ATTRIBUTE, "Atrybut ''{0}'' znajduje się poza elementem."};
        contents[11] = new Object[]{MsgKey.ER_STRAY_NAMESPACE, "Deklaracja przestrzeni nazw ''{0}''=''{1}'' znajduje się poza elementem."};
        contents[12] = new Object[]{"ER_COULD_NOT_LOAD_RESOURCE", "Nie można załadować ''{0}'' (sprawdź CLASSPATH) - używane są teraz wartości domyślne"};
        contents[13] = new Object[]{MsgKey.ER_ILLEGAL_CHARACTER, "Próba wyprowadzenia znaku wartości całkowitej {0}, który nie jest reprezentowany w podanym kodowaniu wyjściowym {1}."};
        contents[14] = new Object[]{MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY, "Nie można załadować pliku właściwości ''{0}'' dla metody wyjściowej ''{1}'' (sprawdź CLASSPATH)"};
        contents[15] = new Object[]{"ER_INVALID_PORT", "Niepoprawny numer portu"};
        contents[16] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "Nie można ustawić portu, kiedy host jest pusty"};
        contents[17] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "Host nie jest poprawnie skonstruowanym adresem"};
        contents[18] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "Schemat nie jest zgodny."};
        contents[19] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Nie można ustawić schematu z pustego ciągu znaków"};
        contents[20] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "Ścieżka zawiera nieznaną sekwencję o zmienionym znaczeniu"};
        contents[21] = new Object[]{"ER_PATH_INVALID_CHAR", "Ścieżka zawiera niepoprawny znak {0}"};
        contents[22] = new Object[]{"ER_FRAG_INVALID_CHAR", "Fragment zawiera niepoprawny znak"};
        contents[23] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "Nie można ustawić fragmentu, kiedy ścieżka jest pusta"};
        contents[24] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Fragment można ustawić tylko dla ogólnego URI"};
        contents[25] = new Object[]{"ER_NO_SCHEME_IN_URI", "Nie znaleziono schematu w URI"};
        contents[26] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "Nie można zainicjować URI z pustymi parametrami"};
        contents[27] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Nie można podać fragmentu jednocześnie w ścieżce i fragmencie"};
        contents[28] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "Tekstu zapytania nie można podać w tekście ścieżki i zapytania"};
        contents[29] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "Nie można podać portu, jeśli nie podano hosta"};
        contents[30] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Nie można podać informacji o użytkowniku, jeśli nie podano hosta"};
        contents[31] = new Object[]{MsgKey.ER_XML_VERSION_NOT_SUPPORTED, "Ostrzeżenie:  Wymaganą wersją dokumentu wyjściowego jest ''{0}''.  Ta wersja XML nie jest obsługiwana.  Wersją dokumentu wyjściowego będzie ''1.0''."};
        contents[32] = new Object[]{"ER_SCHEME_REQUIRED", "Schemat jest wymagany!"};
        contents[33] = new Object[]{MsgKey.ER_FACTORY_PROPERTY_MISSING, "Obiekt klasy Properties przekazany do klasy SerializerFactory nie ma właściwości ''{0}''."};
        contents[34] = new Object[]{"ER_ENCODING_NOT_SUPPORTED", "Ostrzeżenie:  dekodowany ''{0}'' nie jest obsługiwany przez środowisko wykonawcze Java."};
        contents[35] = new Object[]{"FEATURE_NOT_FOUND", "Parametr ''{0}'' nie został rozpoznany."};
        contents[36] = new Object[]{"FEATURE_NOT_SUPPORTED", "Parametr ''{0}'' został rozpoznany, ale nie można ustawić żądanej wartości."};
        contents[37] = new Object[]{MsgKey.ER_STRING_TOO_LONG, "Wynikowy łańcuch jest zbyt długi, aby się zmieścić w obiekcie DOMString: ''{0}''."};
        contents[38] = new Object[]{MsgKey.ER_TYPE_MISMATCH_ERR, "Typ wartości parametru o tej nazwie jest niezgodny z oczekiwanym typem wartości. "};
        contents[39] = new Object[]{MsgKey.ER_NO_OUTPUT_SPECIFIED, "Docelowe miejsce zapisu danych wyjściowych było puste (null)."};
        contents[40] = new Object[]{MsgKey.ER_UNSUPPORTED_ENCODING, "Napotkano nieobsługiwane kodowanie."};
        contents[41] = new Object[]{MsgKey.ER_UNABLE_TO_SERIALIZE_NODE, "Nie można przekształcić węzła do postaci szeregowej."};
        contents[42] = new Object[]{MsgKey.ER_CDATA_SECTIONS_SPLIT, "Sekcja CDATA zawiera jeden lub kilka znaczników zakończenia ']]>'."};
        contents[43] = new Object[]{MsgKey.ER_WARNING_WF_NOT_CHECKED, "Nie można utworzyć instancji obiektu sprawdzającego Well-Formedness.  Parametr well-formed ustawiono na wartość true, ale nie można było dokonać sprawdzenia poprawności konstrukcji."};
        contents[44] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER, "Węzeł ''{0}'' zawiera niepoprawne znaki XML."};
        contents[45] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT, "W komentarzu znaleziono niepoprawny znak XML (Unicode: 0x{0})."};
        contents[46] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_PI, "W danych instrukcji przetwarzania znaleziono niepoprawny znak XML (Unicode: 0x{0})."};
        contents[47] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA, "W sekcji CDATA znaleziono niepoprawny znak XML (Unicode: 0x{0})."};
        contents[48] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT, "W treści danych znakowych węzła znaleziono niepoprawny znak XML (Unicode: 0x{0})."};
        contents[49] = new Object[]{MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME, "W {0} o nazwie ''{1}'' znaleziono niepoprawne znaki XML."};
        contents[50] = new Object[]{MsgKey.ER_WF_DASH_IN_COMMENT, "Ciąg znaków \"--\" jest niedozwolony w komentarzu."};
        contents[51] = new Object[]{MsgKey.ER_WF_LT_IN_ATTVAL, "Wartość atrybutu \"{1}\" związanego z typem elementu \"{0}\" nie może zawierać znaku ''<''."};
        contents[52] = new Object[]{MsgKey.ER_WF_REF_TO_UNPARSED_ENT, "Odwołanie do encji nieprzetwarzanej \"&{0};\" jest niedozwolone."};
        contents[53] = new Object[]{MsgKey.ER_WF_REF_TO_EXTERNAL_ENT, "Odwołanie do zewnętrznej encji \"&{0};\" jest niedozwolone w wartości atrybutu."};
        contents[54] = new Object[]{MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND, "Nie można związać przedrostka \"{0}\" z przestrzenią nazw \"{1}\"."};
        contents[55] = new Object[]{MsgKey.ER_NULL_LOCAL_ELEMENT_NAME, "Nazwa lokalna elementu \"{0}\" jest pusta (null)."};
        contents[56] = new Object[]{MsgKey.ER_NULL_LOCAL_ATTR_NAME, "Nazwa lokalna atrybutu \"{0}\" jest pusta (null)."};
        contents[57] = new Object[]{"unbound-prefix-in-entity-reference", "Tekst zastępujący węzła encji \"{0}\" zawiera węzeł elementu \"{1}\" o niezwiązanym przedrostku \"{2}\"."};
        contents[58] = new Object[]{"unbound-prefix-in-entity-reference", "Tekst zastępujący węzła encji \"{0}\" zawiera węzeł atrybutu \"{1}\" o niezwiązanym przedrostku \"{2}\"."};
        return contents;
    }
}
