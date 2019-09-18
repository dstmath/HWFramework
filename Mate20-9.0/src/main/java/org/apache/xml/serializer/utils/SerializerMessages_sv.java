package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_sv extends ListResourceBundle {
    public Object[][] getContents() {
        return new Object[][]{new Object[]{"ER_INVALID_PORT", "Ogiltigt portnummer"}, new Object[]{"ER_PORT_WHEN_HOST_NULL", "Port kan inte sättas när värd är null"}, new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "Värd är inte en välformulerad adress"}, new Object[]{"ER_SCHEME_NOT_CONFORMANT", "Schemat är inte likformigt."}, new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Kan inte sätta schema från null-sträng"}, new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "Väg innehåller ogiltig flyktsekvens"}, new Object[]{"ER_PATH_INVALID_CHAR", "Väg innehåller ogiltigt tecken: {0}"}, new Object[]{"ER_FRAG_INVALID_CHAR", "Fragment innehåller ogiltigt tecken"}, new Object[]{"ER_FRAG_WHEN_PATH_NULL", "Fragment kan inte sättas när väg är null"}, new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Fragment kan bara sättas för en allmän URI"}, new Object[]{"ER_NO_SCHEME_IN_URI", "Schema saknas i URI: {0}"}, new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "Kan inte initialisera URI med tomma parametrar"}, new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Fragment kan inte anges i både vägen och fragmentet"}, new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "Förfrågan-sträng kan inte anges i väg och förfrågan-sträng"}, new Object[]{"ER_NO_PORT_IF_NO_HOST", "Port får inte anges om värden inte är angiven"}, new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Userinfo får inte anges om värden inte är angiven"}, new Object[]{"ER_SCHEME_REQUIRED", "Schema krävs!"}};
    }
}
