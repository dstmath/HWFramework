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

    @Override // org.apache.http.Header
    public String getName() {
        return this.name;
    }

    @Override // org.apache.http.Header
    public String getValue() {
        return this.value;
    }

    @Override // java.lang.Object
    public String toString() {
        return BasicLineFormatter.DEFAULT.formatHeader((CharArrayBuffer) null, this).toString();
    }

    @Override // org.apache.http.Header
    public HeaderElement[] getElements() throws ParseException {
        String str = this.value;
        if (str != null) {
            return BasicHeaderValueParser.parseElements(str, (HeaderValueParser) null);
        }
        return new HeaderElement[0];
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
