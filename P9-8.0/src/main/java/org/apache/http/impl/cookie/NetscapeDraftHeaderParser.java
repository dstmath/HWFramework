package org.apache.http.impl.cookie;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.message.BasicHeaderElement;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class NetscapeDraftHeaderParser {
    public static final NetscapeDraftHeaderParser DEFAULT = new NetscapeDraftHeaderParser();
    private static final char[] DELIMITERS = new char[]{';'};
    private final BasicHeaderValueParser nvpParser = BasicHeaderValueParser.DEFAULT;

    public HeaderElement parseHeader(CharArrayBuffer buffer, ParserCursor cursor) throws ParseException {
        if (buffer == null) {
            throw new IllegalArgumentException("Char array buffer may not be null");
        } else if (cursor == null) {
            throw new IllegalArgumentException("Parser cursor may not be null");
        } else {
            NameValuePair nvp = this.nvpParser.parseNameValuePair(buffer, cursor, DELIMITERS);
            List<NameValuePair> params = new ArrayList();
            while (!cursor.atEnd()) {
                params.add(this.nvpParser.parseNameValuePair(buffer, cursor, DELIMITERS));
            }
            return new BasicHeaderElement(nvp.getName(), nvp.getValue(), (NameValuePair[]) params.toArray(new NameValuePair[params.size()]));
        }
    }
}
