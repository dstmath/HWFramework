package org.apache.http.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.util.CharArrayBuffer;

@Deprecated
public class HeaderGroup implements Cloneable {
    private List headers = new ArrayList(16);

    public void clear() {
        this.headers.clear();
    }

    public void addHeader(Header header) {
        if (header != null) {
            this.headers.add(header);
        }
    }

    public void removeHeader(Header header) {
        if (header != null) {
            this.headers.remove(header);
        }
    }

    public void updateHeader(Header header) {
        if (header != null) {
            for (int i = 0; i < this.headers.size(); i++) {
                if (((Header) this.headers.get(i)).getName().equalsIgnoreCase(header.getName())) {
                    this.headers.set(i, header);
                    return;
                }
            }
            this.headers.add(header);
        }
    }

    public void setHeaders(Header[] headers2) {
        clear();
        if (headers2 != null) {
            for (Header header : headers2) {
                this.headers.add(header);
            }
        }
    }

    public Header getCondensedHeader(String name) {
        Header[] headers2 = getHeaders(name);
        if (headers2.length == 0) {
            return null;
        }
        if (headers2.length == 1) {
            return headers2[0];
        }
        CharArrayBuffer valueBuffer = new CharArrayBuffer(128);
        valueBuffer.append(headers2[0].getValue());
        for (int i = 1; i < headers2.length; i++) {
            valueBuffer.append(", ");
            valueBuffer.append(headers2[i].getValue());
        }
        return new BasicHeader(name.toLowerCase(Locale.ENGLISH), valueBuffer.toString());
    }

    public Header[] getHeaders(String name) {
        ArrayList headersFound = new ArrayList();
        for (int i = 0; i < this.headers.size(); i++) {
            Header header = (Header) this.headers.get(i);
            if (header.getName().equalsIgnoreCase(name)) {
                headersFound.add(header);
            }
        }
        return (Header[]) headersFound.toArray(new Header[headersFound.size()]);
    }

    public Header getFirstHeader(String name) {
        for (int i = 0; i < this.headers.size(); i++) {
            Header header = (Header) this.headers.get(i);
            if (header.getName().equalsIgnoreCase(name)) {
                return header;
            }
        }
        return null;
    }

    public Header getLastHeader(String name) {
        for (int i = this.headers.size() - 1; i >= 0; i--) {
            Header header = (Header) this.headers.get(i);
            if (header.getName().equalsIgnoreCase(name)) {
                return header;
            }
        }
        return null;
    }

    public Header[] getAllHeaders() {
        List list = this.headers;
        return (Header[]) list.toArray(new Header[list.size()]);
    }

    public boolean containsHeader(String name) {
        for (int i = 0; i < this.headers.size(); i++) {
            if (((Header) this.headers.get(i)).getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public HeaderIterator iterator() {
        return new BasicListHeaderIterator(this.headers, null);
    }

    public HeaderIterator iterator(String name) {
        return new BasicListHeaderIterator(this.headers, name);
    }

    public HeaderGroup copy() {
        HeaderGroup clone = new HeaderGroup();
        clone.headers.addAll(this.headers);
        return clone;
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        HeaderGroup clone = (HeaderGroup) super.clone();
        clone.headers = new ArrayList(this.headers);
        return clone;
    }
}
