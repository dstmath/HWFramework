package ohos.com.sun.org.apache.xalan.internal.xsltc.runtime;

import ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer;
import ohos.org.xml.sax.SAXException;

public final class StringValueHandler extends EmptySerializer {
    private static final String EMPTY_STR = "";
    private StringBuilder _buffer = new StringBuilder();
    private int _nestedLevel = 0;
    private String _str = null;
    private boolean m_escaping = false;

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer
    public void characters(char[] cArr, int i, int i2) throws SAXException {
        if (this._nestedLevel <= 0) {
            String str = this._str;
            if (str != null) {
                this._buffer.append(str);
                this._str = null;
            }
            this._buffer.append(cArr, i, i2);
        }
    }

    public String getValue() {
        if (this._buffer.length() != 0) {
            String sb = this._buffer.toString();
            this._buffer.setLength(0);
            return sb;
        }
        String str = this._str;
        this._str = null;
        return str != null ? str : "";
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void characters(String str) throws SAXException {
        if (this._nestedLevel <= 0) {
            if (this._str == null && this._buffer.length() == 0) {
                this._str = str;
                return;
            }
            String str2 = this._str;
            if (str2 != null) {
                this._buffer.append(str2);
                this._str = null;
            }
            this._buffer.append(str);
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void startElement(String str) throws SAXException {
        this._nestedLevel++;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.ExtendedContentHandler
    public void endElement(String str) throws SAXException {
        this._nestedLevel--;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.EmptySerializer, ohos.com.sun.org.apache.xml.internal.serializer.SerializationHandler
    public boolean setEscaping(boolean z) {
        boolean z2 = this.m_escaping;
        this.m_escaping = z;
        return z;
    }

    public String getValueOfPI() {
        String value = getValue();
        if (value.indexOf("?>") <= 0) {
            return value;
        }
        int length = value.length();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < length) {
            int i2 = i + 1;
            char charAt = value.charAt(i);
            if (charAt == '?' && value.charAt(i2) == '>') {
                sb.append("? >");
                i2++;
            } else {
                sb.append(charAt);
            }
            i = i2;
        }
        return sb.toString();
    }
}
