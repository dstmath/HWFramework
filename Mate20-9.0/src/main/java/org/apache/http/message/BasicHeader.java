package org.apache.http.message;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class BasicHeader implements Header, Cloneable {
    private final String name;
    private final String value;

    public BasicHeader(String name2, String value2) {
        if (name2 != null) {
            this.name = name2;
            this.value = value2;
            return;
        }
        throw new IllegalArgumentException("Name may not be null");
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return BasicLineFormatter.DEFAULT.formatHeader((CharArrayBuffer) null, (Header) this).toString();
    }

    public HeaderElement[] getElements() throws ParseException {
        if (this.value != null) {
            return BasicHeaderValueParser.parseElements(this.value, (HeaderValueParser) null);
        }
        return new HeaderElement[0];
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
