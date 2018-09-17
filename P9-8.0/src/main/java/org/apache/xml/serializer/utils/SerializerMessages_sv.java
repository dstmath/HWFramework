package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_sv extends ListResourceBundle {
    public Object[][] getContents() {
        Object[][] contents = new Object[17][];
        contents[0] = new Object[]{"ER_INVALID_PORT", "Ogiltigt portnummer"};
        contents[1] = new Object[]{"ER_PORT_WHEN_HOST_NULL", "Port kan inte sättas när värd är null"};
        contents[2] = new Object[]{"ER_HOST_ADDRESS_NOT_WELLFORMED", "Värd är inte en välformulerad adress"};
        contents[3] = new Object[]{"ER_SCHEME_NOT_CONFORMANT", "Schemat är inte likformigt."};
        contents[4] = new Object[]{"ER_SCHEME_FROM_NULL_STRING", "Kan inte sätta schema från null-sträng"};
        contents[5] = new Object[]{"ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE", "Väg innehåller ogiltig flyktsekvens"};
        contents[6] = new Object[]{"ER_PATH_INVALID_CHAR", "Väg innehåller ogiltigt tecken: {0}"};
        contents[7] = new Object[]{"ER_FRAG_INVALID_CHAR", "Fragment innehåller ogiltigt tecken"};
        contents[8] = new Object[]{"ER_FRAG_WHEN_PATH_NULL", "Fragment kan inte sättas när väg är null"};
        contents[9] = new Object[]{"ER_FRAG_FOR_GENERIC_URI", "Fragment kan bara sättas för en allmän URI"};
        contents[10] = new Object[]{"ER_NO_SCHEME_IN_URI", "Schema saknas i URI: {0}"};
        contents[11] = new Object[]{"ER_CANNOT_INIT_URI_EMPTY_PARMS", "Kan inte initialisera URI med tomma parametrar"};
        contents[12] = new Object[]{"ER_NO_FRAGMENT_STRING_IN_PATH", "Fragment kan inte anges i både vägen och fragmentet"};
        contents[13] = new Object[]{"ER_NO_QUERY_STRING_IN_PATH", "Förfrågan-sträng kan inte anges i väg och förfrågan-sträng"};
        contents[14] = new Object[]{"ER_NO_PORT_IF_NO_HOST", "Port får inte anges om värden inte är angiven"};
        contents[15] = new Object[]{"ER_NO_USERINFO_IF_NO_HOST", "Userinfo får inte anges om värden inte är angiven"};
        contents[16] = new Object[]{"ER_SCHEME_REQUIRED", "Schema krävs!"};
        return contents;
    }
}
