package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;

public class SerializerMessages_sv extends ListResourceBundle {
    public Object[][] getContents() {
        Object[][] contents = new Object[17][];
        contents[0] = new Object[]{MsgKey.ER_INVALID_PORT, "Ogiltigt portnummer"};
        contents[1] = new Object[]{MsgKey.ER_PORT_WHEN_HOST_NULL, "Port kan inte s\u00e4ttas n\u00e4r v\u00e4rd \u00e4r null"};
        contents[2] = new Object[]{MsgKey.ER_HOST_ADDRESS_NOT_WELLFORMED, "V\u00e4rd \u00e4r inte en v\u00e4lformulerad adress"};
        contents[3] = new Object[]{MsgKey.ER_SCHEME_NOT_CONFORMANT, "Schemat \u00e4r inte likformigt."};
        contents[4] = new Object[]{MsgKey.ER_SCHEME_FROM_NULL_STRING, "Kan inte s\u00e4tta schema fr\u00e5n null-str\u00e4ng"};
        contents[5] = new Object[]{MsgKey.ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE, "V\u00e4g inneh\u00e5ller ogiltig flyktsekvens"};
        contents[6] = new Object[]{MsgKey.ER_PATH_INVALID_CHAR, "V\u00e4g inneh\u00e5ller ogiltigt tecken: {0}"};
        contents[7] = new Object[]{MsgKey.ER_FRAG_INVALID_CHAR, "Fragment inneh\u00e5ller ogiltigt tecken"};
        contents[8] = new Object[]{MsgKey.ER_FRAG_WHEN_PATH_NULL, "Fragment kan inte s\u00e4ttas n\u00e4r v\u00e4g \u00e4r null"};
        contents[9] = new Object[]{MsgKey.ER_FRAG_FOR_GENERIC_URI, "Fragment kan bara s\u00e4ttas f\u00f6r en allm\u00e4n URI"};
        contents[10] = new Object[]{MsgKey.ER_NO_SCHEME_IN_URI, "Schema saknas i URI: {0}"};
        contents[11] = new Object[]{MsgKey.ER_CANNOT_INIT_URI_EMPTY_PARMS, "Kan inte initialisera URI med tomma parametrar"};
        contents[12] = new Object[]{MsgKey.ER_NO_FRAGMENT_STRING_IN_PATH, "Fragment kan inte anges i b\u00e5de v\u00e4gen och fragmentet"};
        contents[13] = new Object[]{MsgKey.ER_NO_QUERY_STRING_IN_PATH, "F\u00f6rfr\u00e5gan-str\u00e4ng kan inte anges i v\u00e4g och f\u00f6rfr\u00e5gan-str\u00e4ng"};
        contents[14] = new Object[]{MsgKey.ER_NO_PORT_IF_NO_HOST, "Port f\u00e5r inte anges om v\u00e4rden inte \u00e4r angiven"};
        contents[15] = new Object[]{MsgKey.ER_NO_USERINFO_IF_NO_HOST, "Userinfo f\u00e5r inte anges om v\u00e4rden inte \u00e4r angiven"};
        contents[16] = new Object[]{MsgKey.ER_SCHEME_REQUIRED, "Schema kr\u00e4vs!"};
        return contents;
    }
}
